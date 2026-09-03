package forma;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import kontroler.Kontroler;
import model.Ordinacija;
import model.Pacijent;

/**
 * Modalni dijalog za unos novog pacijenta, odnosno za izmenu postojeceg (ako
 * je pacijent prosledjen u konstruktoru).
 *
 * @author vukla
 */
public class NoviPacijentDijalog extends JDialog {

    /** Ogranicenje iz specifikacije: broj knjizice ima tacno ovoliko znakova. */
    private static final int DUZINA_BROJA_KNJIZICE = 11;

    /** Gornja granica sirine polja da dijalog ne bi bio prosiren dugackim nazivima. */
    private static final int MAKS_SIRINA_POLJA = 320;

    private final UpravljanjePacijentimaForma roditeljskaForma;
    private final Pacijent pacijentZaIzmenu;

    private JTextField txtIme;
    private JTextField txtPrezime;
    private JTextField txtBrojTelefona;
    private JTextField txtBrojKnjizice;
    private JComboBox<Ordinacija> cmbOrdinacija;
    private JButton btnPotvrdi;
    private JButton btnOdustani;

    public NoviPacijentDijalog(UpravljanjePacijentimaForma roditeljskaForma, Pacijent pacijentZaIzmenu) {
        super(roditeljskaForma, true);
        this.roditeljskaForma = roditeljskaForma;
        this.pacijentZaIzmenu = pacijentZaIzmenu;

        inicijalizujKomponente();
        ucitajOrdinacije();
        popuniPodatke();

        // pakuje se tek kad je lista ordinacija popunjena, da se zna stvarna sirina
        ogranicSirinu(cmbOrdinacija);
        pack();
        setLocationRelativeTo(roditeljskaForma);
    }

    private void inicijalizujKomponente() {
        setTitle(jeIzmena() ? "Izmena pacijenta" : "Novi pacijent");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        txtIme = new JTextField(18);
        txtPrezime = new JTextField(18);
        txtBrojTelefona = new JTextField(18);
        txtBrojKnjizice = new JTextField(18);
        cmbOrdinacija = new JComboBox<>();
        cmbOrdinacija.setRenderer(new RendererOrdinacije());

        dodajRed(panel, gbc, 0, "Ime:", txtIme);
        dodajRed(panel, gbc, 1, "Prezime:", txtPrezime);
        dodajRed(panel, gbc, 2, "Broj telefona:", txtBrojTelefona);
        dodajRed(panel, gbc, 3, "Broj knjižice:", txtBrojKnjizice);
        dodajRed(panel, gbc, 4, "Ordinacija:", cmbOrdinacija);

        add(panel, BorderLayout.CENTER);

        JPanel panelDugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelDugmad.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 10));
        btnPotvrdi = new JButton(jeIzmena() ? "Sačuvaj izmene" : "Zapamti pacijenta");
        btnOdustani = new JButton("Odustani");
        panelDugmad.add(btnPotvrdi);
        panelDugmad.add(btnOdustani);
        add(panelDugmad, BorderLayout.SOUTH);

        btnPotvrdi.addActionListener(e -> potvrdi());
        btnOdustani.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnPotvrdi);
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

    private void ogranicSirinu(Component komponenta) {
        Dimension zeljena = komponenta.getPreferredSize();
        if (zeljena.width > MAKS_SIRINA_POLJA) {
            komponenta.setPreferredSize(new Dimension(MAKS_SIRINA_POLJA, zeljena.height));
        }
    }

    /**
     * Ucitava listu ordinacija i puni combo box.
     */
    private void ucitajOrdinacije() {
        try {
            for (Ordinacija ordinacija : Kontroler.getInstanca().vratiListuOrdinacija()) {
                cmbOrdinacija.addItem(ordinacija);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * U rezimu izmene popunjava polja podacima izabranog pacijenta.
     */
    private void popuniPodatke() {
        if (!jeIzmena()) {
            return;
        }

        txtIme.setText(pacijentZaIzmenu.getIme());
        txtPrezime.setText(pacijentZaIzmenu.getPrezime());
        txtBrojTelefona.setText(pacijentZaIzmenu.getBrojTelefona());
        txtBrojKnjizice.setText(pacijentZaIzmenu.getBrojKnjizice());
        izaberiOrdinaciju(pacijentZaIzmenu.getOrdinacija());
    }

    private void izaberiOrdinaciju(Ordinacija ordinacija) {
        if (ordinacija == null) {
            return;
        }
        for (int i = 0; i < cmbOrdinacija.getItemCount(); i++) {
            if (cmbOrdinacija.getItemAt(i).getIdOrdinacija() == ordinacija.getIdOrdinacija()) {
                cmbOrdinacija.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * Validira unos, pravi pacijenta i prosledjuje ga kontroleru.
     */
    private void potvrdi() {
        String ime = txtIme.getText().trim();
        if (ime.isEmpty()) {
            upozori("Morate uneti ime pacijenta.", txtIme);
            return;
        }

        String prezime = txtPrezime.getText().trim();
        if (prezime.isEmpty()) {
            upozori("Morate uneti prezime pacijenta.", txtPrezime);
            return;
        }

        String brojTelefona = txtBrojTelefona.getText().trim();
        if (brojTelefona.isEmpty()) {
            upozori("Morate uneti broj telefona pacijenta.", txtBrojTelefona);
            return;
        }

        // ogranicenje iz specifikacije: broj knjizice ima tacno 11 znakova
        String brojKnjizice = txtBrojKnjizice.getText().trim();
        if (brojKnjizice.length() != DUZINA_BROJA_KNJIZICE) {
            upozori("Broj knjižice mora imati tačno " + DUZINA_BROJA_KNJIZICE + " znakova.\n"
                    + "Uneto je " + brojKnjizice.length() + " znakova.", txtBrojKnjizice);
            return;
        }

        Ordinacija ordinacija = (Ordinacija) cmbOrdinacija.getSelectedItem();
        if (ordinacija == null) {
            JOptionPane.showMessageDialog(this, "Morate izabrati ordinaciju.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // u rezimu izmene zadrzava se postojeci id pacijenta
        int idPacijent = jeIzmena() ? pacijentZaIzmenu.getIdPacijent() : 0;
        Pacijent pacijent = new Pacijent(idPacijent, ime, prezime, brojTelefona, brojKnjizice, ordinacija);

        try {
            if (jeIzmena()) {
                Kontroler.getInstanca().promeniPacijenta(pacijent);
            } else {
                Kontroler.getInstanca().ubaciPacijenta(pacijent);
            }
        } catch (Exception ex) {
            // dijalog ostaje otvoren da korisnik ispravi podatke; poruka stize sa servera
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Sistem je zapamtio pacijenta.",
                "Uspeh", JOptionPane.INFORMATION_MESSAGE);

        roditeljskaForma.ucitajPacijente();
        dispose();
    }

    private void upozori(String poruka, JTextField polje) {
        JOptionPane.showMessageDialog(this, poruka, "Upozorenje", JOptionPane.WARNING_MESSAGE);
        polje.requestFocusInWindow();
        polje.selectAll();
    }

    private boolean jeIzmena() {
        return pacijentZaIzmenu != null;
    }
}
