/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.timeseries.TsPeriod;
import jd.data.DataBlock;
import demetra.timeseries.TsDomain;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;
import jd.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class JulianEasterVariableTest {

    public JulianEasterVariableTest() {
    }

    @Test
    public void testMonthly() {
        for (int i = 2; i < 120; ++i) {
            ec.tstoolkit.timeseries.regression.JulianEasterVariable ovar = new ec.tstoolkit.timeseries.regression.JulianEasterVariable();
            ec.tstoolkit.timeseries.simplets.TsPeriod ostart = new ec.tstoolkit.timeseries.simplets.TsPeriod(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 2000, 0);
            ec.tstoolkit.data.DataBlock odata = new ec.tstoolkit.data.DataBlock(i);
            ovar.data(ostart, odata);

            JulianEasterVariable var = new JulianEasterVariable(6, true);
            TsPeriod start = TsPeriod.monthly(2000, 1);
            TsDomain dom = TsDomain.of(start, i);
            DataBlock data = Regression.x(dom, var);
            assertTrue(data.distance(DoubleSeq.of(odata.getData())) < 1e-9);
        }
    }

    @Test
    public void testQuarterly() {
        for (int i = 2; i < 40; ++i) {
            ec.tstoolkit.timeseries.regression.JulianEasterVariable ovar = new ec.tstoolkit.timeseries.regression.JulianEasterVariable();
            ec.tstoolkit.timeseries.simplets.TsPeriod ostart = new ec.tstoolkit.timeseries.simplets.TsPeriod(ec.tstoolkit.timeseries.simplets.TsFrequency.Quarterly, 2000, 0);
            ec.tstoolkit.data.DataBlock odata = new ec.tstoolkit.data.DataBlock(i);
            ovar.data(ostart, odata);

            JulianEasterVariable var = new JulianEasterVariable(6, true);
            TsPeriod start = TsPeriod.quarterly(2000, 1);
            TsDomain dom = TsDomain.of(start, i);
            DataBlock data = Regression.x(dom, var);
            assertTrue(data.distance(DoubleSeq.of(odata.getData())) < 1e-9);
        }
    }
}
