/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forma;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import model.Pacijent;
import model.Termin;

/**
 * Model tabele za prikaz liste termina.
 *
 * @author vukla
 */
public class ModelTabeleTermin extends AbstractTableModel {

    private final String[] koloneNazivi = {
        "Id", "Datum", "Vreme", "Pacijent", "Status", "Napomena"
    };

    private List<Termin> termini;

    public ModelTabeleTermin(List<Termin> termini) {
        this.termini = termini != null ? termini : new ArrayList<Termin>();
    }

    @Override
    public int getRowCount() {
        return termini.size();
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
        Termin termin = termini.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return termin.getIdTermin();
            case 1:
                return termin.getDatum();
            case 2:
                return termin.getVreme();
            case 3:
                return imePacijenta(termin.getPacijent());
            case 4:
                return termin.getStatus();
            case 5:
                return termin.getNapomena();
            default:
                return "";
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    /**
     * Vraca termin iz zadatog reda tabele.
     */
    public Termin vratiTermin(int red) {
        return termini.get(red);
    }

    /**
     * Postavlja novu listu termina i osvezava prikaz tabele.
     */
    public void postaviListu(List<Termin> termini) {
        this.termini = termini != null ? termini : new ArrayList<Termin>();
        fireTableDataChanged();
    }

    private String imePacijenta(Pacijent pacijent) {
        if (pacijent == null) {
            return "";
        }
        return pacijent.getIme() + " " + pacijent.getPrezime();
    }
}
