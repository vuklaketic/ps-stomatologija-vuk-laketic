package forma;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import model.Pacijent;

/**
 * Prikaz pacijenta u padajucoj listi. Domenske klase nemaju toString, pa
 * prikaz resava renderer; ostale stavke (npr. "Svi") idu kroz podrazumevani.
 *
 * @author vukla
 */
class RendererPacijenta extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> lista, Object vrednost, int indeks,
            boolean izabran, boolean fokusiran) {
        super.getListCellRendererComponent(lista, vrednost, indeks, izabran, fokusiran);
        if (vrednost instanceof Pacijent) {
            Pacijent p = (Pacijent) vrednost;
            setText(p.getIme() + " " + p.getPrezime() + " (" + p.getBrojKnjizice() + ")");
        }
        return this;
    }
}
