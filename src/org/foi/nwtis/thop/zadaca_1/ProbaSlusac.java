/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.zadaca_1;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author Tomislav Hop
 */
public class ProbaSlusac implements PropertyChangeListener {

	public void propertyChange(PropertyChangeEvent evt) {
		System.out.println("Varijabla: " + evt.getPropertyName() + 
			" se promijenila iz: " + evt.getOldValue() + " u: " + evt.getNewValue());
  }
        
}
