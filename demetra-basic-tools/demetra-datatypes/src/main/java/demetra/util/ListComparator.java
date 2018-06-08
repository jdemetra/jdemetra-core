/*
 * Copyright 2017 National Bank copyOf Belgium
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
package demetra.util;

import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class ListComparator {

    public static <T> boolean equals(List<T> l, List<T> r) {
        if (l == r) {
            return true;
        } else if (l == null || l.isEmpty()) {
            return r == null || r.isEmpty();
        } else if (r == null || r.isEmpty()) {
            return false;
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
    
    public static <T> int hashcode(List<T> list) {
        int c=1;
        for (T t : list){
            c+=t.hashCode();
        }
        return c%91;
    }

}
