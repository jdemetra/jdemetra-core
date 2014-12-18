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
package ec.tstoolkit.maths.realfunctions.riso;

import ec.tstoolkit.algorithm.IProcessingHook;
import ec.tstoolkit.algorithm.ProcessingHookProvider;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.maths.realfunctions.IFunctionDerivatives;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.maths.realfunctions.IFunctionMinimizer;
import ec.tstoolkit.maths.realfunctions.ParamValidation;

/**
 *
 * @author Jean Palate
 */
public class LbfgsMinimizer extends ProcessingHookProvider<LbfgsMinimizer, IFunctionInstance> implements IFunctionMinimizer {

    private static final int MAX_FAILED = 100;

    private double m_xtol = 1e-9, m_gtol = 1e-5;

    private int m_maxiter = 100, m_nfailed;

    private Lbfgs m_lbfgs = new Lbfgs();

    private double m_eps = 1e-9;

    private int m_m = 7;
    private IFunction m_fn;
    private IFunctionInstance m_fcur;
    private boolean m_converged;

    @Override
    public IFunctionMinimizer exemplar() {
        LbfgsMinimizer min = new LbfgsMinimizer();
        min.m_eps = m_eps;
        min.m_m = m_m;
        min.m_maxiter = m_maxiter;
        min.m_xtol = m_xtol;
        min.m_gtol = m_gtol;
        min.copyHooks(this);
        return min;
    }

    @Override
    public double getConvergenceCriterion() {
        return m_eps;
    }

    @Override
    public Matrix getCurvature() {
        return m_fn.getDerivatives(m_fcur).getHessian();
    }

    @Override
    public double[] getGradient() {
        return m_fn.getDerivatives(m_fcur).getGradient();
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
        return m_gtol;
    }

    /**
     *
     * @return
     */
    @Override
    public int getIterCount() {
        return m_lbfgs.getNIter();
    }

    /**
     *
     * @return
     */
    @Override
    public int getMaxIter() {
        return m_maxiter;
    }

    /**
     *
     * @return
     */
    public int getNIter() {
        return m_lbfgs.getNIter();
    }

    /**
     *
     * @return
     */
    @Override
    public IFunctionInstance getResult() {
        return m_fcur;
    }

    @Override
    public double getObjective() {
        return m_fcur == null ? Double.NaN : m_fcur.getValue();
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
        return m_xtol;
    }

    /**
     *
     * @param function
     * @param start
     * @return
     */
    @Override
    public boolean minimize(IFunction function, IFunctionInstance start) {
        m_fn = function;
        m_fcur = start;
        m_converged = false;

        IReadDataBlock px = m_fcur.getParameters();
        int n = px.getLength();
        if (n == 0) {
            return true;
        }
        double[] x = new double[n];
        px.copyTo(x, 0);
        int[] iflag = new int[]{0};
        int[] iprint = new int[]{-1};
        double[] diag = new double[n];
        m_nfailed = 0;
        double fmin = m_fcur.getValue(), fprev = 0, fcur = fmin;
        boolean failed = false;
        boolean success = false;
        IFunctionDerivatives df = null;
        do {
            try {
                success = false;
                if (!failed) {
                    df = m_fn.getDerivatives(m_fcur);
                }
                m_lbfgs.lbfgs(n, m_m, x, failed ? 2 * Math.abs(fcur) : fcur, df.getGradient(), false, diag,
                        iprint, m_gtol, m_xtol, iflag);
                DataBlock rx = new DataBlock(x);
                if (iflag[0] == 0) {
                    m_fcur = m_fn.evaluate(rx);
                    break;
                }
                if (iflag[0] != 1) {
                    return false;
                }
                if (!m_fn.getDomain().checkBoundaries(rx)) {
                    failed = true;
                } else {
                    IFunctionInstance efn = m_fn.evaluate(rx);
                    fcur = efn.getValue();
                    if (fcur <= fmin) {
                        fprev = fmin;
                        fmin = fcur;
                        m_fcur = efn;
                        m_converged = Math.abs(fprev - fmin) < (1 + Math.abs(fmin)) * m_eps;
                        success = true;
                    }
                    failed = false;
                }
            } catch (RuntimeException ex) {
                failed = true;
            }
        } while (next(failed, success));
        return m_converged;
    }

    protected boolean next(boolean failed, boolean success) {
        if (success && hasHooks()) {
            IProcessingHook.HookInformation<LbfgsMinimizer, IFunctionInstance> hinfo
                    = new IProcessingHook.HookInformation<>(this, m_fcur);
            processHooks(hinfo, true);
            if (hinfo.cancel) {
                return false;
            }
        }
        if (failed) {
            if (m_nfailed++ > MAX_FAILED) {
                return false;
            }
            return true;
        }
        m_nfailed = 0;
        return m_lbfgs.getNIter() <= m_maxiter && !m_converged;
    }

    /**
     *
     * @param value
     */
    @Override
    public void setConvergenceCriterion(double value
    ) {
        m_eps = value;
    }

    /**
     *
     * @param value
     */
    public void setGTol(double value) {
        m_gtol = value;
    }

    /**
     *
     * @param val
     */
    @Override
    public void setMaxIter(int val) {
        m_maxiter = val;
    }

    /**
     *
     * @param value
     */
    public void setXTol(double value) {
        m_xtol = value;
    }

    /**
     * @return the m_m
     */
    public int getMemoryLength() {
        return m_m;
    }

    /**
     * @param m_m the m_m to set
     */
    public void setMemoryLength(int m) {
        m_m = m;
    }
}
