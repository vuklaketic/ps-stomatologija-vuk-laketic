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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.TableColumnModel;
import kontroler.Kontroler;
import model.Pacijent;
import model.StatusTermina;
import model.StavkaTermina;
import model.Stomatolog;
import model.Termin;
import model.Usluga;

/**
 * Modalni dijalog za unos novog termina, odnosno za izmenu postojeceg.
 *
 * Ako je u konstruktoru prosledjen termin, dijalog radi u rezimu izmene,
 * u suprotnom kreira novi termin.
 *
 * Termin je slozen domenski objekat: pored sopstvenih podataka (pacijent,
 * datum, vreme, status, napomena) sadrzi i listu stavki, gde je svaka stavka
 * jedna usluga u odredjenoj kolicini. Zato dijalog ima dva dela: gornji, u kome
 * se unose podaci o samom terminu, i donji, u kome se sastavlja lista stavki.
 *
 * Stavke koje stomatolog dodaje u tabelu postoje samo u memoriji klijenta - u
 * bazu se ne upisuje nista dok se ne pozove pamcenje termina. Tek tada ceo
 * termin sa svojom listom stavki odlazi na server, gde jedna sistemska
 * operacija u jednoj transakciji upisuje i termin i sve njegove stavke.
 *
 * @author vukla
 */
public class NoviTerminDijalog extends JDialog {

    private static final DateTimeFormatter FORMAT_VREMENA = DateTimeFormatter.ofPattern("HH:mm");

    /** Radno vreme ordinacije i korak izmedju dva slobodna termina. */
    private static final LocalTime POCETAK_RADNOG_VREMENA = LocalTime.of(8, 0);
    private static final LocalTime KRAJ_RADNOG_VREMENA = LocalTime.of(20, 0);
    private static final int KORAK_MINUTA = 15;

    /** Gornja granica sirine polja da dijalog ne bi bio prosiren dugackim nazivima. */
    private static final int MAKS_SIRINA_POLJA = 320;

    /** Najveca kolicina jedne usluge u okviru jednog termina. */
    private static final int NAJVECA_KOLICINA = 99;

    private final GlavnaForma roditeljskaForma;
    private final Termin terminZaIzmenu;

    private JComboBox<Stomatolog> cmbStomatolog;
    private JComboBox<Pacijent> cmbPacijent;
    private JComboBox<Usluga> cmbUsluga;
    private JComboBox<StatusTermina> cmbStatus;
    private DatePicker biracDatuma;
    private JComboBox<LocalTime> cmbVreme;
    private JTextArea txtNapomena;
    private JSpinner spnKolicina;
    private JTable tabelaStavki;
    private ModelTabeleStavki modelStavki;
    private JLabel lblUkupno;
    private JButton btnDodajStavku;
    private JButton btnUkloniStavku;
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
        ogranicSirinu(cmbStomatolog);
        ogranicSirinu(cmbPacijent);
        ogranicSirinu(cmbUsluga);
        pack();
        setLocationRelativeTo(roditeljskaForma);
    }

    private void inicijalizujKomponente() {
        setTitle(jeIzmena() ? "Izmena termina" : "Novi termin");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);
        setLayout(new BorderLayout());

        add(napraviPanelPodataka(), BorderLayout.NORTH);
        add(napraviPanelStavki(), BorderLayout.CENTER);
        add(napraviPanelDugmadi(), BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnPotvrdi);
    }

    /**
     * Pravi gornji deo dijaloga, sa podacima o samom terminu.
     */
    private JPanel napraviPanelPodataka() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(15, 20, 5, 20),
                BorderFactory.createTitledBorder("Podaci o terminu")));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // stomatolog se ne bira - termin se uvek zakazuje kod ulogovanog, pa
        // polje stoji prikazano ali onemoguceno, isto kao status pri unosu
        cmbStomatolog = new JComboBox<>();
        cmbStomatolog.setRenderer(new RendererStomatologa());
        cmbStomatolog.setEnabled(false);
        cmbPacijent = new JComboBox<>();
        cmbPacijent.setRenderer(new RendererPacijenta());
        cmbStatus = new JComboBox<>(StatusTermina.values());
        // datum se bira iz kalendara, pa neispravan unos nije ni moguc
        biracDatuma = BiracDatuma.napravi(false);
        cmbVreme = napraviBiracVremena();
        // napomena se pise u vise redova, pa polje raste nadole, a ne u stranu
        txtNapomena = new JTextArea(3, 15);
        txtNapomena.setLineWrap(true);
        txtNapomena.setWrapStyleWord(true);

        dodajRed(panel, gbc, 0, "Stomatolog:", cmbStomatolog);
        dodajRed(panel, gbc, 1, "Pacijent:", cmbPacijent);
        dodajRed(panel, gbc, 2, "Datum:", biracDatuma);
        dodajRed(panel, gbc, 3, "Vreme:", cmbVreme);
        dodajRed(panel, gbc, 4, "Status:", cmbStatus);
        dodajVisokRed(panel, gbc, 5, "Napomena:", new JScrollPane(txtNapomena));

        return panel;
    }

    /**
     * Pravi donji deo dijaloga, u kome se sastavlja lista usluga na terminu.
     *
     * Stomatolog bira uslugu i kolicinu i dugmetom je dodaje u tabelu, odnosno
     * uklanja izabranu stavku iz tabele. Sve to se dogadja samo u memoriji.
     */
    private JPanel napraviPanelStavki() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 20, 5, 20),
                BorderFactory.createTitledBorder("Stavke termina")));

        cmbUsluga = new JComboBox<>();
        cmbUsluga.setRenderer(new RendererUsluge());
        spnKolicina = new JSpinner(new SpinnerNumberModel(1, 1, NAJVECA_KOLICINA, 1));
        // spiner ne dozvoljava unos slova, pa ostaje samo provera granica
        ((JSpinner.DefaultEditor) spnKolicina.getEditor()).getTextField().setColumns(3);

        btnDodajStavku = new JButton("Dodaj stavku");
        btnUkloniStavku = new JButton("Ukloni stavku");

        JPanel panelUnosa = new JPanel(new GridBagLayout());
        panelUnosa.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;

        gbc.gridx = 0;
        panelUnosa.add(new JLabel("Usluga:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelUnosa.add(cmbUsluga, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panelUnosa.add(new JLabel("Količina:"), gbc);
        gbc.gridx = 3;
        panelUnosa.add(spnKolicina, gbc);

        gbc.gridx = 4;
        panelUnosa.add(btnDodajStavku, gbc);
        gbc.gridx = 5;
        panelUnosa.add(btnUkloniStavku, gbc);

        panel.add(panelUnosa, BorderLayout.NORTH);

        modelStavki = new ModelTabeleStavki(new ArrayList<StavkaTermina>());
        tabelaStavki = new JTable(modelStavki);
        tabelaStavki.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        podesiTabelu();

        JScrollPane klizac = new JScrollPane(tabelaStavki);
        klizac.setPreferredSize(new Dimension(560, 150));
        panel.add(klizac, BorderLayout.CENTER);

        // ukupan iznos termina je zbir iznosa svih stavki i menja se sa svakom
        // dodatom, odnosno uklonjenom stavkom
        lblUkupno = new JLabel();
        lblUkupno.setFont(lblUkupno.getFont().deriveFont(Font.BOLD));
        JPanel panelUkupno = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        panelUkupno.add(lblUkupno);
        panel.add(panelUkupno, BorderLayout.SOUTH);
        prikaziUkupanIznos();

        btnDodajStavku.addActionListener(e -> dodajStavku());
        btnUkloniStavku.addActionListener(e -> ukloniStavku());

        return panel;
    }

    private JPanel napraviPanelDugmadi() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 10));

        btnPotvrdi = new JButton(jeIzmena() ? "Sačuvaj izmene" : "Zakaži termin");
        btnOdustani = new JButton("Odustani");
        panel.add(btnPotvrdi);
        panel.add(btnOdustani);

        btnPotvrdi.addActionListener(e -> potvrdi());
        btnOdustani.addActionListener(e -> dispose());

        return panel;
    }

    /**
     * Podesava izgled tabele stavki: visinu redova, odnos sirina kolona i
     * podebljano zaglavlje.
     */
    private void podesiTabelu() {
        tabelaStavki.setRowHeight(26);
        tabelaStavki.setFillsViewportHeight(true);
        tabelaStavki.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabelaStavki.getTableHeader().setFont(
                tabelaStavki.getTableHeader().getFont().deriveFont(Font.BOLD));
        tabelaStavki.getTableHeader().setReorderingAllowed(false);

        int[] sirine = {40, 240, 80, 110, 110};
        TableColumnModel kolone = tabelaStavki.getColumnModel();
        for (int i = 0; i < kolone.getColumnCount() && i < sirine.length; i++) {
            kolone.getColumn(i).setPreferredWidth(sirine[i]);
        }
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
            for (Stomatolog s : Kontroler.getInstanca().vratiListuStomatologa()) {
                cmbStomatolog.addItem(s);
            }
            izaberiStomatologa();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
        }

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
            biracDatuma.setDate(LocalDate.now());
            izaberiVreme(POCETAK_RADNOG_VREMENA);
            return;
        }

        if (terminZaIzmenu.getDatum() != null) {
            biracDatuma.setDate(terminZaIzmenu.getDatum());
        }
        izaberiVreme(terminZaIzmenu.getVreme());
        txtNapomena.setText(terminZaIzmenu.getNapomena());
        cmbStatus.setSelectedItem(terminZaIzmenu.getStatus());

        izaberiPacijenta(terminZaIzmenu.getPacijent());
        popuniStavke();
    }

    /**
     * U rezimu izmene puni tabelu stavkama koje termin vec ima u bazi.
     *
     * Stavke se prepisuju u nove objekte, da izmene u tabeli ne bi dirale
     * termin koji glavna forma prikazuje u svojoj listi - ako stomatolog
     * odustane od izmene, prikaz mora da ostane onakav kakav je bio.
     */
    private void popuniStavke() {
        List<StavkaTermina> postojece = terminZaIzmenu.getStavke();
        if (postojece == null) {
            return;
        }

        List<StavkaTermina> kopije = new ArrayList<>();
        for (StavkaTermina stavka : postojece) {
            StavkaTermina kopija = new StavkaTermina();
            kopija.setRb(stavka.getRb());
            kopija.setUsluga(stavka.getUsluga());
            kopija.setKolicina(stavka.getKolicina());
            kopija.setCenaUsluge(stavka.getCenaUsluge());
            kopija.setIznos(stavka.getIznos());
            kopije.add(kopija);
        }

        modelStavki.postaviListu(kopije);
        prikaziUkupanIznos();
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

    /**
     * Bira stomatologa kod koga je termin zakazan: pri unosu je to ulogovani
     * stomatolog, a pri izmeni onaj koji je na terminu zapamcen.
     */
    private void izaberiStomatologa() {
        Stomatolog trazeni = jeIzmena()
                ? terminZaIzmenu.getStomatolog()
                : Kontroler.getInstanca().getUlogovaniStomatolog();
        if (trazeni == null) {
            return;
        }
        for (int i = 0; i < cmbStomatolog.getItemCount(); i++) {
            if (cmbStomatolog.getItemAt(i).getIdStomatolog() == trazeni.getIdStomatolog()) {
                cmbStomatolog.setSelectedIndex(i);
                return;
            }
        }
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

    /**
     * Dodaje izabranu uslugu u zadatoj kolicini u tabelu stavki.
     *
     * Stavka se dodaje samo u memoriju - u bazu odlazi tek kada stomatolog
     * pozove sistem da zapamti termin.
     */
    private void dodajStavku() {
        Usluga usluga = (Usluga) cmbUsluga.getSelectedItem();
        if (usluga == null) {
            JOptionPane.showMessageDialog(this, "Morate izabrati uslugu.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int kolicina = procitajKolicinu();
        if (kolicina <= 0) {
            JOptionPane.showMessageDialog(this, "Količina mora biti veća od nule.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modelStavki.dodajStavku(usluga, kolicina);
        prikaziUkupanIznos();

        // posle dodavanja se kolicina vraca na jedan, jer je to najcesci unos
        spnKolicina.setValue(1);
    }

    /**
     * Uklanja stavku izabranu u tabeli.
     */
    private void ukloniStavku() {
        int red = tabelaStavki.getSelectedRow();
        if (red < 0) {
            JOptionPane.showMessageDialog(this, "Morate izabrati stavku koju uklanjate.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modelStavki.ukloniStavku(tabelaStavki.convertRowIndexToModel(red));
        prikaziUkupanIznos();
    }

    /**
     * Cita kolicinu iz spinera. Vrednost koju je korisnik otkucao, a nije je
     * potvrdio pritiskom na Enter, spiner jos uvek drzi u polju za unos, pa se
     * prvo prihvata izmena, a tek onda cita vrednost.
     */
    private int procitajKolicinu() {
        try {
            spnKolicina.commitEdit();
        } catch (java.text.ParseException ex) {
            // otkucan je neispravan tekst - spiner vraca poslednju ispravnu
            // vrednost, pa se u polju prikazuje ona
            spnKolicina.setValue(spnKolicina.getValue());
        }
        return ((Number) spnKolicina.getValue()).intValue();
    }

    private void prikaziUkupanIznos() {
        lblUkupno.setText(String.format("Ukupno: %.2f din", modelStavki.ukupanIznos()));
    }

    /**
     * Validira unos, pravi termin sa svim njegovim stavkama i prosledjuje ga
     * kontroleru.
     */
    private void potvrdi() {
        Pacijent pacijent = (Pacijent) cmbPacijent.getSelectedItem();
        if (pacijent == null) {
            JOptionPane.showMessageDialog(this, "Morate izabrati pacijenta.",
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

        if (jeUProslosti(datum, vreme) && !jeZadrzanPostojeciTermin(datum, vreme)) {
            JOptionPane.showMessageDialog(this, "Termin ne može biti zakazan u prošlosti.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (modelStavki.jePrazna()) {
            JOptionPane.showMessageDialog(this, "Morate dodati bar jednu stavku termina.",
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

        // stavke se terminu dodaju onim redosledom kojim stoje u tabeli, a
        // svaka zna kom terminu pripada
        List<StavkaTermina> stavke = modelStavki.vratiStavke();
        for (StavkaTermina stavka : stavke) {
            stavka.setTermin(termin);
        }
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

        JOptionPane.showMessageDialog(this, "Sistem je zapamtio termin.",
                "Termin", JOptionPane.INFORMATION_MESSAGE);

        roditeljskaForma.ucitajTermine();
        dispose();
    }

    private boolean jeIzmena() {
        return terminZaIzmenu != null;
    }

    /**
     * Cita izabrani datum. Kalendar ne dozvoljava neispravan unos, pa ostaje
     * samo provera da li je datum uopste izabran.
     */
    private LocalDate procitajDatum() {
        LocalDate datum = biracDatuma.getDate();
        if (datum == null) {
            JOptionPane.showMessageDialog(this, "Morate izabrati datum termina.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            biracDatuma.openPopup();
        }
        return datum;
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
     * Utvrdjuje da li su datum i vreme ostali onakvi kakve termin vec ima
     * zapamcene. Takav termin se ne premesta, pa pravilo o proslosti za njega
     * ne vazi - inace protekao termin ne bi mogao ni da se dopuni ni da mu se
     * promeni status.
     */
    private boolean jeZadrzanPostojeciTermin(LocalDate datum, LocalTime vreme) {
        return jeIzmena()
                && datum.equals(terminZaIzmenu.getDatum())
                && vreme.equals(terminZaIzmenu.getVreme());
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
                setText(String.format("%s (%.2f din, %d min)",
                        u.getNaziv(), u.getCena(), u.getTrajanje()));
            }
            return this;
        }
    }

    private static class RendererStomatologa extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object vrednost, int indeks,
                boolean izabran, boolean fokusiran) {
            super.getListCellRendererComponent(lista, vrednost, indeks, izabran, fokusiran);
            if (vrednost instanceof Stomatolog) {
                Stomatolog s = (Stomatolog) vrednost;
                setText("dr " + s.getIme() + " " + s.getPrezime());
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
