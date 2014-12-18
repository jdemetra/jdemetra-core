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

import ec.tstoolkit.arima.LinearModel;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.RationalFilter;

/**
 * This class represents the wiener-Kolmogorov estimator of a component of an
 * Ucarima model.
 * It contains the Wiener-Kolmogorov filter (which is applied on the observations)
 * and the model of the estimator (which is related to the innovations)
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class WienerKolmogorovEstimator {

    private final LinearModel m_model;

    private final RationalFilter m_filter;

    /**
     * @param filter Wiener-Kolmogorov filter
     * @param model Model of the estimator
     */
    public WienerKolmogorovEstimator(final RationalFilter filter, final LinearModel model) {
	m_filter = filter;
	m_model = model;
    }

    /**
     * The Wiener-Kolmogorov filter of the component
     * @return
     */
    public RationalFilter getFilter() {
	return m_filter;
    }

    /**
     * The model of the final estimator of the component
     * @return
     */
    public LinearModel getModel() {
	return m_model;
    }

}
