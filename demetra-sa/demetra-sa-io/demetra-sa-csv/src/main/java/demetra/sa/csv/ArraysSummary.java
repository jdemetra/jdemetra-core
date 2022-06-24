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
package demetra.sa.csv;

import demetra.data.DoubleSeq;
import demetra.information.Explorable;
import demetra.math.matrices.Matrix;
import demetra.sa.SaDocument;
import demetra.util.WildCards;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Supports currently matrices, arrays and doubleseq
 */
public class ArraysSummary {

    private final String name;
    private final Map<String, DoubleArray> data = new LinkedHashMap<>();

    public ArraysSummary(String[] items, String name, SaDocument document) {
        this.name = name;
        fillDictionary(items, document.getResults());
    }

     public String getName() {
        return name;
    }

   private void fillDictionary(String[] items, Explorable results) {
        for (String item : items) {
            try {
                item = item.toLowerCase();
                if (results != null) {
                    if (WildCards.hasWildCards(item)) {
                        Map<String, Matrix> mall = results.searchAll(item, Matrix.class);
                        mall.entrySet().forEach(entry -> data.put(entry.getKey(), DoubleArray.of(entry.getValue())));
                        Map<String, double[]> all = results.searchAll(item, double[].class);
                        all.entrySet().forEach(entry -> data.put(entry.getKey(), DoubleArray.of(entry.getValue())));
                        Map<String, DoubleSeq> vall = results.searchAll(item, DoubleSeq.class);
                        vall.entrySet().forEach(entry -> data.put(entry.getKey(), DoubleArray.of(entry.getValue())));
                    } else {
                        Matrix m = results.getData(item, Matrix.class);
                        if (m != null) {
                            data.put(item, DoubleArray.of(m));
                        } else {
                            double[] v = results.getData(item, double[].class);
                            if (v != null) {
                                data.put(item, DoubleArray.of(v));
                            } else {
                                DoubleSeq s = results.getData(item, DoubleSeq.class);
                                if (s != null) {
                                    data.put(item, DoubleArray.of(s));
                                }
                            }
                        }
                    }
                } else {
                    data.put(item, null);
                }
            } catch (Exception err) {
                data.put(item, null);
            }

        }
    }

    public DoubleArray getArray(String name) {
        return data.get(name);
    }

    void fill(Set<String> set) {
        set.addAll(data.keySet());
    }
}
