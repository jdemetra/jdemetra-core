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
package jdplus.ucarima;

import jdplus.arima.ArimaModel;
import jdplus.data.DataBlock;
import nbbrd.design.Development;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.math.polynomials.Polynomial;
import jdplus.math.linearsystem.LinearSystemSolver;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class RootDecomposer extends SimpleModelDecomposer {

    private IRootSelector m_selector;

    private BackFilter m_sur, m_nur;

    private BackFilter m_sar, m_nar;

    private SymmetricFilter m_sma, m_sfds, m_sfdn;

    /**
     *
     */
    public RootDecomposer() {
        m_selector = new AllSelector();
    }

    /**
     *
     * @param sel
     */
    public RootDecomposer(final IRootSelector sel) {
        m_selector = sel;
    }

    /**
     *
     */
    @Override
    protected void calc() {
        if (model.isNull()) {
            signal = ArimaModel.NULL;
            noise = ArimaModel.NULL;
            return;
        }

        if (m_selector == null) {
            signal = ArimaModel.NULL;
            noise = model;
            return;
        }

        splitRoots();
        m_sma = model.symmetricMa();
        m_sfds = SymmetricFilter.convolutionOf(m_sur.times(m_sar));
        m_sfdn = SymmetricFilter.convolutionOf(m_nur.times(m_nar));

        if (!checkSpecialCases()) {
            return;
        }

        double[] n = m_sma.coefficientsAsPolynomial().toArray();
        double[] ds = m_sfds.coefficientsAsPolynomial().toArray();
        double[] dn = m_sfdn == null ? Polynomial.ONE.toArray() : m_sfdn.coefficientsAsPolynomial().toArray();

        int q = n.length - 1;
        int p = ds.length + dn.length - 2;
        int ps = ds.length - 1, pn = dn.length - 1, qs = ps > 0 ? ps - 1 : 0, qn = pn > 0 ? pn - 1
                : 0;
        if (q >= p) // qn > 0 and pn > 0
        {
            qn = q - ps;
        }

        int xs = qs + 1, xn = qn + 1, x = xs + xn;

        FastMatrix m = FastMatrix.square(x);

        // modify the arrays to get the frequency response (and not the agf)
        n[0] /= 2;
        ds[0] /= 2;
        dn[0] /= 2;

        // cos j * cos k =0.5 * ( cos (j+k) + cos (j-k) )
        // j identifies de coeff of ns (nn) and k de coeff of dn (ds).
        // j is in [0, qs] ([0, qn]) and k in [0, pn] ([0, ps])
        // cos (j+k)
        for (int j = 0; j <= qs; ++j) {
            for (int k = 0; k <= pn; ++k) {
                m.set(j + k, j, dn[k]);
            }
        }
        for (int j = 0; j <= qn; ++j) {
            for (int k = 0; k <= ps; ++k) {
                m.set(j + k, j + xs, ds[k]);
            }
        }
        for (int j = 0; j <= qs; ++j) {
            for (int k = 0; k <= pn; ++k) {
                int i = j - k;
                if (i < 0) {
                    i = -i;
                }
                m.set(i, j, m.get(i, j) + dn[k]);
            }
        }
        for (int j = 0; j <= qn; ++j) {
            for (int k = 0; k <= ps; ++k) {
                int i = j - k;
                if (i < 0) {
                    i = -i;
                }
                m.set(i, j + xs, m.get(i, j + xs) + ds[k]);
            }
        }

        double[] sq = new double[x];
        for (int i = 0; i <= q; ++i) {
            sq[i] = n[i];
        }
        LinearSystemSolver.robustSolver().solve(m, DataBlock.of(sq));

        double[] rcs = new double[xs];
        double[] rcn = new double[xn];

        for (int i = 0; i < rcs.length; ++i) {
            rcs[i] = sq[i];
        }
        for (int i = 0; i < rcn.length; ++i) {
            rcn[i] = sq[xs + i];
        }

        rcs[0] *= 2;
        rcn[0] *= 2;
        SymmetricFilter sma = SymmetricFilter.ofInternal(rcs);
        SymmetricFilter nma = SymmetricFilter.ofInternal(rcn);

        signal = new ArimaModel(m_sar, m_sur, sma);
        noise = new ArimaModel(m_nar, m_nur, nma);
//        ArimaModel check = model.minus(noise.plus(signal));
    }

    private boolean checkSpecialCases() {
        // No selected roots:
        if (m_sfds.length() <= 1) {
            signal = new ArimaModel(BackFilter.ONE, m_sur, BackFilter.ONE, 0);
            noise = new ArimaModel(m_nar, m_nur, m_sma);
            return false;
        } else {
            return true;
        }

    }

    /**
     *
     */
    @Override
    protected void clear() {
        super.clear();
        m_sur = null;
        m_nur = null;
        m_sar = null;
        m_nar = null;
        m_sfds = null;
        m_sfdn = null;
        m_sma = null;
    }

    /**
     *
     * @return
     */
    public IRootSelector getSelector() {
        return m_selector;
    }

    /**
     *
     * @param value
     */
    public void setSelector(final IRootSelector value) {
        m_selector = value;
        clear();
    }

    private void splitRoots() {
        m_selector.select(model.getStationaryAr().asPolynomial());
        if (m_selector.getSelection() != null) {
            m_sar = new BackFilter(m_selector.getSelection());
        } else {
            m_sar = BackFilter.ONE;
        }
        if (m_selector.getOutofSelection() != null) {
            m_nar = new BackFilter(m_selector.getOutofSelection());
        } else {
            m_nar = BackFilter.ONE;
        }

        m_selector.selectUnitRoots(model.getNonStationaryAr().asPolynomial());
        if (m_selector.getSelection() != null) {
            m_sur = new BackFilter(m_selector.getSelection());
        } else {
            m_sur = BackFilter.ONE;
        }
        if (m_selector.getOutofSelection() != null) {
            m_nur = new BackFilter(m_selector.getOutofSelection());
        } else {
            m_nur = BackFilter.ONE;
        }
    }
}
