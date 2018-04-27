/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ITsVariableTest {
    
    public ITsVariableTest() {
    }

    @Test
    public void testName() {
        String name="var";
        for (int i=0; i<20; ++i){
            name=ITsVariable.nextName(name);
        }
        assertTrue(name.equals("var(20)"));
    }
    
}
