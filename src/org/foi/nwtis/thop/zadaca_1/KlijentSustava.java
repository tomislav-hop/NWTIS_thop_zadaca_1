/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.zadaca_1;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.thop.konfiguracije.Konfiguracija;
import org.foi.nwtis.thop.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.thop.konfiguracije.NemaKonfiguracije;

/**
 *
 * @author NWTiS_3
 */
public class KlijentSustava {

    protected String parametri;
    protected Matcher mParametri;
    private Konfiguracija konfig;
    private int brDretvi = 1;
    private int razmakDretvi = 0;
    private int brPonavljanja = 1;
    private int cekaj = 0;

    public KlijentSustava(String parametri) throws Exception {
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
        String sintaksa = "^-user -s ([^\\s]+) -port (\\d{4}) -u ([^\\s]+) -konf +([^\\s]+.xml)( -cekaj (\\d{1}))?( -multi)?( -ponavljaj (\\d{1}))?$";

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

    public void pokreniKlijenta() {
        String datoteka = mParametri.group(4);
        File dat = new File(datoteka);

        if (!dat.exists()) {
            System.out.println("Datoteka konfiguracije ne postoji!");
            return;
        }

        konfig = null;

        try {
            konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);

            if (this.mParametri.group(6) != null) {
                int cekajBrojSekundi = Integer.parseInt(this.mParametri.group(6));
                System.out.println("CEKAJ: " + cekajBrojSekundi + " SEKUNDI");
                cekaj = cekajBrojSekundi;
                //TimeUnit.SECONDS.sleep(100);
            }
            if (this.mParametri.group(7) != null) {
                int brojDretvi = Integer.parseInt(konfig.dajPostavku("brojDretvi"));
                int randomBrojDretvi = randomBroj(1, brojDretvi);
                brDretvi = randomBrojDretvi;
                System.out.println("MULTI JE: " + randomBrojDretvi);

                int razmakDretviKonfig = Integer.parseInt(konfig.dajPostavku("razmakDretvi"));
                //TODO Pitaj novaka
                int randomBrojRazmak = randomBroj(1, razmakDretviKonfig);
                System.out.println("RAZMAK JE: " + randomBrojRazmak);
                razmakDretvi = randomBrojRazmak;
            }
            if (this.mParametri.group(9) != null) {
                int brojPonavljanja = Integer.parseInt(this.mParametri.group(9));
                brPonavljanja = brojPonavljanja;
                System.out.println("BROJ PONAVLJANJA JE: " + brojPonavljanja);

            }

            String server = this.mParametri.group(1);
            int port = Integer.parseInt(this.mParametri.group(2));

            for (int i = 0; i < brDretvi; i++) {

                try {
                    TimeUnit.SECONDS.sleep(razmakDretvi);
                } catch (InterruptedException ex) {
                    Logger.getLogger(KlijentSustava.class.getName()).log(Level.SEVERE, null, ex);
                }
                SlanjeZahtjeva sz = new SlanjeZahtjeva();
                //sz.setKonfig(konfig);
                sz.setServer(server);
                sz.setPort(port);
                sz.setBrojPonavljanja(brPonavljanja);
                sz.setCekaj(cekaj);
                sz.setKorisnik(this.mParametri.group(3));
                sz.setLozinka("");
                sz.setDobivenaKomanda("");
                sz.start();
            }
        } catch (NemaKonfiguracije ex) {
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
    }

    private int randomBroj(int odBroja, int doBroja) {
        int rBroj = (doBroja - odBroja) + odBroja;
        rBroj = (int) (Math.random() * rBroj) + 1;
        return rBroj;
    }
}
