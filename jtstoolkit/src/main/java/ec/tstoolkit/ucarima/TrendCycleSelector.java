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
package ec.tstoolkit.ucarima;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.polynomials.AbstractRootSelector;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRoots;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TrendCycleSelector extends AbstractRootSelector {

    private double m_bound = 0.4;

    /**
     *
     */
    public TrendCycleSelector() {
    }

    /**
     *
     * @param bound
     */
    public TrendCycleSelector(final double bound) {
        m_bound = bound;
    }

    /**
     *
     * @param root
     * @return
     */
    @Override
    public boolean accept(final Complex root) {
        if (root.getIm() == 0) {
            return 1 / root.getRe() >= m_bound;
        } else {
            double arg = Math.abs(root.arg());
            // TO DO
            
            return false;
        }
    }

    /**
     *
     * @return
     */
    public double getBound() {
        return m_bound;
    }

    /**
     *
     * @param value
     */
    public void setBound(final double value) {
        m_bound = value;
    }

    @Override
    public boolean selectUnitRoots(Polynomial p) {
        // remove (1_B)
        m_sel = Polynomial.ONE;
        m_nsel = p;
        Polynomial D = UnitRoots.D1;
        do {
            Polynomial.Division div = Polynomial.divide(m_nsel, D);
            if (div.isExact()) {
                m_sel = m_sel.times(D);
                m_nsel = div.getQuotient();
            } else {
                break;
            }
        } while (p.getDegree() > 1);
        return m_sel.getDegree() > 0;
    }
}
