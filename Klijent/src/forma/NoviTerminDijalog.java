/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forma;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import javax.swing.JTextField;
import komunikacija.Komunikacija;
import komunikacija.Odgovor;
import komunikacija.Operacija;
import model.Pacijent;
import model.StatusTermina;
import model.StavkaTermina;
import model.Stomatolog;
import model.Termin;
import model.Usluga;

/**
 * Modalni dijalog za unos novog termina, odnosno za izmenu postojeceg.
 *
 * Ako je u konstruktoru prosledjen termin, dijalog radi u rezimu izmene
 * (salje PROMENI_TERMIN), u suprotnom kreira novi termin (salje UBACI_TERMIN).
 *
 * @author vukla
 */
public class NoviTerminDijalog extends JDialog {

    private static final DateTimeFormatter FORMAT_DATUMA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMAT_VREMENA = DateTimeFormatter.ofPattern("HH:mm");

    private final GlavnaForma roditeljskaForma;
    private final Stomatolog ulogovaniStomatolog;
    private final Termin terminZaIzmenu;

    private JComboBox<Pacijent> cmbPacijent;
    private JComboBox<Usluga> cmbUsluga;
    private JComboBox<StatusTermina> cmbStatus;
    private JTextField txtDatum;
    private JTextField txtVreme;
    private JTextField txtNapomena;
    private JButton btnPotvrdi;
    private JButton btnOdustani;

    public NoviTerminDijalog(GlavnaForma roditeljskaForma, Stomatolog ulogovaniStomatolog,
            Termin terminZaIzmenu) {
        super(roditeljskaForma, true);
        this.roditeljskaForma = roditeljskaForma;
        this.ulogovaniStomatolog = ulogovaniStomatolog;
        this.terminZaIzmenu = terminZaIzmenu;

        inicijalizujKomponente();
        ucitajListe();
        popuniPodatke();
    }

    private void inicijalizujKomponente() {
        setTitle(jeIzmena() ? "Izmena termina" : "Novi termin");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        cmbPacijent = new JComboBox<>();
        cmbPacijent.setRenderer(new RendererPacijenta());
        cmbUsluga = new JComboBox<>();
        cmbUsluga.setRenderer(new RendererUsluge());
        cmbStatus = new JComboBox<>(StatusTermina.values());
        txtDatum = new JTextField(15);
        txtVreme = new JTextField(15);
        txtNapomena = new JTextField(15);

        dodajRed(panel, gbc, 0, "Pacijent:", cmbPacijent);
        dodajRed(panel, gbc, 1, "Datum (yyyy-MM-dd):", txtDatum);
        dodajRed(panel, gbc, 2, "Vreme (HH:mm):", txtVreme);
        dodajRed(panel, gbc, 3, "Usluga:", cmbUsluga);
        dodajRed(panel, gbc, 4, "Status:", cmbStatus);
        dodajRed(panel, gbc, 5, "Napomena:", txtNapomena);

        add(panel, BorderLayout.CENTER);

        JPanel panelDugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPotvrdi = new JButton(jeIzmena() ? "Sačuvaj izmene" : "Zakaži termin");
        btnOdustani = new JButton("Odustani");
        panelDugmad.add(btnPotvrdi);
        panelDugmad.add(btnOdustani);
        add(panelDugmad, BorderLayout.SOUTH);

        btnPotvrdi.addActionListener(e -> potvrdi());
        btnOdustani.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnPotvrdi);

        pack();
        setLocationRelativeTo(roditeljskaForma);
    }

    private void dodajRed(JPanel panel, GridBagConstraints gbc, int red, String naziv, Component komponenta) {
        gbc.gridx = 0;
        gbc.gridy = red;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(naziv), gbc);

        gbc.gridx = 1;
        gbc.gridy = red;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(komponenta, gbc);
    }

    /**
     * Ucitava sa servera liste pacijenata i usluga i puni combo box-ove.
     */
    @SuppressWarnings("unchecked")
    private void ucitajListe() {
        Komunikacija komunikacija = vratiKomunikaciju();
        if (komunikacija == null) {
            return;
        }

        Odgovor odgovorPacijenti = komunikacija.posaljiZahtev(Operacija.VRATI_LISTU_PACIJENATA, null);
        if (odgovorPacijenti != null && odgovorPacijenti.getOdgovor() != null) {
            List<Pacijent> pacijenti = (List<Pacijent>) odgovorPacijenti.getOdgovor();
            for (Pacijent p : pacijenti) {
                cmbPacijent.addItem(p);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Neuspešno učitavanje liste pacijenata.",
                    "Greška", JOptionPane.ERROR_MESSAGE);
        }

        Odgovor odgovorUsluge = komunikacija.posaljiZahtev(Operacija.VRATI_LISTU_USLUGA, null);
        if (odgovorUsluge != null && odgovorUsluge.getOdgovor() != null) {
            List<Usluga> usluge = (List<Usluga>) odgovorUsluge.getOdgovor();
            for (Usluga u : usluge) {
                cmbUsluga.addItem(u);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Neuspešno učitavanje liste usluga.",
                    "Greška", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * U rezimu izmene popunjava polja podacima izabranog termina.
     */
    private void popuniPodatke() {
        if (!jeIzmena()) {
            cmbStatus.setSelectedItem(StatusTermina.ZAKAZAN);
            return;
        }

        if (terminZaIzmenu.getDatum() != null) {
            txtDatum.setText(terminZaIzmenu.getDatum().format(FORMAT_DATUMA));
        }
        if (terminZaIzmenu.getVreme() != null) {
            txtVreme.setText(terminZaIzmenu.getVreme().format(FORMAT_VREMENA));
        }
        txtNapomena.setText(terminZaIzmenu.getNapomena());
        cmbStatus.setSelectedItem(terminZaIzmenu.getStatus());

        izaberiPacijenta(terminZaIzmenu.getPacijent());

        List<StavkaTermina> stavke = terminZaIzmenu.getStavke();
        if (stavke != null && !stavke.isEmpty()) {
            izaberiUslugu(stavke.get(0).getUsluga());
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
     * Validira unos, pravi termin i salje ga serveru.
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

        LocalDate datum;
        try {
            datum = LocalDate.parse(txtDatum.getText().trim(), FORMAT_DATUMA);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Datum mora biti u formatu yyyy-MM-dd.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalTime vreme;
        try {
            vreme = LocalTime.parse(txtVreme.getText().trim(), FORMAT_VREMENA);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Vreme mora biti u formatu HH:mm.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StatusTermina status = (StatusTermina) cmbStatus.getSelectedItem();
        String napomena = txtNapomena.getText().trim();

        // u rezimu izmene zadrzava se postojeci id termina
        int idTermin = jeIzmena() ? terminZaIzmenu.getIdTermin() : 0;

        Termin termin = new Termin(idTermin, datum, vreme, status, napomena,
                ulogovaniStomatolog, pacijent);

        // konstruktor termina ne prima stavke - pravi praznu listu, pa se
        // izabrana usluga dodaje kao stavka naknadno
        List<StavkaTermina> stavke = new ArrayList<>();
        int kolicina = 1;
        stavke.add(new StavkaTermina(1, kolicina, usluga.getCena() * kolicina,
                usluga.getCena(), termin, usluga));
        termin.setStavke(stavke);

        Komunikacija komunikacija = vratiKomunikaciju();
        if (komunikacija == null) {
            return;
        }

        Operacija operacija = jeIzmena() ? Operacija.PROMENI_TERMIN : Operacija.UBACI_TERMIN;
        Odgovor odgovor = komunikacija.posaljiZahtev(operacija, termin);

        if (odgovor == null) {
            JOptionPane.showMessageDialog(this, "Greška u komunikaciji sa serverom.",
                    "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Boolean uspesno = (Boolean) odgovor.getOdgovor();
        if (uspesno == null || !uspesno) {
            // dijalog ostaje otvoren da korisnik moze da ispravi podatke
            JOptionPane.showMessageDialog(this,
                    jeIzmena()
                            ? "Izmena termina nije uspela. Proverite da li je termin zauzet."
                            : "Zakazivanje termina nije uspelo. Proverite da li je termin zauzet.",
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

    private Komunikacija vratiKomunikaciju() {
        try {
            return Komunikacija.getInstanca();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Server nije dostupan. Proverite da li je server pokrenut.",
                    "Greška", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Domenske klase nemaju toString, pa se prikaz u combo box-u resava rendererom.
     */
    private static class RendererPacijenta extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object vrednost, int indeks,
                boolean izabran, boolean fokusiran) {
            super.getListCellRendererComponent(lista, vrednost, indeks, izabran, fokusiran);
            if (vrednost instanceof Pacijent) {
                Pacijent p = (Pacijent) vrednost;
                setText(p.getIme() + " " + p.getPrezime() + " (" + p.getBrojKnjizice() + ")");
            }
            return this;
        }
    }

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
}
