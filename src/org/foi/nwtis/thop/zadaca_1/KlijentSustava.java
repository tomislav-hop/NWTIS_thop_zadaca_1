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
 * @author Tomislav Hop
 *
 * Klasa koja na osnovi argumenata stvara jednu dretvu ili random broj dretvi te
 * pokreće dretve za slanje zahtjeva
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

    /**
     *
     * @param p
     * @return
     *
     * Metoda koja provjerava parametre zadane putem komandne linija, 3
     * opcionalne vrijednosti na kraju regexa
     */
    public Matcher provjeraParametara(String p) {
        //TODO txt datoteka
        //String sintaksa = "^-user -s ([^\\s]+) -port (\\d{4}) -u ([^\\s]+) -konf +([^\\s]+.xml)( -cekaj (\\d{1}))?( -multi)?( -ponavljaj (\\d{1}))?$";
        //-konf +([^\\\\s]+.(txt|xml))
        
        String sintaksa = "^-user -s ([^\\s]+) -port (\\d{4}) -u ([^\\s]+) -konf +([^\\s]+.(txt|xml))( -cekaj (\\d{1}))?( -multi)?( -ponavljaj (\\d{1}))?$";
        
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

    /**
     * Metoda koja pokreće klijenta te na osnovi opcionalnih parametara dohvaća
     * vrijednosti iz datoteke konfiguracije
     */
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
            /**
             * Ako je postavljen argument -cekaj čitamo tu vrijednost iz
             * parametara i spremamo ju u variablu cekaj
             */
            if (this.mParametri.group(7) != null) {
                int cekajBrojSekundi = Integer.parseInt(this.mParametri.group(7));
                cekaj = cekajBrojSekundi;
                System.out.println("Cekaj: " + cekaj);
            }
            /**
             * Ako je postavljen argument -multi generiramo radom broj od 1 do
             * maksimalnog broja dretvi te generiramo random razmak od 1 do
             * maksimalnog razmaka dretvi. Obje maksimalne vrijednosti se nalaze
             * u datoteci konfiguracije.
             */
            if (this.mParametri.group(8) != null) {
                int brojDretvi = Integer.parseInt(konfig.dajPostavku("brojDretvi"));
                int randomBrojDretvi = randomBroj(1, brojDretvi);
                brDretvi = randomBrojDretvi;
                int razmakDretviKonfig = Integer.parseInt(konfig.dajPostavku("razmakDretvi"));
                int randomBrojRazmak = randomBroj(1, razmakDretviKonfig);
                razmakDretvi = randomBrojRazmak;
                System.out.println("Broj dretvi generiranih: " + brDretvi+ "\nRazmak izmedju dretvi: "+razmakDretvi);
            }
            /**
             * Ako je postavljen argument -ponavljaj čitamo broj upisan i
             * šaljemo ga u dretvu za slanje zahtjeva
             */
            if (this.mParametri.group(10) != null) {
                int brojPonavljanja = Integer.parseInt(this.mParametri.group(10));
                brPonavljanja = brojPonavljanja;
                System.out.println("Broj ponavljanja: " + brPonavljanja);
            }
            String server = this.mParametri.group(1);
            int port = Integer.parseInt(this.mParametri.group(2));
            /**
             * Ako je postavljen multi ovdje prolazimo kroz for petlju random
             * broj puta
             */
            for (int i = 0; i < brDretvi; i++) {
                /**
                 * Ako je postavljen multi ovdje čekamo između kreiranja svake
                 * dretve random broj sekundi
                 */
                try {
                    TimeUnit.SECONDS.sleep(razmakDretvi);
                } catch (InterruptedException ex) {
                    Logger.getLogger(KlijentSustava.class.getName()).log(Level.SEVERE, null, ex);
                }
                int brPokusaja = Integer.parseInt(konfig.dajPostavku("brojPokusajaProblema"));
                int pauzaProblema = Integer.parseInt(konfig.dajPostavku("pauzaProblema"));
                int intervalDretve = Integer.parseInt(konfig.dajPostavku("intervalDretve"));
                SlanjeZahtjeva sz = new SlanjeZahtjeva();
                sz.setServer(server);
                sz.setPort(port);
                sz.setBrojPonavljanja(brPonavljanja);
                sz.setCekaj(cekaj);
                sz.setKorisnik(this.mParametri.group(3));
                sz.setLozinka("");
                sz.setDobivenaKomanda("");
                sz.setBrojPokusajaProblema(brPokusaja);
                sz.setPauzaProblema(pauzaProblema);
                sz.setIntervalDretve(intervalDretve);
                sz.start();
            }
        } catch (NemaKonfiguracije ex) {
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
    }

    /**
     *
     * @param odBroja
     * @param doBroja
     * @return
     *
     * Funkcija koja vraća random broj od vrijednosti varijable odBroja do
     * vrijednosti varijable doBroja
     */
    private int randomBroj(int odBroja, int doBroja) {
        int rBroj = (doBroja - odBroja) + odBroja;
        rBroj = (int) (Math.random() * rBroj) + 1;
        return rBroj;
    }
}
