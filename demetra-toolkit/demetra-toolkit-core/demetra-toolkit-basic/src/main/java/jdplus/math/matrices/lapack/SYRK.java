/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.math.matrices.Matrix;

/**
 * Performs symmetric rank k operations.
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class SYRK {

    /**
     * C = b*C + a*A*A' or C = b*C + a*A'*A Only the lower triangular matrix is
     * computed
     *
     * @param AAt
     * @param alpha
     * @param A
     * @param beta
     * @param C
     */
    public void lapply(boolean AAt, double alpha, Matrix A, double beta, Matrix C) {
        int n = C.getRowsCount();
        int k = A.getRowsCount();
        if (n == 0 || ((alpha == 0 || k == 0) && beta == 1)) {
            return;
        }
        lmul(C, beta);
        if (alpha == 0) {
            return;
        }
        if (AAt) {
            laddaAAt(alpha, A, C);
        } else {
            laddaAtA(alpha, A, C);
        }
    }

    /**
     * S = S + a*xx'
     *
     * Only the lower part of S is computed
     *
     * @param S Symmetric matrix
     * @param x Array
     * @param a
     */
    public void laddaXXt(final double a, final DataPointer x, final Matrix S) {
        if (a == 0) {
            return;
        }
        double[] sx = S.getStorage();
        int n = S.getRowsCount(), sstart = S.getStartPosition(), slda = S.getColumnIncrement();
        int dinc = slda + 1;
        int xinc=x.inc();
        // Raw gaxpy implementation
        for (int spos = sstart, ypos = x.pos, c = 0; c < n; ypos += xinc, ++c, spos += dinc) {
            double yc = x.p[ypos];
            if (yc != 0) {
                yc *= a;
                if (xinc == 1) {
                    int xmax = x.pos + n;
                    for (int xpos = ypos, jpos = spos; xpos < xmax; ++jpos, ++xpos) {
                        sx[jpos] += yc * x.p[xpos];
                    }
                } else {
                    int xmax = x.pos + n * xinc;
                    for (int xpos = ypos, jpos = spos; xpos != xmax; ++jpos, xpos += xinc) {
                        sx[jpos] += yc * x.p[xpos];
                    }
                }
            }
        }
    }

    /**
     * S = S + a*xx'
     *
     * Only the lower part of S is computed
     *
     * @param S Symmetric matrix
     * @param x Array
     * @param a
     */
    public void uaddaXXt(final double a, final DataPointer x, final Matrix S) {
        if (a == 0) {
            return;
        }
        double[] sx = S.getStorage();
        int n = S.getRowsCount(), sstart = S.getStartPosition(), slda = S.getColumnIncrement();

        // Raw gaxpy implementation
        int xinc=x.inc();
        for (int spos = sstart, ypos = x.pos, c = 1; c <= n; ypos += xinc, ++c, spos += slda) {
            double yc = x.p[ypos];
            if (yc != 0) {
                yc *= a;
                if (xinc == 1) {
                    int jmax = spos + c;
                    for (int xpos = x.pos, jpos = spos; jpos < jmax; ++jpos, ++xpos) {
                        sx[jpos] += yc * x.p[xpos];
                    }
                } else {
                    int jmax = spos + c;
                    for (int xpos = x.pos, jpos = spos; jpos < jmax; ++jpos, xpos += xinc) {
                        sx[jpos] += yc * x.p[xpos];
                    }
                }
            }
        }
    }

    /**
     * S = S + a * A * A' Only the lower triangular part of S is computed
     *
     * @param S
     * @param A
     * @param a
     */
    public void laddaAAt(final double a, final Matrix A, final Matrix S) {
        if (a == 0) {
            return;
        }
        CPointer col = new CPointer(A.getStorage(), A.getStartPosition());
        int n = A.getColumnsCount(), lda = A.getColumnIncrement();
        for (int c = 0; c < n; ++c, col.pos += lda) {
            laddaXXt(a, col, S);
        }
    }

    /**
     * S = S + a * A * A'. Only the upper triangular part of S is computed
     *
     * @param S
     * @param A
     * @param a
     */
    public void uaddaAAt(final double a, final Matrix A, final Matrix S) {
        if (a == 0) {
            return;
        }
        CPointer col = new CPointer(A.getStorage(), A.getStartPosition());
        int n = A.getColumnsCount(), lda = A.getColumnIncrement();
        for (int c = 0; c < n; ++c, col.pos += lda) {
            uaddaXXt(a, col, S);
        }
    }

    /**
     * S = S + a * A' * A. Only the Lower triangular part of S is computed
     *
     * @param S
     * @param A
     * @param a
     */
    public void laddaAtA(final double a, final Matrix A, final Matrix S) {
        if (a == 0) {
            return;
        }
        int m = A.getRowsCount(), lda = A.getColumnIncrement();
        RPointer row = new RPointer(A.getStorage(), A.getStartPosition(), lda);
        for (int r = 0; r < m; ++r, ++row.pos) {
            laddaXXt(a, row, S);
        }
    }

    /*
     * S = S + a * A' * A.
     * Only the Upper triangular part of S is computed
     *
     * @param S
     * @param A
     * @param a
     */
    public void uaddaAtA(final double a, final Matrix A, final Matrix S) {
        if (a == 0) {
            return;
        }
        int m = A.getRowsCount(), lda = A.getColumnIncrement();
        RPointer row = new RPointer(A.getStorage(), A.getStartPosition(), lda);
        for (int r = 0; r < m; ++r, ++row.pos) {
            uaddaXXt(a, row, S);
        }
    }

    private void lmul(Matrix C, double beta) {
        if (beta == 1) {
            return;
        }
        int start = C.getStartPosition(), lda = C.getColumnIncrement(), n = C.getRowsCount();
        double[] pc = C.getStorage();
        if (beta == 0) {
            for (int c = 0, ic = start; c < n; ++c, ic += lda) {
                int jmax = ic + n;
                for (int jc = ic + c; jc < jmax; ++jc) {
                    pc[jc] = 0;
                }
            }
        } else {
            for (int c = 0, ic = start; c < n; ++c, ic += lda) {
                int jmax = ic + n;
                for (int jc = ic + c; jc < jmax; ++jc) {
                    pc[jc] *= beta;
                }
            }
        }
    }

    private void umul(Matrix C, double beta) {
        if (beta == 1) {
            return;
        }
        int start = C.getStartPosition(), lda = C.getColumnIncrement(), n = C.getRowsCount();
        double[] pc = C.getStorage();
        if (beta == 0) {
            for (int c = 1, ic = start; c <= n; ++c, ic += lda) {
                int jmax = ic + c;
                for (int jc = ic; jc < jmax; ++jc) {
                    pc[jc] = 0;
                }
            }
        } else {
            for (int c = 1, ic = start; c <= n; ++c, ic += lda) {
                int jmax = ic + c;
                for (int jc = ic; jc < jmax; ++jc) {
                    pc[jc] *= beta;
                }
            }
        }
    }

//      INFO = 0
//      IF ((.NOT.UPPER) .AND. (.NOT.LSAME(UPLO,'L'))) THEN
//          INFO = 1
//      ELSE IF ((.NOT.LSAME(TRANS,'N')) .AND.
//     +         (.NOT.LSAME(TRANS,'T')) .AND.
//     +         (.NOT.LSAME(TRANS,'C'))) THEN
//          INFO = 2
//      ELSE IF (N.LT.0) THEN
//          INFO = 3
//      ELSE IF (K.LT.0) THEN
//          INFO = 4
//      ELSE IF (LDA.LT.MAX(1,NROWA)) THEN
//          INFO = 7
//      ELSE IF (LDC.LT.MAX(1,N)) THEN
//          INFO = 10
//      END IF
//      IF (INFO.NE.0) THEN
//          CALL XERBLA('DSYRK ',INFO)
//          RETURN
//      END IF
//*
//*     Quick return if possible.
//*
//      IF ((N.EQ.0) .OR. (((ALPHA.EQ.ZERO).OR.
//     +    (K.EQ.0)).AND. (BETA.EQ.ONE))) RETURN
//*
//*     And when  alpha.eq.zero.
//*
//*
//*     Start the operations.
//*
//      IF (LSAME(TRANS,'N')) THEN
//*
//*        Form  C := alpha*A*A**T + beta*C.
//*
//          IF (UPPER) THEN
//              DO 130 J = 1,N
//                  IF (BETA.EQ.ZERO) THEN
//                      DO 90 I = 1,J
//                          C(I,J) = ZERO
//   90                 CONTINUE
//                  ELSE IF (BETA.NE.ONE) THEN
//                      DO 100 I = 1,J
//                          C(I,J) = BETA*C(I,J)
//  100                 CONTINUE
//                  END IF
//                  DO 120 L = 1,K
//                      IF (A(J,L).NE.ZERO) THEN
//                          TEMP = ALPHA*A(J,L)
//                          DO 110 I = 1,J
//                              C(I,J) = C(I,J) + TEMP*A(I,L)
//  110                     CONTINUE
//                      END IF
//  120             CONTINUE
//  130         CONTINUE
//          ELSE
//              DO 180 J = 1,N
//                  IF (BETA.EQ.ZERO) THEN
//                      DO 140 I = J,N
//                          C(I,J) = ZERO
//  140                 CONTINUE
//                  ELSE IF (BETA.NE.ONE) THEN
//                      DO 150 I = J,N
//                          C(I,J) = BETA*C(I,J)
//  150                 CONTINUE
//                  END IF
//                  DO 170 L = 1,K
//                      IF (A(J,L).NE.ZERO) THEN
//                          TEMP = ALPHA*A(J,L)
//                          DO 160 I = J,N
//                              C(I,J) = C(I,J) + TEMP*A(I,L)
//  160                     CONTINUE
//                      END IF
//  170             CONTINUE
//  180         CONTINUE
//          END IF
//      ELSE
//*
//*        Form  C := alpha*A**T*A + beta*C.
//*
//          IF (UPPER) THEN
//              DO 210 J = 1,N
//                  DO 200 I = 1,J
//                      TEMP = ZERO
//                      DO 190 L = 1,K
//                          TEMP = TEMP + A(L,I)*A(L,J)
//  190                 CONTINUE
//                      IF (BETA.EQ.ZERO) THEN
//                          C(I,J) = ALPHA*TEMP
//                      ELSE
//                          C(I,J) = ALPHA*TEMP + BETA*C(I,J)
//                      END IF
//  200             CONTINUE
//  210         CONTINUE
//          ELSE
//              DO 240 J = 1,N
//                  DO 230 I = J,N
//                      TEMP = ZERO
//                      DO 220 L = 1,K
//                          TEMP = TEMP + A(L,I)*A(L,J)
//  220                 CONTINUE
//                      IF (BETA.EQ.ZERO) THEN
//                          C(I,J) = ALPHA*TEMP
//                      ELSE
//                          C(I,J) = ALPHA*TEMP + BETA*C(I,J)
//                      END IF
//  230             CONTINUE
//  240         CONTINUE
//          END IF
//      END IF
//*
//      RETURN
//*
//*     End of DSYRK .
//*
//      END
}
