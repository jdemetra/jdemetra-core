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
package demetra.dfm.internal;

import jdplus.data.DataBlock;
import demetra.dfm.IDfmMeasurement;

/**
 * Z = 1 2 3 2 1 [ 0 ... 0] for len = 3 (from monthly growth to quarterly
 * growth) Z = 1 2 1 for len = 2 Z = 1 2 3 4 3 2 1 for len = 4 ...
 *
 * @author Jean Palate
 */
public class CumulatedVariationsMeasurement implements IDfmMeasurement {

    public static final CumulatedVariationsMeasurement MCD3 = new CumulatedVariationsMeasurement(3);

    public CumulatedVariationsMeasurement(int l) {
        len = l;
    }
    private final int len;

    @Override
    public int getLength() {
        return 2 * len - 1;
    }

    @Override
    public void fill(DataBlock z) {
        int n = (len << 1) - 1;
        for (int i = 1; i < len; ++i) {
            z.set(i - 1, i);
            z.set(n - i, i);
        }
        z.set(len - 1, len);
    }

    @Override
    public double dot(DataBlock x) {
        double r = 0;
        int n = (len << 1) - 1;
        for (int i = 1; i < len; ++i) {
            r += i * (x.get(i - 1) + x.get(n - i));
        }
        r += len * x.get(len - 1);
        return r;
    }
}
