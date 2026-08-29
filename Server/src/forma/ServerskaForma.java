package forma;

import baza.Konekcija;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import niti.GlavniServer;
import niti.OsluskivacServera;

/**
 * Glavna forma serverske aplikacije.
 *
 * Sa forme se server pokrece i zaustavlja, prati se sta se na njemu desava i
 * menjaju se parametri konekcije sa bazom (meni "Podešavanja"), bez rucnog
 * menjanja konfiguracionog fajla i bez ponovnog pokretanja aplikacije.
 *
 * Sama logika servera ostaje u klasi {@link GlavniServer} - forma je samo
 * upravlja i prikazuje poruke koje joj ta klasa salje kao osluskivacu.
 *
 * @author vukla
 */
public class ServerskaForma extends JFrame implements OsluskivacServera {

    private static final DateTimeFormatter FORMAT_VREMENA = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Server se posle zaustavljanja ne moze ponovo pokrenuti kao ista nit, pa
     * se za svako pokretanje pravi nova instanca.
     */
    private GlavniServer server;

    private JTextArea txtStatus;
    private JLabel lblStanje;
    private JButton btnPokreni;
    private JButton btnZaustavi;

    public ServerskaForma() {
        inicijalizujKomponente();
        zabelezi("Server nije pokrenut.");
    }

    private void inicijalizujKomponente() {
        setTitle("Server - Zakazivanje termina");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(true);
        setLayout(new BorderLayout());

        setJMenuBar(napraviMeni());

        JPanel panelStanje = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelStanje.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        JLabel lblNaslov = new JLabel("Status:");
        lblNaslov.setFont(lblNaslov.getFont().deriveFont(Font.BOLD));
        panelStanje.add(lblNaslov);
        lblStanje = new JLabel();
        lblStanje.setFont(lblStanje.getFont().deriveFont(Font.BOLD));
        panelStanje.add(lblStanje);
        add(panelStanje, BorderLayout.NORTH);

        txtStatus = new JTextArea();
        txtStatus.setEditable(false);
        txtStatus.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        txtStatus.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane klizac = new JScrollPane(txtStatus);
        klizac.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 20, 0, 20),
                BorderFactory.createTitledBorder("Poruke servera")));
        add(klizac, BorderLayout.CENTER);

        JPanel panelDugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panelDugmad.setBorder(BorderFactory.createEmptyBorder(0, 20, 5, 10));
        btnPokreni = new JButton("Pokreni server");
        btnZaustavi = new JButton("Zaustavi server");
        panelDugmad.add(btnPokreni);
        panelDugmad.add(btnZaustavi);
        add(panelDugmad, BorderLayout.SOUTH);

        btnPokreni.addActionListener(e -> pokreniServer());
        btnZaustavi.addActionListener(e -> zaustaviServer());

        prikaziStanje(false);

        // prozor se otvara maksimizovan, a ova velicina vazi kada ga korisnik
        // vrati iz maksimizovanog stanja
        setSize(900, 550);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // pri zatvaranju forme server se uredno gasi i konekcija se zatvara
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                zatvoriAplikaciju();
            }
        });
    }

    private JMenuBar napraviMeni() {
        JMenuBar meni = new JMenuBar();

        JMenu meniPodesavanja = new JMenu("Podešavanja");
        JMenuItem stavkaBaza = new JMenuItem("Konekcija sa bazom...");
        stavkaBaza.addActionListener(e -> new FormaKonfBaza(this).setVisible(true));
        meniPodesavanja.add(stavkaBaza);

        meni.add(meniPodesavanja);
        return meni;
    }

    /**
     * Pokrece server u posebnoj niti, da otvaranje soketa i provera baze ne bi
     * blokirali korisnicki interfejs.
     */
    private void pokreniServer() {
        if (server != null && server.jePokrenut()) {
            JOptionPane.showMessageDialog(this, "Server je već pokrenut.",
                    "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnPokreni.setEnabled(false);
        zabelezi("Pokretanje servera...");

        new Thread(() -> {
            // veza sa bazom se proverava pre otvaranja porta, da server ne bi
            // primao klijente ako baza nije dostupna
            try {
                Konekcija.getInstanca().getKonekcija();
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    zabelezi("Server nije pokrenut jer baza nije dostupna: " + ex.getMessage());
                    prikaziStanje(false);
                    JOptionPane.showMessageDialog(ServerskaForma.this,
                            "Server nije pokrenut jer baza nije dostupna:\n" + ex.getMessage()
                            + "\n\nProverite podešavanja u meniju Podešavanja > Konekcija sa bazom...",
                            "Greška", JOptionPane.ERROR_MESSAGE);
                });
                return;
            }

            GlavniServer noviServer = new GlavniServer();
            noviServer.setOsluskivac(ServerskaForma.this);
            try {
                noviServer.pokreniServer();
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    zabelezi("Server nije mogao da se pokrene: " + ex.getMessage());
                    prikaziStanje(false);
                    JOptionPane.showMessageDialog(ServerskaForma.this,
                            "Server nije mogao da se pokrene:\n" + ex.getMessage(),
                            "Greška", JOptionPane.ERROR_MESSAGE);
                });
                return;
            }

            server = noviServer;
            SwingUtilities.invokeLater(() -> prikaziStanje(true));
        }, "Pokretanje-servera").start();
    }

    private void zaustaviServer() {
        if (server == null || !server.jePokrenut()) {
            prikaziStanje(false);
            return;
        }

        btnZaustavi.setEnabled(false);
        server.zaustaviServer();
        server = null;
        prikaziStanje(false);
    }

    /**
     * Uskladjuje dugmad i tekst statusa sa stanjem servera.
     */
    private void prikaziStanje(boolean pokrenut) {
        btnPokreni.setEnabled(!pokrenut);
        btnZaustavi.setEnabled(pokrenut);
        lblStanje.setText(pokrenut ? "POKRENUT" : "ZAUSTAVLJEN");
        lblStanje.setForeground(pokrenut ? new Color(0, 128, 0) : Color.RED);
    }

    /**
     * Ispisuje poruku o dogadjaju u statusnu oblast forme. Poziva se i iz
     * serverskih niti, pa se ispis prebacuje na nit korisnickog interfejsa.
     */
    @Override
    public final void zabelezi(String poruka) {
        String red = "[" + LocalTime.now().format(FORMAT_VREMENA) + "] " + poruka + "\n";
        if (SwingUtilities.isEventDispatchThread()) {
            dodajRed(red);
        } else {
            SwingUtilities.invokeLater(() -> dodajRed(red));
        }
    }

    private void dodajRed(String red) {
        txtStatus.append(red);
        txtStatus.setCaretPosition(txtStatus.getDocument().getLength());
    }

    private void zatvoriAplikaciju() {
        int potvrda = JOptionPane.showConfirmDialog(this,
                "Da li ste sigurni da želite da zatvorite server?",
                "Zatvaranje servera", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (potvrda != JOptionPane.YES_OPTION) {
            return;
        }

        if (server != null) {
            server.zaustaviServer();
        }
        Konekcija.getInstanca().zatvoriKonekciju();
        dispose();
        System.exit(0);
    }
}
