/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling;

import ec.tstoolkit.information.InformationSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class TsVariableDescriptorTest {
    
    public TsVariableDescriptorTest() {
    }

    @Test
    public void testInformationSet() {
        TsVariableDescriptor expected = new TsVariableDescriptor("test");
        TsVariableDescriptor actual = new TsVariableDescriptor("test");
        InformationSet info;
        assertEquals(expected, actual);
        expected.setEffect(TsVariableDescriptor.UserComponentType.Trend);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(TsVariableDescriptor.UserComponentType.Trend, actual.getEffect());
        
        expected = new TsVariableDescriptor("test");
        actual = new TsVariableDescriptor("test");
        expected.setLastLag(2);
        expected.setFirstLag(1);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1, actual.getFirstLag());
        assertEquals(2, actual.getLastLag());
        
    }
    
}
