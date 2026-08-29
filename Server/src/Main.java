import forma.ServerskaForma;
import javax.swing.SwingUtilities;

/**
 * Ulazna tacka serverske aplikacije.
 *
 * Otvara serversku formu sa koje se server pokrece i zaustavlja i sa koje se
 * menjaju parametri konekcije sa bazom.
 *
 * @author vukla
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServerskaForma().setVisible(true));
    }
}
