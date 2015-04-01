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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.thop.konfiguracije.Konfiguracija;
import org.foi.nwtis.thop.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.thop.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.thop.zadaca_1.EvidencijaModel.ZahtjevKorisnika;

/**
 * @author Tomislav Hop
 *
 * Klasa koja obrađuje sve zahtjeve koje server primi.
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
    HashMap<String, EvidencijaModel> hm = new HashMap<>();/* = /*new HashMap<String, EvidencijaModel>();*/

    private Date vrijemeDobivanjaKomande;
    public Slusac slusac;

    public ObradaZahtjeva(ThreadGroup group, String name) {
        super(group, name);
        this.stanje = StanjeDretve.Slobodna;
        //hm = new HashMap<>();
    }

    @Override
    public void interrupt() {
        super.interrupt();
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
                    vrijemeDobivanjaKomande = new Date();
                    break;
                }
                sb.append((char) znak);

            }

            String poruka = null;
            /**
             * Ako je poruka greške prazna nismo settali varijablu porukaGreške
             * što znači da je sve prošlo uredu na serveru
             */
            if (porukaGreske.equals("")) {
                mKomanda = provjeraParametara(sb.toString());
                /**
                 * Ako komanda poslana odgovara sintaksi regexa možemo ju
                 * obraditi
                 */
                if (mKomanda != null) {
                    /**
                     * Ako je komanda poslana TIME znamo da je zahtjev došao od
                     * korisnika pa provjeravamo ako je server pauziran i ako je
                     * šaljemo poruku greške
                     */
                    if (mKomanda.group(7).equals("TIME") && pauzaServera == 1) {
                        poruka = "ERROR 10; Server je pauziran";
                    }
                    /**
                     * Ako je komanda poslana TIME znamo da je zahtjev došao od
                     * korisnika pa provjeravamo ako je server nije pauziran pa
                     * ispisujemo primljenu poruku i šaljemo poruku sa potrebnim
                     * odgovorom
                     */
                    if (mKomanda.group(7).equals("TIME") && pauzaServera == 0) {
                        System.out.println("Primljena poruka: " + sb.toString() + "Dretva: " + this.getName());
                        DateFormat dateFormat = new SimpleDateFormat("YYYY.MM.dd hh:mm:ss");
                        Date date = new Date();
                        poruka = "OK;" + dateFormat.format(date) + "; Dretva: " + this.getName();
                        /**
                         * Ako komanda poslana nije TIME onda znamo da se radi o
                         * administratoru, šaljemo komandu koja je poslana nazad
                         * i izvršavamo funkcionalnost povezanu za poslanu
                         * komandu
                         */
                    } else if (!mKomanda.group(7).equals("TIME")) {
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
                                poruka = "ERROR 90; Komanda koju ste poslali nije ispravna";
                                break;
                        }
                    }
                } else {
                    poruka = "ERROR 10; Komanda koju ste poslali nije ispravna";
                }
            } else {
                poruka = porukaGreske;
            }
            /**
             * Slanje poruke
             */
            if (poruka != null) {
                os.write(poruka.getBytes());
                os.flush();
                DodavanjeZahtjeva(vrijemeDobivanjaKomande.toString(), socket.getInetAddress().toString(), sb.toString(), poruka);
            }
        } catch (IOException ex) {
            Logger.getLogger(ObradaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
        }

        /**
         * Zatvaranje inputstreama, outputstreama i socketa
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

        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ObradaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
        }

        /**
         * Došli smo do kraja runa pa postavljamo stanje dretve u slobodno i
         * provjeravamo ako je postavljena varijabla za pauziranje server i ako
         * je preko interfacea ju šaljemo serveru
         */
        this.stanje = StanjeDretve.Slobodna;
        if (stopServera == true) {
            slusac.pauza(3);
        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    /**
     * Svi setteri korišteni za ovu klasu
     */
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

    public void setHm(HashMap<String, EvidencijaModel> hm) {
        this.hm = hm;
    }

    /**
     *
     * @param vrijeme
     * @param ip
     * @param komanda
     * @param odgovor
     *
     * Metoda koja sprema zahtjeve u hashmapu
     */
    public void DodavanjeZahtjeva(String vrijeme, String ip, String komanda, String odgovor) {

        EvidencijaModel em = new EvidencijaModel(this.getName());
        EvidencijaModel.ZahtjevKorisnika z = em.new ZahtjevKorisnika(vrijeme, ip, komanda, odgovor);

        //EvidencijaModel.ZahtjevKorisnika z2 = em.new ZahtjevKorisnika("KURAC", "KURAC", "KURAC", "KURAC");
        for (Map.Entry<String, EvidencijaModel> m : hm.entrySet()) {
            System.out.println("ULAZ PETLJU ONU");
            EvidencijaModel ee = (EvidencijaModel) m.getValue();

            System.out.println("OZNAKA OD EE : " + ee.getOznaka());
            System.out.println("OZNAKA OD EM : " + em.getOznaka());

            if (ee.getOznaka().equals(em.getOznaka())) {
                System.out.println("ULAZ U EVIDENCIJA MODEL I DODAVANJE ZAHTJEVA U POSTOJECU");
                ArrayList<ZahtjevKorisnika> zahtjevi = ee.getZahtjevi();

                for (int i = 0; i < zahtjevi.size(); i++) {
                    System.out.println("DODAVANJE OPET ZAHTJEVA BROJ: " + i + zahtjevi.get(i).getOdgovor());
                    em.dodajZahtjev(zahtjevi.get(i));
                }
            }
        }

        try {
            System.out.println("DODAVANJE NOVOG ZAHTJEVA!!!!!!!!!!!!!" + z.getOdgovor());
            em.dodajZahtjev(z);
            System.err.println("NEMA GRESKE KOD DODAVANJA NOVOG ZAHTJEVA");
        } catch (Exception e) {
            System.err.println("GRESKA KOD DODAVANJA NOVOG ZAHTJEVA");
        }

        // em.dodajZahtjev(z2);
        em.setPrviZahtjev(null);
        em.setUkupanBrojZahtjeva(2);
        em.setUkupnoVrijemeRada(1333);
        em.setZadnjiZahtjev(null);

        try {
            hm.put(this.getName(), em);
            System.err.println("NEMA GRESKE KOD PUTANJA");

        } catch (Exception e) {
            System.err.println("GRESKA KOD PUTANJA");
        }

        System.out.println("HM SIZE: " + hm.size());

        slusac.spremiMapu(hm/*, this.getName()*/);

    }

    /**
     * @param p
     * @return
     *
     * Metoda koja provjerava parametre zadane putem komandne linija
     */
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

    /**
     * @return
     *
     * Metoda koja postavlja pauzu na 1 što znači da je server pauziran ili
     * javlja grešku ako već pauziran ili ako podatci za admina nisu ispravni
     */
    private String pauziranjeServera() {
        String poruka = null;
        if (provjeraLozinke() && pauzaServera == 0) {
            slusac.pauza(1);
            poruka = "OK";
        } else if (provjeraLozinke() && pauzaServera == 1) {
            poruka = "ERROR 01; Server je vec u stanju pauze";
        } else {
            poruka = "ERROR 00; Korisnicko ime ili lozinka nisu ispravni";
        }
        return poruka;
    }

    /**
     * @return
     *
     * Metoda koja postavlja pauzu na 0 što znači da server nije više pauziran
     * ili javlja grešku kako server nije ni pauziran ili javlja kako korisnički
     * podatci za admina nisu točni
     */
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

    /**
     * @return
     *
     * Metoda koja zaustavlja postavlja poruku na OK ako su korisnički podatci
     * dobri ili javlja grešku. Na osnovi poruke koju vraća gore u kodu
     * postavljamo vrijednost stopServer na true.
     */
    private String stopServera() {
        String poruka = null;
        if (provjeraLozinke()) {
            poruka = "OK";
        } else {
            poruka = "ERROR 00; Korisnicko ime ili lozinka nisu ispravni";
        }
        return poruka;
    }

    /**
     * @return
     *
     * Metoda koja provjerava korisničke podatke od admina koji se nalaze u
     * datoteci definiranoj u konfiguraciji
     */
    private boolean provjeraLozinke() {
        String lozinka = mKomanda.group(1).substring(mKomanda.group(1).lastIndexOf(" ") + 1);
        String korisnickoIme = mKomanda.group(1).split(";")[0];
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
