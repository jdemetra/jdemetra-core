/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.maths.linearfilters;

import demetra.data.DoubleSeq;
import java.util.function.DoubleUnaryOperator;
import jdplus.data.DataBlock;
import jdplus.data.analysis.DiscreteKernel;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.FastMatrix;
import jdplus.maths.matrices.SubMatrix;
import jdplus.maths.matrices.decomposition.Householder;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class HendersonFiltersTest {

    public HendersonFiltersTest() {
    }

    @Test
    public void testWeights() {
        for (int i = 3; i < 99; i += 2) {
            SymmetricFilter f = HendersonFilters.ofLength(i);
            double[] w = f.weightsToArray();
            double s = 0;
            for (int j = 0; j < w.length; ++j) {
                s += w[j];
            }
            assertEquals(s, 1, 1e-9);
        }
    }

    @Test
    public void testGain() {
        DoubleUnaryOperator gain = HendersonFilters.ofLength(23).squaredGainFunction();
        for (int i = 0; i <= 100; ++i) {
            double g = gain.applyAsDouble(i * Math.PI / 100);
//            System.out.println(gain.apply(i * Math.PI / 100));
        }
//        System.out.println("");
//        System.out.println(DataBlock.ofInternal(HendersonFilters.instance.create(13).weightsToArray()));
    }

    @Test
    public void testGeneric() {
        int n = 37;
        SymmetricFilter s = HendersonFilters.ofLength(n);
        FiniteFilter h1 = GenericHendersonFilters.make(n / 2, n / 2);
        SymmetricFilter f2 = LocalPolynomialFilters.ofDefault(n / 2, 3, DiscreteKernel.henderson(n / 2));
        SymmetricFilter f3 = LocalPolynomialFilters.ofDefault2(n / 2, 3, DiscreteKernel.henderson(n / 2));
        DoubleSeq w1 = DoubleSeq.of(s.weightsToArray());
        DoubleSeq w2 = DoubleSeq.of(h1.weightsToArray());
        DoubleSeq w3 = DoubleSeq.of(f2.weightsToArray());
        DoubleSeq w4 = DoubleSeq.of(f3.weightsToArray());
        assertTrue(w1.distance(w2) < 1e-9);
        assertTrue(w1.distance(w3) < 1e-9);
        assertTrue(w1.distance(w4) < 1e-9);
    }

    @Test
    public void testGeneric2() {
        int n = 23;
        SymmetricFilter s = HendersonFilters.ofLength(n);
        FiniteFilter h1 = GenericHendersonFilters.make(16, 6);
        FSTFilterFactory fac = new FSTFilterFactory(16, 6);

        DoubleSeq ws = DoubleSeq.of(s.weightsToArray());
        DoubleSeq w1 = DoubleSeq.of(h1.weightsToArray());
        System.out.println(ws);
        System.out.println(w1);
        for (int i = 2; i <= 30; i += 2) {
            FiniteFilter h2 = fac.make(0, 1, i * .1);
//            DoubleUnaryOperator phase = h2.phaseFunction();
//            double[] q=new double[100];
//            for (int r=0; r<q.length; ++r){
//                q[r]=phase.applyAsDouble(r*Math.PI/300);
//            }
//            DoubleSeq w2 = DoubleSeq.of(q);
            DoubleSeq w2 = DoubleSeq.of(h2.weightsToArray());
            System.out.println(w2);
        }
        s = HendersonFilters.ofLength(13);
        ws = DoubleSeq.of(s.weightsToArray());
        System.out.println(ws);
    }

    public static void main(String[] arg) {
        int K = 1000, n = 13;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            SymmetricFilter f2 = LocalPolynomialFilters.ofDefault(n / 2, 3, DiscreteKernel.henderson(n / 2));
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1-t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            SymmetricFilter f3 = LocalPolynomialFilters.ofDefault2(n / 2, 3, DiscreteKernel.henderson(n / 2));
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1-t0);
    }
}

@lombok.experimental.UtilityClass
class GenericHendersonFilters {

    /**
     * Criterion min sum(D^q(w(k))^2 under sum(w(k))=1, sum(k*w(k))=0
     *
     * @param nlags
     * @param nleads
     * @return
     */
    FiniteFilter make(int nlags, int nleads) {
        return make(nlags, nleads, 2);
    }

    /**
     * Criterion min sum(D^q(w(k))^2 under sum(w(k))=1, sum(k*w(k))=0
     *
     * @param nlags
     * @param nleads
     * @return
     */
    FiniteFilter make(int nlags, int nleads, int deg) {
        int p = deg + 1;
        int n = nlags + nleads + 1;
        CanonicalMatrix J = CanonicalMatrix.square(n + p);
        S(J.extract(0, n, 0, n));
        SubMatrix C = J.extract(n, p, 0, n);
        C.row(0).set(1);
        for (int q = 1; q <= deg; ++q) {
            final int t = q;
            C.row(q).set(k -> kpow(k - nlags, t));
        }
        J.extract(0, n, n, p).copy(C.transpose());
        DataBlock z = DataBlock.make(n + p);
        z.set(n, 1);
        Householder hous = new Householder(false);
        hous.decompose(J);
        hous.solve(z);
        return FiniteFilter.ofInternal(z.extract(0, n).toArray(), -nlags);
    }

    private static double kpow(int k, int d) {
        long z = k;
        for (int i = 1; i < d; ++i) {
            z *= k;
        }
        return z;
    }

    private double[] W = new double[]{20, -15, 6, -1};

    private void S(FastMatrix s) {
        s.diagonal().set(W[0]);
        for (int i = 1; i < W.length; ++i) {
            s.subDiagonal(i).set(W[i]);
            s.subDiagonal(-i).set(W[i]);
        }
    }
}

class FSTFilterFactory {

    private final int nlags, nleads;

    private final FidelityCriterion F = new FidelityCriterion();
    private final SmoothnessCriterion S;
    private final TimelinessCriterion T;

    FSTFilterFactory(int nlags, int nleads) {
        this.nlags = nlags;
        this.nleads = nleads;
        S = new SmoothnessCriterion(nlags, nleads);
        T = new TimelinessCriterion(nlags, nleads);
    }

    FiniteFilter make(double a, double b, double c) {
        int n = nlags + nleads + 1;
        int p = 3;
        CanonicalMatrix J = CanonicalMatrix.square(n + p);
        SubMatrix X = J.extract(0, n, 0, n);
        if (a != 0) {
            F.add(a, X);
        }
        if (b != 0) {
            S.add(b, X);
        }
        if (c != 0) {
            T.add(c, X);
        }
        SubMatrix C = J.extract(n, p, 0, n);
        C.row(0).set(1);
        for (int q = 1; q < p; ++q) {
            final int t = q;
            C.row(q).set(k -> kpow(k - nlags, t));
        }
        J.extract(0, n, n, p).copy(C.transpose());
        DataBlock z = DataBlock.make(n + p);
        z.set(n, 1);
        Householder hous = new Householder(false);
        hous.decompose(J);
        hous.solve(z);
        return FiniteFilter.ofInternal(z.extract(0, n).toArray(), -nlags);
    }

    private static double kpow(int k, int d) {
        long z = k;
        for (int i = 1; i < d; ++i) {
            z *= k;
        }
        return z;
    }

}

class FidelityCriterion {

    void add(double weight, FastMatrix X) {
        X.diagonal().add(weight);
    }
}

class SmoothnessCriterion {

    private final CanonicalMatrix S;
    private double[] W = new double[]{20, -15, 6, -1};

    public SmoothnessCriterion(int nlags, int nleads) {
        int n = nlags + nleads + 1;
        S = CanonicalMatrix.square(n);
        S.diagonal().set(W[0]);
        for (int i = 1; i < W.length; ++i) {
            S.subDiagonal(i).set(W[i]);
            S.subDiagonal(-i).set(W[i]);
        }
    }

    void add(double weight, FastMatrix X) {
        X.addAY(weight, S);
    }
}

class TimelinessCriterion {

    private final double w0, w1;

    private final CanonicalMatrix T;

    public TimelinessCriterion(int nlags, int nleads) {
        int n = 2 * Math.max(nlags, nleads) + 1;
        int m = nlags + nleads + 1;
        T = CanonicalMatrix.square(m);
        w0 = 0;
        w1 = Math.PI / 3;
        double[] sin1 = new double[n];
        double[] sin0 = new double[n];
        for (int i = 0; i < n; ++i) {
            sin1[i] = Math.sin(i * w1);
        }
        if (w0 != 0) {
            for (int i = 0; i < n; ++i) {
                sin0[i] = Math.sin(i * w0);
            }
        }
        for (int i = -nlags; i <= nleads; ++i) {
            for (int j = -nlags; j <= nleads; ++j) {
                int sum = Math.abs(i + j), diff = Math.abs(i - j);
                if (sum == 0) {
                    if (diff != 0) {
                        double dk = w1 - w0, dl = (sin1[diff] - sin0[diff]) / diff;
                        T.set(i + nlags, j + nlags, .5 * (dl - dk));
                    }
                } else if (diff == 0) {
                    double dk = (sin1[sum] - sin0[sum]) / sum, dl = w1 - w0;
                    T.set(i + nlags, j + nlags, .5 * (dl - dk));
                } else {
                    double dk = (sin1[sum] - sin0[sum]) / sum, dl = (sin1[diff] - sin0[diff]) / diff;
                    T.set(i + nlags, j + nlags, .5 * (dl - dk));
                }
            }
        }
    }

    void add(double weight, FastMatrix X) {
        X.addAY(weight, T);
    }

}
