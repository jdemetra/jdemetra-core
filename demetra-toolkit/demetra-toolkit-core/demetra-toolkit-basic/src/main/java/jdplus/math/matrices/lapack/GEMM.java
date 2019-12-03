/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixException;
import jdplus.math.matrices.MatrixTransformation;

/**
 * performs one of the matrix-matrix operations
 *
 * C := alpha*op( A )*op( B ) + beta*C
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class GEMM {

    public void apply(double alpha, Matrix A, Matrix B, double beta, Matrix C) {
        int m = C.getRowsCount(), n = C.getColumnsCount();
        // multiply by beta
        int cstart = C.getStartPosition(), cinc = C.getColumnIncrement();
        CPointer pc = new CPointer(C.getStorage(), cstart);
        if (beta == 0) {
            for (int c = 0; c < n; ++c, pc.pos += cinc) {
                pc.set(m, 0);
            }
            pc.pos = cstart;
        } else if (beta == -1) {
            for (int c = 0; c < n; ++c, pc.pos += cinc) {
                pc.chs(m);
            }
            pc.pos = cstart;
        } else if (beta != 1) {
            for (int c = 0; c < n; ++c, pc.pos += cinc) {
                pc.mul(m, beta);
            }
            pc.pos = cstart;
        }
        if (alpha != 0) {
            int k = A.getColumnsCount();
            int astart = A.getStartPosition(), ainc = A.getColumnIncrement();
            int bstart = B.getStartPosition(), binc = B.getColumnIncrement();
            CPointer pa = new CPointer(A.getStorage(), astart);
            CPointer pb = new CPointer(B.getStorage(), bstart);

            for (int j = 0; j < n; ++j, pc.pos += cinc, pb.pos += binc) {
                for (int i = 0; i < k; ++i, pa.pos += ainc) {
                    double w = alpha * pb.value(i);
                    if (w != 0) {
                        pc.addAX(m, w, pa);
                    }
                }
                pa.pos = astart;
            }
        }
    }

    public void apply(double alpha, Matrix A, Matrix B, double beta, Matrix C,
            MatrixTransformation ta, MatrixTransformation tb) {
        int m = C.getRowsCount(), n = C.getColumnsCount();
        int ma = A.getRowsCount(), na = A.getColumnsCount();
        int mb = B.getRowsCount(), nb = B.getColumnsCount();

        if ((ta == MatrixTransformation.Transpose && m != na)
                || (ta == MatrixTransformation.None && m != ma)
                || (tb == MatrixTransformation.Transpose && n != mb) || (tb == MatrixTransformation.None && n != nb)) {
            throw new MatrixException(MatrixException.DIM);
        }
        int k = ta == MatrixTransformation.Transpose ? ma : na;
        if ((tb == MatrixTransformation.Transpose && k != nb) || (tb == MatrixTransformation.None && k != mb)) {
            throw new MatrixException(MatrixException.DIM);
        }
        if (m == 0 || n == 0 || ((alpha == 0 || k == 0) && beta == 1)) {
            return;
        }
        int cstart = C.getStartPosition(), clda = C.getColumnIncrement();
        double[] pc = C.getStorage();
        if (alpha == 0) {
            mul(beta, pc, m, n, cstart, clda);
            return;
        }

        switch (tb) {
            case Transpose:
                switch (ta) {
                    case Transpose:
                        addAtBt(C, beta, A, B, alpha);
                        break;
                    case None:
                        addABt(C, beta, A, B, alpha);
                        break;
                }
                break;
            case None:
                switch (ta) {
                    case Transpose:
                        addAtB(C, beta, A, B, alpha);
                        break;
                    case None:
                        addAB(C, beta, A, B, alpha);
                        break;
                }
        }
    }

    private void mul(double beta, double[] pc, int m, int n, int cstart, int clda) {
        if (beta == 0) {
            int icmax = cstart + n * clda;
            for (int ic = cstart; ic < icmax; ic += clda) {
                int jcmax = ic + m;
                for (int jc = ic; jc < jcmax; ++jc) {
                    pc[jc] = 0;
                }
            }
        } else if (beta != 1) {
            int icmax = cstart + n * clda;
            for (int ic = cstart; ic < icmax; ic += clda) {
                int jcmax = ic + m;
                for (int jc = ic; jc < jcmax; ++jc) {
                    pc[jc] *= beta;
                }
            }
        }
    }

    /**
     * C = beta*C + alpha*A*B
     *
     * @param C
     * @param beta
     * @param A
     * @param B
     * @param alpha
     */
    private static void addAB(Matrix C, double beta, Matrix A, Matrix B, double alpha) {
        int m = C.getRowsCount(), n = C.getColumnsCount(), k = A.getColumnsCount();
        int astart = A.getStartPosition(), alda = A.getColumnIncrement();
        int bstart = B.getStartPosition(), blda = B.getColumnIncrement();
        int cstart = C.getStartPosition(), clda = C.getColumnIncrement();
        CPointer lc = new CPointer(C.getStorage(), cstart);
        CPointer la = new CPointer(A.getStorage(), astart);
        CPointer lb = new CPointer(B.getStorage(), bstart);
        for (int j = 0; j < n; ++j, lc.pos += clda, lb.pos += blda) {
            if (beta == 0) {
                lc.set(m, 0);
            } else if (beta != 1) {
                lc.mul(m, beta);
            }
            la.pos = astart;
            for (int l = 0; l < k; ++l, la.pos += alda) {
                double tmp = alpha * lb.value(l);
                if (tmp != 0) {
                    lc.addAX(m, tmp, la);
                }
            }
        }
    }

    private static void addAtBt(Matrix C, double beta, Matrix A, Matrix B, double alpha) {
        int m = C.getRowsCount(), n = C.getColumnsCount(), k = A.getRowsCount();
        int astart = A.getStartPosition(), alda = A.getColumnIncrement();
        int bstart = B.getStartPosition(), blda = B.getColumnIncrement();
        int cstart = C.getStartPosition(), clda = C.getColumnIncrement();
        DataPointer la = new CPointer(A.getStorage(), astart);
        DataPointer lb = new RPointer(B.getStorage(), bstart, blda);
        double[] pc = C.getStorage();
        for (int j = 0, ic = cstart; j < n; ++j, ic += clda, ++lb.pos) {
            la.pos=astart;
            for (int i = 0, ijc = ic; i < m; ++i, ++ijc, la.pos+=alda) {
                double tmp = la.dot(k, lb);
                if (beta == 0) {
                    pc[ijc] = alpha * tmp;
                } else {
                    pc[ijc] = alpha * tmp + beta * pc[ijc];
                }
            }
        }
    }

    private static void addAtB(Matrix C, double beta, Matrix A, Matrix B, double alpha) {
        int m = C.getRowsCount(), n = C.getColumnsCount(), k = A.getRowsCount();
        int astart = A.getStartPosition(), alda = A.getColumnIncrement();
        int bstart = B.getStartPosition(), blda = B.getColumnIncrement();
        int cstart = C.getStartPosition(), clda = C.getColumnIncrement();
        DataPointer la = new CPointer(A.getStorage(), astart);
        DataPointer lb = new CPointer(B.getStorage(), bstart);
        double[] pc = C.getStorage();
        for (int j = 0, ic = cstart; j < n; ++j, ic += clda, lb.pos += blda) {
            la.pos=astart;
            for (int i = 0, ijc = ic; i < m; ++i, ++ijc, la.pos+=alda) {
                double tmp = la.dot(k, lb);
                if (beta == 0) {
                    pc[ijc] = alpha * tmp;
                } else {
                    pc[ijc] = alpha * tmp + beta * pc[ijc];
                }
            }
        }

    }

    private static void addABt(Matrix C, double beta, Matrix A, Matrix B, double alpha) {
        int m = C.getRowsCount(), n = C.getColumnsCount(), k = A.getColumnsCount();
        int astart = A.getStartPosition(), alda = A.getColumnIncrement();
        int bstart = B.getStartPosition(), blda = B.getColumnIncrement();
        int cstart = C.getStartPosition(), clda = C.getColumnIncrement();
        DataPointer lc = new CPointer(C.getStorage(), cstart);
        DataPointer la = new CPointer(A.getStorage(), astart);
        DataPointer lb = new RPointer(B.getStorage(), bstart, blda);
        for (int j = 0; j < n; ++j, lc.pos += clda, lb.pos++) {
            if (beta == 0) {
                SCAL.apply(m, 0, lc);
            } else if (beta != 1) {
                SCAL.apply(m, beta, lc);
            }
            la.pos = astart;
            for (int l = 0; l < k; ++l, la.pos += alda) {
                double tmp = alpha * lb.value(l);
                if (tmp != 0) {
                    AXPY.apply(m, tmp, la, lc);
                }
            }
        }
    }
}
