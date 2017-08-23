/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.sarima.estimation;

import demetra.arima.regarima.RegArimaEstimation;
import demetra.arima.regarima.RegArimaModel;
import demetra.data.Data;
import demetra.data.DoubleSequence;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class GlsSarimaMonitorTest {

    public GlsSarimaMonitorTest() {
    }

    @Test
    public void testNew() {
        GlsSarimaMonitor monitor = new GlsSarimaMonitor(new HannanRissanenInitializer(true, true, false));
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline();
        SarimaModel arima = SarimaModel.builder(spec)
                .setDefault()
                .build();
        RegArimaModel<SarimaModel> regs = RegArimaModel.builder(DoubleSequence.of(Data.PROD), arima)
                .meanCorrection(true)
                .build();
        RegArimaEstimation<SarimaModel> rslt = monitor.compute(regs);
        System.out.println("New");
        System.out.println(rslt.statistics(2, 0));
        System.out.println(rslt.getModel().arima());
    }

    @Test
    public void testLegacy() {
        ec.tstoolkit.sarima.estimation.GlsSarimaMonitor monitor = new ec.tstoolkit.sarima.estimation.GlsSarimaMonitor();
            monitor.setPrecision(1e-11);
        ec.tstoolkit.sarima.SarimaSpecification spec = new ec.tstoolkit.sarima.SarimaSpecification(12);
        spec.airline();
        ec.tstoolkit.sarima.SarimaModel arima = new ec.tstoolkit.sarima.SarimaModel(spec);
        ec.tstoolkit.arima.estimation.RegArimaModel<ec.tstoolkit.sarima.SarimaModel> regs
                = new ec.tstoolkit.arima.estimation.RegArimaModel<>(arima);
        regs.setY(new ec.tstoolkit.data.DataBlock(Data.PROD));
        regs.setMeanCorrection(true);
        ec.tstoolkit.arima.estimation.RegArimaEstimation<ec.tstoolkit.sarima.SarimaModel> rslt
                = monitor.process(regs);
        System.out.println("Legacy");
        System.out.println(rslt.statistics(2, 0));
        System.out.println(rslt.model.getArima());
    }

    @Test
    public void stressTestNew() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            GlsSarimaMonitor monitor = new GlsSarimaMonitor(new HannanRissanenInitializer(true, true, false));
            SarimaSpecification spec = new SarimaSpecification(12);
            spec.airline();
            SarimaModel arima = SarimaModel.builder(spec)
                    .setDefault()
                    .build();
            RegArimaModel<SarimaModel> regs = RegArimaModel.builder(DoubleSequence.of(Data.PROD), arima)
                    .meanCorrection(true)
                    .build();
            RegArimaEstimation<SarimaModel> rslt = monitor.compute(regs);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("New");
        System.out.println(t1 - t0);
    }

    @Test
    public void stressTestLegacy() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            ec.tstoolkit.sarima.estimation.GlsSarimaMonitor monitor = new ec.tstoolkit.sarima.estimation.GlsSarimaMonitor();
            monitor.setPrecision(1e-9);
            ec.tstoolkit.sarima.SarimaSpecification spec = new ec.tstoolkit.sarima.SarimaSpecification(12);
            spec.airline();
            ec.tstoolkit.sarima.SarimaModel arima = new ec.tstoolkit.sarima.SarimaModel(spec);
            ec.tstoolkit.arima.estimation.RegArimaModel<ec.tstoolkit.sarima.SarimaModel> regs
                    = new ec.tstoolkit.arima.estimation.RegArimaModel<>(arima);
            regs.setY(new ec.tstoolkit.data.DataBlock(Data.PROD));
            regs.setMeanCorrection(true);
            ec.tstoolkit.arima.estimation.RegArimaEstimation<ec.tstoolkit.sarima.SarimaModel> rslt
                    = monitor.process(regs);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Legacy");
        System.out.println(t1 - t0);
    }

}
