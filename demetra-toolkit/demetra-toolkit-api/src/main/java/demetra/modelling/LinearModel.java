/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.modelling;

import demetra.data.DoubleSeq;
import demetra.math.matrices.Matrix;

/**
 * Describe a raw linear model
 * y(t) = X(t)b+u(t)
 * @author PALATEJ
 */
@lombok.Value
public class LinearModel<M> {
    @lombok.NonNull
    /**
     * Exogenous variable. Might contain missing values 
     */
    DoubleSeq y;

    /**
     * Regression variables
     */
    Matrix X;
    
    /**
     * 
     */
    M stochasticModel;
    
}
