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

    @Override
    public void interrupt() {
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
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
            //System.out.println("POCETNO VRIJEME JE: " + dateFormat.format(pocetnoVrijeme));

            //TESTIRANJE TAJMERA
            /*try {
             TimeUnit.SECONDS.sleep(10);
             } catch (InterruptedException ex) {
             Logger.getLogger(SlanjeZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
             }*/
            //System.out.println("Ponavljanje broj: " + i);
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
                if (brojNeuspjelihPokusaja == brojPokusajaProblema) {
                    Date zavrsnoVrijeme = new Date();
                    intervalFix(zavrsnoVrijeme, pocetnoVrijeme);
                    //System.out.println("POCETNO VRIJEME JE: " + dateFormat.format(zavrsnoVrijeme));
                    System.out.println("Server ne odgovara! Prekid rada.");
                    break;
                }
                
                 if (porukaPrimljena) {
                    Date zavrsnoVrijeme = new Date();
                    brojNeuspjelihPokusaja=0;
                    if(intervalFix(zavrsnoVrijeme, pocetnoVrijeme))
                    {
                        brojPonavljanja++;
                    }
                    //System.out.println("POCETNO VRIJEME JE: " + dateFormat.format(zavrsnoVrijeme));
                    break;
                }

                if (spavanje) {
                    int duzinaSpavanja = (brojNeuspjelihPokusaja + 1) * pauzaProblema;
                    //if(duzinaSpavanja<=)
                    //TODO PROVJERI KAK SE OVO TREBA PONASATI
                    
                    if(duzinaSpavanja > intervalDretve)
                    {
                        intervalDretve = intervalDretve*2;
                    }
                    
                    try {
                        TimeUnit.SECONDS.sleep(duzinaSpavanja);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SlanjeZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

    }

    //Funkcija koja vraća false ako je trajanje dretve manje od intervala dretve i sleepa do intervala dretve kako bi svaki ciklus dovoljno dugo trajao
    //Ako je trajanje dretve duze onda vraca true i povecavamo broj ponavljanja za jedan gore u kodu kako bi smanjili broj ciklusa
    public boolean intervalFix(Date zavrsnoVrijeme, Date pocetnoVrijeme) {
        long razlikaVremena = zavrsnoVrijeme.getTime() - pocetnoVrijeme.getTime();
                    //System.out.println("Trajalo je: " + razlikaVremena);
        //System.out.println("INTERVAL DRETVE: " + intervalDretve);

        if (razlikaVremena < intervalDretve * 1000) {
            long pricekajJos = intervalDretve * 1000 - razlikaVremena;
            //System.out.println("PRICEKAJ JOS: " + pricekajJos);
            try {
                sleep(pricekajJos);
                return false;
            } catch (InterruptedException ex) {
                Logger.getLogger(SlanjeZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else if(razlikaVremena > intervalDretve * 1000)
        {
            System.out.println("Trajanje dretve je predugacko preskakanje jednog ciklusa");
            return true;
        }
        return false;
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

    public void setBrojPokusajaProblema(int brojPokusajaProblema) {
        this.brojPokusajaProblema = brojPokusajaProblema;
    }

    public void setPauzaProblema(int pauzaProblema) {
        this.pauzaProblema = pauzaProblema;
    }

    public void setIntervalDretve(int intervalDretve) {
        this.intervalDretve = intervalDretve;
    }

}
