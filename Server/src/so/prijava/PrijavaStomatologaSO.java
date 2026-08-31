package so.prijava;

import model.Stomatolog;
import so.OpstaSistemskaOperacija;
import util.SqlUtil;

/**
 * Sistemska operacija prijave stomatologa na sistem.
 *
 * @author vukla
 */
public class PrijavaStomatologaSO extends OpstaSistemskaOperacija {

    private Stomatolog ulogovani;

    public Stomatolog getUlogovani() {
        return ulogovani;
    }

    @Override
    protected void preduslovi(Object objekat) throws Exception {
        if (objekat == null || !(objekat instanceof Object[])) {
            throw new Exception("Nije prosledjen parametar odgovarajuceg tipa.");
        }
        Object[] podaci = (Object[]) objekat;
        if (podaci.length != 2 || !(podaci[0] instanceof String) || !(podaci[1] instanceof String)) {
            throw new Exception("Nisu prosledjeni korisnicko ime i sifra.");
        }
        if (((String) podaci[0]).trim().isEmpty() || ((String) podaci[1]).isEmpty()) {
            throw new Exception("Korisnicko ime i sifra ne smeju biti prazni.");
        }
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        Object[] podaci = (Object[]) objekat;
        String korisnickoIme = ((String) podaci[0]).trim();
        String sifra = (String) podaci[1];

        String uslov = " WHERE korisnickoIme = '" + SqlUtil.escapiraj(korisnickoIme) + "'"
                + " AND sifra = '" + SqlUtil.escapiraj(sifra) + "'";

        ulogovani = (Stomatolog) broker.vratiObjekat(new Stomatolog(), uslov);

        if (ulogovani == null) {
            throw new Exception("Pogresno korisnicko ime ili sifra.");
        }
    }
}
