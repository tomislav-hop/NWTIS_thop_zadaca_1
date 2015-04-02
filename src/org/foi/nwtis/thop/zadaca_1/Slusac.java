/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.zadaca_1;

import java.util.HashMap;

/**
 * @author Tomislav Hop
 *
 * Interface koji koristim kako bi implementirao funkciju pauza u serveru u koju
 * šaljem jednu vrijednost iz obrade zahtjeva
 */
public interface Slusac {

    public void pauza(int p);
    public void spremiMapu(HashMap<String, EvidencijaModel> mapa);

}
