/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixException;

/**
 * DGEHRD reduces a real general matrix A to upper Hessenberg form H by an
 * orthogonal similarity transformation: Q' * A * Q = H
 *
 * @author Jean Palate
 */
public class DGEHRD {
    
    private double[] tau;
    
    

    /**
     * @param A On entry, the N-by-N general matrix to be reduced. On exit, the
     * upper triangle and the first subdiagonal of A are overwritten with the
     * upper Hessenberg matrix H, and the elements below the first subdiagonal,
     * with the array TAU, represent the orthogonal matrix Q as a product of
     * elementary reflectors. See details.
     * @param low See high
     * @param high It is assumed that A is already upper triangular in rows and
     * columns [0, low[ and [High, n[. low and high are normally set by a
     * previous call to DGEBAL; otherwise they should be set to 0 and n
     * respectively. 0 <= low <= high <= n
     */
    public void process(Matrix A, int low, int high) {
        if (! A.isSquare())
            throw new MatrixException(MatrixException.DIM);
        int n=A.getColumnsCount();
        tau=new double[n];
        // number of columns being treated
        int nh = high - low;
        if (nh <= 1) {
            return;
        }

//  254       nh = ihi - ilo + 1
//  255       IF( nh.LE.1 ) THEN
//  256          work( 1 ) = 1
//  257          RETURN
//  258       END IF
//  259 *
//  260 *     Determine the block size
//  261 *
//  262       nb = min( nbmax, ilaenv( 1, 'DGEHRD', ' ', n, ilo, ihi, -1 ) )
//  263       nbmin = 2
//  264       IF( nb.GT.1 .AND. nb.LT.nh ) THEN
//  265 *
//  266 *        Determine when to cross over from blocked to unblocked code
//  267 *        (last block is always handled by unblocked code)
//  268 *
//  269          nx = max( nb, ilaenv( 3, 'DGEHRD', ' ', n, ilo, ihi, -1 ) )
//  270          IF( nx.LT.nh ) THEN
//  271 *
//  272 *           Determine if workspace is large enough for blocked code
//  273 *
//  274             IF( lwork.LT.n*nb+tsize ) THEN
//  275 *
//  276 *              Not enough workspace to use optimal NB:  determine the
//  277 *              minimum value of NB, and reduce NB or force use of
//  278 *              unblocked code
//  279 *
//  280                nbmin = max( 2, ilaenv( 2, 'DGEHRD', ' ', n, ilo, ihi,
//  281      $                 -1 ) )
//  282                IF( lwork.GE.(n*nbmin + tsize) ) THEN
//  283                   nb = (lwork-tsize) / n
//  284                ELSE
//  285                   nb = 1
//  286                END IF
//  287             END IF
//  288          END IF
//  289       END IF
//  290       ldwork = n
//  291 *
//  292       IF( nb.LT.nbmin .OR. nb.GE.nh ) THEN
//  293 *
//  294 *        Use unblocked code below
//  295 *
//  296          i = ilo
//  297 *
//  298       ELSE
//  299 *
//  300 *        Use blocked code
//  301 *
//  302          iwt = 1 + n*nb
//  303          DO 40 i = ilo, ihi - 1 - nx, nb
//  304             ib = min( nb, ihi-i )
//  305 *
//  306 *           Reduce columns i:i+ib-1 to Hessenberg form, returning the
//  307 *           matrices V and T of the block reflector H = I - V*T*V**T
//  308 *           which performs the reduction, and also the matrix Y = A*V*T
//  309 *
//  310             CALL dlahr2( ihi, i, ib, a( 1, i ), lda, tau( i ),
//  311      $                   work( iwt ), ldt, work, ldwork )
//  312 *
//  313 *           Apply the block reflector H to A(1:ihi,i+ib:ihi) from the
//  314 *           right, computing  A := A - Y * V**T. V(i+ib,ib-1) must be set
//  315 *           to 1
//  316 *
//  317             ei = a( i+ib, i+ib-1 )
//  318             a( i+ib, i+ib-1 ) = one
//  319             CALL dgemm( 'No transpose', 'Transpose',
//  320      $                  ihi, ihi-i-ib+1,
//  321      $                  ib, -one, work, ldwork, a( i+ib, i ), lda, one,
//  322      $                  a( 1, i+ib ), lda )
//  323             a( i+ib, i+ib-1 ) = ei
//  324 *
//  325 *           Apply the block reflector H to A(1:i,i+1:i+ib-1) from the
//  326 *           right
//  327 *
//  328             CALL dtrmm( 'Right', 'Lower', 'Transpose',
//  329      $                  'Unit', i, ib-1,
//  330      $                  one, a( i+1, i ), lda, work, ldwork )
//  331             DO 30 j = 0, ib-2
//  332                CALL daxpy( i, -one, work( ldwork*j+1 ), 1,
//  333      $                     a( 1, i+j+1 ), 1 )
//  334    30       CONTINUE
//  335 *
//  336 *           Apply the block reflector H to A(i+1:ihi,i+ib:n) from the
//  337 *           left
//  338 *
//  339             CALL dlarfb( 'Left', 'Transpose', 'Forward',
//  340      $                   'Columnwise',
//  341      $                   ihi-i, n-i-ib+1, ib, a( i+1, i ), lda,
//  342      $                   work( iwt ), ldt, a( i+1, i+ib ), lda,
//  343      $                   work, ldwork )
//  344    40    CONTINUE
//  345       END IF
//  346 *
//  347 *     Use unblocked code to reduce the rest of the matrix
//  348 *
//  349       CALL dgehd2( n, i, ihi, a, lda, tau, work, iinfo )
//  350       work( 1 ) = lwkopt
//  351 *
//  352       RETURN
//  353 *
    }

    /**
     * @return the tau
     */
    public double[] getTau() {
        return tau;
    }
}
