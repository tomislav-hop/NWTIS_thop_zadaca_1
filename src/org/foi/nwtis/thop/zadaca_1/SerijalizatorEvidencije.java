/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.zadaca_1;

import org.foi.nwtis.thop.konfiguracije.Konfiguracija;

/**
 *
 * @author NWTiS_3
 * test
 */
public class SerijalizatorEvidencije extends Thread{
    
    Konfiguracija konfig;

    public SerijalizatorEvidencije(Konfiguracija konfig) {
        super();
        this.konfig = konfig;
    }

    @Override
    public void interrupt() {
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        //TODO dovrsiti za serijalizaciju evidencije
    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

    
    
    
}
