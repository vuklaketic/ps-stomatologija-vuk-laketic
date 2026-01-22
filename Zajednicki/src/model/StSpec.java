/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;

/**
 *
 * @author vukla
 */
public class StSpec {

    private LocalDate datumSticanja;
    private Stomatolog stomatolog;
    private Specijalizacija specijalizacija;

    public StSpec() {
    }

    public StSpec(LocalDate datumSticanja, Stomatolog stomatolog, Specijalizacija specijalizacija) {
        this.datumSticanja = datumSticanja;
        this.stomatolog = stomatolog;
        this.specijalizacija = specijalizacija;
    }

    public LocalDate getDatumSticanja() {
        return datumSticanja;
    }

    public void setDatumSticanja(LocalDate datumSticanja) {
        this.datumSticanja = datumSticanja;
    }

    public Stomatolog getStomatolog() {
        return stomatolog;
    }

    public void setStomatolog(Stomatolog stomatolog) {
        this.stomatolog = stomatolog;
    }

    public Specijalizacija getSpecijalizacija() {
        return specijalizacija;
    }

    public void setSpecijalizacija(Specijalizacija specijalizacija) {
        this.specijalizacija = specijalizacija;
    }
    
    
}
