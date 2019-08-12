/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.linearfilters;

import java.util.function.IntToDoubleFunction;
import jdplus.data.DataBlock;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.FastMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import jdplus.maths.matrices.decomposition.Householder;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class AsymmetricFilters {

    public FiniteFilter musgraveFilter(final SymmetricFilter s, final int q, double r) {
        double[] h = s.weightsToArray();
        int n = s.length();
        int l = (n - 1) / 2;
        int m = l + q + 1;
        double[] c = new double[m];

        for (int i = 0; i < m; i++) {
            c[i] = h[i];
            double p1 = 0.0, p2 = 0.0;
            for (int j = m; j < n; j++) {
                p1 += h[j];
                p2 += h[j] * ((j + 1) - (m + 1) / 2.0);
            }
            p1 /= m;
            p2 *= (((i + 1) - (m + 1) / 2.0) * r / (1 + (m * (m - 1) * (m + 1)
                    * r * (1.0 / 12.0))));
            c[i] += (p1 + p2);
        }
        return FiniteFilter.ofInternal(c, -l);
    }

    public FiniteFilter cutAndNormalizeFilter(final SymmetricFilter s, final int q) {
        IntToDoubleFunction weights = s.weights();
        int l = s.getLowerBound();
        double[] w = new double[q - l + 1];
        double n = 0;
        for (int i = 0; i < w.length; ++i) {
            w[i] = weights.applyAsDouble(l + i);
            n += w[i];
        }
        for (int i = 0; i < w.length; ++i) {
            w[i] /= n;
        }
        return FiniteFilter.ofInternal(w, l);
    }

    /**
     * Provides an asymmetric filter [-h, p] based on the given symmetric
     * filter. The asymmetric filter minimizes the mean square revision error
     * (mmsre) relative to the symmetric filter. The series follows the model
     * y=U*du + Z*dz + e, std(e) = sigma/ki
     *
     * See Proietti, Luati, "Real time estimation in local polynomial regression
     * with application to trend-cycle analysis.
     *
     * @param sw The symmetric filter
     * @param q The horizon of the asymmetric filter (from 0 to deg(w)/2)
     * @param u The degree of the constraints (U)
     * @param dz Coefficients of the linear model
     * @param k The weighting factors (null for no weighting)
     * @return
     */
    public FiniteFilter mmsreFilter(SymmetricFilter sw, int q, int u, double[] dz, IntToDoubleFunction k) {
        double[] w = sw.weightsToArray();
        int h = w.length / 2;
        int nv = h + q + 1;
        DataBlock wp = DataBlock.of(w, 0, nv);
        DataBlock wf = DataBlock.of(w, nv, w.length);
        FastMatrix Zp = LocalPolynomialFilters.z(-h, q, u + 1, u + dz.length);
        FastMatrix Zf = LocalPolynomialFilters.z(q + 1, h, u + 1, u + dz.length);
        FastMatrix Up = LocalPolynomialFilters.z(-h, q, 0, u);
        FastMatrix Uf = LocalPolynomialFilters.z(q + 1, h, 0, u);
        DataBlock d = DataBlock.of(dz);

        FastMatrix H = SymmetricMatrix.XtX(Up);

        DataBlock a1 = DataBlock.make(u + 1);
        a1.product(Uf.columnsIterator(), wf); // U'f x wf

        DataBlock a2 = a1.deepClone();
        Householder hous = new Householder();
        hous.decompose(H);

        hous.solve(a2); // (U'p x Up)^-1 Uf x Wf

        DataBlock a3 = DataBlock.make(nv);
        a3.product(Up.rowsIterator(), a2); // Up x (U'p x Up)^-1 Uf x Wf

        DataBlock a4 = DataBlock.make(dz.length);
        a4.product(Zp.columnsIterator(), a3);
        a4.chs();
        a4.addProduct(Zf.columnsIterator(), wf); // (Zf' - Zp' x  Up x (U'p x Up)^-1 Uf ) Wf

        DataBlock a5 = DataBlock.make(nv);
        a5.product(Zp.rowsIterator(), d); // Zp x d

        DataBlock a6 = DataBlock.make(u + 1);
        a6.product(Up.columnsIterator(), a5); // Up' x Zp x d

        DataBlock a7 = a6.deepClone();
        hous.solve(a7);

        DataBlock a8 = DataBlock.make(nv);
        a8.product(Up.rowsIterator(), a7);

        DataBlock a9 = a5.deepClone();
        a9.sub(a8);

        DataBlock a10 = DataBlock.make(dz.length);
        a10.product(Zp.columnsIterator(), a9);
        CanonicalMatrix C = CanonicalMatrix.square(dz.length);
        for (int i = 0; i < dz.length; ++i) {
            for (int j = 0; j < dz.length; ++j) {
                double x = a10.get(i) * dz[j];
                if (i == j) {
                    x += 1;
                }
                C.set(i, j, x);
            }
        }
        DataBlock a11 = a4.deepClone();
        hous.decompose(C);
        hous.solve(a11);

        double s = a11.dot(d);
        a9.mul(s);

        wp.add(a9);
        wp.add(a3);

        return FiniteFilter.ofInternal(wp.toArray(), -h);
    }

}
