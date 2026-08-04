/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kontroler;

import java.io.IOException;
import java.util.List;
import komunikacija.Komunikacija;
import komunikacija.Odgovor;
import komunikacija.Operacija;
import model.Pacijent;
import model.Stomatolog;
import model.Termin;
import model.Usluga;

/**
 * Klijentski kontroler - jedini sloj koji forme koriste za rad sa serverom.
 *
 * Forme ne prave Zahtev niti kastuju sirov Object iz odgovora, vec pozivaju
 * domenski imenovane metode ove klase. Sve greske se prijavljuju bacanjem
 * izuzetka, pa forma samo hvata Exception i prikazuje poruku.
 *
 * Ovde je na jednom mestu sakupljen i oblik parametara koji se salje serveru,
 * pa se eventualno usaglasavanje sa serverom radi samo u ovoj klasi.
 *
 * @author vukla
 */
public class Kontroler {

    private static Kontroler instanca;

    private Stomatolog ulogovaniStomatolog;

    private Kontroler() {
    }

    public static synchronized Kontroler getInstanca() {
        if (instanca == null) {
            instanca = new Kontroler();
        }
        return instanca;
    }

    public Stomatolog getUlogovaniStomatolog() {
        return ulogovaniStomatolog;
    }

    /**
     * Prijavljuje stomatologa na sistem i pamti ga kao ulogovanog korisnika.
     *
     * @return ulogovani stomatolog
     * @throws Exception ako su podaci pogresni ili server nije dostupan
     */
    public Stomatolog prijaviSe(String korisnickoIme, String sifra) throws Exception {
        Object rezultat = posalji(Operacija.PRIJAVA_STOMATOLOG,
                new Object[]{korisnickoIme, sifra});

        if (rezultat == null) {
            throw new Exception("Pogrešno korisničko ime ili šifra");
        }

        ulogovaniStomatolog = (Stomatolog) rezultat;
        return ulogovaniStomatolog;
    }

    /**
     * Odjavljuje korisnika i zatvara vezu sa serverom.
     */
    public void odjaviSe() {
        ulogovaniStomatolog = null;
        try {
            Komunikacija.getInstanca().zatvoriVezu();
        } catch (IOException ex) {
            // veza ionako nije uspostavljena, nema sta da se zatvara
        }
    }

    /**
     * Vraca termine koji zadovoljavaju zadati kriterijum (WHERE klauza).
     */
    @SuppressWarnings("unchecked")
    public List<Termin> vratiListuTermina(String kriterijum) throws Exception {
        return (List<Termin>) posalji(Operacija.VRATI_LISTU_TERMINA, kriterijum);
    }

    /**
     * Vraca termine ulogovanog stomatologa.
     */
    public List<Termin> vratiTermineUlogovanog() throws Exception {
        if (ulogovaniStomatolog == null) {
            throw new Exception("Nijedan stomatolog nije prijavljen na sistem.");
        }
        return vratiListuTermina("idStomatolog = " + ulogovaniStomatolog.getIdStomatolog());
    }

    @SuppressWarnings("unchecked")
    public List<Pacijent> vratiListuPacijenata() throws Exception {
        List<Pacijent> pacijenti = (List<Pacijent>) posalji(Operacija.VRATI_LISTU_PACIJENATA, null);
        if (pacijenti == null) {
            throw new Exception("Neuspešno učitavanje liste pacijenata.");
        }
        return pacijenti;
    }

    @SuppressWarnings("unchecked")
    public List<Usluga> vratiListuUsluga() throws Exception {
        List<Usluga> usluge = (List<Usluga>) posalji(Operacija.VRATI_LISTU_USLUGA, null);
        if (usluge == null) {
            throw new Exception("Neuspešno učitavanje liste usluga.");
        }
        return usluge;
    }

    @SuppressWarnings("unchecked")
    public List<Stomatolog> vratiListuStomatologa() throws Exception {
        List<Stomatolog> stomatolozi = (List<Stomatolog>) posalji(Operacija.VRATI_LISTU_STOMATOLOGA, null);
        if (stomatolozi == null) {
            throw new Exception("Neuspešno učitavanje liste stomatologa.");
        }
        return stomatolozi;
    }

    public Termin pretraziTermin(int idTermin) throws Exception {
        return (Termin) posalji(Operacija.PRETRAZI_TERMIN, idTermin);
    }

    public void ubaciTermin(Termin termin) throws Exception {
        proveriUspeh(posalji(Operacija.UBACI_TERMIN, termin),
                "Zakazivanje termina nije uspelo. Proverite da li je termin zauzet.");
    }

    public void promeniTermin(Termin termin) throws Exception {
        proveriUspeh(posalji(Operacija.PROMENI_TERMIN, termin),
                "Izmena termina nije uspela. Proverite da li je termin zauzet.");
    }

    public void obrisiTermin(Termin termin) throws Exception {
        proveriUspeh(posalji(Operacija.OBRISI_TERMIN, termin),
                "Brisanje termina nije uspelo.");
    }

    /**
     * Server na operacije ubacivanja/izmene/brisanja vraca Boolean.
     */
    private void proveriUspeh(Object rezultat, String porukaGreske) throws Exception {
        if (!(rezultat instanceof Boolean) || !((Boolean) rezultat)) {
            throw new Exception(porukaGreske);
        }
    }

    /**
     * Salje zahtev serveru i vraca sadrzaj odgovora.
     *
     * @throws Exception ako server nije dostupan ili je komunikacija pukla
     */
    private Object posalji(Operacija operacija, Object parametar) throws Exception {
        Komunikacija komunikacija;
        try {
            komunikacija = Komunikacija.getInstanca();
        } catch (IOException ex) {
            throw new Exception("Server nije dostupan. Proverite da li je server pokrenut.");
        }

        Odgovor odgovor = komunikacija.posaljiZahtev(operacija, parametar);
        if (odgovor == null) {
            throw new Exception("Greška u komunikaciji sa serverom.");
        }
        return odgovor.getOdgovor();
    }
}
