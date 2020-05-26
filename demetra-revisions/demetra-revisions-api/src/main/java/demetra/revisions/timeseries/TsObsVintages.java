/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.revisions.timeseries;

import demetra.timeseries.TsPeriod;

/**
 *
 * @author Philippe Charles
 * @param <K>
 */
@lombok.Value
public class TsObsVintages<K extends Comparable> {

    @lombok.Value
    public static class Entry<K extends Comparable> implements Comparable {

        K key;
        double value;

        @Override
        public int compareTo(Object o) {
            Entry<K> k = (Entry<K>) o;
            return key.compareTo(k.key);
        }
    }

    @lombok.NonNull
    TsPeriod period;

    Entry<K>[] vintages;

    public Entry<K> first() {
        return vintages[0];
    }

    public Entry<K> last() {
        return vintages[vintages.length - 1];
    }

    public double[] values() {
        double[] vals = new double[vintages.length];
        for (int i = 0; i < vals.length; ++i) {
            vals[i] = vintages[i].value;
        }
        return vals;
    }
}
