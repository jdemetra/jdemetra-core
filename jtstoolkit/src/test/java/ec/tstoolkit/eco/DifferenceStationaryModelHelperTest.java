/*
 * Copyright 2013-2014 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.tstoolkit.eco;

import ec.benchmarking.Cumulator;
import ec.benchmarking.ssf.SsfDisaggregation;
import ec.tstoolkit.arima.estimation.ConcentratedLikelihoodEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.ssf.SsfAlgorithm;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.SsfModel;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.regression.RegressionUtilities;
import ec.tstoolkit.timeseries.simplets.AverageInterpolator;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.IntList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class DifferenceStationaryModelHelperTest {

    private static final double[] Y, X;
    private static final Matrix J;
    private static final SarimaSpecification airline;
    private static final TsDomain domain;

    static {
        airline = new SarimaSpecification(12);
        airline.airline();
        TsData tsy = data.Data.P;
        domain = tsy.getDomain();
        TsData tsx = tsy.changeFrequency(TsFrequency.Quarterly, TsAggregationType.Sum, true);
        int n = tsy.getLength();
        Y = new double[n];
        tsy.copyTo(Y, 0);
        TsPeriodSelector sel = new TsPeriodSelector();
        int ny2 = tsy.getLength() / 24;
        sel.first(ny2 * 4);
        TsData tsx0 = tsx.select(sel);
        sel.excluding(ny2 * 12, 0);
        TsData tsy1 = tsy.select(sel);
        int m = tsx0.getLength() + tsy1.getLength();
        X = new double[m];
        int d = airline.getDifferenceOrder();
        tsy1.rextract(0, d).copyTo(X, 0);
        tsx0.copyTo(X, d);
        tsy1.rextract(d, tsy1.getLength() - d).copyTo(X, d + tsx0.getLength());
        J = new Matrix(m, n);
        int l = tsx0.getLength() * 3;
        for (int i = 0; i < d; ++i) {
            J.set(i, l + i, 1);
        }
        for (int i = 0, c = 0; i < tsx0.getLength(); ++i) {
            for (int j = 0; j < 3; ++j) {
                J.set(d + i, c++, 1);
            }
        }
        J.subMatrix(d + tsx0.getLength(), m, l + d, n).diagonal().set(1);
     }

    public DifferenceStationaryModelHelperTest() {
    }

    @Test
    public void testMissings() {
        int n = 120;
        DataBlock k = new DataBlock(n);
        k.randomize(0);
        int m = 0;
        for (int i = 0; i < n - 5; i += n / 3) {
            k.set(i, Double.NaN);
            ++m;
        }
        for (int i = n - 5; i < n; ++i) {
            k.set(i, Double.NaN);
            ++m;
        }
        Matrix J = DifferenceStationaryModelHelper.missings(k);
        assertTrue(J.getRowsCount() == n - m);
    }

    @Test
    public void testFirst0() {
        Matrix j = DifferenceStationaryModelHelper.first(12, 4);
        assertTrue(j.getRowsCount() == 3);
    }

    @Test
    public void testFirst1() {
        Matrix j = DifferenceStationaryModelHelper.first(13, 4);
        assertTrue(j.getRowsCount() == 4);
    }

    @Test
    public void testLast0() {
        Matrix j = DifferenceStationaryModelHelper.last(12, 4);
        assertTrue(j.getRowsCount() == 3);
    }

    @Test
    public void testLast1() {
        Matrix j = DifferenceStationaryModelHelper.last(11, 4);
        assertTrue(j.getRowsCount() == 2);
    }

    @Test
    public void testAgg0() {
        Matrix j = DifferenceStationaryModelHelper.aggregation(12, 4);
        assertTrue(j.getRowsCount() == 3);
    }

    @Test
    public void testAgg1() {
        Matrix j = DifferenceStationaryModelHelper.aggregation(11, 4);
        assertTrue(j.getRowsCount() == 2);
    }

 //   @Test
    public void demo1() {
        int n = 24, k = 1;
        DataBlock y = new DataBlock(n);
        y.randomize(0);
        for (int i = 2; i < n; i += 6) {
            y.set(i, Double.NaN);
        }
        long t0 = System.currentTimeMillis();
        Matrix B = null, J = null, D = null;
        for (int j = 0; j < k; ++j) {
            BackFilter d = new BackFilter(UnitRoots.D(4).times(UnitRoots.D1));
            int[] inits = DifferenceStationaryModelHelper.searchDefaultInitialValues(y, d.getDegree());
            J = DifferenceStationaryModelHelper.missings(y, inits);
            DifferenceStationaryModelHelper helper = new DifferenceStationaryModelHelper(y, null,J, d);
            B = helper.getB();
            D = helper.getD();

//        System.out.println(J);
//        System.out.println(helper.getD());
//        System.out.println(helper.getB());
        }
        long t1 = System.currentTimeMillis();
        //System.out.println(t1 - t0);
    }

//    @Test
    public void demoAirline() {
        DataBlock jy = new DataBlock(X.length);
        jy.product(J.rows(), new DataBlock(Y));
        assertTrue(new DataBlock(X).distance(jy) < 1e-12);
        double th = -.4, bth = -.5, phi = .3;
        SarimaModel arima = new SarimaModel(airline);
        arima.setTheta(1, th);
        arima.setBTheta(1, bth);
        //       arima.setPhi(1, phi);
        int K = 1;
        long t0 = System.currentTimeMillis();
        Likelihood ll = null;
        for (int k = 0; k < K; ++k) {
            DifferenceStationaryModelHelper helper = new DifferenceStationaryModelHelper(new DataBlock(X), null, J, airline.getDifferencingFilter());
             SarimaModel starima = (SarimaModel) arima.stationaryTransformation().stationaryModel;
            Matrix V = starima.covariance(Y.length - airline.getDifferenceOrder());
            ll = helper.compute(V);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        DiffuseConcentratedLikelihood dll = null;
        for (int k = 0; k < K; ++k) {
            double[] x = Y.clone();

            Cumulator cumul = new Cumulator(3);
            cumul.transform(x);
            int limit = (Y.length / 24) * 12;
            for (int i = 0; i < x.length; ++i) {
                if (i < limit && i % 3 != 2) {
                    x[i] = Double.NaN;
                }
            }
            SsfArima ssf = new SsfArima(arima);
            SsfDisaggregation disagg = new SsfDisaggregation(3, ssf);
            SsfModel<SsfDisaggregation> model = new SsfModel<>(disagg, new SsfData(x, null), null, null);
            SsfAlgorithm<SsfDisaggregation> alg1 = new SsfAlgorithm<>();
            DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> ell = alg1.evaluate(model);
            dll = ell.getLikelihood();

        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

        System.out.println(ll.getLogLikelihood());
        System.out.println(ll.getSsqErr());
        System.out.println(ll.getLogDeterminant());
        System.out.println(dll.getLogLikelihood());
        System.out.println(dll.getSsqErr());
        System.out.println(dll.getLogDeterminant());
    }

//    @Test
    public void demoMissing() {
        double th = .6, bth = -.99, phi = .3;
        SarimaModel arima = new SarimaModel(airline);
        arima.setTheta(1, th);
        arima.setBTheta(1, bth);
        //      arima.setPhi(1, phi);

        double[] y = Y.clone();
        y[2] = Double.NaN;
        y[5] = Double.NaN;
        y[6] = Double.NaN;
        y[16] = Double.NaN;
        double[] ym = y.clone();
        int[] initials = DifferenceStationaryModelHelper.searchDefaultInitialValues(new DataBlock(y), arima.getNonStationaryARCount());
        Matrix j = DifferenceStationaryModelHelper.missings(new DataBlock(y), initials);

        ConcentratedLikelihoodEstimation clle = new ConcentratedLikelihoodEstimation();
        IntList m = new IntList();
        AverageInterpolator.cleanMissings(y, m);
        DataBlock x = new DataBlock(j.getRowsCount());
        x.product(j.rows(), new DataBlock(y));
        RegArimaModel<SarimaModel> regarima = new RegArimaModel(arima);
        regarima.setY(new DataBlock(y));
        regarima.setMissings(m.toArray());
        clle.estimate(regarima);
        ConcentratedLikelihood cll = clle.getLikelihood();
        System.out.println(cll.getLogLikelihood());
        System.out.println(cll.getSsqErr());
        System.out.println(cll.getLogDeterminant());
        DifferenceStationaryModelHelper helper = new DifferenceStationaryModelHelper(x, null, j, arima.getNonStationaryAR());
         SarimaModel starima = (SarimaModel) arima.stationaryTransformation().stationaryModel;
        Matrix V = starima.covariance(y.length - airline.getDifferenceOrder());
        Likelihood ll = helper.compute(V);
        System.out.println(ll.getLogLikelihood());
        System.out.println(ll.getSsqErr());
        System.out.println(ll.getLogDeterminant());

        SsfArima ssf = new SsfArima(arima);
        SsfModel<SsfArima> model = new SsfModel<>(ssf, new SsfData(ym, null), null, null);
        SsfAlgorithm<SsfArima> alg1 = new SsfAlgorithm<>();
        DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> ell = alg1.evaluate(model);
        DiffuseConcentratedLikelihood dll = ell.getLikelihood();
        System.out.println(dll.getLogLikelihood());
        System.out.println(dll.getSsqErr());
        System.out.println(dll.getLogDeterminant());
    }

//    @Test
    public void demoFull() {
        DataBlock jy = new DataBlock(X.length);
        jy.product(J.rows(), new DataBlock(Y));
        assertTrue(new DataBlock(X).distance(jy) < 1e-12);
        double th = .2, bth = -.9, phi = .3;
        SarimaModel arima = new SarimaModel(airline);
        arima.setTheta(1, th);
        arima.setBTheta(1, bth);
//        arima.setPhi(1, phi);
        int K = 1;
        long t0 = System.currentTimeMillis();
        ConcentratedLikelihood ll = null;
        for (int k = 0; k < K; ++k) {
            DifferenceStationaryModelHelper helper = new DifferenceStationaryModelHelper(new DataBlock(X), getX(), J, airline.getDifferencingFilter());
            SarimaModel starima = (SarimaModel) arima.stationaryTransformation().stationaryModel;
            Matrix V = starima.covariance(Y.length - airline.getDifferenceOrder());
            ll = helper.compute(V);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

        System.out.println(ll.getLogLikelihood());
        System.out.println(ll.getSsqErr());
        System.out.println(ll.getLogDeterminant());
        System.out.println(new DataBlock(ll.getB()));
        System.out.println(new DataBlock(ll.getTStats()));

        double[] y = Y.clone();

        Cumulator cumul = new Cumulator(3);
        cumul.transform(y);
        Matrix x = getX();
        for (int i = 0; i < x.getColumnsCount(); ++i) {
            cumul.transform(x.column(i));
        }
        int limit = (Y.length / 24) * 12;
        for (int i = 0; i < y.length; ++i) {
            if (i < limit && i % 3 != 2) {
                y[i] = Double.NaN;
            }
        }
        SsfArima ssf = new SsfArima(arima);
        SsfDisaggregation disagg = new SsfDisaggregation(3, ssf);
        SsfModel<SsfDisaggregation> model = new SsfModel<>(disagg, new SsfData(y, null), x.subMatrix(), null);
        SsfAlgorithm<SsfDisaggregation> alg1 = new SsfAlgorithm<>();
        
        DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> ell = alg1.evaluate(model);
        DiffuseConcentratedLikelihood dll = ell.getLikelihood();
        System.out.println(dll.getLogLikelihood());
        System.out.println(dll.getSsqErr());
        System.out.println(dll.getLogDeterminant());
        System.out.println(new DataBlock(dll.getB()));
        System.out.println(new DataBlock(dll.tstats()));
    }

    private Matrix getX() {
        GregorianCalendarVariables regs = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
        Matrix X = new Matrix(domain.getLength(), regs.getDim());
        return RegressionUtilities.matrix(regs, domain);
    }
}
