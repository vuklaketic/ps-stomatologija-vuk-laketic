package so.termin;

import java.util.ArrayList;
import java.util.List;
import model.ApstraktniDomenskiObjekat;
import model.StavkaTermina;
import model.Termin;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija koja vraca listu termina.
 *
 * Kao parametar dobija termin koji sluzi kao kriterijum pretrage. Svako
 * popunjeno polje kriterijuma sazuje pretragu, a polja koja su ostala prazna
 * se ne uzimaju u obzir: postavljen stomatolog ogranicava pretragu na njegove
 * termine, datum na jedan dan, status na jedno stanje, a pacijent na termine
 * tog pacijenta. Ako nijedno polje nije popunjeno, vracaju se svi termini.
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
     * Sastavlja WHERE klauzu od popunjenih polja kriterijuma.
     *
     * Uslov se sastavlja ovde, na serveru, jer klijent ne poznaje ni tabele ni
     * kolone baze. U upit ulaze samo datum, naziv statusa i identifikatori -
     * dakle vrednosti koje po tipu ne mogu da sadrze apostrof, pa ovde nema
     * mesta za ubacivanje tudjeg SQL koda.
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

        if (uslovi.isEmpty()) {
            return "";
        }
        return " WHERE " + String.join(" AND ", uslovi);
    }

    /**
     * Ucitava stavke jednog termina. Stavke se citaju posebnim upitom, jer je
     * termin prema stavci u odnosu jedan prema vise, pa bi jedan spojeni upit
     * umnozio redove termina.
     */
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
