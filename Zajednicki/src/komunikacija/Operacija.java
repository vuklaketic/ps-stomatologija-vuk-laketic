/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package komunikacija;

import java.io.Serializable;

/**
 *
 * @author vukla
 */
public enum Operacija implements Serializable {
    LOGIN,

    // prijava stomatologa na sistem
    PRIJAVA_STOMATOLOG,

    // operacije nad terminom
    KREIRAJ_TERMIN,
    UBACI_TERMIN,
    PROMENI_TERMIN,
    OBRISI_TERMIN,
    PRETRAZI_TERMIN,

    // operacije nad specijalizacijom
    UBACI_SPECIJALIZACIJA,

    // operacije nad pacijentom
    UBACI_PACIJENT,
    PROMENI_PACIJENT,
    OBRISI_PACIJENT,

    // ucitavanje lista
    VRATI_LISTU_TERMINA,
    VRATI_LISTU_STOMATOLOGA,
    VRATI_LISTU_PACIJENATA,
    VRATI_LISTU_USLUGA,
    VRATI_LISTU_ORDINACIJA,

    // specijalizacije jednog stomatologa
    VRATI_SPECIJALIZACIJE_STOMATOLOGA;
}
