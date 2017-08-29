/*
* Copyright 2013 National Bank ofInternal Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions ofInternal the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy ofInternal the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package demetra.linearsystem;

import demetra.design.Development;
import java.util.Arrays;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.maths.Constants;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixException;
import demetra.linearsystem.ILinearSystemSolver;
import demetra.maths.matrices.UpperTriangularMatrix;

/**
 * Based on fast givens rotations
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class FastRotationsSystemSolver implements ILinearSystemSolver {

    private double[] d2;
    private double eps = Constants.getEpsilon();
    private int n, m;
    private double[] data;

    /**
     * Compute X such that M * X = B (or X = M^-1 * B)
     *
     * @param M
     * @param B
     * @return
     */
    private boolean solve() {
        d2=new double[n];
        for (int i=0; i<n; ++i)
            d2[i]=1;
        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                zero(i, j);
            }
        }
        Matrix Q = Matrix.builder(data).nrows(n).ncolumns(m).build();
        Matrix R = Q.extract(0, n, 0, n);
        Matrix B = Q.extract(0, n, n, m-n);
        UpperTriangularMatrix.rsolve(R, B, eps);

        return true;
    }

    private void zero(int p, int q) {
        int ppos = p + p * n;
        int qpos = q + p * n;
        double yq = data[qpos];
        if (Math.abs(yq) <= eps) {
            data[qpos] = 0;
            return; // nothing to do
        }
        double yp = data[ppos];
        if (Math.abs(yp) <= eps) {
            data[ppos] = 0;
            int i = ppos, j = qpos;
            while (i < data.length) {
                double tmp = data[i];
                data[i] = data[j];
                data[j] = tmp;
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

    private void zero11(int p, int q, double yp, double yq, double g) {
        double t = yq / yp; // small
        double b = t / g;
        double d = 1 + b * t;
        double a = t / d;
        d2[p] /= d;
        d2[q] *= d;
        // apply the transformation on the next columns
        int s = n * (p + 1);
        for (int ppos = p + s, qpos = q + s; ppos < data.length; ppos += n, qpos += n) {
            double zp = data[ppos];
            double zq = data[qpos];
            zp += zq * b;
            zq -= zp * a;
            data[ppos] = zp;
            data[qpos] = zq;
        }

        data[p + p * n] = yp * d;
        data[q + p * n] = 0;
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
        for (int ppos = p + s, qpos = q + s; ppos < data.length; ppos += n, qpos += n) {
            double zp = data[ppos];
            double zq = data[qpos];
            zq -= zp * a;
            zp += zq * b;
            data[ppos] = zp;
            data[qpos] = zq;
        }
        data[q + p * n] = 0;
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
        for (int ppos = p + s, qpos = q + s; ppos < data.length; ppos += n, qpos += n) {
            double zp = data[ppos];
            double zq = data[qpos];
            double wp = zq;
            double wq = a * zq - zp;
            zp = wp - b * wq;
            zq = wq;
            data[ppos] = zp;
            data[qpos] = zq;
        }
        data[p + p * n] = yq;
        data[q + p * n] = 0;
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
        for (int ppos = p + s, qpos = q + s; ppos < data.length; ppos += n, qpos += n) {
            double zp = data[ppos];
            double zq = data[qpos];
            double wp = b * zp + zq;
            double wq = zp;
            zp = wp;
            zq = a * wp - wq;
            data[ppos] = zp;
            data[qpos] = zq;
        }
        data[p + p * n] = yq * d;
        data[q + p * n] = 0;
    }

    @Override
    public void solve(Matrix A, DataBlock b) throws MatrixException {
        n = A.getRowsCount();
        if (b.length() != n) {
            throw new MatrixException(MatrixException.DIM);
        }
        m=n+1;
        data = new double[n * m];
        A.copyTo(data, 0);
        b.copyTo(data, n * n);
        if (!solve()) {
            throw new MatrixException(MatrixException.SINGULAR);
        }
        b.copyFrom(data, n * n);
    }

    @Override
    public void solve(Matrix A, Matrix B) throws MatrixException {
        n = A.getRowsCount();
        int l = B.getColumnsCount();
        m = l + n;
        if (B.getRowsCount() != n) {
            throw new MatrixException(MatrixException.DIM);
        }
        data = new double[n * m];
        A.copyTo(data, 0);
        B.copyTo(data, n * n);
        if (!solve()) {
            throw new MatrixException(MatrixException.SINGULAR);
        }
        Matrix Q = Matrix.builder(data)
                .start(n * n).nrows(n).ncolumns(l).build();
        B.copy(Q);
    }
}
