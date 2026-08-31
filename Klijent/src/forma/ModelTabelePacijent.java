package forma;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import model.Ordinacija;
import model.Pacijent;

/**
 * Model tabele za prikaz liste pacijenata.
 *
 * @author vukla
 */
public class ModelTabelePacijent extends AbstractTableModel {

    private final String[] koloneNazivi = {
        "Id", "Ime", "Prezime", "Broj telefona", "Broj knjižice", "Ordinacija"
    };

    private List<Pacijent> pacijenti;

    public ModelTabelePacijent(List<Pacijent> pacijenti) {
        this.pacijenti = pacijenti != null ? pacijenti : new ArrayList<Pacijent>();
    }

    @Override
    public int getRowCount() {
        return pacijenti.size();
    }

    @Override
    public int getColumnCount() {
        return koloneNazivi.length;
    }

    @Override
    public String getColumnName(int column) {
        return koloneNazivi[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Pacijent pacijent = pacijenti.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return pacijent.getIdPacijent();
            case 1:
                return pacijent.getIme();
            case 2:
                return pacijent.getPrezime();
            case 3:
                return pacijent.getBrojTelefona();
            case 4:
                return pacijent.getBrojKnjizice();
            case 5:
                return nazivOrdinacije(pacijent.getOrdinacija());
            default:
                return null;
        }
    }

    private String nazivOrdinacije(Ordinacija ordinacija) {
        return ordinacija == null ? "" : ordinacija.getNaziv();
    }

    /**
     * Postavlja novu listu pacijenata i osvezava prikaz tabele.
     */
    public void postaviListu(List<Pacijent> pacijenti) {
        this.pacijenti = pacijenti != null ? pacijenti : new ArrayList<Pacijent>();
        fireTableDataChanged();
    }

    /**
     * Vraca pacijenta iz zadatog reda tabele.
     */
    public Pacijent vratiPacijenta(int red) {
        return pacijenti.get(red);
    }
}
