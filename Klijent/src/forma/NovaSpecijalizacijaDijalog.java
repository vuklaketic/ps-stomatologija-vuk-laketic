package forma;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import kontroler.Kontroler;
import model.Specijalizacija;

/**
 * Modalni dijalog za unos nove specijalizacije - samo naziv, pa forma ima
 * jedno polje; slucaj koriscenja trazi samo ubacivanje, bez rezima izmene.
 *
 * @author vukla
 */
public class NovaSpecijalizacijaDijalog extends JDialog {

    private JTextField txtNaziv;
    private JButton btnSacuvaj;
    private JButton btnOdustani;

    public NovaSpecijalizacijaDijalog(GlavnaForma roditeljskaForma) {
        super(roditeljskaForma, true);

        inicijalizujKomponente();
        pack();
        setLocationRelativeTo(roditeljskaForma);
    }

    private void inicijalizujKomponente() {
        setTitle("Nova specijalizacija");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        txtNaziv = new JTextField(18);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Naziv:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtNaziv, gbc);

        add(panel, BorderLayout.CENTER);

        JPanel panelDugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelDugmad.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 10));
        btnSacuvaj = new JButton("Sačuvaj");
        btnOdustani = new JButton("Odustani");
        panelDugmad.add(btnSacuvaj);
        panelDugmad.add(btnOdustani);
        add(panelDugmad, BorderLayout.SOUTH);

        btnSacuvaj.addActionListener(e -> sacuvaj());
        btnOdustani.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnSacuvaj);
    }

    /**
     * Validira unos i prosledjuje specijalizaciju kontroleru.
     */
    private void sacuvaj() {
        String naziv = txtNaziv.getText().trim();
        if (naziv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Morate uneti naziv specijalizacije.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            txtNaziv.requestFocusInWindow();
            return;
        }

        try {
            Kontroler.getInstanca().ubaciSpecijalizaciju(new Specijalizacija(0, naziv));
        } catch (Exception ex) {
            // dijalog ostaje otvoren da korisnik ispravi naziv; poruka stize sa servera
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Sistem je zapamtio specijalizaciju.",
                "Specijalizacija", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
