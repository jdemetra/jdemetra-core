/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import jdplus.data.DataBlock;
import jdplus.ssf.StateComponent;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class ArItemTest {
    
    public ArItemTest() {
    }

    @Test
    public void testDim() {
        ArItem item=new ArItem("", new double[]{.1, .1, .1}, true, 1, true, 0, false);
        int np = item.parametersCount();
        DataBlock p=DataBlock.make(np);
        p.set(.1);
        
        StateComponent s = item.build(p);
        int dim=s.dim();
        assertTrue(dim==item.stateDim());
    }
    
    @Test
    public void testDim2() {
        ArItem item=new ArItem("", new double[]{.1, .1, .1}, true, 1, true, 6, false);
        int np = item.parametersCount();
        DataBlock p=DataBlock.make(np);
        p.set(.1);
        
        StateComponent s = item.build(p);
        int dim=s.dim();
        assertTrue(dim==item.stateDim());
    }
}
