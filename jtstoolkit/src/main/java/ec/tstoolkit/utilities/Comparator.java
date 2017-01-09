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

import ec.tstoolkit.Parameter;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class Comparator {

    public static <T> boolean equals(List<T> l, List<T> r) {
        if (l == r) {
            return true;
        } else if (l == null || l.isEmpty()) {
            return r == null || r.isEmpty();
        } else if (r == null || r.isEmpty()) {
            return l == null || l.isEmpty();
        } else if (l.size() != r.size()) {
            return false;
        } else {
            boolean[] used = new boolean[l.size()];
            for (T lo : l) {
                int idx = 0;
                while (idx < used.length) {
                    if (!used[idx] && lo.equals(r.get(idx))) {
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
    
//    public static <T> boolean equals(T[] l, T[] r, boolean order) {
//        if (l == r) {
//            return true;
//        } else if (Arrays2.isNullOrEmpty(l)) {
//            return Arrays2.isNullOrEmpty(r);
//        } else if (Arrays2.isNullOrEmpty(r)) {
//            return Arrays2.isNullOrEmpty(l);
//        } else if (l.length != r.length) {
//            return false;
//        } else {
//            if (!order) {
//                boolean[] used = new boolean[l.length];
//                for (T lo : l) {
//                    int idx = 0;
//                    while (idx < used.length) {
//                        if (!used[idx] && lo.equals(r[idx])) {
//                            used[idx] = true;
//                            break;
//                        }
//                        ++idx;
//                    }
//                    if (idx == used.length) {
//                        return false;
//                    }
//                }
//            } else {
//                for (int i = 0; i < l.length; ++i) {
//                    if (!Objects.equals(l[i], r[i])) {
//                        return false;
//                    }
//                }
//            }
//            return true;
//        }
//    }

    public static <T> int hashcode(List<T> list) {
        int c=1;
        for (T t : list){
            c+=t.hashCode();
        }
        return c%91;
    }

}
