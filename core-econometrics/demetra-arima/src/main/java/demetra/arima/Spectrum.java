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
package demetra.arima;

import demetra.data.Doubles;
import demetra.design.Development;
import demetra.design.Immutable;
import demetra.maths.linearfilters.SymmetricFrequencyResponse;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.maths.matrices.Matrix;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.functions.GridSearch;
import demetra.maths.functions.IFunction;
import demetra.maths.functions.IFunctionDerivatives;
import demetra.maths.functions.IFunctionPoint;
import demetra.maths.functions.IParametersDomain;
import demetra.maths.functions.NumericalDerivatives;
import demetra.maths.functions.ParametersRange;

/**
 * The (pseudo-)spectrum is the Fourier transform of the auto-covariance
 * generating function (extended to non-stationary models).
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@Immutable
public class Spectrum {

    /**
     * The Minimizer class searches the minimum of the spectrum. Since 2.1.0,
     * the implementation is based on a simple grid search (instead of an
     * explicit computation of the zeroes of the derivative, using the
     * "SymmetricFrequencyResponse" representation of the spectrum (i.e.
     * representation of the spectrum as a rational function in cos(freq))).
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

            SpectrumFunctionInstance(Spectrum spec, double pt) {
                this.spec = spec;
                this.pt = pt;
            }

            @Override
            public Doubles getParameters() {
                return Doubles.of(pt);
            }

            @Override
            public double getValue() {
                return value(spec, pt);
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

            @Override
            public IFunctionPoint evaluate(Doubles parameters) {
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

            GridSearch search = new GridSearch();
            search.setBounds(0, Math.PI);
            search.setMaxIter(1000);
            int nd = spectrum.m_num.getDegree() + spectrum.m_denom.getDegree();
            search.setInitialGridCount(4 * nd - 1);
            search.setConvergenceCriterion(1e-9);
            search.setPrecision(1e-7);
            if (search.minimize(new SpectrumFunction(spectrum), new SpectrumFunctionInstance(spectrum, 0.1))) {
                SpectrumFunctionInstance fmin = (SpectrumFunctionInstance) search.getResult();
                double min = fmin.getValue();
                if (min < m_min) {
                    m_min = min;
                    m_x = fmin.pt;
                }
            } else {
                minimize2(spectrum);
            }
        }


        private double evaluate(Polynomial num, Polynomial denom, double x) {
            if (Math.abs(x) < g_epsilon2) {
                x = 0;
            }
            double n = num.evaluateAt(x), d = denom.evaluateAt(x);
            // normal case: 
            if (Math.abs(d) > g_epsilon) {
                return n / d;
            } else if (Math.abs(n) > g_epsilon) {
                return Double.NaN;
            } else if (x == 0) {
                int rmax = Math.min(num.getDegree(), denom.getDegree());
                for (int i = 1; i <= rmax; ++i) {
                    double cn = num.get(i), cd = denom.get(i);
                    boolean sn = Math.abs(cn) > g_epsilon;
                    boolean sd = Math.abs(cd) > g_epsilon;
                    if (sn && sd) {
                        return cn / cd;
                    } else if (sn) {
                        return Double.NaN;
                    } else if (sd) {
                        return 0;
                    }
                }
                return Double.NaN;
            } else {
                double[] r = new double[]{1, -x};
                Polynomial R = Polynomial.of(r);
                return evaluate(num.divide(R), denom.divide(R), x);

            }
        }
    }
    private final static double g_epsilon = 1e-7;
    private final static double g_epsilon2 = 1e-9;
    private final static double TWOPI = Math.PI * 2;
    private SymmetricFilter m_num, m_denom;

    /**
     *
     */
    public Spectrum() {
    }

    /**
     *
     * @param num
     * @param denom
     */
    public Spectrum(final SymmetricFilter num, final SymmetricFilter denom) {
        m_num = num;
        m_denom = denom;
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
        double d = s.m_denom.frequencyResponse(x).getRe();
        double n = s.m_num.frequencyResponse(x).getRe();
        if (Math.abs(d) > g_epsilon2) {
            return n / d;
        } else if (Math.abs(n)< g_epsilon) { // 0/0
            for (int i = 1; i <= 10; ++i) {
                double dd = new dfr(s.m_denom, i).evaluate(x);
                double nd = new dfr(s.m_num, i).evaluate(x);
                if (Math.abs(dd) > g_epsilon2) {
                    return nd / dd;
                }
                if (Math.abs(nd) > g_epsilon) {
                    break;
                }
            }
        }
        return Double.NaN;

    }

    private static class dfr {

        final int d;
        final SymmetricFilter filter;

        dfr(SymmetricFilter filter, int d) {
            this.filter = filter;
            this.d = d;
        }

        double evaluate(double freq) {
            if (d % 2 == 0) {
                double s = 0;
                for (int i = 1; i <= filter.getDegree(); ++i) {
                    double c = i;
                    for (int j = 1; j < d; ++j) {
                        c *= i;
                    }
                    c *= Math.cos(freq * i)*filter.getWeight(i);
                    s += c;
                }
                return s;
            } else {
                double s = 0;
                for (int i = 1; i <= filter.getDegree(); ++i) {
                    double c = i;
                    for (int j = 1; j < d; ++j) {
                        c *= i;
                    }
                    c *= Math.sin(freq * i)*filter.getWeight(i);
                    s += c;
                }
                return s;
            }
        }
    }

    /**
     *
     * @return
     */
    public SymmetricFilter getDenominator() {
        return m_denom;
    }

    /**
     *
     * @return
     */
    public SymmetricFilter getNumerator() {
        return m_num;
    }

}
