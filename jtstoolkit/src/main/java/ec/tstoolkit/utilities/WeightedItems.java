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

package ec.tstoolkit.utilities;

import ec.tstoolkit.design.Development;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class WeightedItems<T> implements Cloneable {

    public WeightedItems() {
    }

    public List<WeightedItem<T>> items() {
        return Collections.unmodifiableList(m_items);
    }

    public void clear() {
        m_items.clear();
    }

    public int getCount() {
        return m_items.size();
    }

    public WeightedItem<T> get(int idx) {
        return m_items.get(idx);
    }

    public void add(WeightedItem<T> item) {
        m_items.add(item);
    }

    public void add(Collection<WeightedItem<T>> items) {
        m_items.addAll(items);
    }

    public void removeAt(int pos) {
        m_items.remove(pos);
    }

    /// <summary>
    /// Changes the weights so that their sum is 1.
    /// </summary>
    /// <returns></returns>
    public boolean normalize() {
        double sum = getSumWeights();
        if (sum == 0) {
            return false;
        }
        for (WeightedItem<T> item : m_items) {
            item.weight /= sum;
        }
        return true;
    }

    /// <summary>
    /// Sum of the weights
    /// </summary>
    public double getSumWeights() {
        double s = 0;
        for (WeightedItem<T> item : m_items) {
            s += item.weight;
        }
        return s;
    }
    private ArrayList<WeightedItem<T>> m_items = new ArrayList<>();

    @Override
    public WeightedItems<T> clone() {
        try {
            WeightedItems<T> items = (WeightedItems<T>) super.clone();
            items.m_items = (ArrayList<WeightedItem<T>>) m_items.clone();
            return items;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public static <T> boolean equals(WeightedItems<T> l, WeightedItems<T> r) {

        boolean[] used = new boolean[l.getCount()];
        for (WeightedItem<T> wc : l.m_items) {
            int idx = 0;
            while (idx < used.length) {
                WeightedItem<T> cur = r.m_items.get(idx);

                if (!used[idx] && cur.item.equals(wc.item) && cur.weight == wc.weight) {
                    used[idx] = true;
                    break;
                }
                ++idx;
            }
            if (idx == used.length) {
                return false;
            }
        }

        return true;
    }
}
