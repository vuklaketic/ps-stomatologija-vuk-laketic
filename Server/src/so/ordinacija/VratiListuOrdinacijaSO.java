package so.ordinacija;

import java.util.ArrayList;
import java.util.List;
import model.ApstraktniDomenskiObjekat;
import model.Ordinacija;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija koja vraca listu svih ordinacija.
 *
 * @author vukla
 */
public class VratiListuOrdinacijaSO extends OpstaSistemskaOperacija {

    private List<Ordinacija> lista;

    public List<Ordinacija> getLista() {
        return lista;
    }

    @Override
    protected void preduslovi(Object objekat) throws Exception {
        // operacija nema parametar, pa nema ni preduslova
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        lista = new ArrayList<>();
        for (ApstraktniDomenskiObjekat ado : broker.vratiPoUpitu(new Ordinacija(), " ORDER BY naziv")) {
            lista.add((Ordinacija) ado);
        }
    }

    @Override
    protected String porukaONeuspehu() {
        return "Sistem ne moze da ucita listu ordinacija.";
    }
}
