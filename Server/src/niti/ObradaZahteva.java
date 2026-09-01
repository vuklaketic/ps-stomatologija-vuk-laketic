package niti;

import controller.Kontroler;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import komunikacija.Odgovor;
import komunikacija.Operacija;
import komunikacija.Posiljalac;
import komunikacija.Primalac;
import komunikacija.Zahtev;
import model.Pacijent;
import model.Specijalizacija;
import model.Stomatolog;
import model.Termin;

/**
 * Nit koja opsluzuje jednog klijenta: prima zahtev, prosledjuje ga serverskom
 * kontroleru i vraca odgovor. Radi sve dok klijent ne prekine vezu.
 *
 * @author vukla
 */
public class ObradaZahteva extends Thread {

    private static final Logger logger = Logger.getLogger(ObradaZahteva.class.getName());

    private final Socket socket;
    private final Posiljalac posiljalac;
    private final Primalac primalac;
    private final OsluskivacServera osluskivac;

    private volatile boolean kraj;

    public ObradaZahteva(Socket socket) {
        this(socket, null);
    }

    /**
     * Redosled je bitan i mora da odgovara klijentu: prvo se pravi posiljalac
     * (ObjectOutputStream salje zaglavlje), pa tek onda primalac
     * (ObjectInputStream ceka zaglavlje sa druge strane).
     *
     * @param socket veza sa klijentom
     * @param osluskivac osluskivac dogadjaja, moze biti null kada se server
     * koristi bez korisnickog interfejsa
     */
    public ObradaZahteva(Socket socket, OsluskivacServera osluskivac) {
        this.socket = socket;
        this.osluskivac = osluskivac;
        this.posiljalac = new Posiljalac(socket);
        this.primalac = new Primalac(socket);
    }

    /**
     * Prosledjuje poruku osluskivacu, ako je postavljen.
     */
    private void zabelezi(String poruka) {
        if (osluskivac != null) {
            osluskivac.zabelezi(poruka);
        }
    }

    @Override
    public void run() {
        while (!kraj && !socket.isClosed()) {
            Object primljeno = primalac.primi();
            if (primljeno == null) {
                break;
            }
            if (!(primljeno instanceof Zahtev)) {
                logger.log(Level.WARNING, "Primljen neocekivan tip objekta: {0}",
                        primljeno.getClass().getName());
                continue;
            }

            Odgovor odgovor = obradiZahtev((Zahtev) primljeno);
            posiljalac.posalji(odgovor);
        }

        zatvoriVezu();
    }

    /**
     * Izvrsava operaciju iz zahteva. Ako operacija ne uspe, izuzetak se ne gubi
     * vec se pakuje u odgovor, pa klijent prikazuje poruku koja je nastala na
     * mestu greske.
     */
    private Odgovor obradiZahtev(Zahtev zahtev) {
        Operacija operacija = zahtev.getOperacija();
        Object parametar = zahtev.getParametar();

        logger.log(Level.INFO, "Primljen zahtev: {0}", operacija);
        zabelezi("Zahtev klijenta " + socket.getRemoteSocketAddress() + ": " + operacija);

        try {
            return Odgovor.uspeh(izvrsi(operacija, parametar));
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Operacija " + operacija + " nije uspela: " + ex.getMessage());
            return Odgovor.greska(ex);
        }
    }

    private Object izvrsi(Operacija operacija, Object parametar) throws Exception {
        Kontroler kontroler = Kontroler.getInstanca();

        switch (operacija) {
            case PRIJAVA_STOMATOLOG: {
                Object[] podaci = (Object[]) parametar;
                return kontroler.prijaviStomatologa((String) podaci[0], (String) podaci[1]);
            }
            case VRATI_LISTU_TERMINA:
                return kontroler.vratiListuTermina((Termin) parametar);

            case VRATI_LISTU_STOMATOLOGA:
                return kontroler.vratiListuStomatologa();

            case VRATI_SPECIJALIZACIJE_STOMATOLOGA:
                return kontroler.vratiSpecijalizacijeStomatologa((Stomatolog) parametar);

            case VRATI_LISTU_PACIJENATA:
                return kontroler.vratiListuPacijenata((Pacijent) parametar);

            case VRATI_LISTU_ORDINACIJA:
                return kontroler.vratiListuOrdinacija();

            case UBACI_SPECIJALIZACIJA:
                return kontroler.ubaciSpecijalizaciju((Specijalizacija) parametar);

            case PRETRAZI_PACIJENT:
                return kontroler.pretraziPacijenta((Pacijent) parametar);

            case UBACI_PACIJENT:
                return kontroler.ubaciPacijenta((Pacijent) parametar);

            case PROMENI_PACIJENT:
                return kontroler.promeniPacijenta((Pacijent) parametar);

            case OBRISI_PACIJENT:
                kontroler.obrisiPacijenta((Pacijent) parametar);
                return null;

            case VRATI_LISTU_USLUGA:
                return kontroler.vratiListuUsluga();

            case PRETRAZI_TERMIN:
                return kontroler.pretraziTermin((Termin) parametar);

            case UBACI_TERMIN:
                return kontroler.ubaciTermin((Termin) parametar);

            case PROMENI_TERMIN:
                return kontroler.promeniTermin((Termin) parametar);

            case OBRISI_TERMIN:
                kontroler.obrisiTermin((Termin) parametar);
                return null;

            default:
                throw new Exception("Server ne podržava operaciju " + operacija + ".");
        }
    }

    /**
     * Prekida obradu i zatvara vezu sa klijentom.
     */
    public void prekiniObradu() {
        kraj = true;
        zatvoriVezu();
    }

    private void zatvoriVezu() {
        try {
            if (!socket.isClosed()) {
                Object adresa = socket.getRemoteSocketAddress();
                socket.close();
                logger.log(Level.INFO, "Prekinuta veza sa klijentom.");
                zabelezi("Klijent diskonektovan: " + adresa);
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Greska prilikom zatvaranja veze sa klijentom", ex);
        }
    }
}
