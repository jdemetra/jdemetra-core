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

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
public interface Id extends Comparable<Id> {

    String get(int index);

    Id extend(String tail);

    Id parent();

    int getCount();

    @Override
    public String toString();

    default String tail() {
        int n = getCount();
        return n > 0 ? get(n - 1) : null;
    }

    default Id[] path() {
        int n = getCount();
        if (n == 0) {
            return new Id[0];
        }
        Id[] result = new Id[n];
        Id cur = this;
        while (n > 0) {
            result[--n] = cur;
            cur = cur.parent();
        }
        return result;
    }

    default boolean startsWith(Id that) {
        int sn = that.getCount();
        if (sn > getCount()) {
            return false;
        }
        for (int i = 0; i < sn; ++i) {
            if (!get(i).equals(that.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    default int compareTo(Id that) {
        int ln = getCount(), rn = that.getCount();
        if (ln < rn) {
            return -1;
        }
        if (ln > rn) {
            return 1;
        }
        for (int i = 0; i < ln; ++i) {
            int cmp = get(i).compareTo(that.get(i));
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }

    @NonNull
    default String[] toArray() {
        String[] result = new String[getCount()];
        for (int i = 0; i < result.length; i++) {
            result[i] = get(i);
        }
        return result;
    }
}
