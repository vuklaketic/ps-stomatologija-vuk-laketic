package so.termin;

import java.util.ArrayList;
import java.util.List;
import model.ApstraktniDomenskiObjekat;
import model.StavkaTermina;
import model.Termin;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija koja pronalazi jedan termin po identifikatoru.
 *
 * @author vukla
 */
public class PretraziTerminSO extends OpstaSistemskaOperacija {

    private Termin pronadjeni;

    public Termin getPronadjeni() {
        return pronadjeni;
    }

    @Override
    protected void preduslovi(Object objekat) throws Exception {
        if (objekat == null || !(objekat instanceof Termin)) {
            throw new Exception("Nije prosledjen parametar odgovarajuceg tipa.");
        }
        if (((Termin) objekat).getIdTermin() <= 0) {
            throw new Exception("Nije prosledjen identifikator termina.");
        }
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        Termin kriterijum = (Termin) objekat;

        String uslov = Termin.SPOJEVI + " WHERE termin.idTermin = " + kriterijum.getIdTermin();
        pronadjeni = (Termin) broker.vratiObjekat(new Termin(), uslov);

        if (pronadjeni == null) {
            return;
        }

        String uslovStavke = StavkaTermina.SPOJEVI
                + " WHERE stavkatermina.idTermin = " + pronadjeni.getIdTermin()
                + " ORDER BY stavkatermina.rb";

        List<StavkaTermina> stavke = new ArrayList<>();
        for (ApstraktniDomenskiObjekat ado : broker.vratiPoUpitu(new StavkaTermina(), uslovStavke)) {
            StavkaTermina stavka = (StavkaTermina) ado;
            stavka.setTermin(pronadjeni);
            stavke.add(stavka);
        }
        pronadjeni.setStavke(stavke);
    }
}
