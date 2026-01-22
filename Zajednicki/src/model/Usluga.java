/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author vukla
 */
public class Usluga {
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
    
    
}
