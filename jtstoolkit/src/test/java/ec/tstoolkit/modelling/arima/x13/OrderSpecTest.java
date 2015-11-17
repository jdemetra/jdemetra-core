/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.information.InformationSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class OrderSpecTest {
    
    public OrderSpecTest() {
    }

    @Test
    public void testInformationSet() {
        OrderSpec expected = new OrderSpec(1, 2, OrderSpec.Type.Fixed);
        OrderSpec actual = new OrderSpec(2, 1, OrderSpec.Type.Max);
        InformationSet info;
        
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(OrderSpec.Type.Fixed, actual.type);
    }
    
}
