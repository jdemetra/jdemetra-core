/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sarima;

import demetra.data.Data;
import demetra.data.DoubleSequence;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.ami.OutliersDetectionModule;
import ec.tstoolkit.modelling.arima.RegArimaEstimator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FastSarimaProcessorTest {

    public FastSarimaProcessorTest() {
    }

    @Test
    @Ignore
    public void stressTestSomeMethod() {
        GlsSarimaProcessor fallback = GlsSarimaProcessor.builder()
                .precision(1e-5)
                .build();
        FastSarimaProcessor processor = new FastSarimaProcessor(fallback);
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline(true);
        SarimaModel sarima = SarimaModel.builder(spec).setDefault().build();
        RegArimaModel<SarimaModel> regarima = RegArimaModel.builder(SarimaModel.class).y(DoubleSequence.of(Data.PROD)).arima(sarima).build();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            RegArimaEstimation<SarimaModel> rslt = processor.process(regarima);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Fast");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            RegArimaEstimation<SarimaModel> rslt = fallback.process(regarima);
        }
        t1 = System.currentTimeMillis();
        System.out.println("Gls");
        System.out.println(t1 - t0);
        RegSarimaProcessor e=RegSarimaProcessor.builder()
                .precision(1e-5)
                .build();
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            RegArimaEstimation<SarimaModel> rslt = e.process(regarima);
        }
        t1 = System.currentTimeMillis();
        System.out.println("Reg");
        System.out.println(t1 - t0);
    }

}
