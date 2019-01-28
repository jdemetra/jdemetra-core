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

/**
 *
 * @author Jean Palate
 */
public class WildCards {

    private final String w;
    private final char[] wa;
    private int[] na;
    private int la;
    private int lw;
    private String c;
    private int lmin, lc;

    public WildCards(String wc) {
        w = wc;
        lw = wc.length();
        wa = w.toCharArray();
        prepare();
    }

    public boolean match(String str) {
        c = str;
        lc = str.length();
        if (lc < lmin) {
            return false;
        }
        if (na == null && lc != lw)
            return false;
        initTest();
        do {
            if (test()) {
                return true;
            }
        } while (nextTest());

        return false;
    }

    private void prepare() {
        sort();
        simplify();
    }

    private void sort() {
        // sorts the ? * char
        boolean changed;
        do {
            changed = false;
            for (int i = 1; i < lw; ++i) {
                if (wa[i] == '?' && wa[i - 1] == '*') {
                    wa[i] = '*';
                    wa[i - 1] = '?';
                    changed = true;
                }
            }

        } while (changed);
    }

    private void simplify() {
        int ns = 0;
        for (int i = 0; i < lw; ++i) {
            if (wa[i] == '*') {
                ++ns;
                int j = i + 1;
                while (j < lw && wa[j] == '*') {
                    ++j;
                }
                if (j - i > 1) {
                    for (int k = j, l = i + 1; k < lw; ++k, ++l) {
                        wa[l] = wa[k];
                    }
                    lw -= j - i - 1;
                }
            }
        }
        if (ns > 0) {
            lmin = lw - ns;
            na = new int[ns];
        } else {
            lmin = lw;
        }
    }

    private boolean test() {
        for (int i = 0, j = 0, k = 0; i < lw; ++i) {
            if (wa[i] == '*') {
                k += na[j++];
            } else {
                if (wa[i] != '?' && wa[i] != c.charAt(k)) {
                    return false;
                }
                ++k;
            }
        }
        return true;
    }

    private void initTest() {
        // first test. all na == 0, except the last one
        la = lc - lmin;
        if (na != null) {
            int ia = na.length - 1;
            for (int i = 0; i < ia; ++i) {
                na[i] = 0;
            }
            na[ia] = la;
        }
    }

    private boolean nextTest() {
        if (na == null || na.length == 1) {
            return false;
        }
        int ia = 0;
        int ir = la;
        int il=na.length-1;
        while (ia < il) {
            if (ir == na[ia]) {
                break;
            } else {
                ir -= na[ia];
                ++ia;
            }
        }
        if (ia == 0) {
            return false;
        }
        //  increase by 1 the previous na, reset the next to 0 (except the rest)
        ++na[ia - 1];
        --ir;
        for (int i = ia; i < il; ++i) {
            na[i] = 0;
        }
        na[il] = ir;
        return true;
    }
}
