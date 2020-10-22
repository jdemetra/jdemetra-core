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
import nbbrd.design.Development;
import demetra.processing.ProcSpecification;
import demetra.util.Validatable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder=true, builderClassName="Builder", buildMethodName="buildWithoutValidation")
public class DentonSpec implements ProcSpecification, Validatable<DentonSpec> {

    public static final AlgorithmDescriptor ALGORITHM = new AlgorithmDescriptor("benchmarking", "denton", null);

    private boolean multiplicative, modified;
    private int differencing;
    @lombok.NonNull
    private AggregationType aggregationType;
    private int observationPosition;

    public static Builder builder() {
        return new Builder()
                .multiplicative(true)
                .modified(true)
                .differencing(1)
                .aggregationType(AggregationType.Sum)
                .observationPosition(0);
    }

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return ALGORITHM;
    }

    @Override
    public DentonSpec validate() throws IllegalArgumentException {
        if (aggregationType == AggregationType.None || aggregationType == AggregationType.Max
                || aggregationType == AggregationType.Min) {
            throw new IllegalArgumentException();
        }
        if (aggregationType == AggregationType.UserDefined && observationPosition<0)
            throw new IllegalArgumentException();
        return this;
    }

    public static class Builder implements Validatable.Builder<DentonSpec>{
        
    }
    public static final DentonSpec DEFAULT = builder().build();

}
