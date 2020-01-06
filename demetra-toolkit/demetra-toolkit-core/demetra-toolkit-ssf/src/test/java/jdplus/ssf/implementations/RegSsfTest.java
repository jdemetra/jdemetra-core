/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.implementations;

import jdplus.arima.ssf.SsfArima;
import demetra.data.Data;
import jdplus.modelling.regression.Regression;
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
import demetra.timeseries.regression.GenericTradingDaysVariable;
import org.junit.Test;
import static org.junit.Assert.*;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author palatej
 */
public class RegSsfTest {

    public RegSsfTest() {
    }

    @Test
    public void testDirectComposite() {
        SarimaSpecification spec=SarimaSpecification.airline(12);
        SarimaModel airline = SarimaModel.builder(spec)
                .theta(1, -.6)
                .btheta(1, -.8)
                .build();
        StateComponent cmp1 = SsfArima.of(airline);
        Ssf ssf = Ssf.of(cmp1, SsfArima.defaultLoading());
        TsData s = Data.TS_PROD;
        SsfData y = new SsfData(s.getValues());
        GenericTradingDays td = GenericTradingDays.contrasts(DayClustering.TD7);
        Matrix X = Regression.matrix(s.getDomain(), new GenericTradingDaysVariable(td));
        ISsf rssf1 = RegSsf.of(ssf, X);
        CompositeSsf rssf2 = CompositeSsf.builder()
                .add(cmp1, Loading.fromPosition(0))
                .add(RegSsf.of(X.getColumnsCount()), RegSsf.loading(X))
                .build();
        DiffuseLikelihood ll1 = DkToolkit.likelihoodComputer(true, true, false).compute(rssf1, y);
        DiffuseLikelihood ll2 = DkToolkit.likelihoodComputer(true, true, false).compute(rssf2, y);
        assertEquals(ll1.logLikelihood(), ll2.logLikelihood(), 1e-5);
    }

    public static void main(String[] arg) {
        SarimaSpecification spec=SarimaSpecification.airline(12);
        SarimaModel airline = SarimaModel.builder(spec)
                .theta(1, -.6)
                .btheta(1, -.8)
                .build();
        StateComponent cmp1 = SsfArima.of(airline);
        Ssf ssf = Ssf.of(cmp1, SsfArima.defaultLoading());
        TsData s = Data.TS_PROD;
        SsfData y = new SsfData(s.getValues());
        GenericTradingDays td = GenericTradingDays.contrasts(DayClustering.TD7);
        Matrix X = Regression.matrix(s.getDomain(), new GenericTradingDaysVariable(td));
        ISsf rssf1 = RegSsf.of(ssf, X);
        CompositeSsf rssf2 = CompositeSsf.builder()
                .add(cmp1, Loading.fromPosition(0))
                .add(RegSsf.of(X.getColumnsCount()), RegSsf.loading(X))
                .build();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            DkToolkit.likelihood(rssf1, y, true, true);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            DkToolkit.likelihood(rssf2, y, true, true);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

    }
}
