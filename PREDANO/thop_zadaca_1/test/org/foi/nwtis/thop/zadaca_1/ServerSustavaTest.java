/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.zadaca_1;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author NWTiS_3
 */
public class ServerSustavaTest {

    public ServerSustavaTest() {
    }

    /**
     * Test of provjeraParametara method, of class ServerSustava.
     */
    @Test
    public void testProvjeraParametara() {
        System.out.println("provjeraParametara");
        String p = "-server";
        ServerSustava instance = null;

        try {
            instance = new ServerSustava(p);
            Matcher expResult = null;
            Matcher result = instance.provjeraParametara(p);
            
            p="-server -konf NWTiS_thop_1.txt";
            result = instance.provjeraParametara(p);
            assertNotNull(result);
            
            p="-server -konf NWTiS_thop_1.txt -load";
            result = instance.provjeraParametara(p);
            assertNotNull(result);
            
        } catch (Exception ex) {
            Logger.getLogger(ServerSustavaTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
