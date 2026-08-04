/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forma;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import kontroler.Kontroler;
import model.Stomatolog;
import model.Termin;

/**
 * Glavna forma klijenta - prikazuje termine ulogovanog stomatologa.
 *
 * @author vukla
 */
public class GlavnaForma extends JFrame {

    private final Stomatolog ulogovaniStomatolog;

    private JTable tabelaTermina;
    private ModelTabeleTermin modelTabele;

    private JButton btnNovi;
    private JButton btnIzmeni;
    private JButton btnObrisi;
    private JButton btnOsvezi;

    public GlavnaForma() {
        this.ulogovaniStomatolog = Kontroler.getInstanca().getUlogovaniStomatolog();
        inicijalizujKomponente();
        ucitajTermine();
    }

    private void inicijalizujKomponente() {
        setTitle("Zakazivanje termina - dr " + ulogovaniStomatolog.getIme()
                + " " + ulogovaniStomatolog.getPrezime());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lblUlogovani = new JLabel("Ulogovani stomatolog: "
                + ulogovaniStomatolog.getIme() + " " + ulogovaniStomatolog.getPrezime());
        lblUlogovani.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblUlogovani, BorderLayout.NORTH);

        modelTabele = new ModelTabeleTermin(new ArrayList<Termin>());
        tabelaTermina = new JTable(modelTabele);
        tabelaTermina.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabelaTermina), BorderLayout.CENTER);

        JPanel panelDugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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

        // pri zatvaranju forme zatvara se i veza sa serverom
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                zatvoriAplikaciju();
            }
        });
    }

    /**
     * Ucitava termine ulogovanog stomatologa i prikazuje ih u tabeli.
     */
    public final void ucitajTermine() {
        try {
            List<Termin> termini = Kontroler.getInstanca().vratiTermineUlogovanog();
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

    private void zatvoriAplikaciju() {
        Kontroler.getInstanca().odjaviSe();
        dispose();
        System.exit(0);
    }
}
