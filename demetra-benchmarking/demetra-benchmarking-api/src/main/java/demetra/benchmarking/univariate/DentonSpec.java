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
package demetra.benchmarking.univariate;

import demetra.processing.AlgorithmDescriptor;
import demetra.data.AggregationType;
import demetra.design.Development;
import demetra.processing.ProcSpecification;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true)
public class DentonSpec implements ProcSpecification {

    public static final AlgorithmDescriptor ALGORITHM = new AlgorithmDescriptor("benchmarking", "denton", null);

    private boolean multiplicative, modified;
    private int differencing;
    @lombok.NonNull
    private AggregationType aggregationType;

    public static DentonSpecBuilder builder() {
        return new DentonSpecBuilder()
                .multiplicative(true)
                .modified(true)
                .differencing(1)
                .aggregationType(AggregationType.Sum);
    }

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return ALGORITHM;
    }

    public static final DentonSpec DEFAULT = builder().build();

}
