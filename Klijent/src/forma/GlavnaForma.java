/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forma;

import com.github.lgooddatepicker.components.DatePicker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import kontroler.Kontroler;
import model.Pacijent;
import model.Specijalizacija;
import model.StatusTermina;
import model.Stomatolog;
import model.Termin;
import model.Usluga;

/**
 * Glavna forma klijenta - prikazuje termine ulogovanog stomatologa.
 *
 * @author vukla
 */
public class GlavnaForma extends JFrame {

    /** Stavka koja u padajucim listama pretrage znaci "bez filtera". */
    private static final String SVI = "Svi";
    private static final String SVE = "Sve";

    /** Do ove sirine polja pretrage mogu da se skupe u uskom prozoru. */
    private static final int NAJMANJA_SIRINA_POLJA = 45;

    private final Stomatolog ulogovaniStomatolog;

    private JLabel lblSpecijalizacije;

    private JTable tabelaTermina;
    private ModelTabeleTermin modelTabele;

    private DatePicker biracTrazenogDatuma;
    private JComboBox<Object> cmbTrazeniStatus;
    private JComboBox<Object> cmbTrazeniPacijent;
    private JComboBox<Object> cmbTrazenaUsluga;
    private JButton btnPretrazi;
    private JButton btnResetuj;

    /**
     * Kriterijumi po kojima je poslednja pretraga izvrsena. Cuvaju se odvojeno
     * od polja forme, da osvezavanje liste posle unosa ili brisanja termina ne
     * bi ponovo proveravalo ono sto korisnik u medjuvremenu kuca u poljima.
     */
    private LocalDate kriterijumDatum;
    private StatusTermina kriterijumStatus;
    private Pacijent kriterijumPacijent;
    private Usluga kriterijumUsluga;

    private JMenuItem mniNoviTermin;
    private JMenuItem mniIzmeniTermin;
    private JMenuItem mniObrisiTermin;
    private JMenuItem mniOsvezi;
    private JMenuItem mniPacijenti;
    private JMenuItem mniSpecijalizacija;
    private JMenuItem mniOProgramu;
    private JMenuItem mniOdjava;

    private JButton btnNovi;
    private JButton btnIzmeni;
    private JButton btnObrisi;
    private JButton btnOsvezi;
    private JButton btnPacijenti;
    private JButton btnSpecijalizacija;
    private JButton btnOdjava;

    public GlavnaForma() {
        this.ulogovaniStomatolog = Kontroler.getInstanca().getUlogovaniStomatolog();
        inicijalizujKomponente();
        ucitajSpecijalizacije();
        ucitajPacijente();
        ucitajUsluge();
        ucitajTermine();
    }

    private void inicijalizujKomponente() {
        setTitle("Zakazivanje termina - dr " + ulogovaniStomatolog.getIme()
                + " " + ulogovaniStomatolog.getPrezime());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(true);
        setLayout(new BorderLayout());

        setJMenuBar(napraviMeni());

        // u zaglavlju su podaci o ulogovanom stomatologu, a odjava je u desnom
        // uglu, odvojena od akcionih dugmadi nad terminima
        JPanel panelZaglavlje = new JPanel(new BorderLayout());
        panelZaglavlje.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel panelUlogovani = new JPanel();
        panelUlogovani.setLayout(new BoxLayout(panelUlogovani, BoxLayout.Y_AXIS));
        panelUlogovani.setOpaque(false);

        JLabel lblUlogovani = new JLabel("Ulogovani stomatolog: "
                + ulogovaniStomatolog.getIme() + " " + ulogovaniStomatolog.getPrezime());
        lblUlogovani.setFont(lblUlogovani.getFont().deriveFont(Font.BOLD));
        lblUlogovani.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelUlogovani.add(lblUlogovani);

        // linija sa specijalizacijama se popunjava tek posle citanja sa servera,
        // pa za stomatologa koji nema nijednu ostaje sakrivena
        lblSpecijalizacije = new JLabel();
        lblSpecijalizacije.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSpecijalizacije.setVisible(false);
        panelUlogovani.add(lblSpecijalizacije);

        panelZaglavlje.add(panelUlogovani, BorderLayout.WEST);

        // desno u zaglavlju stoje prelazak na pacijente i odjava
        JPanel panelZaglavljeDesno = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPacijenti = new JButton("Pacijenti");
        btnSpecijalizacija = new JButton("Nova specijalizacija");
        btnOdjava = new JButton("Odjavi se");
        panelZaglavljeDesno.add(btnPacijenti);
        panelZaglavljeDesno.add(btnSpecijalizacija);
        panelZaglavljeDesno.add(btnOdjava);
        panelZaglavlje.add(panelZaglavljeDesno, BorderLayout.EAST);

        // u gornjem delu forme stoje zaglavlje i, ispod njega, panel za pretragu
        JPanel panelPretrage = napraviPanelPretrage();
        JPanel panelGore = new JPanel(new BorderLayout());
        panelGore.add(panelZaglavlje, BorderLayout.NORTH);
        panelGore.add(panelPretrage, BorderLayout.CENTER);
        add(panelGore, BorderLayout.NORTH);

        modelTabele = new ModelTabeleTermin(new ArrayList<Termin>());
        tabelaTermina = new JTable(modelTabele);
        tabelaTermina.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        podesiTabelu();

        JScrollPane klizac = new JScrollPane(tabelaTermina);
        klizac.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        add(klizac, BorderLayout.CENTER);

        // razmak izmedju dugmadi i odvajanje od ivica prozora
        JPanel panelDugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panelDugmad.setBorder(BorderFactory.createEmptyBorder(0, 20, 5, 10));
        btnNovi = new JButton("Novi termin");
        btnIzmeni = new JButton("Izmeni termin");
        btnObrisi = new JButton("Obriši termin");
        btnOsvezi = new JButton("Osveži");

        panelDugmad.add(btnNovi);
        panelDugmad.add(btnIzmeni);
        panelDugmad.add(btnObrisi);
        panelDugmad.add(btnOsvezi);
        add(panelDugmad, BorderLayout.SOUTH);

        btnNovi.addActionListener(e -> noviTermin());
        btnIzmeni.addActionListener(e -> izmeniTermin());
        btnObrisi.addActionListener(e -> obrisiTermin());
        btnOsvezi.addActionListener(e -> ucitajTermine(true));
        btnPacijenti.addActionListener(e -> otvoriPacijente());
        btnSpecijalizacija.addActionListener(e -> novaSpecijalizacija());
        btnOdjava.addActionListener(e -> odjaviSe());

        // meni i dugmad su dva ulaza u istu funkcionalnost, pa pozivaju iste
        // metode - nijedna radnja nije napisana dvaput
        mniNoviTermin.addActionListener(e -> noviTermin());
        mniIzmeniTermin.addActionListener(e -> izmeniTermin());
        mniObrisiTermin.addActionListener(e -> obrisiTermin());
        mniOsvezi.addActionListener(e -> ucitajTermine(true));
        mniPacijenti.addActionListener(e -> otvoriPacijente());
        mniSpecijalizacija.addActionListener(e -> novaSpecijalizacija());
        mniOProgramu.addActionListener(e -> oProgramu());
        mniOdjava.addActionListener(e -> odjaviSe());

        // najmanja sirina prozora se izvodi iz stvarne sirine panela za
        // pretragu, pa red sa kriterijumima uvek stane ceo, bez obzira na to
        // koliko je siroka slova tema iscrtala
        int najmanjaSirina = Math.max(900, panelPretrage.getPreferredSize().width + 40);
        setMinimumSize(new Dimension(najmanjaSirina, 550));

        // prozor se otvara maksimizovan, a ova velicina vazi kada ga korisnik
        // vrati iz maksimizovanog stanja
        setSize(Math.max(1000, najmanjaSirina), 600);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // pri zatvaranju forme zatvara se i veza sa serverom
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                zatvoriAplikaciju();
            }
        });
    }

    /**
     * Pravi glavni meni forme.
     *
     * Meni pokriva iste radnje kao dugmad na formi: dugmad su brzi pristup
     * onome sto se najcesce koristi, a meni drzi sve radnje na jednom mestu i
     * grupisane po celinama nad kojima se rade.
     */
    private JMenuBar napraviMeni() {
        JMenuBar meni = new JMenuBar();

        // dokumenti - termin je dokument koji opisuje pruzanje usluge
        JMenu meniDokumenti = new JMenu("Dokumenti");
        meniDokumenti.setMnemonic('D');

        JMenu podmeniTermin = new JMenu("Termin");
        podmeniTermin.setMnemonic('T');
        mniNoviTermin = napraviStavku("Novi termin", 'N', KeyEvent.VK_N);
        mniIzmeniTermin = napraviStavku("Izmeni termin", 'I', KeyEvent.VK_E);
        mniObrisiTermin = napraviStavku("Obriši termin", 'O', KeyEvent.VK_D);
        mniOsvezi = napraviStavku("Osveži", 'v', 0);
        // osvezavanje ima uobicajenu precicu, bez modifikatora
        mniOsvezi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        podmeniTermin.add(mniNoviTermin);
        podmeniTermin.add(mniIzmeniTermin);
        podmeniTermin.add(mniObrisiTermin);
        podmeniTermin.addSeparator();
        podmeniTermin.add(mniOsvezi);
        meniDokumenti.add(podmeniTermin);
        meni.add(meniDokumenti);

        // primalac usluge - pacijent
        JMenu meniPrimalac = new JMenu("Primalac usluge");
        meniPrimalac.setMnemonic('P');

        JMenu podmeniPacijent = new JMenu("Pacijent");
        podmeniPacijent.setMnemonic('a');
        mniPacijenti = napraviStavku("Upravljanje pacijentima...", 'U', 0);
        podmeniPacijent.add(mniPacijenti);
        meniPrimalac.add(podmeniPacijent);
        meni.add(meniPrimalac);

        // sifarnici - od tri sifarnika iz specifikacije, forma postoji samo za
        // specijalizaciju, pa se ostala dva ne prikazuju kao prazne stavke
        JMenu meniSifarnici = new JMenu("Šifarnici");
        meniSifarnici.setMnemonic('S');

        JMenu podmeniSpecijalizacija = new JMenu("Specijalizacija");
        podmeniSpecijalizacija.setMnemonic('c');
        mniSpecijalizacija = napraviStavku("Nova specijalizacija...", 'N', 0);
        podmeniSpecijalizacija.add(mniSpecijalizacija);
        meniSifarnici.add(podmeniSpecijalizacija);
        meni.add(meniSifarnici);

        // podesavanja - klijent nema sta da podesava, jer se podaci o bazi
        // zadaju na serverskoj formi, pa je ovde samo podatak o programu
        JMenu meniPodesavanja = new JMenu("Podešavanja");
        meniPodesavanja.setMnemonic('e');
        mniOProgramu = napraviStavku("O programu", 'O', 0);
        meniPodesavanja.add(mniOProgramu);
        meni.add(meniPodesavanja);

        JMenu meniNalog = new JMenu("Nalog");
        meniNalog.setMnemonic('N');
        mniOdjava = napraviStavku("Odjavi se", 'j', 0);
        meniNalog.add(mniOdjava);
        meni.add(meniNalog);

        return meni;
    }

    /**
     * Pravi stavku menija sa slovom za pristup tastaturom i, ako je zadat,
     * tasterom precice uz sistemski modifikator (Ctrl, odnosno Command).
     *
     * @param tasterPrecice kod tastera ili 0 kada stavka nema precicu
     */
    private JMenuItem napraviStavku(String naziv, char slovo, int tasterPrecice) {
        JMenuItem stavka = new JMenuItem(naziv);
        stavka.setMnemonic(slovo);
        if (tasterPrecice != 0) {
            stavka.setAccelerator(KeyStroke.getKeyStroke(tasterPrecice,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        }
        return stavka;
    }

    /**
     * Pravi panel u kome stomatolog bira kriterijume pretrage termina.
     *
     * Panel koristi GridBagLayout i sve drzi u jednom redu. FlowLayout ovde ne
     * moze da se koristi: kada prozor nije dovoljno sirok, on prelama
     * komponente u novi red, ali za visinu i dalje prijavljuje jedan red, pa
     * prelomljena dugmad ispadnu iz panela i naljegnu na tabelu ispod.
     */
    private JPanel napraviPanelPretrage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 20, 10, 20),
                BorderFactory.createTitledBorder("Pretraga termina")));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // prazan datum je dozvoljen i znaci da se po datumu ne filtrira
        biracTrazenogDatuma = BiracDatuma.napravi(true);

        cmbTrazeniStatus = new JComboBox<>();
        cmbTrazeniStatus.addItem(SVI);
        for (StatusTermina status : StatusTermina.values()) {
            cmbTrazeniStatus.addItem(status);
        }

        cmbTrazeniPacijent = new JComboBox<>();
        cmbTrazeniPacijent.addItem(SVI);
        cmbTrazeniPacijent.setRenderer(new RendererPacijenta());

        cmbTrazenaUsluga = new JComboBox<>();
        cmbTrazenaUsluga.addItem(SVE);
        cmbTrazenaUsluga.setRenderer(new RendererNaziva());

        // polja imaju malu najmanju sirinu, pa se u uskom prozoru skupljaju
        // umesto da guraju dugmad van vidljivog dela panela; duga imena
        // pacijenata se u padajucoj listi i dalje vide u punoj duzini
        ogranicSirinu(biracTrazenogDatuma, 150);
        ogranicSirinu(cmbTrazeniStatus, 130);
        ogranicSirinu(cmbTrazeniPacijent, 200);
        ogranicSirinu(cmbTrazenaUsluga, 180);

        int kolona = 0;
        kolona = dodajURed(panel, gbc, kolona, new JLabel("Datum:"));
        kolona = dodajURed(panel, gbc, kolona, biracTrazenogDatuma);
        kolona = dodajURed(panel, gbc, kolona, new JLabel("Status:"));
        kolona = dodajURed(panel, gbc, kolona, cmbTrazeniStatus);
        kolona = dodajURed(panel, gbc, kolona, new JLabel("Pacijent:"));
        kolona = dodajURed(panel, gbc, kolona, cmbTrazeniPacijent);
        kolona = dodajURed(panel, gbc, kolona, new JLabel("Usluga:"));
        kolona = dodajURed(panel, gbc, kolona, cmbTrazenaUsluga);

        btnPretrazi = new JButton("Pretraži");
        btnResetuj = new JButton("Resetuj filtere");
        gbc.insets = new Insets(8, 10, 8, 0);
        kolona = dodajURed(panel, gbc, kolona, btnPretrazi);
        gbc.insets = new Insets(8, 5, 8, 5);
        kolona = dodajURed(panel, gbc, kolona, btnResetuj);

        // prazna celija na kraju preuzima visak sirine, pa kriterijumi ostaju
        // sabijeni uz levu ivicu umesto da se razvlace po celom prozoru
        gbc.gridx = kolona;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JPanel(), gbc);

        btnPretrazi.addActionListener(e -> pretrazi());
        btnResetuj.addActionListener(e -> resetujFiltere());

        return panel;
    }

    /**
     * Zadaje komponenti pozeljnu sirinu i znatno manju najmanju sirinu, da bi
     * layout imao odakle da uzme prostor kada je prozor uzak.
     */
    private void ogranicSirinu(Component komponenta, int pozeljnaSirina) {
        int visina = komponenta.getPreferredSize().height;
        komponenta.setPreferredSize(new Dimension(pozeljnaSirina, visina));
        komponenta.setMinimumSize(new Dimension(NAJMANJA_SIRINA_POLJA, visina));
    }

    /**
     * Dodaje komponentu u tekucu kolonu reda i vraca broj naredne kolone.
     */
    private int dodajURed(JPanel panel, GridBagConstraints gbc, int kolona, Component komponenta) {
        gbc.gridx = kolona;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(komponenta, gbc);
        return kolona + 1;
    }

    /**
     * Prikazuje specijalizacije ulogovanog stomatologa u zaglavlju. Ako ih
     * stomatolog nema, linija se ne prikazuje, da zaglavlje ne bi nosilo
     * podatak koji nista ne govori.
     */
    private void ucitajSpecijalizacije() {
        try {
            List<String> nazivi = new ArrayList<>();
            for (Specijalizacija specijalizacija
                    : Kontroler.getInstanca().vratiSpecijalizacijeUlogovanog()) {
                nazivi.add(specijalizacija.getNaziv());
            }

            if (!nazivi.isEmpty()) {
                lblSpecijalizacije.setText("Specijalizacije: " + String.join(", ", nazivi));
                lblSpecijalizacije.setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Puni padajucu listu pacijenata za pretragu.
     */
    private void ucitajPacijente() {
        try {
            for (Pacijent pacijent : Kontroler.getInstanca().vratiListuPacijenata()) {
                cmbTrazeniPacijent.addItem(pacijent);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Puni padajucu listu usluga za pretragu.
     */
    private void ucitajUsluge() {
        try {
            for (Usluga usluga : Kontroler.getInstanca().vratiListuUsluga()) {
                cmbTrazenaUsluga.addItem(usluga);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cita kriterijume iz polja i prikazuje termine koji im odgovaraju.
     */
    private void pretrazi() {
        // kalendar vraca null kada datum nije izabran, sto znaci bez filtera
        LocalDate datum = biracTrazenogDatuma.getDate();

        Object izabranStatus = cmbTrazeniStatus.getSelectedItem();
        Object izabranPacijent = cmbTrazeniPacijent.getSelectedItem();
        Object izabranaUsluga = cmbTrazenaUsluga.getSelectedItem();

        kriterijumDatum = datum;
        kriterijumStatus = izabranStatus instanceof StatusTermina ? (StatusTermina) izabranStatus : null;
        kriterijumPacijent = izabranPacijent instanceof Pacijent ? (Pacijent) izabranPacijent : null;
        kriterijumUsluga = izabranaUsluga instanceof Usluga ? (Usluga) izabranaUsluga : null;

        ucitajTermine(true);
    }

    /**
     * Prazni polja pretrage i vraca prikaz svih termina ulogovanog stomatologa.
     */
    private void resetujFiltere() {
        biracTrazenogDatuma.clear();
        cmbTrazeniStatus.setSelectedItem(SVI);
        cmbTrazeniPacijent.setSelectedItem(SVI);
        cmbTrazenaUsluga.setSelectedItem(SVE);

        kriterijumDatum = null;
        kriterijumStatus = null;
        kriterijumPacijent = null;
        kriterijumUsluga = null;

        ucitajTermine(true);
    }

    /**
     * Podesava izgled tabele: visinu redova, raspodelu sirina kolona i
     * podebljano zaglavlje. Kolone se rasporedjuju po celoj sirini prozora, a
     * zadate sirine odredjuju odnos izmedju njih.
     */
    private void podesiTabelu() {
        tabelaTermina.setRowHeight(26);
        tabelaTermina.setFillsViewportHeight(true);
        tabelaTermina.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabelaTermina.getTableHeader().setFont(
                tabelaTermina.getTableHeader().getFont().deriveFont(Font.BOLD));
        tabelaTermina.getTableHeader().setReorderingAllowed(false);

        int[] sirine = {60, 120, 90, 260, 140, 400};
        TableColumnModel kolone = tabelaTermina.getColumnModel();
        for (int i = 0; i < kolone.getColumnCount() && i < sirine.length; i++) {
            kolone.getColumn(i).setPreferredWidth(sirine[i]);
        }
    }

    /**
     * Prikaz usluge u padajucoj listi pretrage. Stavke koje nisu usluga (na
     * primer tekst "Sve") prikazuju se onako kako ih prikazuje podrazumevani
     * renderer.
     */
    private static class RendererNaziva extends javax.swing.DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(javax.swing.JList<?> lista, Object vrednost,
                int indeks, boolean izabran, boolean fokusiran) {
            super.getListCellRendererComponent(lista, vrednost, indeks, izabran, fokusiran);
            if (vrednost instanceof Usluga) {
                setText(((Usluga) vrednost).getNaziv());
            }
            return this;
        }
    }

    /**
     * Ucitava termine ulogovanog stomatologa koji odgovaraju poslednje zadatim
     * kriterijumima pretrage i prikazuje ih u tabeli. Ako kriterijumi nisu
     * zadati, prikazuju se svi njegovi termini.
     *
     * Ovu inacicu koriste dijalog za termin i brisanje termina, pa se prazna
     * lista ne prijavljuje posebnom porukom.
     */
    public final void ucitajTermine() {
        ucitajTermine(false);
    }

    /**
     * @param prijaviIshod true kada je prikaz liste posledica pretrage koju je
     * pokrenuo korisnik (pretraga, resetovanje filtera, osvezavanje), pa
     * scenario trazi poruku o ishodu - i kada su termini nadjeni i kada nisu;
     * false pri osvezavanju posle unosa, izmene ili brisanja termina, gde je
     * korisnik vec dobio poruku o ishodu same radnje
     */
    private void ucitajTermine(boolean prijaviIshod) {
        List<Termin> termini;
        try {
            termini = Kontroler.getInstanca().pretraziTermineUlogovanog(
                    kriterijumDatum, kriterijumStatus, kriterijumPacijent, kriterijumUsluga);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        modelTabele.postaviListu(termini);

        if (prijaviIshod) {
            JOptionPane.showMessageDialog(this,
                    termini.isEmpty()
                            ? "Sistem ne može da nađe termine po zadatim kriterijumima."
                            : "Sistem je našao termine po zadatim kriterijumima.",
                    "Pretraga termina", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Prikazuje osnovne podatke o programu.
     */
    private void oProgramu() {
        JOptionPane.showMessageDialog(this,
                "Softverski sistem za zakazivanje termina\n"
                + "stomatološke ordinacije\n\n"
                + "Verzija 1.0\n\n"
                + "Seminarski rad iz predmeta Projektovanje softvera\n"
                + "Fakultet organizacionih nauka, Beograd\n\n"
                + "Student: Vuk Laketić, 2023-0013\n"
                + "Mentor: prof. dr Siniša Vlajić",
                "O programu", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Otvara formu za rad sa specijalizacijom.
     */
    private void novaSpecijalizacija() {
        new NovaSpecijalizacijaDijalog(this).setVisible(true);
    }

    private void noviTermin() {
        new NoviTerminDijalog(this, null).setVisible(true);
    }

    /**
     * Otvara dijalog za izmenu izabranog termina.
     *
     * Scenario razlikuje pretragu liste termina od trazenja jednog, izabranog
     * termina, pa se i ovde termin ponovo trazi na serveru. Tako dijalog
     * dobija svez podatak, zajedno sa stavkama koje termin ima u bazi.
     */
    private void izmeniTermin() {
        Termin izabrani = vratiIzabraniTermin();
        if (izabrani == null) {
            return;
        }

        Termin pronadjeni = nadjiTermin(izabrani);
        if (pronadjeni == null) {
            return;
        }

        JOptionPane.showMessageDialog(this, "Sistem je našao termin.",
                "Termin", JOptionPane.INFORMATION_MESSAGE);

        new NoviTerminDijalog(this, pronadjeni).setVisible(true);
    }

    /**
     * Trazi izabrani termin na serveru.
     *
     * @return pronadjeni termin ili null ako termina vise nema ili trazenje
     *         nije uspelo, uz poruku koju je korisnik vec dobio
     */
    private Termin nadjiTermin(Termin izabrani) {
        Termin pronadjeni;
        try {
            pronadjeni = Kontroler.getInstanca().pretraziTermin(izabrani.getIdTermin());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (pronadjeni == null) {
            JOptionPane.showMessageDialog(this, "Sistem ne može da nađe termin.",
                    "Termin", JOptionPane.INFORMATION_MESSAGE);
            ucitajTermine();
        }
        return pronadjeni;
    }

    private void obrisiTermin() {
        Termin izabrani = vratiIzabraniTermin();
        if (izabrani == null) {
            return;
        }

        int potvrda = JOptionPane.showConfirmDialog(this,
                "Da li ste sigurni da želite da obrišete izabrani termin?",
                "Potvrda brisanja", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (potvrda != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Kontroler.getInstanca().obrisiTermin(izabrani);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Sistem je obrisao termin.",
                "Uspeh", JOptionPane.INFORMATION_MESSAGE);
        ucitajTermine();
    }

    /**
     * Vraca termin izabran u tabeli ili null uz upozorenje ako nista nije izabrano.
     */
    private Termin vratiIzabraniTermin() {
        int red = tabelaTermina.getSelectedRow();
        if (red == -1) {
            JOptionPane.showMessageDialog(this, "Morate izabrati termin iz tabele.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return modelTabele.vratiTermin(tabelaTermina.convertRowIndexToModel(red));
    }

    /**
     * Otvara formu za rad sa pacijentima kao zaseban prozor.
     */
    private void otvoriPacijente() {
        new UpravljanjePacijentimaForma(this).setVisible(true);
    }

    /**
     * Odjavljuje stomatologa i vraca korisnika na formu za prijavu.
     *
     * Kontroler pri odjavi zatvara i vezu sa serverom, a Komunikacija pri
     * zatvaranju ponistava svoju instancu, pa se pri sledecoj prijavi otvara
     * nova veza. Aplikacija nastavlja da radi - ne poziva se System.exit.
     */
    private void odjaviSe() {
        Kontroler.getInstanca().odjaviSe();
        dispose();
        new LoginForma().setVisible(true);
    }

    private void zatvoriAplikaciju() {
        Kontroler.getInstanca().odjaviSe();
        dispose();
        System.exit(0);
    }
}
