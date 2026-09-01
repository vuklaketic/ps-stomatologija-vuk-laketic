package forma;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import kontroler.Kontroler;
import model.Ordinacija;
import model.Pacijent;

/**
 * Forma za rad sa pacijentima: pretraga, unos, izmena i brisanje.
 *
 * Otvara se iz glavne forme i radi kao zaseban prozor, pa stomatolog moze da
 * se vrati na termine bez zatvaranja ove forme.
 *
 * @author vukla
 */
public class UpravljanjePacijentimaForma extends JFrame {

    /** Stavka koja u padajucoj listi pretrage znaci "bez filtera". */
    private static final String SVE = "Sve";

    /** Do ove sirine polja pretrage mogu da se skupe u uskom prozoru. */
    private static final int NAJMANJA_SIRINA_POLJA = 45;

    private JTable tabelaPacijenata;
    private ModelTabelePacijent modelTabele;

    private JTextField txtTrazenoIme;
    private JTextField txtTrazenoPrezime;
    private JComboBox<Object> cmbTrazenaOrdinacija;
    private JButton btnPretrazi;
    private JButton btnResetuj;
    private JButton btnNazad;

    private JButton btnNovi;
    private JButton btnIzmeni;
    private JButton btnObrisi;
    private JButton btnOsvezi;

    /**
     * Kriterijumi poslednje pretrage. Cuvaju se odvojeno od polja forme, da
     * osvezavanje liste posle unosa ili brisanja ne bi zavisilo od onoga sto
     * korisnik u medjuvremenu kuca.
     */
    private String kriterijumIme;
    private String kriterijumPrezime;
    private Ordinacija kriterijumOrdinacija;

    public UpravljanjePacijentimaForma(JFrame roditeljskaForma) {
        inicijalizujKomponente(roditeljskaForma);
        ucitajOrdinacije();
        ucitajPacijente();
    }

    private void inicijalizujKomponente(JFrame roditeljskaForma) {
        setTitle("Pacijenti");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        setLayout(new BorderLayout());

        // u zaglavlju je naslov, a povratak na termine je u desnom uglu,
        // odvojen od akcionih dugmadi nad pacijentima
        JPanel panelZaglavlje = new JPanel(new BorderLayout());
        panelZaglavlje.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblNaslov = new JLabel("Pregled i unos pacijenata");
        lblNaslov.setFont(lblNaslov.getFont().deriveFont(Font.BOLD));
        panelZaglavlje.add(lblNaslov, BorderLayout.WEST);

        JPanel panelZaglavljeDesno = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnNazad = new JButton("Nazad");
        panelZaglavljeDesno.add(btnNazad);
        panelZaglavlje.add(panelZaglavljeDesno, BorderLayout.EAST);

        JPanel panelPretrage = napraviPanelPretrage();
        JPanel panelGore = new JPanel(new BorderLayout());
        panelGore.add(panelZaglavlje, BorderLayout.NORTH);
        panelGore.add(panelPretrage, BorderLayout.CENTER);
        add(panelGore, BorderLayout.NORTH);

        modelTabele = new ModelTabelePacijent(new ArrayList<Pacijent>());
        tabelaPacijenata = new JTable(modelTabele);
        tabelaPacijenata.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        podesiTabelu();

        JScrollPane klizac = new JScrollPane(tabelaPacijenata);
        klizac.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        add(klizac, BorderLayout.CENTER);

        JPanel panelDugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panelDugmad.setBorder(BorderFactory.createEmptyBorder(0, 20, 5, 10));
        btnNovi = new JButton("Novi pacijent");
        btnIzmeni = new JButton("Izmeni pacijenta");
        btnObrisi = new JButton("Obriši pacijenta");
        btnOsvezi = new JButton("Osveži");
        panelDugmad.add(btnNovi);
        panelDugmad.add(btnIzmeni);
        panelDugmad.add(btnObrisi);
        panelDugmad.add(btnOsvezi);
        add(panelDugmad, BorderLayout.SOUTH);

        btnNovi.addActionListener(e -> noviPacijent());
        btnIzmeni.addActionListener(e -> izmeniPacijenta());
        btnObrisi.addActionListener(e -> obrisiPacijenta());
        btnOsvezi.addActionListener(e -> ucitajPacijente(true));
        // zatvara se samo ovaj prozor, glavna forma ostaje otvorena
        btnNazad.addActionListener(e -> dispose());

        // najmanja sirina se izvodi iz stvarne sirine panela za pretragu, pa
        // red sa kriterijumima uvek stane ceo
        int najmanjaSirina = Math.max(900, panelPretrage.getPreferredSize().width + 40);
        setMinimumSize(new Dimension(najmanjaSirina, 550));
        setSize(Math.max(1000, najmanjaSirina), 600);
        setLocationRelativeTo(roditeljskaForma);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    /**
     * Pravi panel sa kriterijumima pretrage, u jednom redu koji se ne prelama.
     */
    private JPanel napraviPanelPretrage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 20, 10, 20),
                BorderFactory.createTitledBorder("Pretraga pacijenata")));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;

        txtTrazenoIme = new JTextField(12);
        txtTrazenoPrezime = new JTextField(12);
        cmbTrazenaOrdinacija = new JComboBox<>();
        cmbTrazenaOrdinacija.addItem(SVE);
        cmbTrazenaOrdinacija.setRenderer(new RendererOrdinacije());

        ogranicSirinu(txtTrazenoIme, 140);
        ogranicSirinu(txtTrazenoPrezime, 140);
        ogranicSirinu(cmbTrazenaOrdinacija, 220);

        int kolona = 0;
        kolona = dodajURed(panel, gbc, kolona, new JLabel("Ime:"));
        kolona = dodajURed(panel, gbc, kolona, txtTrazenoIme);
        kolona = dodajURed(panel, gbc, kolona, new JLabel("Prezime:"));
        kolona = dodajURed(panel, gbc, kolona, txtTrazenoPrezime);
        kolona = dodajURed(panel, gbc, kolona, new JLabel("Ordinacija:"));
        kolona = dodajURed(panel, gbc, kolona, cmbTrazenaOrdinacija);

        btnPretrazi = new JButton("Pretraži");
        btnResetuj = new JButton("Resetuj filtere");
        gbc.insets = new Insets(8, 10, 8, 0);
        kolona = dodajURed(panel, gbc, kolona, btnPretrazi);
        gbc.insets = new Insets(8, 5, 8, 5);
        kolona = dodajURed(panel, gbc, kolona, btnResetuj);

        // prazna celija na kraju preuzima visak sirine
        gbc.gridx = kolona;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JPanel(), gbc);

        btnPretrazi.addActionListener(e -> pretrazi());
        btnResetuj.addActionListener(e -> resetujFiltere());

        return panel;
    }

    private int dodajURed(JPanel panel, GridBagConstraints gbc, int kolona, Component komponenta) {
        gbc.gridx = kolona;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(komponenta, gbc);
        return kolona + 1;
    }

    private void ogranicSirinu(Component komponenta, int pozeljnaSirina) {
        int visina = komponenta.getPreferredSize().height;
        komponenta.setPreferredSize(new Dimension(pozeljnaSirina, visina));
        komponenta.setMinimumSize(new Dimension(NAJMANJA_SIRINA_POLJA, visina));
    }

    private void podesiTabelu() {
        tabelaPacijenata.setRowHeight(26);
        tabelaPacijenata.setFillsViewportHeight(true);
        tabelaPacijenata.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabelaPacijenata.getTableHeader().setFont(
                tabelaPacijenata.getTableHeader().getFont().deriveFont(Font.BOLD));
        tabelaPacijenata.getTableHeader().setReorderingAllowed(false);

        int[] sirine = {60, 160, 180, 160, 160, 260};
        TableColumnModel kolone = tabelaPacijenata.getColumnModel();
        for (int i = 0; i < kolone.getColumnCount() && i < sirine.length; i++) {
            kolone.getColumn(i).setPreferredWidth(sirine[i]);
        }
    }

    /**
     * Puni padajucu listu ordinacija za pretragu.
     */
    private void ucitajOrdinacije() {
        try {
            for (Ordinacija ordinacija : Kontroler.getInstanca().vratiListuOrdinacija()) {
                cmbTrazenaOrdinacija.addItem(ordinacija);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pretrazi() {
        Object izabranaOrdinacija = cmbTrazenaOrdinacija.getSelectedItem();

        kriterijumIme = txtTrazenoIme.getText().trim();
        kriterijumPrezime = txtTrazenoPrezime.getText().trim();
        kriterijumOrdinacija = izabranaOrdinacija instanceof Ordinacija
                ? (Ordinacija) izabranaOrdinacija : null;

        ucitajPacijente(true);
    }

    private void resetujFiltere() {
        txtTrazenoIme.setText("");
        txtTrazenoPrezime.setText("");
        cmbTrazenaOrdinacija.setSelectedItem(SVE);

        kriterijumIme = null;
        kriterijumPrezime = null;
        kriterijumOrdinacija = null;

        ucitajPacijente(true);
    }

    /**
     * Ucitava pacijente koji odgovaraju poslednje zadatim kriterijumima.
     *
     * Ovu inacicu koristi dijalog za pacijenta i brisanje pacijenta, pa se
     * prazna lista ne prijavljuje posebnom porukom.
     */
    public final void ucitajPacijente() {
        ucitajPacijente(false);
    }

    /**
     * @param prijaviPrazanRezultat true kada je prikaz liste posledica radnje
     * korisnika (pretraga, resetovanje filtera, osvezavanje), pa treba da dobije
     * poruku ako nijedan pacijent ne odgovara kriterijumima
     */
    private void ucitajPacijente(boolean prijaviPrazanRezultat) {
        List<Pacijent> pacijenti;
        try {
            pacijenti = Kontroler.getInstanca().pretraziPacijente(
                    kriterijumIme, kriterijumPrezime, kriterijumOrdinacija);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        modelTabele.postaviListu(pacijenti);

        if (prijaviPrazanRezultat && pacijenti.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Sistem ne može da nađe pacijente po zadatim kriterijumima.",
                    "Pretraga pacijenata", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void noviPacijent() {
        new NoviPacijentDijalog(this, null).setVisible(true);
    }

    private void izmeniPacijenta() {
        Pacijent izabrani = vratiIzabranogPacijenta();
        if (izabrani == null) {
            return;
        }
        new NoviPacijentDijalog(this, izabrani).setVisible(true);
    }

    /**
     * Brisanje pacijenta prati scenario slucaja koriscenja "Obrisi pacijenta":
     * sistem prvo javlja da je nasao izabranog pacijenta, zatim trazi potvrdu
     * brisanja i tek na potvrdu poziva sistemsku operaciju, o cijem ishodu
     * takodje izvestava.
     */
    private void obrisiPacijenta() {
        Pacijent izabrani = vratiIzabranogPacijenta();
        if (izabrani == null) {
            return;
        }

        JOptionPane.showMessageDialog(this, "Sistem je našao pacijenta.",
                "Pacijent", JOptionPane.INFORMATION_MESSAGE);

        int potvrda = JOptionPane.showConfirmDialog(this,
                "Da li ste sigurni da želite da obrišete pacijenta "
                + izabrani.getIme() + " " + izabrani.getPrezime() + "?",
                "Potvrda brisanja", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (potvrda != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Kontroler.getInstanca().obrisiPacijenta(izabrani);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Sistem je obrisao pacijenta.",
                "Pacijent", JOptionPane.INFORMATION_MESSAGE);
        ucitajPacijente();
    }

    /**
     * Vraca pacijenta izabranog u tabeli ili null uz upozorenje ako nista nije
     * izabrano.
     */
    private Pacijent vratiIzabranogPacijenta() {
        int red = tabelaPacijenata.getSelectedRow();
        if (red == -1) {
            JOptionPane.showMessageDialog(this, "Morate izabrati pacijenta iz tabele.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return modelTabele.vratiPacijenta(tabelaPacijenata.convertRowIndexToModel(red));
    }
}
