/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.zadaca_1;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
public class ServerSustava implements Slusac{

    protected String parametri;
    protected Matcher mParametri;
    int brojacDretvi = 0;
    private int pauzaServera = 0;
    
    private ThreadGroup stopS;
    private Socket stopSocket;

    //U KONTSTRUKTORU MORAMO KORSITITI EXCEPTION JER NE MOZEMO KORISTITI RETURN
    public ServerSustava(String parametri) throws Exception {
        this.parametri = parametri;
        mParametri = provjeraParametara(parametri);
        if (mParametri == null) {
            throw new Exception("Parametri servera ne odgovaraju");
        }
    }

    public Matcher provjeraParametara(String p) {
        //TODO txt datoteka
        String sintaksa = "^-server -konf +([^\\s]+.xml)( +-load)?$";

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

    public void pokreniServer() {
        String datoteka = mParametri.group(1);
        File dat = new File(datoteka);

        if (!dat.exists()) {
            System.out.println("Datoteka konfiguracije ne postoji!");
            return;
        }

        Konfiguracija konfig = null;
        try {
            konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);

            if (this.mParametri.group(2) != null) {
                String datEvid = konfig.dajPostavku("evidDatoteka");
                ucitajSerijaliziranuEvidenciju(datEvid);
            }

        } catch (NemaKonfiguracije ex) {
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        SerijalizatorEvidencije se = new SerijalizatorEvidencije(konfig);
        se.start();
        int brojDretvi = Integer.parseInt(konfig.dajPostavku("brojDretvi"));
        ThreadGroup tg = new ThreadGroup("thop");
        stopS = tg;
        ObradaZahtjeva[] dretve = new ObradaZahtjeva[brojDretvi];

        for (int i = 0; i < brojDretvi; i++) {
            dretve[i] = new ObradaZahtjeva(tg, "thop" + i);
            dretve[i].setKonfig(konfig);
        }

        int port = Integer.parseInt(konfig.dajPostavku("port"));
        try {
            ServerSocket ss = new ServerSocket(port);
            while (true) {
                Socket socket = ss.accept();
                stopSocket = socket;
                ObradaZahtjeva oz = dajSlobodnuDretvu(dretve, brojDretvi);
                
                //EVENT LISTENER

                if (oz == null) {
                    oz = new ObradaZahtjeva(tg, "ERROR");
                    oz.setPorukaGreske("ERROR 80; Nema slobodnih dretvi");
                    oz.setSocket(socket);
                    oz.start();

                } else {
                    brojacDretvi++;
                    oz.setStanje(ObradaZahtjeva.StanjeDretve.Zauzeta);
                    oz.setSocket(socket);
                    /*if(pauzaServera == 1){
                        oz.setPorukaGreske("ERROR 10; Server je pauziran");
                    }*/
                    oz.setPauzaServera(pauzaServera);
                    oz.slusac = this;
                    oz.setKonfig(konfig);

                    new Thread(oz).start();

                    /*if(!oz.isAlive())
                     {pauzaServera = oz.getPauzaServera();}*/
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void ucitajSerijaliziranuEvidenciju(String datoteka) {
        //TODO dovrisiti sami
    }

    private ObradaZahtjeva dajSlobodnuDretvu(ObradaZahtjeva[] dretve, int brojDretviMogucih) {
        //brojac za prolaz kroz sve dretve
        int provjeraZauzetihDretvi = 0;
        //brojac koji povecavam samo kad je dretva zauzeta
        int brojacZauzetihDretvi = 0;
        System.out.println("----------------------------------------------------------");
        while (provjeraZauzetihDretvi != brojDretviMogucih) {
            if (dretve[provjeraZauzetihDretvi].getStanje() == ObradaZahtjeva.StanjeDretve.Zauzeta) {
                System.out.println("Dretva broj " + provjeraZauzetihDretvi + " je zauzeta!");
                brojacZauzetihDretvi++;
            }
            provjeraZauzetihDretvi++;

            //ako je brojac zauzetih dretvi == broju mogucih onda vracam null
            if (brojacZauzetihDretvi == brojDretviMogucih) {
                return null;
            }
        }

        //ako je vanjski brojac == broju zadanih dretvi onda resetiraj brojac
        if (brojacDretvi == brojDretviMogucih) {
            brojacDretvi = 0;
        }

        //brojac sa kojim idem kroz dretve s time da pocinjem od sljedece koja bi bila na redu po vanjskom brojacu
        int slobodnaDretva = brojacDretvi;
        while (slobodnaDretva < brojDretviMogucih) {
            if (dretve[slobodnaDretva].getStanje() == ObradaZahtjeva.StanjeDretve.Slobodna) {
                System.out.println("Dretva broj " + slobodnaDretva + " je bila slobodna i sada ju zauzimamo! \nNaziv dretve : " + dretve[slobodnaDretva].getName());
                break;
            }
            slobodnaDretva++;
            if (slobodnaDretva == brojDretviMogucih) {
                slobodnaDretva = 0;
            }
        }
        System.out.println("----------------------------------------------------------");
        return dretve[slobodnaDretva];
    }

    @Override
    public void pauza(int p) {
        pauzaServera = p;
        if(p == 1)
        {
            System.out.println("Pauziranje servera.");
        }
        else if(p == 3)
        {
            try {
                System.exit(0);
                //TODO Serijaliziraj podatke ovdje
                
            } catch (Exception e) {              
                    ObradaZahtjeva oz = new ObradaZahtjeva(stopS, "ERROR");
                    oz.setPorukaGreske("ERROR 03; Greska kod prekida rada ili kod serijalizacije podataka");
                    oz.setSocket(stopSocket);
                    oz.start();
            }
            
        }
        else{
            System.out.println("Start servera.");}
        
    }

}
