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
package demetra.benchmarking.univariate;

import demetra.algorithms.AlgorithmDescriptor;
import demetra.data.AggregationType;
import demetra.processing.IProcSpecification;

/**
 *
 * @author Jean Palate
 */
@lombok.Data
public class DentonSpecification implements IProcSpecification {

    public static final AlgorithmDescriptor ALGORITHM = new AlgorithmDescriptor("benchmarking", "denton", null);

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(multiplicative ? "Mul. " : "Add. ").append("denton ");
        builder.append(" (D=").append(differencing).append(", mod=").append(modified).append(')');
        return builder.toString();
    }

    private boolean multiplicative = true, modified = true;
    private int differencing = 1;
    @lombok.NonNull
    private AggregationType aggregationType = AggregationType.Sum;

    @Override
    public DentonSpecification makeCopy() {
        DentonSpecification spec = new DentonSpecification();
        spec.multiplicative = multiplicative;
        spec.modified = modified;
        spec.differencing = differencing;
        spec.aggregationType = aggregationType;
        return spec;
    }

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return ALGORITHM;
    }

}
