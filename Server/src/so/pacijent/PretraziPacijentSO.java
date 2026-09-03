package so.pacijent;

import model.Pacijent;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija koja pronalazi jednog pacijenta po identifikatoru.
 * Scenariji nad pacijentom razlikuju listu po kriterijumima od trazenja
 * jednog, izabranog pacijenta - ovu drugu radnju obavlja ova operacija.
 *
 * @author vukla
 */
public class PretraziPacijentSO extends OpstaSistemskaOperacija {

    private Pacijent pronadjeni;

    public Pacijent getPronadjeni() {
        return pronadjeni;
    }

    @Override
    protected void preduslovi(Object objekat) throws Exception {
        if (objekat == null || !(objekat instanceof Pacijent)) {
            throw new Exception("Nije prosleđen parametar odgovarajućeg tipa.");
        }
        if (((Pacijent) objekat).getIdPacijent() <= 0) {
            throw new Exception("Nije prosleđen identifikator pacijenta.");
        }
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        Pacijent kriterijum = (Pacijent) objekat;

        String uslov = Pacijent.SPOJEVI + " WHERE pacijent.idPacijent = " + kriterijum.getIdPacijent();
        pronadjeni = (Pacijent) broker.vratiObjekat(new Pacijent(), uslov);
    }

    @Override
    protected String porukaONeuspehu() {
        return "Sistem ne može da nađe pacijenta.";
    }
}
