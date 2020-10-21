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

package demetra.util;

import nbbrd.design.Development;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class WeightedItems<T> implements Cloneable {

    public WeightedItems() {
    }

    public List<WeightedItem<T>> items() {
        return Collections.unmodifiableList(items);
    }

    public void clear() {
        items.clear();
    }

    public int getCount() {
        return items.size();
    }

    public WeightedItem<T> get(int idx) {
        return items.get(idx);
    }

    public void add(WeightedItem<T> item) {
        items.add(item);
    }

    public void add(Collection<WeightedItem<T>> items) {
        this.items.addAll(items);
    }

    public void removeAt(int pos) {
        items.remove(pos);
    }

    /// <summary>
    /// Changes the weights so that their sum is 1.
    /// </summary>
    /// <returns></returns>
    public WeightedItems<T> normalize() {
        double sum = getSumWeights();
        if (sum == 0) {
            return null;
        }
        WeightedItems<T> nw=new WeightedItems<>();
        for (WeightedItem<T> item : items) {
            nw.add(item.reweight(item.getWeight() / sum));
        }
        return nw;
    }

    /// <summary>
    /// Sum of the weights
    /// </summary>
    public double getSumWeights() {
        double s = 0;
        for (WeightedItem<T> item : items) {
            s += item.getWeight();
        }
        return s;
    }
    private ArrayList<WeightedItem<T>> items = new ArrayList<>();

    @Override
    public WeightedItems<T> clone() {
        try {
            WeightedItems<T> c = (WeightedItems<T>) super.clone();
            c.items = (ArrayList<WeightedItem<T>>) items.clone();
            return c;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public static <T> boolean equals(WeightedItems<T> l, WeightedItems<T> r) {

       return Comparator.equals(l.items, r.items);
    }
}
