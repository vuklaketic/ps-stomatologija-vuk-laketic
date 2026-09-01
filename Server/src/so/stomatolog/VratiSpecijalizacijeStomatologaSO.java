package so.stomatolog;

import java.util.ArrayList;
import java.util.List;
import model.ApstraktniDomenskiObjekat;
import model.Specijalizacija;
import model.Stomatolog;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija koja vraca specijalizacije jednog stomatologa.
 *
 * Kao parametar dobija stomatologa cije se specijalizacije traze, a citaju se
 * spajanjem tabele specijalizacija sa veznom tabelom stspec. Stomatolog koji
 * nema nijednu specijalizaciju nije greska - za njega se vraca prazna lista.
 *
 * @author vukla
 */
public class VratiSpecijalizacijeStomatologaSO extends OpstaSistemskaOperacija {

    private List<Specijalizacija> lista;

    public List<Specijalizacija> getLista() {
        return lista;
    }

    @Override
    protected void preduslovi(Object objekat) throws Exception {
        if (objekat == null || !(objekat instanceof Stomatolog)) {
            throw new Exception("Nije prosledjen parametar odgovarajuceg tipa.");
        }
        if (((Stomatolog) objekat).getIdStomatolog() <= 0) {
            throw new Exception("Nije prosledjen stomatolog sa identifikatorom.");
        }
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        Stomatolog stomatolog = (Stomatolog) objekat;

        String uslov = Specijalizacija.SPOJ_SA_STOMATOLOGOM
                + " WHERE stspec.idStomatolog = " + stomatolog.getIdStomatolog()
                + " ORDER BY specijalizacija.naziv";

        lista = new ArrayList<>();
        for (ApstraktniDomenskiObjekat ado : broker.vratiPoUpitu(new Specijalizacija(), uslov)) {
            lista.add((Specijalizacija) ado);
        }
    }

    @Override
    protected String porukaONeuspehu() {
        return "Sistem ne moze da ucita specijalizacije stomatologa.";
    }
}
