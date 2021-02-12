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
package demetra.modelling;

import demetra.likelihood.ParametersEstimation;

/**
 * Description of the stochastic component
 * The link between the model itself and its parameters is domain-dependent
 * and not expressed in this structure (which means that it could be some
 * redundancy.
 * 
 * @author PALATEJ
 * @param <S>
 */
@lombok.Value
public class StochasticModel<S> {
    /**
     * Stochastic (parametric) model
     */
    S model;
    
    /**
     * Underlying parameters. Fixed parameters should be integrated in the
     * parameters (with 0 variance).
     */
    ParametersEstimation parameters;
}
