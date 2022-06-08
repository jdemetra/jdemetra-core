/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.regarima;

import demetra.data.Doubles;
import jdplus.regsarima.ami.ExactOutliersDetector;
import demetra.data.Data;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.RegArimaUtility;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaOrders;
import jdplus.regsarima.internal.HannanRissanenInitializer;
import demetra.timeseries.TsPeriod;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

import demetra.timeseries.TsData;
import demetra.timeseries.regression.Variable;
import static jdplus.x13.regarima.OutliersDetectionModule.EPS;
import jdplus.modelling.regression.AdditiveOutlierFactory;
import jdplus.modelling.regression.LevelShiftFactory;
import jdplus.modelling.regression.TransitoryChangeFactory;
import demetra.timeseries.regression.ModellingUtility;
import jdplus.regarima.outlier.ExactSingleOutlierDetector;
import jdplus.stats.RobustStandardDeviationComputer;
import jdplus.regarima.outlier.SingleOutlierDetector;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.RegSarimaModelling;
import jdplus.sarima.estimation.SarimaMapping;
import org.junit.jupiter.api.Test;

import static jdplus.x13.regarima.Converter.convert;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class OutliersDetectionModuleTest {

    public OutliersDetectionModuleTest() {
    }

    static SingleOutlierDetector<SarimaModel> defaultOutlierDetector(int period) {
        SingleOutlierDetector sod = new ExactSingleOutlierDetector(RobustStandardDeviationComputer.mad(false),
                null, X13Utility.mlComputer());
        sod.setOutlierFactories(AdditiveOutlierFactory.FACTORY,
                LevelShiftFactory.FACTORY_ZEROENDED,
                new TransitoryChangeFactory(EPS));
        return sod;
    }

    @Test
    public void testProd() {
        TsPeriod start = TsPeriod.monthly(1967, 1);
        SarimaOrders spec = SarimaOrders.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec).setDefault().build();

        ExactOutliersDetector od = ExactOutliersDetector.builder()
                .singleOutlierDetector(defaultOutlierDetector(12))
                .criticalValue(3)
                .processor(RegArimaUtility.processor(true, 1e-7))
                .build();
        RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder().y(Doubles.of(Data.PROD)).arima(sarima).build();
        od.prepare(regarima.getObservationsCount());
        od.process(regarima, SarimaMapping.of(spec));
        int[][] outliers = od.getOutliers();
        for (int i = 0; i < outliers.length; ++i) {
            int[] cur = outliers[i];
            System.out.println(od.getFactory(cur[1]).getCode() + '-' + start.plus(cur[0]).display());
        }
        assertTrue(outliers.length == 4);
    }

    @Test
    public void testProdWn() {
        TsPeriod start = TsPeriod.monthly(1967, 1);
        SarimaOrders spec = new SarimaOrders(12);
        spec.setBd(1);
        spec.setD(1);
        SarimaModel sarima = SarimaModel.builder(spec).setDefault().build();
        System.out.println("WN");
        HannanRissanenInitializer hr = HannanRissanenInitializer.builder().build();
        ExactOutliersDetector od = ExactOutliersDetector.builder()
                .singleOutlierDetector(defaultOutlierDetector(12))
                .criticalValue(3)
                .processor(RegArimaUtility.processor(true, 1e-7))
                .build();
        RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder().y(Doubles.of(Data.PROD)).arima(sarima).build();
        od.prepare(regarima.getObservationsCount());
        od.process(regarima, SarimaMapping.of(spec));
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
        System.out.println("Legacy");
        for (IOutlierVariable o : outliers) {
            System.out.println(o.getName());
        }
    }

    @Test
    public void testInsee() {

        TsData[] insee = Data.insee();
        for (int i = 0; i < insee.length; ++i) {
            ec.tstoolkit.modelling.arima.x13.OutliersDetector lod = new ec.tstoolkit.modelling.arima.x13.OutliersDetector();
//            lod.setCriticalValue(3);
//            lod.addOutlierFactory(new ec.tstoolkit.timeseries.regression.AdditiveOutlierFactory());
//            ec.tstoolkit.timeseries.regression.LevelShiftFactory ls = new ec.tstoolkit.timeseries.regression.LevelShiftFactory();
//            ls.setZeroEnded(true);
//            lod.addOutlierFactory(ls);
            lod.setDefault();
            ec.tstoolkit.modelling.arima.ModelDescription ldesc = new ec.tstoolkit.modelling.arima.ModelDescription(convert(insee[i]), null);
            ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();

            ldesc.setAirline(true);
            context.description = ldesc;
            context.hasseas = true;
            lod.process(context);
            List<IOutlierVariable> loutliers = context.description.getOutliers();
            int on = loutliers.size();

            SarimaOrders spec = SarimaOrders.airline(12);

            OutliersDetectionModule od = OutliersDetectionModule.builder()
                    .ao(true)
                    .ls(true)
                    .tc(true)
                    .tcrate(0.7)
                    .precision(1e-7)
                    .maxOutliers(30)
                    .build();

            ModelDescription desc = new ModelDescription(insee[i], null);
            desc.setAirline(true);
            RegSarimaModelling modelling = RegSarimaModelling.of(desc);
            double va=X13Utility.calcCv(insee[i].length());
            od.process(modelling, va);
            Variable[] outs = modelling.getDescription().variables().filter(var -> ModellingUtility.isOutlier(var)).toArray(k -> new Variable[k]);
            int n=outs.length;
//            System.out.print(on);
//            System.out.print('\t');
//            System.out.println(n);
            //assertTrue(on ==n);
        }
    }

    @Test
    public void testInsee31() {

        TsData[] insee = Data.insee();
        for (int i = 0; i < insee.length; ++i) {
            ec.tstoolkit.modelling.arima.x13.OutliersDetector lod = new ec.tstoolkit.modelling.arima.x13.OutliersDetector();
            lod.setDefault();
//            lod.addOutlierFactory(new ec.tstoolkit.timeseries.regression.AdditiveOutlierFactory());
//            ec.tstoolkit.timeseries.regression.LevelShiftFactory ls = new ec.tstoolkit.timeseries.regression.LevelShiftFactory();
//            ls.setZeroEnded(true);
//            lod.addOutlierFactory(ls);
            lod.setCriticalValue(3);
            ec.tstoolkit.modelling.arima.ModelDescription ldesc = new ec.tstoolkit.modelling.arima.ModelDescription(convert(insee[i]), null);
            ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();

            ec.tstoolkit.sarima.SarimaSpecification ospec=new ec.tstoolkit.sarima.SarimaSpecification(12);
            ospec.airline(true);
            ospec.setP(1);
            ldesc.setSpecification(ospec);
            context.description = ldesc;
            context.hasseas = true;
            lod.process(context);
            List<IOutlierVariable> loutliers = context.description.getOutliers();
            int on = loutliers.size();

            SarimaOrders spec = SarimaOrders.airline(12);
            spec.setP(1);

            OutliersDetectionModule od = OutliersDetectionModule.builder()
                    .ao(true)
                    .ls(true)
                    .tc(true)
                    .tcrate(0.7)
                    .precision(1e-7)
                    .maxOutliers(30)
                    .build();

            ModelDescription desc = new ModelDescription(insee[i], null);
            desc.setSpecification(spec);
            RegSarimaModelling modelling = RegSarimaModelling.of(desc);
            od.process(modelling, 3);
            Variable[] outs = modelling.getDescription().variables().filter(var -> ModellingUtility.isOutlier(var)).toArray(k -> new Variable[k]);
            int n=outs.length;
//            System.out.print(on);
//            System.out.print('\t');
//            System.out.println(n);
            //assertTrue(on ==n);
        }
    }

    public static void main(String[] args) {
        stressTestProd();
        stressTestProdLegacy();
    }

    public static void stressTestProd() {
        System.out.println("JD3");
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 200; ++i) {
            SarimaOrders spec = SarimaOrders.airline(12);
            SarimaModel sarima = SarimaModel.builder(spec).setDefault().build();
            ExactOutliersDetector od = ExactOutliersDetector.builder()
                    .singleOutlierDetector(defaultOutlierDetector(12))
                    .criticalValue(3)
                    .processor(RegArimaUtility.processor(true, 1e-7))
                    .build();
            RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder().y(Doubles.of(Data.PROD)).arima(sarima).build();
            od.prepare(regarima.getObservationsCount());
            od.process(regarima, SarimaMapping.of(spec));
            int[][] outliers = od.getOutliers();
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    public static void stressTestProdLegacy() {

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
