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
import jdplus.stats.Kernels;

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
        CanonicalMatrix H = CanonicalMatrix.square(k);
        H.set((i, j) -> kernel.moment(q + i + j));
        return H;
    }

    public CanonicalMatrix hankel(Kernel kernel, int k) {
        CanonicalMatrix H = CanonicalMatrix.square(k);
        H.set((i, j) -> kernel.moment(i + j));
        return H;
    }

    public DoubleUnaryOperator kernel(Kernel kernel, int r) {
        CanonicalMatrix Hk1 = hankel(kernel, 0, r + 1);
        double detHk1 = SymmetricMatrix.determinant(Hk1);
        DoubleUnaryOperator f0 = kernel.asFunction();
        DataBlock row = Hk1.row(0);
        row.set(0, 1);
        boolean pos = r % 2 != 0;
        double q = pos ? detHk1 : -detHk1;
        return x -> {

            double cur = 1;
            for (int j = 1; j <= r; ++j) {
                cur *= x;
                row.set(j, cur);
            }
            double detHx = FastMatrix.determinant(Hk1);
            return (detHx / q) * f0.applyAsDouble(x);
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

    public Polynomial p(Kernel kernel, int r) {
        Polynomial q = Polynomial.ONE;
        for (int i = 1; i <= r; ++i) {
            Polynomial pcur = pk(kernel, i);
            double p0 = pcur.evaluateAt(0);
            if (p0 != 0) {
                q = q.plus(pcur.times(p0));
            }
        }
        return q;
    }

    public Polynomial fastP(Kernel kernel, int r) {
        CanonicalMatrix Hk1 = hankel(kernel, 0, r + 1);
        double detHk1 = SymmetricMatrix.determinant(Hk1);
        boolean pos = r % 2 == 0;
        double[] c = new double[r + 1];
        CanonicalMatrix m = CanonicalMatrix.square(r);
        for (int i = 0; i <= r; ++i) {
            suppress(0, i, Hk1, m);
            double cur = FastMatrix.determinant(m) / detHk1;
            c[i] = pos ? cur : -cur;
            pos = !pos;
        }
        return Polynomial.ofInternal(c);
    }

    public Polynomial pk(Kernel kernel, int r) {
        CanonicalMatrix Hk0 = hankel(kernel, 0, r);
        double detHk0 = SymmetricMatrix.determinant(Hk0);
        CanonicalMatrix Hk1 = hankel(kernel, 0, r + 1);
        double detHk1 = SymmetricMatrix.determinant(Hk1);
        double q = Math.sqrt(detHk0 * detHk1);
        double[] c = new double[r + 1];
        CanonicalMatrix m = CanonicalMatrix.square(r);
        boolean pos = r % 2 == 0;
        for (int i = 0; i <= r; ++i) {
            suppress(r, i, Hk1, m);
            double cur = FastMatrix.determinant(m) / q;
            c[i] = pos ? cur : -cur;
            pos = !pos;
        }
        return Polynomial.ofInternal(c);
    }

    /**
     * k-Kernel
     *
     * @param k
     * @return
     */
    public Polynomial biweightKernel(int k) {
        Polynomial pk = pk(Kernels.BIWEIGHT, k - 1);
        return pk.times(Kernels.biWeightAsPolynomial());
    }

    public Polynomial truncatedBiweightKernel(int k, double q) {
        Polynomial K = biweightKernel(k);
        double w = K.integrate(-1, q);
        return K.divide(w);
    }

    public Polynomial triweightKernel(int k) {
        Polynomial pk = pk(Kernels.TRIWEIGHT, k - 1);
        return pk.times(Kernels.triWeightAsPolynomial());
    }

    public Polynomial truncatedTriweightKernel(int k, double q) {
        Polynomial K = triweightKernel(k);
        double w = K.integrate(-1, q);
        return K.divide(w);
    }

    /**
     * 
     * @param k
     * @param m Length of the Henderson filter (from -m to +m)
     * @return 
     */
    public Polynomial hendersonKernel(int k, int m) {
        Polynomial pk = pk(Kernels.henderson(m), k - 1);
        return pk.times(Kernels.hendersonAsPolynomial(m));
    }

    public Polynomial truncatedHendersonKernel(int k, int m, double q) {
        Polynomial K = hendersonKernel(k, m);
        double w = K.integrate(-1, q);
        return K.divide(w);
    }
}
