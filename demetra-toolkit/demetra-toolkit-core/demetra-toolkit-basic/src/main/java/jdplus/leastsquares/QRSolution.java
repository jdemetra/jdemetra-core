/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.leastsquares;

import demetra.data.DoubleSeq;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.math.matrices.UpperTriangularMatrix;
import jdplus.math.matrices.decomposition.QRDecomposition;

/**
 * Solution to the least squares problem: min || y - X*b ||
 * by means of the QR algorithm:
 *
 * X = Q*R
 * || y - X*b || = || Q'*y - R*b ||
 *
 * Q'*y = z = (z0', z1')'
 * z0 = R*b <=> R^-1*z0 = b
 *
 * || y - X*b || = || z1 ||
 *
 * z1 = e (residuals)
 * z1'z1 = ssqerr
 *
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
public class QRSolution {

    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    private QRDecomposition qr;
    private int rank;
    private DoubleSeq b;
    private DoubleSeq e;
    private double ssqErr;

    public int rank() {
        return rank;
    }

    public Matrix rawR() {
        return qr.rawR();
    }

    public DoubleSeq rawRDiagonal() {
        return qr.rawRdiagonal();
    }

    /**
     * Contains the order in which the columns of X have be handled.
     * pivot[i] indicates which column is in position i after pivoting
     *
     * @return
     */
    public int[] pivot() {
        return qr.pivot();
    }

    public Matrix unscaledCovariance() {
        int[] pivot = qr.pivot();
        Matrix rawR = qr.rawR().extract(0, rank, 0, rank);
        Matrix v = SymmetricMatrix.UUt(UpperTriangularMatrix
                .inverse(rawR));
        int n = qr.n();
        if (pivot == null) {
            if (rank == n) {
                return v;
            } else {
                Matrix V = Matrix.square(n);
                V.extract(0, rank, 0, rank).copy(v);
                return V;
            }
        } else {
            Matrix V = Matrix.square(n);
            for (int i = 0; i < rank; ++i) {
                double sii = v.get(i, i);
                V.set(pivot[i], pivot[i], sii);
                for (int j = 0; j < i; ++j) {
                    double sij = v.get(i, j);
                    V.set(pivot[i], pivot[j], sij);
                    V.set(pivot[j], pivot[i], sij);
                }
            }
            return V;
        }
    }

    /**
     * Inverse of the unscaled covariance matrix
     * =re-ordered rawR'*rawR
     *
     * @return
     */
    public Matrix RtR() {
        int[] pivot = qr.pivot();
        Matrix rawR = qr.rawR();
        Matrix v = SymmetricMatrix.UtU(rawR);
        if (pivot == null) {
            return v;
        } else {
            int n = pivot.length;
            Matrix V = Matrix.square(n);
            for (int i = 0; i < rank; ++i) {
                double sii = v.get(i, i);
                V.set(pivot[i], pivot[i], sii);
                for (int j = 0; j < i; ++j) {
                    double sij = v.get(i, j);
                    V.set(pivot[i], pivot[j], sij);
                    V.set(pivot[j], pivot[i], sij);
                }
            }
            return V;
        }
    }
}
