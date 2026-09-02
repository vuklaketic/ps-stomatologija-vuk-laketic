package so.specijalizacija;

import java.sql.SQLIntegrityConstraintViolationException;
import model.Specijalizacija;
import so.OpstaSistemskaOperacija;
import util.SqlUtil;

/**
 * Sistemska operacija upisa nove specijalizacije.
 *
 * @author vukla
 */
public class UbaciSpecijalizacijuSO extends OpstaSistemskaOperacija {

    /** Naziv ne sme biti duzi od kolone u bazi (VARCHAR(100)). */
    public static final int MAX_DUZINA_NAZIVA = 100;

    private Specijalizacija zapamcena;

    public Specijalizacija getZapamcena() {
        return zapamcena;
    }

    @Override
    protected void preduslovi(Object objekat) throws Exception {
        if (objekat == null || !(objekat instanceof Specijalizacija)) {
            throw new Exception("Nije prosleđen parametar odgovarajućeg tipa.");
        }

        Specijalizacija specijalizacija = (Specijalizacija) objekat;
        if (specijalizacija.getNaziv() == null || specijalizacija.getNaziv().trim().isEmpty()) {
            throw new Exception("Specijalizacija mora imati naziv.");
        }
        if (specijalizacija.getNaziv().trim().length() > MAX_DUZINA_NAZIVA) {
            throw new Exception("Naziv specijalizacije ne sme imati više od "
                    + MAX_DUZINA_NAZIVA + " znakova.");
        }

        // naziv je ono po cemu se specijalizacija prepoznaje, pa dve
        // specijalizacije ne mogu da nose isti naziv
        String uslov = " WHERE specijalizacija.naziv = '"
                + SqlUtil.escapiraj(specijalizacija.getNaziv().trim()) + "'";
        if (broker.vratiObjekat(new Specijalizacija(), uslov) != null) {
            throw new Exception("Specijalizacija sa nazivom "
                    + specijalizacija.getNaziv().trim() + " već postoji.");
        }
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        Specijalizacija specijalizacija = (Specijalizacija) objekat;
        specijalizacija.setNaziv(specijalizacija.getNaziv().trim());

        try {
            int idSpecijalizacija = broker.dodaj(specijalizacija);
            specijalizacija.setIdSpecijalizacija(idSpecijalizacija);
        } catch (SQLIntegrityConstraintViolationException ex) {
            // zastita ako je ista specijalizacija upisana izmedju provere i upisa
            throw new Exception("Specijalizacija sa nazivom "
                    + specijalizacija.getNaziv() + " već postoji.");
        }

        zapamcena = specijalizacija;
    }

    @Override
    protected String porukaONeuspehu() {
        return "Sistem ne može da zapamti specijalizaciju.";
    }
}
