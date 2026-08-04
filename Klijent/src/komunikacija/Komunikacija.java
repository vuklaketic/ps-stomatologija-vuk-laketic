/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package komunikacija;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton klasa koja drzi jednu jedinu konekciju klijenta ka serveru.
 *
 * @author vukla
 */
public class Komunikacija {

    private static final Logger logger = Logger.getLogger(Komunikacija.class.getName());

    private static final String ADRESA_SERVERA = "localhost";
    private static final int PORT_SERVERA = 9000;

    private static Komunikacija instanca;

    private Socket socket;
    private Posiljalac posiljalac;
    private Primalac primalac;

    /**
     * Otvara soket ka serveru i priprema posiljaoca i primaoca.
     *
     * Bitno: prvo se pravi Posiljalac (ObjectOutputStream), pa tek onda
     * Primalac (ObjectInputStream). ObjectInputStream u konstruktoru ceka
     * zaglavlje sa druge strane, pa bi obrnut redosled zablokirao konekciju.
     */
    private Komunikacija() throws IOException {
        socket = new Socket(ADRESA_SERVERA, PORT_SERVERA);
        posiljalac = new Posiljalac(socket);
        primalac = new Primalac(socket);
        logger.log(Level.INFO, "Uspostavljena veza sa serverom {0}:{1}",
                new Object[]{ADRESA_SERVERA, PORT_SERVERA});
    }

    /**
     * Vraca jedinu instancu komunikacije. Ako veza jos nije uspostavljena,
     * pokusava da je uspostavi.
     *
     * @return instanca komunikacije
     * @throws IOException ako server nije pokrenut ili veza ne moze da se otvori
     */
    public static synchronized Komunikacija getInstanca() throws IOException {
        if (instanca == null) {
            instanca = new Komunikacija();
        }
        return instanca;
    }

    /**
     * Salje zahtev serveru i sinhrono ceka odgovor (jedan zahtev -> jedan odgovor).
     *
     * @param operacija operacija koju server treba da izvrsi
     * @param parametar parametar operacije (moze biti null)
     * @return odgovor servera ili null ako je doslo do greske u komunikaciji
     */
    public synchronized Odgovor posaljiZahtev(Operacija operacija, Object parametar) {
        try {
            Zahtev zahtev = new Zahtev(operacija, parametar);
            posiljalac.posalji(zahtev);

            Object primljeno = primalac.primi();
            if (primljeno == null) {
                logger.log(Level.SEVERE, "Server nije vratio odgovor za operaciju {0}", operacija);
                return null;
            }
            if (!(primljeno instanceof Odgovor)) {
                logger.log(Level.SEVERE, "Server je vratio neocekivan tip objekta: {0}",
                        primljeno.getClass().getName());
                return null;
            }
            return (Odgovor) primljeno;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Greska prilikom komunikacije sa serverom", ex);
            return null;
        }
    }

    /**
     * Zatvara vezu sa serverom.
     */
    public synchronized void zatvoriVezu() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Greska prilikom zatvaranja veze", ex);
        } finally {
            instanca = null;
        }
    }
}
