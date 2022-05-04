/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tempdisagg.univariate;

import demetra.data.AggregationType;
import demetra.data.DoubleSeq;
import demetra.data.Parameter;
import demetra.math.functions.ObjectiveFunctionPoint;
import demetra.tempdisagg.univariate.TemporalDisaggregationISpec;
import demetra.timeseries.TsData;
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import jdplus.benchmarking.ssf.SsfCumulator;
import jdplus.benchmarking.univariate.BenchmarkingUtility;
import jdplus.data.DataBlock;
import jdplus.math.functions.IFunction;
import jdplus.math.functions.IFunctionPoint;
import jdplus.math.functions.IParametersDomain;
import jdplus.math.functions.ParamValidation;
import jdplus.math.functions.bfgs.Bfgs;
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.StateComponent;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.implementations.Loading;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.SsfData;

/**
 * Model:
 * we are looking for y where
 * c = a + b y + e
 * with sum(y) = Y
 *
 * See for instance Bournay-Laroque, annales de l'INSEE, 1979, n°36
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class ProcessorI {

    public TemporalDisaggregationIResults process(TsData aggregatedSeries, TsData indicator, TemporalDisaggregationISpec spec) {

        int ratio = indicator.getTsUnit().ratioOf(aggregatedSeries.getTsUnit());
        if (ratio == TsUnit.NO_RATIO || ratio == TsUnit.NO_STRICT_RATIO) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }

        TsData naggregatedSeries;
        switch (spec.getAggregationType()) {
            case Sum, Average -> naggregatedSeries = BenchmarkingUtility.constraints(indicator, aggregatedSeries);
            case Last -> naggregatedSeries = BenchmarkingUtility.constraintsByPosition(indicator, aggregatedSeries, ratio - 1);
            case First -> naggregatedSeries = BenchmarkingUtility.constraintsByPosition(indicator, aggregatedSeries, 0);
            case UserDefined -> naggregatedSeries = BenchmarkingUtility.constraintsByPosition(indicator, aggregatedSeries, spec.getObservationPosition());
            default -> throw new TsException(TsException.INVALID_OPERATION);
        }

        TsPeriod sh = indicator.getStart();
        TsPeriod sl = TsPeriod.of(sh.getUnit(), naggregatedSeries.getStart().start());
        int offset = sh.until(sl);
        switch (spec.getAggregationType()) {
            case Last -> offset += ratio - 1;
            case UserDefined -> offset += spec.getObservationPosition();
        }

        boolean cumul = spec.getAggregationType() == AggregationType.Average
                || spec.getAggregationType() == AggregationType.Sum;
        double[] Y = naggregatedSeries.getValues().toArray();
        int nxc = cumul ? Y.length * ratio : ((Y.length - 1) * ratio + 1);
        double[] xc = indicator.getValues().extract(offset, nxc).toArray();
        if (cumul) {
            cumul(xc, ratio);
        }
        // compute the coefficient b
        Parameter p = parameter(spec);
        FunctionI I = new FunctionI(xc, Y, ratio, p, cumul, spec.getTruncatedRho());
        Bfgs bfgs = Bfgs.builder()
                .functionPrecision(spec.getEstimationPrecision())
                .build();
        DoubleSeq initialParameters;
        double b = initialB(xc, Y, ratio, cumul ? ratio - 1 : 0);
        if (p.isFixed()) {
            initialParameters = DoubleSeq.of(b);
        } else {
            initialParameters = DoubleSeq.of(b, p.getValue());
        }
        bfgs.minimize(I.evaluate(initialParameters));
        FunctionI.Point pt = (FunctionI.Point) bfgs.getResult();
        DoubleSeq grad = bfgs.gradientAtMinimum();
        DoubleSeq finalParameters = pt.getParameters();

        // final estimation
        double[] yall = new double[indicator.length()];
        for (int i = 0; i < offset; ++i) {
            yall[i] = Double.NaN;
        }
        System.arraycopy(pt.getZ(), 0, yall, offset, nxc);
        for (int i = offset + nxc; i < yall.length; ++i) {
            yall[i] = Double.NaN;
        }

        double rho = pt.getRho();
        SsfData data = new SsfData(yall);
        double[] yq;
        b = finalParameters.get(0);
        double a;
        if (cumul) {
            Ssf ssf = Ssf.of(SsfCumulator.of(new StateComponent(new IInitialization(rho), new IDynamics(rho)),
                    Loading.sum(), ratio, offset % ratio), SsfCumulator.defaultLoading(Loading.sum(), ratio, offset % ratio));
            DefaultSmoothingResults sr = DkToolkit.sqrtSmooth(ssf, data, false, false);
            a = sr.a(0).get(1);
            yq = sr.getComponent(2).toArray();

        } else {
            Ssf ssf = Ssf.of(new IInitialization(rho), new IDynamics(rho), Loading.sum());
            DefaultSmoothingResults sr = DkToolkit.sqrtSmooth(ssf, data, false, false);
            a = sr.a(0).get(0);
            yq = sr.getComponent(1).toArray();
        }
        DiffuseLikelihood ll = pt.getLl();
        if (spec.getAggregationType() == AggregationType.Average) {
            b /= ratio;
        }

        for (int i = 0; i < yq.length; ++i) { // x = a + b y + e <-> y = (x - a - e)/b  
            yq[i] = (indicator.getValue(i) - yq[i] - a) / b;
        }

        int np = finalParameters.length();
        if (spec.isConstant()) {
            ++np;
        }
        
        return TemporalDisaggregationIResults.builder()
                .a(a)
                .b(b)
                .maximum(new ObjectiveFunctionPoint(pt.getValue(), finalParameters.toArray(), grad.toArray(), bfgs.curvatureAtMinimum().unmodifiable()))
                .disaggregatedSeries(TsData.ofInternal(indicator.getStart(), yq))
                .likelihood(ll.stats(0, np))
                .build();
    }

    private double initialB(double[] x, double[] y, int ratio, int start) {
        double num = 0, denom = 0;
        for (int i = 0, j = start; i < y.length; ++i, j += ratio) {
            num += x[j];
            denom += y[i];
        }
        return denom == 0 ? 0 : num / denom;
    }

    private void cumul(double[] xc, int ratio) {
        int i = 0;
        while (i < xc.length) {
            ++i;
            for (int j = 1; j < ratio; ++j, ++i) {
                xc[i] += xc[i - 1];
            }
        }
    }

    private Parameter parameter(TemporalDisaggregationISpec spec) {
        return switch (spec.getResidualsModel()) {
            case Rw -> Parameter.fixed(1);
            case Ar1 -> spec.getParameter();
            default -> Parameter.fixed(0);
        };
    }

}

class FunctionI implements IFunction {

    private static final double EPS = 1e-6;

    private final double[] xc;
    private final double[] Y;
    private final int ratio;
    private final Parameter p;
    private final boolean agg;
    private final double truncatedRho;

    FunctionI(final double[] xc, final double[] Y, final int ratio, final Parameter p, final boolean agg, final double truncatedRho) {
        this.xc = xc;
        this.Y = Y;
        this.ratio = ratio;
        this.p = p;
        this.agg = agg;
        this.truncatedRho = truncatedRho == -1 ? -1 + EPS : truncatedRho;
    }

    private double[] z(double b) {
        double[] z = new double[xc.length];
        final int m = ratio - 1;

        int i = 0;
        int j = 0;
        if (!agg) {
            z[i] = xc[i] - b * Y[j++];
            ++i;
        }
        while (j < Y.length) {
            for (int k = 0; k < m; ++k) {
                z[i++] = Double.NaN;
            }
            z[i] = xc[i] - b * Y[j++];
            ++i;
        }
        while (i < xc.length) {
            z[i++] = Double.NaN;
        }
        return z;
    }

    @Override
    public IFunctionPoint evaluate(DoubleSeq parameters) {

        return new Point(parameters);
    }

    @Override
    public IParametersDomain getDomain() {
        return new Domain();
    }

    class Point implements IFunctionPoint {

        /**
         * @return the b
         */
        public double getB() {
            return b;
        }

        /**
         * @return the rho
         */
        public double getRho() {
            return rho;
        }

        /**
         * @return the z
         */
        public double[] getZ() {
            return z;
        }

        /**
         * @return the ll
         */
        public DiffuseLikelihood getLl() {
            return ll;
        }

        private final double b, rho;
        private final double[] z;
        private final DiffuseLikelihood ll;

        Point(DoubleSeq parameters) {
            b = parameters.get(0);
            if (parameters.length() > 1) {
                rho = parameters.get(1);
            } else {
                rho = p.getValue();
            }
            if (Math.abs(rho) > 1) {
                z = null;
                ll = null;
            } else {
                z = z(b);
                Ssf ssf;
                SsfData data = new SsfData(z);
                if (agg) {
                    ssf = Ssf.of(SsfCumulator.of(new StateComponent(new IInitialization(rho), new IDynamics(rho)),
                            Loading.sum(), ratio, 0), SsfCumulator.defaultLoading(Loading.sum(), ratio, 0));
                } else {
                    ssf = Ssf.of(new IInitialization(rho), new IDynamics(rho), Loading.sum());
                }
                ll = DkToolkit.likelihood(ssf, data, true, false);
            }
        }

        @Override
        public IFunction getFunction() {
            return FunctionI.this;
        }

        @Override
        public DoubleSeq getParameters() {
            if (p.isFixed()) {
                return DoubleSeq.of(getB());
            } else {
                return DoubleSeq.of(getB(), getRho());
            }
        }

        @Override
        public double getValue() {
            return -ll.logLikelihood();
        }

    }

    class Domain implements IParametersDomain {

        @Override
        public boolean checkBoundaries(DoubleSeq inparams) {
            if (inparams.length() > 1) {
                double x = inparams.get(1);
                return x >= truncatedRho && x <= 1 - EPS;
            } else {
                return true;
            }
        }

        @Override
        public double epsilon(DoubleSeq inparams, int idx) {
            if (idx == 0) {
                return Math.max(1e-6, 1e-6 * Math.abs(inparams.get(0)));
            } else {
                return inparams.get(1) <= 0 ? 1e-6 : -1e-6;
            }
        }

        @Override
        public int getDim() {
            return p.isFixed() ? 1 : 2;
        }

        @Override
        public double lbound(int idx) {
            if (idx == 0) {
                return -Double.MAX_VALUE;
            } else {
                return truncatedRho;
            }
        }

        @Override
        public double ubound(int idx) {
            if (idx == 0) {
                return Double.MAX_VALUE;
            } else {
                return 1 - EPS;
            }
        }

        @Override
        public ParamValidation validate(DataBlock ioparams) {
            if (ioparams.length() > 1) {
                boolean changed = false;
                double r = ioparams.get(1);
                if (r < truncatedRho) {
                    r = truncatedRho;
                    changed = true;
                }
                if (r > 1 + EPS) {
                    r = 1 / r;
                    changed = true;
                } else if (r > 1 - EPS) {
                    r = 1 / (r + 2 * EPS);
                    changed = true;
                }
                if (changed) {
                    ioparams.set(1, r);
                    return ParamValidation.Changed;
                }
            }
            return ParamValidation.Valid;
        }

    }

}

class IInitialization implements ISsfInitialization {

    private final double rho;

    IInitialization(double rho) {
        this.rho = rho;
    }

    @Override
    public int getStateDim() {
        return 2;
    }

    @Override
    public boolean isDiffuse() {
        return true;
    }

    @Override
    public int getDiffuseDim() {
        return rho == 1 ? 2 : 1;
    }

    @Override
    public void diffuseConstraints(FastMatrix b) {
        if (rho == 1) {
            b.diagonal().set(1);
        } else {
            b.set(0, 0, 1);
        }
    }

    @Override
    public void a0(DataBlock a0) {
    }

    @Override
    public void Pf0(FastMatrix pf0) {
        if (rho != 1) {
            pf0.set(1, 1, 1 / (1 - rho * rho));
        }
    }

    @Override
    public void Pi0(FastMatrix pi0) {
        if (rho == 1) {
            pi0.diagonal().set(1);
        } else {
            pi0.set(0, 0, 1);
        }
    }
}

class IDynamics implements ISsfDynamics {

    private final double rho;

    IDynamics(double rho) {
        this.rho = rho;
    }

    @Override
    public int getInnovationsDim() {
        return 1;
    }

    @Override
    public void V(int pos, FastMatrix qm) {
        qm.set(1, 1, 1);
    }

    @Override
    public void S(int pos, FastMatrix cm) {
        cm.set(1, 0, 1);
    }

    @Override
    public boolean hasInnovations(int pos) {
        return true;
    }

    @Override
    public boolean areInnovationsTimeInvariant() {
        return true;
    }

    @Override
    public void T(int pos, FastMatrix tr) {
        tr.set(0, 0, 1);
        tr.set(1, 1, rho);
    }

    @Override
    public void TX(int pos, DataBlock x) {
        if (rho != 1) {
            x.mul(1, rho);
        }
    }

    @Override
    public void addSU(int pos, DataBlock x, DataBlock u) {
        x.add(1, u.get(0));
    }

    @Override
    public void addV(int pos, FastMatrix p) {
        p.add(1, 1, 1);
    }

    @Override
    public void XT(int pos, DataBlock x) {
        if (rho != 1) {
            x.mul(1, rho);
        }
    }

    @Override
    public void XS(int pos, DataBlock x, DataBlock xs) {
        xs.set(0, x.get(1));
    }

    @Override
    public boolean isTimeInvariant() {
        return true;
    }

}
