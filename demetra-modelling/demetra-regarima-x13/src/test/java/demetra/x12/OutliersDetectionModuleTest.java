/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x12;

import demetra.x12.OutliersDetectionModuleImpl;
import demetra.data.Data;
import demetra.data.DoubleSequence;
import demetra.regarima.RegArimaModel;
import demetra.regarima.RegArimaUtility;
import demetra.sarima.estimation.SarimaMapping;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.sarima.internal.HannanRissanenInitializer;
import demetra.timeseries.TsPeriod;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import java.util.List;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class OutliersDetectionModuleTest {

    public OutliersDetectionModuleTest() {
    }

    @Test
    public void testProd() {
        TsPeriod start = TsPeriod.monthly(1967, 1);
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline(true);
        SarimaModel sarima = SarimaModel.builder(spec).setDefault().build();

        OutliersDetectionModuleImpl od = OutliersDetectionModuleImpl.builder()
                .singleOutlierDetector(OutliersDetectionModuleImpl.defaultOutlierDetector(12))
                .criticalValue(3)
                .processor(RegArimaUtility.processor(SarimaMapping.of(spec), true, 1e-7))
                .build();
        RegArimaModel<SarimaModel> regarima = RegArimaModel.builder(SarimaModel.class).y(DoubleSequence.of(Data.PROD)).arima(sarima).build();
        od.prepare(regarima.getObservationsCount());
        od.process(regarima);
        int[][] outliers = od.getOutliers();
        for (int i = 0; i < outliers.length; ++i) {
            int[] cur = outliers[i];
            System.out.println(od.getFactory(cur[1]).getCode() + '-' + start.plus(cur[0]).display());
        }
        assertTrue(outliers.length == 4);
    }

    @Test
    @Ignore
    public void testProdWn() {
        TsPeriod start = TsPeriod.monthly(1967, 1);
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.setBd(1);
        spec.setD(1);
        SarimaModel sarima = SarimaModel.builder(spec).setDefault().build();
        System.out.println("WN");
        HannanRissanenInitializer hr = HannanRissanenInitializer.builder().build();
        OutliersDetectionModuleImpl od = OutliersDetectionModuleImpl.builder()
                .singleOutlierDetector(OutliersDetectionModuleImpl.defaultOutlierDetector(12))
                .criticalValue(3)
                .processor(RegArimaUtility.processor(SarimaMapping.of(spec), true, 1e-7))
                .build();
        RegArimaModel<SarimaModel> regarima = RegArimaModel.builder(SarimaModel.class).y(DoubleSequence.of(Data.PROD)).arima(sarima).build();
        od.prepare(regarima.getObservationsCount());
        od.process(regarima);
        int[][] outliers = od.getOutliers();
        for (int i = 0; i < outliers.length; ++i) {
            int[] cur = outliers[i];
            System.out.println(od.getFactory(cur[1]).getCode() + '-' + start.plus(cur[0]).display());
        }
    }

    @Test
    public void testProdLegacy() {

        ec.tstoolkit.modelling.arima.x13.OutliersDetector od = new ec.tstoolkit.modelling.arima.x13.OutliersDetector();
        od.setDefault();
        od.setCriticalValue(3);
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, Data.PROD, true);
        ec.tstoolkit.modelling.arima.ModelDescription desc = new ec.tstoolkit.modelling.arima.ModelDescription(s, null);
        ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();

        desc.setAirline(true);
        context.description = desc;
        context.hasseas = true;
        od.process(context);
        List<IOutlierVariable> outliers = context.description.getOutliers();
        int n = outliers.size();
//        System.out.println("Legacy");
//        for (IOutlierVariable o : outliers) {
//            System.out.println(o.getName());
//        }
    }

    @Test
    @Ignore
    public void stressTestProd() {
        System.out.println("JD3");
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 200; ++i) {
            SarimaSpecification spec = new SarimaSpecification(12);
            spec.airline(true);
            SarimaModel sarima = SarimaModel.builder(spec).setDefault().build();

            OutliersDetectionModuleImpl od = OutliersDetectionModuleImpl.builder()
                    .singleOutlierDetector(OutliersDetectionModuleImpl.defaultOutlierDetector(12))
                    .criticalValue(3)
                    .processor(RegArimaUtility.processor(SarimaMapping.of(spec), true, 1e-7))
                    .build();
            RegArimaModel<SarimaModel> regarima = RegArimaModel.builder(SarimaModel.class).y(DoubleSequence.of(Data.PROD)).arima(sarima).build();
            od.prepare(regarima.getObservationsCount());
            od.process(regarima);
            int[][] outliers = od.getOutliers();
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
    @Ignore
    public void stressTestProdLegacy() {

        System.out.println("Legacy");
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 200; ++i) {
            ec.tstoolkit.modelling.arima.x13.OutliersDetector od = new ec.tstoolkit.modelling.arima.x13.OutliersDetector();
            od.setDefault();
            od.setCriticalValue(3);
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, Data.PROD, true);
            ec.tstoolkit.modelling.arima.ModelDescription desc = new ec.tstoolkit.modelling.arima.ModelDescription(s, null);
            ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();

            desc.setAirline(true);
            context.description = desc;
            context.hasseas = true;
            od.process(context);
            List<IOutlierVariable> outliers = context.description.getOutliers();
            int n = outliers.size();
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
