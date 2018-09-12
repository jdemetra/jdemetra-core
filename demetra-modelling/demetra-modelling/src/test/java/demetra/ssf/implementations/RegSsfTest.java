/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.implementations;

import demetra.arima.ssf.SsfArima;
import demetra.data.Data;
import demetra.maths.matrices.Matrix;
import demetra.modelling.regression.GenericTradingDaysVariables;
import demetra.modelling.regression.RegressionUtility;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.ssf.SsfComponent;
import demetra.ssf.StateComponent;
import demetra.ssf.dk.DkLikelihood;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.Ssf;
import demetra.ssf.univariate.SsfData;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class RegSsfTest {

    public RegSsfTest() {
    }

    @Test
    public void testDirectComposite() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline(true);
        SarimaModel airline = SarimaModel.builder(spec)
                .theta(1, -.6)
                .btheta(1, -.8)
                .build();
        Ssf ssf = SsfArima.of(airline);
        StateComponent cmp1 = SsfArima.componentOf(airline);
        TsData s = Data.TS_PROD;
        SsfData y = new SsfData(s.getValues());
        GenericTradingDays td = GenericTradingDays.contrasts(DayClustering.TD7);
        Matrix X = RegressionUtility.data(s.getDomain(), new GenericTradingDaysVariables(td));
        ISsf rssf1 = RegSsf.of(ssf, X);
        CompositeSsf rssf2 = CompositeSsf.builder()
                .add(cmp1, Loading.fromPosition(0))
                .add(RegSsf.of(X))
                .build();
        DkLikelihood ll1 = DkToolkit.likelihoodComputer().compute(rssf1, y);
        DkLikelihood ll2 = DkToolkit.likelihoodComputer().compute(rssf2, y);
        assertEquals(ll1.logLikelihood(), ll2.logLikelihood(), 1e-5);
//        long t0 = System.currentTimeMillis();
//        for (int i = 0; i < 5000; ++i) {
//            DkToolkit.likelihoodComputer().compute(rssf1, y);
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
//        t0 = System.currentTimeMillis();
//        for (int i = 0; i < 5000; ++i) {
//            DkToolkit.likelihoodComputer().compute(rssf2, y);
//        }
//        t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);

    }

}
