/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.zadaca_1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Tomislav Hop
 */
public class Evidencija implements Serializable {

    private List<EvidencijaModel> evidencija;
    private String evidDat;
    private String nazivDat;

    public Evidencija(List<EvidencijaModel> evidencija, String evidDat, String nazivDat) {
        this.evidencija = evidencija;
        this.evidDat = evidDat;
        this.nazivDat = nazivDat;
    }

    public synchronized List<EvidencijaModel> getEvidencija() {
        return evidencija;
    }

    public synchronized void SpremiEvidenciju(Evidencija e) throws IOException {
        File f = new File(nazivDat);
        if (f.exists()) {
            f.delete();
            SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd_hhmmss");
            String datum = sdf.format(new Date());
            String ekstenzija = evidDat.split("\\.")[1];
            String naziv = evidDat.split("\\.")[0];
            nazivDat = naziv + datum + "." + ekstenzija;
            f = new File(nazivDat);
        } else {
            f = new File(this.evidDat);
            nazivDat = evidDat;
        }

        if (f.exists()) {
            f.delete();
            FileOutputStream out;

            out = new FileOutputStream(f);
            ObjectOutputStream s = new ObjectOutputStream(out);

            s.writeObject(e);
            s.close();

            System.out.println("Evidencija serijalizirana!");
        }
    }
    
    
    public synchronized Evidencija ucitajEvidenciju(String putanja) throws ClassNotFoundException, IOException {
        Evidencija e = null;
        FileInputStream in;
        File f = new File(putanja);
        if (f.length() > 0 && f.exists()) {
            in = new FileInputStream(f);
            ObjectInputStream s = new ObjectInputStream(in);
            e = (Evidencija) s.readObject();
            s.close();
        } else {
            System.out.println("Datoteka ne postoji ili ne sadrzi zapise!");
        }
        return e;
    }   

}
