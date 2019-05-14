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
package demetra.maths.functions.riso;

import demetra.data.DataBlock;
import demetra.maths.functions.IFunction;
import demetra.maths.functions.IFunctionDerivatives;
import demetra.maths.functions.IFunctionMinimizer;
import demetra.maths.functions.IFunctionPoint;
import demetra.maths.matrices.CanonicalMatrix;
import demetra.maths.functions.ParamValidation;
import demetra.data.DoubleSeq;


/**
 *
 * @author Jean Palate
 */
public class LbfgsMinimizer implements IFunctionMinimizer {

    private static final int MAX_FAILED = 20;

    private double xtol = 1e-9, gtol = 1e-5;

    private int maxiter = 100, nfailed;

    private Lbfgs lbfgs = new Lbfgs();

    private double feps = 1e-9;

    private int memory = 7;
    private IFunction fn;
    private IFunctionPoint fpt;
    private boolean converged;

    @Override
    public IFunctionMinimizer exemplar() {
        LbfgsMinimizer min = new LbfgsMinimizer();
        min.feps = feps;
        min.memory = memory;
        min.maxiter = maxiter;
        min.xtol = xtol;
        min.gtol = gtol;
        return min;
    }

    @Override
    public double getFunctionPrecision() {
        return feps;
    }

    @Override
    public CanonicalMatrix curvatureAtMinimum() {
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
    @Override
    public int getIterCount() {
        return lbfgs.getNIter();
    }

    /**
     *
     * @return
     */
    @Override
    public int getMaxIter() {
        return maxiter;
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
    // / <param name="xtol">An estimate of the machine precision (e.g. 10e-16 on
    // a
    // / SUN station 3/60). The line search routine will terminate if the
    // / relative width of the interval of uncertainty is less than xtol</param>
    /**
     *
     * @return
     */
    public double getXTol() {
        return xtol;
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
                if (validation==ParamValidation.Invalid) {
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

    /**
     *
     * @param value
     */
    @Override
    public void setFunctionPrecision(double value) {
        feps = value;
    }

    /**
     *
     * @param value
     */
    public void setGTol(double value) {
        gtol = value;
    }

    /**
     *
     * @param val
     */
    @Override
    public void setMaxIter(int val) {
        maxiter = val;
    }

    /**
     * @return the memory
     */
    public int getMemoryLength() {
        return memory;
    }

    /**
     * @param m
     */
    public void setMemoryLength(int m) {
        memory = m;
    }

    @Override
    public double getParametersPrecision() {
        return xtol;
    }

    @Override
    public void setParametersPrecsion(double value) {
        xtol=value;
    }
}
