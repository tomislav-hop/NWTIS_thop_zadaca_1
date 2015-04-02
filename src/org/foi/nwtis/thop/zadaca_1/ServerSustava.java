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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
 * Klasa koja funkcionira kao socket server. Na osnovi konfiguracijske datoteke
 * pokreće server i prima zahtjeve na portu.
 */
public class ServerSustava implements Slusac {

    protected String parametri;
    protected Matcher mParametri;
    int brojacDretvi = 0;
    private int pauzaServera = 0;
    private ThreadGroup stopS;
    private Socket stopSocket;
    Konfiguracija konfig = null;
    private Evidencija e;
    SerijalizatorEvidencije se;
    HashMap<String, EvidencijaModel> hm = new HashMap<>();

    public ServerSustava(String parametri) throws Exception {
        this.parametri = parametri;
        mParametri = provjeraParametara(parametri);
        if (mParametri == null) {
            throw new Exception("Parametri servera ne odgovaraju");
        }
    }

    /**
     * @param p
     * @return
     *
     * Metoda koja provjerava parametre zadane putem komandne linija
     */
    public Matcher provjeraParametara(String p) {
        String sintaksa = "^-server -konf +([^\\s]+.(txt|xml))( -load)?$";
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
     * Metoda koja pokreće server na osnovi podataka iz konfiguracije te za
     * svaki primljeni zahtjev pokreće dretvu koja će obraditi taj zahtjev
     */
    public void pokreniServer() {

        String datoteka = mParametri.group(1);
        File dat = new File(datoteka);

        if (!dat.exists()) {
            System.out.println("Datoteka konfiguracije ne postoji!");
            return;
        }
        konfig = null;
        try {
            konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);

            if (this.mParametri.group(3) != null) {
                ucitajSerijaliziranuEvidenciju();
            }
        } catch (NemaKonfiguracije ex) {
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        se = new SerijalizatorEvidencije(konfig);
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

                /**
                 * Ako je funkcija dajSlobodnuDretvu vratila null onda šaljemo
                 * grešku klijentu, a ako nije instanciramo novu dretvu koja će
                 * obraditi taj zahtjev
                 */
                if (oz == null) {
                    oz = new ObradaZahtjeva(tg, "ERROR");
                    oz.setPorukaGreske("ERROR 80; Nema slobodnih dretvi");
                    oz.setSocket(socket);
                    oz.start();
                } else {
                    brojacDretvi++;
                    oz.setStanje(ObradaZahtjeva.StanjeDretve.Zauzeta);
                    oz.setSocket(socket);
                    oz.setPauzaServera(pauzaServera);
                    oz.slusac = this;
                    oz.setKonfig(konfig);
                    oz.setHm(hm);
                    new Thread(oz).start();
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param datoteka
     *
     * Prvo provjerimo ako datoteka postoji te ako postoji više dohvaćamo zadnju
     * kreiranu pa metoda pokreće citaj mapu iz klase Evidencija
     */
    private void ucitajSerijaliziranuEvidenciju() {

        String datoteka = getLatestFilefromDir();
        System.out.println("Ucitavanje serijalizirane datoteke");
        File dat = new File(datoteka);
        if (!dat.exists()) {
            System.out.println("Serijalizirana datoteka ne postoji!");
            return;
        } else {
            Evidencija e = new Evidencija(datoteka);
            e.citajHashMapu(datoteka);
        }
    }

    /**
     * @return
     *
     * Metoda koja vraća zadnju kreiranu datoteku koja počinje sa stringom iz
     * konfig datoteke
     */
    private String getLatestFilefromDir() {
        File direktorij = new File(System.getProperty("user.dir"));
        String evD = konfig.dajPostavku("evidDatoteka");
        evD = evD.split("\\.")[0];

        File najnoviji = null;
        int i = 0;

        for (File f : direktorij.listFiles()) {
            if (f.getName().startsWith(evD)) {
                if (i == 0) {
                    najnoviji = f;
                }
                i = 1;
                if (najnoviji.lastModified() < f.lastModified()) {
                    najnoviji = f;
                }
            }
        }
        System.out.println("Zadnja datoteka evidencije je: " + najnoviji.getName());
        return najnoviji.getName();
    }

    /**
     * @param dretve
     * @param brojDretviMogucih
     * @return
     *
     * Metoda koja vraća dretvu koja se slobodna u kružnom redoslijedu
     */
    private ObradaZahtjeva dajSlobodnuDretvu(ObradaZahtjeva[] dretve, int brojDretviMogucih) {
        /**
         * brojac za prolaz kroz sve dretve
         */
        int provjeraZauzetihDretvi = 0;
        /**
         * brojac koji povecavam samo kad je dretva zauzeta
         */
        int brojacZauzetihDretvi = 0;
        System.out.println("----------------------------------------------------------");
        while (provjeraZauzetihDretvi != brojDretviMogucih) {
            if (dretve[provjeraZauzetihDretvi].getStanje() == ObradaZahtjeva.StanjeDretve.Zauzeta) {
                System.out.println("Dretva broj " + provjeraZauzetihDretvi + " je zauzeta!");
                brojacZauzetihDretvi++;
            }
            provjeraZauzetihDretvi++;
            /**
             * ako je brojac zauzetih dretvi == broju mogucih onda vracam null
             */
            if (brojacZauzetihDretvi == brojDretviMogucih) {
                return null;
            }
        }
        /**
         * ako je vanjski brojac == broju zadanih dretvi onda resetiraj brojac
         */
        if (brojacDretvi == brojDretviMogucih) {
            brojacDretvi = 0;
        }
        /**
         * brojac sa kojim idem kroz dretve s time da pocinjem od sljedece koja
         * bi bila na redu po vanjskom brojacu
         */
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

    /**
     * @param p
     *
     * Implementirana metoda iz slušača koja postavlja varijablu pauzaServera na
     * vrijednost koja je poslana iz obrade zahtjeva i ako se server stopira
     * onda serijalizira podatke za kraj
     */
    @Override
    public void pauza(int p) {
        pauzaServera = p;
        if (p == 1) {
            System.out.println("Pauziranje servera.");
        } else if (p == 3) {
            try {
                String nazivDat = konfig.dajPostavku("evidDatoteka");
                Evidencija evid = new Evidencija(nazivDat);
                DateFormat dateFormat = new SimpleDateFormat("YYYYMMdd_hhmmss");
                Date date = new Date();
                String datum = dateFormat.format(date);
                evid.spremiHashMapu(hm, datum, nazivDat);
                System.exit(0);
            } catch (Exception e) {
                ObradaZahtjeva oz = new ObradaZahtjeva(stopS, "ERROR");
                oz.setPorukaGreske("ERROR 03; Greska kod prekida rada ili kod serijalizacije podataka");
                oz.setSocket(stopSocket);
                oz.start();
            }
        } else {
            System.out.println("Start servera.");
        }
    }

    @Override
    public void spremiMapu(HashMap<String, EvidencijaModel> mapa) {
        hm = mapa;
        se.setMapa(hm);
    }

}
