/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.ssf;

import demetra.arima.internal.FastArimaForecasts;
import demetra.arima.ssf.ExactArimaForecasts;
import demetra.data.Data;
import demetra.data.DataBlock;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSequence;
import static demetra.data.Doubles.distance;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class IArimaForecastsTest {

    private static final SarimaModel airline, arima;
    private static final DoubleSequence data;

    static {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline(true);
        airline = SarimaModel.builder(spec).theta(1, -.6).btheta(1, -.8).build();
        spec.setP(3);
        arima = SarimaModel.builder(spec).theta(1, -.6).btheta(1, -.8).phi(-.2, -.5, -.2).build();
        data = DataBlock.copyOf(Data.PROD);
    }

    public IArimaForecastsTest() {
    }

    @Test
    public void testAirline() {
        ExactArimaForecasts ef = new ExactArimaForecasts();
        ef.prepare(airline, false);
        DoubleSequence forecasts = ef.forecasts(data, 36);
        DoubleSequence backcasts = ef.backcasts(data, 36);
        FastArimaForecasts ff = new FastArimaForecasts();
        ff.prepare(airline, false);
        DoubleSequence aforecasts = ff.forecasts(data, 36);
        DoubleSequence abackcasts = ff.backcasts(data, 36);

//        System.out.println(forecasts);
//        System.out.println(backcasts);
//        System.out.println(aforecasts);
//        System.out.println(abackcasts);
        assertTrue(distance(forecasts, aforecasts) < 1e-8);
        assertTrue(distance(backcasts, abackcasts) < 1e-8);
    }

    @Test
    public void testMeanAirline() {
        ExactArimaForecasts ef = new ExactArimaForecasts();
        ef.prepare(airline, true);
        DoubleSequence forecasts = ef.forecasts(data, 36);
        DoubleSequence backcasts = ef.backcasts(data, 36);
//        FastArimaForecasts ff=new FastArimaForecasts();
//        ff.prepare(airline, true);
//        DoubleSequence aforecasts = ff.forecasts(data, 36);
//        DoubleSequence abackcasts = ff.backcasts(data, 36);

//        System.out.println(forecasts);
//        System.out.println(backcasts);
//        System.out.println(aforecasts);
//        System.out.println(abackcasts);
    }

    @Test
    public void testArima() {
        ExactArimaForecasts ef = new ExactArimaForecasts();
        ef.prepare(arima, false);
        DoubleSequence forecasts = ef.forecasts(data, 36);
        DoubleSequence backcasts = ef.backcasts(data, 36);
        FastArimaForecasts ff = new FastArimaForecasts();
        ff.prepare(arima, false);
        DoubleSequence aforecasts = ff.forecasts(data, 36);
        DoubleSequence abackcasts = ff.backcasts(data, 36);

//        System.out.println(forecasts);
//        System.out.println(backcasts);
//        System.out.println(aforecasts);
//        System.out.println(abackcasts);
    }
}
