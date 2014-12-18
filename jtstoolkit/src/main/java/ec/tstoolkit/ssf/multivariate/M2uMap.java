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
package ec.tstoolkit.ssf.multivariate;

import ec.tstoolkit.design.Development;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class M2uMap implements IMUMap
{

    private final List<M2uEntry> m_list;

    private final Map<M2uEntry, Integer> m_map;

    private int m_icur = 0;

    private boolean m_bclosed;

    /** Creates a new instance of MUMap */
    public M2uMap() {
	m_list = new ArrayList<>();
	m_map = new HashMap<>();
    }

    /**
     * 
     * @param minSize
     */
    public M2uMap(final int minSize)
    {
	m_list = new ArrayList<>(minSize);
	m_map = new HashMap<>();
    }

    /**
     * 
     * @param it
     * @param ivar
     */
    public void add(final int it, final int ivar)
    {
	if (m_bclosed)
	    return;
	M2uEntry entry = new M2uEntry(it, ivar);
	m_list.add(entry);
	m_map.put(entry, m_icur++);
    }

    /**
     * 
     */
    public void clear()
    {
	if (m_bclosed)
	    return;
	m_list.clear();
	m_map.clear();
	m_icur = 0;
    }

    /**
     * 
     */
    public void close()
    {
	m_bclosed = true;
	// m_list.TrimExcess();
    }

    /**
     * 
     * @param s
     * @return
     */
    public M2uEntry get(final int s)
    {
	return m_list.get(s);
    }

    /**
     * 
     * @param it
     * @param ivar
     * @return
     */
    public int get(final int it, final int ivar)
    {
	final Integer s = m_map.get(new M2uEntry(it, ivar));
	return (s == null) ? -1 : s;
    }

    /**
     * 
     * @return
     */
    public int getCount()
    {
	return m_icur;
    }

    /**
     * 
     * @return
     */
    public boolean isClosed()
    {
	return m_bclosed;
    }
}
