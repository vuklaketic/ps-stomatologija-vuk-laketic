package niti;

/**
 * Osluskivac dogadjaja na serveru.
 *
 * Sluzi da serverske niti javljaju sta se desava (pokretanje, povezivanje
 * klijenta, prekid veze) bez direktne zavisnosti od korisnickog interfejsa.
 * Implementira ga serverska forma i ispisuje poruke u svom statusnom prozoru.
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
