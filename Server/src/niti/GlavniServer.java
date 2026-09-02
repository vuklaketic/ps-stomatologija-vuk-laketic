package niti;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Glavna serverska nit - osluskuje port, prihvata klijente i za svakog otvara
 * po jednu nit {@link ObradaZahteva}. Njome upravlja serverska forma preko
 * {@link #pokreniServer()} i {@link #zaustaviServer()}; posto nasledjuje
 * {@link Thread}, forma za svako novo pokretanje pravi novi objekat.
 *
 * Dogadjaje prati {@link OsluskivacServera}, ako je postavljen, pa forma moze
 * da prikaze iste poruke koje idu i u log.
 *
 * @author vukla
 */
public class GlavniServer extends Thread {

    private static final Logger logger = Logger.getLogger(GlavniServer.class.getName());

    public static final int PORT = 9000;

    private ServerSocket serverSocket;
    private final List<ObradaZahteva> niti = new ArrayList<>();
    private volatile boolean kraj;
    private volatile OsluskivacServera osluskivac;

    /**
     * Postavlja osluskivaca kome se prijavljuju dogadjaji sa servera.
     */
    public void setOsluskivac(OsluskivacServera osluskivac) {
        this.osluskivac = osluskivac;
    }

    /**
     * Vraca true ako server trenutno osluskuje port.
     */
    public boolean jePokrenut() {
        return !kraj && serverSocket != null && !serverSocket.isClosed();
    }

    /**
     * Upisuje poruku u log i prosledjuje je osluskivacu, ako postoji.
     */
    private void zabelezi(String poruka) {
        logger.log(Level.INFO, poruka);
        OsluskivacServera trenutni = osluskivac;
        if (trenutni != null) {
            trenutni.zabelezi(poruka);
        }
    }

    /**
     * Otvara serverski soket i pokrece nit koja prihvata klijente.
     *
     * @throws IOException ako je port zauzet
     */
    public void pokreniServer() throws IOException {
        serverSocket = new ServerSocket(PORT);
        kraj = false;
        start();
        zabelezi("Server je pokrenut na portu " + PORT + ".");
    }

    @Override
    public void run() {
        while (!kraj) {
            try {
                Socket socket = serverSocket.accept();
                zabelezi("Klijent povezan: " + socket.getRemoteSocketAddress());

                ObradaZahteva obrada = new ObradaZahteva(socket, osluskivac);
                synchronized (niti) {
                    niti.add(obrada);
                }
                obrada.start();
            } catch (IOException ex) {
                if (!kraj) {
                    logger.log(Level.SEVERE, "Greska prilikom prihvatanja klijenta", ex);
                    zabelezi("Greška prilikom prihvatanja klijenta: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Zaustavlja server i prekida sve otvorene veze sa klijentima.
     */
    public void zaustaviServer() {
        kraj = true;

        synchronized (niti) {
            for (ObradaZahteva obrada : niti) {
                obrada.prekiniObradu();
            }
            niti.clear();
        }

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Greska prilikom zatvaranja serverskog soketa", ex);
        }

        zabelezi("Server je zaustavljen.");
    }
}
