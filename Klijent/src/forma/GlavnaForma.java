/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forma;

import com.github.lgooddatepicker.components.DatePicker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
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
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import kontroler.Kontroler;
import model.Pacijent;
import model.StatusTermina;
import model.Stomatolog;
import model.Termin;

/**
 * Glavna forma klijenta - prikazuje termine ulogovanog stomatologa.
 *
 * @author vukla
 */
public class GlavnaForma extends JFrame {

    /** Stavka koja u padajucim listama pretrage znaci "bez filtera". */
    private static final String SVI = "Svi";

    private final Stomatolog ulogovaniStomatolog;

    private JTable tabelaTermina;
    private ModelTabeleTermin modelTabele;

    private DatePicker biracTrazenogDatuma;
    private JComboBox<Object> cmbTrazeniStatus;
    private JComboBox<Object> cmbTrazeniPacijent;
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

    private JButton btnNovi;
    private JButton btnIzmeni;
    private JButton btnObrisi;
    private JButton btnOsvezi;
    private JButton btnOdjava;

    public GlavnaForma() {
        this.ulogovaniStomatolog = Kontroler.getInstanca().getUlogovaniStomatolog();
        inicijalizujKomponente();
        ucitajPacijente();
        ucitajTermine();
    }

    private void inicijalizujKomponente() {
        setTitle("Zakazivanje termina - dr " + ulogovaniStomatolog.getIme()
                + " " + ulogovaniStomatolog.getPrezime());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(true);
        setLayout(new BorderLayout());

        // u zaglavlju su podaci o ulogovanom stomatologu, a odjava je u desnom
        // uglu, odvojena od akcionih dugmadi nad terminima
        JPanel panelZaglavlje = new JPanel(new BorderLayout());
        panelZaglavlje.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblUlogovani = new JLabel("Ulogovani stomatolog: "
                + ulogovaniStomatolog.getIme() + " " + ulogovaniStomatolog.getPrezime());
        lblUlogovani.setFont(lblUlogovani.getFont().deriveFont(Font.BOLD));
        panelZaglavlje.add(lblUlogovani, BorderLayout.WEST);

        btnOdjava = new JButton("Odjavi se");
        panelZaglavlje.add(btnOdjava, BorderLayout.EAST);

        // u gornjem delu forme stoje zaglavlje i, ispod njega, panel za pretragu
        JPanel panelGore = new JPanel(new BorderLayout());
        panelGore.add(panelZaglavlje, BorderLayout.NORTH);
        panelGore.add(napraviPanelPretrage(), BorderLayout.CENTER);
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
        btnOsvezi.addActionListener(e -> ucitajTermine());
        btnOdjava.addActionListener(e -> odjaviSe());

        // prozor se otvara maksimizovan, a ova velicina vazi kada ga korisnik
        // vrati iz maksimizovanog stanja
        setSize(1000, 600);
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
     * Pravi panel u kome stomatolog bira kriterijume pretrage termina.
     */
    private JPanel napraviPanelPretrage() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 20, 10, 20),
                BorderFactory.createTitledBorder("Pretraga termina")));

        panel.add(new JLabel("Datum:"));
        // prazan datum je dozvoljen i znaci da se po datumu ne filtrira
        biracTrazenogDatuma = BiracDatuma.napravi(true);
        panel.add(biracTrazenogDatuma);

        panel.add(new JLabel("Status:"));
        cmbTrazeniStatus = new JComboBox<>();
        cmbTrazeniStatus.addItem(SVI);
        for (StatusTermina status : StatusTermina.values()) {
            cmbTrazeniStatus.addItem(status);
        }
        panel.add(cmbTrazeniStatus);

        panel.add(new JLabel("Pacijent:"));
        cmbTrazeniPacijent = new JComboBox<>();
        cmbTrazeniPacijent.addItem(SVI);
        cmbTrazeniPacijent.setRenderer(new RendererPacijenta());
        panel.add(cmbTrazeniPacijent);

        btnPretrazi = new JButton("Pretraži");
        btnResetuj = new JButton("Resetuj filtere");
        panel.add(btnPretrazi);
        panel.add(btnResetuj);

        btnPretrazi.addActionListener(e -> pretrazi());
        btnResetuj.addActionListener(e -> resetujFiltere());

        return panel;
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
     * Cita kriterijume iz polja i prikazuje termine koji im odgovaraju.
     */
    private void pretrazi() {
        // kalendar vraca null kada datum nije izabran, sto znaci bez filtera
        LocalDate datum = biracTrazenogDatuma.getDate();

        Object izabranStatus = cmbTrazeniStatus.getSelectedItem();
        Object izabranPacijent = cmbTrazeniPacijent.getSelectedItem();

        kriterijumDatum = datum;
        kriterijumStatus = izabranStatus instanceof StatusTermina ? (StatusTermina) izabranStatus : null;
        kriterijumPacijent = izabranPacijent instanceof Pacijent ? (Pacijent) izabranPacijent : null;

        ucitajTermine();
    }

    /**
     * Prazni polja pretrage i vraca prikaz svih termina ulogovanog stomatologa.
     */
    private void resetujFiltere() {
        biracTrazenogDatuma.clear();
        cmbTrazeniStatus.setSelectedItem(SVI);
        cmbTrazeniPacijent.setSelectedItem(SVI);

        kriterijumDatum = null;
        kriterijumStatus = null;
        kriterijumPacijent = null;

        ucitajTermine();
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
     * Ucitava termine ulogovanog stomatologa koji odgovaraju poslednje zadatim
     * kriterijumima pretrage i prikazuje ih u tabeli. Ako kriterijumi nisu
     * zadati, prikazuju se svi njegovi termini.
     */
    public final void ucitajTermine() {
        try {
            List<Termin> termini = Kontroler.getInstanca().pretraziTermineUlogovanog(
                    kriterijumDatum, kriterijumStatus, kriterijumPacijent);
            modelTabele.postaviListu(termini);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void noviTermin() {
        new NoviTerminDijalog(this, null).setVisible(true);
    }

    private void izmeniTermin() {
        Termin izabrani = vratiIzabraniTermin();
        if (izabrani == null) {
            return;
        }
        new NoviTerminDijalog(this, izabrani).setVisible(true);
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

        JOptionPane.showMessageDialog(this, "Termin je uspešno obrisan.",
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
