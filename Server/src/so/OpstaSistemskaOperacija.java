package so;

import baza.Konekcija;
import dao.GenericDAO;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Zajednicki predak svih sistemskih operacija.
 *
 * Primenjuje sablon metodu: {@link #izvrsiOperaciju(java.lang.Object)} utvrdjuje
 * nepromenljiv redosled koraka (provera preduslova, izvrsenje, potvrda odnosno
 * ponistavanje transakcije), a konkretne operacije popunjavaju samo korake
 * {@link #preduslovi(java.lang.Object)} i {@link #izvrsi(java.lang.Object)}.
 *
 * Zahvaljujuci tome nijedna konkretna operacija ne barata transakcijom, pa nije
 * moguce da se negde zaboravi potvrda ili ponistavanje.
 *
 * Na istom mestu se razdvajaju i dve vrste neuspeha. Krsenje poslovnog pravila
 * konkretna operacija prijavljuje obicnim izuzetkom sa razumljivom porukom i ta
 * poruka se prosledjuje korisniku nepromenjena. Tehnicki problem, kao sto je
 * nedostupna baza, javlja se kao izuzetak baze ili nepredvidjen izuzetak i
 * njegova poruka nije za korisnika, pa se zamenjuje porukom operacije iz
 * specifikacije, a tehnicki detalj ostaje u logu i kao dopuna poruke.
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
            // tehnicki problem - korisniku ide poruka operacije, a sirova
            // poruka baze ostaje u logu i kao dopuna, radi trazenja uzroka
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

    /**
     * Poruka koju korisnik dobija kada operacija ne uspe iz tehnickih razloga.
     * Svaka operacija vraca recenicu propisanu za njen slucaj koriscenja.
     */
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

    /**
     * Provera preduslova koje parametar mora da ispuni.
     */
    protected abstract void preduslovi(Object objekat) throws Exception;

    /**
     * Sam posao operacije.
     */
    protected abstract void izvrsi(Object objekat) throws Exception;

    private void potvrdiTransakciju() throws Exception {
        Konekcija.getInstanca().potvrdiTransakciju();
    }

    private void ponistiTransakciju() {
        Konekcija.getInstanca().ponistiTransakciju();
    }
}
