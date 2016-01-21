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
package ec.tstoolkit.arima;

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Algorithm;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.maths.linearfilters.SymmetricFrequencyResponse;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.realfunctions.GridSearch;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.maths.realfunctions.IFunctionDerivatives;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.maths.realfunctions.IParametersDomain;
import ec.tstoolkit.maths.realfunctions.NumericalDerivatives;
import ec.tstoolkit.maths.realfunctions.ParametersRange;
import ec.tstoolkit.maths.realfunctions.SingleParameter;

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
    @Algorithm(entryPoint = "minimize")
    public static class Minimizer {

        private double m_min, m_x;

        /**
         *
         */
        public Minimizer() {
        }

        /**
         * Returns the variance that will made the model non invertible (= rescaled
         * minimum of the pseudo-spectrum)
         * @return The minimum of the spectrum multiplied by 2*pi. May be negative
         */
        public double getMinimum() {
            return m_min;
        }

        /**
         * Returns the frequency corresponding to the minimum.
         * @return A number in [0, PI]
         */
        public double getMinimumFrequency() {
            return m_x;
        }

        private static class SpectrumFunctionInstance implements IFunctionInstance {

            private final Spectrum spec;
            private final double pt;

            SpectrumFunctionInstance(Spectrum spec, double pt) {
                this.spec = spec;
                this.pt = pt;
            }

            @Override
            public IReadDataBlock getParameters() {
                return new SingleParameter(pt);
            }

            @Override
            public double getValue() {
                return SpectrumFunction.value(spec, pt);
            }
        }

        private static class SpectrumFunction implements IFunction {

            private final Spectrum spec;

            static double value(Spectrum s, double x) {
                double d = s.m_denom.frequencyResponse(x).getRe();
                if (Math.abs(d) < g_epsilon2) {
                    return Double.NaN;
                }
                double n = s.m_num.frequencyResponse(x).getRe();
                return n / d;
            }

            SpectrumFunction(Spectrum spec) {
                this.spec = spec;
            }

            @Override
            public IFunctionInstance evaluate(IReadDataBlock parameters) {
                return new SpectrumFunctionInstance(spec, parameters.get(0));
            }

            @Override
            public IFunctionDerivatives getDerivatives(final IFunctionInstance point) {
                return new NumericalDerivatives(this, point, true);
            }

            @Override
            public IParametersDomain getDomain() {
                return new ParametersRange(0, Math.PI, true);
            }

        }

        /**
         * Computes the minimum of the spectrum by means of a grid search
         * @param spectrum The spectrum being minimized
         */
        public void minimize(final Spectrum spectrum) {
            m_x = 0;
            m_min = Double.MAX_VALUE;
            double y = SpectrumFunction.value(spectrum, 0);
            if (!Double.isNaN(y)) {
                // evaluates at 0
                m_min = y;
                m_x = 0;
            }
            // evaluates at pi
            y = SpectrumFunction.value(spectrum, Math.PI);
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

        /**
         * Computes the minimum of the spectrum, by explicit computation of
         * the roots of the derivative. This implementation can be instable in the case
         * of complex models (MA and AR seasonal parameters)
         * @param spectrum
         */
        @Deprecated
        public void minimize2(final Spectrum spectrum) {
            SymmetricFrequencyResponse fnum = new SymmetricFrequencyResponse(spectrum.m_num),
                    fdenom = new SymmetricFrequencyResponse(spectrum.m_denom);
            double scale = fdenom.getIntegral();

            Polynomial num = fnum.getPolynomial().divide(scale).adjustDegree(),
                    denom = fdenom.getPolynomial().divide(scale).adjustDegree();
            if (num.isZero()) {
                m_min = 0;
                m_x = 0;
                return;
            }
            if (num.getDegree() == 0 && denom.getDegree() == 0) {
                m_min = num.get(0) / denom.get(0);
                m_x = 0;
                return;
            }
            m_x = 0;
            m_min = Double.MAX_VALUE;
            double y = evaluate(num, denom, 0);
            if (!Double.isNaN(y)) {
                // evaluates at pi/2 (cos pi/2 = 0)
                m_min = y;
                m_x = Math.PI / 2;
            }
            // evaluates at 0 (sin 0 = 0)
            y = evaluate(num, denom, 1);
            if (!Double.isNaN(y) && y < m_min) {
                m_min = y;
                m_x = 0;
            }
            // evaluates at pi (sin pi = 0)
            y = evaluate(num, denom, -1);
            if (!Double.isNaN(y) && y < m_min) {
                m_min = y;
                m_x = Math.PI;
            }
            // real roots in [-1, 1]
            Polynomial r = denom.getDegree() > 0 ? num.derivate().times(denom).minus(
                    num.times(denom.derivate())) : num.derivate();
            ec.tstoolkit.maths.Complex[] roots = r.roots();
            if (roots != null) {
                for (int i = 0; i < roots.length; ++i) {
                    if (Math.abs(roots[i].getIm()) < g_epsilon) {
                        double x = roots[i].getRe();
                        if (x > -1 && x < 1) {
                            y = evaluate(num, denom, x);
                            if (!Double.isNaN(y) && y < m_min) {
                                m_min = y;
                                m_x = Math.acos(x);
                            }
                        }
                    }

                }
            }
        }

        private double evaluate(Polynomial num, Polynomial denom, double x) {
            if (Math.abs(x) < g_epsilon2) {
                x = 0;
            }
            double n = num.evaluateAt(x), d = denom.evaluateAt(x);
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
    private final static double g_epsilon = 1e-6;
    private final static double g_epsilon2 = 1e-9;
    private final static double TwoPi = Math.PI * 2;
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
        double d = m_denom.frequencyResponse(freq).getRe();
        if (Math.abs(d) < g_epsilon2) {
            return Double.POSITIVE_INFINITY;
        }
        double n = m_num.frequencyResponse(freq).getRe();
        double rslt = n / d / TwoPi;
        if (rslt < 0) {
            return 0;
        }
        return rslt;
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
