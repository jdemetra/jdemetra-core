/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.fractionalairline;

import jdplus.arima.ArimaModel;
import jdplus.regarima.GlsArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.highfreq.FractionalAirlineDecomposition;
import demetra.highfreq.FractionalAirlineEstimation;
import demetra.highfreq.FractionalAirlineSpec;
import demetra.highfreq.SeriesComponent;
import demetra.modelling.OutlierDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jdplus.arima.IArimaModel;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockStorage;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.likelihood.LogLikelihoodFunction;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.ApiUtility;
import jdplus.modelling.regression.AdditiveOutlierFactory;
import jdplus.modelling.regression.IOutlierFactory;
import jdplus.modelling.regression.LevelShiftFactory;
import jdplus.modelling.regression.SwitchOutlierFactory;
import jdplus.regarima.ami.OutliersDetectionModule;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.implementations.CompositeSsf;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.ExtendedSsfData;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.SsfData;
import jdplus.ucarima.AllSelector;
import jdplus.ucarima.ModelDecomposer;
import jdplus.ucarima.SeasonalSelector;
import jdplus.ucarima.TrendCycleSelector;
import jdplus.ucarima.UcarimaModel;
import jdplus.ucarima.ssf.SsfUcarima;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class FractionalAirlineKernel {

    public FractionalAirlineEstimation process(FractionalAirlineSpec spec) {
        final MultiPeriodicAirlineMapping mapping = new MultiPeriodicAirlineMapping(spec.getPeriodicities(), false, spec.getDifferencingOrder());
        double[] y = spec.getY();
        RegArimaModel.Builder builder = RegArimaModel.<ArimaModel>builder()
                .y(DoubleSeq.of(y))
                .addX(FastMatrix.of(spec.getX()))
                .arima(mapping.getDefault())
                .meanCorrection(spec.isMeanCorrection());
        OutlierDescriptor[] o = null;
        if (spec.getOutliers() != null) {
            GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                    .precision(1e-5)
                    .build();
            IOutlierFactory[] factories = factories(spec.getOutliers());
            OutliersDetectionModule od = OutliersDetectionModule.build(ArimaModel.class)
                    .maxOutliers(100)
                    .addFactories(factories)
                    .processor(processor)
                    .build();
            od.setCriticalValue(spec.getCriticalValue());
            RegArimaModel regarima = builder.build();
            od.prepare(regarima.getObservationsCount());
            od.process(regarima, mapping);
            int[][] io = od.getOutliers();
            o = new OutlierDescriptor[io.length];
            for (int i = 0; i < io.length; ++i) {
                int[] cur = io[i];
                DataBlock xcur = DataBlock.make(y.length);
                factories[cur[1]].fill(cur[0], xcur);
                o[i] = new OutlierDescriptor(factories[cur[1]].getCode(), cur[0]);
                builder.addX(xcur);
            }
        }
        RegArimaModel regarima = builder.build();
        GlsArimaProcessor<ArimaModel> finalProcessor = GlsArimaProcessor.builder(ArimaModel.class)
                .precision(spec.getPrecision())
                .computeExactFinalDerivatives(!spec.isApproximateHessian())
                .build();
        RegArimaEstimation rslt = finalProcessor.process(regarima, mapping);
        LogLikelihoodFunction.Point max = rslt.getMax();

        return FractionalAirlineEstimation.builder()
                .y(regarima.getY().toArray())
                .x(regarima.variables())
                .model(new demetra.highfreq.FractionalAirline(spec.getPeriodicities(), max.getParameters(), spec.getDifferencingOrder()))
                .coefficients(rslt.getConcentratedLikelihood().coefficients())
                .coefficientsCovariance(rslt.getConcentratedLikelihood().covariance(2, true))
                .likelihood(rslt.statistics())
                .outliers(o)
                .parameters(max.getParameters())
                .parametersCovariance(max.asymptoticCovariance())
                .score(max.getScore())
                .build();
    }

    public FractionalAirlineDecomposition decompose(DoubleSeq s, double period, boolean sn, boolean cov, int nb, int nf) {
        FractionalAirlineMapping mapping = new FractionalAirlineMapping(period, false);

        GlsArimaProcessor.Builder<ArimaModel> builder = GlsArimaProcessor.builder(ArimaModel.class);
        builder.minimizer(LevenbergMarquardtMinimizer.builder())
                .precision(1e-12)
                .useMaximumLikelihood(true)
                .useParallelProcessing(true)
                .build();
        ArimaModel arima = mapping.getDefault();
        RegArimaModel<ArimaModel> regarima
                = RegArimaModel.<ArimaModel>builder()
                        .y(s)
                        .arima(arima)
                        .build();
        GlsArimaProcessor<ArimaModel> monitor = builder.build();
        RegArimaEstimation<ArimaModel> rslt = monitor.process(regarima, mapping);
        LogLikelihoodFunction.Point<RegArimaModel<ArimaModel>, ConcentratedLikelihoodWithMissing> max = rslt.getMax();
        UcarimaModel ucm = ucm(rslt.getModel().arima(), sn);

        demetra.arima.ArimaModel sum = ApiUtility.toApi(ucm.getModel(), "sum");
        demetra.arima.UcarimaModel ucmt;
        if (sn) {
            demetra.arima.ArimaModel mn = ApiUtility.toApi(ucm.getComponent(0), "noise");
            demetra.arima.ArimaModel ms = ApiUtility.toApi(ucm.getComponent(1), "signal");
            ucmt = new demetra.arima.UcarimaModel(sum, new demetra.arima.ArimaModel[]{ms, mn});

        } else {
            demetra.arima.ArimaModel mt = ApiUtility.toApi(ucm.getComponent(0), "trend");
            demetra.arima.ArimaModel ms = ApiUtility.toApi(ucm.getComponent(1), "seasonal");
            demetra.arima.ArimaModel mi = ApiUtility.toApi(ucm.getComponent(2), "irregular");
            ucmt = new demetra.arima.UcarimaModel(sum, new demetra.arima.ArimaModel[]{mt, ms, mi});
        }
        FractionalAirlineDecomposition.Builder dbuilder = FractionalAirlineDecomposition.builder()
                .model(new demetra.highfreq.FractionalAirline(new double[]{period}, max.getParameters(), 2))
                .likelihood(rslt.statistics())
                .parameters(max.getParameters())
                .parametersCovariance(max.asymptoticCovariance())
                .score(max.getScore())
                .ucarima(ucmt);
        CompositeSsf ssf = SsfUcarima.of(ucm);
        ISsfData data = new ExtendedSsfData(new SsfData(s), nb, nf);
        int[] pos = ssf.componentsPosition();
        DoubleSeq yc = s;
        if (cov) {
            try {
                DefaultSmoothingResults sr = DkToolkit.sqrtSmooth(ssf, data, true, true);
                if (sn) {
                    DoubleSeq sc = sr.getComponent(pos[1]), nc = sr.getComponent(pos[0]);
                    if (nb > 0 || nf > 0) {
                        DataBlock q = DataBlock.of(nc);
                        q.add(sc);
                        q.drop(nb, nf).copy(s);
                        yc = q;
                    }
                    return dbuilder
                            .y(yc)
                            .component(new SeriesComponent("S", sc, sr.getComponentVariance(pos[1]).fn(a -> a <= 0 ? 0 : Math.sqrt(a))))
                            .component(new SeriesComponent("N", nc, sr.getComponentVariance(pos[1]).fn(a -> a <= 0 ? 0 : Math.sqrt(a))))
                            .build();
                } else {
                    DoubleSeq sc = sr.getComponent(pos[1]), tc = sr.getComponent(pos[0]), ic = sr.getComponent(pos[2]);
                    if (nb > 0 || nf > 0) {
                        DataBlock q = DataBlock.of(tc);
                        q.add(sc);
                        q.add(ic);
                        q.drop(nb, nf).copy(s);
                        yc = q;
                    }
                    return dbuilder
                            .y(yc)
                            .component(new SeriesComponent("T", tc.commit(), sr.getComponentVariance(pos[0]).fn(a -> a <= 0 ? 0 : Math.sqrt(a))))
                            .component(new SeriesComponent("S", sc.commit(), sr.getComponentVariance(pos[1]).fn(a -> a <= 0 ? 0 : Math.sqrt(a))))
                            .component(new SeriesComponent("I", ic.commit(), sr.getComponentVariance(pos[2]).fn(a -> a <= 0 ? 0 : Math.sqrt(a))))
                            .build();
                }
            } catch (Exception err) {
            }
        }

        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);
        if (sn) {
            DoubleSeq sc = ds.item(pos[1]), nc = ds.item(pos[0]);
            if (nb > 0 || nf > 0) {
                DataBlock q = DataBlock.of(nc);
                q.add(sc);
                q.drop(nb, nf).copy(s);
                yc = q;
            }
            return dbuilder
                    .y(yc)
                    .component(new SeriesComponent("S", ds.item(pos[1]).commit(), DoubleSeq.empty()))
                    .component(new SeriesComponent("N", ds.item(pos[0]).commit(), DoubleSeq.empty()))
                    .build();
        } else {
            DoubleSeq sc = ds.item(pos[1]), tc = ds.item(pos[0]), ic = ds.item(pos[2]);
            if (nb > 0 || nf > 0) {
                DataBlock q = DataBlock.of(tc);
                q.add(sc);
                q.add(ic);
                q.drop(nb, nf).copy(s);
                yc = q;
            }
            return dbuilder
                    .y(yc)
                    .component(new SeriesComponent("S", sc.commit(), DoubleSeq.empty()))
                    .component(new SeriesComponent("T", tc.commit(), DoubleSeq.empty()))
                    .component(new SeriesComponent("I", ic.commit(), DoubleSeq.empty()))
                    .build();
        }
    }

    public FractionalAirlineDecomposition decompose(DoubleSeq s, double[] periods, int ndiff, boolean cov, int nb, int nf) {

        if (periods.length == 1) {
            return decompose(s, periods[0], false, cov, nb, nf);
        }

        double[] dp = periods.clone();
        Arrays.sort(dp);
        int[] ip = new int[dp.length - 1];
        for (int i = 0; i < ip.length; ++i) {
            int p = (int) dp[i];
            if (Math.abs(dp[i] - p) < 1e-9) {
                dp[i] = p;
                ip[i] = p;
            } else {
                throw new IllegalArgumentException("Period " + dp[i] + " should be integer");
            }
        }

        final MultiPeriodicAirlineMapping mapping = new MultiPeriodicAirlineMapping(dp, false, ndiff);

        GlsArimaProcessor.Builder<ArimaModel> builder = GlsArimaProcessor.builder(ArimaModel.class);
        builder.minimizer(LevenbergMarquardtMinimizer.builder())
                .precision(1e-12)
                .useMaximumLikelihood(true)
                .useParallelProcessing(true)
                .build();
        ArimaModel arima = mapping.getDefault();
        RegArimaModel<ArimaModel> regarima
                = RegArimaModel.<ArimaModel>builder()
                        .y(s)
                        .arima(arima)
                        .build();
        GlsArimaProcessor<ArimaModel> monitor = builder.build();
        RegArimaEstimation<ArimaModel> rslt = monitor.process(regarima, mapping);
        LogLikelihoodFunction.Point<RegArimaModel<ArimaModel>, ConcentratedLikelihoodWithMissing> max = rslt.getMax();
        UcarimaModel ucm = ucm(rslt.getModel().arima(), ip);

        demetra.arima.ArimaModel sum = ApiUtility.toApi(ucm.getModel(), "sum");
        demetra.arima.UcarimaModel ucmt;
        demetra.arima.ArimaModel[] all = new demetra.arima.ArimaModel[ucm.getComponentsCount()];
        for (int i = 0; i < all.length; ++i) {
            demetra.arima.ArimaModel m = ApiUtility.toApi(ucm.getComponent(i), "cmp" + (i + 1));
            all[i] = m;
        }
        ucmt = new demetra.arima.UcarimaModel(sum, all);
        FractionalAirlineDecomposition.Builder dbuilder = FractionalAirlineDecomposition.builder()
                .model(new demetra.highfreq.FractionalAirline(dp, max.getParameters(), ndiff))
                .likelihood(rslt.statistics())
                .parameters(max.getParameters())
                .parametersCovariance(max.asymptoticCovariance())
                .score(max.getScore())
                .ucarima(ucmt);
        CompositeSsf ssf = SsfUcarima.of(ucm);
        ISsfData data = new ExtendedSsfData(new SsfData(s), nb, nf);
        int[] pos = ssf.componentsPosition();
        DoubleSeq sc = s;
        if (cov) {
            try {
                DefaultSmoothingResults sr = DkToolkit.sqrtSmooth(ssf, data, true, true);
                if (nb > 0 || nf > 0) {
                    DataBlock q = DataBlock.of(sr.getComponent(pos[0]));
                    for (int i = 1; i < pos.length; ++i) {
                        q.add(sr.getComponent(pos[i]));
                    }
                    sc = q;
                }
                for (int i = 0; i < pos.length; ++i) {
                    dbuilder.component(new SeriesComponent("cmp" + (i + 1),
                            sr.getComponent(pos[i]).commit(),
                            sr.getComponentVariance(i).fn(a -> a <= 0 ? 0 : Math.sqrt(a))));
                }
                return dbuilder
                        .y(sc)
                        .build();
            } catch (Exception err) {
            }
        }

        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);
        if (nb > 0 || nf > 0) {
            sc = ds.item(pos[0]);
            for (int i = 1; i < pos.length; ++i) {
                sc = DoublesMath.add(sc, ds.item(pos[i]));
            }
        }
        for (int i = 0; i < pos.length; ++i) {
            dbuilder.component(new SeriesComponent("Cmp" + (i + 1),
                    ds.item(pos[i]).commit(), DoubleSeq.empty()));
        }
        return dbuilder
                .y(sc)
                .build();
    }

    public UcarimaModel ucm(IArimaModel arima, boolean sn) {

        TrendCycleSelector tsel = new TrendCycleSelector();
        AllSelector ssel = new AllSelector();

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(arima);
        if (sn) {
            ucm = ucm.setVarianceMax(0, true);
        } else {
            ucm = ucm.setVarianceMax(-1, true);
        }
        return ucm.simplify();
    }

    public UcarimaModel ucm(IArimaModel arima, int[] periods) {

        TrendCycleSelector tsel = new TrendCycleSelector();
        AllSelector ssel = new AllSelector();

        int[] np = periods.clone();
        Arrays.sort(np);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        for (int i = 0; i < np.length; ++i) {
            decomposer.add(new SeasonalSelector(np[i], 1e-6));
        }
        decomposer.add(ssel);
        UcarimaModel ucm = decomposer.decompose(arima);
        ucm = ucm.setVarianceMax(-1, true);
        return ucm.simplify();
    }

    private IOutlierFactory[] factories(String[] code) {
        List<IOutlierFactory> fac = new ArrayList<>();
        for (int i = 0; i < code.length; ++i) {
            switch (code[i]) {
                case "ao":
                case "AO":
                    fac.add(AdditiveOutlierFactory.FACTORY);
                    break;
                case "wo":
                case "WO":
                    fac.add(SwitchOutlierFactory.FACTORY);
                    break;
                case "ls":
                case "LS":
                    fac.add(LevelShiftFactory.FACTORY_ZEROENDED);
                    break;
            }
        }
        return fac.toArray(new IOutlierFactory[fac.size()]);
    }

}
