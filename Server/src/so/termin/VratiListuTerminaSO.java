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
 * Kao parametar dobija termin koji sluzi kao kriterijum pretrage: ako ima
 * postavljenog stomatologa, vracaju se samo njegovi termini, a u suprotnom svi.
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
            throw new Exception("Nije prosledjen parametar odgovarajuceg tipa.");
        }
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        Termin kriterijum = (Termin) objekat;

        String uslov = Termin.SPOJEVI;
        if (kriterijum.getStomatolog() != null && kriterijum.getStomatolog().getIdStomatolog() > 0) {
            uslov += " WHERE termin.idStomatolog = " + kriterijum.getStomatolog().getIdStomatolog();
        }
        uslov += " ORDER BY termin.datum, termin.vreme";

        lista = new ArrayList<>();
        for (ApstraktniDomenskiObjekat ado : broker.vratiPoUpitu(new Termin(), uslov)) {
            lista.add((Termin) ado);
        }

        for (Termin termin : lista) {
            ucitajStavke(termin);
        }
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
}
