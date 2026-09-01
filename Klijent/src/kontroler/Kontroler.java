/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kontroler;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import komunikacija.Komunikacija;
import komunikacija.Odgovor;
import komunikacija.Operacija;
import komunikacija.TipOdgovora;
import model.Ordinacija;
import model.Pacijent;
import model.Specijalizacija;
import model.StatusTermina;
import model.StavkaTermina;
import model.Stomatolog;
import model.Termin;
import model.Usluga;

/**
 * Klijentski kontroler - jedini sloj koji forme koriste za rad sa serverom.
 *
 * Forme ne prave Zahtev niti kastuju sirov Object iz odgovora, vec pozivaju
 * domenski imenovane metode ove klase. Sve greske se prijavljuju bacanjem
 * izuzetka, pa forma samo hvata Exception i prikazuje poruku. Poruka pritom
 * stize sa servera, sa mesta na kome je greska i nastala.
 *
 * Kriterijumi pretrage se serveru salju kao domenski objekti, a ne kao delovi
 * SQL upita - klijent ne poznaje ni tabele ni kolone baze.
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
        ulogovaniStomatolog = (Stomatolog) posalji(Operacija.PRIJAVA_STOMATOLOG,
                new Object[]{korisnickoIme, sifra});
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
     * Vraca termine koji odgovaraju zadatom kriterijumu.
     *
     * @param kriterijum termin sa popunjenim podacima po kojima se pretrazuje;
     *                   ako je stomatolog postavljen, vracaju se samo njegovi termini
     */
    @SuppressWarnings("unchecked")
    public List<Termin> vratiListuTermina(Termin kriterijum) throws Exception {
        return (List<Termin>) posalji(Operacija.VRATI_LISTU_TERMINA, kriterijum);
    }

    /**
     * Vraca sve termine ulogovanog stomatologa.
     */
    public List<Termin> vratiTermineUlogovanog() throws Exception {
        return pretraziTermineUlogovanog(null, null, null, null);
    }

    /**
     * Vraca termine ulogovanog stomatologa koji odgovaraju zadatim kriterijumima.
     *
     * Kriterijumi se serveru salju kao popunjena polja domenskog objekta, a ne
     * kao delovi SQL upita. Svaki argument koji je null znaci da se po tom
     * podatku ne filtrira.
     *
     * @param datum dan za koji se traze termini, ili null za sve dane
     * @param status trazeni status termina, ili null za sve statuse
     * @param pacijent pacijent cji se termini traze, ili null za sve pacijente
     * @param usluga usluga koja mora da se nadje na terminu, ili null za sve usluge
     */
    public List<Termin> pretraziTermineUlogovanog(LocalDate datum, StatusTermina status,
            Pacijent pacijent, Usluga usluga) throws Exception {
        if (ulogovaniStomatolog == null) {
            throw new Exception("Nijedan stomatolog nije prijavljen na sistem.");
        }

        Termin kriterijum = new Termin();
        kriterijum.setStomatolog(ulogovaniStomatolog);
        kriterijum.setDatum(datum);
        kriterijum.setStatus(status);
        kriterijum.setPacijent(pacijent);

        // usluga se prosledjuje kao stavka kriterijuma, jer je usluga sa
        // terminom povezana upravo preko stavke
        if (usluga != null) {
            StavkaTermina stavka = new StavkaTermina();
            stavka.setUsluga(usluga);
            kriterijum.getStavke().add(stavka);
        }
        return vratiListuTermina(kriterijum);
    }

    /**
     * Vraca specijalizacije ulogovanog stomatologa, sortirane po nazivu.
     *
     * @return lista specijalizacija, prazna ako stomatolog nema nijednu
     * @throws Exception ako niko nije prijavljen ili ako citanje ne uspe
     */
    @SuppressWarnings("unchecked")
    public List<Specijalizacija> vratiSpecijalizacijeUlogovanog() throws Exception {
        if (ulogovaniStomatolog == null) {
            throw new Exception("Nijedan stomatolog nije prijavljen na sistem.");
        }
        return (List<Specijalizacija>) posalji(Operacija.VRATI_SPECIJALIZACIJE_STOMATOLOGA,
                ulogovaniStomatolog);
    }

    /**
     * Salje serveru novu specijalizaciju na cuvanje.
     *
     * @param specijalizacija specijalizacija koja se pamti
     * @throws Exception ako podaci nisu ispravni ili ako upis ne uspe
     */
    public void ubaciSpecijalizaciju(Specijalizacija specijalizacija) throws Exception {
        posalji(Operacija.UBACI_SPECIJALIZACIJA, specijalizacija);
    }

    @SuppressWarnings("unchecked")
    public List<Pacijent> vratiListuPacijenata() throws Exception {
        return (List<Pacijent>) posalji(Operacija.VRATI_LISTU_PACIJENATA, null);
    }

    /**
     * Vraca pacijente koji odgovaraju zadatim kriterijumima.
     *
     * Kriterijumi se serveru salju kao popunjena polja domenskog objekta.
     * Prazno ime ili prezime, odnosno nepostavljena ordinacija, znace da se po
     * tom podatku ne filtrira.
     */
    public List<Pacijent> pretraziPacijente(String ime, String prezime, Ordinacija ordinacija) throws Exception {
        Pacijent kriterijum = new Pacijent();
        kriterijum.setIme(ime);
        kriterijum.setPrezime(prezime);
        kriterijum.setOrdinacija(ordinacija);
        return vratiListuPacijenata(kriterijum);
    }

    @SuppressWarnings("unchecked")
    public List<Pacijent> vratiListuPacijenata(Pacijent kriterijum) throws Exception {
        return (List<Pacijent>) posalji(Operacija.VRATI_LISTU_PACIJENATA, kriterijum);
    }

    /**
     * Pronalazi pacijenta sa zadatim identifikatorom.
     *
     * @return pronadjeni pacijent ili null ako pacijenta sa tim
     *         identifikatorom nema
     */
    public Pacijent pretraziPacijenta(int idPacijent) throws Exception {
        Pacijent kriterijum = new Pacijent();
        kriterijum.setIdPacijent(idPacijent);
        return (Pacijent) posalji(Operacija.PRETRAZI_PACIJENT, kriterijum);
    }

    public void ubaciPacijenta(Pacijent pacijent) throws Exception {
        posalji(Operacija.UBACI_PACIJENT, pacijent);
    }

    public void promeniPacijenta(Pacijent pacijent) throws Exception {
        posalji(Operacija.PROMENI_PACIJENT, pacijent);
    }

    public void obrisiPacijenta(Pacijent pacijent) throws Exception {
        posalji(Operacija.OBRISI_PACIJENT, pacijent);
    }

    @SuppressWarnings("unchecked")
    public List<Ordinacija> vratiListuOrdinacija() throws Exception {
        return (List<Ordinacija>) posalji(Operacija.VRATI_LISTU_ORDINACIJA, null);
    }

    @SuppressWarnings("unchecked")
    public List<Usluga> vratiListuUsluga() throws Exception {
        return (List<Usluga>) posalji(Operacija.VRATI_LISTU_USLUGA, null);
    }

    @SuppressWarnings("unchecked")
    public List<Stomatolog> vratiListuStomatologa() throws Exception {
        return (List<Stomatolog>) posalji(Operacija.VRATI_LISTU_STOMATOLOGA, null);
    }

    /**
     * Pronalazi termin sa zadatim identifikatorom.
     */
    public Termin pretraziTermin(int idTermin) throws Exception {
        Termin kriterijum = new Termin();
        kriterijum.setIdTermin(idTermin);
        return (Termin) posalji(Operacija.PRETRAZI_TERMIN, kriterijum);
    }

    public void ubaciTermin(Termin termin) throws Exception {
        posalji(Operacija.UBACI_TERMIN, termin);
    }

    public void promeniTermin(Termin termin) throws Exception {
        posalji(Operacija.PROMENI_TERMIN, termin);
    }

    public void obrisiTermin(Termin termin) throws Exception {
        posalji(Operacija.OBRISI_TERMIN, termin);
    }

    /**
     * Salje zahtev serveru i vraca sadrzaj odgovora.
     *
     * @throws Exception ako server nije dostupan, ako je komunikacija pukla ili
     *                   ako je operacija na serveru zavrsila greskom
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

        if (odgovor.getTip() == TipOdgovora.GRESKA) {
            Exception izuzetak = odgovor.getIzuzetak();
            throw izuzetak != null ? izuzetak : new Exception("Operacija nije uspela.");
        }

        return odgovor.getOdgovor();
    }
}
