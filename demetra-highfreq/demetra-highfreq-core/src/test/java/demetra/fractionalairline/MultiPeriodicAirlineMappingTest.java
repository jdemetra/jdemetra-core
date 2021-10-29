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
package demetra.fractionalairline;

import jdplus.fractionalairline.MultiPeriodicAirlineMapping;
import jdplus.arima.ArimaModel;
import jdplus.arima.IArimaModel;
import demetra.data.DoubleSeq;
import demetra.data.MatrixSerializer;
import demetra.math.functions.Optimizer;
import jdplus.modelling.regression.AdditiveOutlierFactory;
import jdplus.modelling.regression.LevelShiftFactory;
import jdplus.modelling.regression.SwitchOutlierFactory;
import jdplus.regarima.GlsArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.ami.OutliersDetectionModule;
import jdplus.ucarima.AllSelector;
import jdplus.ucarima.ModelDecomposer;
import jdplus.ucarima.SeasonalSelector;
import jdplus.ucarima.TrendCycleSelector;
import jdplus.ucarima.UcarimaModel;
import java.io.IOException;
import java.util.function.Consumer;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import demetra.ssf.SsfInitialization;
import java.io.InputStream;
import jdplus.data.DataBlockStorage;
import jdplus.math.linearfilters.RationalFilter;
import jdplus.math.matrices.FastMatrix;
import jdplus.msts.AtomicModels;
import jdplus.msts.CompositeModel;
import jdplus.msts.CompositeModelEstimation;
import jdplus.msts.ModelEquation;
import jdplus.msts.StateItem;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.implementations.CompositeSsf;
import jdplus.ssf.implementations.MultivariateCompositeSsf;
import jdplus.ssf.multivariate.M2uAdapter;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.SsfData;
import jdplus.ucarima.WienerKolmogorovEstimators;
import jdplus.ucarima.ssf.SsfUcarima;
import demetra.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
public class MultiPeriodicAirlineMappingTest {

    public MultiPeriodicAirlineMappingTest() {
    }

//    @Test
    public static void testDaily1() throws IOException {
        InputStream stream = MultiPeriodicAirlineMapping.class.getResourceAsStream("/edf.txt");
        Matrix edf = MatrixSerializer.read(stream);
        DoubleSeq y = edf.column(0).log();
        final MultiPeriodicAirlineMapping mapping = new MultiPeriodicAirlineMapping(new double[]{7}, false, 2);
        GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                .precision(1e-7)
                .build();
        RegArimaModel<ArimaModel> regarima = RegArimaModel.<ArimaModel>builder()
                .y(y)
                .arima(mapping.getDefault())
                .build();
        RegArimaEstimation<ArimaModel> estimation = processor.process(regarima, mapping);
        assertTrue(estimation != null);
        System.out.println(estimation.getMax().getParameters());
        UcarimaModel ucm = ucm(estimation.getModel().arima(), true);
        ucm=ucm.simplify();
        assertTrue(ucm.isValid());
        System.out.println(ucm);
        System.out.println(y);
        CompositeSsf ssf = SsfUcarima.of(ucm);
        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, new SsfData(y));
        int[] cmp = ssf.componentsPosition();
        for (int i = 0; i < cmp.length; ++i) {
            System.out.println(ds.item(cmp[i]));
        }
        WienerKolmogorovEstimators estimators = new WienerKolmogorovEstimators(ucm);
//        for (int i = 0; i < cmp.length; ++i) {
//            System.out.println(DoubleSeq.onMapping(1000, estimators.finalEstimator(i, true).getWienerKolmogorovFilter().weights()));
//        }
        for (int i = 0; i < cmp.length; ++i) {
            RationalFilter wk = estimators.finalEstimator(i, true).getWienerKolmogorovFilter();
            System.out.println(DoubleSeq.onMapping(1000,j->wk.frequencyResponse(j*Math.PI/1000).absSquare()));
        }
//        System.out.println(ucm.getComponent(2).getMA().asPolynomial().coefficients());
    }

//    @Test
    public static void testDaily2() throws IOException {
        InputStream stream = MultiPeriodicAirlineMapping.class.getResourceAsStream("/edf.txt");
        Matrix edf = MatrixSerializer.read(stream);
        DoubleSeq y = edf.column(0).log();
        final MultiPeriodicAirlineMapping mapping = new MultiPeriodicAirlineMapping(new double[]{7, 365}, false, -1);
        GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                .precision(1e-7)
                .build();
        RegArimaModel<ArimaModel> regarima = RegArimaModel.<ArimaModel>builder()
                .y(y)
                .arima(mapping.getDefault())
                .build();
        RegArimaEstimation<ArimaModel> estimation = processor.process(regarima, mapping);
        assertTrue(estimation != null);
        UcarimaModel ucm = ucm(estimation.getModel().arima(), true);
        assertTrue(ucm.isValid());
        System.out.println(ucm);
        System.out.println(y);
        CompositeSsf ssf = SsfUcarima.of(ucm);
        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, new SsfData(y));
        int[] cmp = ssf.componentsPosition();
        for (int i = 0; i < cmp.length; ++i) {
            System.out.println(ds.item(cmp[i]));
        }
    }

    //@Test
    public static void testDaily3() throws IOException {
        InputStream stream = MultiPeriodicAirlineMapping.class.getResourceAsStream("/edf.txt");
        Matrix edf = MatrixSerializer.read(stream);
        final MultiPeriodicAirlineMapping mapping = new MultiPeriodicAirlineMapping(new double[]{7, 365}, true, 2);
        GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                .precision(1e-5)
                .build();
        RegArimaModel<ArimaModel> regarima = RegArimaModel.<ArimaModel>builder()
                .y(edf.column(0))
                .arima(mapping.getDefault())
                .build();
        RegArimaEstimation<ArimaModel> estimation = processor.process(regarima, mapping);
        assertTrue(estimation != null);
        UcarimaModel ucm = ucm(estimation.getModel().arima(), true);
        assertTrue(ucm.isValid());
        System.out.println(edf.column(0));
        CompositeSsf ssf = SsfUcarima.of(ucm);
        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, new SsfData(edf.column(0)));
        int[] cmp = ssf.componentsPosition();
        for (int i = 0; i < cmp.length; ++i) {
            System.out.println(ds.item(cmp[i]));
        }
    }

//    @Test
    public static void testDailySts() throws IOException {
        InputStream stream = MultiPeriodicAirlineMapping.class.getResourceAsStream("/edf.txt");
        Matrix edf = MatrixSerializer.read(stream);
        CompositeModel model = new CompositeModel();
        StateItem l = AtomicModels.localLinearTrend("l", .01, 0.01, false, false);
        StateItem sd = AtomicModels.seasonalComponent("sd", "HarrisonStevens", 7, .01, false);
        StateItem sy = AtomicModels.seasonalComponent("sy", "HarrisonStevens", 365, .01, false);
        StateItem n = AtomicModels.noise("n", .01, false);
        ModelEquation eq = new ModelEquation("eq1", 0, true);
        eq.add(l);
        eq.add(sd);
        eq.add(sy);
        eq.add(n);
        model.add(l);
        model.add(sd);
        model.add(sy);
        model.add(n);
        model.add(eq);
        FastMatrix M = FastMatrix.of(edf);
        M.apply(Math::log);
        CompositeModelEstimation rslt = model.estimate(M, false, true, SsfInitialization.Diffuse, Optimizer.LevenbergMarquardt, 1e-6, null);
        MultivariateCompositeSsf ssf = rslt.getSsf();
        ISsf ussf = M2uAdapter.of(ssf);
        ISsfData udata = new SsfData(M.column(0));
        DataBlockStorage ds = DkToolkit.fastSmooth(ussf, udata);
        int[] cmpPos = rslt.getCmpPos();
        for (int i = 0; i < cmpPos.length; ++i) {
            System.out.println(ds.item(cmpPos[i]));
        }
    }

    @Test
    @Ignore
    public void testOutliers() throws IOException {
        InputStream stream = MultiPeriodicAirlineMapping.class.getResourceAsStream("/births.txt");
        Matrix edf = MatrixSerializer.read(stream);
        final MultiPeriodicAirlineMapping mapping = new MultiPeriodicAirlineMapping(new double[]{7, 365.25}, true, -1);
        GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                .precision(1e-5)
                .build();
        RegArimaModel<ArimaModel> regarima = RegArimaModel.<ArimaModel>builder()
                .y(edf.column(0).range(5000, 8000))
                .arima(mapping.getDefault())
                .build();
        OutliersDetectionModule od = OutliersDetectionModule.build(ArimaModel.class)
                .addFactory(AdditiveOutlierFactory.FACTORY)
                .addFactory(SwitchOutlierFactory.FACTORY)
                .addFactory(LevelShiftFactory.FACTORY_ZEROSTARTED)
                .maxOutliers(100)
                .processor(processor)
                .build();
        Consumer<int[]> hook = c -> {
            String str = c[0] + "-" + c[1];
            System.out.println(str);
        };
        od.setAddHook(hook);
        od.setCriticalValue(5);
        od.prepare(regarima.getObservationsCount());
        od.process(regarima, mapping);
    }

    public static UcarimaModel ucm(IArimaModel arima, boolean week) {

        TrendCycleSelector tsel = new TrendCycleSelector();
        AllSelector ssel = new AllSelector();

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        if (week) {
            decomposer.add(new SeasonalSelector(7));
        }
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(arima);
        ucm = ucm.setVarianceMax(-1, true);
        return ucm;
    }

    public static void main(String[] args) throws IOException {
        testDaily1();
//        testDaily2();
//        testDaily3();
//        testDailySts();
    }
}
