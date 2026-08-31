/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forma;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.table.TableColumnModel;
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
    private JButton btnOdjava;

    public GlavnaForma() {
        this.ulogovaniStomatolog = Kontroler.getInstanca().getUlogovaniStomatolog();
        inicijalizujKomponente();
        ucitajTermine();
    }

    private void inicijalizujKomponente() {
        setTitle("Zakazivanje termina - dr " + ulogovaniStomatolog.getIme()
                + " " + ulogovaniStomatolog.getPrezime());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(true);
        setLayout(new BorderLayout());

        // u zaglavlju su podaci o ulogovanom stomatologu, a odjava je u desnom
        // uglu, odvojena od akcionih dugmadi nad terminima
        JPanel panelZaglavlje = new JPanel(new BorderLayout());
        panelZaglavlje.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblUlogovani = new JLabel("Ulogovani stomatolog: "
                + ulogovaniStomatolog.getIme() + " " + ulogovaniStomatolog.getPrezime());
        lblUlogovani.setFont(lblUlogovani.getFont().deriveFont(Font.BOLD));
        panelZaglavlje.add(lblUlogovani, BorderLayout.WEST);

        btnOdjava = new JButton("Odjavi se");
        panelZaglavlje.add(btnOdjava, BorderLayout.EAST);

        add(panelZaglavlje, BorderLayout.NORTH);

        modelTabele = new ModelTabeleTermin(new ArrayList<Termin>());
        tabelaTermina = new JTable(modelTabele);
        tabelaTermina.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        podesiTabelu();

        JScrollPane klizac = new JScrollPane(tabelaTermina);
        klizac.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        add(klizac, BorderLayout.CENTER);

        // razmak izmedju dugmadi i odvajanje od ivica prozora
        JPanel panelDugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panelDugmad.setBorder(BorderFactory.createEmptyBorder(0, 20, 5, 10));
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
        btnOdjava.addActionListener(e -> odjaviSe());

        // prozor se otvara maksimizovan, a ova velicina vazi kada ga korisnik
        // vrati iz maksimizovanog stanja
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // pri zatvaranju forme zatvara se i veza sa serverom
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                zatvoriAplikaciju();
            }
        });
    }

    /**
     * Podesava izgled tabele: visinu redova, raspodelu sirina kolona i
     * podebljano zaglavlje. Kolone se rasporedjuju po celoj sirini prozora, a
     * zadate sirine odredjuju odnos izmedju njih.
     */
    private void podesiTabelu() {
        tabelaTermina.setRowHeight(26);
        tabelaTermina.setFillsViewportHeight(true);
        tabelaTermina.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabelaTermina.getTableHeader().setFont(
                tabelaTermina.getTableHeader().getFont().deriveFont(Font.BOLD));
        tabelaTermina.getTableHeader().setReorderingAllowed(false);

        int[] sirine = {60, 120, 90, 260, 140, 400};
        TableColumnModel kolone = tabelaTermina.getColumnModel();
        for (int i = 0; i < kolone.getColumnCount() && i < sirine.length; i++) {
            kolone.getColumn(i).setPreferredWidth(sirine[i]);
        }
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

    /**
     * Odjavljuje stomatologa i vraca korisnika na formu za prijavu.
     *
     * Kontroler pri odjavi zatvara i vezu sa serverom, a Komunikacija pri
     * zatvaranju ponistava svoju instancu, pa se pri sledecoj prijavi otvara
     * nova veza. Aplikacija nastavlja da radi - ne poziva se System.exit.
     */
    private void odjaviSe() {
        Kontroler.getInstanca().odjaviSe();
        dispose();
        new LoginForma().setVisible(true);
    }

    private void zatvoriAplikaciju() {
        Kontroler.getInstanca().odjaviSe();
        dispose();
        System.exit(0);
    }
}
