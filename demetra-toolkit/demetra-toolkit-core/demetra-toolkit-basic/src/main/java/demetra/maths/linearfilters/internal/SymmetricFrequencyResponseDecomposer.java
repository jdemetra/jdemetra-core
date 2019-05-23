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
package demetra.maths.linearfilters.internal;

import demetra.design.Development;
import demetra.maths.Complex;
import demetra.maths.ComplexMath;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.maths.linearfilters.SymmetricFrequencyResponse;
import jdplus.maths.polynomials.Polynomial;
import jdplus.maths.polynomials.UnitRoots;
import demetra.maths.polynomials.internal.UnitRootsSolver;
import java.util.function.IntToDoubleFunction;


/**
 * Auxiliary class for computing the moving average process corresponding to
 * a given auto-covariance function.
 * This implementation is based on the procedure in the program SEATS and described
 * in the paper of A. Maravall:
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
class SymmetricFrequencyResponseDecomposer  {

    private double m_var;
    private BackFilter m_bf;
    private int m_freq;
    private double m_epsilon = 1e-4;
    private final double m_repsilon = 1e-1;

    /**
     *
     */
    SymmetricFrequencyResponseDecomposer() {
        m_freq = 12;
    }

    /**
     *
     * @param freq
     */
    SymmetricFrequencyResponseDecomposer(final int freq) {
        m_freq = freq;
    }

    public boolean decompose(final SymmetricFilter sf) {
        IntToDoubleFunction weights = sf.weights();
        m_bf = null;
        if (weights.applyAsDouble(0) <= 0) {
            return false;
        }
        SymmetricFrequencyResponse sfr = new SymmetricFrequencyResponse(sf);
        double var = sfr.getIntegral();
        if (var <= 0) {
            return false;
        }

        // first, we try to remove unit roots from SymmetricFilter
        Polynomial p = Polynomial.ofInternal(sf.weightsToArray());
        UnitRootsSolver urs = new UnitRootsSolver(m_freq);
        if (urs.factorize(p)) {
            UnitRoots ur = urs.getUnitRoots();

            UnitRoots sur = ur.sqrt();
            if (sur != null) {
                m_bf = new BackFilter(sur.toPolynomial());

                // to ensure the symmetry
                SymmetricFrequencyResponse sfrur = SymmetricFrequencyResponse.createFromFilter(m_bf);
                sfr = sfr.divide(sfrur);
            }
        }
        if (m_bf == null) {
            m_bf = BackFilter.ONE;
        }

        Complex[] r = sfr.roots();

        if (r != null) {
            boolean[] used = new boolean[r.length];

            int nused = 0;

            for (int i = 0; i < r.length; ++i) {
                if (!used[i]) // real root
                {
                    if (r[i].getIm() == 0) {
                        double x = r[i].getRe();
//                        if (Math.abs(x) < 1e-9) {
//                            used[i] = true;
//                            ++nused;
//                            r[i] = Complex.ONE;
//                        } else {
                            double ro = x * x - 1;
                            if (Math.abs(ro) < 1e-9) {
                                ro = 0;
                            }
                            if (ro >= 0) {
                                if (x > 0) {
                                    r[i] = Complex.cart(x + Math.sqrt(ro));
                                } else {
                                    r[i] = Complex.cart(x - Math.sqrt(ro));
                                }
                                used[i] = true;
                                ++nused;
                            } else {
//                                Complex c = Complex.cart(x, Math.sqrt(-ro));
//                           must be a double root
//                           double roots are always a problem, but we skip it
//                           by using low precision (is it enough ?)
                                // search for the "best" solution...
                                int jbest = -1;
                                double del = -1;
                                for (int j = i + 1; j < r.length; ++j) {
                                    if (!used[j] && r[j].getIm() == 0) {
                                        double dcur = Math.abs(r[j].getRe() - x);
                                        if (del < 0 || dcur < del) {
                                            del = dcur;
                                            jbest = j;
                                        }
                                    }
                                }
                                if (jbest > 0 && del < m_repsilon) {
                                    used[i] = true;
                                    used[jbest] = true;
                                    // take the (geometric) mean of re
                                    double rr=x * r[jbest].getRe();
                                    if (rr < 0){
                                        rr=0;
                                        x=0;
                                    }else{
                                        x=x > 0 ? Math.sqrt(rr): -Math.sqrt(rr);
                                    }
                                    ro=rr-1;
//                                    x = (x + r[jbest].re) / 2;
//                                    // compute a new ro
//                                    ro = x * x - 1;
                                    if (ro < 0) {
                                        ro = Math.sqrt(-ro);
                                    } else {
                                        ro = 0;   // should not happen
                                    }
                                    r[i] = Complex.cart(x, ro);
                                    r[jbest] = Complex.cart(x, -ro);
                                    nused += 2;
                                } else {
                                    m_bf = null;
                                    return false;
                                }
                            }
//                        }
                    } else {
                        Complex ro = ComplexMath.sqrt(r[i].times(r[i]).minus(1));
                        Complex c0 = r[i].plus(ro);
                        Complex c1 = r[i].minus(ro);
                        if (c1.absSquare() < c0.absSquare()) {
                            c1 = c0;
                        }
                        for (int j = i + 1; j < r.length; ++j) {
                            if (!used[j]
                                    && (r[i].conj().minus(r[j]).absSquare() < m_epsilon)) {
                                used[i] = true;
                                used[j] = true;
                                r[i] = c1;
                                r[j] = c1.conj();
                                nused += 2;
                                break;
                            }
                        }
                        if (!used[i]) {
                            m_bf = null;
                            return false;
                        }

                    }
                }
            }

            if (nused != r.length) {
                m_bf = null;
                return false;
            }
            Polynomial pr = Polynomial.fromComplexRoots(r);
            pr = pr.divide(pr.get(0));
            BackFilter bfr = new BackFilter(pr);
            m_bf = m_bf.times(bfr);

        }

        double s = 0;
        Polynomial coeff = m_bf.asPolynomial();
        for (int i = 0; i <= coeff.degree(); ++i) {
            s += coeff.get(i) * coeff.get(i);
        }
        m_var=var / s;
//        m_var = sf.getWeight(sfr.getDegree()) / m_bf.get(m_bf.getDegree());
        return true;

    }

    public BackFilter getBFilter() {
        return m_bf;
    }

    public double getFactor() {
        return m_var;
    }

    /**
     * Gets the precision used in the search of conjugate roots of the 
     * initial polynomial
     * @return A strictly positive double. 1e-4 by default.
     */
    public double getPrecision() {
        return m_epsilon;
    }

    /**
     * The algorithm will search first for some unit roots. There are specified
     * by this parameter. See the class UnitRootsSolver for further explanations.
     * @return The parameter of the UnitRootSolver used in the decomposition.
     */
    public int getStartingURValue() {
        return m_freq;
    }

    /**
     *
     * @param value
     */
    public void setPrecision(final double value) {
        m_epsilon = value;
    }

    /**
     * The algorithm will search first for some unit roots. There are specified
     * by this parameter. See the class UnitRootsSolver for further explanations.
     * @param value The parameter of the UnitRootSolver used in the decomposition.
     * 12 by default.
     */
    public void setStartingURValue(final int value) {
        m_freq = value;
    }
}
