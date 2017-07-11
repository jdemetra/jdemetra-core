/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.arima.estimation;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FastArimaForecastsTest {

    static final DataBlock x1, x2, x3;

    static {
        x1 = new DataBlock(60);
        x1.randomize(0);
        x2 = new DataBlock(60);
        x2.randomize(1);
        x3 = x1.deepClone();
        x3.add(x2);
    }

    public FastArimaForecastsTest() {
    }

    @Test
    public void testLinearProcess() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline();
        spec.setBP(1);
        SarimaModel arima = new SarimaModel(spec);
        arima.setBPhi(1, -.9);
        FastArimaForecasts fcasts = new FastArimaForecasts(arima, false);
        int N=12;
        double[] f1 = fcasts.forecasts(x1, N);
        double[] f2 = fcasts.forecasts(x2, N);
        double[] f3 = fcasts.forecasts(x3, N);
        for (int i = 0; i < N; ++i) {
            assertEquals(f3[i], f1[i] + f2[i], 1e-9);
        }
         N=60;
        f1 = fcasts.forecasts(x1, N);
        f2 = fcasts.forecasts(x2, N);
        f3 = fcasts.forecasts(x3, N);
        for (int i = 0; i < N; ++i) {
            assertEquals(f3[i], f1[i] + f2[i], 1e-9);
        }
    }

    @Test
    //@Ignore
    public void testMean() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline();
        spec.setBP(1);
        spec.setBD(0);
        SarimaModel arima = new SarimaModel(spec);
        arima.setBPhi(1, -.9);
        FastArimaForecasts fcasts = new FastArimaForecasts(arima, true);
        double[] f1 = fcasts.forecasts(x1, 24);
//        System.out.println(new DataBlock(f1));
//        System.out.println("mean:" + fcasts.getMean());
    }

    @Test
    //@Ignore
    public void testNoMean() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline();
        spec.setBP(1);
        SarimaModel arima = new SarimaModel(spec);
        arima.setBPhi(1, -.9);
        FastArimaForecasts fcasts = new FastArimaForecasts(arima, false);
        double[] f1 = fcasts.forecasts(x1, 24);
//        System.out.println(new DataBlock(f1));
    }
}
