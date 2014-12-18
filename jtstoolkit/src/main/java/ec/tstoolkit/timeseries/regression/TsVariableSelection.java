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

package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.utilities.Jdk6;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TsVariableSelection<T extends ITsVariable> {
    /**
     * 
     * @param <T>
     */
    public static class Item<T extends ITsVariable>
    {

        /**
         *
         */
        public final T variable;
        /**
         *
         */
        public final int position;

        /**
         * 
         * @param variable
         * @param pos
         */
        public Item(T item, int pos)
        {
	    this.variable = item;
	    this.position = pos;
	}
    }

    private ArrayList<Item<T>> m_regs = new ArrayList<>();

    /**
     * 
     */
    public TsVariableSelection()
    {
    }

    public boolean isEmpty(){
        return m_regs.isEmpty();
    }

    /**
     * 
     * @param variable
     * @param pos
     */
    public void add(T item, int pos)
    {
	m_regs.add(new Item<>(item, pos));
    }

    /**
     * 
     */
    public void clear()
    {
	m_regs.clear();
    }

    /**
     * 
     * @param domain
     * @return
     */
    public List<DataBlock> data(TsDomain domain)
    {
	ArrayList<DataBlock> cols = new ArrayList<>();
	int cur = 0;
	for (Item<T> group : m_regs) {
	    int dim = group.variable.getDim();
	    for (int i = 0; i < dim; ++i)
		cols.add(new DataBlock(domain.getLength()));
	    group.variable.data(domain, cols.subList(cur, cur+dim));
	    cur += dim;
	}
	return cols;
    }

    /**
     *
     * @return
     */
    public Item<T>[] elements() {
        return Jdk6.Collections.toArray(m_regs, Item.class);
    }

    /**
     * 
     * @param idx
     * @return
     */
    public Item<T> get(int idx)
    {
	return m_regs.get(idx);
    }

    /**
     * 
     * @return
     */
    public int getItemsCount()
    {
	return m_regs.size();
    }

    /**
     * 
     * @return
     */
    public int[] getPositions()
    {
	int[] pos = new int[getVariablesCount()];
	int cur = 0;
	for (Item<T> var : m_regs)
	    for (int j = 0; j < var.variable.getDim(); ++j)
		pos[cur++] = var.position + j;
	return pos;
    }

    /**
     * 
     * @return
     */
    public int getVariablesCount()
    {
	int n = 0;
	for (Item<T> var : m_regs)
	    n += var.variable.getDim();
	return n;
    }

    /**
     * 
     * @param domain
     * @return
     */
    public Matrix matrix(TsDomain domain)
    {
	int ncols = getVariablesCount();
	int nrows = domain.getLength();
	Matrix m = new Matrix(nrows, ncols);
	ArrayList<DataBlock> cols = new ArrayList<>();
	for (int i = 0; i < ncols; ++i)
	    cols.add(m.column(i));
	int cur = 0;
	for (Item<T> group : m_regs) {
            int dim=group.variable.getDim();
	    group.variable.data(domain, cols.subList(cur, cur+dim));
	    cur += dim;
	}
	return m;
    }

    /**
     * 
     * @param coeffs
     * @param domain
     * @return
     */
    public DataBlock sum(DataBlock coeffs, TsDomain domain)
    {
	if (m_regs.isEmpty())
	    return null;

	DataBlock rslt = new DataBlock(domain.getLength());
	for (Item<T> group : m_regs) {
	    ArrayList<DataBlock> data = new ArrayList<>();
	    int dim = group.variable.getDim();
	    for (int i = 0; i < dim; ++i)
		data.add(new DataBlock(domain.getLength()));
	    group.variable.data(domain, data);
	    for (int i = 0; i < dim; ++i)
		rslt.addAY(coeffs.get(group.position + i), data.get(i));
	}
	return rslt;
    }
}
