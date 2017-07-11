/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.demetra;

import data.Data;
import ec.tstoolkit.arima.ArimaModelBuilder;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.random.MersenneTwister;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.SarmaSpecification;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.sarima.estimation.HannanRissanen;
import ec.tstoolkit.timeseries.simplets.TsData;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class HannanRissanen2Test {

    public HannanRissanen2Test() {
    }

    @Test
    public void testSomeMethod() {
        SarmaSpecification spec = new SarmaSpecification(12);
        spec.setP(3);
        spec.setBP(0);
        spec.setQ(1);
        spec.setBQ(1);
        SarimaModelBuilder builder = new SarimaModelBuilder();
        //builder.setRandomNumberGenerator(new MersenneTwister(1));
        SarimaModel model = new SarimaModel(spec);
        SarimaModel arima = builder.randomize(model, 1);
//        System.out.println(arima);
        ArimaModelBuilder gen = new ArimaModelBuilder();
        double[] data = gen.generateStationary(arima, 240);
        HannanRissanen hr = new HannanRissanen();
        hr.process(new ReadDataBlock(data), spec);
//        System.out.println(hr.getModel());
        HannanRissanen2 hr2 = new HannanRissanen2();
        hr2.process(new ReadDataBlock(data), spec);
//        System.out.println(hr2.getModel());
    }

    @Test
    @Ignore
    public void testRandom() {
        SarmaSpecification spec = new SarmaSpecification(12);
        //spec.setP(3);
        spec.setBP(1);
        //spec.setQ(1);
        //spec.setBQ(1);
        SarimaModelBuilder builder = new SarimaModelBuilder();
        //builder.setRandomNumberGenerator(new MersenneTwister(1));
        SarimaModel model = new SarimaModel(spec);
        SarimaModel arima = builder.randomize(model, 1);
        System.out.println(arima);
        ArimaModelBuilder gen = new ArimaModelBuilder();
        double[] data = gen.generateStationary(arima, 120);
        HannanRissanen2 hr2 = new HannanRissanen2();
        hr2.setBiasCorrection(false);
        hr2.setFinalCorrection(false);
        RegArimaModel<SarimaModel> regarima = new RegArimaModel<>();
        ReadDataBlock s = new ReadDataBlock(data);
        regarima.setY(s);
        for (int p = 0; p <= 3; ++p) {
            for (int q = 0; q <= 3; ++q) {
                for (int bp = 0; bp <= 1; ++bp) {
                    for (int bq = 0; bq <= 1; ++bq) {
                        SarmaSpecification xspec = new SarmaSpecification(12);
                        xspec.setP(p);
                        xspec.setQ(q);
                        xspec.setBP(bp);
                        xspec.setBQ(bq);
                        hr2.process(s, xspec);

                        System.out.print(p);
                        System.out.print('\t');
                        System.out.print(q);
                        System.out.print('\t');
                        System.out.print(bp);
                        System.out.print('\t');
                        System.out.print(bq);
                        System.out.print('\t');
                        System.out.print(hr2.getBic());
                        System.out.print('\t');
                        GlsSarimaMonitor monitor = new GlsSarimaMonitor();
                        regarima.setArima(new SarimaModel(xspec));
                        double bic = monitor.process(regarima).likelihood.BIC(xspec.getParametersCount());
                        System.out.println(bic);

                    }
                }
            }

        }
    }

    @Test
    @Ignore
    public void stressTestRandom() {
        int K = 1000, N = 240;
        SarmaSpecification spec = new SarmaSpecification(12);
        spec.setP(3);
        //spec.setBP(1);
        spec.setQ(1);
        spec.setBQ(1);
        SarimaModelBuilder builder = new SarimaModelBuilder();
        //builder.setRandomNumberGenerator(new MersenneTwister(1));
        SarimaModel model = new SarimaModel(spec);
        SarimaModel arima = builder.randomize(model, 1);
        System.out.println(arima);
        ArimaModelBuilder gen = new ArimaModelBuilder();
        double[] data = gen.generateStationary(arima, N);
        HannanRissanen2 hr2 = new HannanRissanen2();
        hr2.setBiasCorrection(false);
        hr2.setFinalCorrection(false);
        RegArimaModel<SarimaModel> regarima = new RegArimaModel<>();
        ReadDataBlock s = new ReadDataBlock(data);
        regarima.setY(s);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            for (int p = 0; p <= 3; ++p) {
                for (int q = 0; q <= 3; ++q) {
                    for (int bp = 0; bp <= 1; ++bp) {
                        for (int bq = 0; bq <= 1; ++bq) {
                            SarmaSpecification xspec = new SarmaSpecification(12);
                            xspec.setP(p);
                            xspec.setQ(q);
                            xspec.setBP(bp);
                            xspec.setBQ(bq);
                            hr2.process(s, xspec);
                        }
                    }
                }
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            for (int p = 0; p <= 3; ++p) {
                for (int q = 0; q <= 3; ++q) {
                    for (int bp = 0; bp <= 1; ++bp) {
                        for (int bq = 0; bq <= 1; ++bq) {
                            SarmaSpecification xspec = new SarmaSpecification(12);
                            xspec.setP(p);
                            xspec.setQ(q);
                            xspec.setBP(bp);
                            xspec.setBQ(bq);
                            GlsSarimaMonitor monitor = new GlsSarimaMonitor();
                            regarima.setArima(new SarimaModel(xspec));
                            double bic = monitor.process(regarima).likelihood.BIC(xspec.getParametersCount());
                        }
                    }
                }
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
    @Ignore
    public void testActual() {
        PreprocessingModel mtramo = TramoSpecification.TRfull.build().process(Data.P, null);
        TsData s = mtramo.linearizedSeries();
        int d = mtramo.description.getArimaComponent().getD();
        int bd = mtramo.description.getArimaComponent().getBD();
        if (d > 0) {
            s = s.delta(1, d);
        }
        if (bd > 0) {
            s = s.delta(12, bd);
        }
        HannanRissanen2 hr2 = new HannanRissanen2();
        hr2.setBiasCorrection(false);
        hr2.setFinalCorrection(false);
        RegArimaModel<SarimaModel> regarima = new RegArimaModel<>();
        regarima.setY(s);
        for (int p = 0; p <= 3; ++p) {
            for (int q = 0; q <= 3; ++q) {
                for (int bp = 0; bp <= 1; ++bp) {
                    for (int bq = 0; bq <= 1; ++bq) {
                        SarmaSpecification xspec = new SarmaSpecification(12);
                        xspec.setP(p);
                        xspec.setQ(q);
                        xspec.setBP(bp);
                        xspec.setBQ(bq);
                        hr2.process(s, xspec);

                        System.out.print(p);
                        System.out.print('\t');
                        System.out.print(q);
                        System.out.print('\t');
                        System.out.print(bp);
                        System.out.print('\t');
                        System.out.print(bq);
                        System.out.print('\t');
                        System.out.print(hr2.getBic());
                        System.out.print('\t');
                        GlsSarimaMonitor monitor = new GlsSarimaMonitor();
                        regarima.setArima(new SarimaModel(xspec));
                        double bic = monitor.process(regarima).likelihood.BIC(xspec.getParametersCount());
                        System.out.println(bic);

                    }
                }
            }

        }
    }
}
