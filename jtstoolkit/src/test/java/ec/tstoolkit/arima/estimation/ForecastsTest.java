/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.arima.estimation;

import data.Data;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.arima.CheckLast;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.tramo.ArimaSpec;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.ssf.DiffuseFilteringResults;
import ec.tstoolkit.ssf.ExtendedSsfData;
import ec.tstoolkit.ssf.Filter;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.RegSsf;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.arima.SsfArima;
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

    @Test
    public void testOtherMethod() {
        int N=36;
        TsData p = Data.P.clone();
        TramoSpecification spec=TramoSpecification.TRfull.clone();
        ArimaSpec xspec=new ArimaSpec();
        xspec.setP(3);
        xspec.setD(0);
        xspec.setQ(0);
        xspec.setBD(1);
        xspec.setBQ(1);
        spec.setArima(xspec);
        PreprocessingModel model=spec.build().process(p, null);
        TsVariableList vars = model.description.buildRegressionVariables();
        TsDomain fdomain = new TsDomain(model.description.getSeriesDomain().getEnd(), N);
        List<DataBlock> x = vars.all().data(fdomain);


        RegArimaEstimation<SarimaModel> estimation
                = new RegArimaEstimation<>(model.estimation.getRegArima(), model.estimation.getLikelihood());

        Forecasts forecasts1 = new Forecasts();
        forecasts1.calcForecast(estimation, x, N, model.description.getArimaComponent().getFreeParametersCount());
        double[] f1 =forecasts1.getForecasts();
        double[] e1 = forecasts1.getForecastStdevs();
        
        // use the full Kalman filter
        
        TsDomain fulldomain = model.description.getSeriesDomain().extend(0, N);
        Matrix matrix = vars.all().matrix(fulldomain);        

        SsfData s=new SsfData(p.log().internalStorage(), null);
        ExtendedSsfData y=new ExtendedSsfData(s);
        y.setForecastsCount(N);
        SsfArima ssf = new SsfArima(estimation.model.getArima());
        RegSsf xssf=new RegSsf(ssf, matrix.all());
        Filter<ISsf> filter = new Filter<>();
        filter.setSsf(xssf);
        ec.tstoolkit.ssf.DiffuseSquareRootInitializer initializer=
                new ec.tstoolkit.ssf.DiffuseSquareRootInitializer();
        filter.setInitializer(initializer);
        DiffuseFilteringResults frslts = new DiffuseFilteringResults(true);
        frslts.getFilteredData().setSavingA(true);
        filter.process(y, frslts);
        
        double[] f2 =new double[N];
        DataBlock z=new DataBlock(xssf.getStateDim());
        for (int i=0, j=p.getLength(); i<N; ++i, ++j){
            xssf.Z(j, z);
            f2[i]=frslts.getFilteredData().A(j).dot(z);
        }
        assertTrue(new DataBlock(f1).distance(new DataBlock(f2))<1e-9);
        
        
   }
}
