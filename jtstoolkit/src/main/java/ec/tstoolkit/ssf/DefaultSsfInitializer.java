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
package ec.tstoolkit.ssf;

import ec.tstoolkit.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DefaultSsfInitializer implements ISsfInitializer<ISsf> {

    /**
     * 
     */
    public DefaultSsfInitializer()
    {
    }

    /**
     *
     * @param ssf
     * @param data
     * @param state
     * @param rslts
     * @return
     */
    public int initialize(final ISsf ssf, final ISsfData data,
	    final State state, final IFilteringResults rslts) {
        state.P.set(0);
	ssf.Pf0(state.P.subMatrix());
	double[] s0 = data.getInitialState();
	if (s0 != null)
	    state.A.copyFrom(s0, 0);
	else
	    state.A.set(0);
	return 0;
    }
}
