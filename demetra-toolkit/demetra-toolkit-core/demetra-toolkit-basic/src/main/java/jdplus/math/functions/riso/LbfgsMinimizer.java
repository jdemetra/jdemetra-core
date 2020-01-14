/* RISO: an implementation of distributed belief networks.
 + Copyright (C) 1999, Robert Dodier.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA, 02111-1307, USA,
 * or visit the GNU web site, www.gnu.org.
 */
package jdplus.math.functions.riso;

import jdplus.data.DataBlock;
import jdplus.math.functions.IFunction;
import jdplus.math.functions.IFunctionDerivatives;
import jdplus.math.functions.IFunctionPoint;
import jdplus.math.matrices.Matrix;
import jdplus.math.functions.ParamValidation;
import demetra.data.DoubleSeq;
import jdplus.math.functions.FunctionMinimizer;

/**
 *
 * @author Jean Palate
 */
public class LbfgsMinimizer implements FunctionMinimizer {

    private static final int MAX_FAILED = 20;

    public static class LbfgsBuilder implements Builder{

        private double xtol = 1e-9, gtol = 1e-5;
        private int maxiter = 100;
        private double feps = 1e-9;
        private int memory = 7;

        private LbfgsBuilder() {
        }

        @Override
        public LbfgsBuilder functionPrecision(double eps) {
            feps = eps;
            return this;
        }

        public LbfgsBuilder parametersPrecision(double eps) {
            xtol = eps;
            return this;
        }

        public LbfgsBuilder gradientPrecision(double eps) {
            gtol = eps;
            return this;
        }

        @Override
       public LbfgsBuilder maxIter(int niter) {
            maxiter = niter;
            return this;
        }

        public LbfgsBuilder memoryLength(int n) {
            memory = n;
            return this;
        }

        @Override
        public LbfgsMinimizer build() {
            return new LbfgsMinimizer(this);
        }

    }

    public static LbfgsBuilder builder() {
        return new LbfgsBuilder();
    }

    private final double xtol, gtol;
    private final int maxiter;
    private final double feps;
    private final int memory;

    private final Lbfgs lbfgs = new Lbfgs();
    private int nfailed;
    private IFunction fn;
    private IFunctionPoint fpt;
    private boolean converged;
    
    public LbfgsMinimizer(LbfgsBuilder builder){
        this.xtol=builder.xtol;
        this.gtol=builder.gtol;
        this.feps=builder.feps;
        this.maxiter=builder.maxiter;
        this.memory=builder.memory;
    }

    @Override
    public Matrix curvatureAtMinimum() {
        return fpt.derivatives().hessian();
    }

    @Override
    public DoubleSeq gradientAtMinimum() {
        return fpt.derivatives().gradient();
    }

    // / <param name="eps">Determines the accuracy with which the solution
    // / is to be found. The subroutine terminates when ||G|| less EPS
    // max(1,||X||),
    // / where ||.|| denotes the Euclidean norm.</param>
    /**
     *
     * @return
     */
    public double getGTol() {
        return gtol;
    }

    /**
     *
     * @return
     */
    public int getNIter() {
        return lbfgs.getNIter();
    }

    /**
     *
     * @return
     */
    @Override
    public IFunctionPoint getResult() {
        return fpt;
    }

    @Override
    public double getObjective() {
        return fpt == null ? Double.NaN : fpt.getValue();
    }

    /**
     *
     * @param start
     * @return
     */
    @Override
    public boolean minimize(IFunctionPoint start) {
        fn = start.getFunction();
        fpt = start;
        converged = false;

        DoubleSeq px = fpt.getParameters();
        int n = px.length();
        if (n == 0) {
            return true;
        }
        double[] x = new double[n];
        px.copyTo(x, 0);
        int[] iflag = new int[]{0};
        int[] iprint = new int[]{-1};
        double[] diag = new double[n];
        nfailed = 0;
        double fmin = fpt.getValue(), fprev = 0, fcur = fmin;
        boolean failed = false;
        IFunctionDerivatives df = null;
        do {
            try {
                if (!failed) {
                    df = fpt.derivatives();
                }
                lbfgs.lbfgs(n, memory, x, failed ? 2 * Math.abs(fcur) : fcur, df.gradient().toArray(), false, diag,
                        iprint, gtol, xtol, iflag);
                DataBlock rx = DataBlock.copyOf(x);
                if (iflag[0] == 0) {
                    fpt = fn.evaluate(rx);
                    break;
                }
                if (iflag[0] != 1) {
                    return false;
                }
                ParamValidation validation = fn.getDomain().validate(rx);
                if (validation == ParamValidation.Invalid) {
                    failed = true;
                } else {
                    IFunctionPoint efn = fn.evaluate(rx);
                    fcur = efn.getValue();
                    if (fcur <= fmin) {
                        fprev = fmin;
                        fmin = fcur;
                        fpt = efn;
                        converged = Math.abs(fprev - fmin) < (1 + Math.abs(fmin)) * feps;
                    }
                    failed = false;
                }
            } catch (RuntimeException ex) {
                failed = true;
            }
        } while (next(failed));
        return converged;
    }

    protected boolean next(boolean failed) {
        if (failed) {
            if (nfailed++ > MAX_FAILED) {
                return false;
            }
            return true;
        }
        nfailed = 0;
        return lbfgs.getNIter() <= maxiter && !converged;
    }

}
