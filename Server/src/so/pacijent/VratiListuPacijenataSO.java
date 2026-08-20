package so.pacijent;

import java.util.ArrayList;
import java.util.List;
import model.ApstraktniDomenskiObjekat;
import model.Pacijent;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija koja vraca listu svih pacijenata sa njihovim ordinacijama.
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
        // operacija nema parametar, pa nema ni preduslova
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        lista = new ArrayList<>();
        String uslov = Pacijent.SPOJEVI + " ORDER BY pacijent.prezime, pacijent.ime";
        for (ApstraktniDomenskiObjekat ado : broker.vratiPoUpitu(new Pacijent(), uslov)) {
            lista.add((Pacijent) ado);
        }
    }
}
