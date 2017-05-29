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
import demetra.processing.IProcSpecification;

/**
 *
 * @author Jean Palate
 */
@lombok.Data
public class CholetteSpecification implements IProcSpecification {
    public static final AlgorithmDescriptor ALGORITHM = new AlgorithmDescriptor("benchmarking", "denton", null);

    public static double DEF_LAMBDA = 1, DEF_RHO = 1;
    private double rho = DEF_RHO;
    private double lambda = DEF_LAMBDA;
    

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return ALGORITHM;
    }

    @Override
    public CholetteSpecification makeCopy() {
        CholetteSpecification spec=new CholetteSpecification();
        spec.rho=rho;
        spec.lambda=lambda;
        return spec;
    }
    
}
