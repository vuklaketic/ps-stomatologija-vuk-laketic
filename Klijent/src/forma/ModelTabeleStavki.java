package forma;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import model.StavkaTermina;
import model.Usluga;

/**
 * Model tabele za prikaz stavki termina koje se unose na formi.
 *
 * Stavke koje ovaj model drzi postoje samo u memoriji klijenta, sve dok
 * stomatolog ne pozove sistem da zapamti termin. Tek tada ceo termin, zajedno
 * sa svojom listom stavki, odlazi na server i upisuje se u jednoj transakciji.
 *
 * Redni brojevi stavki se dodeljuju po redosledu u tabeli i odrzavaju se posle
 * svakog dodavanja i uklanjanja, pa termin uvek ima stavke numerisane od 1
 * naviste, bez rupa.
 *
 * @author vukla
 */
public class ModelTabeleStavki extends AbstractTableModel {

    private final String[] koloneNazivi = {
        "Rb", "Usluga", "Količina", "Cena usluge", "Iznos"
    };

    private List<StavkaTermina> stavke;

    public ModelTabeleStavki(List<StavkaTermina> stavke) {
        this.stavke = stavke != null ? stavke : new ArrayList<StavkaTermina>();
        prenumerisi();
    }

    @Override
    public int getRowCount() {
        return stavke.size();
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
        StavkaTermina stavka = stavke.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return stavka.getRb();
            case 1:
                return nazivUsluge(stavka.getUsluga());
            case 2:
                return stavka.getKolicina();
            case 3:
                return novcaniIznos(stavka.getCenaUsluge());
            case 4:
                return novcaniIznos(stavka.getIznos());
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    /**
     * Dodaje uslugu u zadatoj kolicini kao novu stavku termina.
     *
     * Ista usluga se ne unosi dva puta: ako je vec u tabeli, uvecava se
     * kolicina postojece stavke, jer dve stavke sa istom uslugom ne bi nosile
     * nikakav novi podatak, a racun bi bio nepregledan. Postojecoj stavci se
     * pritom zadrzava cena po kojoj je uneta.
     *
     * @param usluga usluga koja se dodaje
     * @param kolicina kolicina, mora biti veca od nule
     */
    public void dodajStavku(Usluga usluga, int kolicina) {
        StavkaTermina postojeca = pronadjiPoUsluzi(usluga);
        if (postojeca != null) {
            postojeca.setKolicina(postojeca.getKolicina() + kolicina);
            postojeca.setIznos(postojeca.getKolicina() * postojeca.getCenaUsluge());
        } else {
            StavkaTermina stavka = new StavkaTermina();
            stavka.setUsluga(usluga);
            stavka.setKolicina(kolicina);
            stavka.setCenaUsluge(usluga.getCena());
            stavka.setIznos(kolicina * usluga.getCena());
            stavke.add(stavka);
        }

        prenumerisi();
        fireTableDataChanged();
    }

    /**
     * Uklanja stavku iz zadatog reda tabele.
     */
    public void ukloniStavku(int red) {
        stavke.remove(red);
        prenumerisi();
        fireTableDataChanged();
    }

    /**
     * Postavlja novu listu stavki i osvezava prikaz tabele.
     */
    public void postaviListu(List<StavkaTermina> stavke) {
        this.stavke = stavke != null ? stavke : new ArrayList<StavkaTermina>();
        prenumerisi();
        fireTableDataChanged();
    }

    /**
     * Vraca stavku iz zadatog reda tabele.
     */
    public StavkaTermina vratiStavku(int red) {
        return stavke.get(red);
    }

    /**
     * Vraca sve unete stavke, u redosledu u kome stoje u tabeli.
     */
    public List<StavkaTermina> vratiStavke() {
        return stavke;
    }

    public boolean jePrazna() {
        return stavke.isEmpty();
    }

    /**
     * Vraca zbir iznosa svih stavki, odnosno ukupnu cenu termina.
     */
    public double ukupanIznos() {
        double ukupno = 0;
        for (StavkaTermina stavka : stavke) {
            ukupno += stavka.getIznos();
        }
        return ukupno;
    }

    /**
     * Dodeljuje stavkama redne brojeve od 1 naviste, po redosledu u tabeli.
     */
    private void prenumerisi() {
        int redniBroj = 1;
        for (StavkaTermina stavka : stavke) {
            stavka.setRb(redniBroj++);
        }
    }

    private StavkaTermina pronadjiPoUsluzi(Usluga usluga) {
        for (StavkaTermina stavka : stavke) {
            if (stavka.getUsluga() != null
                    && stavka.getUsluga().getIdUsluga() == usluga.getIdUsluga()) {
                return stavka;
            }
        }
        return null;
    }

    private String nazivUsluge(Usluga usluga) {
        return usluga == null ? "" : usluga.getNaziv();
    }

    /**
     * Prikazuje novcani iznos na dve decimale, kao sto je i zapisan u bazi.
     */
    private String novcaniIznos(double vrednost) {
        return String.format("%.2f", vrednost);
    }
}
