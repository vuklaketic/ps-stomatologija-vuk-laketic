/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vukla
 */
public class StSpec implements ApstraktniDomenskiObjekat {

    private LocalDate datumSticanja;
    private Stomatolog stomatolog;
    private Specijalizacija specijalizacija;

    public StSpec() {
    }

    public StSpec(LocalDate datumSticanja, Stomatolog stomatolog,
            Specijalizacija specijalizacija) {
        this.datumSticanja = datumSticanja;
        this.stomatolog = stomatolog;
        this.specijalizacija = specijalizacija;
    }

    public LocalDate getDatumSticanja() {
        return datumSticanja;
    }

    public void setDatumSticanja(LocalDate datumSticanja) {
        this.datumSticanja = datumSticanja;
    }

    public Stomatolog getStomatolog() {
        return stomatolog;
    }

    public void setStomatolog(Stomatolog stomatolog) {
        this.stomatolog = stomatolog;
    }

    public Specijalizacija getSpecijalizacija() {
        return specijalizacija;
    }

    public void setSpecijalizacija(Specijalizacija specijalizacija) {
        this.specijalizacija = specijalizacija;
    }

    @Override
    public String vratiNazivTabele() {
        return "stspec";
    }

    /** Stomatolog i specijalizacija se prave samo sa identifikatorom, jer upit cita samo kolone tabele stspec. */
    @Override
    public List<ApstraktniDomenskiObjekat> vratiListu(ResultSet rs) throws Exception {
        List<ApstraktniDomenskiObjekat> lista = new ArrayList<>();
        while (rs.next()) {
            java.sql.Date sqlDatum = rs.getDate("stspec.datumSticanja");
            LocalDate datumSticanja = sqlDatum == null ? null : sqlDatum.toLocalDate();

            Stomatolog stomatolog = new Stomatolog();
            stomatolog.setIdStomatolog(rs.getInt("stspec.idStomatolog"));

            Specijalizacija specijalizacija = new Specijalizacija();
            specijalizacija.setIdSpecijalizacija(rs.getInt("stspec.idSpecijalizacija"));

            StSpec ss = new StSpec(datumSticanja, stomatolog, specijalizacija);
            lista.add(ss);
        }
        return lista;
    }

    @Override
    public String vratiKoloneZaUbacivanje() {
        return "idStomatolog, idSpecijalizacija, datumSticanja";
    }

    @Override
    public String vratiVrednostiZaUbacivanje() {
        return stomatolog.getIdStomatolog() + "," + specijalizacija.getIdSpecijalizacija() + ",'" + datumSticanja + "'";
    }

    @Override
    public String vratiPrimarniKljuc() {
        return "idStomatolog=" + stomatolog.getIdStomatolog()
                + " AND idSpecijalizacija=" + specijalizacija.getIdSpecijalizacija();
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
        return "datumSticanja='" + datumSticanja + "'";
    }

}
