package so.termin;

import java.util.ArrayList;
import java.util.List;
import model.ApstraktniDomenskiObjekat;
import model.StatusTermina;
import model.StavkaTermina;
import model.Termin;
import model.Usluga;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija izmene postojeceg termina.
 *
 * Stavke se ne brisu i ne ubacuju ponovo. Umesto toga se stanje stavki u bazi
 * poredi sa stanjem koje je stiglo sa forme, pa se za svaku stavku poziva samo
 * ona operacija koja joj odgovara: stavka koje na formi vise nema se brise,
 * stavka kojoj je promenjena kolicina ili cena se menja, a usluga koje u bazi
 * nije bilo se dodaje. Stavke koje su ostale iste se ne diraju.
 *
 * Stavka se prepoznaje po usluzi, jer je usluga ono sto stavku razlikuje u
 * okviru jednog termina - ista usluga se u istom terminu ne moze pojaviti dva
 * puta, vec se unosi u vecoj kolicini. Redni broj se za tu svrhu ne koristi:
 * on je deo primarnog kljuca stavke i zato ostaje nepromenjen dok stavka
 * postoji, pa uklanjanje jedne stavke ne izaziva izmenu svih ostalih.
 *
 * Sve operacije se izvrsavaju u jednoj transakciji, koju vodi
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
            throw new Exception("Nije prosledjen parametar odgovarajuceg tipa.");
        }

        Termin termin = (Termin) objekat;
        if (termin.getIdTermin() <= 0) {
            throw new Exception("Nije prosledjen identifikator termina.");
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
        // stavka se prepoznaje po usluzi, pa ista usluga ne sme da se pojavi
        // dva puta - u tom slucaju se unosi jedna stavka u vecoj kolicini
        List<Integer> videneUsluge = new ArrayList<>();
        for (StavkaTermina stavka : termin.getStavke()) {
            if (stavka.getUsluga() == null || stavka.getUsluga().getIdUsluga() <= 0) {
                throw new Exception("Svaka stavka termina mora imati uslugu.");
            }
            if (stavka.getKolicina() <= 0) {
                throw new Exception("Kolicina stavke mora biti veca od nule.");
            }
            if (videneUsluge.contains(stavka.getUsluga().getIdUsluga())) {
                throw new Exception("Termin ne moze da sadrzi dve stavke sa istom uslugom.");
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

        // poslovno pravilo: isti stomatolog ne moze da ima dva termina koja nisu
        // otkazana u istom danu i u isto vreme - sam termin se ne racuna
        String uslovZauzetost = Termin.SPOJEVI
                + " WHERE termin.idStomatolog = " + termin.getStomatolog().getIdStomatolog()
                + " AND termin.datum = '" + termin.getDatum() + "'"
                + " AND termin.vreme = '" + termin.getVreme() + "'"
                + " AND termin.status <> '" + StatusTermina.OTKAZAN.name() + "'"
                + " AND termin.idTermin <> " + termin.getIdTermin();

        if (!broker.vratiPoUpitu(new Termin(), uslovZauzetost).isEmpty()) {
            throw new Exception("Stomatolog vec ima zakazan termin "
                    + termin.getDatum() + " u " + termin.getVreme() + ".");
        }
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        Termin termin = (Termin) objekat;

        broker.izmeni(termin);

        List<StavkaTermina> stareStavke = ucitajStavke(termin);
        List<StavkaTermina> noveStavke = termin.getStavke();

        // stavke koje su ostale i posle izmene - njihov redni broj se zadrzava,
        // a nove stavke se nastavljaju na najveci od njih
        List<StavkaTermina> preostale = new ArrayList<>();

        // 1. obrisane stavke - postoje u bazi, a na formi ih vise nema
        for (StavkaTermina stara : stareStavke) {
            if (pronadjiPoUsluzi(noveStavke, stara.getUsluga()) == null) {
                broker.obrisi(stara);
            } else {
                preostale.add(stara);
            }
        }

        // 2. izmenjene stavke - ista usluga, ali druga kolicina ili cena;
        // stavka koja je ostala ista se ne dira
        for (StavkaTermina stara : preostale) {
            StavkaTermina nova = pronadjiPoUsluzi(noveStavke, stara.getUsluga());
            nova.setTermin(termin);
            nova.setRb(stara.getRb());
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
        return "Sistem ne moze da zapamti termin.";
    }
}
