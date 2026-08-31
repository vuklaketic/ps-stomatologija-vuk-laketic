package so.usluga;

import java.util.ArrayList;
import java.util.List;
import model.ApstraktniDomenskiObjekat;
import model.Usluga;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija koja vraca listu svih usluga.
 *
 * @author vukla
 */
public class VratiListuUslugaSO extends OpstaSistemskaOperacija {

    private List<Usluga> lista;

    public List<Usluga> getLista() {
        return lista;
    }

    @Override
    protected void preduslovi(Object objekat) throws Exception {
        // operacija nema parametar, pa nema ni preduslova
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        lista = new ArrayList<>();
        String uslov = " ORDER BY usluga.naziv";
        for (ApstraktniDomenskiObjekat ado : broker.vratiPoUpitu(new Usluga(), uslov)) {
            lista.add((Usluga) ado);
        }
    }

    @Override
    protected String porukaONeuspehu() {
        return "Sistem ne moze da ucita listu usluga.";
    }
}
