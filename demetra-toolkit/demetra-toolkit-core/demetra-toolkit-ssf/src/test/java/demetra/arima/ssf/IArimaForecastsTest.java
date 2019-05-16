/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.ssf;

import demetra.arima.internal.FastArimaForecasts;
import demetra.data.Data;
import jdplus.data.DataBlock;
import demetra.sarima.SarimaModel;
import demetra.arima.SarimaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class IArimaForecastsTest {

    private static final SarimaModel airline, arima;
    private static final DoubleSeq data;

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
        DoubleSeq forecasts = ef.forecasts(data, 36);
        DoubleSeq backcasts = ef.backcasts(data, 36);
        FastArimaForecasts ff = new FastArimaForecasts();
        ff.prepare(airline, false);
        DoubleSeq aforecasts = ff.forecasts(data, 36);
        DoubleSeq abackcasts = ff.backcasts(data, 36);

//        System.out.println(forecasts);
//        System.out.println(backcasts);
//        System.out.println(aforecasts);
//        System.out.println(abackcasts);
        assertTrue(forecasts.distance(aforecasts) < 1e-8);
        assertTrue(backcasts.distance(abackcasts) < 1e-8);
    }

    @Test
    public void testMeanAirline() {
        ExactArimaForecasts ef = new ExactArimaForecasts();
        ef.prepare(airline, true);
        DoubleSeq forecasts = ef.forecasts(data, 36);
        DoubleSeq backcasts = ef.backcasts(data, 36);
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
        DoubleSeq forecasts = ef.forecasts(data, 36);
        DoubleSeq backcasts = ef.backcasts(data, 36);
        FastArimaForecasts ff = new FastArimaForecasts();
        ff.prepare(arima, false);
        DoubleSeq aforecasts = ff.forecasts(data, 36);
        DoubleSeq abackcasts = ff.backcasts(data, 36);

//        System.out.println(forecasts);
//        System.out.println(backcasts);
//        System.out.println(aforecasts);
//        System.out.println(abackcasts);
    }
}
