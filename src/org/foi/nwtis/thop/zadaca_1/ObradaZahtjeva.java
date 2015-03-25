/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.zadaca_1;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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

/**
 *
 * @author NWTiS_3
 */
public class ObradaZahtjeva extends Thread {

    //LISTENER ZA PAUZIRANJE SERVERA VARIJABLE
    public static final String PROP_SAMPLE_PROPERTY = "sampleProperty";
    private String sampleProperty;
    private PropertyChangeSupport propertySupport;
    ///////////////////////////////////////

    public enum StanjeDretve {
        
        Slobodna, Zauzeta
    };
    
    private Konfiguracija konfig;
    private Socket socket;
    private StanjeDretve stanje;
    private String porukaGreske = "";
    protected Matcher mKomanda;
    private int pauzaServera = 0;  
    
    public Slusac slusac;
    
    public ObradaZahtjeva(ThreadGroup group, String name) {
        super(group, name);
        this.stanje = StanjeDretve.Slobodna;

        //LISTENER
        propertySupport = new PropertyChangeSupport(this);
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
                    if (pauzaServera == 1) {
                        System.out.println("NESTO ULAZI U SERVER DOK JE PAUZIRAN!");
                        System.out.println("Sada treba javiti gresku klijentu za to sta je poslo dok je pauzirano");
                        
                    }
                    
                    if (mKomanda.group(7).equals("TIME") && pauzaServera == 0) {
                        System.out.println("Primljena poruka: " + sb.toString() + "Dretva: " + this.getName());
                        DateFormat dateFormat = new SimpleDateFormat("YYYY.MM.dd hh:mm:ss");
                        Date date = new Date();
                        poruka = "OK;" + dateFormat.format(date) + "; Dretva: " + this.getName();
                    } else {
                        //TODO AKO JE ADMIN SLAO ZAHTJEV
                        System.out.println("KOMANDA POSLANA JE: " + mKomanda.group(7));
                        poruka = "KOMANDA POSLANA JE: " + mKomanda.group(7);
                        if (mKomanda.group(7).equals("PAUSE")) {
                            System.out.println("SERVER PAUZIRAN!!!!");
                            
                            slusac.pauza(1);
                            this.setSampleProperty("PAUSE");
                            this.setPauzaServera(1);
                        }
                        else if (mKomanda.group(7).equals("START")) {
                            System.out.println("SERVER STARTA!!!!");
                            
                            slusac.pauza(0);
                            //this.setSampleProperty("PAUSE");
                            //this.setPauzaServera(1);
                        }

                        //this.
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

    //KOD ZA LISTENERA SA PREDAVANJA
    public String getSampleProperty() {
        return sampleProperty;
    }
    
    public void setSampleProperty(String value) {
        String oldValue = sampleProperty;
        sampleProperty = value;
        propertySupport.firePropertyChange(
                PROP_SAMPLE_PROPERTY, oldValue, sampleProperty);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
}
