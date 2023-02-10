/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.highfreq.extendedairline.decomposiiton;

import jdplus.highfreq.extendedairline.ExtendedAirlineMapping;
import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.highfreq.ExtendedAirline;
import demetra.highfreq.SeriesComponent;
import demetra.sa.ComponentType;
import java.util.Arrays;
import jdplus.arima.ArimaModel;
import jdplus.arima.IArimaModel;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockStorage;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.regarima.GlsArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.composite.CompositeSsf;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.ExtendedSsfData;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.SsfData;
import jdplus.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.stats.likelihood.LogLikelihoodFunction;
import jdplus.ucarima.AllSelector;
import jdplus.ucarima.ModelDecomposer;
import jdplus.ucarima.SeasonalSelector;
import jdplus.ucarima.TrendCycleSelector;
import jdplus.ucarima.UcarimaModel;
import jdplus.ssf.arima.SsfUcarima;

/**
 *
 * @author PALATEJ
 */
public class ExtendedAirlineDecomposer {

    public static LightExtendedAirlineDecomposition decompose(DoubleSeq s, double period, boolean sn, boolean cov, int nb, int nf) {
        ExtendedAirlineMapping mapping = new ExtendedAirlineMapping(new double[]{period});

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

        IArimaModel sum = ucm.getModel();
        UcarimaModel ucmt;
        if (sn) {
            ArimaModel mn = ucm.getComponent(0);
            ArimaModel ms = ucm.getComponent(1);
            ucmt = UcarimaModel.builder()
                    .model(sum)
                    .add(ms, mn)
                    .build();

        } else {
            ArimaModel mt = ucm.getComponent(0);
            ArimaModel ms = ucm.getComponent(1);
            ArimaModel mi = ucm.getComponent(2);
            ucmt = UcarimaModel.builder()
                    .model(sum)
                    .add(mt, ms, mi)
                    .build();
        }
        LightExtendedAirlineDecomposition.Builder dbuilder = LightExtendedAirlineDecomposition.builder()
                .model(ExtendedAirline.builder()
                        .periodicities(new double[]{period})
                        .ndifferencing(2)
                        .ar(false)
                        .p(max.getParameters())
                        .build())
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
                            .component(new SeriesComponent("S", sc, sr.getComponentVariance(pos[1]).fn(a -> a <= 0 ? 0 : Math.sqrt(a)), ComponentType.Seasonal))
                            .component(new SeriesComponent("N", nc, sr.getComponentVariance(pos[1]).fn(a -> a <= 0 ? 0 : Math.sqrt(a)), ComponentType.SeasonallyAdjusted))
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
                            .component(new SeriesComponent("T", tc.commit(), sr.getComponentVariance(pos[0]).fn(a -> a <= 0 ? 0 : Math.sqrt(a)), ComponentType.Trend))
                            .component(new SeriesComponent("S", sc.commit(), sr.getComponentVariance(pos[1]).fn(a -> a <= 0 ? 0 : Math.sqrt(a)), ComponentType.Seasonal))
                            .component(new SeriesComponent("I", ic.commit(), sr.getComponentVariance(pos[2]).fn(a -> a <= 0 ? 0 : Math.sqrt(a)), ComponentType.Irregular))
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
                    .component(new SeriesComponent("S", ds.item(pos[1]).commit(), DoubleSeq.empty(), ComponentType.Seasonal))
                    .component(new SeriesComponent("N", ds.item(pos[0]).commit(), DoubleSeq.empty(), ComponentType.SeasonallyAdjusted))
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
                    .component(new SeriesComponent("S", sc.commit(), DoubleSeq.empty(), ComponentType.Seasonal))
                    .component(new SeriesComponent("T", tc.commit(), DoubleSeq.empty(), ComponentType.Trend))
                    .component(new SeriesComponent("I", ic.commit(), DoubleSeq.empty(), ComponentType.Irregular))
                    .build();
        }
    }

    public static LightExtendedAirlineDecomposition decompose(DoubleSeq s, double[] periods, int ndiff, boolean ar, boolean cov, int nb, int nf) {

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

        final ExtendedAirlineMapping mapping = new ExtendedAirlineMapping(dp, false, ndiff, ar);

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
        DoubleSeq parameters = max.getParameters();
        UcarimaModel ucm = ucm(rslt.getModel().arima(), ip);

        IArimaModel sum = ucm.getModel();
        UcarimaModel ucmt;
        ArimaModel[] all = new ArimaModel[ucm.getComponentsCount()];
        for (int i = 0; i < all.length; ++i) {
            ArimaModel m = ucm.getComponent(i);
            all[i] = m;
        }
        ucmt = UcarimaModel.builder()
                .model(sum)
                .add(all)
                .build();

        LightExtendedAirlineDecomposition.Builder dbuilder = LightExtendedAirlineDecomposition.builder()
                .model(ExtendedAirline.builder()
                        .periodicities(dp)
                        .ndifferencing(ndiff)
                        .ar(ar)
                        .p(parameters)
                        .build())
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
                            sr.getComponentVariance(i).fn(a -> a <= 0 ? 0 : Math.sqrt(a)), ComponentType.Undefined));
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
                    ds.item(pos[i]).commit(), DoubleSeq.empty(), ComponentType.Undefined));
        }
        return dbuilder
                .y(sc)
                .build();
    }

    public static UcarimaModel ucm(IArimaModel arima, boolean sn) {

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

    public static UcarimaModel ucm(IArimaModel arima, int[] periods) {

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

}
