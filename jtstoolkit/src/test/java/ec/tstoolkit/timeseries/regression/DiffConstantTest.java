/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class DiffConstantTest {
    
    public DiffConstantTest() {
    }

    @Test
    public void testLinear() {
        Day start=Day.toDay();
        TsDomain domain=new TsDomain(new TsPeriod(TsFrequency.Monthly, start), 120);
        DataBlock data0=new DataBlock(domain.getLength());
        DataBlock data1=new DataBlock(domain.getLength());
        DiffConstant dc=new DiffConstant(new BackFilter(UnitRoots.D1), start);
        LinearTrend t=new LinearTrend(start);
        dc.data(domain.getStart(), data0);
        t.data(domain.getStart(), data1);
        assertTrue(data0.distance(data1)<1e-9);
    }
    
}
