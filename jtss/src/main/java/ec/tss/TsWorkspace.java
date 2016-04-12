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

package ec.tss;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ec.tstoolkit.design.Development;
import java.util.stream.Stream;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TsWorkspace implements Iterable<TsCollection> {

    private boolean m_bDirty, m_bRead;

    private ArrayList<TsCollection> m_tsdata = new ArrayList<>();

    private String m_name;

    /**
     *
     */
    public TsWorkspace() {
    }

    /**
     * 
     * @param coll
     */
    public TsWorkspace(Iterable<TsCollection> coll) {
	for (TsCollection c : coll)
	    m_tsdata.add(c);
    }

    /**
     * 
     * @param collection
     * @return
     */
    public int add(TsCollection collection) {
	if (m_bRead)
	    return -1;

	m_tsdata.add(collection);
	m_bDirty = true;
	return m_tsdata.size() - 1;
    }

    /**
     *
     */
    public void clear() {
	if (m_bRead)
	    return;
	if (!m_tsdata.isEmpty()) {
	    m_tsdata.clear();
	    m_bDirty = true;
	}
    }

    /**
     * 
     * @param name
     * @return
     */
    public boolean contains(String name) {
	for (TsCollection coll : m_tsdata)
	    if (coll.getName().equals(name))
		return true;
	return false;
    }

    /**
     * 
     * @return
     */
    public List<TsCollection> elements() {
	return m_tsdata;
    }

    /**
     * 
     * @param icoll
     * @return
     */
    public TsCollection get(int icoll) {
	if (icoll < 0 || icoll >= m_tsdata.size())
	    return null;
	else
	    return m_tsdata.get(icoll);
    }

    /**
     * 
     * @param icoll
     * @param ipos
     * @return
     */
    public Ts get(int icoll, int ipos) {
	if (icoll < 0 || icoll >= m_tsdata.size() || ipos < 0
		|| ipos >= m_tsdata.get(icoll).getCount())
	    return null;
	else
	    return m_tsdata.get(icoll).get(ipos);
    }

    /**
     * 
     * @return
     */
    public int getCount() {
	return m_tsdata.size();
    }

    /**
     * 
     * @return
     */
    public String getName() {
	return m_name;
    }

    /**
     * 
     * @return
     */
    public int getTSCount() {
	int n = 0;
	for (TsCollection c : m_tsdata)
	    n += c.getCount();
	return n;
    }

    /**
     * 
     * @return
     */
    public boolean isDirty() {
	return m_bDirty;
    }

    /**
     * 
     * @return
     */
    public boolean isReadOnly() {
	return m_bRead;
    }

    @Override
    public Iterator<TsCollection> iterator() {
	return m_tsdata.iterator();
    }
    
    public Stream<TsCollection> stream() {
        return m_tsdata.stream();
    }

    /**
     * 
     * @param coll
     */
    public void removeTsCollection(TsCollection coll) {
	if (m_bRead)
	    return;
	m_tsdata.remove(coll);
	m_bDirty = true;
    }

    /**
     * 
     * @param value
     */
    public void setDirty(boolean value) {
	m_bDirty = value;
    }

    /**
     * 
     * @param value
     */
    public void setName(String value) {
	m_name = value;
    }
    
    /**
     * 
     * @param value
     */
    public void setReadOnly(boolean value) {
	m_bRead = value;
    }

}
