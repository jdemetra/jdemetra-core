/*
 * Copyright 2019 National Bank of Belgium
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
package demetra.x13.regarima;

import demetra.x13.regarima.RegArimaKernel;
import demetra.regarima.RegArimaSpec;
import demetra.data.Data;
import demetra.regarima.OutlierSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;
import demetra.data.Doubles;
import jdplus.regsarima.regular.ModelEstimation;

/**
 *
 * @author Jean Palate
 */
public class RegArimaKernelTest {

    private static final double[] data, datamissing;

    static {
        data = Data.PROD.clone();
        datamissing = Data.PROD.clone();
        datamissing[2] = Double.NaN;
        datamissing[100] = Double.NaN;
        datamissing[101] = Double.NaN;
        datamissing[102] = Double.NaN;

    }

    public RegArimaKernelTest() {
    }

    @Test
    public void testProdMissing() {
        RegArimaKernel processor = RegArimaKernel.of(RegArimaSpec.RG5, null);
        TsPeriod start = TsPeriod.monthly(1967, 1);
        TsData s = TsData.of(start, Doubles.of(datamissing));
        ModelEstimation rslt = processor.process(s, null);
        System.out.println("New");
        System.out.println(rslt.getConcentratedLikelihood().logLikelihood());
    }

    @Test
    public void testProdLegacyMissing() {
        IPreprocessor processor = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG5.build();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, datamissing, true);
        ec.tstoolkit.modelling.arima.PreprocessingModel rslt = processor.process(s, null);
        System.out.println("Legacy");
        System.out.println(rslt.estimation.getLikelihood().getLogLikelihood());
    }

//    @Test
    public void testProd() {
        RegArimaSpec spec = RegArimaSpec.RG5;
//        OutlierSpec outlierSpec = spec.getOutliers().toBuilder()
//                .defaultCriticalValue(3)
//                .build();

//        spec = spec.toBuilder()
//                .outliers(outlierSpec)
//                .build();
        RegArimaKernel processor = RegArimaKernel.of(spec, null);
        TsPeriod start = TsPeriod.monthly(1967, 1);
        TsData s = TsData.of(start, Doubles.of(data));
        ModelEstimation rslt = processor.process(s, null);
        RegArimaSpecification ospec = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG5.clone();
//        ospec.getOutliers().setDefaultCriticalValue(3);
        IPreprocessor oprocessor = ospec.build();
        ec.tstoolkit.timeseries.simplets.TsData os = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, data, true);
        ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(os, null);
        assertEquals(rslt.getStatistics().getLogLikelihood(), orslt.estimation.getStatistics().logLikelihood, 1e-4);
    }

    public static void stressTestProd() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 250; ++i) {
            RegArimaSpecification spec = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG5.clone();
            IPreprocessor processor = spec.build();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, data, true);
            ec.tstoolkit.modelling.arima.PreprocessingModel rslt = processor.process(s, null);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("legacy: " + (t1 - t0));
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 250; ++i) {
            RegArimaSpec spec = RegArimaSpec.RG5;
            RegArimaKernel processor = RegArimaKernel.of(spec, null);
            TsPeriod start = TsPeriod.monthly(1967, 1);
            TsData s = TsData.of(start, Doubles.of(data));
            ModelEstimation rslt = processor.process(s, null);
        }
        t1 = System.currentTimeMillis();
        System.out.println("new: " + (t1 - t0));
    }

    public static void main(String[] args) {
        stressTestProd();
    }
}
