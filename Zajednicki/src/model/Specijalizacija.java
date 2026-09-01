/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import util.SqlUtil;

/**
 *
 * @author vukla
 */
public class Specijalizacija implements ApstraktniDomenskiObjekat {

    /**
     * Spajanje sa veznom tabelom, za citanje specijalizacija jednog
     * stomatologa. Ne koristi se u ostalim upitima nad specijalizacijama,
     * jer im vezna tabela nije potrebna.
     */
    public static final String SPOJ_SA_STOMATOLOGOM =
            " JOIN stspec ON stspec.idSpecijalizacija = specijalizacija.idSpecijalizacija";

    private int idSpecijalizacija;
    private String naziv;

    public Specijalizacija() {
    }

    public Specijalizacija(int idSpecijalizacija, String naziv) {
        this.idSpecijalizacija = idSpecijalizacija;
        this.naziv = naziv;
    }

    public int getIdSpecijalizacija() {
        return idSpecijalizacija;
    }

    public void setIdSpecijalizacija(int idSpecijalizacija) {
        this.idSpecijalizacija = idSpecijalizacija;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    @Override
    public String vratiNazivTabele() {
        return "specijalizacija";
    }

    @Override
    public List<ApstraktniDomenskiObjekat> vratiListu(ResultSet rs) throws Exception {
        List<ApstraktniDomenskiObjekat> lista = new ArrayList();
        while (rs.next()) {
            int idSpecijalizacija = rs.getInt("specijalizacija.idSpecijalizacija");
            String naziv = rs.getString("specijalizacija.naziv");

            Specijalizacija sp = new Specijalizacija(idSpecijalizacija, naziv);
            lista.add(sp);
        }
        return lista;
    }

    @Override
    public String vratiKoloneZaUbacivanje() {
        return "naziv";
    }

    @Override
    public String vratiVrednostiZaUbacivanje() {
        return "'" + SqlUtil.escapiraj(naziv) + "'";
    }

    @Override
    public String vratiPrimarniKljuc() {
        return "idSpecijalizacija=" + idSpecijalizacija;
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
        return "naziv='" + SqlUtil.escapiraj(naziv) + "'";
    }

}
