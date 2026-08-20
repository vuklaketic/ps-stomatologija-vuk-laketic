/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vukla
 */
public class StavkaTermina implements ApstraktniDomenskiObjekat {

    private int rb;
    private Termin termin;
    private int kolicina;
    private double iznos;
    private double cenaUsluge;
    private Usluga usluga;

    public StavkaTermina() {
    }

    public StavkaTermina(int rb, int kolicina, double iznos, double cenaUsluge,
            Termin termin, Usluga usluga) {
        this.rb = rb;
        this.kolicina = kolicina;
        this.iznos = iznos;
        this.cenaUsluge = cenaUsluge;
        this.termin = termin;
        this.usluga = usluga;
    }

    public int getRb() {
        return rb;
    }

    public void setRb(int rb) {
        this.rb = rb;
    }

    public int getKolicina() {
        return kolicina;
    }

    public void setKolicina(int kolicina) {
        this.kolicina = kolicina;
    }

    public double getIznos() {
        return iznos;
    }

    public void setIznos(double iznos) {
        this.iznos = iznos;
    }

    public double getCenaUsluge() {
        return cenaUsluge;
    }

    public void setCenaUsluge(double cenaUsluge) {
        this.cenaUsluge = cenaUsluge;
    }

    public Termin getTermin() {
        return termin;
    }

    public void setTermin(Termin termin) {
        this.termin = termin;
    }

    public Usluga getUsluga() {
        return usluga;
    }

    public void setUsluga(Usluga usluga) {
        this.usluga = usluga;
    }

    @Override
    public String vratiNazivTabele() {
        return "stavkatermina";
    }

    /**
     * Termin i usluga se kreiraju samo sa identifikatorom, jer upit cita iskljucivo
     * kolone tabele stavkatermina. Pune objekte popunjava serverski kontroler.
     */
    @Override
    public List<ApstraktniDomenskiObjekat> vratiListu(ResultSet rs) throws Exception {
        List<ApstraktniDomenskiObjekat> lista = new ArrayList<>();
        while (rs.next()) {
            int rb = rs.getInt("stavkatermina.rb");
            int kolicina = rs.getInt("stavkatermina.kolicina");
            double iznos = rs.getDouble("stavkatermina.iznos");
            double cenaUsluge = rs.getDouble("stavkatermina.cenaUsluge");

            Termin termin = new Termin();
            termin.setIdTermin(rs.getInt("stavkatermina.idTermin"));

            Usluga usluga = new Usluga();
            usluga.setIdUsluga(rs.getInt("stavkatermina.idUsluga"));

            StavkaTermina st = new StavkaTermina(rb, kolicina, iznos, cenaUsluge, termin, usluga);
            lista.add(st);
        }
        return lista;
    }

    @Override
    public String vratiKoloneZaUbacivanje() {
        return "idTermin, rb, kolicina, iznos, cenaUsluge, idUsluga";
    }

    @Override
    public String vratiVrednostiZaUbacivanje() {
        return termin.getIdTermin() + "," + rb + "," + kolicina + "," + iznos + "," + cenaUsluge + "," + usluga.getIdUsluga();
    }

    @Override
    public String vratiPrimarniKljuc() {
        return "idTermin=" + termin.getIdTermin() + " AND rb=" + rb;
    }

    /**
     * Vraca prvi objekat iz result set-a, odnosno null ako upit nije vratio
     * nijedan slog. Koristi se za upite koji vracaju najvise jedan red.
     */
    @Override
    public ApstraktniDomenskiObjekat vratiObjekatRS(ResultSet rs) throws Exception {
        List<ApstraktniDomenskiObjekat> lista = vratiListu(rs);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public String vratiVrednostiZaIzmenu() {
        return "kolicina=" + kolicina + ", iznos=" + iznos + ", cenaUsluge=" + cenaUsluge + ", idUsluga=" + usluga.getIdUsluga();
    }
}
