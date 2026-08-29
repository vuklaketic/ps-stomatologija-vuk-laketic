package forma;

import baza.Konekcija;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Modalni dijalog za podesavanje parametara konekcije sa bazom podataka.
 *
 * Polja se pune vrednostima koje server trenutno koristi. Dugme "Sačuvaj"
 * upisuje unete vrednosti u fajl {@code baza.properties} i odmah ponovo
 * uspostavlja konekciju, pa nema potrebe za ponovnim pokretanjem aplikacije
 * ni za rucnim menjanjem fajla.
 *
 * @author vukla
 */
public class FormaKonfBaza extends JDialog {

    private final ServerskaForma roditeljskaForma;

    private JTextField txtUrl;
    private JTextField txtKorisnik;
    private JPasswordField txtSifra;
    private JButton btnSacuvaj;
    private JButton btnOtkazi;

    public FormaKonfBaza(ServerskaForma roditeljskaForma) {
        super(roditeljskaForma, true);
        this.roditeljskaForma = roditeljskaForma;

        inicijalizujKomponente();
        popuniPodatke();

        pack();
        setLocationRelativeTo(roditeljskaForma);
    }

    private void inicijalizujKomponente() {
        setTitle("Konekcija sa bazom");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        txtUrl = new JTextField(35);
        txtKorisnik = new JTextField(20);
        txtSifra = new JPasswordField(20);

        dodajRed(panel, gbc, 0, "URL:", txtUrl);
        dodajRed(panel, gbc, 1, "Korisničko ime:", txtKorisnik);
        dodajRed(panel, gbc, 2, "Šifra:", txtSifra);

        add(panel, BorderLayout.CENTER);

        JPanel panelDugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSacuvaj = new JButton("Sačuvaj");
        btnOtkazi = new JButton("Otkaži");
        panelDugmad.add(btnSacuvaj);
        panelDugmad.add(btnOtkazi);
        add(panelDugmad, BorderLayout.SOUTH);

        btnSacuvaj.addActionListener(e -> sacuvaj());
        btnOtkazi.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnSacuvaj);
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
     * Popunjava polja podesavanjima sa kojima server trenutno radi.
     */
    private void popuniPodatke() {
        Properties podesavanja = Konekcija.getInstanca().vratiPodesavanja();
        txtUrl.setText(podesavanja.getProperty(Konekcija.KLJUC_URL));
        txtKorisnik.setText(podesavanja.getProperty(Konekcija.KLJUC_KORISNIK));
        txtSifra.setText(podesavanja.getProperty(Konekcija.KLJUC_SIFRA));
    }

    /**
     * Validira unos, upisuje podesavanja u fajl i ponovo uspostavlja konekciju.
     */
    private void sacuvaj() {
        String url = txtUrl.getText().trim();
        String korisnik = txtKorisnik.getText().trim();
        String sifra = String.valueOf(txtSifra.getPassword());

        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "URL baze ne sme biti prazan.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            txtUrl.requestFocusInWindow();
            return;
        }

        if (korisnik.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Korisničko ime ne sme biti prazno.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            txtKorisnik.requestFocusInWindow();
            return;
        }

        try {
            Konekcija.getInstanca().sacuvajPodesavanja(url, korisnik, sifra);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Podešavanja nisu sačuvana: " + ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        roditeljskaForma.zabelezi("Sačuvana su nova podešavanja baze: " + url);

        // podesavanja su vec upisana, pa se neuspela veza prijavljuje kao
        // upozorenje - korisnik moze da ispravi podatke i pokusa ponovo
        try {
            Konekcija.getInstanca().ponovoUspostaviKonekciju();
        } catch (Exception ex) {
            roditeljskaForma.zabelezi("Veza sa bazom nije uspostavljena: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Podešavanja su sačuvana, ali veza sa bazom nije uspostavljena:\n"
                    + ex.getMessage(),
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        roditeljskaForma.zabelezi("Veza sa bazom je ponovo uspostavljena.");
        JOptionPane.showMessageDialog(this,
                "Podešavanja su sačuvana i veza sa bazom je uspostavljena.",
                "Uspeh", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
