package dao;

import baza.Konekcija;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.ApstraktniDomenskiObjekat;

/**
 * Genericki objekat za pristup bazi podataka (broker baze podataka).
 *
 * Klasa ne poznaje nijednu konkretnu domensku klasu - sve sto joj je potrebno
 * (naziv tabele, kolone, vrednosti, primarni kljuc, citanje result set-a)
 * dobija preko interfejsa {@link ApstraktniDomenskiObjekat}. Zahvaljujuci tome
 * je dovoljan jedan broker za ceo sistem.
 *
 * Uslov koji se prosledjuje metodama za citanje dodaje se na upit u izvornom
 * obliku, pa moze da sadrzi i spajanja i WHERE klauzu (npr.
 * {@code Termin.SPOJEVI + " WHERE termin.idStomatolog = 3"}). Uslove sastavljaju
 * sistemske operacije.
 *
 * Transakcijom upravlja {@code OpstaSistemskaOperacija}, jer se jedna sistemska
 * operacija cesto sastoji od vise poziva ovog brokera.
 *
 * @author vukla
 */
public class GenericDAO {

    private static final Logger logger = Logger.getLogger(GenericDAO.class.getName());

    /**
     * Ubacuje objekat u bazu i vraca generisani primarni kljuc.
     *
     * @param ado domenski objekat koji se ubacuje
     * @return generisani kljuc ili 0 ako tabela nema autoinkrementiranu kolonu
     * @throws Exception ako ubacivanje ne uspe
     */
    public int dodaj(ApstraktniDomenskiObjekat ado) throws Exception {
        String upit = "INSERT INTO " + ado.vratiNazivTabele()
                + " (" + ado.vratiKoloneZaUbacivanje() + ")"
                + " VALUES (" + ado.vratiVrednostiZaUbacivanje() + ")";
        logger.log(Level.INFO, upit);

        try (PreparedStatement ps = Konekcija.getInstanca().getKonekcija()
                .prepareStatement(upit, Statement.RETURN_GENERATED_KEYS)) {
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Menja postojeci objekat u bazi, pronalazi ga po primarnom kljucu.
     *
     * @return broj izmenjenih slogova
     */
    public int izmeni(ApstraktniDomenskiObjekat ado) throws Exception {
        String upit = "UPDATE " + ado.vratiNazivTabele()
                + " SET " + ado.vratiVrednostiZaIzmenu()
                + " WHERE " + ado.vratiPrimarniKljuc();
        logger.log(Level.INFO, upit);

        try (PreparedStatement ps = Konekcija.getInstanca().getKonekcija().prepareStatement(upit)) {
            return ps.executeUpdate();
        }
    }

    /**
     * Brise objekat iz baze po primarnom kljucu.
     *
     * @return broj obrisanih slogova
     */
    public int obrisi(ApstraktniDomenskiObjekat ado) throws Exception {
        return obrisiPoUpitu(ado, ado.vratiPrimarniKljuc());
    }

    /**
     * Brise iz tabele sve slogove koji zadovoljavaju zadati uslov.
     *
     * @param ado domenski objekat koji odredjuje tabelu
     * @param uslov WHERE klauza bez kljucne reci WHERE
     * @return broj obrisanih slogova
     */
    public int obrisiPoUpitu(ApstraktniDomenskiObjekat ado, String uslov) throws Exception {
        String upit = "DELETE FROM " + ado.vratiNazivTabele() + " WHERE " + uslov;
        logger.log(Level.INFO, upit);

        try (PreparedStatement ps = Konekcija.getInstanca().getKonekcija().prepareStatement(upit)) {
            return ps.executeUpdate();
        }
    }

    /**
     * Vraca sve slogove iz tabele zadatog domenskog objekta.
     */
    public List<ApstraktniDomenskiObjekat> vratiSve(ApstraktniDomenskiObjekat ado) throws Exception {
        return vratiPoUpitu(ado, null);
    }

    /**
     * Vraca slogove koji zadovoljavaju zadati uslov.
     *
     * @param ado domenski objekat koji odredjuje tabelu i nacin citanja slogova
     * @param uslov deo upita koji se dodaje posle naziva tabele - moze da sadrzi
     *              spajanja i WHERE klauzu, ili null ako se citaju svi slogovi
     */
    public List<ApstraktniDomenskiObjekat> vratiPoUpitu(ApstraktniDomenskiObjekat ado, String uslov)
            throws Exception {
        try (PreparedStatement ps = pripremiUpit(ado, uslov);
                ResultSet rs = ps.executeQuery()) {
            return ado.vratiListu(rs);
        }
    }

    /**
     * Vraca prvi slog koji zadovoljava zadati uslov ili null ako takvog nema.
     */
    public ApstraktniDomenskiObjekat vratiObjekat(ApstraktniDomenskiObjekat ado, String uslov)
            throws Exception {
        try (PreparedStatement ps = pripremiUpit(ado, uslov);
                ResultSet rs = ps.executeQuery()) {
            return ado.vratiObjekatRS(rs);
        }
    }

    private PreparedStatement pripremiUpit(ApstraktniDomenskiObjekat ado, String uslov) throws Exception {
        String upit = "SELECT * FROM " + ado.vratiNazivTabele();
        if (uslov != null) {
            upit += uslov;
        }
        logger.log(Level.INFO, upit);
        return Konekcija.getInstanca().getKonekcija().prepareStatement(upit);
    }
}
