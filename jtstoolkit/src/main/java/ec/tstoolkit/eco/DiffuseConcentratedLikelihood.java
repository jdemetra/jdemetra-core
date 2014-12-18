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

import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.data.DataBlock;

/**
 *
 * @author Jean Palate
 */
public class DiffuseConcentratedLikelihood extends DiffuseLikelihood {

    private double[] m_b;
    private Matrix m_bvar;
    private int m_nx;

    /**
     *
     * @param idx
     * @param unbiased
     * @param hpcount
     * @return
     */
    public double bser(int idx, boolean unbiased, int hpcount) {
        if (unbiased) {
            double n = getN() - getD();
            double c = n / (n - m_nx - hpcount);
            return Math.sqrt(m_bvar.get(idx, idx) * c);
        }
        return Math.sqrt(m_bvar.get(idx, idx));
    }

    /**
     *
     * @param unbiased
     * @param hpcount
     * @return
     */
    public int getDegreesOfFreedom(boolean unbiased, int hpcount) {
        int n = getN();
        if (unbiased) {
            n -= m_nx + hpcount;
        }
        return n;
    }

    /**
     *
     * @return
     */
    public Matrix bvar() {
        return m_bvar.clone();
    }

    /**
     *
     * @param unbiased
     * @param hpcount
     * @return
     */
    public Matrix bvar(boolean unbiased, int hpcount) {
        if (unbiased) {
            double n = getN() - getD();
            double c = n / (n - m_nx - hpcount);
            return m_bvar.times(c);
        }
        return m_bvar.clone();
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
     * @return
     */
    public int getNx() {
        return m_nx;
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

    @Override
    public void rescale(double yfactor) {
        super.rescale(yfactor);
        if (m_b == null) {
            return;
        }
        for (int i = 0; i < m_b.length; ++i) {
            m_b[i] /= yfactor;
        }
        double yfactor2=yfactor*yfactor;
        m_bvar.mul(1/yfactor2);
    }
    /**
     *
     * @param b
     * @param bvar
     * @param nx
     */
    public void setB(double[] b, Matrix bvar, int nx) {
        m_b = b.clone();
        m_bvar = bvar.clone();
        m_nx = nx;
    }

    /**
     *
     * @return
     */
    public double[] tstats() {
        return tstats(false, 0);
    }

    /**
     *
     * @param unbiased
     * @param hpcount
     * @return
     */
    public double[] tstats(boolean unbiased, int hpcount) {
        double c = 1;
        if (unbiased) {
            double n = getN() - getD();
            c = n / (n - m_nx - hpcount);
        }

        DataBlock diag = m_bvar.diagonal();
        double[] t = new double[diag.getLength()];
        for (int i = 0; i < t.length; ++i) {
            t[i] = m_b[i] / Math.sqrt(c * diag.get(i));
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
    
    public ConcentratedLikelihood toConcentratedLikelihood(){
        ConcentratedLikelihood ll=new ConcentratedLikelihood();
        ll.set(this.getSsqErr(),  this.getLogDeterminant()+this.getDiffuseLogDeterminant(), this.getN()-this.getD() );
        ll.setB(m_b, m_bvar, m_nx);
        ll.setRes(this.getResiduals());
        return ll;
    }
}
