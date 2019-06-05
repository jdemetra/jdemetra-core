/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tempdisagg.univariate;

import demetra.arima.ssf.AR1;
import demetra.arima.ssf.Arima_1_1_0;
import demetra.arima.ssf.Rw;
import demetra.benchmarking.ssf.SsfDisaggregation;
import demetra.data.AggregationType;
import jdplus.data.DataBlock;
import demetra.data.Parameter;
import demetra.data.ParameterType;
import demetra.stats.ProbabilityType;
import jdplus.dstats.T;
import demetra.likelihood.DiffuseConcentratedLikelihood;
import demetra.likelihood.LikelihoodStatistics;
import demetra.likelihood.MaximumLogLikelihood;
import demetra.linearmodel.Coefficient;
import demetra.linearmodel.LinearModelEstimation;
import jdplus.maths.functions.IParametricMapping;
import jdplus.maths.functions.ParamValidation;
import jdplus.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.modelling.regression.Constant;
import demetra.modelling.regression.ITsVariable;
import demetra.modelling.regression.LinearTrend;
import demetra.modelling.regression.UserVariable;
import demetra.ssf.ISsfLoading;
import demetra.ssf.SsfComponent;
import demetra.ssf.akf.AkfToolkit;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.dk.SsfFunction;
import demetra.ssf.dk.SsfFunctionPoint;
import demetra.ssf.implementations.RegSsf;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.Ssf;
import demetra.ssf.univariate.SsfData;
import demetra.ssf.univariate.SsfRegressionModel;
import jdplus.stats.tests.NiidTests;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import org.openide.util.lookup.ServiceProvider;
import demetra.tempdisagg.univariate.TemporalDisaggregationSpec.Model;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.util.ArrayList;
import java.util.List;
import demetra.data.DoubleSeq;
import demetra.data.Doubles;
import demetra.maths.matrices.Matrix;
import demetra.tempdisagg.univariate.ResidualsDiagnostics;
import demetra.tempdisagg.univariate.TemporalDisaggregation;
import demetra.tempdisagg.univariate.TemporalDisaggregationResults;
import demetra.tempdisagg.univariate.TemporalDisaggregationSpec;
import jdplus.maths.matrices.FastMatrix;
import jdplus.maths.functions.ssq.SsqFunctionMinimizer;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@ServiceProvider(service = TemporalDisaggregation.Processor.class)
public class TemporalDisaggregationProcessor implements TemporalDisaggregation.Processor {

    public static final TemporalDisaggregationProcessor PROCESSOR = new TemporalDisaggregationProcessor();

    @Override
    public TemporalDisaggregationResults process(TsData aggregatedSeries, TsData[] indicators, TemporalDisaggregationSpec spec) {
        DisaggregationModel model = createModel(aggregatedSeries, indicators, spec);
        return compute(model, spec);
    }

    @Override
    public TemporalDisaggregationResults process(TsData aggregatedSeries, TsDomain domain, TemporalDisaggregationSpec spec) {
        DisaggregationModel model = createModel(aggregatedSeries, domain, spec);
        return compute(model, spec);
    }

    private DisaggregationModel createModel(TsData aggregatedSeries, TsData[] indicators, TemporalDisaggregationSpec spec) {
        TsDomain hdomain = indicators[0].getDomain();
        for (int i = 1; i < indicators.length; ++i) {
            hdomain = hdomain.intersection(indicators[i].getDomain());
        }

        List<ITsVariable> vars = new ArrayList<>();
        if (spec.isConstant()) {
            vars.add(new Constant());
        }
        if (spec.isTrend()) {
            vars.add(new LinearTrend(hdomain.start()));
        }
        for (int i = 0; i < indicators.length; ++i) {
            vars.add(new UserVariable("var" + (i + 1), indicators[i]));
        }
        return new DisaggregationModelBuilder(aggregatedSeries)
                .disaggregationDomain(hdomain)
                .aggregationType(spec.getAggregationType())
                .addX(vars)
                .rescale(spec.isRescale())
                .build();
    }

    private DisaggregationModel createModel(TsData aggregatedSeries, TsDomain hdomain, TemporalDisaggregationSpec spec) {
        List<ITsVariable> vars = new ArrayList<>();
        if (spec.isConstant()) {
            vars.add(new Constant());
        }
        if (spec.isTrend()) {
            vars.add(new LinearTrend(hdomain.start()));
        }
        return new DisaggregationModelBuilder(aggregatedSeries)
                .disaggregationDomain(hdomain)
                .aggregationType(spec.getAggregationType())
                .addX(vars)
                .rescale(spec.isRescale())
                .build();
    }

    private TemporalDisaggregationResults compute(DisaggregationModel model, TemporalDisaggregationSpec spec) {
        switch (spec.getAggregationType()) {
            case Sum:
            case Average:
                return disaggregate(model, spec);
            case First:
            case Last:
            case UserDefined:
                return interpolate(model, spec);
            default:
                return null;
        }
    }

    private TemporalDisaggregationResults interpolate(DisaggregationModel model, TemporalDisaggregationSpec spec) {
        SsfComponent nssf;
        MaximumLogLikelihood ml = null;
        int[] diffuse = diffuseRegressors(model.nx(), spec);
        DiffuseConcentratedLikelihood dll;
        if (!spec.isParameterEstimation()) {
            nssf = noiseModel(spec);
            SsfData ssfdata = new SsfData(model.getHEY());
            SsfRegressionModel ssfmodel = new SsfRegressionModel(Ssf.of(nssf, 0), ssfdata, model.getHEX(), diffuse);
            dll = DkToolkit.concentratedLikelihoodComputer().compute(ssfmodel);
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
            dll = rslt.getLikelihood();
            double c=2*rslt.getSsqE()/(dll.dim()-dll.nx()-1);
            ml = new MaximumLogLikelihood(rslt.getLikelihood().logLikelihood(),
                    p, fmin.gradientAtMinimum().map(z->-z/c), fmin.curvatureAtMinimum().times(1/c));

            if (spec.getResidualsModel() == Model.Ar1) {
                nssf = AR1.of(p.get(0), 1, spec.isZeroInitialization());
            } else {
                nssf = Arima_1_1_0.of(p.get(0), 1, spec.isZeroInitialization());
            }
        }

        // for computing the full model, we prefer to use the "slower" approach
        // which is much simpler
        // The estimation of the initial covariance matrices is unstable in case of 
        // large values in the regression variables. Two solutions: rescaling of the 
        // regression variables (no garantee) or use of the augmented Kalman smoother (default solution)
        // A square root form of the diffuse smoothing should also be investigated.
        ISsf rssf = RegSsf.of(Ssf.of(nssf, 0), model.getHX());
        SsfData ssfdata = new SsfData(model.getHY());
        DefaultSmoothingResults srslts;
        switch (spec.getAlgorithm()) {
            case Augmented:
                srslts = AkfToolkit.smooth(rssf, ssfdata, true);
                break;
            case Diffuse:
                srslts = DkToolkit.smooth(rssf, ssfdata, true, false);
                break;
            default:
                srslts = DkToolkit.smooth(rssf, ssfdata, true, false);
        }
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
        TsData res = hresiduals(model, dll.coefficients());
        res = res.multiply(f);
        dll = dll.rescale(model.getYfactor(), model.getXfactor());
        return TemporalDisaggregationResults.builder()
                .maximum(ml)
                .likelihood(lstats(dll, model.getLEDom().length(), spec))
                .disaggregatedSeries(TsData.ofInternal(model.getHDom().getStartPeriod(), yh))
                .stdevDisaggregatedSeries(TsData.ofInternal(model.getHDom().getStartPeriod(), vyh))
                .regressionEffects(regeffect)
                .residuals(res)
                .residualsDiagnostics(diagnostic(res, Ssf.of(nssf, 0), model.getYUnit()))
                .estimation(lestimation(dll, spec))
                .build();
    }

    private TemporalDisaggregationResults disaggregate(DisaggregationModel model, TemporalDisaggregationSpec spec) {
        Ssf ssf;
        SsfComponent nssf;
        int[] diffuse = diffuseRegressors(model.nx(), spec);
        MaximumLogLikelihood ml = null;
        DiffuseConcentratedLikelihood dll;
        if (!spec.isParameterEstimation()) {
            nssf = noiseModel(spec);
            ssf = SsfDisaggregation.of(nssf, model.getFrequencyRatio());
            SsfData ssfdata = new SsfData(model.getHEY());
            SsfRegressionModel ssfmodel = new SsfRegressionModel(ssf, ssfdata, model.getHEX(), diffuse);
            dll = DkToolkit.concentratedLikelihoodComputer().compute(ssfmodel);
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
            dll = rslt.getLikelihood();
            double c=.5*(dll.dim()-dll.nx()-1)/rslt.getSsqE();
            ml = new MaximumLogLikelihood(rslt.getLikelihood().logLikelihood(),
                    p, fmin.gradientAtMinimum().map(z->-z*c), fmin.curvatureAtMinimum().times(c));

            if (spec.getResidualsModel() == Model.Ar1) {
                nssf = AR1.of(p.get(0), 1, spec.isZeroInitialization());
            } else {
                nssf = Arima_1_1_0.of(p.get(0), 1, spec.isZeroInitialization());
            }
        }

        // for computing the full model, we prefer to use the "slower" approach
        // which is much simpler
        // The estimation of the initial covariance matrices is unstable in case of 
        // large values in the regression variables. Two solutions: rescaling of the 
        // regression variables (no garantee) or use of the augmented Kalman smoother (default solution)
        // A square root form of the diffuse smoothing should also be investigated.
        SsfComponent rssf = RegSsf.of(nssf, model.getHX());
        SsfData ssfdata = new SsfData(model.getHY());
        ssf = SsfDisaggregation.of(rssf, model.getFrequencyRatio(), model.getStart());
        DefaultSmoothingResults srslts;
        switch (spec.getAlgorithm()) {
            case Augmented:
                srslts = AkfToolkit.smooth(ssf, ssfdata, true);
                break;
            case Diffuse:
                srslts = DkToolkit.smooth(ssf, ssfdata, true, false);
                break;
            default:
                srslts = DkToolkit.smooth(ssf, ssfdata, true, false);
        }

        double[] yh = new double[model.getHY().length];
        double[] vyh = new double[model.getHY().length];
        int dim = ssf.getStateDim();
        ISsfLoading loading = rssf.loading();
        double yfac = model.getYfactor();
        if (spec.getAggregationType() == AggregationType.Average) {
            yfac /= model.getFrequencyRatio();
        }
        double[] xfac = model.getXfactor();
        double sigma = Math.sqrt(dll.ssq() / dll.dim()) / yfac;
        for (int i = 0; i < yh.length; ++i) {
            yh[i] = loading.ZX(i, srslts.a(i).drop(1, 0)) / yfac;
            double v = loading.ZVZ(i, srslts.P(i).extract(1, dim, 1, dim));
            vyh[i] = v <= 0 ? 0 : sigma * Math.sqrt(v);
        }
        TsData regeffect = regeffect(model, dll.coefficients());
        if (regeffect != null) {
            regeffect = regeffect.divide(yfac);
        }
        // full residuals are obtained by applying the filter on the series without the
        // regression effects
        TsData res = hresiduals(model, dll.coefficients());
        res = res.divide(yfac);
        dll = dll.rescale(yfac, xfac);
        return TemporalDisaggregationResults.builder()
                .maximum(ml)
                .likelihood(lstats(dll, model.getLEDom().length(), spec))
                .disaggregatedSeries(TsData.ofInternal(model.getHDom().getStartPeriod(), yh))
                .stdevDisaggregatedSeries(TsData.ofInternal(model.getHDom().getStartPeriod(), vyh))
                .regressionEffects(regeffect)
                .residuals(res)
                .estimation(lestimation(dll, spec))
                .residualsDiagnostics(diagnostic(res, SsfDisaggregation.of(nssf, model.getFrequencyRatio()), model.getYUnit()))
                .build();
    }

    private SsfComponent noiseModel(TemporalDisaggregationSpec spec) {
        switch (spec.getResidualsModel()) {
            case Wn:
                return null;
            case Ar1:
                return AR1.of(spec.getParameter().getValue(), 1, spec.isZeroInitialization());
            case RwAr1:
                return Arima_1_1_0.of(spec.getParameter().getValue(), 1, spec.isZeroInitialization());
            case Rw:
                return Rw.of(1, spec.isZeroInitialization());
            default:
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
        SsfComponent cmp = cl ? AR1.of(rho, 1, zeroinit)
                : Arima_1_1_0.of(rho, 1, zeroinit);
        return disagg ? SsfDisaggregation.of(cmp, ratio) : Ssf.of(cmp, 0);
    }

    private int[] diffuseRegressors(int nx, TemporalDisaggregationSpec spec) {
        int[] diffuse = null;
        if (spec.isDiffuseRegressors()) {
            diffuse = new int[nx];
            for (int i = 0; i < diffuse.length; ++i) {
                diffuse[i] = i;
            }
        }
        return diffuse;
    }

    private TsData regeffect(DisaggregationModel model, DoubleSeq coeff) {
        if (model.getHX() == null) {
            return null;
        }
        DataBlock regs = DataBlock.make(model.getHX().getRowsCount());
        regs.product(model.getHX().rowsIterator(), DataBlock.of(coeff));
        return TsData.ofInternal(model.getHDom().getStartPeriod(), regs);
    }

    private TsData hresiduals(DisaggregationModel model, DoubleSeq coeff) {
        double[] y = new double[model.getHEDom().length()];
        double[] hy = model.getHEY();
        FastMatrix hx = model.getHEX();
        for (int i = 0; i < hy.length; ++i) {
            if (Double.isFinite(hy[i])) {
                y[i] = hy[i] - hx.row(i).dot(coeff);
            } else {
                y[i] = Double.NaN;
            }
        }
        return TsData.ofInternal(model.getLEDom().getStartPeriod(), y);
    }

    private ResidualsDiagnostics diagnostic(TsData res, ISsf ssf, TsUnit unit) {
        DiffuseConcentratedLikelihood ll = DkToolkit.concentratedLikelihoodComputer().compute(ssf, new SsfData(res.getValues()));
        DoubleSeq e = ll.e();
        TsPeriod pstart = TsPeriod.of(unit, res.getStart().start());
        pstart = pstart.plus(ll.ndiffuse());
        TsData fres = TsData.ofInternal(pstart, e);
        NiidTests tests = NiidTests.builder()
                .data(e)
                .period(unit.getAnnualFrequency())
                .seasonal(false)
                .build();
        return ResidualsDiagnostics.builder()
                .mean(tests.meanTest() == null ? null : tests.meanTest().toSummary())
                .skewness(tests.skewness() == null ? null : tests.skewness().toSummary())
                .kurtosis(tests.kurtosis() == null ? null : tests.kurtosis().toSummary())
                .doornikHansen(tests.normalityTest() == null ? null : tests.normalityTest().toSummary())
                .ljungBox(tests.ljungBox() == null ? null : tests.ljungBox().toSummary())
                .fullResiduals(fres)
                .runsNumber(tests.runsNumber() == null ? null : tests.runsNumber().toSummary())
                .udRunsNumber(tests.upAndDownRunsNumbber() == null ? null : tests.upAndDownRunsNumbber().toSummary())
                .runsLength(tests.runsLength() == null ? null : tests.runsLength().toSummary())
                .udRunsLength(tests.upAndDownRunsLength() == null ? null : tests.upAndDownRunsLength().toSummary())
                .build();
    }

    private LikelihoodStatistics lstats(DiffuseConcentratedLikelihood dll, int nobs, TemporalDisaggregationSpec spec) {
        return LikelihoodStatistics.statistics(dll.logLikelihood(), nobs)
                .llAdjustment(0)
                .differencingOrder(dll.ndiffuse())
                .parametersCount((spec.isParameterEstimation() ? 1 : 0) + dll.nx() + 1)
                .ssq(dll.ssq())
                .build();
    }

    private LinearModelEstimation lestimation(DiffuseConcentratedLikelihood dll, TemporalDisaggregationSpec spec) {
        if (dll.nx() == 0)
            return LinearModelEstimation.EMPTY; 
        Coefficient[] c=new Coefficient[dll.nx()];
        int pos=0;
        int nparams=spec.isParameterEstimation() ? 1 : 0;
        T tstat=new T(dll.dim()-dll.nx()-nparams);
        DoubleSeq coefficients = dll.coefficients();
        Matrix cov = dll.covariance(nparams, true);
        DoubleSeq ser = cov.diagonal();
        if (spec.isConstant()){
            double ccur=coefficients.get(pos), ecur=Math.sqrt(ser.get(pos));
            double pval=2*tstat.getProbability(Math.abs(ccur/ecur), ProbabilityType.Upper);
            c[pos++]=new Coefficient(ccur, ecur, pval, "constant");
        }
        if (spec.isTrend()){
            double ccur=coefficients.get(pos), ecur=Math.sqrt(ser.get(pos));
            double pval=2*tstat.getProbability(Math.abs(ccur/ecur), ProbabilityType.Upper);
            c[pos++]=new Coefficient(ccur, ecur, pval, "trend");
        }
        int i=1;
        while (pos<c.length){
            double ccur=coefficients.get(pos), ecur=Math.sqrt(ser.get(pos));
            double pval=2*tstat.getProbability(Math.abs(ccur/ecur), ProbabilityType.Upper);
            c[pos++]=new Coefficient(ccur, ecur, pval, "var"+(i++));
        }
        return new LinearModelEstimation(c, cov);
    }

    private static class Mapping implements IParametricMapping<Parameter> {

        private final double lbound;

        private Mapping(double lbound) {
            this.lbound = lbound;
        }

        @Override
        public Parameter map(DoubleSeq p) {
            return new Parameter(p.get(0), ParameterType.Estimated); //To change body of generated methods, choose Tools | Templates.
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
