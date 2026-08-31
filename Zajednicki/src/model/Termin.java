/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import util.SqlUtil;

/**
 *
 * @author vukla
 */
public class Termin implements ApstraktniDomenskiObjekat {

    /**
     * Spajanja koja upit mora da sadrzi da bi metoda vratiListu mogla da napuni
     * i stomatologa i pacijenta zajedno sa njegovom ordinacijom. Sistemske
     * operacije ovaj deo upita prosledjuju brokeru kao deo uslova.
     */
    public static final String SPOJEVI =
            " JOIN stomatolog ON termin.idStomatolog = stomatolog.idStomatolog"
            + " JOIN pacijent ON termin.idPacijent = pacijent.idPacijent"
            + " JOIN ordinacija ON pacijent.idOrdinacija = ordinacija.idOrdinacija";

    private int idTermin;
    private LocalDate datum;
    private LocalTime vreme;
    private StatusTermina status;
    private String napomena;
    private Stomatolog stomatolog;
    private Pacijent pacijent;

    private List<StavkaTermina> stavke = new ArrayList<>();

    public Termin() {
    }

    public Termin(int idTermin, LocalDate datum, LocalTime vreme, StatusTermina status, String napomena,
            Stomatolog stomatolog, Pacijent pacijent) {
        this.idTermin = idTermin;
        this.datum = datum;
        this.vreme = vreme;
        this.status = status;
        this.napomena = napomena;
        this.stomatolog = stomatolog;
        this.pacijent = pacijent;
        this.stavke = new ArrayList<>();
    }

    public int getIdTermin() {
        return idTermin;
    }

    public void setIdTermin(int idTermin) {
        this.idTermin = idTermin;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public LocalTime getVreme() {
        return vreme;
    }

    public void setVreme(LocalTime vreme) {
        this.vreme = vreme;
    }

    public StatusTermina getStatus() {
        return status;
    }

    public void setStatus(StatusTermina status) {
        this.status = status;
    }

    public String getNapomena() {
        return napomena;
    }

    public void setNapomena(String napomena) {
        this.napomena = napomena;
    }

    public Stomatolog getStomatolog() {
        return stomatolog;
    }

    public void setStomatolog(Stomatolog stomatolog) {
        this.stomatolog = stomatolog;
    }

    public Pacijent getPacijent() {
        return pacijent;
    }

    public void setPacijent(Pacijent pacijent) {
        this.pacijent = pacijent;
    }

    public List<StavkaTermina> getStavke() {
        return stavke;
    }

    public void setStavke(List<StavkaTermina> stavke) {
        this.stavke = stavke;
    }

    @Override
    public String vratiNazivTabele() {
        return "termin";
    }

    @Override
    public List<ApstraktniDomenskiObjekat> vratiListu(ResultSet rs) throws Exception {
        List<ApstraktniDomenskiObjekat> lista = new ArrayList<>();
        while (rs.next()) {
            int idTermin = rs.getInt("termin.idTermin");

            java.sql.Date sqlDatum = rs.getDate("termin.datum");
            LocalDate datum = sqlDatum == null ? null : sqlDatum.toLocalDate();

            java.sql.Time sqlVreme = rs.getTime("termin.vreme");
            LocalTime vreme = sqlVreme == null ? null : sqlVreme.toLocalTime();

            String nazivStatusa = rs.getString("termin.status");
            StatusTermina status = nazivStatusa == null ? null : StatusTermina.valueOf(nazivStatusa);

            String napomena = rs.getString("termin.napomena");

            Stomatolog stomatolog = new Stomatolog(
                    rs.getInt("stomatolog.idStomatolog"),
                    rs.getString("stomatolog.ime"),
                    rs.getString("stomatolog.prezime"),
                    rs.getString("stomatolog.email"),
                    rs.getString("stomatolog.korisnickoIme"),
                    rs.getString("stomatolog.sifra"),
                    rs.getString("stomatolog.brojLicence"));

            Ordinacija ordinacija = new Ordinacija(
                    rs.getInt("ordinacija.idOrdinacija"),
                    rs.getString("ordinacija.naziv"),
                    rs.getString("ordinacija.adresa"));

            Pacijent pacijent = new Pacijent(
                    rs.getInt("pacijent.idPacijent"),
                    rs.getString("pacijent.ime"),
                    rs.getString("pacijent.prezime"),
                    rs.getString("pacijent.brojTelefona"),
                    rs.getString("pacijent.brojKnjizice"),
                    ordinacija);

            Termin t = new Termin(idTermin, datum, vreme, status, napomena, stomatolog, pacijent);
            lista.add(t);
        }
        return lista;
    }

    @Override
    public String vratiKoloneZaUbacivanje() {
        return "datum, vreme, status, napomena, idStomatolog, idPacijent";
    }

    @Override
    public String vratiVrednostiZaUbacivanje() {
        return "'" + datum + "','" + vreme + "','" + status.name() + "','"
                + SqlUtil.escapiraj(napomena) + "',"
                + stomatolog.getIdStomatolog() + "," + pacijent.getIdPacijent();
    }

    @Override
    public String vratiPrimarniKljuc() {
        return "idTermin=" + idTermin;
    }

    /**
     * Vraca prvi objekat iz result set-a, odnosno null ako upit nije vratio
     * nijedan slog. Koristi se za upite koji vracaju najvise jedan red.
     */
    @Override
    public ApstraktniDomenskiObjekat vratiObjekatRS(ResultSet rs) throws Exception {
        List<ApstraktniDomenskiObjekat> lista = vratiListu(rs);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public String vratiVrednostiZaIzmenu() {
        return "datum='" + datum + "', vreme='" + vreme + "', status='" + status.name()
                + "', napomena='" + SqlUtil.escapiraj(napomena)
                + "', idStomatolog=" + stomatolog.getIdStomatolog() + ", idPacijent=" + pacijent.getIdPacijent();
    }

}
