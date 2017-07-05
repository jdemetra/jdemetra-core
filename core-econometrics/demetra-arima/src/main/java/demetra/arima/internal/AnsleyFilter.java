/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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
package demetra.arima.internal;

import demetra.arima.IArimaModel;
import demetra.arima.estimation.IArmaFilter;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.Doubles;
import demetra.data.LogSign;
import demetra.design.AlgorithmImplementation;
import static demetra.design.AlgorithmImplementation.Feature.Fast;
import demetra.design.Development;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixException;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.RationalFunction;
import org.openide.util.lookup.ServiceProvider;


/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@AlgorithmImplementation(algorithm=IArmaFilter.class, feature=Fast)
@ServiceProvider(service=IArmaFilter.class)
public class AnsleyFilter implements IArmaFilter {

    private Matrix m_bL;
    private Polynomial m_ar, m_ma;
    private double m_var;
    private int m_n;
    protected boolean m_wnoptimize = true;

    /**
     *
     * @param y
     * @return
     */
    public double[] filter(Doubles y) {
        double[] e = new double[y.length()];
        y.copyTo(e, 0);
        int p = m_ar.getDegree();
        int q = m_ma.getDegree();
        if (m_wnoptimize && p == 0 && q == 0) {
            if (m_var != 1) {
                double std = Math.sqrt(m_var);
                for (int i = 0; i < e.length; ++i) {
                    e[i] /= std;
                }
            }
            return e;
        }
        if (p > 0) {
            for (int i = e.length - 1; i >= p; --i) {
                double s = 0;
                for (int j = 1; j <= p; ++j) {
                    s += m_ar.get(j) * e[i - j];
                }
                e[i] += s;
            }
        }

        rsolve(e);
        return e;
    }

    /**
     *
     * @param y
     * @param yf
     */
    @Override
    public void filter(Doubles y, DataBlock yf) {
        double[] e = filter(y);
        yf.copyFrom(e, 0);
    }

    @Override
    public double getLogDeterminant() {
        if (m_bL == null) {
            return m_n*Math.log(m_var);
        } else {
            DataBlock diag = m_bL.row(0);
            return 2 * LogSign.of(diag).value;
        }
    }

    @Override
    public int initialize(final IArimaModel arima, int n) {
        m_n = n;
        m_bL = null;
        m_ar = arima.getAR().asPolynomial();
        BackFilter ma = arima.getMA();
        m_var = arima.getInnovationVariance();
        m_ma = ma.asPolynomial();
        int p = m_ar.getDegree(), q = m_ma.getDegree();
        if (m_wnoptimize && p == 0 && q == 0) {
            return n;
        }
        int r = Math.max(p, q + 1);
        double[] cov = null, dcov = null;
        if (p > 0) {
            cov = arima.getAutoCovarianceFunction().values(r);
            double[] psi = RationalFunction.of(m_ma, m_ar).coefficients(q);
            dcov = new double[r];
            for (int i = 1; i <= q; ++i) {
                double v = m_ma.get(i);
                for (int j = i + 1; j <= q; ++j) {
                    v += m_ma.get(j) * psi[j - i];
                }
                dcov[i] = v * m_var;
            }
        }

        Polynomial sma = SymmetricFilter.fromFilter(ma).asPolynomial();
        if (m_var != 1) {
            sma = sma.times(m_var);
        }

        m_bL = Matrix.make(r, n);
        // complete the matrix
        // if (i >= j) m(i, j) = lband[i-j, j]; if i-j >= r, m(i, j) =0
        // if (i < j) m(i, j) = lband(j-i, i)

        DataBlockIterator cols = m_bL.columnsIterator();
        for (int j = 0; j < p; ++j) {
            DataBlock col = cols.next();
            for (int i = 0; i < p - j; ++i) {
                col.set(i, cov[i]);
            }
            for (int i = p - j; i < r; ++i) {
                col.set(i, dcov[i]);
            }
        }

        Matrix M = m_bL.extract(0, q + 1, p, n);
        DataBlockIterator rows = M.rowsIterator();

        int pos=0;
        while (rows.hasNext()) {
            double s=sma.get(pos++);
            if ( s!= 0) {
                rows.next().set(s);
            }
        } 

        lcholesky();
        return n;
    }

    private void lcholesky() {
        int r = m_bL.getRowsCount();
        int n = m_bL.getColumnsCount();
        double[] data = m_bL.getStorage();
        if (r == 1) {
            for (int i = 0; i < data.length; ++i) {
                if (data[i] <= 0) {
                    throw new MatrixException(MatrixException.CHOLESKY);
                }
                data[i] = Math.sqrt(data[i]);
            }
        } else {
            // The diagonal item is the first row !
            int dr = r - 1, drr = dr * dr;
            for (int i = 0, idiag = 0; i < n; ++i, idiag += r) {
                // compute aii;
                double aii = data[idiag];
                int rmin = idiag - drr;
                if (rmin < 0) {
                    rmin = 0;
                }
                int rcur = idiag - dr;
                while (rcur >= rmin) {
                    double x = data[rcur];
                    if (x != 0) {
                        aii -= x * x;
                    }
                    rcur -= dr;
                }
                if (aii <= 0) {
                    throw new MatrixException(MatrixException.CHOLESKY);
                }
                aii = Math.sqrt(aii);
                data[idiag] = aii;

                // compute elements i+1 : n of column i
                rcur = idiag - dr;
                int k = i + r - 1;
                while (rcur >= rmin) {
                    double x = data[rcur];
                    if (x != 0) {
                        int q = Math.min(k, n) - i;
                        for (int iy = idiag + 1, ia = rcur + 1; ia < rcur + q; ++ia, ++iy) {
                            data[iy] -= x * data[ia];
                        }
                    }
                    rcur -= dr;
                    --k;
                }
                int ymax = r * (i + 1);
                for (int iy = idiag + 1; iy < ymax; ++iy) {
                    data[iy] /= aii;
                }
            }
        }
    }

    // / <summary>
    // / Lx=b or L^-1 * b = x
    // / </summary>
    // / <param name="b"> On entry: b, on exit: x</param>
    /**
     *
     * @param b
     */
    private void rsolve(double[] b) {
        int n = m_bL.getColumnsCount();
        int r = m_bL.getRowsCount();

        double[] data = m_bL.getStorage();

        int nb = b.length;

        int i = 0;
        while (i < nb && b[i] == 0) {
            ++i;
        }
        for (int idx = i * r; i < nb; ++i, idx += r) {
            double t = b[i] / data[idx];

            int jmax = Math.min(r, n - i);
            for (int j = 1, k = idx + 1; j < jmax; ++j, ++k) {
                b[i + j] -= t * data[k];
            }
            b[i] = t;
        }
    }

    public Matrix getCholeskyFactor() {
        if (m_bL == null) {
            Matrix l = Matrix.make(1, m_n);
            l.set(Math.sqrt(m_var));
            return l;
        } else {
            return m_bL;
        }
    }

    /**
     * @return the m_wnoptimize
     */
    public boolean isOptimizedForWhiteNoise() {
        return m_wnoptimize;
    }

    /**
     * @param m_wnoptimize the m_wnoptimize to set
     */
    public void setOptimizedForWhiteNoise(boolean m_wnoptimize) {
        this.m_wnoptimize = m_wnoptimize;
    }
}
