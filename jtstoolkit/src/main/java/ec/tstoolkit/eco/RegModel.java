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
package ec.tstoolkit.eco;

import java.util.ArrayList;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;

/**
 * 
 * @author Jean Palate
 */
public class RegModel implements Cloneable {

    /**
     * 
     * @param y
     * @return
     */
    public static int[] cleanMissings(final double[] y) {
	int nmiss = 0;
	int ny = y.length;
	for (int i = 0; i < ny; ++i)
	    if (Double.isNaN(y[i]))
		++nmiss;
	if (nmiss != 0) {
	    int[] missings = new int[nmiss];
	    // starting at the end of the series
	    int i0 = -1, i1 = -1;
	    int im = 0;
	    for (int i = ny - 1; i >= 0; --i)
		if (Double.isNaN(y[i])) {
		    if (i1 == -1)
			i1 = i + 1;
		    i0 = i - 1;
		    missings[im++] = i;
		} else if (i0 != -1) // finishing run of missing values
		{
		    // interpolating
		    double v = (i1 >= ny) ? y[i0] : (y[i1] + y[i0]) / 2;
		    for (int j = i0 + 1; j < i1; ++j)
			y[j] = v;
		    i0 = i1 = -1;
		}
	    if (i1 > 0) {
		if (i1 >= ny)
		    throw new EcoException("Empty series", "Eco.cleanMissings");
		for (int j = 0; j < i1; ++j)
		    y[j] = y[i1];
	    }
	    return missings;
	} else
	    return null;
    }

    private DataBlock m_y;

    private boolean m_bmean;

    private ArrayList<DataBlock> m_x;

    /**
     * 
     */
    public RegModel()
    {
	m_x = new ArrayList<>();
	m_bmean = false;
    }

    /**
     * 
     * @param x
     */
    public void addX(final DataBlock x) {
	m_x.add(x);
    }

    /**
     * 
     * @param b
     * @return
     */
    public DataBlock calcRes(final IReadDataBlock b) {
	if (b.getLength() != this.getVarsCount())
	    return null;

	DataBlock res = m_y.deepClone();
	int idx = 0;
	if (m_bmean) {
	    double m = b.get(idx++);
	    res.sub(m);
	}

	for (int i = 0; i < m_x.size(); ++i) {
	    double c = -b.get(idx++);
	    res.addAY(c, m_x.get(i));
	}
	return res;
    }

    /**
     *
     */
    public void clearX() {
	m_x.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public RegModel clone() {
	try {
	    RegModel reg = (RegModel) super.clone();
            reg.m_x = (ArrayList<DataBlock>) m_x.clone();
            return reg;
	} catch (CloneNotSupportedException err) {
            throw new AssertionError();
	}
    }

    /**
     * 
     * @return
     */
    public int getObsCount() {
	return m_y.getLength();
    }

    /**
     * 
     * @return
     */
    public int getVarsCount() {
	int n = m_x.size();
	if (m_bmean)
	    ++n;
	return n;
    }

    /**
     * 
     * @return
     */
    public int getXCount() {
	return m_x.size();
    }

    /**
     * 
     * @return
     */
    public DataBlock getY() {
	return m_y;
    }

    /**
     * 
     * @param pos
     * @param x
     */
    public void insertX(final int pos, final DataBlock x) {
	m_x.add(pos, x);
    }

    /**
     * 
     * @return
     */
    public boolean isMeanCorrection() {
	return m_bmean;
    }

    /**
     * 
     * @param idx
     */
    public void removeX(final int idx) {
	m_x.remove(idx);
    }

    /**
     * 
     * @param i0
     * @param n
     */
    public void removeX(final int i0, final int n) {
	for (int i = 0; i < n; i++)
	    m_x.remove(i0);
    }

    /**
     * 
     * @param value
     */
    public void setMeanCorrection(final boolean value) {
	m_bmean = value;
    }

    /**
     * 
     * @param value
     */
    public void setY(final DataBlock value) {
	m_y = value;
    }

    /**
     * 
     * @return
     */
    public Matrix variables() {
	int nc = getVarsCount(), nr = getObsCount();
	if (nc == 0 || nr == 0)
	    return null;
	Matrix m = new Matrix(nr, nc);
	DataBlockIterator cols = m.columns();
	DataBlock cur = cols.getData();
	if (m_bmean) {
	    cur.set(1);
	    cols.next();
	}

	for (int i = 0; i < m_x.size(); ++i) {
	    cur.copy(m_x.get(i));
	    cols.next();
	}
	return m;
    }

    /**
     * 
     * @param idx
     * @return
     */
    public DataBlock X(final int idx) {
	return m_x.get(idx);
    }
}
