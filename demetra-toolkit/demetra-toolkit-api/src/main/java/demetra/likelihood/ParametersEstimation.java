/*
 * Copyright 2021 National Bank of Belgium
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
package demetra.likelihood;

import demetra.data.DoubleSeq;
import nbbrd.design.Development;
import demetra.math.matrices.MatrixType;

/**
 * Estimated parameters in the context of maximum likelihood estimation
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@Development(status = Development.Status.Release)
public class ParametersEstimation {
    /**
     * Estimated values of the parameters
     */
    @lombok.NonNull
    private DoubleSeq values;
    /**
     * Covariance of the parameters.
     * Pre-specified parameters should have a variance equal to 0
     */
    private MatrixType covariance;
    /**
     * Score of the parameter (in the context of maximum likelihood
     */
    private DoubleSeq scores;
    /**
     * Any suitable information/description
     */
    private String description;
}
