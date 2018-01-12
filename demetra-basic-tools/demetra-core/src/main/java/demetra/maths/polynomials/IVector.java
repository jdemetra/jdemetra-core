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
package demetra.maths.polynomials;

import demetra.design.Development;
import java.util.Arrays;

/**
 * 
 * @author Jzan Palate
 */
@Development(status = Development.Status.Alpha)
class IVector implements Cloneable {

    private final static int g_atom = 4;

    /**
     * 
     * @param n
     * @param d
     * @return
     */
    public static int simplify(final IVector n, final IVector d) {
	n.RSort();
	d.RSort();
	int di = 0, ni = 0;
	int ns = 0;
	while ((di < d.m_sz) && (ni < n.m_sz))
	    if (d.m_vals[di] == n.m_vals[ni]) {
		++ns;
		d.m_vals[di++] = 0;
		n.m_vals[ni++] = 0;
	    } else if (n.m_vals[ni] > d.m_vals[di])
		++ni;
	    else
		++di;
	if (ns > 0) {
	    n.compact();
	    d.compact();
	}
	return ns;
    }

    private int[] m_vals;

    private int m_sz;

    /** Creates new IVector */
    public IVector() {
    }

    /**
     * 
     * @param s
     */
    public void add(final int s) {
	if (m_vals == null) {
	    m_vals = new int[g_atom];
	    m_vals[0] = s;
	    m_sz = 1;
	} else if (m_vals.length == m_sz) // full
	{
	    int[] tmp = new int[m_sz + g_atom];
	    for (int i = 0; i < m_sz; ++i)
		tmp[i] = m_vals[i];
	    tmp[m_sz++] = s;
	    m_vals = tmp;
	} else
	    m_vals[m_sz++] = s;
    }

    /**
     * 
     * @param s
     * @param ns
     */
    public void add(final int[] s, final int ns) {
	if ((m_vals != null) && (m_sz + ns <= m_vals.length))
	    for (int i = 0; i < ns; ++i)
		m_vals[m_sz++] = s[i];
	else {
	    int l = m_sz + ns;
	    l = (1 + l / g_atom) * g_atom;
	    int[] tmp = new int[l];
	    for (int i = 0; i < m_sz; ++i)
		tmp[i] = m_vals[i];
	    for (int i = 0; i < ns; ++i)
		tmp[m_sz++] = s[i];
	    m_vals = tmp;
	}
    }

    /**
     * 
     * @param iv
     */
    public void add(final IVector iv) {
	if (iv.m_sz == 0)
	    return;
	add(iv.m_vals, iv.m_sz);
    }

    /**
     *
     */
    public void clear() {
	m_sz = 0;
	m_vals = null;
    }

    @Override
    public IVector clone() {
	try {
	    IVector iv = (IVector) super.clone();
            if (m_vals != null)
                iv.m_vals = m_vals.clone();
            return iv;
	} catch (CloneNotSupportedException err) {
            throw new AssertionError();
	}
    }

    private void compact() {
	int j = 0;
	for (int i = 0; i < m_sz; ++i)
	    if (m_vals[i] != 0)
		m_vals[j++] = m_vals[i];
	m_sz = j;
    }

    private boolean equals(final IVector iv) {
	if (m_sz != iv.m_sz)
	    return false;
	if (m_sz == 0)
	    return true;
	// we suppose that the vector are often ordered
	int last = 0;
	for (int i = 0; i < m_sz; ++i) {
	    int cur = m_vals[i];
	    if (cur != last) {
		// how many cur in this ?
		int n0 = 0, n1 = 0;
		for (int j = 0; j < m_sz; ++j) {
		    if (m_vals[j] == cur)
			++n0;
		    if (iv.m_vals[j] == cur)
			++n1;
		    if (n0 != n1)
			return false;
		}
		last = cur;
	    }
	}
	return true;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof IVector && equals((IVector) obj));
    }
    
    /**
     * 
     * @param idx
     * @return
     */
    public int get(final int idx) {
	return m_vals[idx];
    }

    /**
     * 
     * @return
     */
    public int getSize() {
	return m_sz;
    }

    @Override
    public int hashCode() {
	if (m_sz == 0)
	    return 0;
	int n = 0;
	for (int i = 0; i < m_sz; ++i)
	    n += m_vals[i];
	return n;
    }

    /**
     * 
     * @return
     */
    public int[] RPowers() {
	// compute the number of different roots
	if (m_sz == 0)
	    return null;
	int n = 1;
	for (int i = 1, j = 0; i < m_sz; ++i)
	    if (m_vals[i] != m_vals[j]) {
		++n;
		j = i;
	    }

	int[] rslt = new int[2 * n];

	rslt[0] = m_vals[0];
	n = 1;
	for (int i = 1, j = 0; i < m_sz; ++i)
	    if (m_vals[i] != rslt[j]) {
		rslt[++j] = n;
		rslt[++j] = m_vals[i];
		n = 1;
	    } else
		++n;
	rslt[rslt.length - 1] = n;
	return rslt;
    }

    /**
     *
     */
    public void RSort() {
	if (m_sz == 0)
	    return;
	Arrays.sort(m_vals, 0, m_sz);
	int n = m_sz / 2;
	for (int i = 0, j = m_sz - 1; i < n; ++i, --j) {
	    int tmp = m_vals[i];
	    m_vals[i] = m_vals[j];
	    m_vals[j] = tmp;
	}
    }

    /**
     * 
     * @return
     */
    public IVector sqrt() {
	IVector rslt = new IVector();
	for (int i = 0; i < m_sz; i += 2)
	    if (m_vals[i] != m_vals[i + 1])
		return null;
	    else
		rslt.add(m_vals[i]);
	return rslt;
    }

    /**
     * 
     * @return
     */
    public IVector squared() {
	IVector rslt = new IVector();
	if (m_sz == 0)
	    return rslt;
	rslt.m_sz = 2 * m_sz;
	rslt.m_vals = new int[m_vals.length * 2];
	for (int i = 0, j = 0; i < m_sz; ++i) {
	    rslt.m_vals[j++] = m_vals[i];
	    rslt.m_vals[j++] = m_vals[i];
	}
	return rslt;
    }
}
