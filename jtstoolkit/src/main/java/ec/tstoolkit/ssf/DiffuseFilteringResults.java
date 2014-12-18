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
public class DiffuseFilteringResults extends ResidualsCumulator implements
	IFilteringResults, IFastFilteringResults, IDiffuseFilteringResults {

    private final DiffuseVarianceFilter m_var;

    private final FilteredData m_fdata;

    private final Determinant m_ddet = new Determinant();

    /**
     * 
     */
    public DiffuseFilteringResults()
    {
	m_var = new DiffuseVarianceFilter();
	m_fdata = new FilteredData();
    }

    /**
     * 
     * @param hasC
     */
    public DiffuseFilteringResults(final boolean hasC)
    {
	m_var = new DiffuseVarianceFilter(hasC);
	m_fdata = new FilteredData();
    }

    /**
     *
     */
    @Override
    public void clear() {
	m_fdata.clear();
	m_var.clear();
	super.clear();
	m_ddet.clear();
    }

    /**
     * 
     */
    @Override
    public void close()
    {
	m_var.close();
    }

    /**
     * 
     */
    @Override
    public void closeDiffuse()
    {
	m_var.closeDiffuse();
    }

    /**
     * 
     * @return
     */
    public int getDiffuseCount()
    {
	return m_var.m_ndiffuse;
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
     * @return
     */
    public int getEndDiffusePosition()
    {
	return m_var.m_enddiffusepos;
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
    public DiffuseVarianceFilter getVarianceFilter()
    {
	return m_var;
    }

    /**
     * 
     * @param ssf
     * @param data
     */
    @Override
    public void prepare(final ISsf ssf, final ISsfData data)
    {
	if (m_var.isOpen())
	    m_fdata.checkSize(data.getCount());
	else {
	    m_fdata.init(ssf.getStateDim(), data.getCount());
	    m_ddet.clear();
	    super.clear();
	}
	m_var.prepare(ssf, data);
    }

    /**
     * 
     * @param ssf
     * @param data
     */
    @Override
    public void prepareDiffuse(final ISsf ssf, final ISsfData data)
    {
	clear();
	m_var.prepareDiffuse(ssf, data);
	m_fdata.init(ssf.getStateDim(), ssf.getNonStationaryDim());
    }

    /**
     * 
     * @param t
     * @param state
     */
    @Override
    public void save(final int t, final DiffuseState state)
    {
	m_var.save(t, state);
	m_fdata.checkSize(t + 1);
	m_fdata.save(t, state);
	if (!state.isMissing())
	    if (state.fi > 0)
		m_ddet.add(state.fi);
	    else
		super.add(state.e, state.f);
    }

    /**
     * 
     * @param t
     * @param state
     */
    @Override
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
