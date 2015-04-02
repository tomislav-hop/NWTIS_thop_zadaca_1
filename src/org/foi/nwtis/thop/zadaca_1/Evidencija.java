/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.zadaca_1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.foi.nwtis.thop.konfiguracije.Konfiguracija;

/**
 *
 * @author Tomislav Hop
 */
public class Evidencija implements Serializable {

    private HashMap<String, EvidencijaModel> evidencijaRad;
    private String nazivEvidDatoteke;

    public Evidencija(String nazivEvidDatoteke) {
        this.nazivEvidDatoteke = nazivEvidDatoteke;
        this.evidencijaRad = new HashMap<>();
    }

    public HashMap<String, EvidencijaModel> getEvidencijaRad() {
        return evidencijaRad;
    }

    public void setEvidencijaRad(HashMap<String, EvidencijaModel> evidencijaRad) {
        this.evidencijaRad = evidencijaRad;
    }

    public synchronized void spremiHashMapu(HashMap<String, EvidencijaModel> map, String datum, String nazivDat) {
        try {
            String prviDio = nazivDat.split("\\.")[0];
            String drugiDio = nazivDat.split("\\.")[1];
            String naziv = prviDio + datum + "." + drugiDio;
            System.out.println("Spremljena datoteka: " + naziv);
            File fileOne = new File(naziv);
            FileOutputStream fos = new FileOutputStream(fileOne);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(map);
            oos.flush();
            oos.close();
            fos.close();
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    public void citajHashMapu(String nazivDretve) {
        try {
            String naziv = nazivEvidDatoteke;
            File toRead = new File(naziv);
            FileInputStream fis = new FileInputStream(toRead);
            ObjectInputStream ois = new ObjectInputStream(fis);
            HashMap<String, EvidencijaModel> mapInFile = (HashMap<String, EvidencijaModel>) ois.readObject();
            ois.close();
            fis.close();
            System.out.println("\n------------ISPIS-SERIJALIZIRANE-DATOTEKE----------------");
            //System.out.println("mapInFile size: " + mapInFile.size());

            for (Map.Entry<String, EvidencijaModel> m : mapInFile.entrySet()) {
                EvidencijaModel ee = (EvidencijaModel) m.getValue();
                ArrayList<EvidencijaModel.ZahtjevKorisnika> zz = ee.getZahtjevi();
                System.out.println("---------------PODATCI-OD-DRETVE-" + ee.getOznaka() + "-------------------");
                System.out.println("Vrijeme prvog zahtjeva: " + ee.getPrviZahtjev());
                System.out.println("Vrijeme zadnjeg zahtjeva: " + ee.getZadnjiZahtjev());
                System.out.println("Ukupno vrijeme rada: " + ee.getUkupnoVrijemeRada() + " sekundi");
                System.out.println("Broj zahtjeva: " + ee.getUkupanBrojZahtjeva());
                //System.out.println("Velicina arraya je : " + zz.size());
                System.out.println("\n-----------------------ZAHTJEVI-------------------------");
                for (int i = 0; i < zz.size(); i++) {
                    System.out.println("Odgovor: " + zz.get(i).getOdgovor());
                    System.out.println("IP adresa: " + zz.get(i).getIpAdresa());
                    System.out.println("Komanda: " + zz.get(i).getZahtjev());
                    System.out.println("Vrijeme dobivanja komande: " + zz.get(i).getVrijeme());
                    System.out.println("---------------------------------------------------------\n\n");
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
