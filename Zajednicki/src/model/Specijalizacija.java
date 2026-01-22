/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author vukla
 */
public class Specijalizacija {
    private int idSpecijalizacija;
    private String naziv;

    public Specijalizacija() {
    }

    public Specijalizacija(int idSpecijalizacija, String naziv) {
        this.idSpecijalizacija = idSpecijalizacija;
        this.naziv = naziv;
    }

    public int getIdSpecijalizacija() {
        return idSpecijalizacija;
    }

    public void setIdSpecijalizacija(int idSpecijalizacija) {
        this.idSpecijalizacija = idSpecijalizacija;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }
    
    
}
