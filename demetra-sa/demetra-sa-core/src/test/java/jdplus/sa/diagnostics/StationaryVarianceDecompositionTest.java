/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.diagnostics;

import demetra.data.Data;
import demetra.data.Doubles;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class StationaryVarianceDecompositionTest {
    
    public StationaryVarianceDecompositionTest() {
    }

    @Test
    public void testLongTerm() {
        TsData t1 = new StationaryVarianceDecomposition.LinearTrendComputer().calcLongTermTrend(TsData.of(TsPeriod.yearly(1900), 
                Doubles.of(Data.NILE)));
        System.out.println(TsData.of(TsPeriod.yearly(1900), 
                Doubles.of(Data.NILE)));
        System.out.println(t1);
        TsData t2 = new StationaryVarianceDecomposition.HPTrendComputer(20).calcLongTermTrend(TsData.of(TsPeriod.yearly(1900), 
                Doubles.of(Data.NILE)));
        System.out.println(t2);
    }
    
}
