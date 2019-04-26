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
package demetra.data.accumulator;

import demetra.design.AlgorithmImplementation;
import static demetra.design.AlgorithmImplementation.Feature.Balanced;

/**
 * Kahan and Babuska summation, Neumaier variant
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@AlgorithmImplementation(algorithm=DoubleAccumulator.class, feature=Balanced)
public final class NeumaierAccumulator implements DoubleAccumulator {

    private double del, sum;

    @Override
    public void reset() {
        del = 0;
        sum = 0;
    }

    @Override
    public void add(double x) {
        double t = sum + x;
        if (Math.abs(sum) >= Math.abs(x)) {
            del += (sum - t) + x;
        } else {
            del += (x - t) + sum;
        }
        sum = t;
    }

    @Override
    public double sum() {
        return sum + del;
    }

    @Override
    public void set(double value) {
        del = 0;
        sum = value;
    }

}
