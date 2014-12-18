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
 * @param <S>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FastInitializer<S extends ISsf> implements IFastInitializer<S>,
	IFastArrayInitializer<S> {

    /**
     *
     */
    public final ISsfInitializer<S> initializer;

    /**
     * 
     */
    public FastInitializer()
    {
	initializer = null;
    }

    /**
     * 
     * @param initializer
     */
    public FastInitializer(ISsfInitializer<S> initializer)
    {
	this.initializer = initializer;
    }

    /**
     *
     * @param ssf
     * @param data
     * @param fstate
     * @param rslts
     * @return
     */
    @Override
    public int initialize(final S ssf, final ISsfData data,
	    final FastArrayState fstate, final IFastArrayFilteringResults rslts) {
	State state = new State(ssf.getStateDim(), data.hasData());
	IFilteringResults frslts = (IFilteringResults) rslts;

	int ndiffuse = initializer != null ? initializer.initialize(ssf, data,
		state, frslts) : new DurbinKoopmanInitializer().initialize(ssf, data,
		state, frslts);
	if (ndiffuse < 0)
	    return ndiffuse;
	FastArrayState tmp = new FastArrayState(ssf, state, ndiffuse);
	fstate.copy(tmp);
	return ndiffuse;
    }

    /**
     *
     * @param ssf
     * @param data
     * @param fstate
     * @param rslts
     * @return
     */
    @Override
    public int initialize(final S ssf, final ISsfData data,
	    final FastState fstate, final IFastFilteringResults rslts) {
	State state = new State(ssf.getStateDim(), data.hasData());
	IFilteringResults frslts = (IFilteringResults) rslts;

	int ndiffuse = initializer != null ? initializer.initialize(ssf, data,
		state, frslts) : new DurbinKoopmanInitializer().initialize(ssf, data,
		state, frslts);
	if (ndiffuse < 0)
	    return ndiffuse;
	FastState tmp = new FastState(ssf, state, ndiffuse);
	fstate.copy(tmp);
	return ndiffuse;
    }
}
