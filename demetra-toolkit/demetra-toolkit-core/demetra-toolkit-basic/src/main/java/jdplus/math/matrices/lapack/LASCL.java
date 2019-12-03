/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import demetra.math.Constants;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.TypeOfMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class LASCL {

    /**
     * Multiplies the M by N real matrix A by the real scalar CTO/CFROM.This is
 done without over/underflow as long as the final result CTO*A(I,J)/CFROM
 does not over/underflow.TYPE specifies that A may be full, upper
 triangular, lower triangular or upper Hessenberg.
     *
     * @param A
     * @param type 'G': A is a full matrix, 'L': A is a lower triangular matrix,
     * 'U': A is an upper triangular matrix, 'H': A is an upper Hessenberg
     * matrix
     * @param cfrom
     * @param cto
     */
    public void apply(Matrix A, TypeOfMatrix type, double cfrom, double cto) {
        if (A.isEmpty()) {
            return;
        }
        double smlnum = Constants.BIGINV, bignum = Constants.BIG;
        double cfromc = cfrom, ctoc = cto;
        double cfrom1 = cfromc * smlnum;
        double mul;
        double cto1;
        boolean done = false;
        while (!done) {
            if (cfromc == cfrom1) {
//   cfromc is an inf.  Multiply by a correctly signed zero for
//   finite ctoc, or a NaN if ctoc is infinite.
                mul = ctoc / cfromc;
                done = true;
                cto1 = ctoc;
            } else {
                cto1 = ctoc / bignum;
                if (cto1 == ctoc) {
//   ctoc is either 0 or an inf.  In both cases, ctoc itself
//   serves as the correct multiplication factor.
                    mul = ctoc;
                    done = true;
                    cfromc = 1;
                } else if (Math.abs(cfrom1) > Math.abs(ctoc) && ctoc != 0) {
                    mul = smlnum;
                    done = false;
                    cfromc = cfrom1;
                } else if (Math.abs(cto1) > Math.abs(cfromc)) {
                    mul = bignum;
                    done = false;
                    ctoc = cto1;
                } else {
                    mul = ctoc / cfromc;
                    done = true;
                }
            }

            switch (type) {
                case LowerTriangular:
                    lscal(A, mul);
                    break;
                case UpperTriangular:
                    uscal(A, mul);
                    break;
                case UpperHessenberg:
                    hscal(A, mul);
                    break;
                default:
                    gscal(A, mul);
             }
        }
    }

    private void gscal(Matrix A, double mul) {
        double[] storage = A.getStorage();
             int i0 = A.getStartPosition();
            int cinc = A.getColumnIncrement();
            int nr = A.getRowsCount();
            int i1 = i0 + cinc * A.getColumnsCount();
            int j = i0 + 1 + nr;
            for (int i = i0; i < i1; i += cinc, j += cinc) {
                for (int k = i; k < j; ++k) {
                    storage[k] *= mul;
                }
            }
    }

    private void lscal(Matrix A, double mul) {
        double[] storage = A.getStorage();
        int i0 = A.getStartPosition();
        int nc = A.getColumnsCount(), cinc = A.getColumnIncrement();
        int i1 = i0 + cinc * nc;
        int nr = A.getRowsCount();
        int j = i0 + 1 + nr;
        int dinc = cinc + 1;
        for (int i = i0; i < i1; i += dinc, j += cinc) {
            for (int k = i; k < j; ++k) {
                storage[k] *= mul;
            }
        }
    }

    private void uscal(Matrix A, double mul) {
        double[] storage = A.getStorage();
        int i0 = A.getStartPosition();
        int cinc = A.getColumnIncrement();
        int nr = A.getRowsCount();
        int i1 = i0 + cinc * A.getColumnsCount();
        int j = i0 + 1;
        int dinc = cinc + 1;
        for (int i = i0; i < i1; i += cinc, j += dinc) {
            for (int k = i; k < j; ++k) {
                storage[k] *= mul;
            }
        }

    }

    private void hscal(Matrix A, double mul) {
        double[] storage = A.getStorage();
        int i0 = A.getStartPosition();
        int cinc = A.getColumnIncrement();
        int nr = A.getRowsCount();
        int i1 = i0 + cinc * A.getColumnsCount();
        int j = i0 + 1 + nr;
        int dinc = cinc + 1;
        for (int i = i0; i < i1; i += dinc, j += cinc) {
            for (int k = i; k < j; ++k) {
                storage[k] *= mul;
            }
        }

    }

//*
//      IF( ITYPE.EQ.0 ) THEN
//*
//*        Full matrix
//*
//         DO 30 J = 1, N
//            DO 20 I = 1, M
//               A( I, J ) = A( I, J )*MUL
//   20       CONTINUE
//   30    CONTINUE
//*
//      ELSE IF( ITYPE.EQ.1 ) THEN
//*
//*        Lower triangular matrix
//*
//         DO 50 J = 1, N
//            DO 40 I = J, M
//               A( I, J ) = A( I, J )*MUL
//   40       CONTINUE
//   50    CONTINUE
//*
//      ELSE IF( ITYPE.EQ.2 ) THEN
//*
//*        Upper triangular matrix
//*
//         DO 70 J = 1, N
//            DO 60 I = 1, MIN( J, M )
//               A( I, J ) = A( I, J )*MUL
//   60       CONTINUE
//   70    CONTINUE
//*
//      ELSE IF( ITYPE.EQ.3 ) THEN
//*
//*        Upper Hessenberg matrix
//*
//         DO 90 J = 1, N
//            DO 80 I = 1, MIN( J+1, M )
//               A( I, J ) = A( I, J )*MUL
//   80       CONTINUE
//   90    CONTINUE
//*
//      ELSE IF( ITYPE.EQ.4 ) THEN
//*
//*        Lower half of a symmetric band matrix
//*
//         K3 = KL + 1
//         K4 = N + 1
//         DO 110 J = 1, N
//            DO 100 I = 1, MIN( K3, K4-J )
//               A( I, J ) = A( I, J )*MUL
//  100       CONTINUE
//  110    CONTINUE
//*
//      ELSE IF( ITYPE.EQ.5 ) THEN
//*
//*        Upper half of a symmetric band matrix
//*
//         K1 = KU + 2
//         K3 = KU + 1
//         DO 130 J = 1, N
//            DO 120 I = MAX( K1-J, 1 ), K3
//               A( I, J ) = A( I, J )*MUL
//  120       CONTINUE
//  130    CONTINUE
//*
//      ELSE IF( ITYPE.EQ.6 ) THEN
//*
//*        Band matrix
//*
//         K1 = KL + KU + 2
//         K2 = KL + 1
//         K3 = 2*KL + KU + 1
//         K4 = KL + KU + 1 + M
//         DO 150 J = 1, N
//            DO 140 I = MAX( K1-J, K2 ), MIN( K3, K4-J )
//               A( I, J ) = A( I, J )*MUL
//  140       CONTINUE
//  150    CONTINUE
//*
//      END IF
//*
//      IF( .NOT.DONE )
//     $   GO TO 10
//*
//      RETURN
//*
//*     End of DLASCL
//*
//      END
}
