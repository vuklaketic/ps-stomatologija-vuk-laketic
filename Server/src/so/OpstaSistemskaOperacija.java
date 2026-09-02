package so;

import baza.Konekcija;
import dao.GenericDAO;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Zajednicki predak svih sistemskih operacija - primenjuje sablon metodu:
 * {@link #izvrsiOperaciju(java.lang.Object)} vodi nepromenljiv redosled
 * (preduslovi, izvrsenje, potvrda/ponistavanje transakcije), a konkretne
 * operacije popunjavaju samo preduslovi() i izvrsi().
 *
 * Krsenje poslovnog pravila se prijavljuje obicnim izuzetkom sa porukom za
 * korisnika; tehnicki problem (npr. baza nedostupna) dobija poruku iz
 * specifikacije operacije, a pravi detalj ostaje u logu.
 *
 * @author vukla
 */
public abstract class OpstaSistemskaOperacija {

    private static final Logger logger = Logger.getLogger(OpstaSistemskaOperacija.class.getName());

    /** Tehnicki detalj u poruci korisniku se skracuje na ovoliko znakova. */
    private static final int NAJVECA_DUZINA_DETALJA = 120;

    protected final GenericDAO broker;

    public OpstaSistemskaOperacija() {
        this.broker = new GenericDAO();
    }

    /**
     * Izvrsava sistemsku operaciju kao jednu transakciju.
     *
     * @param objekat parametar operacije
     * @throws Exception ako preduslovi nisu ispunjeni ili izvrsenje ne uspe;
     *                   u tom slucaju je transakcija ponistena
     */
    public final void izvrsiOperaciju(Object objekat) throws Exception {
        try {
            preduslovi(objekat);
            izvrsi(objekat);
            potvrdiTransakciju();
        } catch (SQLException | RuntimeException ex) {
            // tehnicki problem - korisniku ide poruka operacije, sirovi detalj u log
            ponistiTransakciju();
            logger.log(Level.SEVERE, "Tehnicka greska u operaciji "
                    + getClass().getSimpleName(), ex);
            throw new Exception(porukaONeuspehu()
                    + System.lineSeparator() + "Detalj: " + kratakDetalj(ex));
        } catch (Exception ex) {
            // krsenje poslovnog pravila - poruka je vec razumljiva
            ponistiTransakciju();
            throw ex;
        }
    }

    /** Poruka za tehnicki neuspeh - svaka operacija je propisuje za svoj slucaj koriscenja. */
    protected abstract String porukaONeuspehu();

    /**
     * Skracuje tehnicku poruku na prvi red i razumnu duzinu, da dijalog kod
     * korisnika ostane citljiv. Ceo izuzetak, sa svim redovima i stekom, ostaje
     * u serverskom logu.
     */
    private String kratakDetalj(Exception ex) {
        String poruka = ex.getMessage();
        if (poruka == null || poruka.trim().isEmpty()) {
            return ex.getClass().getSimpleName();
        }

        String prviRed = poruka.split("\\R", 2)[0].trim();
        return prviRed.length() > NAJVECA_DUZINA_DETALJA
                ? prviRed.substring(0, NAJVECA_DUZINA_DETALJA) + "..."
                : prviRed;
    }

    protected abstract void preduslovi(Object objekat) throws Exception;

    protected abstract void izvrsi(Object objekat) throws Exception;

    private void potvrdiTransakciju() throws Exception {
        Konekcija.getInstanca().potvrdiTransakciju();
    }

    private void ponistiTransakciju() {
        Konekcija.getInstanca().ponistiTransakciju();
    }
}
