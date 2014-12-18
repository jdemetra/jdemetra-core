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
package ec.tstoolkit.ssf.ucarima;

// NEW IMPLEMENTATION: JEAN (12/10/07)
import ec.tstoolkit.arima.AutoCovarianceFunction;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.RationalFunction;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.ucarima.UcarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class SsfUcarimaWithMean implements ISsf
{
    /**
     * 
     * @param ucm
     * @return
     */
    public static ISsf build(UcarimaModel ucm)
    {
        return new SsfUcarimaWithMean(ucm);
    }
    // trend component
    private UcarimaModel m_ucm;
    private int m_tr;
    private double m_tvar;
    private double[] m_tdif, m_tphi, m_tacgf, m_tpsi; // tphi, tacgf, tpsi are
    // related to the
    // stationary part.
    private DataBlock m_tPhi;
    // other components
    private int m_ncmps, m_dim;
    private double[][] m_dif, m_phi, m_psi, m_stacgf, m_stpsi;
    private double[] m_var;
    private int[] m_r;
    private DataBlock[] m_Phi;
    private DataBlock m_ctmp;

    /**
     * 
     */
    public SsfUcarimaWithMean()
    {
    }

    /**
     * 
     * @param ucm
     */
    public SsfUcarimaWithMean(UcarimaModel ucm)
    {
        m_ucm = ucm;
        initModel();
    }

    /**
     * 
     * @param cmp
     * @return
     */
    public int cmpDim(int cmp)
    {
        if (cmp >= m_ucm.getComponentsCount()
                || m_ucm.getComponent(cmp).isNull())
            return 0;
        if (cmp == 0)
            return m_tr;
        int j = 0;
        for (int i = 1; i < cmp; ++i)
            if (!m_ucm.getComponent(i).isNull())
                ++j;
        return m_r[j];
    }

    /**
     * 
     * @param cmp
     * @return
     */
    public int cmpPos(int cmp)
    {
        if (cmp >= m_ucm.getComponentsCount()
                || m_ucm.getComponent(cmp).isNull())
            return -1;
        if (cmp == 0)
            return 0;
        int pos = m_tr;
        for (int i = 1, j = 0; i < cmp; ++i)
            if (!m_ucm.getComponent(i).isNull())
                pos += m_r[j++];
        return pos;
    }

    /**
     * 
     * @param b
     */
    public void diffuseConstraints(SubMatrix b)
    {
        b.diagonal().range(0, m_tdif.length).set(1);
        for (int c = 0, s = m_tr, t = m_tdif.length; c < m_ncmps; ++c)
        {
            int dim = m_r[c], d = m_dif[c].length - 1;
            if (d > 0)
            {
                SsfArima.B0(b.extract(s, s + dim, t, t + d), m_dif[c]);
                t += d;
            }
            s += dim;
        }
    }

    private int dtot()
    {
        int nd = m_tdif.length;
        for (int i = 0; i < m_ncmps; ++i)
            nd += m_dif[i].length - 1;
        return nd;
    }

    /**
     * 
     * @param pos
     * @param qm
     */
    public void fullQ(int pos, SubMatrix qm)
    {
        if (m_tvar != 0)
        {
            int dim = m_tpsi.length;
            int s = m_tdif.length;
            for (int i = 0; i < dim; ++i)
                for (int j = 0; j <= i; ++j)
                    qm.set(s + i, s + j, m_tpsi[i] * m_tpsi[j] * m_tvar);

        }
        for (int cmp = 0, s = m_tr; cmp < m_ncmps; ++cmp)
        {
            int dim = m_r[cmp];
            for (int i = 0; i < dim; ++i)
                for (int j = 0; j <= i; ++j)
                    qm.set(s + i, s + j, m_psi[cmp][i] * m_psi[cmp][j]
                            * m_var[cmp]);
            s += dim;
        }
        SymmetricMatrix.fromLower(qm);
    }

    /**
     * 
     * @return
     */
    public int getNonStationaryDim()
    {
        return dtot();
    }

    /**
     * 
     * @return
     */
    public int getStateDim()
    {
        return m_dim;
    }

    /**
     * 
     * @return
     */
    public int getTransitionResCount()
    {
        return m_dim - m_tdif.length;
    }

    /**
     * 
     * @return
     */
    public int getTransitionResDim()
    {
        return m_ncmps + 1;
    }

    /**
     * 
     * @return
     */
    public UcarimaModel getUCModel()
    {
        return m_ucm;
    }

    /**
     * 
     * @param pos
     * @return
     */
    public double H(int pos)
    {
        return 0;
    }

    /**
     * 
     * @return
     */
    public boolean hasH()
    {
        return false;
    }

    /**
     * 
     * @return
     */
    public boolean hasR()
    {
        return true;
    }

    /**
     * 
     * @param pos
     * @return
     */
    public boolean hasTransitionRes(int pos)
    {
        return true;
    }

    /**
     * 
     * @return
     */
    public boolean hasW()
    {
        return true;
    }

    private void initModel()
    {
        m_ncmps = 0;
        m_dim = 0;
        initTModel();
        for (int i = 1; i < m_ucm.getComponentsCount(); ++i)
            if (!m_ucm.getComponent(i).isNull())
                ++m_ncmps;
        m_dif = new double[m_ncmps][];
        m_phi = new double[m_ncmps][];
        m_Phi = new DataBlock[m_ncmps];
        m_psi = new double[m_ncmps][];
        m_r = new int[m_ncmps];
        m_stacgf = new double[m_ncmps][];
        m_stpsi = new double[m_ncmps][];
        m_var = new double[m_ncmps];
        for (int i = 1, j = 0; i < m_ucm.getComponentsCount(); ++i)
            if (!m_ucm.getComponent(i).isNull())
                initModel(j++, i);
        //
        m_ctmp = new DataBlock(m_dim);
    }

    private void initModel(int i, int cmp)
    {
        ArimaModel model = m_ucm.getComponent(cmp);
        m_var[i] = model.getInnovationVariance();
        BackFilter ur = model.getNonStationaryAR();
        m_dif[i] = ur.getCoefficients();
        Polynomial phi = model.getAR().getPolynomial();
        m_phi[i] = phi.getCoefficients();
        m_Phi[i] = new DataBlock(m_phi[i], 1, m_phi[i].length, 1);
        Polynomial theta = model.getMA().getPolynomial();
        m_r[i] = Math.max(phi.getDegree(), theta.getDegree() + 1);
        m_dim += m_r[i];
        m_psi[i] = new RationalFunction(theta, phi).coefficients(m_r[i]);

        Polynomial stphi = model.getStationaryAR().getPolynomial();
        m_stacgf[i] = new AutoCovarianceFunction(theta, stphi, m_var[i]).values(m_r[i]);
        m_stpsi[i] = new RationalFunction(theta, stphi).coefficients(m_r[i]);
    }

    private void initTModel()
    {
        IArimaModel model = m_ucm.getComponent(0);
        m_tvar = model.getInnovationVariance();
        BackFilter ur = model.getNonStationaryAR();
        m_tdif = ur.getCoefficients();
        if (m_tvar != 0)
        {
            Polynomial tphi = model.getStationaryAR().getPolynomial();
            m_tphi = tphi.getCoefficients();
            m_tPhi = new DataBlock(m_tphi, 1, m_tphi.length, 1);
            Polynomial theta = model.getMA().getPolynomial();
            int r = Math.max(tphi.getDegree(), theta.getDegree() + 1);
            m_tr = r + m_tdif.length;
            m_dim += m_tr;
            m_tacgf = new AutoCovarianceFunction(theta, tphi, m_tvar).values(r);
            m_tpsi = new RationalFunction(theta, tphi).coefficients(r);
        }
        else
        {
            m_tr = m_tdif.length;
            m_dim += m_tr;
            // other parts are null;
        }
    }

    /**
     * 
     * @return
     */
    public boolean isDiffuse()
    {
        return true;
    }

    /**
     * 
     * @return
     */
    public boolean isMeasurementEquationTimeInvariant()
    {
        return true;
    }

    /**
     * 
     * @return
     */
    public boolean isTimeInvariant()
    {
        return true;
    }

    /**
     * 
     * @return
     */
    public boolean isTransitionEquationTimeInvariant()
    {
        return true;
    }

    /**
     * 
     * @return
     */
    public boolean isTransitionResidualTimeInvariant()
    {
        return true;
    }

    /**
     * 
     * @return
     */
    public boolean isValid()
    {
        return m_ucm != null;
    }

    /**
     * 
     * @param pos
     * @param k
     * @param lm
     */
    public void L(int pos, DataBlock k, SubMatrix lm)
    {
        T(pos, lm);
        lm.column(0).sub(k);
        int ndif = m_tdif.length;
        for (int j = 1; j < ndif; ++j)
            lm.column(j).addAY(m_tdif[j], k);
        if (m_tvar != 0)
            lm.column(ndif).sub(k);

        for (int cmp = 0, c = m_tr; cmp < m_ncmps; ++cmp)
        {
            lm.column(c).sub(k);
            c += m_r[cmp];
        }
    }

    /**
     * 
     * @param pf0
     */
    public void Pf0(SubMatrix pf0)
    {
        // trend part
        if (m_tvar != 0)
        {
            int dim = m_tpsi.length;
            int s = m_tdif.length;
            for (int j = 0; j < dim; ++j)
                pf0.set(s + j, s, m_tacgf[j]);
            for (int j = 0; j < dim - 1; ++j)
            {
                pf0.set(s + j + 1, s + j + 1, pf0.get(s + j, s + j) - m_tpsi[j]
                        * m_tpsi[j] * m_tvar);
                for (int k = 0; k < j; ++k)
                    pf0.set(s + j + 1, s + k + 1, pf0.get(s + j, s + k)
                            - m_tpsi[j] * m_tpsi[k] * m_tvar);
            }
            SymmetricMatrix.fromLower(pf0.extract(s, s + dim, s, s + dim));

        }

        // normal part
        for (int c = 0, s = m_tr; c < m_ncmps; ++c)
        {
            int dim = m_r[c];
            Matrix stV = new Matrix(dim, dim);
            SsfArima.stVar(stV.subMatrix(), m_stpsi[c], m_stacgf[c], m_var[c]);
            Matrix K = new Matrix(dim, dim);
            SsfArima.Ksi(K.subMatrix(), m_dif[c]);
            SymmetricMatrix.quadraticFormT(stV.subMatrix(), K.subMatrix(), pf0.extract(s, s + dim, s, s + dim));
            s += dim;
        }
    }

    /**
     * 
     * @param pi0
     */
    public void Pi0(SubMatrix pi0)
    {
        // overkill
        Matrix B = new Matrix(m_dim, dtot());
        diffuseConstraints(B.subMatrix());
        SymmetricMatrix.XXt(B.subMatrix(), pi0);
    }

    /**
     * 
     * @param pos
     * @param qm
     */
    public void Q(int pos, SubMatrix qm)
    {
        if (m_tvar != 0)
        {
            qm.set(0, 0, m_tvar);
            qm.diagonal().drop(1, 0).copyFrom(m_var, 0);
        }
        else
            qm.diagonal().copyFrom(m_var, 0);
    }

    /**
     * 
     * @param pos
     * @param rv
     */
    public void R(int pos, SubArrayOfInt rv)
    {
        int start = m_tdif.length;
        int n = getTransitionResCount();
        for (int i = 0; i < n; ++i)
            rv.set(i, start + i);
    }

    /**
     * 
     * @param pos
     * @param tr
     */
    public void T(int pos, SubMatrix tr)
    {
        tr.set(0, 0, 1);
        tr.set(1, 0, 1);
        int ndif = m_tdif.length;
        for (int j = 1; j < ndif; ++j)
            tr.set(1, j, -m_tdif[j]);

        for (int j = 2; j < ndif; ++j)
            tr.set(j, j - 1, 1);
        if (m_tvar != 0)
        {
            tr.set(1, ndif, 1);
            int n = m_tr;
            for (int i = ndif + 1; i < n; ++i)
                tr.set(i - 1, i, 1);
            for (int i = 1; i < m_tphi.length; ++i)
                tr.set(n - 1, n - i, -m_tphi[i]);
        }

        // normal part
        for (int cmp = 0, s = m_tr; cmp < m_ncmps; ++cmp)
        {
            int n = m_r[cmp], sn = s + n;
            for (int i = 1; i < n; ++i)
                tr.set(s + i - 1, s + i, 1);
            double[] phi = m_phi[cmp];
            for (int i = 1; i < phi.length; ++i)
                tr.set(sn - 1, sn - i, -phi[i]);
            s += n;
        }
    }

    /**
     * 
     * @param pos
     * @param vm
     */
    public void TVT(int pos, SubMatrix vm)
    {
        DataBlockIterator cols = vm.columns();
        DataBlock col = cols.getData();
        do
            TX(pos, col);
        while (cols.next());

        DataBlockIterator rows = vm.rows();
        DataBlock row = rows.getData();
        do
            TX(pos, row);
        while (rows.next());
    }

    /**
     * 
     * @param pos
     * @param x
     */
    public void TX(int pos, DataBlock x)
    {
        int ndif = m_tdif.length;
        // x0 unchanged
        // x1 through Zx(partial). Save the result
        double x1 = x.get(0);
        for (int j = 1; j < ndif; ++j)
            x1 -= x.get(j) * m_tdif[j];
        if (m_tvar != 0)
            x1 += x.get(ndif);

        if (ndif > 2)
            x.range(1, ndif).fshift(DataBlock.ShiftOption.Zero);
        x.set(1, x1);

        //
        if (m_tvar != 0)
        {
            int n = m_tpsi.length, s = ndif;
            DataBlock xc = x.range(s, s + n);
            double last = m_tPhi.dotReverse(xc);
            xc.bshift(DataBlock.ShiftOption.None);
            xc.set(n - 1, -last);
        }

        // normal part

        for (int c = 0, s = m_tr; c < m_ncmps; ++c)
        {
            int n = m_r[c];
            DataBlock xc = x.range(s, s + n);
            double last = m_Phi[c].dotReverse(xc);
            xc.bshift(DataBlock.ShiftOption.None);
            xc.set(n - 1, -last);
            s += n;
        }
    }

    /**
     * 
     * @param pos
     * @param vm
     * @param d
     */
    public void VpZdZ(int pos, SubMatrix vm, double d)
    {
        // trend part
        int n = m_tdif.length;
        for (int r = 0; r < n; ++r)
        {
            double xr = 1;
            if (r > 0)
                xr = -m_tdif[r];
            if (xr == 0)
                continue;
            xr *= d;
            for (int c = 0; c <= r; ++c)
            {
                double xc = 1;
                if (c > 0)
                    xc = -m_tdif[c];
                if (xc == 0)
                    continue;
                double x = xr * xc;
                vm.add(r, c, x);
                if (r != c)
                    vm.add(c, r, x);
            }
            if (m_tvar != 0)
            {
                vm.add(r, n, xr);
                vm.add(n, r, xr);
            }
        }
        if (m_tvar != 0)
            vm.add(n, n, d);

        // cross part
        for (int ccmp = 0, c = m_tr; ccmp < m_ncmps; ++ccmp)
        {
            for (int r = 0; r < n; ++r)
            {
                double xr = 1;
                if (r > 0)
                    xr = -m_tdif[r];
                if (xr != 0)
                {
                    xr *= d;
                    vm.add(r, c, xr);
                    vm.add(c, r, xr);
                }
            }
            if (m_tvar != 0)
            {
                vm.add(n, c, d);
                vm.add(c, n, d);
            }
            c += m_r[ccmp];
        }

        // normal part
        for (int rcmp = 0, r = m_tr; rcmp < m_ncmps; ++rcmp)
        {
            for (int ccmp = 0, c = m_tr; ccmp <= rcmp; ++ccmp)
            {
                vm.add(r, c, d);
                if (r != c)
                    vm.add(c, r, d);
                c += m_r[ccmp];
            }
            r += m_r[rcmp];
        }
    }

    /**
     * 
     * @param pos
     * @param wv
     */
    public void W(int pos, SubMatrix wv)
    {
        int istart = 0, jstart = 0;
        if (m_tvar != 0)
        {
            int n = m_tpsi.length;
            wv.column(0).range(0, n).copyFrom(m_tpsi, 0);

            istart = 1;
            jstart = n;
        }
        for (int i = 0, j = jstart; i < m_ncmps; ++i)
        {
            int n = m_r[i];
            wv.column(i + istart).range(j, j + n).copyFrom(m_psi[i], 0);
            j += n;
        }
    }

    /**
     * 
     * @param pos
     * @param x
     * @param d
     */
    public void XpZd(int pos, DataBlock x, double d)
    {
        x.add(0, d);
        int ndif = m_tdif.length;
        for (int j = 1; j < ndif; ++j)
            x.add(j, -d * m_tdif[j]);
        if (m_tvar != 0)
            x.add(ndif, d);
        for (int cmp = 0, s = m_tr; cmp < m_ncmps; ++cmp)
        {
            x.add(s, d);
            s += m_r[cmp];
        }
    }

    /**
     * 
     * @param pos
     * @param x
     */
    public void XT(int pos, DataBlock x)
    {
        double x1 = x.get(1);
        // constant
        x.add(0, x1);
        int ndif = m_tdif.length;
        for (int i = 1; i < ndif - 1; ++i)
            x.set(i, x.get(i + 1) - m_tdif[i] * x1);
        x.set(ndif - 1, -m_tdif[ndif - 1] * x1);
        if (m_tvar != 0)
        {
            int n = m_tpsi.length;
            DataBlock xc = x.range(ndif, ndif + n);
            double last = -xc.get(n - 1);
            xc.fshift(DataBlock.ShiftOption.None);
            xc.set(0, 0);
            if (last != 0)
                for (int i = 1; i < m_tphi.length; ++i)
                    if (m_tphi[i] != 0)
                        xc.add(n - i, last * m_tphi[i]);
            xc.add(0, x1);
        }

        for (int c = 0, s = m_tr; c < m_ncmps; ++c)
        {
            int n = m_r[c];
            DataBlock xc = x.range(s, s + n);
            double last = -xc.get(n - 1);
            xc.fshift(DataBlock.ShiftOption.None);
            xc.set(0, 0);
            if (last != 0)
                for (int i = 1; i < m_phi[c].length; ++i)
                    if (m_phi[c][i] != 0)
                        xc.add(n - i, last * m_phi[c][i]);
            s += n;
        }
    }

    /**
     * 
     * @param pos
     * @param x
     */
    public void Z(int pos, DataBlock x)
    {
        x.set(0, 1);
        int ndif = m_tdif.length;
        for (int j = 1; j < ndif; ++j)
            x.set(j, -m_tdif[j]);
        if (m_tvar != 0)
            x.set(ndif, 1);
        for (int cmp = 0, s = m_tr; cmp < m_ncmps; ++cmp)
        {
            x.set(s, 1);
            s += m_r[cmp];
        }
    }

    /**
     * 
     * @param pos
     * @param m
     * @param x
     */
    public void ZM(int pos, SubMatrix m, DataBlock x)
    {
        x.copy(m.row(0));
        int ndif = m_tdif.length;
        for (int j = 1; j < ndif; ++j)
            x.addAY(-m_tdif[j], m.row(j));
        if (m_tvar != 0)
            x.add(m.row(ndif));
        for (int i = 0, c = m_tr; i < m_ncmps; ++i)
        {
            x.add(m.row(c));
            c += m_r[i];
        }
    }

    /**
     * 
     * @param pos
     * @param vm
     * @return
     */
    public double ZVZ(int pos, SubMatrix vm)
    {
        ZM(pos, vm, m_ctmp);
        return ZX(pos, m_ctmp);
    }

    /**
     * 
     * @param pos
     * @param x
     * @return
     */
    public double ZX(int pos, DataBlock x)
    {
        double r = x.get(0);
        int ndif = m_tdif.length;
        for (int j = 1; j < ndif; ++j)
            r -= x.get(j) * m_tdif[j];
        if (m_tvar != 0)
            r += x.get(ndif);
        for (int i = 0, c = m_tr; i < m_ncmps; ++i)
        {
            r += x.get(c);
            c += m_r[i];
        }
        return r;
    }

    /*
     * private SsfUCArimawithMean () { }
     * 
     * public static ISsf build(final UCModel ucm) { int dim = 1; int nstdim =
     * 1; int rescount = 0; int resdim = 0;
     * 
     * // Trend component BFilter urt = new BFilter(0); IArimaModel trend =
     * ucm.getComponent(0), sttrend = null; if (trend.getInnovationVariance() >
     * 0) { sttrend = trend.doStationary(urt); int d = urt.getLength() - 1;
     * nstdim += d; int stdim = Math .max(sttrend.getARCount(),
     * sttrend.getMACount() + 1); dim += d + stdim; ++resdim; rescount += stdim;
     * }
     * 
     * //
     * 
     * SsfArima[] cmpssf = new SsfArima[ucm.getComponentsCount() - 1]; for (int
     * i = 0; i < cmpssf.length; ++i) { IArimaModel arima = ucm.getComponent(i +
     * 1); if (arima.getInnovationVariance() > 0) { cmpssf[i] = new
     * SsfArima(arima); dim += cmpssf[i].getStateDim(); nstdim +=
     * cmpssf[i].getNonStationaryDim(); rescount += cmpssf[i].getStateDim();
     * ++resdim; } }
     * 
     * DefaultTimeInvariantSsf ssf = new DefaultTimeInvariantSsf(); Matrix T =
     * new Matrix(dim, dim); Matrix Pf0 = new Matrix(dim, dim); Matrix Q = new
     * Matrix(resdim, resdim); Matrix B = new Matrix(dim, nstdim); Matrix W =
     * new Matrix(rescount, resdim); double[] Z = new double[dim]; int[] R = new
     * int[rescount];
     * 
     * // Initializing the matrices // Trend T.set(0, 0, 1); B.set(0, 0, 1);
     * Z[0] = 1;
     * 
     * int idx = 1, didx = 1, ires = 0, iq = 0; // int idx = 0, didx=0; if
     * (sttrend != null) { double v = sttrend.getInnovationVariance(); for (int
     * j = 0; j < urt.getLength() - 1; ++j) B.set(idx + j, didx + j, 1);
     * double[] c = urt.getWeights(); int dcur = c.length - 1; for (int j = 0; j
     * < dcur; ++j) { Z[idx + j] = -c[dcur - j - 1]; T.set(idx, idx + j, -c[dcur
     * - j - 1]); if (j != dcur - 1) T.set(idx + j + 1, idx + j, 1); } if
     * (c.length > 1) { T.set(1, 0, 1); T.set(1, 1 + dcur, 1); } idx += dcur;
     * didx += dcur; Z[idx] = 1;
     * 
     * // initialisation of the stationary components double[] phi =
     * sttrend.getAR().getCoefficients(), theta = sttrend
     * .getMA().getCoefficients(); int r = Math.max(phi.length - 1,
     * theta.length); double[] psi = RationalFunction.promote(theta,
     * phi).coefficients(r); double[] acgf = ACGF.promote(theta, phi,
     * v).coefficients(r); // T for (int j = 1; j < r; ++j) T.set(idx + j - 1,
     * idx + j, 1); for (int j = 1; j < phi.length; ++j) T.set(idx + r - 1, idx
     * + r - j, -phi[j]); // Pf0 for (int j = 0; j < r; ++j) Pf0.set(idx + j,
     * idx, acgf[j]);
     * 
     * for (int j = 0; j < r - 1; ++j) { Pf0.set(idx + j + 1, idx + j + 1,
     * Pf0.get(idx + j, idx + j) - psi[j] * psi[j] * v); for (int k = 0; k < j;
     * ++k) Pf0.set(idx + j + 1, idx + k + 1, Pf0.get(idx + j, idx + k) - psi[j]
     * * psi[k] * v); } SymmetricMatrix .fromLower(Pf0.subMatrix(idx, idx + r,
     * idx, idx + r)); // W for (int j = 0; j < r; ++j) W.set(j, 0, psi[j]); //
     * Q Q.set(0, 0, v); // R for (int j = 0; j < r; ++j) R[j] = idx + j; ires =
     * r; idx += r; ++iq; }
     * 
     * for (int icmp = 0; icmp < cmpssf.length; ++icmp) { ISsf curssf =
     * cmpssf[icmp]; if (curssf != null) { int r = curssf.getStateDim(); int d =
     * curssf.getNonStationaryDim(); curssf.T(0, T.subMatrix(idx, idx + r, idx,
     * idx + r)); curssf.Z(0, DataBlock.create(Z, idx, idx + r, 1)); curssf.Q(0,
     * Q.subMatrix(iq, iq + 1, iq, iq + 1)); curssf.W(0, W.subMatrix(ires, ires
     * + r, iq, iq + 1)); curssf.Pf0(Pf0.subMatrix(idx, idx + r, idx, idx + r));
     * curssf.diffuseConstraints(B.subMatrix(idx, idx + r, didx, didx + d)); for
     * (int j = 0; j < r; ++j) R[ires + j] = idx + j; ires += r; idx += r; didx
     * += d; ++iq; } }
     * 
     * ssf.initialize(dim, rescount, resdim); ssf.setT(T); ssf.setZ(Z);
     * ssf.setQ(Q); ssf.setW(W); ssf.setR(R); ssf.setPf0(Pf0); ssf.setB0(B);
     * return ssf; }
     */
}
