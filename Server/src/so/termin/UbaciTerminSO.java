package so.termin;

import model.StatusTermina;
import model.StavkaTermina;
import model.Termin;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija zakazivanja novog termina zajedno sa njegovim stavkama.
 *
 * @author vukla
 */
public class UbaciTerminSO extends OpstaSistemskaOperacija {
    
    private Termin zakazani;
    public Termin getZakazani() {
        return zakazani;
    }
    @Override
    protected void preduslovi(Object objekat) throws Exception {
        if (objekat == null || !(objekat instanceof Termin)) {
            throw new Exception("Nije prosleđen parametar odgovarajućeg tipa.");
        }

        Termin termin = (Termin) objekat;
        if (termin.getDatum() == null || termin.getVreme() == null) {
            throw new Exception("Termin mora imati datum i vreme.");
        }
        if (termin.getStomatolog() == null || termin.getStomatolog().getIdStomatolog() <= 0) {
            throw new Exception("Termin mora imati stomatologa.");
        }
        if (termin.getPacijent() == null || termin.getPacijent().getIdPacijent() <= 0) {
            throw new Exception("Termin mora imati pacijenta.");
        }
        if (termin.getStavke() == null || termin.getStavke().isEmpty()) {
            throw new Exception("Termin mora imati bar jednu uslugu.");
        }
        if (termin.getStatus() == null) {
            termin.setStatus(StatusTermina.ZAKAZAN);
        }
        if (termin.getNapomena() == null) {
            termin.setNapomena("");
        }

        String uslov = Termin.SPOJEVI
                + " WHERE termin.idStomatolog = " + termin.getStomatolog().getIdStomatolog()
                + " AND termin.datum = '" + termin.getDatum() + "'"
                + " AND termin.vreme = '" + termin.getVreme() + "'"
                + " AND termin.status <> '" + StatusTermina.OTKAZAN.name() + "'";

        if (!broker.vratiPoUpitu(new Termin(), uslov).isEmpty()) {
            throw new Exception("Stomatolog već ima zakazan termin "
                    + termin.getDatum() + " u " + termin.getVreme() + ".");
        }
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        Termin termin = (Termin) objekat;

        int idTermin = broker.dodaj(termin);
        termin.setIdTermin(idTermin);

        // termin se tek upisuje, pa njegove stavke dobijaju redne brojeve od
        // jedan naviste, redosledom kojim su unete na formi
        int redniBroj = 1;
        for (StavkaTermina stavka : termin.getStavke()) {
            stavka.setTermin(termin);
            stavka.setRb(redniBroj++);
            broker.dodaj(stavka);
        }

        zakazani = termin;
    }

    @Override
    protected String porukaONeuspehu() {
        return "Sistem ne može da zapamti termin.";
    }
}
