/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.rkhs;

import demetra.data.DoubleSeqCursor;
import java.util.function.DoubleUnaryOperator;
import jdplus.data.DataBlock;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.FastMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import jdplus.maths.polynomials.Polynomial;
import jdplus.stats.Kernel;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class HighOrderKernels {

    /**
     * Returns the Hankel matrix of order k, built on the moments q,q+1... of
     * the given kernel
     *
     * @param kernel
     * @param q
     * @param k
     * @return
     */
    public CanonicalMatrix hankel(Kernel kernel, int q, int k) {
        CanonicalMatrix H = CanonicalMatrix.square(k-1);
        H.set((i, j) -> kernel.moment(q + i + j));
        return H;
    }
    
    public DoubleUnaryOperator kernel(Kernel kernel, int k) {
        CanonicalMatrix Hk1 = hankel(kernel, 0, k + 1);
        double detHk1 = SymmetricMatrix.determinant(Hk1);
        DoubleUnaryOperator f0 = kernel.asFunction();
        DataBlock row = Hk1.row(0);
        row.set(0, 1);
        return x -> {
            double cur = 1;
            for (int j = 1; j < k; ++j) {
                cur *= x;
                row.set(j, cur);
            }
            double detHx = FastMatrix.determinant(Hk1);
            return detHx / detHk1 * f0.applyAsDouble(x);
        };
    }
    
    private void suppress(int row, int column, CanonicalMatrix all, CanonicalMatrix t) {
        int k = all.getColumnsCount();
        for (int c = 0, tc = 0; c < k; ++c) {
            if (c != column) {
                DataBlock cur = all.column(c);
                DataBlock tcur = t.column(tc++);
                DoubleSeqCursor.OnMutable cursor = cur.cursor();
                DoubleSeqCursor.OnMutable tcursor = tcur.cursor();
                for (int r = 0; r < k; ++r) {
                    if (r != row) {
                        tcursor.setAndNext(cursor.getAndNext());
                    } else {
                        cursor.skip(1);
                    }
                }                
            }
        }
    }
    
    public Polynomial p(Kernel kernel, int k) {
        CanonicalMatrix Hk1 = hankel(kernel, 0, k + 1);
        double detHk1 = SymmetricMatrix.determinant(Hk1);
        double[] c = new double[k];
        CanonicalMatrix m = CanonicalMatrix.square(k - 1);
        boolean pos=true;
        for (int i = 0; i < k; ++i) {
            suppress(0, i, Hk1, m);
            double cur=FastMatrix.determinant(m)/detHk1;
            c[i]=pos ? cur : -cur;
            pos=!pos;
        }
        return Polynomial.ofInternal(c);
    }
    
}
