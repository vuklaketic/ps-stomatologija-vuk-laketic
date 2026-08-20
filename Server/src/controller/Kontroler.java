package controller;

import baza.Konekcija;
import dao.GenericDAO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.ApstraktniDomenskiObjekat;
import model.Ordinacija;
import model.Pacijent;
import model.StatusTermina;
import model.StavkaTermina;
import model.Stomatolog;
import model.Termin;
import model.Usluga;

/**
 * Serverski kontroler - sadrzi sve sistemske operacije koje server ume da izvrsi.
 *
 * Klasa obavlja tri posla:
 * <ul>
 *   <li>proverava poslovna pravila (prijava, zauzetost termina),</li>
 *   <li>poziva {@link GenericDAO} za rad sa bazom,</li>
 *   <li>povezuje procitane domenske objekte u celinu, jer DAO cita jednu po jednu
 *       tabelu pa objekti u vezama stizu samo sa identifikatorom.</li>
 * </ul>
 *
 * Sve javne metode su sinhronizovane zato sto server otvara nit po klijentu, a
 * svi dele jednu konekciju ka bazi sa iskljucenim automatskim potvrdjivanjem
 * transakcije. Sinhronizacija sprecava da se transakcije dve niti isprepletu.
 *
 * @author vukla
 */
public class Kontroler {

    private static final Logger logger = Logger.getLogger(Kontroler.class.getName());

    private static Kontroler instanca;

    private final GenericDAO dao = new GenericDAO();

    private Kontroler() {
    }

    public static synchronized Kontroler getInstanca() {
        if (instanca == null) {
            instanca = new Kontroler();
        }
        return instanca;
    }

    // ---------------------------------------------------------------------
    // prijava na sistem
    // ---------------------------------------------------------------------

    /**
     * Prijavljuje stomatologa na sistem.
     *
     * @return pronadjeni stomatolog ili null ako korisnicko ime i sifra ne odgovaraju
     */
    public synchronized Stomatolog prijaviStomatologa(String korisnickoIme, String sifra) throws Exception {
        if (korisnickoIme == null || korisnickoIme.trim().isEmpty()
                || sifra == null || sifra.isEmpty()) {
            return null;
        }

        String uslov = "korisnickoIme = '" + escapiraj(korisnickoIme.trim()) + "'"
                + " AND sifra = '" + escapiraj(sifra) + "'";

        try {
            return (Stomatolog) dao.vratiObjekat(new Stomatolog(), uslov);
        } finally {
            zavrsiCitanje();
        }
    }

    // ---------------------------------------------------------------------
    // ucitavanje lista
    // ---------------------------------------------------------------------

    public synchronized List<Stomatolog> vratiListuStomatologa() throws Exception {
        try {
            List<Stomatolog> stomatolozi = new ArrayList<>();
            for (ApstraktniDomenskiObjekat ado : dao.vratiSve(new Stomatolog())) {
                stomatolozi.add((Stomatolog) ado);
            }
            return stomatolozi;
        } finally {
            zavrsiCitanje();
        }
    }

    public synchronized List<Usluga> vratiListuUsluga() throws Exception {
        try {
            List<Usluga> usluge = new ArrayList<>();
            for (ApstraktniDomenskiObjekat ado : dao.vratiSve(new Usluga())) {
                usluge.add((Usluga) ado);
            }
            return usluge;
        } finally {
            zavrsiCitanje();
        }
    }

    /**
     * Vraca sve pacijente sa popunjenim podacima o ordinaciji.
     */
    public synchronized List<Pacijent> vratiListuPacijenata() throws Exception {
        try {
            List<Pacijent> pacijenti = new ArrayList<>();
            for (ApstraktniDomenskiObjekat ado : dao.vratiSve(new Pacijent())) {
                pacijenti.add((Pacijent) ado);
            }

            Map<Integer, Ordinacija> ordinacije = mapaOrdinacija();
            for (Pacijent pacijent : pacijenti) {
                if (pacijent.getOrdinacija() != null) {
                    Ordinacija ordinacija = ordinacije.get(pacijent.getOrdinacija().getIdOrdinacija());
                    if (ordinacija != null) {
                        pacijent.setOrdinacija(ordinacija);
                    }
                }
            }
            return pacijenti;
        } finally {
            zavrsiCitanje();
        }
    }

    /**
     * Vraca termine koji zadovoljavaju kriterijum koji je poslao klijent.
     *
     * Kriterijum je WHERE klauza nad kolonama tabele termin (npr.
     * "idStomatolog = 3"). Ako je prazan, vracaju se svi termini.
     */
    public synchronized List<Termin> vratiListuTermina(String kriterijum) throws Exception {
        try {
            List<Termin> termini = new ArrayList<>();
            for (ApstraktniDomenskiObjekat ado : dao.vratiPoUpitu(new Termin(), kriterijum)) {
                termini.add((Termin) ado);
            }

            poveziTermine(termini);
            return termini;
        } finally {
            zavrsiCitanje();
        }
    }

    /**
     * Vraca termin sa zadatim identifikatorom ili null ako ne postoji.
     */
    public synchronized Termin pretraziTermin(int idTermin) throws Exception {
        List<Termin> termini = vratiListuTermina("idTermin = " + idTermin);
        return termini.isEmpty() ? null : termini.get(0);
    }

    // ---------------------------------------------------------------------
    // operacije nad terminom
    // ---------------------------------------------------------------------

    /**
     * Zakazuje novi termin zajedno sa svim njegovim stavkama.
     *
     * @return true ako je termin uspesno zakazan
     * @throws Exception ako je stomatolog vec zauzet u to vreme ili je pukao upit
     */
    public synchronized boolean ubaciTermin(Termin termin) throws Exception {
        proveriTermin(termin);
        proveriZauzetost(termin);

        try {
            int idTermin = dao.dodaj(termin);
            termin.setIdTermin(idTermin);

            ubaciStavke(termin);

            Konekcija.getInstanca().potvrdiTransakciju();
            return true;
        } catch (Exception ex) {
            Konekcija.getInstanca().ponistiTransakciju();
            logger.log(Level.SEVERE, "Neuspesno zakazivanje termina", ex);
            throw ex;
        }
    }

    /**
     * Menja postojeci termin. Stavke se brisu i ponovo ubacuju, jer je stavka
     * slaba celina koja postoji samo uz svoj termin.
     *
     * @return true ako je termin uspesno izmenjen
     * @throws Exception ako termin ne postoji, ako je stomatolog vec zauzet u to
     *                   vreme ili je pukao upit
     */
    public synchronized boolean promeniTermin(Termin termin) throws Exception {
        proveriTermin(termin);
        if (termin.getIdTermin() <= 0 || !postojiTermin(termin.getIdTermin())) {
            throw new Exception("Termin koji se menja ne postoji u bazi.");
        }
        proveriZauzetost(termin);

        try {
            dao.izmeni(termin);
            dao.obrisiPoUpitu(new StavkaTermina(), "idTermin = " + termin.getIdTermin());
            ubaciStavke(termin);

            Konekcija.getInstanca().potvrdiTransakciju();
            return true;
        } catch (Exception ex) {
            Konekcija.getInstanca().ponistiTransakciju();
            logger.log(Level.SEVERE, "Neuspesna izmena termina", ex);
            throw ex;
        }
    }

    /**
     * Brise termin i sve njegove stavke.
     *
     * @return true ako je termin obrisan, false ako u bazi nije bilo takvog termina
     */
    public synchronized boolean obrisiTermin(Termin termin) throws Exception {
        if (termin == null || termin.getIdTermin() <= 0) {
            throw new Exception("Nije prosledjen termin za brisanje.");
        }

        try {
            dao.obrisiPoUpitu(new StavkaTermina(), "idTermin = " + termin.getIdTermin());
            int obrisano = dao.obrisi(termin);

            Konekcija.getInstanca().potvrdiTransakciju();
            return obrisano > 0;
        } catch (Exception ex) {
            Konekcija.getInstanca().ponistiTransakciju();
            logger.log(Level.SEVERE, "Neuspesno brisanje termina", ex);
            throw ex;
        }
    }

    // ---------------------------------------------------------------------
    // pomocne metode
    // ---------------------------------------------------------------------

    /**
     * Popunjava termine punim objektima stomatologa, pacijenta i stavkama,
     * jer DAO cita samo tabelu termin pa veze stizu kao identifikatori.
     */
    private void poveziTermine(List<Termin> termini) throws Exception {
        if (termini.isEmpty()) {
            return;
        }

        Map<Integer, Stomatolog> stomatolozi = new HashMap<>();
        for (Stomatolog stomatolog : vratiListuStomatologa()) {
            stomatolozi.put(stomatolog.getIdStomatolog(), stomatolog);
        }

        Map<Integer, Pacijent> pacijenti = new HashMap<>();
        for (Pacijent pacijent : vratiListuPacijenata()) {
            pacijenti.put(pacijent.getIdPacijent(), pacijent);
        }

        Map<Integer, Termin> poIdu = new HashMap<>();
        for (Termin termin : termini) {
            if (termin.getStomatolog() != null) {
                Stomatolog stomatolog = stomatolozi.get(termin.getStomatolog().getIdStomatolog());
                if (stomatolog != null) {
                    termin.setStomatolog(stomatolog);
                }
            }
            if (termin.getPacijent() != null) {
                Pacijent pacijent = pacijenti.get(termin.getPacijent().getIdPacijent());
                if (pacijent != null) {
                    termin.setPacijent(pacijent);
                }
            }
            termin.setStavke(new ArrayList<StavkaTermina>());
            poIdu.put(termin.getIdTermin(), termin);
        }

        ucitajStavke(poIdu);
    }

    /**
     * Jednim upitom ucitava stavke svih prosledjenih termina i raspodeljuje ih
     * po terminima kojima pripadaju.
     */
    private void ucitajStavke(Map<Integer, Termin> terminiPoIdu) throws Exception {
        StringBuilder identifikatori = new StringBuilder();
        for (Integer idTermin : terminiPoIdu.keySet()) {
            if (identifikatori.length() > 0) {
                identifikatori.append(",");
            }
            identifikatori.append(idTermin);
        }

        Map<Integer, Usluga> usluge = new HashMap<>();
        for (Usluga usluga : vratiListuUsluga()) {
            usluge.put(usluga.getIdUsluga(), usluga);
        }

        String uslov = "idTermin IN (" + identifikatori + ")";
        for (ApstraktniDomenskiObjekat ado : dao.vratiPoUpitu(new StavkaTermina(), uslov)) {
            StavkaTermina stavka = (StavkaTermina) ado;

            Termin termin = terminiPoIdu.get(stavka.getTermin().getIdTermin());
            if (termin == null) {
                continue;
            }
            stavka.setTermin(termin);

            if (stavka.getUsluga() != null) {
                Usluga usluga = usluge.get(stavka.getUsluga().getIdUsluga());
                if (usluga != null) {
                    stavka.setUsluga(usluga);
                }
            }
            termin.getStavke().add(stavka);
        }
    }

    private void ubaciStavke(Termin termin) throws Exception {
        List<StavkaTermina> stavke = termin.getStavke();
        if (stavke == null) {
            return;
        }

        int redniBroj = 1;
        for (StavkaTermina stavka : stavke) {
            stavka.setTermin(termin);
            stavka.setRb(redniBroj++);
            dao.dodaj(stavka);
        }
    }

    private Map<Integer, Ordinacija> mapaOrdinacija() throws Exception {
        Map<Integer, Ordinacija> ordinacije = new HashMap<>();
        for (ApstraktniDomenskiObjekat ado : dao.vratiSve(new Ordinacija())) {
            Ordinacija ordinacija = (Ordinacija) ado;
            ordinacije.put(ordinacija.getIdOrdinacija(), ordinacija);
        }
        return ordinacije;
    }

    private boolean postojiTermin(int idTermin) throws Exception {
        return dao.vratiObjekat(new Termin(), "idTermin = " + idTermin) != null;
    }

    /**
     * Proverava da li termin sadrzi sve podatke neophodne za upis u bazu.
     */
    private void proveriTermin(Termin termin) throws Exception {
        if (termin == null) {
            throw new Exception("Nije prosledjen termin.");
        }
        if (termin.getDatum() == null || termin.getVreme() == null) {
            throw new Exception("Termin mora imati datum i vreme.");
        }
        if (termin.getStatus() == null) {
            termin.setStatus(StatusTermina.ZAKAZAN);
        }
        if (termin.getStomatolog() == null || termin.getStomatolog().getIdStomatolog() <= 0) {
            throw new Exception("Termin mora imati stomatologa.");
        }
        if (termin.getPacijent() == null || termin.getPacijent().getIdPacijent() <= 0) {
            throw new Exception("Termin mora imati pacijenta.");
        }
        if (termin.getNapomena() == null) {
            termin.setNapomena("");
        }
    }

    /**
     * Poslovno pravilo: isti stomatolog ne moze da ima dva termina koja nisu
     * otkazana u istom danu i u isto vreme.
     */
    private void proveriZauzetost(Termin termin) throws Exception {
        String uslov = "idStomatolog = " + termin.getStomatolog().getIdStomatolog()
                + " AND datum = '" + termin.getDatum() + "'"
                + " AND vreme = '" + termin.getVreme() + "'"
                + " AND status <> '" + StatusTermina.OTKAZAN.name() + "'"
                + " AND idTermin <> " + termin.getIdTermin();

        if (!dao.vratiPoUpitu(new Termin(), uslov).isEmpty()) {
            throw new Exception("Stomatolog vec ima zakazan termin "
                    + termin.getDatum() + " u " + termin.getVreme() + ".");
        }
    }

    /**
     * Zatvara transakciju koju je otvorilo citanje.
     *
     * Konekcija radi sa iskljucenim automatskim potvrdjivanjem, pa i obican
     * SELECT otvara transakciju. Dok je ona otvorena, InnoDB u podrazumevanom
     * nivou izolacije REPEATABLE READ vraca isti snimak podataka, pa klijent ne
     * bi video izmene koje su u medjuvremenu potvrdjene sa druge strane.
     * Posto citanje nista ne menja, transakcija se ponistava.
     */
    private void zavrsiCitanje() {
        Konekcija.getInstanca().ponistiTransakciju();
    }

    /**
     * Udvostrucava apostrof da bi vrednost mogla da se ugradi u SQL upit.
     */
    private String escapiraj(String vrednost) {
        return vrednost.replace("'", "''");
    }
}
