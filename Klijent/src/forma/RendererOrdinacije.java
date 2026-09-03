package forma;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import model.Ordinacija;

/**
 * Prikaz ordinacije u padajucoj listi. Domenske klase nemaju toString, pa
 * prikaz resava renderer; ostale stavke (npr. "Sve") idu kroz podrazumevani.
 *
 * @author vukla
 */
class RendererOrdinacije extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> lista, Object vrednost, int indeks,
            boolean izabran, boolean fokusiran) {
        super.getListCellRendererComponent(lista, vrednost, indeks, izabran, fokusiran);
        if (vrednost instanceof Ordinacija) {
            Ordinacija o = (Ordinacija) vrednost;
            setText(o.getNaziv() + " (" + o.getAdresa() + ")");
        }
        return this;
    }
}
