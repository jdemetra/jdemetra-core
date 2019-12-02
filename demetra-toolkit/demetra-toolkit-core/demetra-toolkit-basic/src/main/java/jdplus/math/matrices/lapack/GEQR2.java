/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.math.matrices.Matrix;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class GEQR2 {
    
    /**
     * QR decomposition of A.
     * The matrix Q is represented as a product of elementary reflectors
     * Q = H(1) H(2) . . . H(k), where k = min(m,n).
     * 
     * Each H(i) has the form
     * 
     * H(i) = I - tau * v * v**T
     * 
     * where tau is a real scalar, and v is a real vector with
     * v(0:i-1) = 0 and v(i) = 1; v(i+1:m) is stored on exit in A(i+1:m,i),
     * and tau in TAU(i).
     * 
     * @param A On entry, the m by n matrix A. On exit, the elements on and 
     * above the diagonal of the Matrix contain the min(m,n) by n upper 
     * trapezoidal matrix R (R is upper triangular if m >= n); 
     * the elements below the diagonal, with the array tAU, represent the 
     * orthogonal matrix Q as a product of elementary reflectors.
     * 
     * @param tau The scalar factors of the elementary reflectors (out parameter).
     * The size of tau must be equal to the number of columns of A (n)
     */
    public void apply(Matrix A, double[] tau){
        int m=A.getRowsCount(), n=A.getColumnsCount(), lda=A.getColumnIncrement(),
                start=A.getStartPosition();
        int k=Math.min(m,n);
        HouseholderReflector hous=new HouseholderReflector(A.getStorage(),1);
        for (int i=0, j=start; i<k; ++i, j+=lda+1){
            hous.set(j, m-i);
            LARFG.apply(hous);
            tau[i]=hous.tau;
        }
    }
    
//*
//      DO 10 I = 1, K
//*
//*        Generate elementary reflector H(i) to annihilate A(i+1:m,i)
//*
//         CALL DLARFG( M-I+1, A( I, I ), A( MIN( I+1, M ), I ), 1,
//     $                TAU( I ) )
//         IF( I.LT.N ) THEN
//*
//*           Apply H(i) to A(i:m,i+1:n) from the left
//*
//            AII = A( I, I )
//            A( I, I ) = ONE
//            CALL DLARF( 'Left', M-I+1, N-I, A( I, I ), 1, TAU( I ),
//     $                  A( I, I+1 ), LDA, WORK )
//            A( I, I ) = AII
//         END IF
//   10 CONTINUE
//      RETURN
//*
//*     End of DGEQR2
//*
//      END
    
}
