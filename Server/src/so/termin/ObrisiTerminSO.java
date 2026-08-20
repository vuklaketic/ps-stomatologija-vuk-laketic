package so.termin;

import model.StavkaTermina;
import model.Termin;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija brisanja termina zajedno sa njegovim stavkama.
 *
 * @author vukla
 */
public class ObrisiTerminSO extends OpstaSistemskaOperacija {

    @Override
    protected void preduslovi(Object objekat) throws Exception {
        if (objekat == null || !(objekat instanceof Termin)) {
            throw new Exception("Nije prosledjen parametar odgovarajuceg tipa.");
        }

        Termin termin = (Termin) objekat;
        if (termin.getIdTermin() <= 0) {
            throw new Exception("Nije prosledjen identifikator termina.");
        }

        String uslov = Termin.SPOJEVI + " WHERE termin.idTermin = " + termin.getIdTermin();
        if (broker.vratiObjekat(new Termin(), uslov) == null) {
            throw new Exception("Termin koji se brise ne postoji u bazi.");
        }
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        Termin termin = (Termin) objekat;

        broker.obrisiPoUpitu(new StavkaTermina(), "idTermin = " + termin.getIdTermin());
        broker.obrisi(termin);
    }
}
