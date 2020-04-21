/*
 * Copyright 2019 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
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
package demetra.data;

import demetra.design.Development;


/**
 * Identifies the way a parameter should be interpreted
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public enum ParameterType {
    /**
     * Undefined parameter. Means that the parameter is currently undefined, but
     * that it is presents in the model; it should be estimated.
     */
    Undefined,
    /**
     * The value should be considered as an initial value. It should be estimated, 
     * using that initial information
     */
    Initial,
    /**
     * The value is fixed. It will/has not be(en) estimated.
     */
    Fixed,
    /**
     * The value has been estimated. No further estimation is needed.
     * The differences in comparison with fixed parameters are:
     * - in the computation of statistics (degrees of freedom)
     * - in re-estimation (fixed parameters should not be considered, but
     * estimated parameters should be recomputed)
     */
    Estimated,
//    /**
//     * The value is derived from other parameters. Such parameters are just
//     * added for information (they are not actual parameters)
//     */
//    Derived
}
