/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.lapack.ScaledSumOfSquares;

/**
 * Returns the value of the 1-norm, Frobenius norm, infinity-norm, or the
 * largest absolute value of any element of a general rectangular matrix.
 *
 * DLANGE in LAPACK
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class MatrixNorms {

    /**
     * Max of abs value
     *
     * @param A
     * @return
     */
    public double absNorm(Matrix A) {
        double[] storage = A.getStorage();
        double max = 0;
        int i0 = A.getStartPosition();
        int cinc = A.getColumnIncrement();
        int nr = A.getRowsCount();
        int i1 = i0 + cinc * A.getColumnsCount();
        int j = i0 + nr;
        for (int i = i0; i < i1; i += cinc, j += cinc) {
            for (int k = i; k < j; ++k) {
                double z = storage[k];
                if (Double.isNaN(z)) {
                    return Double.NaN;
                }
                double az = Math.abs(z);
                if (az > max) {
                    max = az;
                }
            }
        }
        return max;
    }

    /**
     * Max of column abs sum
     *
     * @param A
     * @return
     */
    public double norm1(Matrix A) {
        double[] storage = A.getStorage();
        double max = 0;
        int i0 = A.getStartPosition();
        int nc = A.getColumnsCount(), cinc = A.getColumnIncrement();
        int nr = A.getRowsCount();
        int i1 = i0 + cinc * nc;
        int j = i0 + nr;
        for (int i = i0; i < i1; i += cinc, j += cinc) {
            double sum = 0;
            for (int k = i; k < j; ++k) {
                double z = storage[k];
                if (Double.isNaN(z)) {
                    return Double.NaN;
                }
                sum += Math.abs(z);
            }
            if (sum > max) {
                max = sum;
            }
        }
        return max;
    }

    /**
     * Max of row abs sum
     *
     * @param A
     * @return
     */
    public double infinityNorm(Matrix A) {
        double[] storage = A.getStorage();
        double max = 0;
        int i0 = A.getStartPosition();
        int nc = A.getColumnsCount(), cinc = A.getColumnIncrement();
        int nr = A.getRowsCount();
        int i1 = i0 + nr;
        int j = i0 + cinc * nc;
        for (int i = i0; i < i1; ++i, ++j) {
            double sum = 0;
            for (int k = i; k < j; k += cinc) {
                double z = storage[k];
                if (Double.isNaN(z)) {
                    return Double.NaN;
                }
                sum += Math.abs(z);
            }
            if (sum > max) {
                max = sum;
            }
        }
        return max;
    }

    /**
     * square root of sum of squares
     *
     * @param A
     * @return
     */
    public double frobeniusNorm(Matrix A) {
        double[] storage = A.getStorage();
        ScaledSumOfSquares sssq = new ScaledSumOfSquares();
        int i0 = A.getStartPosition();
        int cinc = A.getColumnIncrement();
        int nr = A.getRowsCount();
        int i1 = i0 + cinc * A.getColumnsCount();
        for (int i = i0; i < i1; i += cinc) {
            sssq.add(storage, i, nr, 1);
        }
        return sssq.sqrt();
    }

    public double frobeniusNorm2(Matrix A) {
        ScaledSumOfSquares sssq = new ScaledSumOfSquares();
        int n = A.getRowsCount();
        int m = A.getColumnsCount();
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < m; ++j) {
                sssq.add(A.get(i, j));
            }
        }
        return sssq.sqrt();
    }
//*> \brief \b DLANGE returns the value of the 1-norm, Frobenius norm, infinity-norm, or the largest absolute value of any element of a general rectangular matrix.
//*
//*  =========== DOCUMENTATION ===========
//*
//* Online html documentation available at
//*            http://www.netlib.org/lapack/explore-html/
//*
//*> \htmlonly
//*> Download DLANGE + dependencies
//*> <a href="http://www.netlib.org/cgi-bin/netlibfiles.tgz?format=tgz&filename=/lapack/lapack_routine/dlange.f">
//*> [TGZ]</a>
//*> <a href="http://www.netlib.org/cgi-bin/netlibfiles.zip?format=zip&filename=/lapack/lapack_routine/dlange.f">
//*> [ZIP]</a>
//*> <a href="http://www.netlib.org/cgi-bin/netlibfiles.txt?format=txt&filename=/lapack/lapack_routine/dlange.f">
//*> [TXT]</a>
//*> \endhtmlonly
//*
//*  Definition:
//*  ===========
//*
//*       DOUBLE PRECISION FUNCTION DLANGE( NORM, M, N, A, LDA, WORK )
//*
//*       .. Scalar Arguments ..
//*       CHARACTER          NORM
//*       INTEGER            LDA, M, N
//*       ..
//*       .. Array Arguments ..
//*       DOUBLE PRECISION   A( LDA, * ), WORK( * )
//*       ..
//*
//*
//*> \par Purpose:
//*  =============
//*>
//*> \verbatim
//*>
//*> DLANGE  returns the value of the one norm,  or the Frobenius norm, or
//*> the  infinity norm,  or the  element of  largest absolute value  of a
//*> real matrix A.
//*> \endverbatim
//*>
//*> \return DLANGE
//*> \verbatim
//*>
//*>    DLANGE = ( max(abs(A(i,j))), NORM = 'M' or 'm'
//*>             (
//*>             ( norm1(A),         NORM = '1', 'O' or 'o'
//*>             (
//*>             ( normI(A),         NORM = 'I' or 'i'
//*>             (
//*>             ( normF(A),         NORM = 'F', 'f', 'E' or 'e'
//*>
//*> where  norm1  denotes the  one norm of a matrix (maximum column sum),
//*> normI  denotes the  infinity norm  of a matrix  (maximum row sum) and
//*> normF  denotes the  Frobenius norm of a matrix (square root of sum of
//*> squares).  Note that  max(abs(A(i,j)))  is not a consistent matrix norm.
//*> \endverbatim
//*
//*  Arguments:
//*  ==========
//*
//*> \param[in] NORM
//*> \verbatim
//*>          NORM is CHARACTER*1
//*>          Specifies the value to be returned in DLANGE as described
//*>          above.
//*> \endverbatim
//*>
//*> \param[in] M
//*> \verbatim
//*>          M is INTEGER
//*>          The number of rows of the matrix A.  M >= 0.  When M = 0,
//*>          DLANGE is set to zero.
//*> \endverbatim
//*>
//*> \param[in] N
//*> \verbatim
//*>          N is INTEGER
//*>          The number of columns of the matrix A.  N >= 0.  When N = 0,
//*>          DLANGE is set to zero.
//*> \endverbatim
//*>
//*> \param[in] A
//*> \verbatim
//*>          A is DOUBLE PRECISION array, dimension (LDA,N)
//*>          The m by n matrix A.
//*> \endverbatim
//*>
//*> \param[in] LDA
//*> \verbatim
//*>          LDA is INTEGER
//*>          The leading dimension of the array A.  LDA >= max(M,1).
//*> \endverbatim
//*>
//*> \param[out] WORK
//*> \verbatim
//*>          WORK is DOUBLE PRECISION array, dimension (MAX(1,LWORK)),
//*>          where LWORK >= M when NORM = 'I'; otherwise, WORK is not
//*>          referenced.
//*> \endverbatim
//*
//*  Authors:
//*  ========
//*
//*> \author Univ. of Tennessee
//*> \author Univ. of California Berkeley
//*> \author Univ. of Colorado Denver
//*> \author NAG Ltd.
//*
//*> \date December 2016
//*
//*> \ingroup doubleGEauxiliary
//*
//*  =====================================================================
//      DOUBLE PRECISION FUNCTION DLANGE( NORM, M, N, A, LDA, WORK )
//*
//*  -- LAPACK auxiliary routine (version 3.7.0) --
//*  -- LAPACK is a software package provided by Univ. of Tennessee,    --
//*  -- Univ. of California Berkeley, Univ. of Colorado Denver and NAG Ltd..--
//*     December 2016
//*
//*     .. Scalar Arguments ..
//      CHARACTER          NORM
//      INTEGER            LDA, M, N
//*     ..
//*     .. Array Arguments ..
//      DOUBLE PRECISION   A( LDA, * ), WORK( * )
//*     ..
//*
//* =====================================================================
//*
//*     .. Parameters ..
//      DOUBLE PRECISION   ONE, ZERO
//      PARAMETER          ( ONE = 1.0D+0, ZERO = 0.0D+0 )
//*     ..
//*     .. Local Scalars ..
//      INTEGER            I, J
//      DOUBLE PRECISION   SCALE, SUM, VALUE, TEMP
//*     ..
//*     .. External Subroutines ..
//      EXTERNAL           DLASSQ
//*     ..
//*     .. External Functions ..
//      LOGICAL            LSAME, DISNAN
//      EXTERNAL           LSAME, DISNAN
//*     ..
//*     .. Intrinsic Functions ..
//      INTRINSIC          ABS, MIN, SQRT
//*     ..
//*     .. Executable Statements ..
//*
//      IF( MIN( M, N ).EQ.0 ) THEN
//         VALUE = ZERO
//      ELSE IF( LSAME( NORM, 'M' ) ) THEN
//*
//*        Find max(abs(A(i,j))).
//*
//         VALUE = ZERO
//         DO 20 J = 1, N
//            DO 10 I = 1, M
//               TEMP = ABS( A( I, J ) )
//               IF( VALUE.LT.TEMP .OR. DISNAN( TEMP ) ) VALUE = TEMP
//   10       CONTINUE
//   20    CONTINUE
//      ELSE IF( ( LSAME( NORM, 'O' ) ) .OR. ( NORM.EQ.'1' ) ) THEN
//*
//*        Find norm1(A).
//*
//         VALUE = ZERO
//         DO 40 J = 1, N
//            SUM = ZERO
//            DO 30 I = 1, M
//               SUM = SUM + ABS( A( I, J ) )
//   30       CONTINUE
//            IF( VALUE.LT.SUM .OR. DISNAN( SUM ) ) VALUE = SUM
//   40    CONTINUE
//      ELSE IF( LSAME( NORM, 'I' ) ) THEN
//*
//*        Find normI(A).
//*
//         DO 50 I = 1, M
//            WORK( I ) = ZERO
//   50    CONTINUE
//         DO 70 J = 1, N
//            DO 60 I = 1, M
//               WORK( I ) = WORK( I ) + ABS( A( I, J ) )
//   60       CONTINUE
//   70    CONTINUE
//         VALUE = ZERO
//         DO 80 I = 1, M
//            TEMP = WORK( I )
//            IF( VALUE.LT.TEMP .OR. DISNAN( TEMP ) ) VALUE = TEMP
//   80    CONTINUE
//      ELSE IF( ( LSAME( NORM, 'F' ) ) .OR. ( LSAME( NORM, 'E' ) ) ) THEN
//*
//*        Find normF(A).
//*
//         SCALE = ZERO
//         SUM = ONE
//         DO 90 J = 1, N
//            CALL DLASSQ( M, A( 1, J ), 1, SCALE, SUM )
//   90    CONTINUE
//         VALUE = SCALE*SQRT( SUM )
//      END IF
//*
//      DLANGE = VALUE
//      RETURN
//*
//*     End of DLANGE
//*
//      END
//    
}
