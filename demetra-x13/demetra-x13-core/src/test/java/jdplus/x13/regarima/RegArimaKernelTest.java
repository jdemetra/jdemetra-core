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
package jdplus.x13.regarima;

import demetra.data.Data;
import demetra.data.Doubles;
import demetra.regarima.RegArimaSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import jdplus.regsarima.regular.RegSarimaModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

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
        RegSarimaModel rslt = processor.process(s, null);
        System.out.println("New");
        System.out.println(rslt.getEstimation().getStatistics().getLogLikelihood());
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
        RegSarimaModel rslt = processor.process(s, null);
        RegArimaSpecification ospec = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG5.clone();
//        ospec.getOutliers().setDefaultCriticalValue(3);
        IPreprocessor oprocessor = ospec.build();
        ec.tstoolkit.timeseries.simplets.TsData os = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, data, true);
        ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(os, null);
        assertEquals(rslt.getEstimation().getStatistics().getLogLikelihood(), orslt.estimation.getStatistics().logLikelihood, 1e-4);
    }

    @Test
    public void testInsee0() {
        TsData[] all = Data.insee();
        RegArimaKernel processor = RegArimaKernel.of(RegArimaSpec.RG0, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG0.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("RG0");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee1() {
        TsData[] all = Data.insee();
        RegArimaKernel processor = RegArimaKernel.of(RegArimaSpec.RG1, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG1.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("RG1");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee2() {
        TsData[] all = Data.insee();
        RegArimaKernel processor = RegArimaKernel.of(RegArimaSpec.RG2, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG2.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("RG2");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee3() {
        TsData[] all = Data.insee();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegArimaKernel processor = RegArimaKernel.of(RegArimaSpec.RG3, null);
            IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG3.build();
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("RG3");
        System.out.println(n);
        assertTrue(n >= .9 * all.length);
    }

    @Test
    public void testInsee4() {
        TsData[] all = Data.insee();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegArimaKernel processor = RegArimaKernel.of(RegArimaSpec.RG4, null);
            IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG4.build();
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("RG4");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee5() {
        TsData[] all = Data.insee();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegArimaKernel processor = RegArimaKernel.of(RegArimaSpec.RG5, null);
            IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG5.build();
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("RG5");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
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
            RegSarimaModel rslt = processor.process(s, null);
        }
        t1 = System.currentTimeMillis();
        System.out.println("new: " + (t1 - t0));
    }

    public static void main(String[] args) {
        stressTestProd();
    }
}
