/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats;

import jdplus.stats.DescriptiveStatistics;
import demetra.data.DoubleSeq;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class DescriptiveStatisticsTest {
    
    public DescriptiveStatisticsTest() {
    }
    
    private double[] random(int n){
        double[] values=new double[50];
        Random rnd=new Random(0);
        for (int i=0; i<values.length; ++i){
            values[i]=rnd.nextDouble();
        }
        return values;
    }

    private double[] randomWithMissing(int n){
        double[] values=new double[50];
        Random rnd=new Random(0);
        for (int i=0; i<values.length; ++i){
            if (i%5 == 0)
                values[i]=Double.NaN;
            else
            values[i]=rnd.nextDouble();
        }
        return values;
    }

    @Test
    public void testNoMissing() {
        double[] values=random(50);
        
        DescriptiveStatistics stats=DescriptiveStatistics.of(DoubleSeq.of(values));
        ec.tstoolkit.data.DescriptiveStatistics ostats=new ec.tstoolkit.data.DescriptiveStatistics(values);
        
        assertEquals(stats.getSkewness(), ostats.getSkewness(), 1e-9);
        assertEquals(stats.getKurtosis(), ostats.getKurtosis(), 1e-9);
        assertEquals(stats.getMedian(), ostats.getMedian(), 1e-9);
        assertEquals(stats.getVarDF(3), ostats.getVarDF(3), 1e-9);
    }
    
    @Test
    public void testMissing() {
        double[] values=randomWithMissing(50);
        Random rnd=new Random(0);
         DescriptiveStatistics stats=DescriptiveStatistics.of(DoubleSeq.of(values));
        ec.tstoolkit.data.DescriptiveStatistics ostats=new ec.tstoolkit.data.DescriptiveStatistics(values);
        
        assertEquals(stats.getSkewness(), ostats.getSkewness(), 1e-9);
        assertEquals(stats.getKurtosis(), ostats.getKurtosis(), 1e-9);
        assertEquals(stats.getMedian(), ostats.getMedian(), 1e-9);
        assertEquals(stats.getVarDF(3), ostats.getVarDF(3), 1e-9);
    }
}
