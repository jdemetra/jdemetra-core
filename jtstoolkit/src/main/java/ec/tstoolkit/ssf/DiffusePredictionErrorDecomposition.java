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
import ec.tstoolkit.eco.Determinant;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DiffusePredictionErrorDecomposition extends
	PredictionErrorDecomposition implements IDiffuseFilteringResults {

    private final Determinant m_ddet = new Determinant();
    private int m_nd;

    /**
     * 
     * @param bres
     */
    public DiffusePredictionErrorDecomposition(final boolean bres)
    {
	super(bres);
    }

    /**
     * 
     */
    @Override
    public void closeDiffuse()
    {
    }

    /**
     * 
     * @return
     */
    public int getDiffuseCount()
    {
	return m_nd;
    }

    /**
     * 
     * @return
     */
    public double getDiffuseLogDeterminant()
    {
	return m_ddet.getLogDeterminant();
    }

    /**
     * 
     * @param ssf
     * @param data
     */
    @Override
    public void prepareDiffuse(final ISsf ssf, final ISsfData data)
    {
	m_ddet.clear();
	m_nd = 0;
        super.init(ssf, ssf.getNonStationaryDim());
    }

    /**
     * 
     * @param t
     * @param state
     */
    @Override
    public void save(final int t, final DiffuseState state)
    {
	if (!state.isMissing())
	    if (state.fi > 0) {
		++m_nd;
		m_ddet.add(state.fi);
	    } else
		super.save(t, state);
    }
}
