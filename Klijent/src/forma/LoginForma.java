/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forma;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import komunikacija.Komunikacija;
import komunikacija.Odgovor;
import komunikacija.Operacija;
import model.Stomatolog;

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
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Korisničko ime:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtKorisnickoIme = new JTextField(18);
        panel.add(txtKorisnickoIme, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Šifra:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtSifra = new JPasswordField(18);
        panel.add(txtSifra, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        btnPrijava = new JButton("Prijavi se");
        panel.add(btnPrijava, gbc);

        // prijava i na klik i na Enter u polju za šifru
        btnPrijava.addActionListener(e -> prijaviSe());
        txtSifra.addActionListener(e -> prijaviSe());
        getRootPane().setDefaultButton(btnPrijava);

        add(panel);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Šalje serveru zahtev za prijavu i, ako je prijava uspešna, otvara glavnu formu.
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

        Komunikacija komunikacija;
        try {
            komunikacija = Komunikacija.getInstanca();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Server nije dostupan. Proverite da li je server pokrenut.",
                    "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Odgovor odgovor = komunikacija.posaljiZahtev(
                Operacija.PRIJAVA_STOMATOLOG,
                new Object[]{korisnickoIme, sifra});

        if (odgovor == null) {
            JOptionPane.showMessageDialog(this,
                    "Greška u komunikaciji sa serverom.",
                    "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (odgovor.getOdgovor() == null) {
            JOptionPane.showMessageDialog(this,
                    "Pogrešno korisničko ime ili šifra",
                    "Neuspešna prijava", JOptionPane.ERROR_MESSAGE);
            txtSifra.setText("");
            return;
        }

        Stomatolog ulogovani = (Stomatolog) odgovor.getOdgovor();
        new GlavnaForma(ulogovani).setVisible(true);
        dispose();
    }
}
