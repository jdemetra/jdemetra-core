/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.arima.estimation;

import data.Data;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.modelling.arima.CheckLast;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ForecastsTest {

    public ForecastsTest() {
    }

    @Test
    public void testSomeMethod() {
        int N=36;
        TsData p = Data.P.clone();
        for (int i = 13; i < 25; ++i) {
            p.set(i, Double.NaN);
        }
        for (int i = 300; i < 320; ++i) {
            p.set(i, Double.NaN);
        }
        for (int i = 200; i < 220; ++i) {
            p.set(i, Double.NaN);
        }
        PreprocessingModel model=TramoSpecification.TRfull.build().process(p, null);
        TsVariableList vars = model.description.buildRegressionVariables();
        TsDomain fdomain = new TsDomain(model.description.getSeriesDomain().getEnd(), N);
        List<DataBlock> x = vars.all().data(fdomain);


        RegArimaEstimation<SarimaModel> estimation
                = new RegArimaEstimation<>(model.estimation.getRegArima(), model.estimation.getLikelihood());

        Forecasts forecasts1 = new Forecasts();
        forecasts1.calcForecast(estimation, x, N, model.description.getArimaComponent().getFreeParametersCount());
        double[] f1 =forecasts1.getForecasts();
        double[] e1 = forecasts1.getForecastStdevs();
        Forecasts forecasts2 = new Forecasts();
        forecasts2.calcForecast2(estimation, x, N, model.description.getArimaComponent().getFreeParametersCount());
        double[] f2 =forecasts2.getForecasts();
        double[] e2 = forecasts2.getForecastStdevs();
        assertTrue(new DataBlock(f1).distance(new DataBlock(f2))<1e-9);
        assertTrue(new DataBlock(e1).distance(new DataBlock(e2))<1e-9);
    }

}
