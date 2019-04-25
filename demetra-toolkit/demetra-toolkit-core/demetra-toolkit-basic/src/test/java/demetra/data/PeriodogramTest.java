/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.data;

import demetra.data.analysis.Periodogram;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class PeriodogramTest {
    
    double[] data;
    
    public PeriodogramTest() {
        data=new double[Data.ABS_RETAIL.length-1];
        for (int i=0; i<data.length; ++i){
            data[i]=Math.log(Data.ABS_RETAIL[i+1])-Math.log(Data.ABS_RETAIL[i]);
        }
    }

    @Test
    public void testDFT() {
        Periodogram p1 = Periodogram.of(DoubleSeq.of(data));
        ec.tstoolkit.data.Periodogram p2=new ec.tstoolkit.data.Periodogram(new ec.tstoolkit.data.ReadDataBlock(data), false);
        for (int i=0; i<p1.getP().length; ++i){
            assertEquals(p1.getP()[i], p2.getP()[i], 1e-9);
        }
    }
    
}
