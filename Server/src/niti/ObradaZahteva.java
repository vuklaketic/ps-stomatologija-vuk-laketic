package server;

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

    private volatile boolean kraj;

    /**
     * Redosled je bitan i mora da odgovara klijentu: prvo se pravi posiljalac
     * (ObjectOutputStream salje zaglavlje), pa tek onda primalac
     * (ObjectInputStream ceka zaglavlje sa druge strane).
     */
    public ObradaZahteva(Socket socket) {
        this.socket = socket;
        this.posiljalac = new Posiljalac(socket);
        this.primalac = new Primalac(socket);
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
     * Izvrsava operaciju iz zahteva. Ako operacija ne uspe, u odgovoru se vraca
     * null (za pretrage) odnosno false (za izmene stanja), kako klijentski
     * kontroler i ocekuje.
     */
    private Odgovor obradiZahtev(Zahtev zahtev) {
        Operacija operacija = zahtev.getOperacija();
        Object parametar = zahtev.getParametar();

        logger.log(Level.INFO, "Primljen zahtev: {0}", operacija);

        try {
            Kontroler kontroler = Kontroler.getInstanca();

            switch (operacija) {
                case PRIJAVA_STOMATOLOG:
                case LOGIN: {
                    Object[] podaci = (Object[]) parametar;
                    String korisnickoIme = (String) podaci[0];
                    String sifra = (String) podaci[1];
                    return new Odgovor(kontroler.prijaviStomatologa(korisnickoIme, sifra));
                }
                case VRATI_LISTU_TERMINA:
                    return new Odgovor(kontroler.vratiListuTermina((String) parametar));

                case VRATI_LISTU_STOMATOLOGA:
                    return new Odgovor(kontroler.vratiListuStomatologa());

                case VRATI_LISTU_PACIJENATA:
                    return new Odgovor(kontroler.vratiListuPacijenata());

                case VRATI_LISTU_USLUGA:
                    return new Odgovor(kontroler.vratiListuUsluga());

                case PRETRAZI_TERMIN:
                    return new Odgovor(kontroler.pretraziTermin(((Number) parametar).intValue()));

                case KREIRAJ_TERMIN:
                case UBACI_TERMIN:
                    return new Odgovor(kontroler.ubaciTermin((Termin) parametar));

                case PROMENI_TERMIN:
                    return new Odgovor(kontroler.promeniTermin((Termin) parametar));

                case OBRISI_TERMIN:
                    return new Odgovor(kontroler.obrisiTermin((Termin) parametar));

                default:
                    logger.log(Level.WARNING, "Nepoznata operacija: {0}", operacija);
                    return new Odgovor(null);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Greska prilikom izvrsavanja operacije " + operacija, ex);
            return new Odgovor(neuspehZa(operacija));
        }
    }

    /**
     * Operacije koje menjaju stanje sistema klijentu vracaju logicku vrednost,
     * a operacije pretrage null.
     */
    private Object neuspehZa(Operacija operacija) {
        switch (operacija) {
            case KREIRAJ_TERMIN:
            case UBACI_TERMIN:
            case PROMENI_TERMIN:
            case OBRISI_TERMIN:
                return Boolean.FALSE;
            default:
                return null;
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
                socket.close();
                logger.log(Level.INFO, "Prekinuta veza sa klijentom.");
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Greska prilikom zatvaranja veze sa klijentom", ex);
        }
    }
}
