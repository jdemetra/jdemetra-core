/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.matrices.lapack;

import demetra.data.DoubleSeqCursor;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class BLAS {

    /**
     * Computes A=A+alpha*x*y'
     *
     * @param A
     * @param alpha
     * @param x
     * @param y
     */
    public void dger(FastMatrix A, double alpha, DataBlock x, DataBlock y) {
        // Quick return if possible
        if (A.isEmpty() || alpha == 0) {
            return;
        }
        // operate by columns
        DataBlockIterator columns = A.columnsIterator();
        DoubleSeqCursor.OnMutable cursor = y.cursor();
        while (columns.hasNext()) {
            double tmp = cursor.getAndNext();
            DataBlock col = columns.next();
            if (tmp != 0) {
                col.addAY(tmp * alpha, x);
            }
        }
    }

}
