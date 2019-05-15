/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package rssf;

import demetra.data.Data;
import jd.data.DataBlock;
import jd.maths.matrices.CanonicalMatrix;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.implementations.CompositeSsf;
import demetra.ssf.implementations.RegSsf;
import demetra.arima.ssf.AR1;
import demetra.ssf.SsfComponent;
import demetra.sts.LocalLevel;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.Ssf;
import demetra.ssf.univariate.SsfData;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class DisaggregationModelsTest {
    
    private static final double rho=0.9947781880696613;

    public DisaggregationModelsTest() {
    }

    @Test
    public void testChowLin() {
        int m = Data.PCRA.length;
        int n = Data.IND_PCR.length;
        DataBlock edata = DataBlock.make(n);
        CanonicalMatrix x = CanonicalMatrix.make(n, 2);
        x.column(0).set(1);
        x.column(1).copyFrom(Data.IND_PCR, 0);
        edata.set(Double.NaN);
        edata.extract(3, m, 4).copyFrom(Data.PCRA, 0);
        SsfData sdata = new SsfData(edata);
        SsfComponent ar = AR1.of(rho);
        SsfComponent rssf = RegSsf.of(ar, x);
        ISsf dssf = DisaggregationModels.of(rssf, 4);
        DefaultSmoothingResults srslts = DkToolkit.smooth(dssf, sdata, true, true);
        DataBlock z = DataBlock.make(dssf.getStateDim());
        for (int i = 0; i < n; ++i) {
            dssf.loading().Z(i, z);
            z.set(0, 0);
//            System.out.println(z.dot(srslts.a(i)));
        }
    }

    //@Test
    public void testFernandez() {
        int m = Data.PCRA.length;
        int n = Data.IND_PCR.length;
        DataBlock edata = DataBlock.make(n);
        CanonicalMatrix x = CanonicalMatrix.make(n, 2);
        x.column(0).set(1);
        x.column(1).copyFrom(Data.IND_PCR, 0);
        edata.set(Double.NaN);
        edata.extract(3, m, 4).copyFrom(Data.PCRA, 0);
        SsfData sdata = new SsfData(edata);
        CompositeSsf rssf = CompositeSsf.builder()
                .add(LocalLevel.of(1))
                .add(RegSsf.of(x))
                .measurementError(1)
                .build();
        ISsf dssf = DisaggregationModels.of(rssf, 4);
        DefaultSmoothingResults srslts = DkToolkit.sqrtSmooth(dssf, sdata, true, true);
        DataBlock z = DataBlock.make(dssf.getStateDim());
        for (int i = 0; i < n; ++i) {
            dssf.loading().Z(i, z);
            z.set(0, 0);
            System.out.println(z.dot(srslts.a(i)));
        }
    }

    @Test
    public void testChowLinLegacy() {
        int m = Data.PCRA.length;
        int n = Data.IND_PCR.length;
        ec.tstoolkit.timeseries.simplets.TsData Y = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Yearly,
                1980, 0, Data.PCRA, true);
        ec.tstoolkit.timeseries.simplets.TsData Q = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Quarterly,
                1980, 0, Data.IND_PCR, true);
        ec.benchmarking.simplets.ChowLin cl = new ec.benchmarking.simplets.ChowLin();
        cl.setConstant(true);
        
        ec.tstoolkit.timeseries.regression.TsVariableList vars = new ec.tstoolkit.timeseries.regression.TsVariableList();
        vars.add(new ec.tstoolkit.timeseries.regression.TsVariable(Q));
        cl.process(Y, vars);
 //       System.out.println(cl.getDisaggregatedSeries());
    }

}
