/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts;

import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.design.BuilderPattern;
import demetra.sts.BsmSpec;
import demetra.sts.Component;
import demetra.util.IntList;
import jdplus.data.DataBlock;
import jdplus.likelihood.DiffuseConcentratedLikelihood;
import jdplus.linearsystem.LinearSystemSolver;
import jdplus.math.functions.IFunctionDerivatives;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.akf.AugmentedSmoother;
import jdplus.ssf.dk.SsfFunction;
import jdplus.ssf.dk.SsfFunctionPoint;
import jdplus.ssf.implementations.RegSsf;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.SsfData;
import jdplus.stats.RobustStandardDeviationComputer;
import jdplus.sts.internal.BsmMapping;
import jdplus.sts.internal.BsmMonitor;

/**
 *
 * @author PALATEJ
 */
public class OutliersDetection {

    public static enum Estimation {
        Full, Score, Point;
    }

    private final BsmSpec spec;
    private final boolean ao, ls;
    private final double cv, tcv;
    private final int maxIter;
    private final Estimation forwardEstimation, backwardEstimation;
    private final double eps, eps2, fullEstimationThreshold;

    @BuilderPattern(OutliersDetection.class)
    public static class Builder {

        private BsmSpec spec;
        private boolean ao = true, ls = true;
        private double cv = 0;
        private double tcv = 0;
        private int maxIter = 20;
        private double precision = 1e-5;
        private double fullEstimationThreshold=5;
        private Estimation forwardEstimation = Estimation.Score, backwardEstimation = Estimation.Point;

        public Builder bsm(BsmSpec spec) {
            this.spec = spec;
            return this;
        }

        public Builder ao(boolean ao) {
            this.ao = ao;
            return this;
        }

        public Builder ls(boolean ls) {
            this.ls = ls;
            return this;
        }

        public Builder criticalValue(double cv) {
            this.cv = cv;
            return this;
        }

        public Builder tcriticalValue(double tcv) {
            this.tcv = tcv;
            return this;
        }

        public Builder maxIter(int maxIter) {
            this.maxIter = maxIter;
            return this;
        }

        public Builder forwardEstimation(Estimation method) {
            this.forwardEstimation = method;
            return this;
        }

        public Builder backardEstimation(Estimation method) {
            this.backwardEstimation = method;
            return this;
        }

        public Builder precision(double eps) {
            this.precision = eps;
            return this;
        }

        public Builder fullEstimationThreshold(double ft) {
            this.fullEstimationThreshold=ft;
            return this;
        }

        public OutliersDetection build() {
            return new OutliersDetection(spec, ao, ls, cv, tcv, maxIter, forwardEstimation, backwardEstimation, precision, fullEstimationThreshold);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private OutliersDetection(BsmSpec spec, boolean ao, boolean ls, double cv, double tcv,
            int maxIter, Estimation forwardEstimation, Estimation backwardEstimation, double eps, double ft) {
        this.spec = spec.clone();
        this.ao = ao;
        this.ls = ls;
        this.cv = cv;
        this.tcv = tcv;
        this.maxIter = maxIter;
        this.forwardEstimation = forwardEstimation;
        this.backwardEstimation = backwardEstimation;
        this.eps = eps;
        this.eps2 = Math.sqrt(eps);
        this.fullEstimationThreshold=ft;
    }

    public boolean process(DoubleSeq y, Matrix X, int period) {
        clear();
        int i = 0;

        Matrix W = x(y.length(), X);
        if (!fullEstimation(y, W, period, eps2)) {
            return false;
        }
        initialModel=model;
        initialLikelihood=getLikelihood();
        double curcv = criticalValue(y.length());
        // forward recursion
        while (i++ < maxIter) {
            sig = robustSigma();
            if (!iterate(y, W, curcv)) {
                break;
            }
            W = x(y.length(), X);
            if (!estimate(y, W, forwardEstimation)) {
                break;
            }
//            SsfFunction<BasicStructuralModel, SsfBsm2> fn = currentFunction(y, W);
//            likelihood = fn.evaluate(maxp).getLikelihood();
        }
        // backward recursion

        if (!fullEstimation(y, W, period, eps)) {
            return false;
        }
        double tcvcur=tcriticalValue(y.length());
        do {
            if (W == null) {
                break;
            }
            double[] tstats = getLikelihood().tstats(0, false);
            int nx = X == null ? 0 : X.getColumnsCount();
            if (tstats.length == nx) {
                break;
            }
            int jmin = 0;
            double tmin = Math.abs(tstats[nx]);
            for (int j = nx + 1; j < tstats.length; ++j) {
                if (Math.abs(tstats[j]) < tmin) {
                    tmin = Math.abs(tstats[j]);
                    jmin = j - nx;
                }
            }
            if (tmin > tcvcur) {
                break;
            }
            if (jmin < aoPositions.size()) {
                aoPositions.remove(jmin);
            } else {
                lsPositions.remove(jmin - aoPositions.size());
            }
            W = x(y.length(), X);
            if (!estimate(y, W, backwardEstimation)) {
                break;
            }
        } while (!aoPositions.isEmpty() && !lsPositions.isEmpty());

        return true;

    }

    private void clear() {
        aoPositions.clear();
        lsPositions.clear();
        model = null;
        likelihood = null;
        initialModel=null;
        initialLikelihood=null;
        regressors = null;
        curp = null;
        sig = 0;
        full=false;
    }

    private double criticalValue(int n) {
        if (cv != 0) {
            return cv;
        }
        if (ao && ls) {
            return defaultCriticalValue2(n);
        } else {
            return defaultCriticalValue(n);
        }
    }

    private double tcriticalValue(int n) {
        if (tcv != 0) {
            return tcv;
        }
        return Math.sqrt(defaultCriticalValue(n));
    }

    private boolean iterate(DoubleSeq y, Matrix W, double curcv) {
        SsfBsm ssf = SsfBsm.of(model);
        Ssf wssf = W == null ? ssf : RegSsf.ssf(ssf, W);
        AugmentedSmoother smoother = new AugmentedSmoother();
        smoother.setCalcVariances(true);
        SsfData data = new SsfData(y);
        DefaultSmoothingResults sd = DefaultSmoothingResults.full();
        int n = data.length();
        double sig2 = sig * sig;
        sd.prepare(wssf.getStateDim(), 0, data.length());
        smoother.process(wssf, data, sd);
        int imax = -1;
        double smax = 0;
        int type = -1;
        boolean isnoise=model.getVariance(Component.Noise)!= 0;
       for (int i = 0; i < n; ++i) {
             try {
                DataBlock R = DataBlock.of(sd.R(i));
                Matrix Rvar = sd.RVariance(i);
                double sao = 0, sls = 0, sall = 0;
                if (ao && isnoise) {
                    sao = R.get(0) * R.get(0) / Rvar.get(0, 0) / sig2;
                }
                if (ls && i>0) {
                    int c=isnoise ? 1 : 0;
                    sls = R.get(c) * R.get(c) / Rvar.get(c, c) / sig2;
                }
                if (!Double.isFinite(sao)) {
                    sao = 0;
                }
                if (!Double.isFinite(sls)) {
                    sls = 0;
                }
                if (! ls || i==0 ) {
                    sall = sao;
                } else if (!ao || ! isnoise) {
                    sall = sls;
                } else {
                    Matrix S = Rvar.extract(0, 2, 0, 2).deepClone();
                    DataBlock ur = R.extract(0, 2).deepClone();
                    SymmetricMatrix.lcholesky(S, 1e-9);
                    LowerTriangularMatrix.solveLx(S, ur, 1e-9);
                    sall = ur.ssq() / sig2;
                }
                if (sall > smax) {
                    imax = i;
                    smax = sall;
                    type = sao > sls ? 0 : 1;
                }
            } catch (Exception err) {
            }
        }
        if (smax < curcv) {
            return false;
        }
        full = smax > fullEstimationThreshold*curcv;
        switch (type) {
            case 0:
                aoPositions.add(imax);
                break;
            case 1:
                lsPositions.add(imax);
                break;
        }
        return true;
    }

    private boolean fullEstimation(DoubleSeq y, Matrix W, int period, double eps) {
        BsmMonitor monitor = new BsmMonitor();
        monitor.setSpecification(spec);
        monitor.useDiffuseRegressors(true);
        monitor.setPrecision(eps);
        monitor.process(y, W, period);
        curp = monitor.maxLikelihoodFunction().getParameters();
        model = monitor.getResult();
        likelihood = monitor.getLikelihood();
        return model != null;
    }

    private void pointEstimation(DoubleSeq y, Matrix W) {
        SsfFunction<BasicStructuralModel, SsfBsm2> fn = currentFunction(y, W);
        SsfFunctionPoint<BasicStructuralModel, SsfBsm2> pt = fn.evaluate(curp);
        likelihood = pt.getLikelihood();
        model = pt.getCore();
    }

    private void scoreEstimation(DoubleSeq y, Matrix W) {
        SsfFunction<BasicStructuralModel, SsfBsm2> fn = currentFunction(y, W);
        SsfFunctionPoint<BasicStructuralModel, SsfBsm2> pt = fn.evaluate(curp);
        try {
            IFunctionDerivatives D = pt.derivatives();
            Matrix H = D.hessian();
            DataBlock G = DataBlock.of(D.gradient());
            LinearSystemSolver.fastSolver().solve(H, G);
            DoubleSeq np = DoublesMath.subtract(curp, G);
            if (fn.getMapping().checkBoundaries(np)) {
                curp = np;
                pt = fn.evaluate(curp);
            }
        } catch (Exception err) {
        }
        likelihood = pt.getLikelihood();
        model = pt.getCore();
    }

    private boolean estimate(DoubleSeq y, Matrix W, Estimation method) {
        if (full)
            return fullEstimation(y, W, model.getPeriod(), eps2);
        try {
            switch (method) {
                case Point:
                    pointEstimation(y, W);
                    return true;
                case Score:
                    scoreEstimation(y, W);
                    return true;
                default:
                    return fullEstimation(y, W, model.getPeriod(), eps2);
            }
        } catch (Exception err) {
            return false;
        }
    }

//    private static double robustSigma(DoubleSeq y, Matrix W, BasicStructuralModel model) {
//        SsfBsm2 ssf = SsfBsm2.of(model);
//        Ssf wssf = W == null ? ssf : RegSsf.ssf(ssf, W);
//        DiffusePredictionErrorDecomposition e = new DiffusePredictionErrorDecomposition(true);
//        e.prepare(ssf, y.length());
//        DkToolkit.sqrtFilter(wssf, new SsfData(y), e, true);
//        DoubleSeq errors = e.errors(true, true);
//        return RobustStandardDeviationComputer.mad().compute(errors);
//    }
//
    private double robustSigma() {
        DoubleSeq errors = getLikelihood().e();
        return RobustStandardDeviationComputer.mad().compute(errors);
    }

    private Matrix x(int m, Matrix X) {
        int nx = X == null ? 0 : X.getColumnsCount();
        int nw = nx + aoPositions.size() + lsPositions.size();
        if (nw == 0) {
            return null;
        }
        Matrix W = Matrix.make(m, nw);
        int p = 0;
        if (nx > 0) {
            W.extract(0, m, 0, nx).copy(X);
            p = nx;
        }
        for (int i = 0; i < aoPositions.size(); ++i, ++p) {
            W.set(aoPositions.get(i), p, 1);
        }
        for (int i = 0; i < lsPositions.size(); ++i, ++p) {
            W.column(p).drop(lsPositions.get(i), 0).set(1);
        }
        return W;
    }

    SsfFunction<BasicStructuralModel, SsfBsm2> currentFunction(DoubleSeq y, Matrix W) {
        BsmMapping mapper = new BsmMapping(model.specification(), model.getPeriod(), BsmMapping.Transformation.None);
        int[] diffuse = null;
        if (W != null) {
            diffuse = new int[W.getColumnsCount()];
            for (int i = 0; i < diffuse.length; ++i) {
                diffuse[i] = i;
            }
        }
        return SsfFunction.builder(new SsfData(y), mapper, bsmmodel -> SsfBsm2.of(bsmmodel))
                .regression(W, diffuse)
                .useFastAlgorithm(true)
                .useParallelProcessing(false)
                .useLog(true)
                .useScalingFactor(true)
                .build();
    }

    private final IntList aoPositions = new IntList();
    private final IntList lsPositions = new IntList();
    private BasicStructuralModel initialModel, model;
    private DiffuseConcentratedLikelihood initialLikelihood, likelihood;
    private DoubleSeq curp;
    private Matrix regressors;
    private double sig;
    private boolean full;

    /**
     * @return the aoPositions
     */
    public int[] getAoPositions() {
        return aoPositions.toArray();
    }

    /**
     * @return the lsPositions
     */
    public int[] getLsPositions() {
        return lsPositions.toArray();
    }

    /**
     * @return the model
     */
    public BasicStructuralModel getModel() {
        return model;
    }

    /**
     * @return the initialModel
     */
    public BasicStructuralModel getInitialModel() {
        return initialModel;
    }

    /**
     * @return the initialLikelihood
     */
    public DiffuseConcentratedLikelihood getInitialLikelihood() {
        return initialLikelihood;
    }

    /**
     * @return the likelihood
     */
    public DiffuseConcentratedLikelihood getLikelihood() {
        return likelihood;
    }
    
    /**
     * @return the regressors
     */
    public Matrix getRegressors() {
        return regressors;
    }

    public static double defaultCriticalValue(int n) {
        return 1 / (0.06506323 + 2.19443019 / n - 17.48793935 / (n * n));
    }

    public static double defaultCriticalValue2(int n) {
        return 1 / (0.05508697 + 1.72286327 / n - 13.62470737 / (n * n));
    }


}
