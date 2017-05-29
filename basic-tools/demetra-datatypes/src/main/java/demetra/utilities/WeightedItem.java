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
package demetra.utilities;

import demetra.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class WeightedItem<T> {

    /**
     *
     */
    public final T item;
    /**
     *
     */
    public double weight;

    /**
     *
     * @param item
     */
    public WeightedItem(T item) {
        this.item = item;
        this.weight = 1;
    }

    /**
     *
     * @param item
     * @param w
     */
    public WeightedItem(T item, double w) {
        this.item = item;
        this.weight = w;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof WeightedItem && equals((WeightedItem) obj));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.item.hashCode();
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.weight) ^ (Double.doubleToLongBits(this.weight) >>> 32));
        return hash;
    }

    public boolean equals(WeightedItem c) {
        return weight == c.weight && item.equals(c.item);
    }
}
