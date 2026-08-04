/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forma;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
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
import komunikacija.Komunikacija;
import komunikacija.Odgovor;
import komunikacija.Operacija;
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

    public GlavnaForma(Stomatolog ulogovaniStomatolog) {
        this.ulogovaniStomatolog = ulogovaniStomatolog;
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
     * Ucitava sa servera listu termina ulogovanog stomatologa i prikazuje ih u tabeli.
     */
    @SuppressWarnings("unchecked")
    public final void ucitajTermine() {
        Komunikacija komunikacija = vratiKomunikaciju();
        if (komunikacija == null) {
            return;
        }

        String kriterijum = "idStomatolog = " + ulogovaniStomatolog.getIdStomatolog();
        Odgovor odgovor = komunikacija.posaljiZahtev(Operacija.VRATI_LISTU_TERMINA, kriterijum);

        if (odgovor == null) {
            prikaziGreskuKomunikacije();
            return;
        }

        List<Termin> termini = (List<Termin>) odgovor.getOdgovor();
        modelTabele.postaviListu(termini);
    }

    private void noviTermin() {
        NoviTerminDijalog dijalog = new NoviTerminDijalog(this, ulogovaniStomatolog, null);
        dijalog.setVisible(true);
    }

    private void izmeniTermin() {
        Termin izabrani = vratiIzabraniTermin();
        if (izabrani == null) {
            return;
        }
        NoviTerminDijalog dijalog = new NoviTerminDijalog(this, ulogovaniStomatolog, izabrani);
        dijalog.setVisible(true);
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

        Komunikacija komunikacija = vratiKomunikaciju();
        if (komunikacija == null) {
            return;
        }

        Odgovor odgovor = komunikacija.posaljiZahtev(Operacija.OBRISI_TERMIN, izabrani);
        if (odgovor == null) {
            prikaziGreskuKomunikacije();
            return;
        }

        Boolean uspesno = (Boolean) odgovor.getOdgovor();
        if (uspesno != null && uspesno) {
            JOptionPane.showMessageDialog(this, "Termin je uspešno obrisan.",
                    "Uspeh", JOptionPane.INFORMATION_MESSAGE);
            ucitajTermine();
        } else {
            JOptionPane.showMessageDialog(this, "Brisanje termina nije uspelo.",
                    "Greška", JOptionPane.ERROR_MESSAGE);
        }
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

    private void prikaziGreskuKomunikacije() {
        JOptionPane.showMessageDialog(this,
                "Greška u komunikaciji sa serverom.",
                "Greška", JOptionPane.ERROR_MESSAGE);
    }

    private void zatvoriAplikaciju() {
        try {
            Komunikacija.getInstanca().zatvoriVezu();
        } catch (IOException ex) {
            // veza ionako nije uspostavljena, nema sta da se zatvara
        }
        dispose();
        System.exit(0);
    }
}
