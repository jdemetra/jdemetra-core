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

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class SimpleModelDecomposer
{
    ArimaModel m_model, m_s, m_n;

    /**
     *
     */
    protected SimpleModelDecomposer() {
	m_model = m_s = m_n = null;
    }

    /**
         *
         */
    protected abstract void calc();

    /**
         *
         */
    protected void clear() {
	m_s = m_n = null;
    }

    /**
     * 
     * @return
     */
    public ArimaModel getModel() {
	return m_model;
    }

    /**
     * 
     * @return
     */
    public ArimaModel getNoise() {
	if (m_n == null)
	    calc();
	return m_n;
    }

    /**
     * 
     * @return
     */
    public ArimaModel getSignal() {
	if (m_s == null)
	    calc();
	return m_s;
    }

    /**
     * 
     * @param value
     */
    public void setModel(final ArimaModel value) {
	m_model = value;
	clear();
    }

}
