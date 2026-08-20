import baza.Konekcija;
import java.util.logging.Level;
import java.util.logging.Logger;
import niti.GlavniServer;

/**
 * Ulazna tacka serverske aplikacije.
 *
 * @author vukla
 */
public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        GlavniServer server = new GlavniServer();

        // veza sa bazom se proverava odmah, da server ne bi primao klijente
        // ako baza nije dostupna
        try {
            Konekcija.getInstanca().getKonekcija();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Server nije pokrenut jer baza nije dostupna", ex);
            return;
        }

        try {
            server.pokreniServer();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Server nije mogao da se pokrene", ex);
            Konekcija.getInstanca().zatvoriKonekciju();
            return;
        }

        // uredno gasenje servera na prekid rada aplikacije (Ctrl+C)
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.zaustaviServer();
                Konekcija.getInstanca().zatvoriKonekciju();
            }
        });
    }
}
