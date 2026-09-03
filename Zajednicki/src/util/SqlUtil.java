package util;

/**
 * Pomocne metode za sastavljanje SQL upita. Domenske klase vrednosti za
 * INSERT/UPDATE sastavljaju nadovezivanjem stringova, pa svaka tekstualna
 * vrednost mora da se pripremi - inace apostrof puca upit ili omogucava
 * ubacivanje tudjeg SQL koda.
 *
 * @author vukla
 */
public class SqlUtil {

    private SqlUtil() {
    }

    /**
     * Priprema tekstualnu vrednost za ugradnju u SQL string literal.
     *
     * Apostrof se udvostrucava, sto je standardan SQL nacin, a obrnuta kosa
     * crta se takodje udvostrucava jer je MySQL podrazumevano tumaci kao
     * pocetak escape sekvence. Redosled zamena je bitan - prvo kosa crta, pa
     * apostrof, da se novododate kose crte ne bi ponovo obradjivale.
     *
     * @param vrednost tekst koji se ugradjuje u upit, moze biti null
     * @return pripremljen tekst, prazan string ako je prosledjen null
     */
    public static String escapiraj(String vrednost) {
        if (vrednost == null) {
            return "";
        }
        return vrednost.replace("\\", "\\\\").replace("'", "''");
    }
}
