package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Glavna serverska nit - osluskuje port, prihvata klijente i za svakog otvara
 * po jednu nit {@link ObradaZahteva}.
 *
 * @author vukla
 */
public class GlavniServer extends Thread {

    private static final Logger logger = Logger.getLogger(GlavniServer.class.getName());

    public static final int PORT = 9000;

    private ServerSocket serverSocket;
    private final List<ObradaZahteva> niti = new ArrayList<>();
    private volatile boolean kraj;

    /**
     * Otvara serverski soket i pokrece nit koja prihvata klijente.
     *
     * @throws IOException ako je port zauzet
     */
    public void pokreniServer() throws IOException {
        serverSocket = new ServerSocket(PORT);
        kraj = false;
        start();
        logger.log(Level.INFO, "Server je pokrenut i osluskuje port {0}", PORT);
    }

    @Override
    public void run() {
        while (!kraj) {
            try {
                Socket socket = serverSocket.accept();
                logger.log(Level.INFO, "Povezao se klijent {0}", socket.getRemoteSocketAddress());

                ObradaZahteva obrada = new ObradaZahteva(socket);
                synchronized (niti) {
                    niti.add(obrada);
                }
                obrada.start();
            } catch (IOException ex) {
                if (!kraj) {
                    logger.log(Level.SEVERE, "Greska prilikom prihvatanja klijenta", ex);
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

        logger.log(Level.INFO, "Server je zaustavljen.");
    }
}
