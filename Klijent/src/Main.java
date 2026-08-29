/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */

import forma.Izgled;
import forma.LoginForma;
import javax.swing.SwingUtilities;

/**
 * Ulazna tacka klijentske aplikacije.
 *
 * @author vukla
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Izgled.primeni();
                new LoginForma().setVisible(true);
            }
        });
    }
}
