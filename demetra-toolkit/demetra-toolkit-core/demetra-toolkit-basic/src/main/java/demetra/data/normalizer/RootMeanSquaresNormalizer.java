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

package demetra.data.normalizer;

import demetra.data.DataBlock;
import demetra.design.AlgorithmImplementation;
import demetra.design.Development;

/**
 * Normalization based on the mean of the absolute values. 
 * The scaling factor is the inverse of the mean of the absolute values of the data.
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@AlgorithmImplementation(algorithm=DataNormalizer.class)
public class RootMeanSquaresNormalizer implements DataNormalizer {

    @Override
    public double normalize(DataBlock data) {

	final int n = data.length();
        double norm = data.norm2();
        double c=Math.sqrt(n)/norm;
        data.mul(c);
        return c;
    }
}
