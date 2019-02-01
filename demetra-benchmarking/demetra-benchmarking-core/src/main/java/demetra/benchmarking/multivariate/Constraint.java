/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.benchmarking.multivariate;

import java.util.HashMap;

/**
 *
 * @author Jean Palate
 */
@lombok.Data
class Constraint {

    /**
     *
     */
    final int[] index;
    /**
     *
     */
    final double[] weights;

    /**
     *
     * @param cnt
     */
    Constraint(HashMap<Integer, Double> cnt) {
        index = new int[cnt.size()];
        int j = 0;
        for (Integer i : cnt.keySet()) {
            index[j++] = i;
        }
        java.util.Arrays.sort(index);
        weights = new double[index.length];
        for (int i = 0; i < index.length; ++i) {
            weights[i] = cnt.get(index[i]);
        }
    }
}
