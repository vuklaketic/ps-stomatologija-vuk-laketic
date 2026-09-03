/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package komunikacija;

import java.io.Serializable;

/**
 * Odgovor servera na jedan zahtev klijenta - pored rezultata nosi tip
 * odgovora i izuzetak nastao na serveru, pa klijent prikazuje konkretnu
 * poruku greske.
 *
 * @author vukla
 */
public class Odgovor implements Serializable {

    private TipOdgovora tip;
    private Object odgovor;
    private Exception izuzetak;

    public Odgovor() {
    }

    public Odgovor(TipOdgovora tip, Object odgovor, Exception izuzetak) {
        this.tip = tip;
        this.odgovor = odgovor;
        this.izuzetak = izuzetak;
    }

    public static Odgovor uspeh(Object odgovor) {
        return new Odgovor(TipOdgovora.USPEH, odgovor, null);
    }

    public static Odgovor greska(Exception izuzetak) {
        return new Odgovor(TipOdgovora.GRESKA, null, izuzetak);
    }

    public TipOdgovora getTip() {
        return tip;
    }

    public void setTip(TipOdgovora tip) {
        this.tip = tip;
    }

    public Object getOdgovor() {
        return odgovor;
    }

    public void setOdgovor(Object odgovor) {
        this.odgovor = odgovor;
    }

    public Exception getIzuzetak() {
        return izuzetak;
    }

    public void setIzuzetak(Exception izuzetak) {
        this.izuzetak = izuzetak;
    }
}
