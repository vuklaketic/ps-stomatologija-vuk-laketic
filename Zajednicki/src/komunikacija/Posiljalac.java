package komunikacija;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Posiljalac {

    private static final Logger logger =
            Logger.getLogger(Posiljalac.class.getName());

    private Socket socket;
    private ObjectOutputStream out;

    public Posiljalac(Socket socket) {
        this.socket = socket;
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Greška pri inicijalizaciji pošiljaoca", ex);
        }
    }

    public void posalji(Object obj) {
        try {
            out.writeObject(obj);
            out.flush();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Greška prilikom slanja objekta", ex);
        }
    }
}
