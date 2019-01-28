/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.stl;

/**
 *
 * @author Jean Palate
 */
public class LowPassLoessFilter {

    private final LoessFilter filter;
    private final int np;

    public LowPassLoessFilter(LoessSpecification spec, int np) {
        this.filter = new LoessFilter(spec);
        this.np = np;
    }

    public boolean filter(IDataGetter x, IDataSelector t) {

        // moving averages...
        int n = x.getLength();
        double[] w1 = new double[n - np + 1];
        double[] w2 = new double[n - 2 * np + 2];
        double[] w3 = new double[n - 2 * np];
        ma(np, x, IDataSelector.of(w1));
        ma(np, IDataGetter.of(w1), IDataSelector.of(w2));
        ma(3, IDataGetter.of(w2), IDataSelector.of(w3));
        filter.filter(IDataGetter.of(w3), null, t);
        return true;
    }

    public void ma(int len, IDataGetter x, IDataSelector ave) {
        int n = x.getLength();
        int newn = n - len + 1;
        double v = 0, flen = len;
        int i0 = x.getStart(), i1 = i0 + len;
        for (int i = i0; i < i1; ++i) {
            v += x.get(i);
        }
        int j = ave.getStart();
        ave.set(j, v / flen);
        if (newn > 1) {
            for (int i = j + 1, k = i1, m = i0; i < newn; ++i, ++k, ++m) {
                v = v - x.get(m) + x.get(k);
                ave.set(i, v / flen);
            }
        }
    }
}
