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
import demetra.modelling.OutlierDescriptor;
import java.util.ArrayList;
import java.util.List;
import jdplus.arima.IArimaModel;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockStorage;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.likelihood.LogLikelihoodFunction;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.math.matrices.Matrix;
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
        final MultiPeriodicAirlineMapping mapping = new MultiPeriodicAirlineMapping(spec.getPeriodicities(), true, false);
        double[] y = spec.getY();
        RegArimaModel.Builder builder = RegArimaModel.<ArimaModel>builder()
                .y(DoubleSeq.of(y))
                .addX(Matrix.of(spec.getX()))
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
                .model(new demetra.highfreq.FractionalAirline(spec.getPeriodicities(), max.getParameters(), spec.isAdjustToInt()))
                .coefficients(rslt.getConcentratedLikelihood().coefficients())
                .coefficientsCovariance(rslt.getConcentratedLikelihood().covariance(2, true))
                .likelihood(rslt.statistics())
                .outliers(o)
                .parameters(max.getParameters())
                .parametersCovariance(max.asymptoticCovariance())
                .score(max.getScore())
                .build();
    }

    public FractionalAirlineDecomposition decompose(DoubleSeq s, double period, boolean adjust, boolean sn, boolean cov, int nb, int nf) {
        int iperiod = (int) period;
        if (period - iperiod < 1e-9) {
            period = iperiod;
            adjust = false;
        }
        FractionalAirlineMapping mapping = new FractionalAirlineMapping(period, adjust, false);

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

        ucm = ucm.simplify();

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
                .model(new demetra.highfreq.FractionalAirline(new double[]{1, period}, max.getParameters(), adjust))
                .likelihood(rslt.statistics())
                .parameters(max.getParameters())
                .parametersCovariance(max.asymptoticCovariance())
                .score(max.getScore())
                .ucarima(ucmt);
        CompositeSsf ssf = SsfUcarima.of(ucm);
        ISsfData data = new ExtendedSsfData(new SsfData(s), nb, nf);
        int[] pos = ssf.componentsPosition();
        double[] yc = s.toArray();
        if (cov) {
            try {
                DefaultSmoothingResults sr = DkToolkit.sqrtSmooth(ssf, data, true, true);
                if (sn) {
                    DoubleSeq sc = sr.getComponent(pos[1]), nc = sr.getComponent(pos[0]);
                    if (nb > 0 || nf > 0) {
                        double[] z = DoublesMath.add(sc, nc).toArray();
                        System.arraycopy(yc, 0, z, nb, yc.length);
                        yc = z;
                    }
                    return dbuilder
                            .y(yc)
                            .s(sc.toArray())
                            .n(nc.toArray())
                            .stdeS(sr.getComponentVariance(pos[1]).fastOp(a -> a <= 0 ? 0 : Math.sqrt(a)).toArray())
                            .stdeN(sr.getComponentVariance(pos[0]).fastOp(a -> a <= 0 ? 0 : Math.sqrt(a)).toArray())
                            .build();
                } else {
                    DoubleSeq sc = sr.getComponent(pos[1]), tc = sr.getComponent(pos[0]), ic = sr.getComponent(pos[2]);
                    if (nb > 0 || nf > 0) {
                        double[] z = DoublesMath.add(tc, sc, ic).toArray();
                        System.arraycopy(yc, 0, z, nb, yc.length);
                        yc = z;
                    }
                    return dbuilder
                            .y(yc)
                            .s(sc.toArray())
                            .t(tc.toArray())
                            .i(ic.toArray())
                            .stdeS(sr.getComponentVariance(pos[1]).fastOp(a -> a <= 0 ? 0 : Math.sqrt(a)).toArray())
                            .stdeT(sr.getComponentVariance(pos[0]).fastOp(a -> a <= 0 ? 0 : Math.sqrt(a)).toArray())
                            .stdeI(sr.getComponentVariance(pos[2]).fastOp(a -> a <= 0 ? 0 : Math.sqrt(a)).toArray())
                            .build();
                }
            } catch (Exception err) {
            }
        }

        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);
        if (sn) {
            DoubleSeq sc = ds.item(pos[1]), nc = ds.item(pos[0]);
            if (nb > 0 || nf > 0) {
                double[] z = DoublesMath.add(sc, nc).toArray();
                System.arraycopy(yc, 0, z, nb, yc.length);
                yc = z;
            }
            return dbuilder
                    .y(yc)
                    .s(ds.item(pos[1]).toArray())
                    .n(ds.item(pos[0]).toArray())
                    .build();
        } else {
            DoubleSeq sc = ds.item(pos[1]), tc = ds.item(pos[0]), ic = ds.item(pos[2]);
            if (nb > 0 || nf > 0) {
                double[] z = DoublesMath.add(tc, sc, ic).toArray();
                System.arraycopy(yc, 0, z, nb, yc.length);
                yc = z;
            }
            return dbuilder
                    .y(yc)
                    .s(sc.toArray())
                    .t(tc.toArray())
                    .i(ic.toArray())
                    .build();
        }
    }

    public UcarimaModel ucm(IArimaModel arima, boolean sn) {

        TrendCycleSelector tsel = new TrendCycleSelector();
        AllSelector ssel = new AllSelector();

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(arima);
        if (sn) {
            ucm = ucm.setVarianceMax(0, false);
        } else {
            ucm = ucm.setVarianceMax(-1, false);
        }
        return ucm;
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
