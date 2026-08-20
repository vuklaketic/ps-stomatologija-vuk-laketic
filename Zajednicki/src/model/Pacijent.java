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
public class Pacijent implements ApstraktniDomenskiObjekat {

    private int idPacijent;
    private String ime;
    private String prezime;
    private String brojTelefona;
    private String brojKnjizice;
    private Ordinacija ordinacija;

    public Pacijent() {
    }

    public Pacijent(int idPacijent, String ime, String prezime, String brojTelefona,
            String brojKnjizice, Ordinacija ordinacija) {
        this.idPacijent = idPacijent;
        this.ime = ime;
        this.prezime = prezime;
        this.brojTelefona = brojTelefona;
        this.brojKnjizice = brojKnjizice;
        this.ordinacija = ordinacija;
    }

    public int getIdPacijent() {
        return idPacijent;
    }

    public void setIdPacijent(int idPacijent) {
        this.idPacijent = idPacijent;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getBrojTelefona() {
        return brojTelefona;
    }

    public void setBrojTelefona(String brojTelefona) {
        this.brojTelefona = brojTelefona;
    }

    public String getBrojKnjizice() {
        return brojKnjizice;
    }

    public void setBrojKnjizice(String brojKnjizice) {
        this.brojKnjizice = brojKnjizice;
    }

    public Ordinacija getOrdinacija() {
        return ordinacija;
    }

    public void setOrdinacija(Ordinacija ordinacija) {
        this.ordinacija = ordinacija;
    }

    @Override
    public String vratiNazivTabele() {
        return "pacijent";
    }

    /**
     * Ordinacija se ovde kreira samo sa identifikatorom, jer upit cita iskljucivo
     * kolone tabele pacijent. Pun objekat ordinacije popunjava serverski kontroler.
     */
    @Override
    public List<ApstraktniDomenskiObjekat> vratiListu(ResultSet rs) throws Exception {
        List<ApstraktniDomenskiObjekat> lista = new ArrayList<>();
        while (rs.next()) {
            int idPacijent = rs.getInt("pacijent.idPacijent");
            String ime = rs.getString("pacijent.ime");
            String prezime = rs.getString("pacijent.prezime");
            String brojTelefona = rs.getString("pacijent.brojTelefona");
            String brojKnjizice = rs.getString("pacijent.brojKnjizice");

            Ordinacija ordinacija = new Ordinacija();
            ordinacija.setIdOrdinacija(rs.getInt("pacijent.idOrdinacija"));

            Pacijent p = new Pacijent(idPacijent, ime, prezime, brojTelefona, brojKnjizice, ordinacija);
            lista.add(p);
        }
        return lista;
    }

    @Override
    public String vratiKoloneZaUbacivanje() {
        return "ime, prezime, brojTelefona, brojKnjizice, idOrdinacija";
    }

    @Override
    public String vratiVrednostiZaUbacivanje() {
        return "'" + ime + "','" + prezime + "','" + brojTelefona + "','" + brojKnjizice + "'," + ordinacija.getIdOrdinacija();
    }

    @Override
    public String vratiPrimarniKljuc() {
        return "idPacijent=" + idPacijent;
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
        return "ime='" + ime + "', prezime='" + prezime + "', brojTelefona='" + brojTelefona
                + "', brojKnjizice='" + brojKnjizice + "', idOrdinacija=" + ordinacija.getIdOrdinacija();
    }

}
