package so.stomatolog;

import java.util.ArrayList;
import java.util.List;
import model.ApstraktniDomenskiObjekat;
import model.Stomatolog;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija koja vraca listu svih stomatologa.
 *
 * @author vukla
 */
public class VratiListuStomatologaSO extends OpstaSistemskaOperacija {

    private List<Stomatolog> lista;

    public List<Stomatolog> getLista() {
        return lista;
    }

    @Override
    protected void preduslovi(Object objekat) throws Exception {
        // operacija nema parametar, pa nema ni preduslova
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        lista = new ArrayList<>();
        String uslov = " ORDER BY stomatolog.prezime, stomatolog.ime";
        for (ApstraktniDomenskiObjekat ado : broker.vratiPoUpitu(new Stomatolog(), uslov)) {
            lista.add((Stomatolog) ado);
        }
    }

    @Override
    protected String porukaONeuspehu() {
        return "Sistem ne može da učita listu stomatologa.";
    }
}
