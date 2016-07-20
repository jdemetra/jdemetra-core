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

package ec.tstoolkit.arima.estimation;

import ec.tstoolkit.arima.*;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.polynomials.RationalFunction;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.maths.matrices.MatrixException;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.polynomials.Polynomial;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class AnsleyFilter implements IArmaFilter {

    private Matrix m_bL;
    private Polynomial m_ar, m_ma;
    
    @Override
    public AnsleyFilter  exemplar(){
        return new AnsleyFilter();
    }

    /**
     *
     * @param y
     * @return
     */
    public double[] filter(IReadDataBlock y) {
        double[] e = new double[y.getLength()];
        y.copyTo(e, 0);
        int p = m_ar.getDegree();
        for (int i = e.length - 1; i >= p; --i) {
            double s = 0;
            for (int j = 1; j <= p; ++j) {
                s += m_ar.get(j) * e[i - j];
            }
            e[i] += s;
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
    public void filter(IReadDataBlock y, DataBlock yf) {
        double[] e = filter(y);
        yf.copy(new DataBlock(e));
    }

    @Override
    public double getLogDeterminant() {
        DataBlock diag = m_bL.row(0);
        return 2 * diag.sumLog().value;
    }

    @Override
    public int initialize(final IArimaModel arima, int n) {
        m_ar = arima.getAR().getPolynomial();
        BackFilter ma = arima.getMA();
        double var = arima.getInnovationVariance();
        m_ma = ma.getPolynomial();
        int p = m_ar.getDegree(), q = m_ma.getDegree();
        int r = Math.max(p, q + 1);
        double[] cov = null, dcov = null;
        if (p > 0) {
            cov = arima.getAutoCovarianceFunction().values(r);
            double[] psi = new RationalFunction(m_ma, m_ar).coefficients(q);
            dcov = new double[r];
            for (int i = 1; i <= q; ++i) {
                double v = m_ma.get(i);
                for (int j = i + 1; j <= q; ++j) {
                    v += m_ma.get(j) * psi[j - i];
                }
                dcov[i] = v * var;
            }
        }

        Polynomial sma = SymmetricFilter.createFromFilter(ma).getPolynomial();
        if (var != 1) {
            sma = sma.times(var);
        }

        m_bL = new Matrix(r, n);
        // complete the matrix
        // if (i >= j) m(i, j) = lband[i-j, j]; if i-j >= r, m(i, j) =0
        // if (i < j) m(i, j) = lband(j-i, i)

        DataBlockIterator cols = m_bL.columns();
        DataBlock col = cols.getData();
        for (int j = 0; j < p; ++j) {
            for (int i = 0; i < p - j; ++i) {
                col.set(i, cov[i]);
            }
            for (int i = p - j; i < r; ++i) {
                col.set(i, dcov[i]);
            }
            cols.next();
        }

        SubMatrix M = m_bL.subMatrix(0, q + 1, p, n);
        DataBlockIterator rows = M.rows();

        DataBlock row = rows.getData();
        do {
            if (sma.get(rows.getPosition()) != 0) {
                row.set(sma.get(rows.getPosition()));
            }
        }
        while (rows.next());

        lcholesky();
        return n;
    }

    private void lcholesky() {
        int r = m_bL.getRowsCount();
        int n = m_bL.getColumnsCount();
        double[] data = m_bL.internalStorage();
        if (r == 1) {
            for (int i = 0; i < data.length; ++i) {
                if (data[i] <= 0) {
                    throw new MatrixException(MatrixException.CholeskyFailed);
                }
                data[i] = Math.sqrt(data[i]);
            }
        }
        else {
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
                    throw new MatrixException(MatrixException.CholeskyFailed);
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

        double[] data = m_bL.internalStorage();

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
    
    public Matrix getCholeskyFactor(){
        return m_bL;
    }
}
