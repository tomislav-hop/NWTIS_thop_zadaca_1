/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.zadaca_1;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.thop.konfiguracije.Konfiguracija;
import org.foi.nwtis.thop.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.thop.konfiguracije.NemaKonfiguracije;

/**
 *
 * @author Tomislav Hop
 */
public class AdministratorSustava {

    protected String parametri;
    protected Matcher mParametri;
    private Konfiguracija konfig;

    public AdministratorSustava(String parametri) throws Exception {
        this.parametri = parametri;
        mParametri = provjeraParametara(parametri);
        if (mParametri == null) {
            throw new Exception("Parametri servera ne odgovaraju");
        }
    }

    public Matcher provjeraParametara(String p) {

        //TODO korisnik  može sadržavati mala i velika slova, brojeve i znakove: _, -
        //TODO OVO JE NEST SPOMINJO: String sintaksa1 = "^-server -konf ([^\\s]+\\.(?i)txt|xml)( +-load)?$";
        //TODO txt datoteka
        String sintaksa = "^-admin -s ([^\\s]+) -port (\\d{4}) -u ([^\\s]+) -p ([^\\s]+)( -(pause|start|stop|save|clean|stat|upload ([^\\\\s]+.xml)|download ([^\\\\s]+.xml)))?$";

        //String sintaksa = "^-admin -s ([^\\s]+) -port (\\d{4}) -u ([^\\s]+) -p ([^\\s]+) -konf +([^\\s]+.xml)( -(pause|start|stop|save|clean|stat|upload ([^\\\\s]+.xml)|download ([^\\\\s]+.xml)))?$";
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

    public void pokreniAdministratora() {

        if (mParametri.group(6).equals("pause")) {
            System.out.println("PAUZA");
            saljiZahtjev("PAUSE");
        } else if (mParametri.group(6).equals("start")) {
            System.out.println("START");
            saljiZahtjev("START");
        } else if (mParametri.group(6).equals("stop")) {
            System.out.println("STOP");
            saljiZahtjev("STOP");
        } else if (mParametri.group(6).equals("save")) {
            System.out.println("SAVE");
            saljiZahtjev("SAVE");
        } else if (mParametri.group(6).equals("clean")) {
            System.out.println("CLEAN");
            saljiZahtjev("CLEAN");
        } else if (mParametri.group(6).equals("stat")) {
            System.out.println("STAT");
            saljiZahtjev("STAT");
        } else if (mParametri.group(6).startsWith("upload")) {
            System.out.println("UPLOAD: " + mParametri.group(7));
            saljiZahtjev("UPLOAD");
        } else if (mParametri.group(6).startsWith("download")) {
            System.out.println("DOWNLOAD: " + mParametri.group(8));
            saljiZahtjev("DOWNLOAD");
        }
    }

    public void saljiZahtjev(String komanda) {
        SlanjeZahtjeva sz = new SlanjeZahtjeva();
        sz.setServer(this.mParametri.group(1));
        sz.setPort(Integer.parseInt(this.mParametri.group(2)));
        sz.setKorisnik(this.mParametri.group(3));
        sz.setLozinka(this.mParametri.group(4));
        sz.setBrojPonavljanja(1);
        sz.setCekaj(0);
        
        sz.setBrojPokusajaProblema(1);
        sz.setPauzaProblema(0);
        sz.setIntervalDretve(0);
        
        sz.setDobivenaKomanda(komanda);


        sz.start();
    }
}
