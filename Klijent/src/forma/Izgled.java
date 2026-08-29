package forma;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Font;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.InsetsUIResource;

/**
 * Podesavanje izgleda korisnickog interfejsa.
 *
 * Postavlja FlatLaf temu, malo veci font i razmake koji vaze za sve forme, pa
 * pojedinacne forme ne moraju da se bave bojama i fontovima. Poziva se jednom,
 * na pocetku metode main, pre nego sto se napravi prva forma.
 *
 * @author vukla
 */
public class Izgled {

    private static final Logger logger = Logger.getLogger(Izgled.class.getName());

    /** Podrazumevani Swing font je oko 11px i sitan je za citanje. */
    private static final int VELICINA_FONTA = 14;

    private static final Color BOJA_PARNIH_REDOVA = new Color(0xF2, 0xF5, 0xF9);

    private Izgled() {
    }

    /**
     * Primenjuje temu i zajednicka podesavanja izgleda.
     */
    public static void primeni() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            // ako tema ne moze da se ucita, aplikacija nastavlja da radi
            // sa podrazumevanim izgledom
            logger.log(Level.WARNING, "Tema nije primenjena, koristi se podrazumevani izgled.", ex);
        }

        postaviVelicinuFonta(VELICINA_FONTA);

        // blago zaobljene ivice i vise vazduha u komponentama
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("Button.margin", new InsetsUIResource(6, 14, 6, 14));

        // tabela: visi redovi i naizmenicno bojenje
        UIManager.put("Table.rowHeight", 26);
        UIManager.put("Table.alternateRowColor", BOJA_PARNIH_REDOVA);
        UIManager.put("Table.showHorizontalLines", true);
    }

    /**
     * Menja velicinu svih fontova teme, a zadrzava familiju koju je tema
     * izabrala za tekuci operativni sistem.
     */
    private static void postaviVelicinuFonta(int velicina) {
        UIDefaults podrazumevano = UIManager.getLookAndFeelDefaults();
        for (Enumeration<Object> kljucevi = podrazumevano.keys(); kljucevi.hasMoreElements();) {
            Object kljuc = kljucevi.nextElement();
            Object vrednost = UIManager.get(kljuc);
            if (vrednost instanceof Font) {
                Font font = (Font) vrednost;
                UIManager.put(kljuc, new FontUIResource(font.deriveFont((float) velicina)));
            }
        }
    }
}
