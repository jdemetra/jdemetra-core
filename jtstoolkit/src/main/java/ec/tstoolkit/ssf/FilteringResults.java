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
public class FilteringResults extends ResidualsCumulator implements
	IFilteringResults, IFastFilteringResults {

    private final VarianceFilter m_var;

    private final FilteredData m_fdata;

    /**
     * 
     */
    public FilteringResults()
    {
	m_var = new VarianceFilter();
	m_fdata = new FilteredData();
    }

    /**
     * 
     * @param hasC
     */
    public FilteringResults(final boolean hasC)
    {
	m_var = new VarianceFilter(hasC);
	m_fdata = new FilteredData();
    }

    /**
     *
     */
    @Override
    public void clear() {
	super.clear();
	m_fdata.clear();
	m_var.clear();
    }

    /**
     * 
     */
    public void close()
    {
    }

    /**
     * 
     * @return
     */
    public FilteredData getFilteredData()
    {
	return m_fdata;
    }

    /**
     * 
     * @return
     */
    public VarianceFilter getVarianceFilter()
    {
	return m_var;
    }

    /**
     * 
     * @param ssf
     * @param data
     */
    public void prepare(final ISsf ssf, final ISsfData data)
    {
	if (m_var.isOpen())
	    m_fdata.checkSize(data.getCount());
	else {
	    super.clear();
	    m_fdata.init(ssf.getStateDim(), data.getCount());
	}
	m_var.prepare(ssf, data);
    }

    /**
     * 
     * @param t
     * @param state
     */
    public void save(final int t, final FastState state)
    {
	m_var.save(t, state);
	m_fdata.save(t, state);
	if (!state.isMissing())
	    super.add(state.e, state.f);
    }

    /**
     * 
     * @param t
     * @param state
     */
    @Override
    public void save(final int t, final State state)
    {
	m_var.save(t, state);
	m_fdata.save(t, state);
	if (!state.isMissing() )
	    super.add(state.e, state.f);
    }
}
