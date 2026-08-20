/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package komunikacija;

import java.io.Serializable;

/**
 * Odgovor servera na jedan zahtev klijenta.
 *
 * Pored samog rezultata nosi i tip odgovora, kao i izuzetak koji je nastao na
 * serveru. Zahvaljujuci tome klijent moze da prikaze konkretnu poruku greske
 * umesto uopstene, jer poruka stize sa mesta na kome je greska i nastala.
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

    /**
     * Pravi odgovor koji oznacava uspesno izvrsenu operaciju.
     */
    public static Odgovor uspeh(Object odgovor) {
        return new Odgovor(TipOdgovora.USPEH, odgovor, null);
    }

    /**
     * Pravi odgovor koji oznacava da operacija nije uspela.
     */
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
