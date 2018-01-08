/*
 * Copyright 2013-2014 National Bank of Belgium
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
package demetra.dfm;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class MeasurementStructure implements Comparable<MeasurementStructure> {

    public final MeasurementType type;
    public final boolean[] used;

    public MeasurementStructure(final MeasurementType type, final boolean[] used) {
        this.type = type;
        this.used = used;
    }

    @Override
    public int compareTo(MeasurementStructure o) {
        int cmp = type.compareTo(o.type);
        if (cmp != 0) {
            return cmp;
        } else { 
            if (used.length < o.used.length) {
                return -1;
            }
            if (used.length > o.used.length) {
                return 1;
            }
            for (int i = 0; i < used.length; ++i) {
                if (!used[i] && o.used[i]) {
                    return -1;
                } else if (used[i] && !o.used[i]) {
                    return 1;
                }
            }
            return 0;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type).append('[');
        if (used.length > 0) {
            builder.append(used[0] ? 1 : 0);
        }
        for (int i = 1; i < used.length; ++i) {
            builder.append(' ').append(used[i] ? 1 : 0);
        }
        builder.append(']');
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MeasurementStructure) {
            MeasurementStructure m = (MeasurementStructure) o;
            return type == m.type && Arrays.equals(used, m.used);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.type);
        hash = 37 * hash + Arrays.hashCode(this.used);
        return hash;
    }
}
