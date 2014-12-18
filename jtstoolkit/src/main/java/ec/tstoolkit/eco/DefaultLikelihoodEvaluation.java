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
package ec.tstoolkit.eco;

/**
 *
 * @author Jean Palate
 * @param <L>
 */
public class DefaultLikelihoodEvaluation<L extends ILikelihood> {

    private boolean m_ml = true, m_llog = true;

    private final L m_ll;

    /**
     *
     * @param ll
     */
    public DefaultLikelihoodEvaluation(final L ll) {
        m_ll = ll;
    }

    /**
     *
     * @return
     */
    public double[] getE() {
        if (m_ll == null) {
            return null;
        }
        double[] err = m_ll.getResiduals();
        if (m_ml && m_ll.getFactor() != 1) {
            err = err.clone();
            double sqrfactor = Math.sqrt(m_ll.getFactor());
            for (int i = 0; i < err.length; ++i) {
                err[i] *= sqrfactor;
            }
        }
        return err;
    }

    /**
     *
     * @return
     */
    public L getLikelihood() {
        return m_ll;
    }

    /**
     *
     * @return
     */
    public double getSsqValue() {
        if (m_ll == null) {
            return Double.NaN;
        }
        return m_ml ? m_ll.getSsqErr() * m_ll.getFactor() : m_ll.getSsqErr();
    }

    /**
     *
     * @return
     */
    public double getValue() {
        if (m_ll == null) {
            return Double.NaN;
        }
        if (m_llog) {
            return m_ml ? -m_ll.getLogLikelihood() : Math.log(m_ll.getSsqErr());
        } else {
            return m_ml ? m_ll.getSsqErr() * m_ll.getFactor() : m_ll
                    .getSsqErr();
        }
    }

    /**
     *
     * @return
     */
    public boolean isUsingLogLikelihood() {
        return m_llog;
    }

    /**
     *
     * @return
     */
    public boolean isUsingML() {
        return m_ml;
    }

    /**
     *
     * @param value
     */
    public void useLogLikelihood(final boolean value) {
        m_llog = value;
    }

    /**
     *
     * @param value
     */
    public void useML(final boolean value) {
        m_ml = value;
    }
}
