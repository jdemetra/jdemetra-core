/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.regarima.regular.diagnostics;

import demetra.data.Data;
import demetra.regarima.RegArimaModel;
import demetra.sarima.RegSarimaProcessor;
import demetra.sarima.RegSarimaProcessorTest;
import demetra.sarima.SarimaModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class OneStepAheadForecastingTestTest {

    public OneStepAheadForecastingTestTest() {
    }

    @Test
    public void testProd() {
        RegArimaModel<SarimaModel> model = RegSarimaProcessorTest.prodAirline();
        RegSarimaProcessor processor = RegSarimaProcessor.builder().build();
        OneStepAheadForecastingTest<SarimaModel> os = new OneStepAheadForecastingTest<>(processor, 18);
        os.test(model);
        System.out.println(os.inSampleMeanTest());
        System.out.println(os.outOfSampleMeanTest());
        ec.tstoolkit.sarima.SarimaSpecification spec = new ec.tstoolkit.sarima.SarimaSpecification(12);
        spec.airline();
        ec.tstoolkit.sarima.estimation.GlsSarimaMonitor omonitor = new ec.tstoolkit.sarima.estimation.GlsSarimaMonitor();
        ec.tstoolkit.arima.estimation.RegArimaModel omodel = new ec.tstoolkit.arima.estimation.RegArimaModel(new ec.tstoolkit.sarima.SarimaModel(spec), new ec.tstoolkit.data.DataBlock(Data.PROD));
        omodel.setMeanCorrection(true);
        ec.tstoolkit.arima.estimation.RegArimaEstimation orslt = omonitor.process(omodel);

        ec.tstoolkit.modelling.arima.diagnostics.OneStepAheadForecastingTest otest=
                new ec.tstoolkit.modelling.arima.diagnostics.OneStepAheadForecastingTest(18);
        otest.test(orslt.model);
        System.out.println(otest.inSampleMeanTest().getValue());
        System.out.println(otest.outOfSampleMeanTest().getValue());
    }

}
