/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.tstoolkit.eco;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;

/**
 * This class represents the concentrated likelihood of a linear regression
 * model.
 *
 *
 * @author Jean Palate
 */
public class ConcentratedLikelihood extends Likelihood {

    private double[] m_b;
    private Matrix m_bvar;
    private int m_nx;

    /**
     * Creates a new instance of ConcentredLikelihood
     */
    public ConcentratedLikelihood() {
    }

    /**
     *
     * @return
     */
    public double[] getB() {
        return m_b;
    }

    /**
     *
     * @param idx
     * @param unbiased
     * @param hpcount
     * @return
     */
    public double getBSer(int idx, boolean unbiased, int hpcount) {
        if (unbiased) {
            double n = getN();
            double c = n / (n - m_nx - hpcount);
            return Math.sqrt(m_bvar.get(idx, idx) * c);
        }
        return Math.sqrt(m_bvar.get(idx, idx));
    }

    /**
     *
     * @param idx
     * @param unbiased
     * @param hpcount
     * @return
     */
    public double[] getBSer(boolean unbiased, int hpcount) {
        if (m_b == null) {
            return null;
        }
        double[] se = new double[m_b.length];
        double c = 1;
        if (unbiased) {
            double n = getN();
            c = n / (n - m_nx - hpcount);
        }

        for (int i = 0; i < se.length; ++i) {
            se[i] = Math.sqrt(m_bvar.get(i, i) * c);
        }
        return se;
    }

    /**
     *
     * @return
     */
    public Matrix getBVar() {
        return m_bvar;
    }

    /**
     * Returns the variance/covariance matrix V of the coefficients of the
     * regression variables. V = sig2 * (X'X)^-1 sig2 may be computed as 1.
     * ssqErr/n (ml estimate) or as 2. ssqErr/(n-nx-nhp) (unbiased estimate)
     *
     * n is the number of obs. nx is the number of regression variables nhp is
     * the number of hyper-parameters
     *
     * ML estimate will always lead to smaller (co)variances.
     *
     * @param unbiased False if the ML estimate is used. True otherwise. See the
     * description for more information.
     * @param hpcount The number of hyper-parameters. Can be 0.
     * @return The covariance matrix. The matrix should not be modified.
     */
    public Matrix getBVar(boolean unbiased, int hpcount) {
        if (unbiased && m_bvar != null) {
            double n = getN();
            double c = n / (n - m_nx - hpcount);
            return m_bvar.times(c);
        }
        return m_bvar;
    }

    /**
     * Returns the number of degrees of freedom used in the computation of the
     * different variance/standard deviations
     *
     * @param unbiased True if ML estimates are used, false otherwise.
     * @param hpcount Number of hyper-paraneters that should be taken into
     * account. hpcount is not considered if unbiased is set to false.
     * @return
     */
    public int getDegreesOfFreedom(boolean unbiased, int hpcount) {
        int n = getN();
        if (unbiased) {
            n -= m_nx + hpcount;
            if (n <= 0) {
                n += hpcount;
            }
        }
        return n;
    }

    /**
     * Number of regression variables
     *
     * @return
     */
    public int getNx() {
        return m_nx;
    }

    /**
     *
     * @return
     */
    public double[] getTStats() {
        return getTStats(false, 0);
    }

    /**
     *
     * @param unbiased
     * @param hpcount
     * @return
     */
    public double[] getTStats(boolean unbiased, int hpcount) {
        if (m_bvar == null) {
            return null;
        }
        double c = 1;
        if (unbiased) {
            double n = getN();
            c = n / (n - m_nx - hpcount);
        }

        DataBlock diag = m_bvar.diagonal();
        double[] t = new double[diag.getLength()];
        for (int i = 0; i < t.length; ++i) {
            if (m_b[i] == 0) {
                t[i] = 0;
            } else {
                t[i] = m_b[i] / Math.sqrt(c * diag.get(i));
            }
        }
        return t;
    }

    public double getTStat(int idx, boolean unbiased, int hpcount) {
        double e = m_bvar.get(idx, idx);
        if (e == 0) {
            return Double.NaN;
        }
        double b = m_b[idx];
        if (b == 0) {
            return 0;
        }
        if (unbiased) {
            double n = getN();
            return b / Math.sqrt(e * n / (n - m_nx - hpcount));
        } else {
            return b / Math.sqrt(e);
        }
    }
    // / <summary>
    // / Adjust the likelihood if the data (y and/or Xs) have been
    // pre-multiplied by a given scaling factor
    // / </summary>
    // / <param name="yfactor"></param>
    // / <param name="xfactor"></param>

    /**
     *
     * @param yfactor
     * @param xfactor
     */
    public void rescale(double yfactor, double[] xfactor) {
        super.rescale(yfactor);
        if (m_b == null || xfactor == null) {
            return;
        }
        for (int i = 0; i < m_b.length; ++i) {
            double ifactor = xfactor[i] / yfactor;
            m_b[i] *= ifactor;
            for (int j = 0; j < i; ++j) {
                double ijfactor = ifactor * xfactor[j] / yfactor;
                m_bvar.mul(i, j, ijfactor);
                m_bvar.mul(j, i, ijfactor);
            }
            m_bvar.mul(i, i, ifactor * ifactor);
        }
    }

    /**
     *
     * @param b
     * @param bvar
     * @param nx
     */
    public void setB(double[] b, Matrix bvar, int nx) {
        if (b != null) {
            m_b = b.clone();
            m_bvar = bvar.clone();
        } else {
            m_b = null;
            m_bvar = null;
        }
        m_nx = nx;
    }
}
