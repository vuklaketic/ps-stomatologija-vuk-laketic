/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forma;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import kontroler.Kontroler;
import model.Pacijent;
import model.StatusTermina;
import model.StavkaTermina;
import model.Termin;
import model.Usluga;

/**
 * Modalni dijalog za unos novog termina, odnosno za izmenu postojeceg.
 *
 * Ako je u konstruktoru prosledjen termin, dijalog radi u rezimu izmene,
 * u suprotnom kreira novi termin.
 *
 * @author vukla
 */
public class NoviTerminDijalog extends JDialog {

    /**
     * Strogi format datuma - uz ResolverStyle.STRICT (i sablon uuuu umesto yyyy)
     * odbacuju se i nepostojeci datumi poput 2026-02-31.
     */
    private static final DateTimeFormatter FORMAT_DATUMA =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter FORMAT_VREMENA = DateTimeFormatter.ofPattern("HH:mm");

    /** Radno vreme ordinacije i korak izmedju dva slobodna termina. */
    private static final LocalTime POCETAK_RADNOG_VREMENA = LocalTime.of(8, 0);
    private static final LocalTime KRAJ_RADNOG_VREMENA = LocalTime.of(20, 0);
    private static final int KORAK_MINUTA = 15;

    /** Gornja granica sirine polja da dijalog ne bi bio prosiren dugackim nazivima. */
    private static final int MAKS_SIRINA_POLJA = 320;

    private final GlavnaForma roditeljskaForma;
    private final Termin terminZaIzmenu;

    private JComboBox<Pacijent> cmbPacijent;
    private JComboBox<Usluga> cmbUsluga;
    private JComboBox<StatusTermina> cmbStatus;
    private JTextField txtDatum;
    private JComboBox<LocalTime> cmbVreme;
    private JTextArea txtNapomena;
    private JButton btnPotvrdi;
    private JButton btnOdustani;

    public NoviTerminDijalog(GlavnaForma roditeljskaForma, Termin terminZaIzmenu) {
        super(roditeljskaForma, true);
        this.roditeljskaForma = roditeljskaForma;
        this.terminZaIzmenu = terminZaIzmenu;

        inicijalizujKomponente();
        ucitajListe();
        popuniPodatke();

        // dijalog se pakuje tek kada su sve liste popunjene, jer se tek tada
        // zna stvarna sirina komponenti - u suprotnom bi labele bile odsecene
        ogranicSirinu(cmbPacijent);
        ogranicSirinu(cmbUsluga);
        pack();
        setLocationRelativeTo(roditeljskaForma);
    }

    private void inicijalizujKomponente() {
        setTitle(jeIzmena() ? "Izmena termina" : "Novi termin");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        cmbPacijent = new JComboBox<>();
        cmbPacijent.setRenderer(new RendererPacijenta());
        cmbUsluga = new JComboBox<>();
        cmbUsluga.setRenderer(new RendererUsluge());
        cmbStatus = new JComboBox<>(StatusTermina.values());
        txtDatum = new JTextField(15);
        cmbVreme = napraviBiracVremena();
        // napomena se pise u vise redova, pa polje raste nadole, a ne u stranu
        txtNapomena = new JTextArea(4, 15);
        txtNapomena.setLineWrap(true);
        txtNapomena.setWrapStyleWord(true);

        dodajRed(panel, gbc, 0, "Pacijent:", cmbPacijent);
        dodajRed(panel, gbc, 1, "Datum (yyyy-MM-dd):", txtDatum);
        dodajRed(panel, gbc, 2, "Vreme:", cmbVreme);
        dodajRed(panel, gbc, 3, "Usluga:", cmbUsluga);
        dodajRed(panel, gbc, 4, "Status:", cmbStatus);
        dodajVisokRed(panel, gbc, 5, "Napomena:", new JScrollPane(txtNapomena));

        add(panel, BorderLayout.CENTER);

        JPanel panelDugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelDugmad.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 10));
        btnPotvrdi = new JButton(jeIzmena() ? "Sačuvaj izmene" : "Zakaži termin");
        btnOdustani = new JButton("Odustani");
        panelDugmad.add(btnPotvrdi);
        panelDugmad.add(btnOdustani);
        add(panelDugmad, BorderLayout.SOUTH);

        btnPotvrdi.addActionListener(e -> potvrdi());
        btnOdustani.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnPotvrdi);
    }

    /**
     * Pravi padajucu listu termina u okviru radnog vremena, u koracima od 15 minuta.
     */
    private JComboBox<LocalTime> napraviBiracVremena() {
        JComboBox<LocalTime> combo = new JComboBox<>();
        for (LocalTime slot = POCETAK_RADNOG_VREMENA; !slot.isAfter(KRAJ_RADNOG_VREMENA);
                slot = slot.plusMinutes(KORAK_MINUTA)) {
            combo.addItem(slot);
        }
        combo.setRenderer(new RendererVremena());
        return combo;
    }

    private void dodajRed(JPanel panel, GridBagConstraints gbc, int red, String naziv, Component komponenta) {
        gbc.gridx = 0;
        gbc.gridy = red;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(naziv), gbc);

        gbc.gridx = 1;
        gbc.gridy = red;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(komponenta, gbc);
    }

    /**
     * Dodaje red u kome komponenta zauzima prostor po visini, a ne samo po
     * sirini. Labela se poravnava uz vrh, da stoji uz prvi red teksta.
     */
    private void dodajVisokRed(JPanel panel, GridBagConstraints gbc, int red, String naziv, Component komponenta) {
        gbc.gridx = 0;
        gbc.gridy = red;
        gbc.weightx = 0;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(naziv), gbc);

        gbc.gridx = 1;
        gbc.gridy = red;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(komponenta, gbc);

        // vracanje na podrazumevano stanje, da naredni redovi ostanu jednoredni
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
    }

    /**
     * Ogranicava sirinu komponente da dugacki nazivi ne bi razvukli ceo dijalog.
     */
    private void ogranicSirinu(Component komponenta) {
        Dimension zeljena = komponenta.getPreferredSize();
        if (zeljena.width > MAKS_SIRINA_POLJA) {
            komponenta.setPreferredSize(new Dimension(MAKS_SIRINA_POLJA, zeljena.height));
        }
    }

    /**
     * Ucitava liste pacijenata i usluga i puni combo box-ove.
     */
    private void ucitajListe() {
        try {
            for (Pacijent p : Kontroler.getInstanca().vratiListuPacijenata()) {
                cmbPacijent.addItem(p);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
        }

        try {
            for (Usluga u : Kontroler.getInstanca().vratiListuUsluga()) {
                cmbUsluga.addItem(u);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * U rezimu izmene popunjava polja podacima izabranog termina.
     */
    private void popuniPodatke() {
        if (!jeIzmena()) {
            // status novog termina nije stvar izbora - svaki novi termin je
            // zakazan, pa polje stoji prikazano ali onemoguceno
            cmbStatus.setSelectedItem(StatusTermina.ZAKAZAN);
            cmbStatus.setEnabled(false);
            txtDatum.setText(LocalDate.now().format(FORMAT_DATUMA));
            izaberiVreme(POCETAK_RADNOG_VREMENA);
            return;
        }

        if (terminZaIzmenu.getDatum() != null) {
            txtDatum.setText(terminZaIzmenu.getDatum().format(FORMAT_DATUMA));
        }
        izaberiVreme(terminZaIzmenu.getVreme());
        txtNapomena.setText(terminZaIzmenu.getNapomena());
        cmbStatus.setSelectedItem(terminZaIzmenu.getStatus());

        izaberiPacijenta(terminZaIzmenu.getPacijent());

        List<StavkaTermina> stavke = terminZaIzmenu.getStavke();
        if (stavke != null && !stavke.isEmpty()) {
            izaberiUslugu(stavke.get(0).getUsluga());
        }
    }

    /**
     * Bira zadato vreme u padajucoj listi. Ako postojeci termin nije zakazan
     * na tacan slot od 15 minuta, njegovo vreme se dodaje u listu da ne bi
     * bilo nehotice promenjeno prilikom izmene.
     */
    private void izaberiVreme(LocalTime vreme) {
        if (vreme == null) {
            return;
        }
        for (int i = 0; i < cmbVreme.getItemCount(); i++) {
            if (cmbVreme.getItemAt(i).equals(vreme)) {
                cmbVreme.setSelectedIndex(i);
                return;
            }
        }
        cmbVreme.addItem(vreme);
        cmbVreme.setSelectedItem(vreme);
    }

    private void izaberiPacijenta(Pacijent pacijent) {
        if (pacijent == null) {
            return;
        }
        for (int i = 0; i < cmbPacijent.getItemCount(); i++) {
            if (cmbPacijent.getItemAt(i).getIdPacijent() == pacijent.getIdPacijent()) {
                cmbPacijent.setSelectedIndex(i);
                return;
            }
        }
    }

    private void izaberiUslugu(Usluga usluga) {
        if (usluga == null) {
            return;
        }
        for (int i = 0; i < cmbUsluga.getItemCount(); i++) {
            if (cmbUsluga.getItemAt(i).getIdUsluga() == usluga.getIdUsluga()) {
                cmbUsluga.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * Validira unos, pravi termin i prosledjuje ga kontroleru.
     */
    private void potvrdi() {
        Pacijent pacijent = (Pacijent) cmbPacijent.getSelectedItem();
        if (pacijent == null) {
            JOptionPane.showMessageDialog(this, "Morate izabrati pacijenta.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usluga usluga = (Usluga) cmbUsluga.getSelectedItem();
        if (usluga == null) {
            JOptionPane.showMessageDialog(this, "Morate izabrati uslugu.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate datum = procitajDatum();
        if (datum == null) {
            return;
        }

        LocalTime vreme = (LocalTime) cmbVreme.getSelectedItem();
        if (vreme == null) {
            JOptionPane.showMessageDialog(this, "Morate izabrati vreme termina.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // pri kreiranju je status uvek ZAKAZAN, bez obzira na stanje polja;
        // pri izmeni ga korisnik bira (npr. OTKAZAN ili ODRZAN)
        StatusTermina status = jeIzmena()
                ? (StatusTermina) cmbStatus.getSelectedItem()
                : StatusTermina.ZAKAZAN;
        String napomena = txtNapomena.getText().trim();

        // u rezimu izmene zadrzava se postojeci id termina
        int idTermin = jeIzmena() ? terminZaIzmenu.getIdTermin() : 0;

        Termin termin = new Termin(idTermin, datum, vreme, status, napomena,
                Kontroler.getInstanca().getUlogovaniStomatolog(), pacijent);

        // konstruktor termina ne prima stavke - pravi praznu listu, pa se
        // izabrana usluga dodaje kao stavka naknadno
        List<StavkaTermina> stavke = new ArrayList<>();
        int kolicina = 1;
        stavke.add(new StavkaTermina(1, kolicina, usluga.getCena() * kolicina,
                usluga.getCena(), termin, usluga));
        termin.setStavke(stavke);

        try {
            if (jeIzmena()) {
                Kontroler.getInstanca().promeniTermin(termin);
            } else {
                Kontroler.getInstanca().ubaciTermin(termin);
            }
        } catch (Exception ex) {
            // dijalog ostaje otvoren da korisnik moze da ispravi podatke
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                jeIzmena() ? "Termin je uspešno izmenjen." : "Termin je uspešno zakazan.",
                "Uspeh", JOptionPane.INFORMATION_MESSAGE);

        roditeljskaForma.ucitajTermine();
        dispose();
    }

    private boolean jeIzmena() {
        return terminZaIzmenu != null;
    }

    /**
     * Cita i validira datum iz tekstualnog polja. Ako unos nije ispravan,
     * prikazuje poruku i vraca null, pa dijalog ostaje otvoren.
     */
    private LocalDate procitajDatum() {
        String unos = txtDatum.getText().trim();
        if (unos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Morate uneti datum termina.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            txtDatum.requestFocusInWindow();
            return null;
        }

        try {
            return LocalDate.parse(unos, FORMAT_DATUMA);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Datum \"" + unos + "\" nije ispravan.\n"
                    + "Datum se unosi u formatu yyyy-MM-dd, na primer " 
                    + LocalDate.now().format(FORMAT_DATUMA) + ".",
                    "Neispravan datum", JOptionPane.WARNING_MESSAGE);
            txtDatum.requestFocusInWindow();
            txtDatum.selectAll();
            return null;
        }
    }

    /**
     * Domenske klase nemaju toString, pa se prikaz u combo box-u resava
     * rendererom. Renderer pacijenta je izdvojen u zasebnu klasu, jer ga
     * koristi i pretraga na glavnoj formi.
     */
    private static class RendererUsluge extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object vrednost, int indeks,
                boolean izabran, boolean fokusiran) {
            super.getListCellRendererComponent(lista, vrednost, indeks, izabran, fokusiran);
            if (vrednost instanceof Usluga) {
                Usluga u = (Usluga) vrednost;
                setText(u.getNaziv() + " (" + u.getCena() + " din, " + u.getTrajanje() + " min)");
            }
            return this;
        }
    }

    private static class RendererVremena extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object vrednost, int indeks,
                boolean izabran, boolean fokusiran) {
            super.getListCellRendererComponent(lista, vrednost, indeks, izabran, fokusiran);
            if (vrednost instanceof LocalTime) {
                setText(((LocalTime) vrednost).format(FORMAT_VREMENA));
            }
            return this;
        }
    }
}
