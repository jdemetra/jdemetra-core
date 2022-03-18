/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.fractionalairline;

import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.highfreq.FractionalAirlineSpec;
import demetra.highfreq.SeriesComponent;
import demetra.math.matrices.Matrix;
import demetra.modelling.OutlierDescriptor;
import demetra.timeseries.calendars.CalendarDefinition;
import demetra.timeseries.regression.ModellingContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import jdplus.arima.ArimaModel;
import jdplus.arima.IArimaModel;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockStorage;
import jdplus.highfreq.FractionalAirlineDecomposition;
import jdplus.highfreq.FractionalAirlineEstimation;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.regression.AdditiveOutlierFactory;
import jdplus.modelling.regression.IOutlierFactory;
import jdplus.modelling.regression.LevelShiftFactory;
import jdplus.modelling.regression.SwitchOutlierFactory;
import jdplus.regarima.GlsArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.ami.GenericOutliersDetection;
import jdplus.regarima.ami.OutliersDetectionModule;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.implementations.CompositeSsf;
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
import jdplus.ucarima.ssf.SsfUcarima;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class FractionalAirlineKernel {

    public FractionalAirlineEstimation process(DoubleSeq y, FractionalAirlineSpec spec) {
        return process(y, spec, ModellingContext.getActiveContext());
    }

    public FractionalAirlineEstimation process(DoubleSeq y, FractionalAirlineSpec spec, ModellingContext context) {
        if (spec.isLog()) {
            y = y.log();
        }
        final MultiPeriodicAirlineMapping mapping = new MultiPeriodicAirlineMapping(spec.getPeriodicities(), spec.isAdjustToInt(), spec.getDifferencingOrder(), spec.isAr());
        //
        RegArimaModel.Builder builder = RegArimaModel.<ArimaModel>builder()
                .y(y)
                .addX(FastMatrix.of(spec.getX()))
                .arima(mapping.getDefault())
                .meanCorrection(spec.isMeanCorrection());
        OutlierDescriptor[] o = null;
        String[] outliers = spec.getOutliers();
        if (outliers.length>0) {
            GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                    .precision(1e-5)
                    .build();
            IOutlierFactory[] factories = factories(outliers);
            OutliersDetectionModule od = OutliersDetectionModule.build(ArimaModel.class)
                    .maxOutliers(100)
                    .addFactories(factories)
                    .processor(processor)
                    .build();

            double cv = Math.max(spec.getCriticalValue(), GenericOutliersDetection.criticalValue(y.length(), 0.01));
            od.setCriticalValue(cv);

            RegArimaModel regarima = builder.build();
            od.prepare(regarima.getObservationsCount());
            od.process(regarima, mapping);
            int[][] io = od.getOutliers();
            o = new OutlierDescriptor[io.length];
            for (int i = 0; i < io.length; ++i) {
                int[] cur = io[i];
                DataBlock xcur = DataBlock.make(y.length());
                factories[cur[1]].fill(cur[0], xcur);
                o[i] = new OutlierDescriptor(factories[cur[1]].getCode(), cur[0]);
                builder.addX(xcur);
            }
        }else
            o=new OutlierDescriptor[0];
        RegArimaModel regarima = builder.build();
        GlsArimaProcessor<ArimaModel> finalProcessor = GlsArimaProcessor.builder(ArimaModel.class)
                .precision(spec.getPrecision())
                .computeExactFinalDerivatives(!spec.isApproximateHessian())
                .build();
        RegArimaEstimation rslt = finalProcessor.process(regarima, mapping);
        LogLikelihoodFunction.Point max = rslt.getMax();
        DoubleSeq parameters = max.getParameters();
        double phi;
        DoubleSeq theta;
        if (spec.isAr()) {
            phi = parameters.get(0);
            theta = parameters.drop(1, 0);
        } else {
            phi = 0;
            theta = parameters;
        }

        return FractionalAirlineEstimation.builder()
                .y(regarima.getY().toArray())
                .x(regarima.variables())
                .model(new demetra.highfreq.FractionalAirline(spec.getPeriodicities(), spec.getDifferencingOrder(), phi, theta))
                .coefficients(rslt.getConcentratedLikelihood().coefficients())
                .coefficientsCovariance(rslt.getConcentratedLikelihood().covariance(mapping.getDim(), true))
                .likelihood(rslt.statistics())
                .residuals(rslt.getConcentratedLikelihood().e())
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
        FractionalAirlineDecomposition.Builder dbuilder = FractionalAirlineDecomposition.builder()
                .model(new demetra.highfreq.FractionalAirline(new double[]{period}, 2, 0, max.getParameters()))
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

    public FractionalAirlineDecomposition decompose(DoubleSeq s, double[] periods, int ndiff, boolean ar, boolean cov, int nb, int nf) {

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

        final MultiPeriodicAirlineMapping mapping = new MultiPeriodicAirlineMapping(dp, false, ndiff, ar);

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
        double phi;
        DoubleSeq theta;
        if (ar) {
            phi = parameters.get(0);
            theta = parameters.drop(1, 0);
        } else {
            phi = 0;
            theta = parameters;
        }
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

        FractionalAirlineDecomposition.Builder dbuilder = FractionalAirlineDecomposition.builder()
                .model(new demetra.highfreq.FractionalAirline(dp, ndiff, phi, theta))
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
