/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.functions.bfgs;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.data.DoubleSeqCursor;
import jdplus.maths.matrices.MatrixException;
import jdplus.maths.functions.FunctionException;
import jdplus.maths.functions.IFunction;
import jdplus.maths.functions.IFunctionPoint;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import demetra.data.DoubleSeq;
import jdplus.maths.functions.FunctionMinimizer;

/**
 * BFGS variable-metric method, based on Pascal code in J.C. Nash, `Compact
 * Numerical Methods for Computers', 2nd edition, converted by p2c then
 * re-crafted by B.D. Ripley. The current implementation is a refactoring of the
 * BFGS routine implemented in R.
 *
 * @author Jean Palate
 */
public class Bfgs implements FunctionMinimizer {

    private static final double STEPREDN = 0.2, ACCTOL = 0.0001, RELTEST = 10.0;

    public static BfgsBuilder builder() {
        return new BfgsBuilder();
    }

    /**
     * @return the fail
     */
    public int getFailCode() {
        return fail;
    }

    /**
     * @return the grcount
     */
    public int getGradientCount() {
        return grcount;
    }

    /**
     * @return the fncount
     */
    public int getFnCount() {
        return fncount;
    }

    public static class BfgsBuilder implements Builder {

        private double stepredn = STEPREDN, acctol = ACCTOL, reltest = RELTEST;
        private int maxit = 1000;
        private boolean trace = false;
        private int nreport = 10;
        private double abstol = 1e-9, reltol = 1e-9;

        @Override
        public BfgsBuilder maxIter(int maxiter) {
            this.maxit = maxiter;
            return this;
        }

        public BfgsBuilder trace(boolean trace) {
            this.trace = trace;
            return this;
        }

//      Too technical
//        
//        public Builder relativeTest(double reltest){
//            this.reltest=reltest;
//            return this;
//        }
//        
//        public Builder acceptationTol(double acctol){
//            this.acctol=acctol;
//            return this;
//        }
        public BfgsBuilder reportingInterval(int nreport) {
            this.nreport = nreport;
            return this;
        }

        public BfgsBuilder stepReduction(double stepredn) {
            this.stepredn = stepredn;
            return this;
        }

        public BfgsBuilder absolutePrecision(double abstol) {
            this.abstol = abstol;
            return this;
        }

        @Override
        public BfgsBuilder functionPrecision(double reltol) {
            this.reltol = reltol;
            return this;
        }

        @Override
        public Bfgs build() {
            return new Bfgs(this);
        }
    }

    private final double stepredn, acctol, reltest;
    private final int maxit;
    private final boolean trace;
    private final int nreport;
    private final double abstol;
    private final double reltol;

    private int fail, iter;
    private int grcount;
    private int fncount;

    // current function point
    private IFunctionPoint fcur;
    private double Fmin;
    private double[] btry;
    private CanonicalMatrix H;
    private DoubleSeq g;

    public Bfgs(BfgsBuilder builder) {
        this.abstol = builder.abstol;
        this.acctol = builder.acctol;
        this.maxit = builder.maxit;
        this.nreport = builder.nreport;
        this.reltest = builder.reltest;
        this.reltol = builder.reltol;
        this.stepredn = builder.stepredn;
        this.trace = builder.trace;
    }

    private void vmmin(DoubleSeq b, IFunction fn) {
        boolean accpoint, enough;
        double[] t, X, c;
        CanonicalMatrix B;
        int count, funcount, gradcount;
        double f, gradproj;
        int i, j, ilast;
        double s, steplength;
        double D1, D2;
        int[] l;
        iter = 0;
        btry = b.toArray();
        int n = btry.length;
        fcur = fn.evaluate(b);
        f = fcur.getValue();
        if (maxit <= 0 || n == 0) {
            fail = 0;
            Fmin = fcur.getValue();
            fncount = grcount = 0;
            return;
        }

        if (nreport <= 0) {
            throw new FunctionException("REPORT must be > 0 (method = \"BFGS\")");
        }
        t = new double[n];
        B = CanonicalMatrix.square(n);

        if (!Double.isFinite(f)) {
            throw new FunctionException("initial value in 'vmmin' is not finite");
        }
        if (trace) {
            System.out.println("initial  value " + f);
        }
        Fmin = f;
        funcount = gradcount = 1;

        g = fcur.derivatives().gradient();
        iter++;
        ilast = gradcount;

        do {
            if (ilast == gradcount) {
                B.set(0);
                B.diagonal().set(1);
            }

            X = btry.clone();
            c = g.toArray();
            gradproj = 0.0;
            DataBlockIterator bcols = B.columnsIterator();
            DoubleSeqCursor gcur = g.cursor();
            for (i = 0; i < n; i++) {
                s = -bcols.next().dot(g);
                t[i] = s;
                gradproj += s * gcur.getAndNext();
            }

            if (gradproj < 0.0) {
                /* search direction is downhill */
                steplength = 1.0;
                accpoint = false;
                do {
                    count = 0;
                    for (i = 0; i < n; i++) {
                        btry[i] = X[i] + steplength * t[i];
                        if (reltest + X[i] == reltest + btry[i]) /* no change */ {
                            count++;
                        }
                    }
                    if (count < n) {
                        DoubleSeq Btry = DoubleSeq.of(btry);
                        if (fn.getDomain().checkBoundaries(Btry)) {
                            fcur = fn.evaluate(Btry);
                            f = fcur.getValue();
                            funcount++;
                            accpoint = Double.isFinite(f) && (f <= Fmin + gradproj * steplength * acctol);
                        } else {
                            accpoint = false;
                        }
                        if (!accpoint) {
                            steplength *= stepredn;
                        }
                    }
                } while (!(count == n || accpoint));
                enough = (f > abstol)
                        && Math.abs(f - Fmin) > reltol * (Math.abs(Fmin) + reltol);
                /* stop if value if small or if relative change is low */
                if (!enough) {
                    count = n;
                    Fmin = f;
                }
                if (count < n) {/* making progress */
                    Fmin = f;
                    g = fcur.derivatives().gradient();
                    gradcount++;
                    iter++;
                    D1 = 0.0;
                    for (i = 0; i < n; i++) {
                        t[i] = steplength * t[i];
                        c[i] = g.get(i) - c[i];
                        D1 += t[i] * c[i];
                    }
                    if (D1 > 0) {
                        D2 = 0.0;
                        bcols.begin();
                        DataBlock C = DataBlock.of(c);
                        for (i = 0; i < n; i++) {
                            s = bcols.next().dot(C);
                            X[i] = s;
                            D2 += s * c[i];
                        }
                        D2 = 1.0 + D2 / D1;
                        for (i = 0; i < n; i++) {
                            for (j = 0; j <= i; j++) {
                                B.add(i, j, (D2 * t[i] * t[j]
                                        - X[i] * t[j] - t[i] * X[j]) / D1);
                            }
                        }
                        SymmetricMatrix.fromLower(B);
                    } else {
                        /* D1 < 0 */
                        ilast = gradcount;
                    }
                } else /* no progress */ if (ilast < gradcount) {
                    count = 0;
                    ilast = gradcount;
                }
            } else {
                /* uphill search */
                count = 0;
                if (ilast == gradcount) {
                    count = n;
                } else {
                    ilast = gradcount;
                }
                /* Resets unless has just been reset */
            }
            if (trace && (iter % nreport == 0)) {
                System.out.printf("iter%4d value %f", iter, f).println();
            }
            if (iter >= maxit) {
                break;
            }
            if (gradcount - ilast > 2 * n) {
                ilast = gradcount;
                /* periodic restart */
            }
        } while (count != n || ilast != gradcount);
        if (trace) {
            System.out.printf("final  value %f", Fmin).println();
            if (iter < maxit) {
                System.out.println("converged");
            } else {
                System.out.printf("stopped after %d iterations\n", iter).println();
            }
        }
        fail = (iter < maxit) ? 0 : 1;
        fncount = funcount;
        grcount = gradcount;
        try {
            H = SymmetricMatrix.inverse(B);
        } catch (MatrixException err) {
            H = fcur.derivatives().hessian();
        }
    }

    public BfgsBuilder toBuilder() {
        BfgsBuilder builder = new BfgsBuilder();
        builder.abstol = abstol;
        builder.acctol = acctol;
        builder.maxit = maxit;
        builder.nreport = nreport;
        builder.reltest = reltest;
        builder.reltol = reltol;
        builder.stepredn = stepredn;
        builder.trace = trace;
        return builder;
    }

    @Override
    public CanonicalMatrix curvatureAtMinimum() {
        return H;
    }

    @Override
    public DoubleSeq gradientAtMinimum() {
        return g;
    }

    @Override
    public IFunctionPoint getResult() {
        return fcur;
    }

    @Override
    public double getObjective() {
        return Fmin;
    }

    @Override
    public boolean minimize(IFunctionPoint start) {
        vmmin(start.getParameters(), start.getFunction());
        return fail == 0;
    }

}
