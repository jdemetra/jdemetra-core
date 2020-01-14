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
package jdplus.math.linearfilters.internal;

import demetra.design.Development;
import demetra.math.Complex;
import jdplus.math.ComplexUtility;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.math.polynomials.LeastSquaresDivision;
import jdplus.math.polynomials.Polynomial;
import jdplus.math.polynomials.UnitRoots;
import jdplus.math.polynomials.UnitRootsSolver;
import java.util.function.IntToDoubleFunction;


/**
 * Auxiliary class for computing the moving average process corresponding to a
 * given auto-covariance function. This implementation is based on the procedure
 * in the program SEATS and described in the paper of A. Maravall:
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public class RobustSymmetricFrequencyResponseDecomposer  {

    private double m_var;
    private BackFilter m_bf;
    private int m_freq;
    private double m_epsilon = 1e-4;

    /**
     *
     */
    public RobustSymmetricFrequencyResponseDecomposer() {
        m_freq = 0;
    }

    /**
     *
     * @param freq
     */
    public RobustSymmetricFrequencyResponseDecomposer(final int freq) {
        m_freq = freq;
    }

    public boolean decompose(final SymmetricFilter sf) {
        IntToDoubleFunction fn = sf.weights();
        double var = fn.applyAsDouble(0);
        if (var <= 0) {
            return false;
        }
        SymmetricFilter cur=sf;
        m_bf = null;
        // first, we try to remove unit roots from SymmetricFilter
        double[] weights = cur.weightsToArray();
        for (int i=0; i<weights.length; ++i){
            weights[i]/=var;
        }
        Polynomial P = Polynomial.of(weights);
        // normalize the polynomial, for numerical reason
        
        UnitRootsSolver urs = new UnitRootsSolver(m_freq);
        if (urs.factorize(P)) {
            UnitRoots ur = urs.getUnitRoots();
            UnitRoots sur = ur.sqrt();
            if (sur != null) {
                Polynomial urp = sur.asPolynomial();
                Polynomial ur2=urp.times(urp);
                m_bf = new BackFilter(urp);
             // ensure symmetry
                LeastSquaresDivision lsd=new LeastSquaresDivision();
                lsd.divide(P, ur2);
                P=lsd.getQuotient();
                double[] c = P.toArray();
                int d=P.degree();
                int n=d/2;
                for (int i=0; i<n; ++i){
                    double q=(c[i]+c[d-i])/2;
                    c[i]=q;
                    c[d-i]=q;
                }
                P=Polynomial.of(c);
           }
        }
        if (m_bf == null) {
            m_bf = BackFilter.ONE;
        }
        SymmetricMullerNewtonSolver solver = new SymmetricMullerNewtonSolver();
        if (!solver.factorize(P)) {
            return false;
        }
        Complex[] sroots = solver.getStableRoots();
        ComplexUtility.lejaOrder(sroots);
        Polynomial P2 = Polynomial.fromComplexRoots(sroots);
        BackFilter bf = new BackFilter(P2);
        double v = bf.get(0);
        m_bf= bf.times(m_bf);
        m_bf = m_bf.normalize();
        
        Polynomial coeff = m_bf.asPolynomial();
        m_var=var /coeff.coefficients().ssq();
        return m_var>=0;
    }

    public BackFilter getBFilter() {
        return m_bf;
    }

    public double getFactor() {
        return m_var;
    }

     /**
     * Gets the precision used in the search of conjugate roots of the initial
     * polynomial
     *
     * @return A strictly positive double. 1e-4 by default.
     */
    public double getPrecision() {
        return m_epsilon;
    }

    /**
     * The algorithm will search first for some unit roots. There are specified
     * by this parameter. See the class UnitRootsSolver for further
     * explanations.
     *
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
     * by this parameter. See the class UnitRootsSolver for further
     * explanations.
     *
     * @param value The parameter of the UnitRootSolver used in the
     * decomposition. 12 by default.
     */
    public void setStartingURValue(final int value) {
        m_freq = value;
    }
}
