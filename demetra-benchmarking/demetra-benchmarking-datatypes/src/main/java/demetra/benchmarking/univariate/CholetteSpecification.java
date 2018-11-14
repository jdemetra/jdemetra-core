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
package demetra.benchmarking.univariate;

import demetra.algorithms.AlgorithmDescriptor;
import demetra.data.AggregationType;
import demetra.processing.ProcSpecification;

/**
 *
 * @author Jean Palate
 */
@lombok.Data
public class CholetteSpecification implements ProcSpecification {

    public static final AlgorithmDescriptor ALGORITHM = new AlgorithmDescriptor("benchmarking", "cholette", null);

    public static enum BiasCorrection {

        None, Additive, Multiplicative
    };

    public static BiasCorrection DEF_BIAS = BiasCorrection.None;
    public static double DEF_LAMBDA = 1, DEF_RHO = 1;
    private double rho = DEF_RHO;
    private double lambda = DEF_LAMBDA;
    private BiasCorrection bias = DEF_BIAS;
    @lombok.NonNull
    private AggregationType aggregationType = AggregationType.Sum;

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return ALGORITHM;
    }

    @Override
    public CholetteSpecification makeCopy() {
        CholetteSpecification spec = new CholetteSpecification();
        spec.rho = rho;
        spec.lambda = lambda;
        spec.bias=bias;
        spec.aggregationType=aggregationType;
        return spec;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Cholette ");
        builder.append(" (lambda=").append(lambda)
                .append(", rho=").append(rho)
                .append(", bias=").append(bias).append(')');
        return builder.toString();
    }
}
