/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.leastsquares;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DoubleSequence;
import demetra.data.DoubleSequence;
import demetra.maths.Constants;
import demetra.maths.matrices.Matrix;
import demetra.maths.MatrixException;
import demetra.maths.matrices.UpperTriangularMatrix;
import org.openide.util.lookup.ServiceProvider;

/**
 * Method based on self-scaling fast (givens) rotations. See Anda A. and Park H. [1994,
 * 1996]
 *
 * @author Jean Palate
 */
@ServiceProvider(service = QRSolver.class)
public class FastRotationsSolver implements QRSolver {

    private Matrix R;
    private double[] c, d2;
    private double eps = Constants.getEpsilon();
    private int n, m;
    private double[] A;

    @Override
    public boolean solve(DoubleSequence y, Matrix x) {
        try {
            m = x.getColumnsCount();
            n = x.getRowsCount();
            A = new double[n * (m + 1)];
            x.copyTo(A, 0);
            y.copyTo(A, n * m);
            d2 = new double[n];
            for (int i = 0; i < n; ++i) {
                d2[i] = 1;
            }
            for (int i = 0; i < m; ++i) {
                for (int j = i + 1; j < n; ++j) {
                    zero(i, j);
                }
            }
            c = new double[m];
            System.arraycopy(A, n * m, c, 0, m);
            R = Matrix.square(m);
            DataBlockIterator cols = R.columnsIterator();
            int pos = 0;
            while (cols.hasNext()) {
                cols.next().copyFrom(A, pos);
                pos += n;
            }
            UpperTriangularMatrix.rsolve(R, DataBlock.ofInternal(c), eps);

            return true;
        } catch (MatrixException err) {
            return false;
        }
    }

    private void zero(int p, int q) {
        int ppos = p + p * n;
        int qpos = q + p * n;
        double yq = A[qpos];
        if (Math.abs(yq) <= eps) {
            A[qpos] = 0;
            return; // nothing to do
        }
        double yp = A[ppos];
        if (Math.abs(yp) <= eps) {
            A[ppos] = 0;
            int i = ppos, j = qpos;
            while (i < A.length) {
                double tmp = A[i];
                A[i] = A[j];
                A[j] = tmp;
                i += n;
                j += n;
            }
            double tmp = d2[p];
            d2[p] = d2[q];
            d2[q] = tmp;
            return;
        }
        double r = yq / yp;
        double g = d2[p] / d2[q];

        if (r * r < g) {
            if (g >= 1) {
                zero11(p, q, yp, yq, g);
            } else {
                zero12(p, q, yp, yq, g);
            }
        } else if (g >= 1) {
            zero21(p, q, yp, yq, g);
        } else {
            zero22(p, q, yp, yq, g);
        }
    }

    @Override
    public DoubleSequence coefficients() {
        return DoubleSequence.ofInternal(c);
    }

    @Override
    public Matrix R() {
        return R;
    }

    private void zero11(int p, int q, double yp, double yq, double g) {
        double t = yq / yp; // small
        double b = t / g;
        double d = 1 + b * t;
        double a = t / d;
        d2[p] /= d;
        d2[q] *= d;
        // apply the transformation on the next columns
        int s = n * (p + 1);
        for (int ppos = p + s, qpos = q + s; ppos < A.length; ppos += n, qpos += n) {
            double zp = A[ppos];
            double zq = A[qpos];
            zp += zq * b;
            zq -= zp * a;
            A[ppos] = zp;
            A[qpos] = zq;
        }

        A[p + p * n] = yp * d;
        A[q + p * n] = 0;
    }

    private void zero12(int p, int q, double yp, double yq, double g) {
        double a = yq / yp; // small
        double t = a / g;
        double d = 1 + a * t;
        double b = t / d;
        d2[p] *= d;
        d2[q] /= d;
        // apply the transformation on the next columns
        int s = n * (p + 1);
        for (int ppos = p + s, qpos = q + s; ppos < A.length; ppos += n, qpos += n) {
            double zp = A[ppos];
            double zq = A[qpos];
            zq -= zp * a;
            zp += zq * b;
            A[ppos] = zp;
            A[qpos] = zq;
        }
        A[q + p * n] = 0;
    }

    private void zero21(int p, int q, double yp, double yq, double g) {
        double a = yp / yq;
        double t = a * g;
        double d = 1 + a * t;
        double b = t / d;
        double tmp = d2[p];
        d2[p] = d2[q] * d;
        d2[q] = tmp / d;
        // apply the transformation on the next columns
        int s = n * (p + 1);
        for (int ppos = p + s, qpos = q + s; ppos < A.length; ppos += n, qpos += n) {
            double zp = A[ppos];
            double zq = A[qpos];
            double wp = zq;
            double wq = a * zq - zp;
            zp = wp - b * wq;
            zq = wq;
            A[ppos] = zp;
            A[qpos] = zq;
        }
        A[p + p * n] = yq;
        A[q + p * n] = 0;
    }

    private void zero22(int p, int q, double yp, double yq, double g) {
        double t = yp / yq;
        double b = t * g;
        double d = 1 + b * t;
        double a = t / d;
        double tmp = d2[p];
        d2[p] = d2[q] / d;
        d2[q] = tmp * d;
        // apply the transformation on the next columns
        int s = n * (p + 1);
        for (int ppos = p + s, qpos = q + s; ppos < A.length; ppos += n, qpos += n) {
            double zp = A[ppos];
            double zq = A[qpos];
            double wp = b * zp + zq;
            double wq = zp;
            zp = wp;
            zq = a * wp - wq;
            A[ppos] = zp;
            A[qpos] = zq;
        }
        A[p + p * n] = yq * d;
        A[q + p * n] = 0;
    }

    @Override
    public DoubleSequence residuals() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double ssqerr() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

 }
