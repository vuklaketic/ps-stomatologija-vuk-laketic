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
public class Ordinacija implements ApstraktniDomenskiObjekat {

    private int idOrdinacija;
    private String naziv;
    private String adresa;

    public Ordinacija() {
    }

    public Ordinacija(int idOrdinacija, String naziv, String adresa) {
        this.idOrdinacija = idOrdinacija;
        this.naziv = naziv;
        this.adresa = adresa;
    }

    public int getIdOrdinacija() {
        return idOrdinacija;
    }

    public void setIdOrdinacija(int idOrdinacija) {
        this.idOrdinacija = idOrdinacija;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    @Override
    public String toString() {
        return "Naziv=" + naziv + ", Adresa=" + adresa;
    }

    @Override
    public String vratiNazivTabele() {
        return "ordinacija";
    }

    @Override
    public List<ApstraktniDomenskiObjekat> vratiListu(ResultSet rs) throws Exception {
        List<ApstraktniDomenskiObjekat> lista = new ArrayList();
        while (rs.next()) {
            int idOrdinacija = rs.getInt("ordinacija.idOrdinacija");
            String naziv = rs.getString("ordinacija.naziv");
            String adresa = rs.getString("ordinacija.adresa");

            Ordinacija o = new Ordinacija(idOrdinacija, naziv, adresa);
            lista.add(o);
        }
        return lista;
    }

    @Override
    public String vratiKoloneZaUbacivanje() {
        return "naziv, adresa";
    }

    @Override
    public String vratiVrednostiZaUbacivanje() {
        return "'" + SqlUtil.escapiraj(naziv) + "','" + SqlUtil.escapiraj(adresa) + "'";
    }

    @Override
    public String vratiPrimarniKljuc() {
        return "idOrdinacija=" + idOrdinacija;
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
        return "naziv='" + SqlUtil.escapiraj(naziv) + "', adresa='" + SqlUtil.escapiraj(adresa) + "'";
    }

}
