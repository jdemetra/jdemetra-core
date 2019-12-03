/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.math.matrices.Matrix;

/**
 * LARF applies an elementary reflector to a general rectangular matrix.
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class LARF {

    public void apply(int n, HouseholderReflector hous, CPointer x) {
        if (hous.tau == 0) {
            return;
        }
        // Hx = x - tau * (1, v)' (1, v)*x
        DataPointer h = hous.x();
        double xh = h.dot(n, x);
        x.addAX(n, -hous.tau * xh, h);
    }

    public void apply(int n, HouseholderReflector hous, RPointer x) {
        if (hous.tau == 0) {
            return;
        }
        // Hx = x - tau * (1, v)' (1, v)*x
        DataPointer h = hous.x();
        double xh = h.dot(n, x);
        x.addAX(n, -hous.tau * xh, h);
    }

    /**
     * HX
     * @param hous
     * @param X 
     */
    public void lapply(HouseholderReflector hous, Matrix X) {
        int m = X.getRowsCount(), n = X.getColumnsCount(), lda = X.getColumnIncrement();
        CPointer col = new CPointer(X.getStorage(), X.getStartPosition());
        DataPointer h = hous.x();
        for (int i = 0; i < n; ++i, col.pos += lda) {
            double xh = h.dot(m, col);
            col.addAX(m, -hous.tau * xh, h);
        }
    }

   /**
     * XH
     * @param hous
     * @param X 
     */
    public void rapply(HouseholderReflector hous, Matrix X) {
        int m = X.getRowsCount(), n = X.getColumnsCount(), lda = X.getColumnIncrement();
        RPointer row = new RPointer(X.getStorage(), X.getStartPosition(), lda);
        DataPointer h = hous.x();
        for (int i = 0; i < m; ++i, ++row.pos) {
            double xh = h.dot(n, row);
            row.addAX(n, -hous.tau * xh, h);
        }
    }
    
}
