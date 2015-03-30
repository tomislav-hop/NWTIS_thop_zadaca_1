/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.zadaca_1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.thop.konfiguracije.Konfiguracija;

/**
 *
 * @author NWTiS_3
 */
public class SlanjeZahtjeva extends Thread {

    /**
     * sve varijable koje settam kod pokretanja dretve
     */
    private Konfiguracija konfig;
    private String server;
    private int port;
    private String korisnik;
    private String lozinka;
    private int brojPonavljanja;
    private int cekaj;
    private String dobivenaKomanda;
    private int brojPokusajaProblema;
    private int pauzaProblema;
    private int intervalDretve;
    private boolean ovoJeAdmin = false;

    @Override
    public void interrupt() {
        super.interrupt();
    }

    @Override
    public void run() {
        InputStream is = null;
        OutputStream os = null;
        Socket socket = null;
        boolean spavanje = false;
        boolean porukaPrimljena = false;

        //Ponovi slanje koliko puta je zadano
        for (int i = 0; i < brojPonavljanja; i++) {
            DateFormat dateFormat = new SimpleDateFormat("YYYY.MM.dd hh:mm:ss");
            Date pocetnoVrijeme = new Date();

            int brojNeuspjelihPokusaja = 0;

            try {
                TimeUnit.SECONDS.sleep(cekaj);
            } catch (InterruptedException ex) {
                Logger.getLogger(SlanjeZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
            }

            while (true) {
                //System.out.println("PROLAZ BROJ: " + brojNeuspjelihPokusaja);

                spavanje = false;
                try {

                    socket = new Socket(server, port);
                    os = socket.getOutputStream();
                    is = socket.getInputStream();
                    String komanda = "";

                    if (lozinka.isEmpty() && dobivenaKomanda.isEmpty()) {
                        komanda = "USER " + korisnik + ";TIME;";
                    } else {
                        komanda = "USER " + korisnik + "; PASSWD " + lozinka + ";" + dobivenaKomanda + ";";

                    }
                    os.write(komanda.getBytes());
                    os.flush();
                    socket.shutdownOutput();

                    StringBuilder sb = new StringBuilder();
                    while (true) {
                        int znak = is.read();
                        if (znak == -1) {
                            porukaPrimljena = true;
                            break;
                        }
                        sb.append((char) znak);

                    }

                    System.out.println("\nPrimljena poruka:\n----------------------------------------------------------\n" + sb.toString());

                } catch (IOException ex) {
                    Logger.getLogger(SlanjeZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
                    brojNeuspjelihPokusaja++;
                    spavanje = true;
                }
                /**
                 * zatvaranje input steama, output streama i socketa
                 */
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

                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        Logger.getLogger(ObradaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                /**
                 * Ako je postavljeno spavanje množim pauzu sa brojem neuspjelih
                 * pokusaja kako bi se vrijeme cekanja povecalo kao u zadatku
                 */
                if (spavanje) {
                    int duzinaSpavanja = brojNeuspjelihPokusaja * pauzaProblema;
                    System.out.println("SPAVANJE NA " + duzinaSpavanja + " SEKUNDI");
                    //if(duzinaSpavanja<=)
                    //TODO PROVJERI KAK SE OVO TREBA PONASATI

                    /*if (duzinaSpavanja > intervalDretve) {

                     intervalDretve = intervalDretve * 2;
                     }*/
                    try {
                        TimeUnit.SECONDS.sleep(duzinaSpavanja);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SlanjeZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                /**
                 * ako je broj neuspjelih pokusaja jednak maksimalnom broju
                 * pokusaja mozemo cekati ili preskakati cikluse na osnovi
                 * podatak koje je vratila funkcija intervalFix te nakon toga
                 * izaći iz petlje
                 */
                if (brojNeuspjelihPokusaja == brojPokusajaProblema && ovoJeAdmin == false) {
                    Date zavrsnoVrijeme = new Date();
                    i = i + intervalFix(zavrsnoVrijeme, pocetnoVrijeme, brojNeuspjelihPokusaja);

                    System.out.println("i = " + i);
                    System.out.println("Server ne odgovara! Prekid rada.");
                    break;
                }

                /**
                 * Ako je dobivena povratna poruka od servera onda smo postavili
                 * porukaPrimljena na true pa mozemo postaviti broj neuspjelih
                 * pokusaja na 0 te cekati ili preskakati cikluse na osnovi
                 * podatak koje je vratila funkcija intervalFix te nakon toga
                 * izaći iz petlje
                 */
                if (porukaPrimljena) {
                    Date zavrsnoVrijeme = new Date();
                    brojNeuspjelihPokusaja = 0;
                    i = i + intervalFix(zavrsnoVrijeme, pocetnoVrijeme, brojNeuspjelihPokusaja);
                    System.out.println("i = " + i);
                    //System.out.println("POCETNO VRIJEME JE: " + dateFormat.format(zavrsnoVrijeme));
                    break;
                }

                /**
                 * Ako smo imali spavanje opet popravljamo interval as funkcijom
                 * kako bi imali imali dobro trajanje ciklusa
                 */
                if (spavanje) {
                    //System.out.println("DRUGO SPAVANJE PROSLO I sad NADODKADI");
                    Date zavrsnoVrijeme = new Date();
                    int interval = intervalFix(zavrsnoVrijeme, pocetnoVrijeme, brojNeuspjelihPokusaja);
                    i = i + interval;
                    //System.out.println("INTERVAL ONAJ JE: " + interval);
                    System.out.println("i = " + i);
                }
            }

        }

    }

    /**
     *
     * @param zavrsnoVrijeme
     * @param pocetnoVrijeme
     * @return
     *
     * Ako ja trajanje dretve manje od intervala funkcija ceka do intervala i
     * vraca 0 kako ne bi promjenili broj ciklusa ako je vrijeme vece od
     * intervala onda racunamo koliko se ciklusa preskace i vraca taj broj kako
     * bi povecali brojac ciklusa za taj broj
     */
    public int intervalFix(Date zavrsnoVrijeme, Date pocetnoVrijeme, int neuspjeha) {
        if (ovoJeAdmin) {
            return 0;
        }
        int a = 0;
        long razlikaVremena = zavrsnoVrijeme.getTime() - pocetnoVrijeme.getTime();
        //System.out.println("Trajalo je: " + razlikaVremena);
        //System.out.println("INTERVAL DRETVE: " + intervalDretve);

        if (razlikaVremena < intervalDretve * 1000) {
            long pricekajJos = intervalDretve * 1000 - razlikaVremena;
            System.out.println("PRICEKAJ JOS: " + pricekajJos);
            try {
                sleep(pricekajJos);
                return a;
            } catch (InterruptedException ex) {
                Logger.getLogger(SlanjeZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (neuspjeha > 0 || razlikaVremena > intervalDretve * 1000) {

            System.out.println("TRAJANJE DRETVE DO SAD JE : " + razlikaVremena);
            float kolikoPutaJePreslo = (float) razlikaVremena / (float) (intervalDretve * 1000);

            //System.out.println("OVO JE FLOAT: " + razlikaVremena + " / " + (intervalDretve * 1000) + " = " + kolikoPutaJePreslo);
            a = (int) Math.ceil(kolikoPutaJePreslo);
            //System.out.println("OVO JE NEKI BROJ: " + a);
            long pricekajJos = (intervalDretve * 1000 * a) - razlikaVremena;
            System.out.println("PRICEKAJ JOS ZBOG PAUZE: " + pricekajJos + " ms");
            try {
                sleep(pricekajJos);
                //return 0;
            } catch (InterruptedException ex) {
                Logger.getLogger(SlanjeZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println("Trajanje dretve je predugacko preskakanje " + (a - 1) + " ciklusa");
            return a - 1;
        }
        return a;
    }

    /**
     * Svi setteri koje koristim za dretvu
     */
    @Override
    public synchronized void start() {
        super.start();
    }

    public void setKonfig(Konfiguracija konfig) {
        this.konfig = konfig;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setKorisnik(String korisnik) {
        this.korisnik = korisnik;
    }

    public void setBrojPonavljanja(int brojPonavljanja) {
        this.brojPonavljanja = brojPonavljanja;
    }

    public void setCekaj(int cekaj) {
        this.cekaj = cekaj;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public void setDobivenaKomanda(String dobivenaKomanda) {
        this.dobivenaKomanda = dobivenaKomanda;
    }

    public void setBrojPokusajaProblema(int brojPokusajaProblema) {
        this.brojPokusajaProblema = brojPokusajaProblema;
    }

    public void setPauzaProblema(int pauzaProblema) {
        this.pauzaProblema = pauzaProblema;
    }

    public void setIntervalDretve(int intervalDretve) {
        this.intervalDretve = intervalDretve;
    }

    public void setOvoJeAdmin(boolean ovoJeAdmin) {
        this.ovoJeAdmin = ovoJeAdmin;
    }

}
