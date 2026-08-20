package so.termin;

import model.StatusTermina;
import model.StavkaTermina;
import model.Termin;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija izmene postojeceg termina.
 *
 * Stavke se brisu i ponovo ubacuju, jer je stavka slaba celina koja postoji samo
 * uz svoj termin.
 *
 * @author vukla
 */
public class PromeniTerminSO extends OpstaSistemskaOperacija {

    private Termin izmenjeni;

    public Termin getIzmenjeni() {
        return izmenjeni;
    }

    @Override
    protected void preduslovi(Object objekat) throws Exception {
        if (objekat == null || !(objekat instanceof Termin)) {
            throw new Exception("Nije prosledjen parametar odgovarajuceg tipa.");
        }

        Termin termin = (Termin) objekat;
        if (termin.getIdTermin() <= 0) {
            throw new Exception("Nije prosledjen identifikator termina.");
        }
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

        String uslovPostoji = Termin.SPOJEVI + " WHERE termin.idTermin = " + termin.getIdTermin();
        if (broker.vratiObjekat(new Termin(), uslovPostoji) == null) {
            throw new Exception("Termin koji se menja ne postoji u bazi.");
        }

        // poslovno pravilo: isti stomatolog ne moze da ima dva termina koja nisu
        // otkazana u istom danu i u isto vreme - sam termin se ne racuna
        String uslovZauzetost = Termin.SPOJEVI
                + " WHERE termin.idStomatolog = " + termin.getStomatolog().getIdStomatolog()
                + " AND termin.datum = '" + termin.getDatum() + "'"
                + " AND termin.vreme = '" + termin.getVreme() + "'"
                + " AND termin.status <> '" + StatusTermina.OTKAZAN.name() + "'"
                + " AND termin.idTermin <> " + termin.getIdTermin();

        if (!broker.vratiPoUpitu(new Termin(), uslovZauzetost).isEmpty()) {
            throw new Exception("Stomatolog vec ima zakazan termin "
                    + termin.getDatum() + " u " + termin.getVreme() + ".");
        }
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        Termin termin = (Termin) objekat;

        broker.izmeni(termin);
        broker.obrisiPoUpitu(new StavkaTermina(), "idTermin = " + termin.getIdTermin());

        int redniBroj = 1;
        for (StavkaTermina stavka : termin.getStavke()) {
            stavka.setTermin(termin);
            stavka.setRb(redniBroj++);
            broker.dodaj(stavka);
        }

        izmenjeni = termin;
    }
}
