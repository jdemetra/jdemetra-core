/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.decomposition;

import demetra.data.DoubleSeq;
import demetra.math.Constants;
import jdplus.data.DataBlock;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixException;
import jdplus.math.matrices.UpperTriangularMatrix;

/**
 * QR decomposition of a matrix. The matrix Q is represented as a product of
 * elementary reflectors Q = H(1) H(2) . . . H(k), where k = min(m,n).
 *
 * Each H(i) has the form
 *
 * H(i) = I - tau * v * v'
 *
 * where tau is a real scalar, and v is a real vector with v(0:i-1) = 0
 * v(i) = 1; v(i+1:m) is stored in qr(i+1:m,i), and tau in tau(i).
 * 
 * v is defined by x/||x|| -+ e1 (- if x0 lt 0, + otherwise
 * we write x/||x||=y
 * 
 * tau = 2/(v*v') = 2/( y-+e1)(y-+e1)' = 2/(2-+2y0)) = 1/(1-+y0)=-1/v0
 * 
 * v(i) = -1/tau(i) (not stored); v(i+1:m) is stored in qr(i+1:m,i), and tau in tau(i).
 * 
 * H*x = H*y*||x|| = -+ e1*||x|| 
 * 
 * In the case of Singular matrix, householder with pivoting should be used.
 * All the taus equal to 0 should be placed at the end of the tau-vector
 *
 * When pivoting is not used, pivot should be null.
 *
 * @author palatej
 */
public class QRDecomposition {

    private final Matrix qr;
    private final double[] beta;
    private final int[] pivot;

    public QRDecomposition(Matrix qr, double[] beta, int[] pivot) {
        this.qr = qr;
        this.beta = beta;
        this.pivot = pivot;
    }

    /**
     * The method multiplies an array of double by Q.
     *
     * @param b The array of double. It contains the product at the // return of
     * the method
     */
    public void applyQ(double[] b) {
        double[] p = qr.getStorage();
        int m = qr.getRowsCount(), n = qr.getColumnsCount();
        if (m != b.length) {
            throw new MatrixException(MatrixException.DIM);
        }
        for (int k = n - 1, ik = (m + 1) * n; k >= 0; --k, ik -= m + 1) {
            double s = b[k]/beta[k];
            for (int i = k+1, j = ik+1; i < m; ++i, ++j) {
                s += p[j] * b[i];
            }
            if (s != 0) {
                b[k]-=s;
                s *= -beta[k];
                for (int i = k+1, j = ik+1; i < m; ++i, ++j) {
                    b[i] += s * p[j];
                }
            }
        }
    }

    /**
     * The method multiplies an array of double by the transpose of Q (also
     * equal to its inverse).
     *
     * @param b
     */
    public void applyQt(double[] b) {
        double[] p = qr.getStorage();
        int m = qr.getRowsCount(), n = qr.getColumnsCount();
        if (m != b.length) {
            throw new MatrixException(MatrixException.DIM);
        }
        for (int k = 0, ik = 0; k < n; ++k, ik += m + 1) {
            double s = b[k]/beta[k];
            for (int i = k+1, j = ik+1; i < m; ++i, ++j) {
                s +=  b[i]*p[j];
            }
            if (s != 0) {
                b[k]-=s;
                s *= -beta[k];
                for (int i = k+1, j = ik+1; i < m; ++i, ++j) {
                    b[i] += s * p[j];
                }
            }
        }
    }

    public int rank(double eps) {
        return qr.diagonal().count(x -> Math.abs(x) > eps);
    }

    public void leastSquares(DoubleSeq x, DataBlock b, DataBlock res, double eps) {
        double[] y = new double[x.length()];
        int rank = rank(eps);
        x.copyTo(y, 0);
        applyQt(y);
        if (res != null) {
            res.copyFrom(y, rank);
        }
        // Solve R*X = Y;
        UpperTriangularMatrix.solveUx(qr.extract(0, rank, 0, rank), DataBlock.of(y));
        if (pivot == null) {
            b.copyFrom(y, 0);
        } else {
            for (int i = 0; i < rank; ++i) {
                b.set(pivot[i], y[i]);
            }
        }

    }

    public void leastSquares(DoubleSeq x, DataBlock b, DataBlock res) {
        leastSquares(x, b, res, Constants.getEpsilon());
    }

    public Matrix rawR() {
        int n = qr.getColumnsCount();
        Matrix R = qr.extract(0, n, 0, n).deepClone();
        UpperTriangularMatrix.toUpper(R);
        return R;
    }

    public DoubleSeq rawRdiagonal() {
        return qr.diagonal();
    }

}
