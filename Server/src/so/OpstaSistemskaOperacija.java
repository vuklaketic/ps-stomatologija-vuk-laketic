package so;

import baza.Konekcija;
import dao.GenericDAO;

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
 * @author vukla
 */
public abstract class OpstaSistemskaOperacija {

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
        } catch (Exception ex) {
            ponistiTransakciju();
            throw ex;
        }
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
