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
public class ArItem2Test {
    
    public ArItem2Test() {
    }

    @Test
    public void testDim() {
        ArItem2 item=new ArItem2("", new double[]{.1, .1, .1}, true, 1, true, 5, 7);
        int np = item.parametersCount();
        DataBlock p=DataBlock.make(np);
        p.set(.1);
        
        StateComponent s = item.build(p);
        int dim=s.dim();
        assertTrue(dim==item.stateDim());
    }
    
    @Test
    public void testDim2() {
        ArItem2 item=new ArItem2("", new double[]{.1, .1, .1}, true, 1, true, 1, 0);
        int np = item.parametersCount();
        DataBlock p=DataBlock.make(np);
        p.set(.1);
        
        StateComponent s = item.build(p);
        int dim=s.dim();
        assertTrue(dim==item.stateDim());
    }
    
    @Test
    public void testDim3() {
        ArItem2 item=new ArItem2("", new double[]{.1, .1, .1}, true, 1, true, 0, 3);
        int np = item.parametersCount();
        DataBlock p=DataBlock.make(np);
        p.set(.1);
        
        StateComponent s = item.build(p);
        int dim=s.dim();
        assertTrue(dim==item.stateDim());
    }
}
