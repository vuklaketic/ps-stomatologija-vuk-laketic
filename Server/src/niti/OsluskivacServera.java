package niti;

/**
 * Osluskivac dogadjaja na serveru - serverske niti njime javljaju sta se
 * desava (pokretanje, povezivanje, prekid) bez zavisnosti od UI-ja. Implementira
 * ga serverska forma, koja poruke ispisuje u svom statusnom prozoru.
 *
 * @author vukla
 */
public interface OsluskivacServera {

    /**
     * Belezi jednu poruku o dogadjaju na serveru.
     *
     * @param poruka tekst poruke
     */
    void zabelezi(String poruka);
}
