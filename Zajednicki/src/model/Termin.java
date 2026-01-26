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

/**
 *
 * @author vukla
 */
public class Termin implements ApstraktniDomenskiObjekat {

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
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String vratiKoloneZaUbacivanje() {
        return "datum, vreme, status, napomena, idStomatolog, idPacijent";
    }

    @Override
    public String vratiVrednostiZaUbacivanje() {
        return "'" + datum + "','" + vreme + "','" + status.name() + "','" + napomena + "',"
                + stomatolog.getIdStomatolog() + "," + pacijent.getIdPacijent();
    }

    @Override
    public String vratiPrimarniKljuc() {
        return "idTermin=" + idTermin;
    }

    @Override
    public ApstraktniDomenskiObjekat vratiObjekatRS(ResultSet rs) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String vratiVrednostiZaIzmenu() {
        return "datum='" + datum + "', vreme='" + vreme + "', status='" + status.name() + "', napomena='" + napomena
                + "', idStomatolog=" + stomatolog.getIdStomatolog() + ", idPacijent=" + pacijent.getIdPacijent();
    }

}
