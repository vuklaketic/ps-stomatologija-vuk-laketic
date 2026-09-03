import forma.Izgled;
import forma.ServerskaForma;
import javax.swing.SwingUtilities;

/**
 * Ulazna tacka serverske aplikacije - otvara serversku formu sa koje se
 * server pokrece/zaustavlja i menjaju parametri konekcije sa bazom.
 *
 * @author vukla
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Izgled.primeni();
            new ServerskaForma().setVisible(true);
        });
    }
}
