package so.termin;

import java.util.ArrayList;
import java.util.List;
import model.ApstraktniDomenskiObjekat;
import model.StavkaTermina;
import model.Termin;
import model.Usluga;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija koja vraca listu termina. Termin prosledjen kao
 * parametar sluzi kao kriterijum pretrage - svako popunjeno polje (stomatolog,
 * datum, status, pacijent, usluga u stavci) suzava pretragu, a prazna se
 * ignorisu, pa bez ijednog popunjenog polja vraca sve termine.
 *
 * @author vukla
 */
public class VratiListuTerminaSO extends OpstaSistemskaOperacija {

    private List<Termin> lista;

    public List<Termin> getLista() {
        return lista;
    }

    @Override
    protected void preduslovi(Object objekat) throws Exception {
        if (objekat == null || !(objekat instanceof Termin)) {
            throw new Exception("Nije prosleđen parametar odgovarajućeg tipa.");
        }
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        Termin kriterijum = (Termin) objekat;

        String uslov = Termin.SPOJEVI + sastaviUslov(kriterijum)
                + " ORDER BY termin.datum, termin.vreme";

        lista = new ArrayList<>();
        for (ApstraktniDomenskiObjekat ado : broker.vratiPoUpitu(new Termin(), uslov)) {
            lista.add((Termin) ado);
        }

        for (Termin termin : lista) {
            ucitajStavke(termin);
        }
    }

    /**
     * Sastavlja WHERE klauzu od popunjenih polja kriterijuma. Sastavlja se
     * ovde jer klijent ne poznaje tabele ni kolone baze; ulaze samo datum,
     * status i identifikatori, koji po tipu ne mogu sadrzati apostrof.
     *
     * @return WHERE klauza sa vodecim razmakom, ili prazan string ako nijedno
     * polje kriterijuma nije popunjeno
     */
    private String sastaviUslov(Termin kriterijum) {
        List<String> uslovi = new ArrayList<>();

        if (kriterijum.getStomatolog() != null && kriterijum.getStomatolog().getIdStomatolog() > 0) {
            uslovi.add("termin.idStomatolog = " + kriterijum.getStomatolog().getIdStomatolog());
        }
        if (kriterijum.getDatum() != null) {
            uslovi.add("termin.datum = '" + kriterijum.getDatum() + "'");
        }
        if (kriterijum.getStatus() != null) {
            uslovi.add("termin.status = '" + kriterijum.getStatus().name() + "'");
        }
        if (kriterijum.getPacijent() != null && kriterijum.getPacijent().getIdPacijent() > 0) {
            uslovi.add("termin.idPacijent = " + kriterijum.getPacijent().getIdPacijent());
        }

        // podupit nad stavkama, da termin sa vise stavki ostane jedan red
        Usluga usluga = vratiUsluguKriterijuma(kriterijum);
        if (usluga != null) {
            uslovi.add("EXISTS (SELECT 1 FROM stavkatermina"
                    + " WHERE stavkatermina.idTermin = termin.idTermin"
                    + " AND stavkatermina.idUsluga = " + usluga.getIdUsluga() + ")");
        }

        if (uslovi.isEmpty()) {
            return "";
        }
        return " WHERE " + String.join(" AND ", uslovi);
    }

    /**
     * Vraca uslugu po kojoj se pretrazuje, koju klijent salje kao stavku
     * kriterijuma.
     *
     * @return usluga iz prve stavke kriterijuma ili null ako usluga nije zadata
     */
    private Usluga vratiUsluguKriterijuma(Termin kriterijum) {
        if (kriterijum.getStavke() == null) {
            return null;
        }
        for (StavkaTermina stavka : kriterijum.getStavke()) {
            if (stavka.getUsluga() != null && stavka.getUsluga().getIdUsluga() > 0) {
                return stavka.getUsluga();
            }
        }
        return null;
    }

    /** Ucitava stavke termina posebnim upitom, da spojeni upit ne bi umnozio redove termina. */
    private void ucitajStavke(Termin termin) throws Exception {
        String uslov = StavkaTermina.SPOJEVI
                + " WHERE stavkatermina.idTermin = " + termin.getIdTermin()
                + " ORDER BY stavkatermina.rb";

        List<StavkaTermina> stavke = new ArrayList<>();
        for (ApstraktniDomenskiObjekat ado : broker.vratiPoUpitu(new StavkaTermina(), uslov)) {
            StavkaTermina stavka = (StavkaTermina) ado;
            stavka.setTermin(termin);
            stavke.add(stavka);
        }
        termin.setStavke(stavke);
    }

    @Override
    protected String porukaONeuspehu() {
        return "Sistem ne može da nađe termine po zadatim kriterijumima.";
    }
}
