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
public class DiffuseFilter {

    /**
     * 
     */
    public DiffuseFilter()
    {
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final ISsf ssf, final ISsfData data,
	    final IDiffuseFilteringResults rslts) {
	int dim = ssf.getStateDim();
	State state = new State(dim, true);
	DurbinKoopmanInitializer dk = new DurbinKoopmanInitializer();
	int pos = dk.initialize(ssf, data, state, rslts);
	if (pos == -1)
	    return false;
	Filter<ISsf> filter = new Filter<>(ssf, new SsfInitializer(pos,
		state));
	return filter.process(data, rslts);
    }
}
