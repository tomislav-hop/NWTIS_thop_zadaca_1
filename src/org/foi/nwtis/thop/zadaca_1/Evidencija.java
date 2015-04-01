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

    public synchronized void spremiHashMapu(HashMap<String, EvidencijaModel> map) {
        try {
            File fileOne = new File(nazivEvidDatoteke);
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

    public void citajHashMapu(HashMap<String, EvidencijaModel> map) {
        try {
            File toRead = new File(nazivEvidDatoteke);
            FileInputStream fis = new FileInputStream(toRead);
            ObjectInputStream ois = new ObjectInputStream(fis);

            HashMap<String, EvidencijaModel> mapInFile = (HashMap<String, EvidencijaModel>) ois.readObject();

            ois.close();
            fis.close();

            for (Map.Entry<String, EvidencijaModel> m : mapInFile.entrySet()) {
                System.out.println(m.getKey() + " : " + m.getValue());
                EvidencijaModel ee = (EvidencijaModel) m.getValue();
                ArrayList<EvidencijaModel.ZahtjevKorisnika> zz = ee.getZahtjevi();
                System.out.println("HOPNOBO" + zz.get(0).getIpAdresa());
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
