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

import java.util.Arrays;
import java.util.Collection;

class NameKey implements Comparable<NameKey> {

    static String[] sort(Collection<NameKey> nk) {
        NameKey[] array = nk.toArray(new NameKey[nk.size()]);
        Arrays.sort(array, KeyComparer.INSTANCE);
        String[] r = new String[array.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = array[i].name;
        }
        return r;
    }

    NameKey(String n) {
        name = n;
        key = KEY++;
    }
    String name;
    final int key;
    private static int KEY = 0;

    @Override
    public int compareTo(NameKey o) {
        return name.compareTo(o.name);
    }

    private enum KeyComparer implements java.util.Comparator<NameKey> {

        INSTANCE;
        
        @Override
        public int compare(NameKey x, NameKey y) {
            return Integer.compare(x.key, y.key);
        }
    }
}
