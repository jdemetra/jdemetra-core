/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tempdisagg.univariate;

import jdplus.ssf.arima.AR1;
import jdplus.ssf.arima.Arima_1_1_0;
import jdplus.ssf.arima.Rw;
import jdplus.ssf.benchmarking.SsfCumulator;
import demetra.data.AggregationType;
import jdplus.data.DataBlock;
import demetra.data.ParameterType;
import jdplus.stats.likelihood.DiffuseConcentratedLikelihood;
import demetra.math.functions.ObjectiveFunctionPoint;
import jdplus.math.functions.IParametricMapping;
import jdplus.math.functions.ParamValidation;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.timeseries.regression.Constant;
import demetra.timeseries.regression.LinearTrend;
import demetra.timeseries.regression.UserVariable;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;
import jdplus.ssf.akf.AkfToolkit;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.dk.SsfFunction;
import jdplus.ssf.dk.SsfFunctionPoint;
import jdplus.ssf.basic.RegSsf;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.SsfData;
import jdplus.ssf.univariate.SsfRegressionModel;
import jdplus.stats.tests.NiidTests;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.tempdisagg.univariate.TemporalDisaggregationSpec.Model;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.util.ArrayList;
import java.util.List;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.Doubles;
import demetra.data.Parameter;
import demetra.tempdisagg.univariate.TemporalDisaggregationSpec;
import demetra.timeseries.regression.Variable;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.functions.ssq.SsqFunctionMinimizer;
import jdplus.math.matrices.QuadraticForm;
import jdplus.ssf.DataBlockResults;
import jdplus.ssf.dk.DefaultDiffuseFilteringResults;
import jdplus.ssf.dk.DiffuseSmoother;
import jdplus.ssf.dk.FastDkSmoother;
import jdplus.ssf.sts.Noise;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class TemporalDisaggregationProcessor {

    @lombok.Value
    @lombok.AllArgsConstructor
    private static class TemporalDisaggregationEstimation {

        ObjectiveFunctionPoint ml;
        DiffuseConcentratedLikelihood dll;
        StateComponent noise;
        ISsfLoading loading;
    }

    public TemporalDisaggregationResults process(TsData aggregatedSeries, TsData[] indicators, TemporalDisaggregationSpec spec) {
        if (indicators == null || indicators.length == 0) {
            int hfreq = spec.getDefaultPeriod(), lfreq = aggregatedSeries.getAnnualFrequency();
            if (lfreq >= hfreq) {
                return null;
            }
            TsDomain domain = TsDomain.of(TsPeriod.of(TsUnit.ofAnnualFrequency(hfreq), aggregatedSeries.getDomain().getStartPeriod().start()), aggregatedSeries.length() * hfreq / lfreq);
            return process(aggregatedSeries, domain, spec);
        }
        DisaggregationModel model = createModel(aggregatedSeries, indicators, spec);
        return compute(model, spec);
    }

    public TemporalDisaggregationResults process(TsData aggregatedSeries, TsDomain domain, TemporalDisaggregationSpec spec) {
        DisaggregationModel model = createModel(aggregatedSeries, domain, spec);
        return compute(model, spec);
    }

    private DisaggregationModel createModel(TsData aggregatedSeries, TsData[] indicators, TemporalDisaggregationSpec spec) {
        TsDomain hdomain = indicators[0].getDomain();
        for (int i = 1; i < indicators.length; ++i) {
            hdomain = hdomain.intersection(indicators[i].getDomain());
        }

        List<Variable> vars = new ArrayList<>();
        if (spec.isConstant()) {
            vars.add(Variable.variable("C", Constant.C));
        }
        if (spec.isTrend()) {
            vars.add(Variable.variable("Trend", new LinearTrend(hdomain.start())));
        }
        for (int i = 0; i < indicators.length; ++i) {
            vars.add(Variable.variable("var" + (i + 1), new UserVariable(null, indicators[i])));
        }
        return new DisaggregationModelBuilder(aggregatedSeries)
                .disaggregationDomain(hdomain)
                .aggregationType(spec.getAggregationType())
                .addX(vars)
                .rescale(spec.isRescale())
                .build();
    }

    private DisaggregationModel createModel(TsData aggregatedSeries, TsDomain hdomain, TemporalDisaggregationSpec spec) {
        List<Variable> vars = new ArrayList<>();
        if (spec.isConstant()) {
            vars.add(Variable.variable("C", Constant.C));
        }
        if (spec.isTrend()) {
            vars.add(Variable.variable("Trend", new LinearTrend(hdomain.start())));
        }
        return new DisaggregationModelBuilder(aggregatedSeries)
                .disaggregationDomain(hdomain)
                .aggregationType(spec.getAggregationType())
                .addX(vars)
                .rescale(spec.isRescale())
                .build();
    }

    private TemporalDisaggregationResults compute(DisaggregationModel model, TemporalDisaggregationSpec spec) {
        return switch (spec.getAggregationType()) {
            case Sum, Average ->
                spec.isFast()
                ? disaggregate2(model, spec) : disaggregate(model, spec);
            case First, Last, UserDefined ->
                spec.isFast()
                ? interpolate2(model, spec) : interpolate(model, spec);
            default ->
                null;
        };
    }

    private TemporalDisaggregationResults interpolate2(DisaggregationModel model, TemporalDisaggregationSpec spec) {
        TemporalDisaggregationEstimation eim = estimateInterpolationModel(model, spec);
        TsDomain hDom = model.getHDom();
        double[] yh = new double[hDom.length()];
        double[] eyh = new double[hDom.length()];

        interpolateEstimation(model, eim, yh, eyh);

        double yfac = model.getYfactor();
        double[] xfac = model.getXfactor();

        DiffuseConcentratedLikelihood dll = eim.getDll();
        TsData regeffect = regeffect(model, dll.coefficients());
        if (regeffect != null && yfac != 1) {
            regeffect = regeffect.divide(yfac);
        }
        Ssf ssf = Ssf.of(eim.getNoise(), eim.getLoading());
        TsData res = hresiduals(model, dll.coefficients(), ssf);
        if (yfac != 1) {
            res = res.divide(yfac);
        }
        res = res.aggregate(model.getLDom().getTsUnit(), AggregationType.Last, true);
        dll = dll.rescale(yfac, xfac);
        int nparams = spec.isParameterEstimation() ? 1 : 0;
        return TemporalDisaggregationResults.builder()
                .originalSeries(model.getOriginalSeries())
                .disaggregationDomain(model.getHDom())
                .indicators(model.getIndicators())
                .maximum(eim.getMl())
                .likelihood(dll)
                .hyperParametersCount(nparams)
                .stats(dll.stats(0, nparams))
                .disaggregatedSeries(TsData.ofInternal(model.getHDom().getStartPeriod(), yh))
                .stdevDisaggregatedSeries(TsData.ofInternal(model.getHDom().getStartPeriod(), eyh))
                .regressionEffects(regeffect)
                .residualsDiagnostics(diagnostic(res))
                .build();
    }

    private TemporalDisaggregationResults interpolate(DisaggregationModel model, TemporalDisaggregationSpec spec) {
//        Ssf nmodel = noiseModel(spec);
//        ObjectiveFunctionPoint ml = null;
//        int diffuse = diffuseRegressors(model.nx(), spec);
//        DiffuseConcentratedLikelihood dll;
//        if (!spec.isParameterEstimation()) {
//            SsfData ssfdata = new SsfData(model.getHEY());
//            SsfRegressionModel ssfmodel = new SsfRegressionModel(nmodel, ssfdata, model.getHEX(), diffuse);
//            dll = DkToolkit.concentratedLikelihoodComputer(true, false, true).compute(ssfmodel);
//        } else {
//            SsfFunction<Parameter, Ssf> fn = ssfFunction(model, spec);
//            SsqFunctionMinimizer fmin = LevenbergMarquardtMinimizer
//                    .builder()
//                    .functionPrecision(spec.getEstimationPrecision())
//                    .build();
//            double start = spec.getParameter().getType() == ParameterType.Undefined
//                    ? .9 : spec.getParameter().getValue();
//            fmin.minimize(fn.ssqEvaluate(Doubles.of(start)));
//            SsfFunctionPoint<Parameter, Ssf> rslt = (SsfFunctionPoint<Parameter, Ssf>) fmin.getResult();
//            DoubleSeq p = rslt.getParameters();
//            dll = rslt.getLikelihood();
//            double c = .5 * (dll.dim() - dll.nx() - 1) / rslt.getValue();
//            double[] grad = fmin.gradientAtMinimum().toArray();
//            for (int i = 0; i < grad.length; ++i) {
//                grad[i] *= -c;
//            }
//            FastMatrix hessian = fmin.curvatureAtMinimum();
//            hessian.mul(-c);
//            ml = new ObjectiveFunctionPoint(rslt.getLikelihood().logLikelihood(),
//                    p.toArray(), grad, hessian);
//
//            if (spec.getResidualsModel() == Model.Ar1) {
//                nmodel = Ssf.of(AR1.of(p.get(0), 1, spec.isZeroInitialization()), AR1.defaultLoading());
//            } else {
//                nmodel = Ssf.of(Arima_1_1_0.of(p.get(0), 1, spec.isZeroInitialization()), Arima_1_1_0.defaultLoading());
//            }
//        }
        TemporalDisaggregationEstimation edm = estimateInterpolationModel(model, spec);
        Ssf nmodel = Ssf.of(edm.getNoise(), edm.getLoading());
        DiffuseConcentratedLikelihood dll = edm.getDll();

        // for computing the full model, we prefer to use the "slower" approach
        // which is much simpler
        // The estimation of the initial covariance matrices is unstable in case of 
        // large values in the regression variables. Two solutions: rescaling of the 
        // regression variables (no guarantee) or use of the augmented Kalman smoother (default solution)
        // A square root form of the diffuse smoothing should also be investigated.
        ISsf rssf = RegSsf.ssf(nmodel, model.getHX());
        SsfData ssfdata = new SsfData(model.getHY());
        DefaultSmoothingResults srslts;
        srslts = switch (spec.getAlgorithm()) {
            case Augmented ->
                AkfToolkit.smooth(rssf, ssfdata, true, false, false);
            case SqrtDiffuse ->
                DkToolkit.sqrtSmooth(rssf, ssfdata, true, false);
            case Augmented_NoCollapsing ->
                AkfToolkit.smooth(rssf, ssfdata, true, false, true);
            case Augmented_Robust ->
                AkfToolkit.robustSmooth(rssf, ssfdata, true, false).getSmoothing();
            default ->
                DkToolkit.smooth(rssf, ssfdata, true, false);
        };
        double[] Y = model.getHY();
        double[] O = model.getHO();
        double[] yh = new double[Y.length];
        double[] vyh = new double[Y.length];
        ISsfLoading loading = rssf.loading();
        double f = 1 / model.getYfactor();
        double sigma = f * Math.sqrt(dll.ssq() / dll.dim());
        for (int i = 0; i < yh.length; ++i) {
            if (Double.isFinite(Y[i])) {
                yh[i] = O[i];
                vyh[i] = 0;
            } else {
                yh[i] = f * loading.ZX(i, srslts.a(i));
                double v = loading.ZVZ(i, srslts.P(i));
                vyh[i] = v <= 0 ? 0 : sigma * Math.sqrt(v);
            }
        }
        TsData regeffect = regeffect(model, dll.coefficients());
        if (regeffect != null) {
            regeffect = regeffect.multiply(f);
        }
        TsData res = hresiduals(model, dll.coefficients(), nmodel);
        res = res.multiply(f);
        res = res.aggregate(model.getLDom().getTsUnit(), AggregationType.Last, true);
        dll = dll.rescale(model.getYfactor(), model.getXfactor());
        int nparams = spec.isParameterEstimation() ? 1 : 0;
        return TemporalDisaggregationResults.builder()
                .originalSeries(model.getOriginalSeries())
                .disaggregationDomain(model.getHDom())
                .indicators(model.getIndicators())
                .maximum(edm.getMl())
                .likelihood(dll)
                .hyperParametersCount(nparams)
                .stats(dll.stats(0, nparams))
                .disaggregatedSeries(TsData.ofInternal(model.getHDom().getStartPeriod(), yh))
                .stdevDisaggregatedSeries(TsData.ofInternal(model.getHDom().getStartPeriod(), vyh))
                .regressionEffects(regeffect)
                .residualsDiagnostics(diagnostic(res))
                .build();
    }

    // Estimate the disaggregation  model
    private TemporalDisaggregationEstimation estimateDisaggregationModel(DisaggregationModel model, TemporalDisaggregationSpec spec) {
        StateComponent ncmp = noiseComponent(spec);
        ISsfLoading nloading = noiseLoading(spec);
        int diffuse = diffuseRegressors(model.nx(), spec);
        if (!spec.isParameterEstimation()) {
            Ssf cssf = Ssf.of(SsfCumulator.of(ncmp, nloading, model.getFrequencyRatio(), 0),
                    SsfCumulator.defaultLoading(nloading, model.getFrequencyRatio(), 0));
            SsfData ssfdata = new SsfData(model.getHEY());
            SsfRegressionModel ssfmodel = new SsfRegressionModel(cssf, ssfdata, model.getHEX(), diffuse);
            return new TemporalDisaggregationEstimation(
                    null,
                    DkToolkit.concentratedLikelihoodComputer(true, false, true).compute(ssfmodel),
                    ncmp,
                    nloading
            );
        } else {
            SsfFunction<Parameter, Ssf> fn = ssfFunction(model, spec);
            SsqFunctionMinimizer fmin = LevenbergMarquardtMinimizer
                    .builder()
                    .functionPrecision(spec.getEstimationPrecision())
                    .build();
            double start = spec.getParameter().getType() == ParameterType.Undefined
                    ? .9 : spec.getParameter().getValue();
            fmin.minimize(fn.ssqEvaluate(Doubles.of(start)));
            SsfFunctionPoint<Parameter, Ssf> rslt = (SsfFunctionPoint<Parameter, Ssf>) fmin.getResult();
            DoubleSeq p = rslt.getParameters();
            DiffuseConcentratedLikelihood dll = rslt.getLikelihood();
            double c = -.5 * (dll.degreesOfFreedom() - 1) / rslt.getValue();
            double[] grad = fmin.gradientAtMinimum().toArray();
            for (int i = 0; i < grad.length; ++i) {
                grad[i] *= c;
            }
            FastMatrix hessian = fmin.curvatureAtMinimum().times(c);
            ObjectiveFunctionPoint ml = new ObjectiveFunctionPoint(rslt.getLikelihood().logLikelihood(),
                    p.toArray(), grad, hessian);
            if (spec.getResidualsModel() == Model.Ar1) {
                ncmp = AR1.of(p.get(0), 1, spec.isZeroInitialization());
            } else {
                ncmp = Arima_1_1_0.of(p.get(0), 1, spec.isZeroInitialization());
            }
            return new TemporalDisaggregationEstimation(
                    ml, dll, ncmp, nloading
            );
        }
    }

    // Estimate the interpolation  model
    private TemporalDisaggregationEstimation estimateInterpolationModel(DisaggregationModel model, TemporalDisaggregationSpec spec) {
        StateComponent ncmp = noiseComponent(spec);
        ISsfLoading nloading = noiseLoading(spec);
        int diffuse = diffuseRegressors(model.nx(), spec);
        if (!spec.isParameterEstimation()) {
            Ssf ssf = Ssf.of(ncmp, nloading);
            SsfData ssfdata = new SsfData(model.getHEY());
            SsfRegressionModel ssfmodel = new SsfRegressionModel(ssf, ssfdata, model.getHEX(), diffuse);
            return new TemporalDisaggregationEstimation(
                    null,
                    DkToolkit.concentratedLikelihoodComputer(true, false, true).compute(ssfmodel),
                    ncmp,
                    nloading
            );
        } else {
            SsfFunction<Parameter, Ssf> fn = ssfFunction(model, spec);
            SsqFunctionMinimizer fmin = LevenbergMarquardtMinimizer
                    .builder()
                    .build();
            double start = spec.getParameter().getType() == ParameterType.Undefined
                    ? .9 : spec.getParameter().getValue();
            fmin.minimize(fn.ssqEvaluate(Doubles.of(start)));
            SsfFunctionPoint<Parameter, Ssf> rslt = (SsfFunctionPoint<Parameter, Ssf>) fmin.getResult();
            DoubleSeq p = rslt.getParameters();
            DiffuseConcentratedLikelihood dll = rslt.getLikelihood();
            double c = -.5 * (dll.degreesOfFreedom() - 1) / rslt.getValue();
            double[] grad = fmin.gradientAtMinimum().toArray();
            for (int i = 0; i < grad.length; ++i) {
                grad[i] *= c;
            }
            FastMatrix hessian = fmin.curvatureAtMinimum().times(c);
            ObjectiveFunctionPoint ml = new ObjectiveFunctionPoint(rslt.getLikelihood().logLikelihood(),
                    p.toArray(), grad, hessian);
            ncmp = rslt.getSsf().asComponent();
            return new TemporalDisaggregationEstimation(
                    ml, dll, ncmp, nloading
            );
        }
    }

    private void interpolateEstimation(DisaggregationModel model, TemporalDisaggregationEstimation estimation, final double[] z, final double[] e) {
        double[] hy = model.getHY();
        FastMatrix hX = model.getHX();
        double[] O = model.getHO();
        double f = 1 / model.getYfactor();

        StateComponent ncmp = estimation.getNoise();
        ISsfLoading nloading = estimation.getLoading();
        Ssf ssf = Ssf.of(ncmp, nloading);
        DiffuseSmoother smoother = DiffuseSmoother.builder(ssf)
                .calcVariance(true)
                .rescaleVariance(false)
                .build();
        DefaultSmoothingResults srslts = DefaultSmoothingResults.full();
        srslts.prepare(ssf.getStateDim(), 0, hy.length);
        DiffuseConcentratedLikelihood dll = estimation.getDll();
        double sigma = f * Math.sqrt(dll.sigma2());
        if (hX == null || hX.isEmpty()) {
            SsfData data = new SsfData(hy);
            DefaultDiffuseFilteringResults frslts = DkToolkit.filter(ssf, data, true);
            smoother.process(hy.length, frslts, srslts);
            for (int i = 0; i < z.length; ++i) {
                if (Double.isFinite(hy[i])) {
                    z[i] = O[i];
                    e[i] = 0;
                } else {
                    z[i] = f * nloading.ZX(i, srslts.a(i));
                    double v = nloading.ZVZ(i, srslts.P(i));
                    e[i] = v <= 0 ? 0 : sigma * Math.sqrt(v);
                }
            }
        } else {
            // Xb
            DoubleSeq b = dll.coefficients();
            DataBlock Xb = DataBlock.make(hX.getRowsCount());
            DoubleSeqCursor bcur = b.cursor();
            DataBlockIterator xcols = hX.columnsIterator();
            double c = bcur.getAndNext();
            Xb.setAY(c, xcols.next());
            while (xcols.hasNext()) {
                Xb.addAY(bcur.getAndNext(), xcols.next());
            }
            // u=y-Xb
            DataBlock u = DataBlock.copyOf(hy);
            u.sub(Xb);

            //L(y-Xb)
            SsfData data = new SsfData(u);
            DefaultDiffuseFilteringResults frslts = DkToolkit.filter(ssf, data, true);
            smoother.process(hy.length, frslts, srslts);

            // Z = L(y-Xb) + Xb  
            // V = V(L(y-Xb)) + (LX-X) V(B) (LX-X)'
            FastMatrix Vb = dll.unscaledCovariance();
            FastMatrix LhX = FastMatrix.make(hX.getRowsCount(), hX.getColumnsCount());
            DataBlockIterator lxcols = LhX.columnsIterator();
            xcols.reset();
            FastDkSmoother fsmoother = new FastDkSmoother(ssf, frslts);
            while (xcols.hasNext()) {
                fsmoother.smooth(xcols.next());
                DataBlockResults ss = fsmoother.smoothedStates();
                lxcols.next().set(i -> nloading.ZX(i, ss.datablock(i)));
            }

            LhX.sub(hX);
            for (int i = 0; i < z.length; ++i) {
                if (Double.isFinite(hy[i])) {
                    z[i] = O[i];
                    e[i] = 0;
                } else {
                    z[i] = f * (nloading.ZX(i, srslts.a(i)) + Xb.get(i));
                    double v = nloading.ZVZ(i, srslts.P(i));
                    v += QuadraticForm.apply(Vb, LhX.row(i));
                    e[i] = v <= 0 ? 0 : sigma * Math.sqrt(v);
                }
            }
        }
    }

    private void disaggregateEstimation(DisaggregationModel model, TemporalDisaggregationEstimation estimation, final double[] z, final double[] e) {
        double[] hy = model.getHY();
        FastMatrix hX = model.getHX();
        FastMatrix hXC = model.getHXC();

        StateComponent ncmp = estimation.getNoise();
        ISsfLoading nloading = estimation.getLoading();
        Ssf cssf = Ssf.of(SsfCumulator.of(ncmp, nloading, model.getFrequencyRatio(), model.getStart()),
                SsfCumulator.defaultLoading(nloading, model.getFrequencyRatio(), model.getStart()));
        DiffuseSmoother smoother = DiffuseSmoother.builder(cssf)
                .calcVariance(true)
                .rescaleVariance(false)
                .build();
        DefaultSmoothingResults srslts = DefaultSmoothingResults.full();
        srslts.prepare(cssf.getStateDim(), 0, hy.length);
        DiffuseConcentratedLikelihood dll = estimation.getDll();
        double sigma = Math.sqrt(dll.sigma2());
        int dim = ncmp.dim();
        if (hX == null || hX.isEmpty()) {
            SsfData data = new SsfData(hy);
            DefaultDiffuseFilteringResults frslts = DkToolkit.filter(cssf, data, true);
            smoother.process(hy.length, frslts, srslts);
            for (int i = 0; i < z.length; ++i) {
                z[i] = nloading.ZX(i, srslts.a(i).drop(1, 0));
                double v = nloading.ZVZ(i, srslts.P(i).extract(1, dim, 1, dim));
                e[i] = v <= 0 ? 0 : sigma * Math.sqrt(v);
            }
        } else {
            // Xb
            DoubleSeq b = dll.coefficients();
            DataBlock Xb = DataBlock.make(hXC.getRowsCount());
            DataBlock Xbc = DataBlock.make(hXC.getRowsCount());
            DoubleSeqCursor bcur = b.cursor();
            DataBlockIterator xcols = hX.columnsIterator();
            DataBlockIterator xccols = hXC.columnsIterator();
            double c = bcur.getAndNext();
            Xb.setAY(c, xcols.next());
            Xbc.setAY(c, xccols.next());
            while (xcols.hasNext()) {
                c = bcur.getAndNext();
                Xb.addAY(c, xcols.next());
                Xbc.addAY(c, xccols.next());
            }
            // u=y-Xb
            DataBlock u = DataBlock.copyOf(hy);
            u.sub(Xbc);

            //L(y-Xb)
            SsfData data = new SsfData(u);
            DefaultDiffuseFilteringResults frslts = DkToolkit.filter(cssf, data, true);
            smoother.process(hy.length, frslts, srslts);

            // Z = L(y-Xb) + Xb  
            // V = V(L(y-Xb)) + (LX-X) V(B) (LX-X)'
            FastMatrix Vb = dll.unscaledCovariance();
            FastMatrix LhX = FastMatrix.make(hX.getRowsCount(), hX.getColumnsCount());
            DataBlockIterator lxcols = LhX.columnsIterator();
            xccols.reset();
           while (xccols.hasNext()) {
              FastDkSmoother fsmoother = new FastDkSmoother(cssf, frslts);
               fsmoother.smooth(xccols.next());
                DataBlockResults ss = fsmoother.smoothedStates();
                lxcols.next().set(i -> nloading.ZX(i, ss.datablock(i).drop(1, 0)));
            }

            LhX.sub(hX);
            for (int i = 0; i < z.length; ++i) {
                z[i] = nloading.ZX(i, srslts.a(i).drop(1, 0)) + Xb.get(i);
                double v = nloading.ZVZ(i, srslts.P(i).extract(1, dim, 1, dim));
                v += QuadraticForm.apply(Vb, LhX.row(i));
                e[i] = v <= 0 ? 0 : sigma * Math.sqrt(v);
            }
        }
    }

    private TemporalDisaggregationResults disaggregate2(DisaggregationModel model, TemporalDisaggregationSpec spec) {
        TemporalDisaggregationEstimation edm = estimateDisaggregationModel(model, spec);
        TsDomain hDom = model.getHDom();
        double[] yh = new double[hDom.length()];
        double[] eyh = new double[hDom.length()];

        disaggregateEstimation(model, edm, yh, eyh);
        double yfac = model.getYfactor();
        if (spec.getAggregationType() == AggregationType.Average) {
            yfac /= model.getFrequencyRatio();
        }
        if (yfac != 1) {
            for (int i = 0; i < yh.length; ++i) {
                yh[i] /= yfac;
                eyh[i] /= yfac;
            }
        }

        double[] xfac = model.getXfactor();

        DiffuseConcentratedLikelihood dll = edm.getDll();
        TsData regeffect = regeffect(model, dll.coefficients());
        if (regeffect != null) {
            regeffect = regeffect.divide(yfac);
        }
        // full residuals are obtained by applying the filter on the series without the
        // regression effects
        Ssf ssf = Ssf.of(SsfCumulator.of(edm.getNoise(), edm.getLoading(), model.getFrequencyRatio(), model.getStart()),
                SsfCumulator.defaultLoading(edm.getLoading(), model.getFrequencyRatio(), model.getStart()));
        TsData res = hresiduals(model, dll.coefficients(), ssf);
        res = res.divide(yfac);
        res = res.aggregate(model.getLDom().getTsUnit(), AggregationType.Last, true);
        dll = dll.rescale(yfac, xfac);
        int nparams = spec.isParameterEstimation() ? 1 : 0;
        return TemporalDisaggregationResults.builder()
                .originalSeries(model.getOriginalSeries())
                .disaggregationDomain(model.getHDom())
                .indicators(model.getIndicators())
                .maximum(edm.getMl())
                .likelihood(dll)
                .hyperParametersCount(nparams)
                .stats(dll.stats(0, nparams))
                .disaggregatedSeries(TsData.ofInternal(hDom.getStartPeriod(), yh))
                .stdevDisaggregatedSeries(TsData.ofInternal(hDom.getStartPeriod(), eyh))
                .regressionEffects(regeffect)
                .residualsDiagnostics(diagnostic(res))
                .build();
    }

    private TemporalDisaggregationResults disaggregate(DisaggregationModel model, TemporalDisaggregationSpec spec) {
//        StateComponent ncmp = noiseComponent(spec);
//        ISsfLoading nloading = noiseLoading(spec);
//        int diffuse = diffuseRegressors(model.nx(), spec);
//        ObjectiveFunctionPoint ml = null;
//        DiffuseConcentratedLikelihood dll;
//        if (!spec.isParameterEstimation()) {
//            Ssf cssf = Ssf.of(SsfCumulator.of(ncmp, nloading, model.getFrequencyRatio(), 0),
//                    SsfCumulator.defaultLoading(nloading, model.getFrequencyRatio(), 0));
//            SsfData ssfdata = new SsfData(model.getHEY());
//            SsfRegressionModel ssfmodel = new SsfRegressionModel(cssf, ssfdata, model.getHEX(), diffuse);
//            dll = DkToolkit.concentratedLikelihoodComputer(true, false, true).compute(ssfmodel);
//        } else {
//            SsfFunction<Parameter, Ssf> fn = ssfFunction(model, spec);
//            SsqFunctionMinimizer fmin = LevenbergMarquardtMinimizer
//                    .builder()
//                    .functionPrecision(spec.getEstimationPrecision())
//                    .build();
//            double start = spec.getParameter().getType() == ParameterType.Undefined
//                    ? .9 : spec.getParameter().getValue();
//            fmin.minimize(fn.ssqEvaluate(Doubles.of(start)));
//            SsfFunctionPoint<Parameter, Ssf> rslt = (SsfFunctionPoint<Parameter, Ssf>) fmin.getResult();
//            DoubleSeq p = rslt.getParameters();
//            dll = rslt.getLikelihood();
//            double c = -.5 * (dll.degreesOfFreedom() - 1) / rslt.getValue();
//            double[] grad = fmin.gradientAtMinimum().toArray();
//            for (int i = 0; i < grad.length; ++i) {
//                grad[i] *= c;
//            }
//            FastMatrix hessian = fmin.curvatureAtMinimum().times(c);
//            ml = new ObjectiveFunctionPoint(rslt.getLikelihood().logLikelihood(),
//                    p.toArray(), grad, hessian);
//
//            if (spec.getResidualsModel() == Model.Ar1) {
//                ncmp = AR1.of(p.get(0), 1, spec.isZeroInitialization());
//            } else {
//                ncmp = Arima_1_1_0.of(p.get(0), 1, spec.isZeroInitialization());
//            }
//        }
        TemporalDisaggregationEstimation edm = estimateDisaggregationModel(model, spec);
        StateComponent ncmp = edm.getNoise();
        ISsfLoading nloading = edm.getLoading();
        DiffuseConcentratedLikelihood dll = edm.getDll();

        StateComponent rcmp = (model.getHX() == null || model.getHX().isEmpty()) ? ncmp : RegSsf.of(ncmp, model.getHX());
        ISsfLoading rloading = (model.getHX() == null || model.getHX().isEmpty()) ? nloading : RegSsf.defaultLoading(ncmp.dim(), nloading, model.getHX());
        SsfData ssfdata = new SsfData(model.getHY());
        Ssf ssf = Ssf.of(SsfCumulator.of(rcmp, rloading, model.getFrequencyRatio(), model.getStart()),
                SsfCumulator.defaultLoading(rloading, model.getFrequencyRatio(), model.getStart()));
        DefaultSmoothingResults srslts;
        srslts = switch (spec.getAlgorithm()) {
            case Augmented ->
                AkfToolkit.smooth(ssf, ssfdata, true, false, false);
            case SqrtDiffuse ->
                DkToolkit.sqrtSmooth(ssf, ssfdata, true, false);
            case Augmented_NoCollapsing ->
                AkfToolkit.smooth(ssf, ssfdata, true, false, true);
            case Augmented_Robust ->
                AkfToolkit.robustSmooth(ssf, ssfdata, true, false).getSmoothing();
            default ->
                DkToolkit.smooth(ssf, ssfdata, true, false);
        };

        double[] yh = new double[model.getHY().length];
        double[] vyh = new double[model.getHY().length];
        int dim = ssf.getStateDim();
        double yfac = model.getYfactor();
        if (spec.getAggregationType() == AggregationType.Average) {
            yfac /= model.getFrequencyRatio();
        }
        double[] xfac = model.getXfactor();
        double sigma = Math.sqrt(dll.sigma2()) / yfac;
        for (int i = 0; i < yh.length; ++i) {
            yh[i] = rloading.ZX(i, srslts.a(i).drop(1, 0)) / yfac;
            double v = rloading.ZVZ(i, srslts.P(i).extract(1, dim, 1, dim));
            vyh[i] = v <= 0 ? 0 : sigma * Math.sqrt(v);
        }
        TsData regeffect = regeffect(model, dll.coefficients());
        if (regeffect != null) {
            regeffect = regeffect.divide(yfac);
        }
        // full residuals are obtained by applying the filter on the series without the
        // regression effects
        TsData res = hresiduals(model, dll.coefficients(), ssf);
        res = res.divide(yfac);
        res = res.aggregate(model.getLDom().getTsUnit(), AggregationType.Last, true);
        dll = dll.rescale(yfac, xfac);
        int nparams = spec.isParameterEstimation() ? 1 : 0;
        return TemporalDisaggregationResults.builder()
                .originalSeries(model.getOriginalSeries())
                .disaggregationDomain(model.getHDom())
                .indicators(model.getIndicators())
                .maximum(edm.getMl())
                .likelihood(dll)
                .hyperParametersCount(nparams)
                .stats(dll.stats(0, nparams))
                .disaggregatedSeries(TsData.ofInternal(model.getHDom().getStartPeriod(), yh))
                .stdevDisaggregatedSeries(TsData.ofInternal(model.getHDom().getStartPeriod(), vyh))
                .regressionEffects(regeffect)
                .residualsDiagnostics(diagnostic(res))
                .build();
    }

    private StateComponent noiseComponent(TemporalDisaggregationSpec spec) {
        switch (spec.getResidualsModel()) {
            case Wn -> {
                return Noise.of(1);
            }
            case Ar1 -> {
                return AR1.of(spec.getParameter().getValue(), 1, spec.isZeroInitialization());
            }
            case RwAr1 -> {
                return Arima_1_1_0.of(spec.getParameter().getValue(), 1, spec.isZeroInitialization());
            }
            case Rw -> {
                return Rw.of(1, spec.isZeroInitialization());
            }
            default ->
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private Ssf noiseModel(TemporalDisaggregationSpec spec) {
        switch (spec.getResidualsModel()) {
            case Wn -> {
                return Ssf.of(Noise.of(1), Noise.defaultLoading());
            }
            case Ar1 -> {
                return Ssf.of(AR1.of(spec.getParameter().getValue(), 1, spec.isZeroInitialization()),
                        AR1.defaultLoading());
            }
            case RwAr1 -> {
                return Ssf.of(Arima_1_1_0.of(spec.getParameter().getValue(), 1, spec.isZeroInitialization()),
                        Arima_1_1_0.defaultLoading());
            }
            case Rw -> {
                return Ssf.of(Rw.of(1, spec.isZeroInitialization()),
                        Rw.defaultLoading());
            }
            default ->
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private ISsfLoading noiseLoading(TemporalDisaggregationSpec spec) {
        switch (spec.getResidualsModel()) {
            case Wn -> {
                return Noise.defaultLoading();
            }
            case Ar1 -> {
                return AR1.defaultLoading();
            }
            case RwAr1 -> {
                return Arima_1_1_0.defaultLoading();
            }
            case Rw -> {
                return Rw.defaultLoading();
            }
            default ->
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private SsfFunction<Parameter, Ssf> ssfFunction(DisaggregationModel model, TemporalDisaggregationSpec spec) {
        SsfData data = new SsfData(model.getHEY());
        Double lbound = spec.getTruncatedParameter();
        Mapping mapping = new Mapping(lbound == null ? -1 : lbound);
        boolean cl = spec.getResidualsModel() == Model.Ar1;
        boolean disagg = spec.getAggregationType() == AggregationType.Average || spec.getAggregationType() == AggregationType.Sum;
        return SsfFunction.builder(data, mapping,
                p -> ssf(p.getValue(), disagg, cl, spec.isZeroInitialization(), model.getFrequencyRatio()))
                .regression(model.getHEX(), diffuseRegressors(model.nx(), spec))
                .useMaximumLikelihood(true)
                .build();
    }

    private static Ssf ssf(double rho, boolean disagg, boolean cl, boolean zeroinit, int ratio) {
        StateComponent cmp = cl ? AR1.of(rho, 1, zeroinit)
                : Arima_1_1_0.of(rho, 1, zeroinit);
        ISsfLoading loading = cl ? AR1.defaultLoading() : Arima_1_1_0.defaultLoading();
        if (disagg) {
            return Ssf.of(SsfCumulator.of(cmp, loading, ratio, 0),
                    SsfCumulator.defaultLoading(loading, ratio, 0));
        } else {
            return Ssf.of(cmp, loading);
        }
    }

    private int diffuseRegressors(int nx, TemporalDisaggregationSpec spec) {
        if (spec.isDiffuseRegressors()) {
            return nx;
        } else if (!spec.getResidualsModel().isStationary() && spec.isConstant()) // to be compatible with other specifications. Could be changed
        {
            return 1;
        } else {
            return 0;
        }
    }

    private TsData regeffect(DisaggregationModel model, DoubleSeq coeff) {
        if (model.getHX() == null) {
            return null;
        }
        DataBlock regs = DataBlock.make(model.getHX().getRowsCount());
        regs.product(model.getHX().rowsIterator(), DataBlock.of(coeff));
        return TsData.of(model.getHDom().getStartPeriod(), regs);
    }

    private TsData hresiduals(DisaggregationModel model, DoubleSeq coeff, ISsf ssf) {
        double[] y = new double[model.getHEDom().length()];
        double[] hy = model.getHEY();
        FastMatrix hx = model.getHEX();
        if (hx != null) {
            for (int i = 0; i < hy.length; ++i) {
                if (Double.isFinite(hy[i])) {
                    y[i] = hy[i] - hx.row(i).dot(coeff);
                } else {
                    y[i] = Double.NaN;
                }
            }
        }
        DefaultDiffuseFilteringResults fr = DkToolkit.filter(ssf, new SsfData(y), false);
        return TsData.of(model.getHEDom().getStartPeriod(), fr.errors(true, false));
    }

    private ResidualsDiagnostics diagnostic(TsData res) {
        NiidTests tests = NiidTests.builder()
                .data(res.getValues())
                .period(res.getAnnualFrequency())
                .seasonal(false)
                .build();
        return ResidualsDiagnostics.builder()
                .fullResiduals(res)
                .niid(tests)
                .build();

    }

    private static class Mapping implements IParametricMapping<Parameter> {

        private final double lbound;

        private Mapping(double lbound) {
            this.lbound = lbound;
        }

        @Override
        public Parameter map(DoubleSeq p) {
            return Parameter.estimated(p.get(0));
        }

        @Override
        public DoubleSeq getDefaultParameters() {
            return Doubles.of(.9);
        }

        @Override
        public boolean checkBoundaries(DoubleSeq inparams) {
            double p = inparams.get(0);
            if (lbound == -1) {
                return p > -1 && p < 1;
            } else {
                return p >= lbound && p < 1;
            }
        }

        @Override
        public double epsilon(DoubleSeq inparams, int idx) {
            return 1e-8;
        }

        @Override
        public int getDim() {
            return 1;
        }

        @Override
        public double lbound(int idx) {
            return lbound;
        }

        @Override
        public double ubound(int idx) {
            return 1;
        }

        @Override
        public ParamValidation validate(DataBlock ioparams) {
            double p = ioparams.get(0);
            if (lbound == -1) {
                if (p > -1 && p < 1) {
                    return ParamValidation.Valid;
                } else {
                    if (p == 1) {
                        p = 1 - 1e-6;
                    } else if (p == -1) {
                        p = -1 + 1e-6;
                    } else {
                        p = 1 / p;
                    }
                    ioparams.set(p);
                    return ParamValidation.Changed;
                }
            } else if (p >= lbound && p < 1) {
                return ParamValidation.Valid;
            } else {
                if (p < lbound) {
                    p = lbound;
                } else if (p == -1) {
                    p = -1 + 1e-6;
                } else {
                    p = 1 / Math.abs(p);
                }
                ioparams.set(p);
                return ParamValidation.Changed;
            }
        }

    }
}
