/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.regarima;

import demetra.data.Data;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.maths.matrices.Matrix;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class RegArimaModelTest {

    public RegArimaModelTest() {
    }

    @Test
    public void testMissing() {
        SarimaSpecification spec = new SarimaSpecification();
        spec.airline(12);
        DoubleSequence y = DataBlock.ofInternal(Data.PROD);
        int[] missingPos = new int[30];
        for (int i = 0; i < missingPos.length - 15; ++i) {
            missingPos[i] = i;
        }
        for (int i = missingPos.length - 15, j = 15; i < missingPos.length; ++i, --j) {
            missingPos[i] = y.length() - j;
        }
        SarimaModel arima = SarimaModel.builder(spec).build();
        RegArimaModel<SarimaModel> model = RegArimaModel.builder(y, arima)
                .meanCorrection(true)
                .missing(missingPos)
                .build();
//        Matrix variables = model.differencedModel().getLinearModel().variables();
//        assertTrue(variables.getColumnsCount() == 1 + missingPos.length
//                && variables.getRowsCount() == y.length() - spec.getDifferenceOrder());
    }

    @Test
    public void testEstimation() {

        SarimaSpecification spec = new SarimaSpecification();
        spec.airline(12);
        DoubleSequence y = DataBlock.ofInternal(Data.PROD);
        int[] missingPos = new int[15];
        for (int i = 0; i < missingPos.length; ++i) {
            missingPos[i] = 2 * i;
        }
        SarimaModel arima = SarimaModel.builder(spec).setDefault().build();
        RegArimaModel<SarimaModel> model = RegArimaModel.builder(y, arima)
                .meanCorrection(true)
                .missing(missingPos)
                .build();
        RegArimaEstimation<SarimaModel> estimation = RegArimaEstimation.compute(model);
        estimation.statistics(2, 0);
//        System.out.println("New estimation");
//        System.out.println(estimation.statistics(2, 0));
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

        SarimaSpecification spec = new SarimaSpecification();
        spec.airline(12);
        DoubleSequence y = DataBlock.ofInternal(Data.PROD);
        int[] missingPos = new int[15];
        for (int i = 0; i < missingPos.length; ++i) {
            missingPos[i] = 2 * i;
        }
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 100000; ++i) {
            SarimaModel arima = SarimaModel.builder(spec).setDefault().build();
            RegArimaModel<SarimaModel> model = RegArimaModel.builder(y, arima)
                    .meanCorrection(true)
                    .missing(missingPos)
                    .build();
            RegArimaEstimation<SarimaModel> estimation = RegArimaEstimation.compute(model);
            estimation.statistics(2, 0);
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

}
