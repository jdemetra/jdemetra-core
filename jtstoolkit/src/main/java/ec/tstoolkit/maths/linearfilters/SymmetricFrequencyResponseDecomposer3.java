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
package ec.tstoolkit.maths.linearfilters;

import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.design.Algorithm;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.polynomials.LeastSquaresDivision;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.SymmetricMullerNewtonSolver;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.maths.polynomials.UnitRootsSolver;

/**
 * Auxiliary class for computing the moving average process corresponding to a
 * given auto-covariance function. This implementation is based on the procedure
 * in the program SEATS and described in the paper of A. Maravall:
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@Algorithm(entryPoint = "decompose")
public class SymmetricFrequencyResponseDecomposer3 implements ISymmetricFilterDecomposer {

    private double m_var;
    private BackFilter m_bf;
    private int m_freq;
    private double m_epsilon = 1e-4;
    private final double m_repsilon = 1e-1;

    /**
     *
     */
    public SymmetricFrequencyResponseDecomposer3() {
        m_freq = 12;
    }

    /**
     *
     * @param freq
     */
    public SymmetricFrequencyResponseDecomposer3(final int freq) {
        m_freq = freq;
    }

    @Override
    public boolean decompose(final SymmetricFilter sf) {
        double var = sf.getWeight(0);
        if (var <= 0) {
            return false;
        }
        SymmetricFilter cur=sf;
        m_bf = null;
        // first, we try to remove unit roots from SymmetricFilter
        double[] weights = cur.getWeights();
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
                Polynomial urp = sur.toPolynomial();
                Polynomial ur2=urp.times(urp);
                m_bf = new BackFilter(urp);
             // ensure symmetry
                LeastSquaresDivision lsd=new LeastSquaresDivision();
                lsd.divide(P, ur2);
                P=lsd.getQuotient();
                double[] c = P.getCoefficients();
                int d=P.getDegree();
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
        Polynomial P2 = Polynomial.fromComplexRoots(sroots);
        BackFilter bf = new BackFilter(P2);
        double v = bf.get(0);
        m_bf= bf.times(m_bf);
        m_bf = m_bf.normalize();
        
        Polynomial coeff = m_bf.getPolynomial();
        m_var=var /coeff.ssq();
        return m_var>=0;
    }

    @Override
    public BackFilter getBFilter() {
        return m_bf;
    }

    @Override
    public double getFactor() {
        return m_var;
    }

    @Override
    public ForeFilter getFFilter() {
        return m_bf.mirror();
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
