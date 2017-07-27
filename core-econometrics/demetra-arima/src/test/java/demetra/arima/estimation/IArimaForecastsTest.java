/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.estimation;

import demetra.data.Data;
import demetra.data.DataBlock;
import demetra.data.Doubles;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class IArimaForecastsTest {

    private static final SarimaModel airline, arima;
    private static final Doubles data;

    static {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline();
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
        Doubles forecasts = ef.forecasts(data, 36);
        Doubles backcasts = ef.backcasts(data, 36);
        FastArimaForecasts ff = new FastArimaForecasts();
        ff.prepare(airline, false);
        Doubles aforecasts = ff.forecasts(data, 36);
        Doubles abackcasts = ff.backcasts(data, 36);

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
        Doubles forecasts = ef.forecasts(data, 36);
        Doubles backcasts = ef.backcasts(data, 36);
//        FastArimaForecasts ff=new FastArimaForecasts();
//        ff.prepare(airline, true);
//        Doubles aforecasts = ff.forecasts(data, 36);
//        Doubles abackcasts = ff.backcasts(data, 36);

//        System.out.println(forecasts);
//        System.out.println(backcasts);
//        System.out.println(aforecasts);
//        System.out.println(abackcasts);
    }

    @Test
    public void testArima() {
        ExactArimaForecasts ef = new ExactArimaForecasts();
        ef.prepare(arima, false);
        Doubles forecasts = ef.forecasts(data, 36);
        Doubles backcasts = ef.backcasts(data, 36);
        FastArimaForecasts ff = new FastArimaForecasts();
        ff.prepare(arima, false);
        Doubles aforecasts = ff.forecasts(data, 36);
        Doubles abackcasts = ff.backcasts(data, 36);

//        System.out.println(forecasts);
//        System.out.println(backcasts);
//        System.out.println(aforecasts);
//        System.out.println(abackcasts);
    }
}
