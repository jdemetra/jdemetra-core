/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.implementations;

import jdplus.ssf.implementations.RegSsf;
import jdplus.ssf.implementations.Loading;
import jdplus.ssf.implementations.CompositeSsf;
import jdplus.arima.ssf.SsfArima;
import demetra.data.Data;
import demetra.modelling.regression.GenericTradingDaysVariable;
import demetra.modelling.regression.Regression;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaSpecification;
import jdplus.ssf.StateComponent;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.SsfData;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import org.junit.Test;
import static org.junit.Assert.*;
import jdplus.maths.matrices.FastMatrix;

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
        FastMatrix X = Regression.matrix(s.getDomain(), new GenericTradingDaysVariable(td));
        ISsf rssf1 = RegSsf.of(ssf, X);
        CompositeSsf rssf2 = CompositeSsf.builder()
                .add(cmp1, Loading.fromPosition(0))
                .add(RegSsf.of(X))
                .build();
        DiffuseLikelihood ll1 = DkToolkit.likelihoodComputer().compute(rssf1, y);
        DiffuseLikelihood ll2 = DkToolkit.likelihoodComputer().compute(rssf2, y);
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
