/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import util.SqlUtil;

/**
 *
 * @author vukla
 */
public class Usluga implements ApstraktniDomenskiObjekat {

    private int idUsluga;
    private String naziv;
    private double cena;
    private int trajanje; // u minutima

    public Usluga() {
    }

    public Usluga(int idUsluga, String naziv, double cena, int trajanje) {
        this.idUsluga = idUsluga;
        this.naziv = naziv;
        this.cena = cena;
        this.trajanje = trajanje;
    }

    public int getIdUsluga() {
        return idUsluga;
    }

    public void setIdUsluga(int idUsluga) {
        this.idUsluga = idUsluga;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public double getCena() {
        return cena;
    }

    public void setCena(double cena) {
        this.cena = cena;
    }

    public int getTrajanje() {
        return trajanje;
    }

    public void setTrajanje(int trajanje) {
        this.trajanje = trajanje;
    }

    @Override
    public String vratiNazivTabele() {
        return "usluga";
    }

    @Override
    public List<ApstraktniDomenskiObjekat> vratiListu(ResultSet rs) throws Exception {
        List<ApstraktniDomenskiObjekat> lista = new ArrayList();
        while (rs.next()) {
            int idUsluga = rs.getInt("usluga.idUsluga");
            String naziv = rs.getString("usluga.naziv");
            double cena = rs.getDouble("usluga.cena");
            int trajanje = rs.getInt("usluga.trajanje");

            Usluga u = new Usluga(idUsluga, naziv, cena, trajanje);
            lista.add(u);
        }
        return lista;
    }

    @Override
    public String vratiKoloneZaUbacivanje() {
        return "naziv, cena, trajanje";
    }

    @Override
    public String vratiVrednostiZaUbacivanje() {
        return "'" + SqlUtil.escapiraj(naziv) + "'," + cena + "," + trajanje;
    }

    @Override
    public String vratiPrimarniKljuc() {
        return "idUsluga=" + idUsluga;
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
        return "naziv='" + SqlUtil.escapiraj(naziv) + "', cena=" + cena + ", trajanje=" + trajanje;
    }

}
