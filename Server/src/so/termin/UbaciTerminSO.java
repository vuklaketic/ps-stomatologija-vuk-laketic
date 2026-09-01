package so.termin;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import model.StatusTermina;
import model.StavkaTermina;
import model.Termin;
import model.Usluga;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija zakazivanja novog termina zajedno sa njegovim stavkama.
 *
 * @author vukla
 */
public class UbaciTerminSO extends OpstaSistemskaOperacija {

    /** Stomatolog ne moze imati vise od ovoliko termina u jednom danu. */
    private static final int NAJVISE_TERMINA_DNEVNO = 10;

    private Termin zakazani;
    public Termin getZakazani() {
        return zakazani;
    }
    @Override
    protected void preduslovi(Object objekat) throws Exception {
        if (objekat == null || !(objekat instanceof Termin)) {
            throw new Exception("Nije prosleđen parametar odgovarajućeg tipa.");
        }

        Termin termin = (Termin) objekat;
        if (termin.getDatum() == null || termin.getVreme() == null) {
            throw new Exception("Termin mora imati datum i vreme.");
        }
        // termin se ne zakazuje unazad; za danasnji dan se poredi i vreme.
        // Postojecem terminu se datum i vreme ne diraju, pa isto pravilo ne
        // vazi i za izmenu
        if (jeUProslosti(termin.getDatum(), termin.getVreme())) {
            throw new Exception("Termin ne može biti zakazan u prošlosti.");
        }
        if (termin.getStomatolog() == null || termin.getStomatolog().getIdStomatolog() <= 0) {
            throw new Exception("Termin mora imati stomatologa.");
        }
        if (termin.getPacijent() == null || termin.getPacijent().getIdPacijent() <= 0) {
            throw new Exception("Termin mora imati pacijenta.");
        }
        if (termin.getStavke() == null || termin.getStavke().isEmpty()) {
            throw new Exception("Termin mora imati bar jednu uslugu.");
        }
        // stavka se prepoznaje po usluzi, pa ista usluga ne sme da se pojavi
        // dva puta - u tom slucaju se unosi jedna stavka u vecoj kolicini
        List<Integer> videneUsluge = new ArrayList<>();
        for (StavkaTermina stavka : termin.getStavke()) {
            if (stavka.getUsluga() == null || stavka.getUsluga().getIdUsluga() <= 0) {
                throw new Exception("Svaka stavka termina mora imati uslugu.");
            }
            if (stavka.getKolicina() <= 0) {
                throw new Exception("Količina stavke mora biti veća od nule.");
            }
            if (videneUsluge.contains(stavka.getUsluga().getIdUsluga())) {
                throw new Exception("Termin ne može da sadrži dve stavke sa istom uslugom.");
            }
            videneUsluge.add(stavka.getUsluga().getIdUsluga());
        }
        if (termin.getStatus() == null) {
            termin.setStatus(StatusTermina.ZAKAZAN);
        }
        if (termin.getNapomena() == null) {
            termin.setNapomena("");
        }

        String uslov = Termin.SPOJEVI
                + " WHERE termin.idStomatolog = " + termin.getStomatolog().getIdStomatolog()
                + " AND termin.datum = '" + termin.getDatum() + "'"
                + " AND termin.vreme = '" + termin.getVreme() + "'"
                + " AND termin.status <> '" + StatusTermina.OTKAZAN.name() + "'";

        if (!broker.vratiPoUpitu(new Termin(), uslov).isEmpty()) {
            throw new Exception("Stomatolog već ima zakazan termin "
                    + termin.getDatum() + " u " + termin.getVreme() + ".");
        }

        // poslovno pravilo: stomatolog u jednom danu ne moze da primi vise od
        // deset pacijenata; otkazani termini se ne racunaju, jer je to vreme
        // ponovo slobodno
        String uslovBroja = Termin.SPOJEVI
                + " WHERE termin.idStomatolog = " + termin.getStomatolog().getIdStomatolog()
                + " AND termin.datum = '" + termin.getDatum() + "'"
                + " AND termin.status <> '" + StatusTermina.OTKAZAN.name() + "'";

        if (broker.vratiPoUpitu(new Termin(), uslovBroja).size() >= NAJVISE_TERMINA_DNEVNO) {
            throw new Exception("Stomatolog već ima maksimalan broj termina ("
                    + NAJVISE_TERMINA_DNEVNO + ") zakazanih za " + termin.getDatum() + ".");
        }
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        Termin termin = (Termin) objekat;

        int idTermin = broker.dodaj(termin);
        termin.setIdTermin(idTermin);

        // termin se tek upisuje, pa njegove stavke dobijaju redne brojeve od
        // jedan naviste, redosledom kojim su unete na formi
        int redniBroj = 1;
        for (StavkaTermina stavka : termin.getStavke()) {
            stavka.setTermin(termin);
            stavka.setRb(redniBroj++);
            // cena usluge i iznos su vezani za sifarnik usluga, pa se ne
            // preuzimaju onakvi kakvi su stigli sa klijenta: cena se cita iz
            // baze, a iznos racuna iz nje i kolicine
            stavka.setCenaUsluge(vratiCenuUsluge(stavka));
            stavka.izracunajIznos();
            broker.dodaj(stavka);
        }

        zakazani = termin;
    }

    /**
     * Utvrdjuje da li zadati trenutak vec pripada proslosti. Za danasnji dan se
     * poredi i vreme, jer termin koji je danas u devet ujutru u podne vise ne
     * moze da se zakaze.
     */
    private boolean jeUProslosti(LocalDate datum, LocalTime vreme) {
        LocalDate danas = LocalDate.now();
        if (datum.isBefore(danas)) {
            return true;
        }
        return datum.equals(danas) && vreme.isBefore(LocalTime.now());
    }

    /**
     * Cita cenu usluge iz sifarnika, jer je cena stavke po pravilu upravo cena
     * usluge koja je na stavci.
     */
    private double vratiCenuUsluge(StavkaTermina stavka) throws Exception {
        String uslov = " WHERE usluga.idUsluga = " + stavka.getUsluga().getIdUsluga();
        Usluga usluga = (Usluga) broker.vratiObjekat(new Usluga(), uslov);

        if (usluga == null) {
            throw new Exception("Usluga sa stavke termina ne postoji u bazi.");
        }
        if (usluga.getCena() <= 0) {
            throw new Exception("Cena usluge mora biti veća od nule.");
        }
        return usluga.getCena();
    }

    @Override
    protected String porukaONeuspehu() {
        return "Sistem ne može da zapamti termin.";
    }
}
