/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.zadaca_1;

import java.util.regex.Matcher;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author NWTiS_3
 */
public class Zadaca_thop_1Test {
    
    public Zadaca_thop_1Test() {
    }

    /**
     * Test of main method, of class Zadaca_thop_1.
     */
    @Ignore @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        Zadaca_thop_1.main(args);
        fail("The test case is a prototype.");
    }

    /**
     * Test of provjeraParametara method, of class Zadaca_thop_1.
     */
    @Test
    public void testProvjeraParametara() {
        System.out.println("provjeraParametara");
        String p = "-pero";
        Zadaca_thop_1 instance = new Zadaca_thop_1();
        Matcher expResult = null;
        Matcher result = instance.provjeraParametara(p);
        assertNull(result);
        
        p= "-server";
        result = instance.provjeraParametara(p);
        assertNull(result);
        
        p="-server -konf NWTiS_thop_1.txt";
        result = instance.provjeraParametara(p);
        assertNotNull(result);
        
        p="-admin -konf NWTiS_thop_1.txt";
        result = instance.provjeraParametara(p);
        assertNotNull(result);

    }
    
}
