/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forma;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import kontroler.Kontroler;

/**
 * Forma za prijavu stomatologa na sistem.
 *
 * @author vukla
 */
public class LoginForma extends JFrame {

    private JTextField txtKorisnickoIme;
    private JPasswordField txtSifra;
    private JButton btnPrijava;

    public LoginForma() {
        inicijalizujKomponente();
    }

    private void inicijalizujKomponente() {
        setTitle("Prijava na sistem - Stomatološka ordinacija");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // naslov iznad polja, preko obe kolone
        JLabel lblNaslov = new JLabel("Prijava stomatologa");
        lblNaslov.setFont(lblNaslov.getFont().deriveFont(Font.BOLD, 20f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 8, 20, 8);
        panel.add(lblNaslov, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Korisničko ime:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtKorisnickoIme = new JTextField(18);
        panel.add(txtKorisnickoIme, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Šifra:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtSifra = new JPasswordField(18);
        panel.add(txtSifra, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 8, 0, 8);
        btnPrijava = new JButton("Prijavi se");
        panel.add(btnPrijava, gbc);

        // prijava i na klik i na Enter u polju za šifru
        btnPrijava.addActionListener(e -> prijaviSe());
        txtSifra.addActionListener(e -> prijaviSe());
        getRootPane().setDefaultButton(btnPrijava);

        // panel sa poljima ostaje prirodne velicine i centriran je na sredini
        // maksimizovanog prozora
        JPanel omotac = new JPanel(new GridBagLayout());
        omotac.add(panel, new GridBagConstraints());
        add(omotac, BorderLayout.CENTER);

        // velicina koja se dobija kada korisnik vrati prozor iz maksimizovanog
        setSize(900, 600);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    /**
     * Šalje zahtev za prijavu preko kontrolera i, ako je prijava uspešna,
     * otvara glavnu formu.
     */
    private void prijaviSe() {
        String korisnickoIme = txtKorisnickoIme.getText().trim();
        String sifra = new String(txtSifra.getPassword());

        if (korisnickoIme.isEmpty() || sifra.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Morate uneti korisničko ime i šifru.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Kontroler.getInstanca().prijaviSe(korisnickoIme, sifra);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Neuspešna prijava", JOptionPane.ERROR_MESSAGE);
            txtSifra.setText("");
            return;
        }

        JOptionPane.showMessageDialog(this, "Korisničko ime i šifra su ispravni.",
                "Prijava", JOptionPane.INFORMATION_MESSAGE);

        new GlavnaForma().setVisible(true);
        dispose();
    }
}
