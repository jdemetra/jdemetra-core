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
package demetra.likelihood;

import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.maths.MatrixType;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@Development(status=Development.Status.Release)
public class MaximumLogLikelihood {
    /**
     * Max of the log-likelihood
     */
    private double value;
    /**
     * Parameters that maximize the likelihood
     */
    private DoubleSequence parameters;
    /**
     * Gradient of the log likelihood function (=score) at its maximum.
     * Should be very near 0
     */
    private DoubleSequence gradient;
    /**
     * Hessian of the log likelihood function at its maximum. 
     * E(hessian)=Information 
     */
    private MatrixType hessian;
}
