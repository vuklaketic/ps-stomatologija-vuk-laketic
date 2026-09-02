package baza;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton koji drzi jednu konekciju servera ka bazi. Parametri se citaju iz
 * {@code baza.properties}; ako fajl ne postoji, koriste se podrazumevane
 * vrednosti iz ove klase, pa server radi i bez dodatnog podesavanja.
 *
 * Konekcija ima autoCommit=false, jer se sistemske operacije nad terminom
 * sastoje od vise SQL naredbi koje moraju da se izvrse kao celina.
 *
 * @author vukla
 */
public class Konekcija {

    private static final Logger logger = Logger.getLogger(Konekcija.class.getName());

    private static final String FAJL_SA_PODESAVANJIMA = "baza.properties";

    public static final String KLJUC_URL = "url";
    public static final String KLJUC_KORISNIK = "korisnik";
    public static final String KLJUC_SIFRA = "sifra";

    private static final String PODRAZUMEVANI_URL =
            "jdbc:mysql://localhost:3306/stomatologija?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Belgrade";
    private static final String PODRAZUMEVANI_KORISNIK = "root";
    private static final String PODRAZUMEVANA_SIFRA = "";

    private static Konekcija instanca;

    private Connection konekcija;

    private Konekcija() {
    }

    public static synchronized Konekcija getInstanca() {
        if (instanca == null) {
            instanca = new Konekcija();
        }
        return instanca;
    }

    /**
     * Vraca otvorenu konekciju ka bazi. Ako konekcija jos nije uspostavljena ili
     * je u medjuvremenu zatvorena, otvara novu.
     *
     * @return konekcija ka bazi podataka
     * @throws SQLException ako baza nije dostupna ili su podaci za prijavu pogresni
     */
    public synchronized Connection getKonekcija() throws SQLException {
        if (konekcija == null || konekcija.isClosed()) {
            otvoriKonekciju();
        }
        return konekcija;
    }

    private void otvoriKonekciju() throws SQLException {
        Properties podesavanja = vratiPodesavanja();

        String url = podesavanja.getProperty(KLJUC_URL);
        String korisnik = podesavanja.getProperty(KLJUC_KORISNIK);
        String sifra = podesavanja.getProperty(KLJUC_SIFRA);

        konekcija = DriverManager.getConnection(url, korisnik, sifra);
        konekcija.setAutoCommit(false);

        logger.log(Level.INFO, "Uspostavljena konekcija sa bazom {0}", url);
    }

    private Properties ucitajPodesavanja() {
        Properties podesavanja = new Properties();
        File fajl = new File(FAJL_SA_PODESAVANJIMA);
        if (!fajl.exists()) {
            logger.log(Level.INFO, "Fajl {0} ne postoji, koriste se podrazumevana podesavanja baze.",
                    FAJL_SA_PODESAVANJIMA);
            return podesavanja;
        }
        try (InputStream ulaz = new FileInputStream(fajl)) {
            podesavanja.load(ulaz);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Greska pri citanju fajla " + FAJL_SA_PODESAVANJIMA
                    + ", koriste se podrazumevana podesavanja baze.", ex);
        }
        return podesavanja;
    }

    /**
     * Vraca trenutno vazeca podesavanja konekcije, sa popunjenim podrazumevanim
     * vrednostima za sve sto u fajlu nije zadato. Koristi je serverska forma da
     * popuni polja u dijalogu za podesavanje baze.
     *
     * @return podesavanja sa kljucevima url, korisnik i sifra
     */
    public synchronized Properties vratiPodesavanja() {
        Properties podesavanja = ucitajPodesavanja();

        Properties vazeca = new Properties();
        vazeca.setProperty(KLJUC_URL, podesavanja.getProperty(KLJUC_URL, PODRAZUMEVANI_URL));
        vazeca.setProperty(KLJUC_KORISNIK, podesavanja.getProperty(KLJUC_KORISNIK, PODRAZUMEVANI_KORISNIK));
        vazeca.setProperty(KLJUC_SIFRA, podesavanja.getProperty(KLJUC_SIFRA, PODRAZUMEVANA_SIFRA));
        return vazeca;
    }

    /**
     * Upisuje nova podesavanja u fajl {@code baza.properties}, u istom formatu
     * u kome se i citaju. Sama konekcija se ovim ne menja - za to sluzi
     * {@link #ponovoUspostaviKonekciju()}.
     *
     * @param url adresa baze
     * @param korisnik korisnicko ime
     * @param sifra sifra
     * @throws IOException ako fajl ne moze da se upise
     */
    public synchronized void sacuvajPodesavanja(String url, String korisnik, String sifra) throws IOException {
        Properties podesavanja = new Properties();
        podesavanja.setProperty(KLJUC_URL, url);
        podesavanja.setProperty(KLJUC_KORISNIK, korisnik);
        podesavanja.setProperty(KLJUC_SIFRA, sifra);

        File fajl = new File(FAJL_SA_PODESAVANJIMA);
        try (OutputStream izlaz = new FileOutputStream(fajl)) {
            podesavanja.store(izlaz, "Podesavanja konekcije sa bazom");
        }

        logger.log(Level.INFO, "Podesavanja baze su sacuvana u {0}", fajl.getAbsolutePath());
    }

    /**
     * Zatvara postojecu konekciju i odmah otvara novu, sa podesavanjima koja se
     * ponovo citaju iz fajla. Poziva se posle izmene podataka o bazi, da bi
     * server odmah radio sa novim parametrima.
     *
     * @return nova konekcija ka bazi
     * @throws SQLException ako baza nije dostupna ili su podaci pogresni
     */
    public synchronized Connection ponovoUspostaviKonekciju() throws SQLException {
        zatvoriKonekciju();
        return getKonekcija();
    }

    public synchronized void potvrdiTransakciju() throws SQLException {
        if (konekcija != null && !konekcija.isClosed()) {
            konekcija.commit();
        }
    }

    public synchronized void ponistiTransakciju() {
        try {
            if (konekcija != null && !konekcija.isClosed()) {
                konekcija.rollback();
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Greska prilikom ponistavanja transakcije", ex);
        }
    }

    public synchronized void zatvoriKonekciju() {
        try {
            if (konekcija != null && !konekcija.isClosed()) {
                konekcija.close();
            }
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Greska prilikom zatvaranja konekcije", ex);
        } finally {
            konekcija = null;
        }
    }
}
