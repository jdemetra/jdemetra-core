/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.tests.seasonal;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.data.TrigonometricSeries;
import demetra.data.WindowFunction;
import demetra.design.IBuilder;
import demetra.dstats.F;
import demetra.dstats.ProbabilityType;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.linearmodel.LeastSquaresResults;
import demetra.linearmodel.LinearModel;
import demetra.linearmodel.Ols;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.stats.RobustCovarianceComputer;
import demetra.timeseries.regression.PeriodicDummies;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class CanovaHansen {

    public static enum Variables {

        Dummy, Trigonometric, UserDefined
    }

    public static Builder test(DoubleSequence s, int period) {
        return new Builder(s, period);
    }

    public static class Builder implements IBuilder<CanovaHansen> {

        private final DoubleSequence s;
        private final int period;
        private boolean lag1 = true;
        private Variables type = Variables.Dummy;
        private WindowFunction winFunction=WindowFunction.Bartlett;
        private int truncationLag=12;
        private int startPosition;

        private Builder(DoubleSequence s, int period) {
            this.s = s;
            this.period = period;
        }

        public Builder variables(Variables variables) {
            this.type = variables;
            return this;
        }

        public Builder lag1(boolean lag1) {
            this.lag1 = lag1;
            return this;
        }

        public Builder truncationLag(int truncationLag) {
            this.truncationLag = truncationLag;
            return this;
        }

        public Builder windowFunction(WindowFunction winFunction) {
            this.winFunction = winFunction;
            return this;
        }

        public Builder startPosition(int startPosition) {
            this.startPosition = startPosition;
            return this;
        }

        @Override
        public CanovaHansen build() {
            Matrix x = sx();
            LinearModel lm = buildModel(x);
            Ols ols = new Ols();
            LeastSquaresResults olsResults = ols.compute(lm);
            ConcentratedLikelihood likelihood = olsResults.getLikelihood();
            DoubleSequence e = lm.calcResiduals(olsResults.getCoefficients());
            Matrix xe = x.deepClone();
            // multiply the columns of x by e
            xe.applyByColumns(c -> c.apply(e, (a, b) -> a * b));
            Matrix omega = RobustCovarianceComputer.covariance(xe, winFunction, truncationLag);
            return new CanovaHansen(x, xe, e, likelihood, omega);
        }

        private Matrix sx() {
            int len = s.length();
            int pos = startPosition;
            if (lag1) {
                ++pos;
                --len;
            }
            switch (type) {
                case Dummy: {
                    PeriodicDummies vars = new PeriodicDummies(period);
                    return vars.matrix(len, pos);
                }
                case Trigonometric: {
                    TrigonometricSeries vars = TrigonometricSeries.regular(period);
                    return vars.matrix(len, pos);
                }
                default:
                    return null;
            }

        }

        private LinearModel buildModel(Matrix sx) {

            LinearModel.Builder builder = LinearModel.builder();
            if (lag1) {
                builder.y(s.drop(1, 0))
                        .addX(s.drop(0, 1));
            } else {
                builder.y(s);
            }
            switch (type) {
                case Dummy: {
                    builder.addX(sx);
                    break;
                }
                case Trigonometric: {
                    builder.addX(sx)
                            .meanCorrection(true);
                    break;
                }
                default:
                    builder.meanCorrection(true);
            }
            return builder.build();
        }
    }

    /**
     * @return the e
     */
    public DoubleSequence getE() {
        return e;
    }

    /**
     * @return the ll
     */
    public ConcentratedLikelihood getLikelihood() {
        return ll;
    }

    private final Matrix x, xe, cxe, omega;
    private final DoubleSequence e;
    private final ConcentratedLikelihood ll;

    private CanovaHansen(final Matrix x, final Matrix xe,
            final DoubleSequence e, final ConcentratedLikelihood ll, final Matrix omega) {
        this.x = x;
        this.xe = xe;
        this.e = e;
        this.ll = ll;
        cxe = xe.deepClone();
        cxe.applyByColumns(c -> c.cumul());
        this.omega = omega;
    }

    public double test(int var) {
        return computeStat(omega.extract(var, 1, var, 1), cxe.extract(0, cxe.getRowsCount(), var, 1));
    }

    public double test(int var, int nvars) {
        return computeStat(omega.extract(var, nvars, var, nvars), cxe.extract(0, cxe.getRowsCount(), var, nvars));
    }

    public double testAll() {
        return computeStat(omega, cxe);
    }

    public double robustTestCoefficients() {
        Matrix rcov = robustCovarianceOfCoefficients();
        SymmetricMatrix.lcholesky(rcov);
        double[] tmp = ll.coefficients().toArray();
        DataBlock b = DataBlock.ofInternal(tmp, tmp.length - rcov.getRowsCount(), tmp.length, 1);
        LowerTriangularMatrix.rsolve(rcov, b);
        double f = b.ssq() / x.getColumnsCount();
        F ftest = new F(b.length(), x.getRowsCount());
        return ftest.getProbability(f, ProbabilityType.Upper);
    }

//    public double olsTestCoefficients() {
//        Matrix rcov = ll.getBVar().clone();
//        SymmetricMatrix.lcholesky(rcov);
//        double[] tmp = ll.getB().clone();
//        DataBlock b = DataBlock.ofInternal(tmp, 1, tmp.length, 1);
//        LowerTriangularMatrix.rsolve(rcov, b);
//        double f = b.ssq() / x.getColumnsCount();
//        F ftest = new F(b.length(), x.getRowsCount());
//        return ftest.getProbability(f, ProbabilityType.Upper);
//    }
    private double computeStat(Matrix O, Matrix cx) {
        int n = cx.getRowsCount(), nx = cx.getColumnsCount();
        // compute tr( O^-1*xe'*xe)
        // cusum
        Matrix FF = Matrix.square(nx);
        for (int i = 0; i < n; ++i) {
            FF.addXaXt(1, cx.row(i));
        }
        // LL'^-1 * xe2 = L'^-1* L^-1 xe2 = L'^-1*a <-> a=L^-1 xe2 <->La=xe2
        Matrix sig = O.deepClone();
        SymmetricMatrix.lcholesky(sig);
        LowerTriangularMatrix.rsolve(sig, FF);
        // b=L'^-1*a <-> L'b=a <->b'L = a'
        LowerTriangularMatrix.lsolve(sig, FF.transpose());
        double tr = FF.diagonal().sum();
        return tr / (n * n);
    }

    private Matrix robustCovarianceOfCoefficients() {
        Matrix Lo = omega.deepClone();
        SymmetricMatrix.lcholesky(Lo);

        Matrix Lx = SymmetricMatrix.XXt(x);
        SymmetricMatrix.lcholesky(Lx);
        LowerTriangularMatrix.rsolve(Lx, Lo);
        LowerTriangularMatrix.lsolve(Lx, Lo.transpose());

        Matrix XXt = SymmetricMatrix.XXt(Lo);
        XXt.mul(xe.getRowsCount());
        return XXt;
    }

}
