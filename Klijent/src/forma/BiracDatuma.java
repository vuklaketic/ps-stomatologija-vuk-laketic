package forma;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Pravi birac datuma sa padajucim kalendarom.
 *
 * Datum se bira iskljucivo klikom u kalendaru - rucno kucanje je iskljuceno,
 * pa u polje ne moze da dospe neispravan datum i nema potrebe za proverom
 * formata. Kalendar se otvara i klikom na samo polje, ne samo na dugme pored
 * njega.
 *
 * @author vukla
 */
class BiracDatuma {

    /** Datum se prikazuje u istom obliku u kome se cuva u bazi. */
    private static final String FORMAT_DATUMA = "yyyy-MM-dd";

    private BiracDatuma() {
    }

    /**
     * @param dozvoliPrazno true kada prazno polje ima znacenje, na primer u
     * pretrazi gde znaci da se po datumu ne filtrira
     */
    static DatePicker napravi(boolean dozvoliPrazno) {
        DatePickerSettings podesavanja = new DatePickerSettings();
        podesavanja.setFormatForDatesCommonEra(FORMAT_DATUMA);
        podesavanja.setAllowKeyboardEditing(false);
        podesavanja.setAllowEmptyDates(dozvoliPrazno);

        DatePicker birac = new DatePicker(podesavanja);
        birac.getComponentDateTextField().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!birac.isPopupOpen()) {
                    birac.openPopup();
                }
            }
        });
        return birac;
    }
}
