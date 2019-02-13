/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tempdisagg.univariate;

import demetra.arima.ssf.AR1;
import demetra.arima.ssf.Arima_1_1_0;
import demetra.arima.ssf.Rw;
import demetra.benchmarking.ssf.SsfDisaggregation;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.data.Parameter;
import demetra.data.ParameterType;
import demetra.likelihood.DiffuseConcentratedLikelihood;
import demetra.likelihood.MaximumLogLikelihood;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.ParamValidation;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.modelling.regression.Constant;
import demetra.modelling.regression.ITsVariable;
import demetra.modelling.regression.LinearTrend;
import demetra.modelling.regression.UserVariable;
import demetra.ssf.ISsfLoading;
import demetra.ssf.SsfAlgorithm;
import demetra.ssf.SsfComponent;
import demetra.ssf.akf.AkfToolkit;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.dk.SsfFunction;
import demetra.ssf.dk.SsfFunctionPoint;
import demetra.ssf.implementations.RegSsf;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.Ssf;
import demetra.ssf.univariate.SsfData;
import demetra.ssf.univariate.SsfRegressionModel;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import org.openide.util.lookup.ServiceProvider;
import demetra.tempdisagg.univariate.TemporalDisaggregationSpec.Model;
import java.util.ArrayList;
import java.util.List;

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
        return new DisaggregationModelBuilder()
                .y(aggregatedSeries)
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
        return new DisaggregationModelBuilder()
                .y(aggregatedSeries)
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

        return null;
    }

    private TemporalDisaggregationResults disaggregate(DisaggregationModel model, TemporalDisaggregationSpec spec) {
        TemporalDisaggregationResults.Builder builder = TemporalDisaggregationResults.builder();
        Ssf ssf;
        SsfComponent nssf;
        SsfData ssfdata = new SsfData(model.hY);
        int[] diffuse = diffuseRegressors(model.nx(), spec);
        DiffuseConcentratedLikelihood dll;
        if (!spec.isParameterEstimation()) {
            nssf = noiseModel(spec);
            ssf = SsfDisaggregation.of(nssf, model.frequencyRatio);
            SsfRegressionModel ssfmodel = new SsfRegressionModel(ssf, ssfdata, model.hEX, diffuse);
            dll = DkToolkit.concentratedLikelihoodComputer().compute(ssfmodel);
        } else {
            SsfFunction<Parameter, Ssf> fn = ssfFunction(model, spec);
            ISsqFunctionMinimizer fmin = new LevenbergMarquardtMinimizer();
            double start = spec.getParameter().getType() == ParameterType.Undefined
                    ? .9 : spec.getParameter().getValue();
            fmin.minimize(fn.ssqEvaluate(DoubleSequence.of(start)));
            SsfFunctionPoint<Parameter, Ssf> rslt = (SsfFunctionPoint<Parameter, Ssf>) fmin.getResult();
            DoubleSequence p = rslt.getParameters();
            dll = rslt.getLikelihood();
            builder.maximum(new MaximumLogLikelihood(rslt.getLikelihood().logLikelihood(),
                    p, fmin.gradientAtMinimum(), fmin.curvatureAtMinimum()));

            if (spec.getResidualsModel() == Model.Ar1) {
                nssf = AR1.of(p.get(0), 1, spec.isZeroInitialization());
            } else {
                nssf = Arima_1_1_0.of(p.get(0), 1, spec.isZeroInitialization());
            }
        }
        builder.concentratedLikelihood(dll.rescale(model.yfactor, model.xfactor));

        // for computing the full model, we prefer to use the "slower" approach
        // which is much simpler
        SsfComponent rssf = RegSsf.of(nssf, model.hX);
        ssf = SsfDisaggregation.of(rssf, model.frequencyRatio);
        DefaultSmoothingResults srslts;
        if (spec.getAlgorithm() != SsfAlgorithm.Augmented) {
            srslts = DkToolkit.smooth(ssf, ssfdata, true, false);
        } else {
            srslts = AkfToolkit.smooth(ssf, ssfdata, true);
        }

// The estimation of the initial covariance matrices is unstable in case of 
// large values in the regression variables. Two solutions: rescaling of the 
// regression variables (no garantee) or use of the augmented Kalman smoother (preferred solution)
// A square root form of the diffuse smoothing should also be investigated.
        double[] yh = new double[model.hY.length];
        double[] vyh = new double[model.hY.length];
        int dim = ssf.getStateDim();
        ISsfLoading loading = rssf.loading();
        double f = 1 / model.yfactor;
        double sigma = f * Math.sqrt(dll.ssq() / dll.dim());
        for (int i = 0; i < yh.length; ++i) {
            yh[i] = loading.ZX(i, srslts.a(i).drop(1, 0));
            double v = loading.ZVZ(i, srslts.P(i).extract(1, dim, 1, dim));
            vyh[i] = v <= 0 ? 0 : sigma * Math.sqrt(v);
        }
        builder.disaggregatedSeries(TsData.ofInternal(model.hDom.getStartPeriod(), yh).multiply(f))
                .stdevDisaggregatedSeries(TsData.ofInternal(model.hDom.getStartPeriod(), vyh));
        return builder.build();
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
        SsfData data = new SsfData(model.hY);
        Double lbound = spec.getTruncatedParameter();
        Mapping mapping = new Mapping(lbound == null ? -1 : lbound);
        boolean cl = spec.getResidualsModel() == Model.Ar1;
        return SsfFunction.builder(data, mapping,
                p -> ssf(p.getValue(), cl, spec.isZeroInitialization(), model.frequencyRatio))
                .regression(model.hEX, diffuseRegressors(model.nx(), spec))
                .useMaximumLikelihood(true)
                .build();
    }

    private static Ssf ssf(double rho, boolean cl, boolean zeroinit, int ratio) {
        SsfComponent cmp = cl ? AR1.of(rho, 1, zeroinit)
                : Arima_1_1_0.of(rho, 1, zeroinit);
        return SsfDisaggregation.of(cmp, ratio);
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

    private static class Mapping implements IParametricMapping<Parameter> {

        private final double lbound;

        private Mapping(double lbound) {
            this.lbound = lbound;
        }

        @Override
        public Parameter map(DoubleSequence p) {
            return new Parameter(p.get(0), ParameterType.Estimated); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public DoubleSequence getDefaultParameters() {
            return DoubleSequence.of(.9);
        }

        @Override
        public boolean checkBoundaries(DoubleSequence inparams) {
            double p = inparams.get(0);
            if (lbound == -1) {
                return p > -1 && p < 1;
            } else {
                return p >= lbound && p < 1;
            }
        }

        @Override
        public double epsilon(DoubleSequence inparams, int idx) {
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
