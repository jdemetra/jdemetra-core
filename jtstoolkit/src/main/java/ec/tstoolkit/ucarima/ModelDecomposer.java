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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ec.tstoolkit.BaseException;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.polynomials.IRootSelector;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ModelDecomposer
{

    private final List<SimpleModelDecomposer> m_smds = new ArrayList<>();

    /**
     *
     */
    public ModelDecomposer() {
    }

    /**
     * 
     * @param selector
     */
    public void add(final IRootSelector selector) {
	RootDecomposer decomposer = new RootDecomposer(selector);
	m_smds.add(decomposer);
    }

    /**
     * 
     * @param smd
     */
    public void add(final SimpleModelDecomposer smd) {
	m_smds.add(smd);
    }

    /**
     *
     */
    public void clear() {
	m_smds.clear();
    }

    /**
     * 
     * @param m
     * @return
     */
    public UcarimaModel decompose(final IArimaModel m) {
	try {
	    ArimaModel arima = ArimaModel.create(m);
	    if (arima == null)
		return null;
	    int n = m_smds.size();
	    ArimaModel[] cmps = new ArimaModel[n + 1];
	    if (n == 0)
		cmps[0] = arima;
	    else {
		ArimaModel cur = arima;
		for (int i = 0; i < n; ++i) {
		    SimpleModelDecomposer smd = m_smds.get(i);
		    smd.setModel(cur);
		    cmps[i] = smd.getSignal();
		    cur = smd.getNoise();
		}
		cmps[n] = cur;
	    }
	    return new UcarimaModel(m, Arrays.asList(cmps));
	} catch (BaseException ex) {
	    return null;
	}
    }
}
