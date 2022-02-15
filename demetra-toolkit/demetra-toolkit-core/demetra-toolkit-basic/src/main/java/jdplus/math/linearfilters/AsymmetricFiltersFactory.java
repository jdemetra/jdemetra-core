/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.linearfilters;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.linearsystem.LinearSystemSolver;
import jdplus.math.functions.NumericalIntegration;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.SymmetricMatrix;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class AsymmetricFiltersFactory {

    public static enum Option {
        Direct,
        CutAndNormalize,
        MMSRE
    }

    /**
     *
     * @param s
     * @param q
     * @param ic I/C Ratio
     * @return
     */
    public IFiniteFilter musgraveFilter(final SymmetricFilter s, final int q, double ic) {
        double r = 4 / (Math.PI * ic * ic);
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

    public IFiniteFilter[] musgraveFilters(final SymmetricFilter s, double ic) {
        int horizon = s.getUpperBound();
        IFiniteFilter[] ff = new IFiniteFilter[horizon];
        for (int i = 0, h = horizon - 1; i < horizon; ++i, --h) {
            ff[i] = musgraveFilter(s, h, ic);
        }
        return ff;
    }

    public IFiniteFilter cutAndNormalizeFilter(final SymmetricFilter s, final int q) {
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

    public IFiniteFilter[] cutAndNormalizeFilters(final SymmetricFilter s) {
        int horizon = s.getUpperBound();
        IFiniteFilter[] ff = new IFiniteFilter[horizon];
        for (int i = 0, h = horizon - 1; i < horizon; ++i, --h) {
            ff[i] = cutAndNormalizeFilter(s, h);
        }
        return ff;
    }

    @Deprecated
    public IFiniteFilter mmsreFilter2(SymmetricFilter sw, int q, int u, @NonNull double[] dz, IntToDoubleFunction k) {
        double[] w = sw.weightsToArray();
        int h = w.length / 2;
        int nv = h + q + 1;
        int deg = u + dz.length;
        DataBlock wp = DataBlock.of(w, 0, nv);
        DataBlock wf = DataBlock.of(w, nv, w.length);
        FastMatrix Z = LocalPolynomialFilters.createZ(h, deg);
        FastMatrix Zp = LocalPolynomialFilters.z(Z, -h, q, u + 1, u + dz.length);
        FastMatrix Zf = LocalPolynomialFilters.z(Z, q + 1, h, u + 1, u + dz.length);
        FastMatrix Up = LocalPolynomialFilters.z(Z, -h, q, 0, u);
        FastMatrix Uf = LocalPolynomialFilters.z(Z, q + 1, h, 0, u);
        DataBlock d = DataBlock.of(dz);

        FastMatrix H = SymmetricMatrix.XtX(Up);

        DataBlock a1 = DataBlock.make(u + 1);
        a1.product(Uf.columnsIterator(), wf); // U'f x wf

        DataBlock a2 = a1.deepClone();
        LinearSystemSolver.fastSolver().solve(H, a2);// (U'p x Up)^-1 Uf x Wf

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
        LinearSystemSolver.fastSolver().solve(H, a7);
//        hous.solve(a7);

        DataBlock a8 = DataBlock.make(nv);
        a8.product(Up.rowsIterator(), a7);

        DataBlock a9 = a5.deepClone();
        a9.sub(a8);

        DataBlock a10 = DataBlock.make(dz.length);
        a10.product(Zp.columnsIterator(), a9);
        FastMatrix C = FastMatrix.square(dz.length);
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
//        hous=new Householder(C);
//        hous.solve(a11);
        LinearSystemSolver.fastSolver().solve(C, a11);
        double s = a11.dot(d);
        a9.mul(s);

        wp.add(a9);
        wp.add(a3);

        return FiniteFilter.ofInternal(wp.toArray(), -h);
    }

    /**
     * Provides an asymmetric filter [-h, p] based on the given symmetric
     * filter. The asymmetric filter minimizes the mean square revision error
     * (mmsre) relative to the symmetric filter. The series follows the model
     * y=U*du + Z*dz + e, std(e) = sigma/ki
     *
     * See Proietti, Luati, "Real time estimation in local polynomial regression
     * with application to trend-cycle analysis".
     *
     * @param sw The symmetric filter
     * @param q The horizon of the asymmetric filter (from 0 to deg(w)/2)
     * @param u The degree of the constraints (U, the weights preserve
     * polynomials of degree at most u).
     * @param dz Coefficients of the linear model. The number of the
     * coefficients and the degree of the constraints define the type of the
     * linear model.
     * @param k The weighting factors (null for no weighting)
     * @return
     */
    public IFiniteFilter mmsreFilter(SymmetricFilter sw, int q, int u, double[] dz, IntToDoubleFunction k) {
        return mmsreFilter(sw, q, u, dz, k, 0, 0);
    }

    /**
     * Extension of the usual asymmetric filters that introduces a timeliness
     * criterion,
     * "à la Guggemos".
     * See Grun-Rehomme, Guggemos and Ladiray, "Asymmetric Moving Averages
     * Minimizing Phase-Shift"
     *
     * @param sw
     * @param q
     * @param u
     * @param dz
     * @param k
     * @param passBand
     * @param tweight
     * @return
     */
    public IFiniteFilter mmsreFilter(SymmetricFilter sw, int q, int u, double[] dz, IntToDoubleFunction k, double passBand, double tweight) {
        double[] w = sw.weightsToArray();
        int h = w.length / 2;
        int nv = h + q + 1;
        int deg = u + (dz == null ? 0 : dz.length);
        DataBlock wp = DataBlock.of(w, 0, nv);
        DataBlock wf = DataBlock.of(w, nv, w.length);
        FastMatrix Z = LocalPolynomialFilters.createZ(h, deg);
        FastMatrix Up = LocalPolynomialFilters.z(Z, -h, q, 0, u);
        FastMatrix Uf = LocalPolynomialFilters.z(Z, q + 1, h, 0, u);
        FastMatrix Q = FastMatrix.square(nv + u + 1);
        FastMatrix D = Q.extract(0, nv, 0, nv);
        D.diagonal().set(1);
        Q.extract(nv, u + 1, 0, nv).copyTranspose(Up);
        Q.extract(0, nv, nv, u + 1).copy(Up);
        DataBlock a = DataBlock.make(Q.getRowsCount());
        a.extract(nv, u + 1).product(wf, Uf.columnsIterator());
        if (dz != null && dz.length > 0) {
            DataBlock d = DataBlock.of(dz);
            FastMatrix Zp = LocalPolynomialFilters.z(Z, -h, q, u + 1, u + dz.length);
            FastMatrix Zf = LocalPolynomialFilters.z(Z, q + 1, h, u + 1, u + dz.length);
            DataBlock Yp = DataBlock.make(nv);
            DataBlockIterator cols = Zp.columnsIterator();
            DoubleSeqCursor.OnMutable cursor = d.cursor();
            while (cols.hasNext()) {
                Yp.addAY(cursor.getAndNext(), cols.next());
            }
            DataBlock Yf = DataBlock.make(wf.length());
            cols = Zf.columnsIterator();
            cursor.moveTo(0);
            while (cols.hasNext()) {
                Yf.addAY(cursor.getAndNext(), cols.next());
            }
            D.addXaXt(1, Yp);
            a.extract(0, nv).setAY(Yf.dot(wf), Yp);
        }
        if (passBand > 0 && tweight > 0) {
            FastMatrix W = buildMatrix(passBand, h, q);
            D.addAY(tweight, W);
            // we have to update a
            DataBlock row = DataBlock.of(wp);
            row.mul(-tweight);
            a.addProduct(row, W.columnsIterator());
        }
        LinearSystemSolver.fastSolver().solve(Q, a);
        wp.add(a.extract(0, nv));
        return FiniteFilter.ofInternal(wp.toArray(), -h);
    }

    private FastMatrix buildMatrix(double w, int nlags, int nleads) {
        int n = 2 * Math.max(nlags, nleads) + 1;
        int m = nlags + nleads + 1;
        FastMatrix T = FastMatrix.square(m);
        double[] sin1 = new double[n];
        for (int i = 0; i < n; ++i) {
            sin1[i] = Math.sin(i * w);
        }
        for (int i = -nlags; i <= nleads; ++i) {
            for (int j = -nlags; j <= nleads; ++j) {
                int sum = Math.abs(i + j), diff = Math.abs(i - j);
                if (sum == 0) {
                    if (diff != 0) {
                        double dk = w, dl = sin1[diff] / diff;
                        T.set(i + nlags, j + nlags, .5 * (dl - dk));
                    }
                } else if (diff == 0) {
                    double dk = sin1[sum] / sum, dl = w;
                    T.set(i + nlags, j + nlags, .5 * (dl - dk));
                } else {
                    double dk = sin1[sum] / sum, dl = sin1[diff] / diff;
                    T.set(i + nlags, j + nlags, .5 * (dl - dk));
                }
            }
        }
        return T;
    }

    public IFiniteFilter[] mmsreFilters(final SymmetricFilter s, int u, double[] dz, IntToDoubleFunction k) {
        int horizon = s.getUpperBound();
        IFiniteFilter[] ff = new IFiniteFilter[horizon];
        for (int i = 0, h = horizon - 1; i < horizon; ++i, --h) {
            ff[i] = mmsreFilter(s, h, u, dz, k);
        }
        return ff;
    }

    public IFiniteFilter[] mmsreFilters(final SymmetricFilter s, int u, double[] dz, IntToDoubleFunction k, double passBand, double tweight) {
        int horizon = s.getUpperBound();
        IFiniteFilter[] ff = new IFiniteFilter[horizon];
        for (int i = 0, h = horizon - 1; i < horizon; ++i, --h) {
            ff[i] = mmsreFilter(s, h, u, dz, k, passBand, tweight);
        }
        return ff;
    }

    /**
     * Retrieve the implicit forecasts corresponding to the asymmetric filters
     *
     * @param sfilter The underlying symmetric filter
     * @param afilters The asymmetric filters (from the longest to the
     * shortest).
     * @param x The observations, from n-h to n
     * @return The forecasts, from n+1 to n+h
     */
    public double[] implicitForecasts(IFiniteFilter sfilter, IFiniteFilter[] afilters, DoubleSeq x) {
        int h = sfilter.getUpperBound();
        if (h != afilters.length || x.length() != h + 1) {
            return null;
        }
        double[] f = new double[afilters.length];
        FastMatrix L = FastMatrix.square(h);
        IntToDoubleFunction sw = sfilter.weights();// from -h to h
        for (int i = 0; i < h; ++i) {
            IntToDoubleFunction aw = afilters[i].weights(); // from -h to h-i-1
            double q = 0;
            int j = -h;
            for (int k = 0; j <= 0; ++j, ++k) {
                q += (aw.applyAsDouble(j) - sw.applyAsDouble(j)) * x.get(k);
            }
            f[i] = q;
            for (; j <= h - i - 1; ++j) {
                L.set(i, j - 1, sw.applyAsDouble(j) - aw.applyAsDouble(j));
            }
            for (; j <= h; ++j) {
                L.set(i, j - 1, sw.applyAsDouble(j));
            }
        }
//        Householder hous = new Householder(L);
//        hous.solve(DataBlock.of(f));
        LinearSystemSolver.fastSolver().solve(L, DataBlock.of(f));
        return f;
    }

    private final double Q = 1 / (2 * Math.PI);

    public static interface Distance {

        double compute(SymmetricFilter sf, FiniteFilter af);
    }

    public Distance frequencyResponseDistance(@lombok.NonNull DoubleUnaryOperator spectralDensity) {
        return (SymmetricFilter sf, FiniteFilter af) -> {
            DoubleUnaryOperator fn = x -> spectralDensity.applyAsDouble(x) * sf.frequencyResponse(x).squareDistance(af.frequencyResponse(x));
            return 2 * NumericalIntegration.integrate(fn, 0, Math.PI);
        };
    }

    public Distance frequencyResponseDistance() {

        return frequencyResponseDistance(x -> Q);
    }

    public Distance accuracyDistance(@lombok.NonNull DoubleUnaryOperator spectralDensity, double passBand) {
        return (SymmetricFilter sf, FiniteFilter af) -> {
            DoubleUnaryOperator fn = x -> {
                double y = sf.gainFunction().applyAsDouble(x) - af.gainFunction().applyAsDouble(x);
                return y * y * spectralDensity.applyAsDouble(x);
            };
            return 2 * NumericalIntegration.integrate(fn, 0, passBand);
        };
    }

    public Distance accuracyDistance(SymmetricFilter sf, FiniteFilter af, double passBand) {
        return accuracyDistance(x -> Q, passBand);
    }

    public Distance smoothnessDistance(DoubleUnaryOperator spectralDensity, double passBand) {
        return (SymmetricFilter sf, FiniteFilter af) -> {
            DoubleUnaryOperator fn = x -> {
                double y = sf.gainFunction().applyAsDouble(x) - af.gainFunction().applyAsDouble(x);
                return y * y * spectralDensity.applyAsDouble(x);
            };
            return 2 * NumericalIntegration.integrate(fn, passBand, Math.PI);
        };
    }

    public Distance smoothnessDistance(double passBand) {
        return smoothnessDistance(x -> Q, passBand);
    }

    public Distance timelinessDistance(DoubleUnaryOperator spectralDensity, double passBand) {
        return (SymmetricFilter sf, FiniteFilter af) -> {
            DoubleUnaryOperator fn = x -> {
                double g = Math.abs(sf.realFrequencyResponse(x));
                double ga = af.frequencyResponse(x).abs();
                double s = Math.sin(af.frequencyResponse(x).arg() / 2);
                return g * ga * s * s * spectralDensity.applyAsDouble(x);
            };
            return 8 * NumericalIntegration.integrate(fn, 0, passBand);
        };
    }

    public Distance timelinessDistance(double passBand) {
        return timelinessDistance(x -> Q, passBand);
    }

    public Distance timelinessDistance2(DoubleUnaryOperator spectralDensity, double a, double b) {
        return (SymmetricFilter sf, FiniteFilter af) -> {
            DoubleUnaryOperator fn = x -> {
                double p = af.frequencyResponse(x).arg();
                return x == 0 ? 0 : Math.abs(p / x) * spectralDensity.applyAsDouble(x);
            };
            return 8 * NumericalIntegration.integrate(fn, a, b);
        };
    }

    public Distance timelinessDistance2(double a, double b) {
        return timelinessDistance2(x -> Q, a, b);
    }

}
