package so.termin;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.ApstraktniDomenskiObjekat;
import model.StatusTermina;
import model.StavkaTermina;
import model.Termin;
import model.Usluga;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija izmene postojeceg termina. Stavke se ne brisu i ne
 * ubacuju ponovo - porede se sa stanjem u bazi i za svaku se poziva samo
 * odgovarajuca operacija (obrisi/izmeni/dodaj). Transakciju vodi
 * {@link OpstaSistemskaOperacija}.
 *
 * @author vukla
 */
public class PromeniTerminSO extends OpstaSistemskaOperacija {

    /** Cena se u bazi cuva na dve decimale, pa se razlika manja od ove ne racuna. */
    private static final double TOLERANCIJA_CENE = 0.005;

    private Termin izmenjeni;

    public Termin getIzmenjeni() {
        return izmenjeni;
    }

    @Override
    protected void preduslovi(Object objekat) throws Exception {
        if (objekat == null || !(objekat instanceof Termin)) {
            throw new Exception("Nije prosleđen parametar odgovarajućeg tipa.");
        }

        Termin termin = (Termin) objekat;
        if (termin.getIdTermin() <= 0) {
            throw new Exception("Nije prosleđen identifikator termina.");
        }
        if (termin.getDatum() == null || termin.getVreme() == null) {
            throw new Exception("Termin mora imati datum i vreme.");
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
        // ista usluga ne sme dva puta u jednom terminu
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

        String uslovPostoji = Termin.SPOJEVI + " WHERE termin.idTermin = " + termin.getIdTermin();
        if (broker.vratiObjekat(new Termin(), uslovPostoji) == null) {
            throw new Exception("Termin koji se menja ne postoji u bazi.");
        }

        proveriZauzetost(termin);
    }

    /**
     * Proverava da li je stomatolog slobodan za termin posle izmene, poredeci
     * intervale trajanja (ne samo pocetno vreme) - termini koji se nadovezuju
     * nisu preklapanje. Sam termin koji se menja i otkazani termini se
     * izostavljaju.
     */
    private void proveriZauzetost(Termin termin) throws Exception {
        LocalDateTime pocetakIzmenjenog = LocalDateTime.of(termin.getDatum(), termin.getVreme());
        LocalDateTime krajIzmenjenog =
                pocetakIzmenjenog.plusMinutes(trajanjeIzmenjenogTermina(termin));

        String uslov = Termin.SPOJEVI
                + " WHERE termin.idStomatolog = " + termin.getStomatolog().getIdStomatolog()
                + " AND termin.datum = '" + termin.getDatum() + "'"
                + " AND termin.status <> '" + StatusTermina.OTKAZAN.name() + "'"
                + " AND termin.idTermin <> " + termin.getIdTermin();

        for (ApstraktniDomenskiObjekat ado : broker.vratiPoUpitu(new Termin(), uslov)) {
            Termin postojeci = (Termin) ado;

            LocalDateTime pocetak = LocalDateTime.of(postojeci.getDatum(), postojeci.getVreme());
            LocalDateTime kraj = pocetak.plusMinutes(trajanjePostojecegTermina(postojeci));

            if (pocetakIzmenjenog.isBefore(kraj) && pocetak.isBefore(krajIzmenjenog)) {
                throw new Exception("Stomatolog već ima zakazan termin koji se preklapa"
                        + " sa izabranim terminom (" + postojeci.getDatum()
                        + " u " + postojeci.getVreme() + ").");
            }
        }
    }

    /**
     * Racuna trajanje termina kao zbir (trajanje usluge x kolicina) po
     * stavkama, isto kao i iznos. Cita se iz sifarnika, ne sa klijenta.
     */
    private int trajanjeIzmenjenogTermina(Termin termin) throws Exception {
        int trajanje = 0;
        for (StavkaTermina stavka : termin.getStavke()) {
            trajanje += stavka.getKolicina() * vratiTrajanjeUsluge(stavka);
        }
        return trajanje;
    }

    /**
     * Isto sto i {@link #trajanjeIzmenjenogTermina}, za termin koji je vec
     * zapamcen.
     */
    private int trajanjePostojecegTermina(Termin postojeci) throws Exception {
        int trajanje = 0;
        for (StavkaTermina stavka : ucitajStavke(postojeci)) {
            trajanje += stavka.getKolicina() * stavka.getUsluga().getTrajanje();
        }
        return trajanje;
    }

    /**
     * Cita trajanje usluge iz sifarnika, po uzoru na citanje cene.
     */
    private int vratiTrajanjeUsluge(StavkaTermina stavka) throws Exception {
        String uslov = " WHERE usluga.idUsluga = " + stavka.getUsluga().getIdUsluga();
        Usluga usluga = (Usluga) broker.vratiObjekat(new Usluga(), uslov);

        if (usluga == null) {
            throw new Exception("Usluga sa stavke termina ne postoji u bazi.");
        }
        if (usluga.getTrajanje() <= 0) {
            throw new Exception("Trajanje usluge mora biti veće od nule.");
        }
        return usluga.getTrajanje();
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        Termin termin = (Termin) objekat;

        broker.izmeni(termin);

        List<StavkaTermina> stareStavke = ucitajStavke(termin);
        List<StavkaTermina> noveStavke = termin.getStavke();

        // redni broj postojecih stavki se zadrzava
        List<StavkaTermina> preostale = new ArrayList<>();

        // 1. obrisane stavke - postoje u bazi, a na formi ih vise nema
        for (StavkaTermina stara : stareStavke) {
            if (pronadjiPoUsluzi(noveStavke, stara.getUsluga()) == null) {
                broker.obrisi(stara);
            } else {
                preostale.add(stara);
            }
        }

        // 2. izmenjene stavke - ista usluga, ali druga kolicina ili cena
        for (StavkaTermina stara : preostale) {
            StavkaTermina nova = pronadjiPoUsluzi(noveStavke, stara.getUsluga());
            nova.setTermin(termin);
            nova.setRb(stara.getRb());
            nova.setCenaUsluge(vratiCenuUsluge(nova));
            nova.izracunajIznos();
            if (jeIzmenjena(stara, nova)) {
                broker.izmeni(nova);
            }
        }

        // 3. dodate stavke - usluga koje u bazi nije bilo
        int sledeciRb = najveciRb(preostale) + 1;
        for (StavkaTermina nova : noveStavke) {
            if (pronadjiPoUsluzi(stareStavke, nova.getUsluga()) == null) {
                nova.setTermin(termin);
                nova.setRb(sledeciRb++);
                nova.setCenaUsluge(vratiCenuUsluge(nova));
                nova.izracunajIznos();
                broker.dodaj(nova);
            }
        }

        izmenjeni = termin;
    }

    /**
     * Ucitava stavke koje termin trenutno ima u bazi, da bi se videlo sta je
     * stomatolog na formi dodao, promenio i uklonio.
     */
    private List<StavkaTermina> ucitajStavke(Termin termin) throws Exception {
        String uslov = StavkaTermina.SPOJEVI
                + " WHERE stavkatermina.idTermin = " + termin.getIdTermin()
                + " ORDER BY stavkatermina.rb";

        List<StavkaTermina> stavke = new ArrayList<>();
        for (ApstraktniDomenskiObjekat ado : broker.vratiPoUpitu(new StavkaTermina(), uslov)) {
            StavkaTermina stavka = (StavkaTermina) ado;
            stavka.setTermin(termin);
            stavke.add(stavka);
        }
        return stavke;
    }

    /**
     * Trazi stavku sa zadatom uslugom u listi stavki.
     *
     * @return pronadjena stavka ili null ako liste nema stavku sa tom uslugom
     */
    private StavkaTermina pronadjiPoUsluzi(List<StavkaTermina> stavke, Usluga usluga) {
        for (StavkaTermina stavka : stavke) {
            if (stavka.getUsluga() != null
                    && stavka.getUsluga().getIdUsluga() == usluga.getIdUsluga()) {
                return stavka;
            }
        }
        return null;
    }

    /**
     * Utvrdjuje da li se stavka stvarno promenila. Iznos se ne poredi posebno,
     * jer je proizvod kolicine i cene, pa se menja samo sa njima.
     */
    private boolean jeIzmenjena(StavkaTermina stara, StavkaTermina nova) {
        return stara.getKolicina() != nova.getKolicina()
                || Math.abs(stara.getCenaUsluge() - nova.getCenaUsluge()) >= TOLERANCIJA_CENE;
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

    /**
     * Vraca najveci redni broj u listi stavki, odnosno nulu za praznu listu.
     */
    private int najveciRb(List<StavkaTermina> stavke) {
        int najveci = 0;
        for (StavkaTermina stavka : stavke) {
            najveci = Math.max(najveci, stavka.getRb());
        }
        return najveci;
    }

    @Override
    protected String porukaONeuspehu() {
        return "Sistem ne može da zapamti termin.";
    }
}
