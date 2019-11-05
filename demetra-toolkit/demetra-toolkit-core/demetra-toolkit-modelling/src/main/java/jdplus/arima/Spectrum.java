/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.arima;

import demetra.design.Development;
import demetra.design.Immutable;
import jdplus.maths.linearfilters.SymmetricFilter;
import jdplus.math.functions.GridSearch;
import jdplus.math.functions.IFunction;
import jdplus.math.functions.IFunctionPoint;
import jdplus.math.functions.IParametersDomain;
import jdplus.math.functions.ParametersRange;
import java.util.function.IntToDoubleFunction;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.Doubles;
import java.util.function.DoubleUnaryOperator;

/**
 * The (pseudo-)spectrum is the Fourier transform of the auto-covariance
 * generating function (extended to non-stationary models).
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@Immutable
public final class Spectrum {

    private final static double EPS = 1e-7;
    private final static double EPS2 = 1e-12;
    private final static double TWOPI = Math.PI * 2;
    private final SymmetricFilter num, denom;

    /**
     *
     * @param num
     * @param denom
     */
    public Spectrum(final SymmetricFilter num, final SymmetricFilter denom) {
        this.num = num;
        this.denom = denom;
    }

    /**
     *
     * @param freq
     * @return
     */
    public double get(final double freq) {
        double val = value(this, freq);
        if (val < 0) {
            return 0;
        }
        if (Double.isNaN(val)) {
            return Double.POSITIVE_INFINITY;
        }
        return val / TWOPI;
    }

    static double value(Spectrum s, double x) {
        double d = s.denom.realFrequencyResponse(x);
        double n = s.num.realFrequencyResponse(x);
        if (Math.abs(d) > EPS) {
            return n / d;
        } else if (Math.abs(n) < EPS) { // 0/0
            try {
                for (int i = 1; i <= 10; ++i) {
                    double dd = new dfr(s.denom, i).evaluate(x);
                    double nd = new dfr(s.num, i).evaluate(x);
                    if (Math.abs(dd) > EPS) {
                        return nd / dd;
                    }
                    if (Math.abs(nd) > EPS) {
                        break;
                    }
                }
            } catch (Exception err) {
                return Double.NaN;
            }
        }
        return Double.NaN;
    }

    public DoubleUnaryOperator asFunction() {
        return f -> get(f);
    }

    private static class dfr {

        final int d;
        final SymmetricFilter filter;

        dfr(SymmetricFilter filter, int d) {
            this.filter = filter;
            this.d = d;
        }

        double evaluate(double freq) {
            IntToDoubleFunction weights = filter.weights();
            if (d % 2 == 0) {
                double s = 0;
                for (int i = 1; i < filter.length(); ++i) {
                    double c = i;
                    for (int j = 1; j < d; ++j) {
                        c *= i;
                    }
                    c *= Math.cos(freq * i) * weights.applyAsDouble(i);
                    s += c;
                }
                return s;
            } else {
                double s = 0;
                for (int i = 1; i < filter.length(); ++i) {
                    double c = i;
                    for (int j = 1; j < d; ++j) {
                        c *= i;
                    }
                    c *= Math.sin(freq * i) * weights.applyAsDouble(i);
                    s += c;
                }
                return s;
            }
        }
    }

    private static class fr {

        final SymmetricFilter filter;
        private double f, df, d2f;

        fr(SymmetricFilter filter) {
            this.filter = filter;
        }

        void evaluate(double freq) {
            DoubleSeq weights = filter.coefficientsAsPolynomial().coefficients();
            DoubleSeqCursor cursor = weights.cursor();
            f = cursor.getAndNext();
            df = 0;
            double c1=Math.cos(freq), s1=Math.sin(freq);
            double c0=c1, s0=s1;
            for (int i = 1; i < weights.length(); ++i) {
                double w = cursor.getAndNext();
                double wc = 2 * c0 * w;
                double ws = 2 * s0 * w;
                double cnext = c0*c1-s0*s1;
                double snext=s0*c1+c0*s1;
                c0=cnext;
                s0=snext;
                f += wc;
                df -= i * ws;
                d2f -= i * i * wc;
            }
        }

        double f() {
            return f;
        }

        double df() {
            return df;
        }

        double d2f() {
            return d2f;
        }
    }

    /**
     * The Minimizer class searches the minimum of the spectrum. 
     * This implementtion searches local minima using the newton algortihm.
     * The number of starting points is defined by the degrees of the polynomials
     * The starting points are distributed in a uniform way. 
     */
    public static class Minimizer {

        private double m_min, m_x;

        /**
         *
         */
        public Minimizer() {
        }

        /**
         * Returns the variance that will made the model non invertible (=
         * rescaled minimum of the pseudo-spectrum)
         *
         * @return The minimum of the spectrum multiplied by 2*pi. May be
         * negative
         */
        public double getMinimum() {
            return m_min;
        }

        /**
         * Returns the frequency corresponding to the minimum.
         *
         * @return A number in [0, PI]
         */
        public double getMinimumFrequency() {
            return m_x;
        }

        private static class SpectrumFunctionInstance implements IFunctionPoint {

            private final Spectrum spec;
            private final double pt;
            private final double f, df, d2f;

            SpectrumFunctionInstance(Spectrum spec, double pt) {
                this.spec = spec;
                this.pt = pt;
                fr num = new fr(spec.num), denom = new fr(spec.denom);
                num.evaluate(pt);
                denom.evaluate(pt);
                double n = num.f, dn = num.df, d2n = num.d2f,
                        d = denom.f, dd = denom.df, d2d = denom.d2f;
                f = n / d;
                df = (dn * d - n * dd) / (d * d);
                d2f = ((d2n * d - n * d2d) * d - 2 * (dn * d - n * dd) * dd) / (d * d * d);
            }

            @Override
            public DoubleSeq getParameters() {
                return Doubles.of(pt);
            }

            @Override
            public double getValue() {
                return value(spec, pt);
            }

            @Override
            public IFunction getFunction() {
                return new SpectrumFunction(spec);
            }
        }

        private static class SpectrumFunction implements IFunction {

            private final Spectrum spec;
            private final double a, b;

            SpectrumFunction(Spectrum spec) {
                this.spec = spec;
                a = 0;
                b = Math.PI;
            }

            SpectrumFunction(Spectrum spec, double a, double b) {
                this.spec = spec;
                this.a = a;
                this.b = b;
            }

            SpectrumFunctionInstance min(final double start) {
                double fd = spec.denom.realFrequencyResponse(start);
                double z = start;
                if (Math.abs(fd) < EPS) {
                    do {
                        z = z + EPS;
                        fd = spec.denom.realFrequencyResponse(z);

                    } while (z <= b && Math.abs(fd) < EPS);
                    if (z > b) {
                        return null;
                    }
                }
                int iter = 0;
                SpectrumFunctionInstance cur = new SpectrumFunctionInstance(spec, z);
                double s = cur.f;
                double zcur = z, zprev = z;
                do {
                    zprev = zcur;
                    zcur -= cur.df / cur.d2f;
                    if (Double.isNaN(zcur)) {
                        break;
                    }
                    if (zcur < a) {
                        zcur = a;
                    } else if (zcur > b) {
                        zcur = b;
                    }
                    SpectrumFunctionInstance ncur = new SpectrumFunctionInstance(spec, zcur);
                    double ns = ncur.f;
                    if (ns < s) {
                        cur = ncur;
                        s = ns;
                    }
                } while (++iter < 200 && Math.abs(zcur - zprev) > EPS2);
                if (iter == 100) {
                    iter = 0;
                }
                return cur;
            }

            @Override
            public SpectrumFunctionInstance evaluate(DoubleSeq parameters) {
                return new SpectrumFunctionInstance(spec, parameters.get(0));
            }

            @Override
            public IParametersDomain getDomain() {
                return new ParametersRange(a, b, true);
            }

        }

        /**
         * Computes the minimum of the spectrum by means of a grid search
         *
         * @param spectrum The spectrum being minimized
         */
        public void minimize(final Spectrum spectrum) {
            if (spectrum.num.length() == 1 && spectrum.denom.length() == 1) {
                // constant
                m_x = 0;
                m_min = value(spectrum, 0);
                return;
            }
            m_x = 0;
            m_min = Double.MAX_VALUE;
            double y = value(spectrum, 0);
            if (!Double.isNaN(y)) {
                // evaluates at 0
                m_min = y;
                m_x = 0;
            }
            // evaluates at pi
            y = value(spectrum, Math.PI);
            if (!Double.isNaN(y) && y < m_min) {
                m_min = y;
                m_x = Math.PI;
            }
            int nd = Math.max(spectrum.num.getUpperBound(), spectrum.denom.getUpperBound())/2;
            // degree of the derivative
            double step = Math.PI / nd, a = 0 ;
            for (int i = 0; i < nd; ++i, a += step) {
                double b = a+step;
                double f = spectrum.denom.realFrequencyResponse(a);
                double na = a;
                while (f <= 0 && na < b) {
                    na += step / 7;
                    f = spectrum.denom.realFrequencyResponse(na);
                }
                if (na >= b) {
                    continue;
                }
                f = spectrum.denom.realFrequencyResponse(b);
                double nb = b;
                while (f <= 0 && nb > na) {
                    nb -= step / 7;
                    f = spectrum.denom.realFrequencyResponse(nb);
                }
                if (nb <= na) {
                    continue;
                }
                SpectrumFunction fn = new SpectrumFunction(spectrum, na, nb);
                SpectrumFunctionInstance min = fn.min((na + nb) / 2);
                if (min != null) {
                    double cmin = min.f;
                    if (cmin < m_min) {
                        m_min = cmin;
                        m_x = min.pt;
                    }
                }
            }
        }
    }

    /**
     *
     * @return
     */
    public SymmetricFilter getDenominator() {
        return denom;
    }

    /**
     *
     * @return
     */
    public SymmetricFilter getNumerator() {
        return num;
    }

}
