/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author vukla
 */
public class Pacijent {
    private int idPacijent;
    private String ime;
    private String prezime;
    private String brojTelefona;
    private String brojKnjizice;
    private Ordinacija ordinacija;

    public Pacijent() {
    }

    public Pacijent(int idPacijent, String ime, String prezime, String brojTelefona,
                    String brojKnjizice, Ordinacija ordinacija) {
        this.idPacijent = idPacijent;
        this.ime = ime;
        this.prezime = prezime;
        this.brojTelefona = brojTelefona;
        this.brojKnjizice = brojKnjizice;
        this.ordinacija = ordinacija;
    }

    public int getIdPacijent() {
        return idPacijent;
    }

    public void setIdPacijent(int idPacijent) {
        this.idPacijent = idPacijent;
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

    public String getBrojTelefona() {
        return brojTelefona;
    }

    public void setBrojTelefona(String brojTelefona) {
        this.brojTelefona = brojTelefona;
    }

    public String getBrojKnjizice() {
        return brojKnjizice;
    }

    public void setBrojKnjizice(String brojKnjizice) {
        this.brojKnjizice = brojKnjizice;
    }

    public Ordinacija getOrdinacija() {
        return ordinacija;
    }

    public void setOrdinacija(Ordinacija ordinacija) {
        this.ordinacija = ordinacija;
    }
    
}
