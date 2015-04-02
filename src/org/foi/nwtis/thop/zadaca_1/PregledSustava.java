/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.zadaca_1;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomislav Hop
 * 
 * Klasa koja otvara datoteku cije je ime zadano u argumentima
 */
public class PregledSustava {

    protected String parametri;
    protected Matcher mParametri;

    public PregledSustava(String parametri) throws Exception {
        this.parametri = parametri;
        mParametri = provjeraParametara(parametri);
        if (mParametri == null) {
            throw new Exception("Parametri servera ne odgovaraju");
        }
    }

    public Matcher provjeraParametara(String p) {
        //String sintaksa1 = "^-server -konf ([^\\s]+\\.(?i)txt|xml)( +-load)?$";
        String sintaksa = "^-show -s +([^\\s]+)?$";

        Pattern pattern = Pattern.compile(sintaksa);
        Matcher m = pattern.matcher(p);
        boolean status = m.matches();
        if (status) {
            return m;
        } else {
            System.out.println("Ne odgovara!");
            return null;
        }
    }

    public void pokreniPreglednik() {
        String datoteka = mParametri.group(1);
        String datotekaZaLoad = datoteka.substring(datoteka.lastIndexOf(" ") + 1);
        System.out.println("DATOTEKA: " + datotekaZaLoad);
        File dat = new File(datotekaZaLoad);
        if (!dat.exists()) {
            System.out.println("Datoteka konfiguracije ne postoji!");
            return;
        }
        Evidencija e = new Evidencija(datoteka);
        e.citajHashMapu(datoteka);
        return;
    }
}
