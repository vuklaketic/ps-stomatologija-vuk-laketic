package so.pacijent;

import java.sql.SQLIntegrityConstraintViolationException;
import model.Pacijent;
import model.Termin;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija brisanja pacijenta. Pacijent koji ima zakazane termine
 * ne moze da se obrise (termin bi ostao bez pacijenta); isto cuva i strani
 * kljuc u bazi, pa se i njegovo krsenje prevodi u istu poruku.
 *
 * @author vukla
 */
public class ObrisiPacijentSO extends OpstaSistemskaOperacija {

    private static final String PORUKA_IMA_TERMINE =
            "Sistem ne može da obriše pacijenta - pacijent ima zakazane termine.";

    @Override
    protected void preduslovi(Object objekat) throws Exception {
        if (objekat == null || !(objekat instanceof Pacijent)) {
            throw new Exception("Nije prosleđen parametar odgovarajućeg tipa.");
        }

        Pacijent pacijent = (Pacijent) objekat;
        if (pacijent.getIdPacijent() <= 0) {
            throw new Exception("Nije prosleđen identifikator pacijenta.");
        }

        String uslovPostoji = Pacijent.SPOJEVI + " WHERE pacijent.idPacijent = " + pacijent.getIdPacijent();
        if (broker.vratiObjekat(new Pacijent(), uslovPostoji) == null) {
            throw new Exception("Pacijent koji se briše ne postoji u bazi.");
        }

        String uslovTermini = Termin.SPOJEVI + " WHERE termin.idPacijent = " + pacijent.getIdPacijent();
        if (!broker.vratiPoUpitu(new Termin(), uslovTermini).isEmpty()) {
            throw new Exception(PORUKA_IMA_TERMINE);
        }
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        Pacijent pacijent = (Pacijent) objekat;

        try {
            broker.obrisi(pacijent);
        } catch (SQLIntegrityConstraintViolationException ex) {
            // zastita ako je termin ubacen izmedju provere i brisanja
            throw new Exception(PORUKA_IMA_TERMINE);
        }
    }

    @Override
    protected String porukaONeuspehu() {
        return "Sistem ne može da obriše pacijenta.";
    }
}
