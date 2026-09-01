package so.pacijent;

import java.util.ArrayList;
import java.util.List;
import model.ApstraktniDomenskiObjekat;
import model.Pacijent;
import so.OpstaSistemskaOperacija;
import util.SqlUtil;

/**
 * Sistemska operacija koja vraca listu pacijenata sa njihovim ordinacijama.
 *
 * Kao parametar moze da dobije pacijenta koji sluzi kao kriterijum pretrage.
 * Popunjeno ime ili prezime trazi se kao deo imena, a postavljena ordinacija
 * ogranicava pretragu na pacijente te ordinacije. Ako parametar nije prosledjen
 * ili nijedno polje nije popunjeno, vracaju se svi pacijenti.
 *
 * @author vukla
 */
public class VratiListuPacijenataSO extends OpstaSistemskaOperacija {

    private List<Pacijent> lista;

    public List<Pacijent> getLista() {
        return lista;
    }

    @Override
    protected void preduslovi(Object objekat) throws Exception {
        if (objekat != null && !(objekat instanceof Pacijent)) {
            throw new Exception("Nije prosleđen parametar odgovarajućeg tipa.");
        }
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        String uslov = Pacijent.SPOJEVI + sastaviUslov((Pacijent) objekat)
                + " ORDER BY pacijent.prezime, pacijent.ime";

        lista = new ArrayList<>();
        for (ApstraktniDomenskiObjekat ado : broker.vratiPoUpitu(new Pacijent(), uslov)) {
            lista.add((Pacijent) ado);
        }
    }

    /**
     * Sastavlja WHERE klauzu od popunjenih polja kriterijuma. Uslov se sastavlja
     * na serveru, jer klijent ne poznaje ni tabele ni kolone baze.
     *
     * @return WHERE klauza sa vodecim razmakom, ili prazan string ako kriterijum
     * nije zadat
     */
    private String sastaviUslov(Pacijent kriterijum) {
        if (kriterijum == null) {
            return "";
        }

        List<String> uslovi = new ArrayList<>();

        if (kriterijum.getIme() != null && !kriterijum.getIme().trim().isEmpty()) {
            uslovi.add("pacijent.ime LIKE '%" + SqlUtil.escapiraj(kriterijum.getIme().trim()) + "%'");
        }
        if (kriterijum.getPrezime() != null && !kriterijum.getPrezime().trim().isEmpty()) {
            uslovi.add("pacijent.prezime LIKE '%" + SqlUtil.escapiraj(kriterijum.getPrezime().trim()) + "%'");
        }
        if (kriterijum.getOrdinacija() != null && kriterijum.getOrdinacija().getIdOrdinacija() > 0) {
            uslovi.add("pacijent.idOrdinacija = " + kriterijum.getOrdinacija().getIdOrdinacija());
        }

        if (uslovi.isEmpty()) {
            return "";
        }
        return " WHERE " + String.join(" AND ", uslovi);
    }

    @Override
    protected String porukaONeuspehu() {
        return "Sistem ne može da nađe pacijente po zadatim kriterijumima.";
    }
}
