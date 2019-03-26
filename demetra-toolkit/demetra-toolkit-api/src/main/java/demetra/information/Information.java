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
package demetra.information;

import demetra.design.Immutable;
import demetra.design.Development;

/**
 * Generic class to handle named values (pair of {name, object}.
 *
 * @param <S>
 * @author Jean Palate
 */
@Immutable
@Development(status = Development.Status.Release)
public final class Information<S> {

    /**
     * Name of information
     */
    @lombok.Getter
    private final String name;

    /**
     * Information itself
     */
    @lombok.Getter
    private final S value;

    @lombok.Getter
    private final long index;

    /**
     * Creates a new named information object. The name is case sensitive.
     *
     * @param name Name of the information
     * @param value Information
     */
    public Information(String name, S value) {
        this.name = name;
        this.value = value;
        this.index = -1;
    }

    Information(String name, S value, long index) {
        this.name = name;
        this.value = value;
        this.index = index;
    }

    public static class NameSorter implements java.util.Comparator<Information<?>> {

        @Override
        public int compare(Information<?> o1, Information<?> o2) {
            return o1.name.compareTo(o2.name);
        }
    }

    public static class IndexedNameSorter implements java.util.Comparator<Information<?>> {

        public IndexedNameSorter(String prefix) {
            this.prefix = prefix;
            start = prefix.length();
        }

        private final String prefix;
        private final int start;

        @Override
        public int compare(Information<?> o1, Information<?> o2) {
            try {
                if (o1.name.startsWith(prefix) && o2.name.startsWith(prefix)) {
                    int i1 = Integer.parseInt(o1.name.substring(start));
                    int i2 = Integer.parseInt(o2.name.substring(start));
                    return Integer.compare(i1, i2);
                } else {
                    return o1.name.compareTo(o2.name);
                }

            } catch (Exception err) {
                return o1.name.compareTo(o2.name);
            }
        }
    }
}
