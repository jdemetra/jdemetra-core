/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.regarima;

import demetra.data.Data;
import jdplus.data.DataBlock;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaSpecification;
import org.junit.Ignore;
import org.junit.Test;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class RegArimaModelTest {

    public RegArimaModelTest() {
    }

    @Test
    public void testMissing() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline(true);
        DoubleSeq y = DataBlock.of(Data.PROD);
        int[] missingPos = new int[30];
        for (int i = 0; i < missingPos.length - 15; ++i) {
            missingPos[i] = i;
        }
        for (int i = missingPos.length - 15, j = 15; i < missingPos.length; ++i, --j) {
            missingPos[i] = y.length() - j;
        }
        SarimaModel arima = SarimaModel.builder(spec).build();
        RegArimaModel<SarimaModel> model = RegArimaModel.builder(SarimaModel.class)
                .y(y)
                .arima(arima)
                .meanCorrection(true)
                .missing(missingPos)
                .build();
//        Matrix variables = model.differencedModel().getLinearModel().variables();
//        assertTrue(variables.getColumnsCount() == 1 + missingPos.length
//                && variables.getRowsCount() == y.length() - spec.getDifferenceOrder());
    }

    @Test
    public void testEstimation() {

        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline(true);
        DoubleSeq y = DataBlock.of(Data.PROD);
        int[] missingPos = new int[15];
        for (int i = 0; i < missingPos.length; ++i) {
            missingPos[i] = 2 * i;
        }
        SarimaModel arima = SarimaModel.builder(spec).setDefault().build();
        RegArimaModel<SarimaModel> model = RegArimaModel.builder(SarimaModel.class)
                .y(y)
                .arima(arima)
                .meanCorrection(true)
                .missing(missingPos)
                .build();
        RegArimaEstimation<SarimaModel> estimation = RegArimaEstimation.of(model, 2);
        estimation.statistics(0);
//        System.out.println("New estimation");
//        System.out.println(estimation.statistics(0));
    }

    @Test
    public void testOldEstimation() {
        ec.tstoolkit.sarima.SarimaSpecification spec = new ec.tstoolkit.sarima.SarimaSpecification(12);
        spec.airline();
        ec.tstoolkit.data.DataBlock y = new ec.tstoolkit.data.DataBlock(Data.PROD);
        int[] missingPos = new int[15];
        for (int i = 0; i < missingPos.length; ++i) {
            missingPos[i] = 2 * i;
        }
        ec.tstoolkit.sarima.SarimaModel arima = new ec.tstoolkit.sarima.SarimaModel(spec);
        arima.setDefault();
        ec.tstoolkit.arima.estimation.RegArimaModel<ec.tstoolkit.sarima.SarimaModel> model = new ec.tstoolkit.arima.estimation.RegArimaModel(arima, y);
        model.setMeanCorrection(true);
        model.setMissings(missingPos);
        ec.tstoolkit.arima.estimation.ConcentratedLikelihoodEstimation est = new ec.tstoolkit.arima.estimation.ConcentratedLikelihoodEstimation();
        est.estimate(model);
        ec.tstoolkit.arima.estimation.RegArimaEstimation<ec.tstoolkit.sarima.SarimaModel> estimation
                = new ec.tstoolkit.arima.estimation.RegArimaEstimation(model, est.getLikelihood());
        estimation.statistics(2, 0);
//        System.out.println("Old estimation");
//        System.out.println(estimation.statistics(2, 0));
    }

    @Test
    @Ignore
    public void stressTestEstimation() {

        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline(true);
        DoubleSeq y = DataBlock.of(Data.PROD);
        int[] missingPos = new int[15];
        for (int i = 0; i < missingPos.length; ++i) {
            missingPos[i] = 2 * i;
        }
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 100000; ++i) {
            SarimaModel arima = SarimaModel.builder(spec).setDefault().build();
            RegArimaModel<SarimaModel> model = RegArimaModel.builder(SarimaModel.class)
                    .y(y)
                    .arima(arima)
                    .meanCorrection(true)
                    .missing(missingPos)
                    .build();
            RegArimaEstimation<SarimaModel> estimation = RegArimaEstimation.of(model, 2);
            estimation.statistics(0);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("New estimation");
        System.out.println(t1 - t0);
//        System.out.println(estimation.statistics(2, 0));
    }

    @Test
    @Ignore
    public void stressTestOldEstimation() {
        ec.tstoolkit.sarima.SarimaSpecification spec = new ec.tstoolkit.sarima.SarimaSpecification(12);
        spec.airline();
        ec.tstoolkit.data.DataBlock y = new ec.tstoolkit.data.DataBlock(Data.PROD);
        int[] missingPos = new int[15];
        for (int i = 0; i < missingPos.length; ++i) {
            missingPos[i] = 2 * i;
        }
        ec.tstoolkit.sarima.SarimaModel arima = new ec.tstoolkit.sarima.SarimaModel(spec);
        arima.setDefault();
        ec.tstoolkit.arima.estimation.RegArimaModel<ec.tstoolkit.sarima.SarimaModel> model = new ec.tstoolkit.arima.estimation.RegArimaModel(arima, y);
        model.setMeanCorrection(true);
        model.setMissings(missingPos);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 100000; ++i) {
            ec.tstoolkit.arima.estimation.ConcentratedLikelihoodEstimation est = new ec.tstoolkit.arima.estimation.ConcentratedLikelihoodEstimation();
            est.estimate(model);
            ec.tstoolkit.arima.estimation.RegArimaEstimation<ec.tstoolkit.sarima.SarimaModel> estimation
                    = new ec.tstoolkit.arima.estimation.RegArimaEstimation(model, est.getLikelihood());
            estimation.statistics(2, 0);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Old estimation");
        System.out.println(t1 - t0);
//        System.out.println(estimation.statistics(2, 0));
    }

//    @Test
//    public void testAO() {
//        
//        int POS=47;
//        SarimaSpecification spec = new SarimaSpecification(12);
//        spec.airline(true);
//        double[] ao = new double[Data.PROD.length];
//        ao[POS] = 1;
//        double[] y = Data.PROD.clone();
//        y[POS] -= 10;
//        for (int i = 1; i <= 60; ++i) {
//            DoubleSequence Y = DoubleSequence.ofInternal(y, 0, POS + i);
//            SarimaModel arima = SarimaModel.builder(spec)
//                    .theta(1, -.6)
//                    .btheta(1, -.5)
//                    .build();
//            RegArimaModel<SarimaModel> model = RegArimaModel.builder(SarimaModel.class)
//                    .y(Y)
//                    .arima(arima)
//                    //.meanCorrection(true)
//                    .addX(DoubleSequence.ofInternal(ao, 0, POS + i))
//                    .build();
//            RegArimaEstimation<SarimaModel> estimation = RegArimaEstimation.of(model, 2);
//            //System.out.println(estimation.getConcentratedLikelihood().coefficients().get(0));
//        }
//    }
//
//    @Test
//    public void testAO2() {
//        
//        int POS=47;
//        SarimaSpecification spec = new SarimaSpecification(12);
//        spec.setQ(1);
//        spec.setBq(1);
//        ec.tstoolkit.sarima.SarimaSpecification ospec = new ec.tstoolkit.sarima.SarimaSpecification(12);
//        ospec.setQ(1);
//        ospec.setBQ(1);
//        ec.tstoolkit.sarima.SarimaModel omodel=new ec.tstoolkit.sarima.SarimaModel(ospec);
//        omodel.setTheta(1, -.6);
//        omodel.setBTheta(1, -.6);
//        double[] y = new ec.tstoolkit.arima.ArimaModelBuilder().generate(omodel, 240);
//        double[] ao = new double[y.length];
//        ao[POS] = 1;
//        y[POS] += 5; //*Math.sqrt(DescriptiveStatistics.var(y, 0, y.length));
//        for (int i = 1; i <= 60; ++i) {
//            DoubleSequence Y = DoubleSequence.ofInternal(y, 0, POS + i);
//            SarimaModel arima = SarimaModel.builder(spec)
//                    .theta(1, -.6)
//                    .btheta(1, -.6)
//                    .build();
//            RegArimaModel<SarimaModel> model = RegArimaModel.builder(SarimaModel.class)
//                    .y(Y)
//                    .arima(arima)
////                    .meanCorrection(true)
//                    .addX(DoubleSequence.ofInternal(ao, 0, POS + i))
//                    .build();
//            RegArimaEstimation<SarimaModel> estimation = RegArimaEstimation.of(model, 2);
//            System.out.println(estimation.getConcentratedLikelihood().coefficients().get(0));
//        }
//    }
//    
//    @Test
//    public void testAO3() {
//        System.out.println();
//        int POS=47;
//        SarimaSpecification spec = new SarimaSpecification(12);
//        spec.airline(true);
//        ec.tstoolkit.sarima.SarimaSpecification ospec = new ec.tstoolkit.sarima.SarimaSpecification(12);
//        ospec.airline(true);
//        ec.tstoolkit.sarima.SarimaModel omodel=new ec.tstoolkit.sarima.SarimaModel(ospec);
//        omodel.setTheta(1, -.6);
//        omodel.setBTheta(1, -.6);
//        double[] y = new ec.tstoolkit.arima.ArimaModelBuilder().generate(omodel, 240);
//        double[] ao = new double[y.length];
//        ao[POS] = 1;
//        y[POS] += 5; //*Math.sqrt(DescriptiveStatistics.var(y, 0, y.length));
//        for (int i = 1; i <= 60; ++i) {
//            DoubleSequence Y = DoubleSequence.ofInternal(y, 0, POS + i);
//            SarimaModel arima = SarimaModel.builder(spec)
//                    .theta(1, -.6)
//                    .btheta(1, -.6)
//                    .build();
//            RegArimaModel<SarimaModel> model = RegArimaModel.builder(SarimaModel.class)
//                    .y(Y)
//                    .arima(arima)
////                    .meanCorrection(true)
//                    .addX(DoubleSequence.ofInternal(ao, 0, POS + i))
//                    .build();
//            RegArimaEstimation<SarimaModel> estimation = RegArimaEstimation.of(model, 2);
//            System.out.println(estimation.getConcentratedLikelihood().coefficients().get(0));
//        }
//    }

}
