package controller;

import java.util.List;
import model.Ordinacija;
import model.Pacijent;
import model.Specijalizacija;
import model.Stomatolog;
import model.Termin;
import model.Usluga;
import so.ordinacija.VratiListuOrdinacijaSO;
import so.pacijent.ObrisiPacijentSO;
import so.pacijent.PromeniPacijentSO;
import so.pacijent.UbaciPacijentSO;
import so.pacijent.VratiListuPacijenataSO;
import so.prijava.PrijavaStomatologaSO;
import so.stomatolog.VratiListuStomatologaSO;
import so.stomatolog.VratiSpecijalizacijeStomatologaSO;
import so.termin.ObrisiTerminSO;
import so.termin.PretraziTerminSO;
import so.termin.PromeniTerminSO;
import so.termin.UbaciTerminSO;
import so.termin.VratiListuTerminaSO;
import so.usluga.VratiListuUslugaSO;

/**
 * Serverski kontroler - jedini sloj koji obrada zahteva poznaje.
 *
 * Kontroler ne sadrzi poslovnu logiku: za svaki zahtev pravi odgovarajucu
 * sistemsku operaciju, pokrece je i vraca njen rezultat. Sva provera preduslova
 * i rad sa bazom nalaze se u klasama paketa {@code so}.
 *
 * Metode su sinhronizovane zato sto server otvara nit po klijentu, a sve niti
 * dele jednu konekciju ka bazi sa iskljucenim automatskim potvrdjivanjem
 * transakcije. Sinhronizacija sprecava da se transakcije dve niti isprepletu.
 *
 * @author vukla
 */
public class Kontroler {

    private static Kontroler instanca;

    private Kontroler() {
    }

    public static synchronized Kontroler getInstanca() {
        if (instanca == null) {
            instanca = new Kontroler();
        }
        return instanca;
    }

    public synchronized Stomatolog prijaviStomatologa(String korisnickoIme, String sifra) throws Exception {
        PrijavaStomatologaSO so = new PrijavaStomatologaSO();
        so.izvrsiOperaciju(new Object[]{korisnickoIme, sifra});
        return so.getUlogovani();
    }

    public synchronized List<Termin> vratiListuTermina(Termin kriterijum) throws Exception {
        VratiListuTerminaSO so = new VratiListuTerminaSO();
        so.izvrsiOperaciju(kriterijum);
        return so.getLista();
    }

    public synchronized Termin pretraziTermin(Termin kriterijum) throws Exception {
        PretraziTerminSO so = new PretraziTerminSO();
        so.izvrsiOperaciju(kriterijum);
        return so.getPronadjeni();
    }

    public synchronized Termin ubaciTermin(Termin termin) throws Exception {
        UbaciTerminSO so = new UbaciTerminSO();
        so.izvrsiOperaciju(termin);
        return so.getZakazani();
    }

    public synchronized Termin promeniTermin(Termin termin) throws Exception {
        PromeniTerminSO so = new PromeniTerminSO();
        so.izvrsiOperaciju(termin);
        return so.getIzmenjeni();
    }

    public synchronized void obrisiTermin(Termin termin) throws Exception {
        ObrisiTerminSO so = new ObrisiTerminSO();
        so.izvrsiOperaciju(termin);
    }

    public synchronized List<Stomatolog> vratiListuStomatologa() throws Exception {
        VratiListuStomatologaSO so = new VratiListuStomatologaSO();
        so.izvrsiOperaciju(null);
        return so.getLista();
    }

    public synchronized List<Specijalizacija> vratiSpecijalizacijeStomatologa(Stomatolog stomatolog) throws Exception {
        VratiSpecijalizacijeStomatologaSO so = new VratiSpecijalizacijeStomatologaSO();
        so.izvrsiOperaciju(stomatolog);
        return so.getLista();
    }

    public synchronized List<Pacijent> vratiListuPacijenata() throws Exception {
        return vratiListuPacijenata(null);
    }

    public synchronized List<Pacijent> vratiListuPacijenata(Pacijent kriterijum) throws Exception {
        VratiListuPacijenataSO so = new VratiListuPacijenataSO();
        so.izvrsiOperaciju(kriterijum);
        return so.getLista();
    }

    public synchronized Pacijent ubaciPacijenta(Pacijent pacijent) throws Exception {
        UbaciPacijentSO so = new UbaciPacijentSO();
        so.izvrsiOperaciju(pacijent);
        return so.getZapamceni();
    }

    public synchronized Pacijent promeniPacijenta(Pacijent pacijent) throws Exception {
        PromeniPacijentSO so = new PromeniPacijentSO();
        so.izvrsiOperaciju(pacijent);
        return so.getIzmenjeni();
    }

    public synchronized void obrisiPacijenta(Pacijent pacijent) throws Exception {
        ObrisiPacijentSO so = new ObrisiPacijentSO();
        so.izvrsiOperaciju(pacijent);
    }

    public synchronized List<Ordinacija> vratiListuOrdinacija() throws Exception {
        VratiListuOrdinacijaSO so = new VratiListuOrdinacijaSO();
        so.izvrsiOperaciju(null);
        return so.getLista();
    }

    public synchronized List<Usluga> vratiListuUsluga() throws Exception {
        VratiListuUslugaSO so = new VratiListuUslugaSO();
        so.izvrsiOperaciju(null);
        return so.getLista();
    }
}
