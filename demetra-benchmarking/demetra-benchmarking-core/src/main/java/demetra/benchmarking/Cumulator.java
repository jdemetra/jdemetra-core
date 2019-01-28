/*
 * Copyright 2017 National Bank of Belgium
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

package demetra.benchmarking;

import demetra.data.DataBlock;
import demetra.design.Development;

/**
 * Cumulator of data d.
 * data[i] = d[i] +data[i-1] except when i%period == 0.
 * In that case data[i] = d[i]
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Cumulator {

    private final int period;

    /**
     * 
     * @param n
     */
    public Cumulator(int n) {
        this.period = n;
    }

    /**
     * 
     * @param data
     */
    public void transform(DataBlock data) {
        int pos = 0;
        for (int i = pos, j = 0; i < data.length(); ++i) {
            if (j++ > 0) {
                data.add(i, data.get(i - 1));
                if (j == period) {
                    j = 0;
                }
            }
        }
    }

}
