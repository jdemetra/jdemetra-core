/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class JulianEasterVariableTest {
    
    public JulianEasterVariableTest() {
    }

    @Test
    public void testLongTermCorrection() {
        
        JulianEasterVariable var=new JulianEasterVariable();
        var.setDuration(28);
        TsPeriod start=new TsPeriod(TsFrequency.Monthly, 1995,0);
        DataBlock m=new DataBlock(532*12);
        var.data(start, m);
        for (int i=0; i<12; ++i){
            assertTrue(m.extract(i, -1, 12).sum()/532<1e-6);
        }
    }
    
}
