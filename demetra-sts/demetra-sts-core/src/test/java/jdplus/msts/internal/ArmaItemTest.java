/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import jdplus.data.DataBlock;
import jdplus.ssf.StateComponent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class ArmaItemTest {
    
    public ArmaItemTest() {
    }

    @Test
    public void testDim() {
        ArmaItem item = new ArmaItem("", new double[]{.1, .1, .1}, new double[]{.1, .1, .1}, 1, true);
        int np = item.parametersCount();
        DataBlock p = DataBlock.make(np);
        p.set(i->.011*i);

        StateComponent s = item.build(p);
        int dim = s.dim();
        assertTrue(dim == item.stateDim());
    }
}
