/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.algorithm;

import ec.tstoolkit.information.InformationSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class CompositeSpecificationTest {
    
    public CompositeSpecificationTest() {
    }

    @Test
    public void testInformationSet() {
        CompositeSpecification expected = new CompositeSpecification();
        CompositeSpecification actual = new CompositeSpecification();
        InformationSet info;
        assertEquals(expected.nodesSet().size(), actual.nodesSet().size());
        assertEquals(expected, actual);
        
        CompositeSpecification.Node n = new CompositeSpecification.Node(IProcSpecification.EMPTY, "node1");
        expected.add("myNode", n);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
    }
    
}
