package komunikacija;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Primalac {

    private static final Logger logger =
            Logger.getLogger(Primalac.class.getName());

    private Socket socket;
    private ObjectInputStream in;

    public Primalac(Socket socket) {
        this.socket = socket;
        try {
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Greška pri inicijalizaciji primaoca", ex);
        }
    }

    public Object primi() {
        try {
            return in.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "Greška prilikom prijema objekta", ex);
            return null;
        }
    }
}
