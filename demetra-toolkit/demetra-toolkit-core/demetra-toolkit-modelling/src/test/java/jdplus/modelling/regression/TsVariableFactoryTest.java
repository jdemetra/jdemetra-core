/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.data.DoubleSeq;
import demetra.data.Doubles;
import demetra.timeseries.regression.UserVariable;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.lapack.FastMatrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TsVariableFactoryTest {
    
    public TsVariableFactoryTest() {
    }

    @Test
    public void testLarger() {
        DoubleSeq x = DoubleSeq.onMapping(24, i->i+1);
        TsData s=TsData.of(TsPeriod.monthly(2000, 1), Doubles.of(x));
        UserVariable var=new UserVariable("test", s);
        Matrix M=Matrix.make(12, 1);
        TsVariableFactory.FACTORY.fill(var, TsPeriod.monthly(2000, 3), M);
        assertTrue(M.get(0, 0)==3);
        assertTrue(M.get(11, 0)==14);
    }
    
    @Test
    public void testInside() {
        DoubleSeq x = DoubleSeq.onMapping(24, i->i+1);
        TsData s=TsData.of(TsPeriod.monthly(2000, 1), Doubles.of(x));
        UserVariable var=new UserVariable("test", s);
        Matrix M=Matrix.make(36, 1);
        TsVariableFactory.FACTORY.fill(var, TsPeriod.monthly(1999, 3), M);
        assertTrue(M.get(9, 0)==0);
        assertTrue(M.get(10, 0)==1);
        assertTrue(M.get(33, 0)==24);
        assertTrue(M.get(34, 0)==0);
    }
    
    @Test
    public void testBefore() {
        DoubleSeq x = DoubleSeq.onMapping(24, i->i+1);
        TsData s=TsData.of(TsPeriod.monthly(2000, 1), Doubles.of(x));
        UserVariable var=new UserVariable("test", s);
        Matrix M=Matrix.make(36, 1);
        TsVariableFactory.FACTORY.fill(var, TsPeriod.monthly(2005, 3), M);
        assertTrue(M.isZero(0));
    }
    
    @Test
    public void testAfter() {
        DoubleSeq x = DoubleSeq.onMapping(24, i->i+1);
        TsData s=TsData.of(TsPeriod.monthly(2000, 1), Doubles.of(x));
        UserVariable var=new UserVariable("test", s);
        Matrix M=Matrix.make(36, 1);
        TsVariableFactory.FACTORY.fill(var, TsPeriod.monthly(1995, 3), M);
        assertTrue(M.isZero(0));
    }
    
    @Test
    public void testAcross1() {
        DoubleSeq x = DoubleSeq.onMapping(24, i->i+1);
        TsData s=TsData.of(TsPeriod.monthly(2000, 1), Doubles.of(x));
        UserVariable var=new UserVariable("test", s);
        Matrix M=Matrix.make(36, 1);
        TsVariableFactory.FACTORY.fill(var, TsPeriod.monthly(2000, 7), M);
        assertTrue(M.get(0, 0)==7);
        assertTrue(M.get(17, 0)==24);
    }
    
    @Test
    public void testAcross2() {
        DoubleSeq x = DoubleSeq.onMapping(24, i->i+1);
        TsData s=TsData.of(TsPeriod.monthly(2000, 1), Doubles.of(x));
        UserVariable var=new UserVariable("test", s);
        Matrix M=Matrix.make(36, 1);
        TsVariableFactory.FACTORY.fill(var, TsPeriod.monthly(1998, 1), M);
        assertTrue(M.get(24, 0)==1);
        assertTrue(M.get(35, 0)==12);
    }
}
