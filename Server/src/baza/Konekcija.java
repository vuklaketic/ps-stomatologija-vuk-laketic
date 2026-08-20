package baza;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton koji drzi jednu jedinu konekciju servera ka bazi podataka.
 *
 * Parametri konekcije se citaju iz fajla {@code baza.properties} iz radnog
 * direktorijuma projekta. Ako fajl ne postoji, koriste se podrazumevane
 * vrednosti definisane u ovoj klasi, pa server moze da se pokrene i bez
 * dodatnog podesavanja.
 *
 * Konekcija se otvara sa iskljucenim automatskim potvrdjivanjem transakcije
 * (autoCommit = false), jer se sistemske operacije nad terminom sastoje od vise
 * SQL naredbi koje moraju da se izvrse kao celina.
 *
 * @author vukla
 */
public class Konekcija {

    private static final Logger logger = Logger.getLogger(Konekcija.class.getName());

    private static final String FAJL_SA_PODESAVANJIMA = "baza.properties";

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
        Properties podesavanja = ucitajPodesavanja();

        String url = podesavanja.getProperty("url", PODRAZUMEVANI_URL);
        String korisnik = podesavanja.getProperty("korisnik", PODRAZUMEVANI_KORISNIK);
        String sifra = podesavanja.getProperty("sifra", PODRAZUMEVANA_SIFRA);

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
     * Potvrdjuje tekucu transakciju.
     */
    public synchronized void potvrdiTransakciju() throws SQLException {
        if (konekcija != null && !konekcija.isClosed()) {
            konekcija.commit();
        }
    }

    /**
     * Ponistava tekucu transakciju.
     */
    public synchronized void ponistiTransakciju() {
        try {
            if (konekcija != null && !konekcija.isClosed()) {
                konekcija.rollback();
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Greska prilikom ponistavanja transakcije", ex);
        }
    }

    /**
     * Zatvara konekciju ka bazi.
     */
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
