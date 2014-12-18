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
package ec.tstoolkit.maths.realfunctions.bfgs;

import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SimpleLineSearch implements ILineSearch {
    // / <summary>
    // / Creates a new SimpleLineSearch
    // / </summary>

    private double m_eps = 1e-9, m_fac = .5;

    /**
     *
     */
    public SimpleLineSearch() {
    }

    /**
     *
     * @return
     */
    @Override
    public ILineSearch exemplar() {
        SimpleLineSearch ls = new SimpleLineSearch();
        return ls;
    }

    /**
     *
     * @param fn
     * @param start
     * @return
     */
    @Override
    public boolean optimize(ILineFunction fn, double start) {
        double stpmax = fn.getStepMax();
        if (stpmax < m_eps) {
            return true;
        }
        double stp = start;
        if (stp > stpmax) {
            stp = stpmax;
        }

        double f0 = fn.getValue();

        do {
            try {
                fn.setStep(stp);
                if (fn.getValue() < f0) {
                    return true;
                }
            } catch (Exception err) {
            }
            stp *= m_fac;
        } while (stp > m_eps);
        return false;
    }
}
