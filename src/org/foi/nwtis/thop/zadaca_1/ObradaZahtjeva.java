/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.zadaca_1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
public class ObradaZahtjeva extends Thread {

    public enum StanjeDretve {

        Slobodna, Zauzeta
    };

    private Konfiguracija konfig;
    private Socket socket;
    private StanjeDretve stanje;
    private String porukaGreske = "";
    protected Matcher mKomanda;
    private int pauzaServera = 0;
    private boolean stopServera = false;

    public Slusac slusac;

    public ObradaZahtjeva(ThreadGroup group, String name) {
        super(group, name);
        this.stanje = StanjeDretve.Slobodna;
    }

    @Override
    public void interrupt() {
        //this.stanje = StanjeDretve.Slobodna;
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {

        InputStream is = null;
        OutputStream os = null;

        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
            StringBuilder sb = new StringBuilder();

            while (true) {
                int znak = is.read();
                if (znak == -1) {
                    break;
                }
                sb.append((char) znak);

            }

            String poruka = null;
            //Slusac slusac = new Slusac;
            if (porukaGreske.equals("")) {
                mKomanda = provjeraParametara(sb.toString());

                if (mKomanda != null) {
                    if (mKomanda.group(7).equals("TIME") && pauzaServera == 1) {
                        System.out.println("KLIJENT ULAZI U SERVER DOK JE PAUZIRAN!");
                        //System.out.println("Sada treba javiti gresku klijentu za to sta je poslo dok je pauzirano");
                        //poruka =  "ERROR 10; Server je pauziran";
                        poruka = "ERROR 10; Server je pauziran";

                    }

                    if (mKomanda.group(7).equals("TIME") && pauzaServera == 0) {
                        System.out.println("Primljena poruka: " + sb.toString() + "Dretva: " + this.getName());
                        DateFormat dateFormat = new SimpleDateFormat("YYYY.MM.dd hh:mm:ss");
                        Date date = new Date();
                        poruka = "OK;" + dateFormat.format(date) + "; Dretva: " + this.getName();
                    } else if (!mKomanda.group(7).equals("TIME")) {
                        //TODO AKO JE ADMIN SLAO ZAHTJEV
                        //System.out.println("Komanda poslana serveru je: " + mKomanda.group(7));
                        poruka = "Komanda poslana serveru je: " + mKomanda.group(7);
                        switch (mKomanda.group(7)) {
                            case "PAUSE":
                                poruka = pauziranjeServera();
                                break;
                            //this.
                            case "START":
                                poruka = startServera();
                                break;
                            case "STOP":
                                poruka = stopServera();
                                if (poruka.equals("OK")) {
                                    stopServera = true;
                                }
                                break;
                            case "CLEAN":
                                
                                break;
                            case "STAT":
                               
                                break;
                            case "UPLOAD":
                                
                                break;
                            default:
                                poruka = "ERROR 10; Komanda koju ste poslali nije ispravna";
                                break;
                        }
                    }
                    /*else{
                     System.out.println("NESTO NE VALJA!");}*/

                } else {
                    poruka = "ERROR 10; Komanda koju ste poslali nije ispravna";
                }
            } else {
                poruka = porukaGreske;
            }

            if (poruka != null) {
                os.write(poruka.getBytes());
                os.flush();
            }

        } catch (IOException ex) {
            Logger.getLogger(ObradaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (is != null) {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(ObradaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (os != null) {
            try {
                os.close();
            } catch (IOException ex) {
                Logger.getLogger(ObradaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            socket.close();
            //this.stanje = StanjeDretve.Slobodna;
        } catch (IOException ex) {
            Logger.getLogger(ObradaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
        }

        /*try {
         sleep(20000);
         } catch (InterruptedException ex) {
         Logger.getLogger(ObradaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
         }*/
        this.stanje = StanjeDretve.Slobodna;
        if (stopServera == true) {
            slusac.pauza(3);
        }
    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

    public void setKonfig(Konfiguracija konfig) {
        this.konfig = konfig;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setStanje(StanjeDretve stanje) {
        this.stanje = stanje;
    }

    public StanjeDretve getStanje() {
        return stanje;
    }

    public void setPorukaGreske(String porukaGreske) {
        this.porukaGreske = porukaGreske;
    }

    public int getPauzaServera() {
        return pauzaServera;
    }

    public void setPauzaServera(int pauzaServera) {
        this.pauzaServera = pauzaServera;
    }

    public Matcher provjeraParametara(String p) {
        //TODO txt datoteka
        //String sintaksa = "USER ([^\\\\s]+);(TIME);$";
        //String sintaksa = "USER ([^\\\\]+)(; PASSWD ([^\\\\]+))?;([A-Z]+);$";
        String sintaksa = "USER ([^\\\\]+)(;)?(( PASSWD )?([^\\\\]+)?)?(;)([A-Z]+);$";

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

    private String pauziranjeServera() {
        String poruka = null;
        if (provjeraLozinke() && pauzaServera == 0) {
            //System.out.println("PROVJERA JE USPJELA!!!!");
            slusac.pauza(1);
            poruka = "OK";
        } else if (provjeraLozinke() && pauzaServera == 1) {
            poruka = "ERROR 01; Server je vec u stanju pauze";
        } else {
            //System.out.println("PROVJERA NIJE USPJELA!!!!");
            poruka = "ERROR 00; Korisnicko ime ili lozinka nisu ispravni";
        }
        return poruka;
    }

    private String startServera() {
        String poruka = null;
        if (provjeraLozinke() && pauzaServera == 1) {
            slusac.pauza(0);
            poruka = "OK";
        } else if (provjeraLozinke() && pauzaServera == 0) {
            poruka = "ERROR 01; Server nije u stanju pauze";
        } else {
            poruka = "ERROR 00; Korisnicko ime ili lozinka nisu ispravni";
        }

        return poruka;
    }

    private String stopServera() {
        String poruka = null;
        if (provjeraLozinke()) {
            poruka = "OK";
        } else {
            poruka = "ERROR 00; Korisnicko ime ili lozinka nisu ispravni";
        }
        return poruka;
    }

    private boolean provjeraLozinke() {
        String lozinka = mKomanda.group(1).substring(mKomanda.group(1).lastIndexOf(" ") + 1);
        //System.out.println("LOZINKA: |" + lozinka + "|");
        String korisnickoIme = mKomanda.group(1).split(";")[0];
        //System.out.println("KORIME: |" + korisnickoIme + "|");

        String datoteka = konfig.dajPostavku("adminDatoteka");
        File dat = new File(datoteka);

        if (!dat.exists()) {
            System.out.println("Datoteka administratora ne postoji!");
            return false;
        }

        Konfiguracija administratori = null;
        try {
            administratori = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);

        } catch (NemaKonfiguracije ex) {
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        if (administratori.postojiPostavka(korisnickoIme)) {
            if (administratori.dajPostavku(korisnickoIme).equals(lozinka)) {
                return true;
            }
        }

        return false;
    }
}
