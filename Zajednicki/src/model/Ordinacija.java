/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author vukla
 */
public class Ordinacija {
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
    
}
