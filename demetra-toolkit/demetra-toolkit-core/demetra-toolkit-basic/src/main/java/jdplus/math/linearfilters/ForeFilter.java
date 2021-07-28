/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.math.linearfilters;

import nbbrd.design.Development;
import nbbrd.design.Immutable;
import demetra.math.Complex;
import jdplus.math.polynomials.Polynomial;
import java.util.function.IntToDoubleFunction;
import jdplus.math.polynomials.RootsSolver;
import jdplus.math.polynomials.PolynomialException;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public final class ForeFilter implements IFiniteFilter {

    /**
     *
     */
    public static final ForeFilter ZERO = new ForeFilter(Polynomial.ZERO);

    /**
     *
     */
    public static final ForeFilter ONE = new ForeFilter(Polynomial.ONE);

    /**
     *
     * @param d
     * @param l
     * @return
     */
    public static ForeFilter add(final double d, final ForeFilter l) {
        Polynomial p = l.polynomial.plus(d);
        return new ForeFilter(p);
    }

    /**
     *
     * @param d
     * @param l
     * @return
     */
    public static ForeFilter multiply(final double d, final ForeFilter l) {
        Polynomial p = l.polynomial.times(d);
        return new ForeFilter(p);
    }

    private final Polynomial polynomial;

    /**
     * Create a new BackFilter from the specified coefficients.<br>
     * Note that a cached one can be returned if available (ONE, ZERO, ...)
     *
     * @param coefficients
     * @return
     */
    public static ForeFilter ofInternal(double[] coefficients) {
        if (coefficients.length == 1) {
            if (coefficients[0] == 1.0) {
                return ForeFilter.ONE;
            } else if (coefficients[0] == 0.0) {
                return ForeFilter.ZERO;
            }
        }
        return new ForeFilter(Polynomial.ofInternal(coefficients));
    }

    /**
     *
     * @param p
     */
    public ForeFilter(final Polynomial p) {
        polynomial = p;
    }

    /**
     *
     * @param r
     * @return
     */
    public ForeFilter divide(final ForeFilter r) {
        Polynomial.Division div = Polynomial.divide(polynomial, r.polynomial);
        if (!div.getRemainder().isZero()) {
            throw new PolynomialException(PolynomialException.DIVISION);
        }
        return new ForeFilter(div.getQuotient());
    }

    /**
     *
     * @param idx
     * @return
     */
    public double get(final int idx) {
        return polynomial.get(idx);
    }

    public Polynomial getPolynomial() {
        return polynomial;
    }

    /**
     *
     * @return
     */
    public int getDegree() {
        return polynomial.degree();
    }

    /**
     *
     * @return
     */
    @Override
    public int length() {
        return polynomial.degree() + 1;
    }

    /**
     *
     * @return
     */
    @Override
    public int getLowerBound() {
        return 0;
    }

    @Override
    public int getUpperBound() {
        return polynomial.degree();
    }

    @Override
    public Complex frequencyResponse(final double freq) {
        return FilterUtility.frequencyResponse(i -> polynomial.get(i), 0, polynomial.degree(), freq);
    }

    /**
     *
     * @return
     */
    @Override
    public IntToDoubleFunction weights() {
        return i -> polynomial.get(i);
    }

    public Polynomial asPolynomial() {
        return polynomial;
    }

    /**
     *
     * @return
     */
    public boolean isIdentity() {
        return polynomial.isIdentity();
    }

    /**
     *
     * @return
     */
    public boolean isNull() {
        return polynomial.isZero();
    }

    /**
     *
     * @param d
     * @return
     */
    public ForeFilter minus(final double d) {
        Polynomial p = polynomial.minus(d);
        return new ForeFilter(p);
    }

    /**
     *
     * @param r
     * @return
     */
    public ForeFilter minus(final ForeFilter r) {
        Polynomial p = polynomial.minus(r.polynomial);
        return new ForeFilter(p);
    }

    /**
     *
     * @return
     */
    @Override
    public BackFilter mirror() {
        return new BackFilter(polynomial);
    }

    /**
     *
     * @return
     */
    public ForeFilter negate() {
        Polynomial p = polynomial.negate();
        return new ForeFilter(p);
    }

    /**
     *
     * @return
     */
    public ForeFilter normalize() {
        double r = polynomial.get(0);
        if (r == 0 || r == 1) {
            return this;
        } else {
            return new ForeFilter(polynomial.times(1 / r));
        }
    }

    /**
     *
     * @param d
     * @return
     */
    public ForeFilter plus(final double d) {
        Polynomial p = polynomial.plus(d);
        return new ForeFilter(p);
    }

    /**
     *
     * @param r
     * @return
     */
    public ForeFilter plus(final ForeFilter r) {
        Polynomial p = polynomial.plus(r.polynomial);
        return new ForeFilter(p);
    }

    /**
     *
     * @return
     */
    public Complex[] roots() {
        return polynomial.roots();
    }

    /**
     *
     * @param searcher
     * @return
     */
    public Complex[] roots(final RootsSolver searcher) {
        return polynomial.roots(searcher);
    }

    /**
     *
     * @param d
     * @return
     */
    public ForeFilter times(final double d) {
        Polynomial p = polynomial.times(d);
        return new ForeFilter(p);
    }

    /**
     *
     * @param r
     * @return
     */
    public ForeFilter times(final ForeFilter r) {
        Polynomial p = polynomial.times(r.polynomial);
        return new ForeFilter(p);
    }

    @Override
    public String toString() {
        return polynomial.toString('F', true);
    }

//    /**
//     *
//     */
//    public static class StationaryTransformation {
//
//        /**
//         *
//         */
//        /**
//         *
//         */
//        public ForeFilter unitRoots, stationaryFilter;
//
//        private int freq;
//
//        /**
//         *
//         */
//        public StationaryTransformation() {
//            this.freq = 0;
//        }
//
//        /**
//         *
//         * @param freq
//         */
//        public StationaryTransformation(int freq) {
//            this.freq = freq;
//        }
//
//        /**
//         *
//         * @param f
//         * @return
//         */
//        public boolean transform(ForeFilter f) {
//            UnitRootsSolver urs = freq == 0 ? new UnitRootsSolver()
//                    : new UnitRootsSolver(freq);
//            urs.factorize(f.polynomial);
//            unitRoots = new ForeFilter(urs.getUnitRoots().toPolynomial());
//            if (unitRoots.getDegree() == 0) {
//                stationaryFilter = f;
//                return false;
//            } else {
//                stationaryFilter = new ForeFilter(urs.remainder());
//                return true;
//            }
//        }
//    }
//
}
