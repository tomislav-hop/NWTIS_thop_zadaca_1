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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.thop.konfiguracije.Konfiguracija;

/**
 *
 * @author NWTiS_3
 */
public class SlanjeZahtjeva extends Thread {

    private Konfiguracija konfig;
    private String server;
    private int port;
    private String korisnik;
    private String lozinka;
    private int brojPonavljanja;
    private int cekaj;
    private String dobivenaKomanda;
    private int brojPokusaja;
    private int vrijemeSpavanja;

    @Override
    public void interrupt() {
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        InputStream is = null;
        OutputStream os = null;
        Socket socket = null;
        int brojNeuspjelihPokusaja;
        boolean spavanje = false;

        //Ponovi slanje koliko puta je zadano
        for (int i = 0; i < brojPonavljanja; i++) {

            brojNeuspjelihPokusaja = 0;
            System.out.println("Ponavljanje broj: " + i);

            while (true) {
                if(brojNeuspjelihPokusaja > 0){
                System.out.println("brojNeuspjelihPokusajaj: " + brojNeuspjelihPokusaja);
                }
                spavanje = false;
                try {
                    TimeUnit.SECONDS.sleep(cekaj);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SlanjeZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
                }
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
                            break;
                        }
                        sb.append((char) znak);
                    }

                    System.out.println(sb.toString());

                } catch (IOException ex) {
                    Logger.getLogger(SlanjeZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
                    brojNeuspjelihPokusaja++;
                    spavanje = true;
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

                if (socket != null) {
                    try {
                        socket.close();
                        //this.stanje = StanjeDretve.Slobodna;
                    } catch (IOException ex) {
                        Logger.getLogger(ObradaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (brojNeuspjelihPokusaja == brojPokusaja) {
                    break;
                }
                if (spavanje) {
                    try {
                        TimeUnit.SECONDS.sleep(vrijemeSpavanja);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SlanjeZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
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

    public void setBrojPokusaja(int brojPokusaja) {
        this.brojPokusaja = brojPokusaja;
    }

    public void setVrijemeSpavanja(int vrijemeSpavanja) {
        this.vrijemeSpavanja = vrijemeSpavanja;
    }

}
