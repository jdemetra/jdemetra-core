/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stats;

import java.util.Random;
import jdplus.data.DataBlock;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class DescriptiveStatisticsTest {
    
    public DescriptiveStatisticsTest() {
    }

    @Test
    public void testNoMissing() {
        DataBlock data=DataBlock.make(50);
        Random rnd=new Random(0);
        data.set(rnd::nextDouble);
        
        DescriptiveStatistics stats=DescriptiveStatistics.of(data);
        ec.tstoolkit.data.DescriptiveStatistics ostats=new ec.tstoolkit.data.DescriptiveStatistics(data.getStorage());
        
        assertEquals(stats.getSkewness(), ostats.getSkewness(), 1e-9);
        assertEquals(stats.getKurtosis(), ostats.getKurtosis(), 1e-9);
        assertEquals(stats.getMedian(), ostats.getMedian(), 1e-9);
        assertEquals(stats.getVarDF(3), ostats.getVarDF(3), 1e-9);
    }
    
    @Test
    public void testMissing() {
        DataBlock data=DataBlock.make(50);
        Random rnd=new Random(0);
        data.set(i->i%5 == 0 ? Double.NaN : rnd.nextDouble());
        
        DescriptiveStatistics stats=DescriptiveStatistics.of(data);
        ec.tstoolkit.data.DescriptiveStatistics ostats=new ec.tstoolkit.data.DescriptiveStatistics(data.getStorage());
        
        assertEquals(stats.getSkewness(), ostats.getSkewness(), 1e-9);
        assertEquals(stats.getKurtosis(), ostats.getKurtosis(), 1e-9);
        assertEquals(stats.getMedian(), ostats.getMedian(), 1e-9);
        assertEquals(stats.getVarDF(3), ostats.getVarDF(3), 1e-9);
    }
}
