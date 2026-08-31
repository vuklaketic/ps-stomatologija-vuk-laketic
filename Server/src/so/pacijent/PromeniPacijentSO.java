package so.pacijent;

import java.sql.SQLIntegrityConstraintViolationException;
import model.Pacijent;
import so.OpstaSistemskaOperacija;

/**
 * Sistemska operacija izmene postojeceg pacijenta.
 *
 * @author vukla
 */
public class PromeniPacijentSO extends OpstaSistemskaOperacija {

    private Pacijent izmenjeni;

    public Pacijent getIzmenjeni() {
        return izmenjeni;
    }

    @Override
    protected void preduslovi(Object objekat) throws Exception {
        if (objekat == null || !(objekat instanceof Pacijent)) {
            throw new Exception("Nije prosledjen parametar odgovarajuceg tipa.");
        }

        Pacijent pacijent = (Pacijent) objekat;
        if (pacijent.getIdPacijent() <= 0) {
            throw new Exception("Nije prosledjen identifikator pacijenta.");
        }
        if (pacijent.getIme() == null || pacijent.getIme().trim().isEmpty()) {
            throw new Exception("Pacijent mora imati ime.");
        }
        if (pacijent.getPrezime() == null || pacijent.getPrezime().trim().isEmpty()) {
            throw new Exception("Pacijent mora imati prezime.");
        }
        if (pacijent.getBrojTelefona() == null || pacijent.getBrojTelefona().trim().isEmpty()) {
            throw new Exception("Pacijent mora imati broj telefona.");
        }
        if (pacijent.getBrojKnjizice() == null
                || pacijent.getBrojKnjizice().trim().length() != UbaciPacijentSO.DUZINA_BROJA_KNJIZICE) {
            throw new Exception("Broj knjizice mora imati tacno "
                    + UbaciPacijentSO.DUZINA_BROJA_KNJIZICE + " znakova.");
        }
        if (pacijent.getOrdinacija() == null || pacijent.getOrdinacija().getIdOrdinacija() <= 0) {
            throw new Exception("Pacijent mora pripadati ordinaciji.");
        }

        String uslovPostoji = Pacijent.SPOJEVI + " WHERE pacijent.idPacijent = " + pacijent.getIdPacijent();
        if (broker.vratiObjekat(new Pacijent(), uslovPostoji) == null) {
            throw new Exception("Pacijent koji se menja ne postoji u bazi.");
        }
    }

    @Override
    protected void izvrsi(Object objekat) throws Exception {
        Pacijent pacijent = (Pacijent) objekat;

        try {
            broker.izmeni(pacijent);
        } catch (SQLIntegrityConstraintViolationException ex) {
            // broj knjizice je u bazi jedinstven
            throw new Exception("Drugi pacijent vec ima broj knjizice "
                    + pacijent.getBrojKnjizice() + ".");
        }

        izmenjeni = pacijent;
    }

    @Override
    protected String porukaONeuspehu() {
        return "Sistem ne moze da zapamti pacijenta.";
    }
}
