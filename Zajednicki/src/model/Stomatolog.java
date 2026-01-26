/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author vukla
 */
public class Stomatolog implements ApstraktniDomenskiObjekat {

    private int idStomatolog;
    private String ime;
    private String prezime;
    private String email;
    private String korisnickoIme;
    private String sifra;
    private String brojLicence;

    public Stomatolog() {
    }

    public Stomatolog(int idStomatolog, String ime, String prezime, String email, String korisnickoIme, String sifra, String brojLicence) {
        this.idStomatolog = idStomatolog;
        this.ime = ime;
        this.prezime = prezime;
        this.email = email;
        this.korisnickoIme = korisnickoIme;
        this.sifra = sifra;
        this.brojLicence = brojLicence;
    }

    public int getIdStomatolog() {
        return idStomatolog;
    }

    public void setIdStomatolog(int idStomatolog) {
        this.idStomatolog = idStomatolog;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    public String getSifra() {
        return sifra;
    }

    public void setSifra(String sifra) {
        this.sifra = sifra;
    }

    public String getBrojLicence() {
        return brojLicence;
    }

    public void setBrojLicence(String brojLicence) {
        this.brojLicence = brojLicence;
    }

    @Override
    public String vratiNazivTabele() {
        return "stomatolog";
    }

    @Override
    public List<ApstraktniDomenskiObjekat> vratiListu(ResultSet rs) throws Exception {
        List<ApstraktniDomenskiObjekat> lista = new ArrayList();
        while (rs.next()) {
            int idStomatolog = rs.getInt("stomatolog.idStomatolog");
            String ime = rs.getString("stomatolog.ime");
            String prezime = rs.getString("stomatolog.prezime");
            String email = rs.getString("stomatolog.email");
            String korisnickoIme = rs.getString("stomatolog.korisnickoIme");
            String sifra = rs.getString("stomatolog.sifra");
            String brojLicence = rs.getString("stomatolog.brojLicence");

            Stomatolog s = new Stomatolog(idStomatolog, ime, prezime, email, korisnickoIme, sifra, brojLicence);
            lista.add(s);
        }
        return lista;
    }

    @Override
    public String vratiKoloneZaUbacivanje() {
        return "ime, prezime, email, korisnickoIme, sifra, brojLicence";
    }

    @Override
    public String vratiVrednostiZaUbacivanje() {
        return "'" + ime + "','" + prezime + "','" + email + "','" + korisnickoIme + "','" + sifra + "','" + brojLicence + "'";
    }

    @Override
    public String vratiPrimarniKljuc() {
        return "idStomatolog=" + idStomatolog;
    }

    @Override
    public ApstraktniDomenskiObjekat vratiObjekatRS(ResultSet rs) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String vratiVrednostiZaIzmenu() {
        return "ime='" + ime + "', prezime='" + prezime + "', email='" + email + "', korisnickoIme='" + korisnickoIme
                + "', sifra='" + sifra + "', brojLicence='" + brojLicence + "'";
    }

}
