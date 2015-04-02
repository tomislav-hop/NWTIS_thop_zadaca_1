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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
                                poruka = startClean();
                                break;
                            case "STAT":
                                //poruka = startStat();
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
     * Metoda koja hvata argumenete potrebne za spremanje, broji zahtjeve i
     * racuna vrijeme rada
     */
    public void DodavanjeZahtjeva(String vrijeme, String ip, String komanda, String odgovor) {
        int ukupanBrojZatjeva = 0;
        String vrijemePrvogZahtjeva = null;
        String vrijemeDrugogZahtjeva = null;
        EvidencijaModel em = new EvidencijaModel(this.getName());
        EvidencijaModel.ZahtjevKorisnika z = em.new ZahtjevKorisnika(vrijeme, ip, komanda, odgovor);
        /**
         * Prolazim kroz hashmapu i kroz svaki njezin EvidencijaModel i gledam
         * ako je oznaka tog EvidencijaModela jednaka novom gore kreiranom
         * EvidencijaModelu kopiram sve stare zahtjeve u novi i kasnije ubacujem
         * i novi zahtjev unutar novog EvidencijaModela
         */
        for (Map.Entry<String, EvidencijaModel> m : hm.entrySet()) {
            EvidencijaModel ee = (EvidencijaModel) m.getValue();
            if (ee.getOznaka().equals(em.getOznaka())) {
                ArrayList<ZahtjevKorisnika> zahtjevi = ee.getZahtjevi();
                ukupanBrojZatjeva = zahtjevi.size();
                /**
                 * Ako je zahtjev na indeksu 0 onda to vrijeme zapisujem u
                 * vrijemePrvogZahtjeva, a ako je na zadnjem onda to vrijeme
                 * upisujem u vrijemeDrugogZahtjeva
                 */
                for (int i = 0; i < zahtjevi.size(); i++) {
                    if (i == 0) {
                        vrijemePrvogZahtjeva = zahtjevi.get(i).getVrijeme();
                    }
                    if (i > 0 && i == zahtjevi.size() - 1) {
                        vrijemeDrugogZahtjeva = zahtjevi.get(i).getVrijeme();
                    }
                    em.dodajZahtjev(zahtjevi.get(i));
                }
            }
        }
        /**
         * Dodavanje novog zahtjeva u EvidencijaModel
         */
        try {
            em.dodajZahtjev(z);
            ukupanBrojZatjeva++;

        } catch (Exception e) {
            System.err.println("GRESKA KOD DODAVANJA NOVOG ZAHTJEVA");
        }
        /**
         * Postavljanje varijabli od EvidencijaModel
         */

        if (vrijemeDrugogZahtjeva == null) {
            em.setPrviZahtjev(popravljanjeDatuma(vrijeme));
            em.setZadnjiZahtjev(popravljanjeDatuma(vrijeme));
        } else {
            em.setPrviZahtjev(popravljanjeDatuma(vrijemePrvogZahtjeva));
            em.setZadnjiZahtjev(popravljanjeDatuma(vrijemeDrugogZahtjeva));
        }

        em.setUkupanBrojZahtjeva(ukupanBrojZatjeva);
        /**
         * Izračun rada dretve
         */
        int vrijemeRada = ukupanBrojZatjeva * Integer.parseInt(konfig.dajPostavku("intervalDretve"));
        em.setUkupnoVrijemeRada(vrijemeRada);

        /**
         * stavljanje novog EvidencijaModela unutar hashmape
         */
        try {
            hm.put(this.getName(), em);

        } catch (Exception e) {
            System.err.println("GRESKA KOD .put");
        }
        slusac.spremiMapu(hm);
    }

    /**
     * @param datum
     * @return
     *
     * Zbog exceptiona u parsiranju datuma morao sam napraviti novi dateformat
     * sa kojim mogu citati vrijeme zahtjeva koje dobivam
     */
    public Date popravljanjeDatuma(String datum) {
        String dateStr = datum;
        DateFormat readFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
        Date date = null;
        try {
            date = readFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * @param p
     * @return
     *
     * Metoda koja provjerava parametre zadane putem komandne linija
     */
    public Matcher provjeraParametara(String p) {
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
     * Nezavršena metoda
     *
     * @return
     */
    private String startStat() {
        String poruka = null;
        File direktorij = new File(System.getProperty("user.dir"));
        String evD = konfig.dajPostavku("evidDatoteka");
        int brojDretvi = Integer.parseInt(konfig.dajPostavku("brojDretvi"));
        for (File f : direktorij.listFiles()) {
            System.out.println(f.getName());
            int[][] poljeZaStat = new int[brojDretvi][2];
            if (f.getName().startsWith(evD)) {
                //TODO otvorit svaki file i dodati za svaku dretvu broj i vrijeme trajanja u poljeZaStat
            }
        }
        return poruka;
    }

    /**
     * @return
     *
     * Metoda koja brise sve file-ove koji počinju sa stringom koji se nalazi u
     * konfig datoteci
     */
    private String startClean() {
        String poruka = null;
        try {
            File direktorij = new File(System.getProperty("user.dir"));
            String evD = konfig.dajPostavku("evidDatoteka");
            evD = evD.split("\\.")[0];
            for (File f : direktorij.listFiles()) {
                System.out.println(f.getName());
                if (f.getName().startsWith(evD)) {
                    f.delete();
                }
            }
            poruka = "OK";
        } catch (Exception e) {
            poruka = "ERROR 04; Greška kod brisanja evidencije";
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
