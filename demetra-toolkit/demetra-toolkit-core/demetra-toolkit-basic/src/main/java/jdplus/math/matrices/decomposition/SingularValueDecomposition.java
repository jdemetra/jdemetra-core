/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
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
package jdplus.math.matrices.decomposition;

import jdplus.data.DataBlock;
import jdplus.math.matrices.MatrixException;
import jdplus.data.DataBlockIterator;
import demetra.math.Constants;
import demetra.data.DoubleSeq;
import jdplus.math.matrices.Matrix;

/**
 * The singularValueDecomposition decomposes a matrix M in M = U * S * V' with:
 * U'U = I S diagonal with the singular values (ordered by size) V'V = I
 *
 * M ~ m x n (n <= m) U ~ m x n S ~ n x n V ~ n x n

 The columns of V are the principal components of M The columns of U * D are
 the principal factors of M We also have: M' = V * S * U' M' * M = V * S * U'
 U * S * V' = V * S^2 * V' ( the columns of V are the eigen vectors of M'M)

 If we set F = U * S we have: M = F * V' (the rows of V are the weights of the
 factors F for decomposing M) M V = F (the columns of V are the weights
 applied on the columns of M to get the factors F) @author Jean Palate, Frank
 Osaer
 */
public class SingularValueDecomposition implements ISingularValueDecomposition {

    public SingularValueDecomposition() {
    }

    /**
     *
     * @param A
     * @throws MatrixException
     */
    @Override
    public void decompose(Matrix A) throws MatrixException {
        init(A);
    }

    private void init(Matrix matrix) {
        double[] A = matrix.toArray();
        m_m = matrix.getRowsCount();
        m_n = matrix.getColumnsCount();

        // Derived from LINPACK code.
        // Initialize.
        int nu = Math.min(m_m, m_n);
        m_s = new double[Math.min(m_m + 1, m_n)];
        m_U = new double[m_m * nu];
        m_V = new double[m_n * m_n];
        // trivial case
        if (m_n == 1) {
            DataBlock a = DataBlock.of(A);
            m_s[0] = a.norm2();
            for (int i = 0; i < m_m; ++i) {
                m_U[i] = A[i] / m_s[0];
            }
            m_V[0] = 1;
            return;
        }
        double[] e = new double[m_n];
        double[] work = new double[m_m];
        boolean wantu = true;
        boolean wantv = true;

        // Reduce A to bidiagonal form, storing the diagonal elements
        // in m_s and the super-diagonal elements in e.
        int nct = Math.min(m_m - 1, m_n);
        int nrt = Math.max(0, Math.min(m_n - 2, m_m));
        for (int k = 0, mk = 0; k < Math.max(nct, nrt); k++, mk += m_m) {
            if (k < nct) {
                // Compute the transformation for the k-th column and
                // place the k-th diagonal in m_s[k].
                // Compute 2-norm of k-th column without under/overflow.
                m_s[k] = 0;
                for (int i = mk + k; i < mk + m_m; i++) {
                    m_s[k] = ElementaryTransformations.hypotenuse(m_s[k], A[i]);
                }
                if (m_s[k] != 0.0) {
                    if (A[k + mk] < 0.0) {
                        m_s[k] = -m_s[k];
                    }
                    for (int i = mk + k; i < mk + m_m; i++) {
                        A[i] /= m_s[k];
                    }
                    A[k + mk] += 1.0;
                }
                m_s[k] = -m_s[k];
            }
            for (int j = k + 1, mj = m_m * j; j < m_n; j++, mj += m_m) {
                if ((k < nct) && (m_s[k] != 0.0)) {

                    // Apply the transformation.
                    double t = 0;
                    for (int i = k; i < m_m; i++) {
                        t += A[i + mk] * A[i + mj];
                    }
                    t = -t / A[k + mk];
                    for (int i = k; i < m_m; i++) {
                        A[i + mj] += t * A[i + mk];
                    }
                }

                // Place the k-th row of A into e for the
                // subsequent calculation of the row transformation.
                e[j] = A[k + mj];
            }
            if (wantu & (k < nct)) {

                // Place the transformation in m_U for subsequent back
                // multiplication.
                System.arraycopy(A, k + mk, m_U, k + mk, m_m - k);
            }
            if (k < nrt) {

                // Compute the k-th row transformation and place the
                // k-th super-diagonal in e[k].
                // Compute 2-norm without under/overflow.
                e[k] = 0;
                for (int i = k + 1; i < m_n; i++) {
                    e[k] = ElementaryTransformations.hypotenuse(e[k], e[i]);
                }
                if (e[k] != 0.0) {
                    if (e[k + 1] < 0.0) {
                        e[k] = -e[k];
                    }
                    for (int i = k + 1; i < m_n; i++) {
                        e[i] /= e[k];
                    }
                    e[k + 1] += 1.0;
                }
                e[k] = -e[k];
                if ((k + 1 < m_m) && (e[k] != 0.0)) {

                    // Apply the transformation.
                    for (int i = k + 1; i < m_m; i++) {
                        work[i] = 0.0;
                    }
                    for (int j = k + 1, mj = m_m * j; j < m_n; j++, mj += m_m) {
                        for (int i = k + 1; i < m_m; i++) {
                            work[i] += e[j] * A[i + mj];
                        }
                    }
                    for (int j = k + 1, mj = m_m * j; j < m_n; j++, mj += m_m) {
                        double t = -e[j] / e[k + 1];
                        for (int i = k + 1; i < m_m; i++) {
                            A[i + mj] += t * work[i];
                        }
                    }
                }
                if (wantv) {

                    // Place the transformation in m_V for subsequent
                    // back multiplication.
                    System.arraycopy(e, k + 1, m_V, m_n * k + k + 1, m_n - k - 1);
                }
            }
        }

        // Set up the final bidiagonal matrix or order p.
        int p = Math.min(m_n, m_m + 1);
        if (nct < m_n) {
            m_s[nct] = A[nct + m_m * nct];
        }
        if (m_m < p) {
            m_s[p - 1] = 0.0;
        }
        if (nrt + 1 < p) {
            e[nrt] = A[nrt + m_m * (p - 1)];
        }
        e[p - 1] = 0.0;

        // If required, generate m_U.
        if (wantu) {
            for (int j = nct, mj = m_m * j; j < nu; j++, mj += m_m) {
                for (int i = 0; i < m_m; i++) {
                    m_U[i + mj] = 0.0;
                }
                m_U[j + mj] = 1.0;
            }
            for (int k = nct - 1, mk = m_m * k; k >= 0; k--, mk -= m_m) {
                if (m_s[k] != 0.0) {
                    for (int j = k + 1, mj = m_m * j; j < nu; j++, mj += m_m) {
                        double t = 0;
                        for (int i = k; i < m_m; i++) {
                            t += m_U[i + mk] * m_U[i + mj];
                        }
                        t = -t / m_U[k + mk];
                        for (int i = k; i < m_m; i++) {
                            m_U[i + mj] += t * m_U[i + mk];
                        }
                    }
                    for (int i = k; i < m_m; i++) {
                        m_U[i + mk] = -m_U[i + mk];
                    }
                    m_U[k + mk] = 1.0 + m_U[k + mk];
                    for (int i = 0; i < k - 1; i++) {
                        m_U[i + mk] = 0.0;
                    }
                } else {
                    for (int i = 0; i < m_m; i++) {
                        m_U[i + mk] = 0.0;
                    }
                    m_U[k + mk] = 1.0;
                }
            }
        }

        // If required, generate m_V.
        if (wantv) {
            for (int k = m_n - 1, nk = m_n * k; k >= 0; k--, nk -= m_n) {
                if ((k < nrt) && (e[k] != 0.0)) {
                    for (int j = k + 1, nj = m_n * j; j < nu; j++, nj += m_n) {
                        double t = 0;
                        for (int i = k + 1; i < m_n; i++) {
                            t += m_V[i + nk] * m_V[i + nj];
                        }
                        t = -t / m_V[k + 1 + nk];
                        for (int i = k + 1; i < m_n; i++) {
                            m_V[i + nj] += t * m_V[i + nk];
                        }
                    }
                }
                for (int i = 0; i < m_n; i++) {
                    m_V[i + nk] = 0.0;
                }
                m_V[k + nk] = 1.0;
            }
        }

        // Main iteration loop for the singular values.
        int pp = p - 1;
        int iter = 0;
        double eps = Constants.getEpsilon();
        while (p > 0) {
            int k, kase;

            // Here is where a allMatch for too many iterations would go.
            // This section of the program inspects for
            // negligible elements in the m_s and e arrays.  On
            // completion the variables kase and k are set as follows.
            // kase = 1     if m_s(p) and e[k-1] are negligible and k<p
            // kase = 2     if m_s(k) is negligible and k<p
            // kase = 3     if e[k-1] is negligible, k<p, and
            //              m_s(k), ..., m_s(p) are not negligible (qr step).
            // kase = 4     if e(p-1) is negligible (convergence).
            for (k = p - 2; k >= -1; k--) {
                if (k == -1) {
                    break;
                }
                if (Math.abs(e[k]) <= eps * (Math.abs(m_s[k]) + Math.abs(m_s[k + 1]))) {
                    e[k] = 0.0;
                    break;
                }
            }
            if (k == p - 2) {
                kase = 4;
            } else {
                int ks;
                for (ks = p - 1; ks >= k; ks--) {
                    if (ks == k) {
                        break;
                    }
                    double t = (ks != p ? Math.abs(e[ks]) : 0.0)
                            + (ks != k + 1 ? Math.abs(e[ks - 1]) : 0.0);
                    if (Math.abs(m_s[ks]) <= eps * t) {
                        m_s[ks] = 0.0;
                        break;
                    }
                }
                if (ks == k) {
                    kase = 3;
                } else if (ks == p - 1) {
                    kase = 1;
                } else {
                    kase = 2;
                }
                k = ks;
            }
            k++;

            // Perform the task indicated by kase.
            switch (kase) {

                // Deflate negligible m_s(p).
                case 1: {
                    double f = e[p - 2];
                    e[p - 2] = 0.0;
                    for (int j = p - 2; j >= k; j--) {
                        double t = ElementaryTransformations.hypotenuse(m_s[j], f);
                        double cs = m_s[j] / t;
                        double sn = f / t;
                        m_s[j] = t;
                        if (j != k) {
                            f = -sn * e[j - 1];
                            e[j - 1] = cs * e[j - 1];
                        }
                        if (wantv) {
                            int np = m_n * (p - 1);
                            int nj = m_n * j;
                            for (int i = 0; i < m_n; i++) {
                                t = cs * m_V[i + nj] + sn * m_V[i + np];
                                m_V[i + np] = -sn * m_V[i + nj] + cs * m_V[i + np];
                                m_V[i + nj] = t;
                            }
                        }
                    }
                }
                break;

                // Split at negligible m_s(k).
                case 2: {
                    double f = e[k - 1];
                    e[k - 1] = 0.0;
                    for (int j = k; j < p; j++) {
                        double t = ElementaryTransformations.hypotenuse(m_s[j], f);
                        double cs = m_s[j] / t;
                        double sn = f / t;
                        m_s[j] = t;
                        f = -sn * e[j];
                        e[j] = cs * e[j];
                        if (wantu) {
                            int mk = m_m * (k - 1);
                            int mj = m_m * j;
                            for (int i = 0; i < m_m; i++) {
                                t = cs * m_U[i + mj] + sn * m_U[i + mk];
                                m_U[i + mk] = -sn * m_U[i + mj] + cs * m_U[i + mk];
                                m_U[i + mj] = t;
                            }
                        }
                    }
                }
                break;

                // Perform one qr step.
                case 3: {

                    // Calculate the shift.
                    double scale = Math.max(Math.max(Math.max(Math.max(
                            Math.abs(m_s[p - 1]), Math.abs(m_s[p - 2])), Math.abs(e[p - 2])),
                            Math.abs(m_s[k])), Math.abs(e[k]));
                    double sp = m_s[p - 1] / scale;
                    double spm1 = m_s[p - 2] / scale;
                    double epm1 = e[p - 2] / scale;
                    double sk = m_s[k] / scale;
                    double ek = e[k] / scale;
                    double b = ((spm1 + sp) * (spm1 - sp) + epm1 * epm1) / 2.0;
                    double c = (sp * epm1) * (sp * epm1);
                    double shift = 0.0;
                    if ((b != 0.0) || (c != 0.0)) {
                        shift = Math.sqrt(b * b + c);
                        if (b < 0.0) {
                            shift = -shift;
                        }
                        shift = c / (b + shift);
                    }
                    double f = (sk + sp) * (sk - sp) + shift;
                    double g = sk * ek;

                    // Chase zeros.
                    for (int j = k; j < p - 1; j++) {
                        int nj = m_n * j;
                        int nj1 = nj + m_n;
                        double t = ElementaryTransformations.hypotenuse(f, g);
                        double cs = f / t;
                        double sn = g / t;
                        if (j != k) {
                            e[j - 1] = t;
                        }
                        f = cs * m_s[j] + sn * e[j];
                        e[j] = cs * e[j] - sn * m_s[j];
                        g = sn * m_s[j + 1];
                        m_s[j + 1] = cs * m_s[j + 1];
                        if (wantv) {
                            for (int i = 0; i < m_n; i++) {
                                t = cs * m_V[i + nj] + sn * m_V[i + nj1];
                                m_V[i + nj1] = -sn * m_V[i + nj] + cs * m_V[i + nj1];
                                m_V[i + nj] = t;
                            }
                        }
                        t = ElementaryTransformations.hypotenuse(f, g);
                        cs = f / t;
                        sn = g / t;
                        m_s[j] = t;
                        f = cs * e[j] + sn * m_s[j + 1];
                        m_s[j + 1] = -sn * e[j] + cs * m_s[j + 1];
                        g = sn * e[j + 1];
                        e[j + 1] = cs * e[j + 1];
                        if (wantu && (j < m_m - 1)) {
                            int mj = m_m * j;
                            int mj1 = mj + m_m;
                            for (int i = 0; i < m_m; i++) {
                                t = cs * m_U[i + mj] + sn * m_U[i + mj1];
                                m_U[i + mj1] = -sn * m_U[i + mj] + cs * m_U[i + mj1];
                                m_U[i + mj] = t;
                            }
                        }
                    }
                    e[p - 2] = f;
                    iter = iter + 1;
                }
                break;

                // Convergence.
                case 4: {

                    // Make the singular values positive.
                    if (m_s[k] <= 0.0) {

                        m_s[k] = (m_s[k] < 0.0 ? -m_s[k] : 0.0);
                        if (wantv) {
                            for (int i = m_n * k; i <= m_n * k + pp; i++) {
                                m_V[i] = -m_V[i];
                            }

                        }
                    }

                    // Order the singular values.
                    while (k < pp) {
                        if (m_s[k] >= m_s[k + 1]) {
                            break;
                        }
                        double t = m_s[k];
                        m_s[k] = m_s[k + 1];
                        m_s[k + 1] = t;
                        if (wantv && (k < m_n - 1)) {
                            int nk = m_n * k;
                            int nk1 = nk + m_n;
                            for (int i = 0; i < m_n; i++) {
                                t = m_V[i + nk1];
                                m_V[i + nk1] = m_V[i + nk];
                                m_V[i + nk] = t;
                            }
                        }
                        if (wantu && (k < m_m - 1)) {
                            int mk = m_m * k;
                            int mk1 = mk + m_m;
                            for (int i = 0; i < m_m; i++) {
                                t = m_U[i + mk1];
                                m_U[i + mk1] = m_U[i + mk];
                                m_U[i + mk] = t;
                            }
                        }
                        k++;
                    }
                    iter = 0;
                    p--;
                }
                break;
            }
        }
    }

    @Override
    public Matrix U() {
        return new Matrix(m_U, m_m, Math.min(m_m, m_n));
    }

    @Override
    public Matrix V() {
        return new Matrix(m_V, m_n, m_n);
    }

    public double[] getSingularValues() {
        return m_s;
    }

    /**
     *
     * @return
     */
    @Override
    public DoubleSeq S() {
        return DoubleSeq.of(m_s);
    }

    public double norm2() {
        return m_s[0];
    }

    public double cond() {
        return m_s[0] / m_s[Math.min(m_m, m_n) - 1];
    }

    @Override
    public int rank() {
        double eps = Constants.getEpsilon();
        double tol = Math.max(m_m, m_n) * m_s[0] * eps;
        int r = 0;
        for (int i = 0; i < m_s.length; i++) {
            if (m_s[i] > tol) {
                r++;
            }
        }
        return r;
    }

    @Override
    public boolean isFullRank() {
        return rank() == m_s.length;
    }

    private double[] solve(double[] b) throws MatrixException {
        if (b.length != m_m) {
            throw new MatrixException("Incompatible dimensions");
        }
        double[] rslt = new double[m_n];
        // rslt = Sum (U[i;] * b / s[i] * V[i;]
        int r = rank();
        for (int i = 0, idxU = 0, idxV = 0; i < r; ++i) {
            double x = 0;
            // compute U[j;i] * b[j]
            for (int j = 0; j < m_m; ++j, ++idxU) {
                x += m_U[idxU] * b[j];
            }
            x /= m_s[i];

            for (int j = 0; j < m_n; ++j, ++idxV) {
                rslt[j] += m_V[idxV] * x;
            }
        }
        return rslt;

    }
    /// <summary>
    /// internal storage of U, V.
    /// </summary>
    private double[] m_U, m_V;
    /// <summary>
    /// Array for internal storage of singular values.
    /// </summary>
    private double[] m_s;
    /// <summary>
    /// Row and column dimensions.
    /// </summary>
    private int m_m, m_n;

    public void solve(DoubleSeq xin, DataBlock xout) throws MatrixException {
        double[] data = new double[xin.length()];
        xin.copyTo(data, 0);
        double[] rslt = solve(data);
        xout.copyFrom(rslt, 0);
    }

    /*
     * Solves A X = B
     */
    public void solve(Matrix B, Matrix X) {
        DataBlockIterator b = B.columnsIterator(), x = X.columnsIterator();
        while (b.hasNext()) {
            solve(b.next(), x.next());
        }
    }
}