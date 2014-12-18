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
package ec.tstoolkit.ssf;

import ec.tstoolkit.design.Development;

/**
 * Data corresponding to a given state space form.
 * An object of this class contains not only the observations themselves
 * but also the initial state of the model. 
 * In most cases, the initial state is not defined (null, corresponding to an array of 0).
 * It should be stressed that an ISsfData is always defined in the context of 
 * a specific ssf.
 * Using this definition, the ssf may be considered as a linear transformation which is 
 * applied on the complete set of the data (the observations and the initial state).
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ISsfData {

    /**
     * Gets a given observation
     * @param n The 0-based index of the observation (in [0, getCount()[
     * @return The n-observation. Double.NaN if missing.
     */
    double get(int n);

    /**
     * Gets the number of observations 
     * @return The number of observations, including missing values
     */
    int getCount();

    /**
     * Gets the initial state vector.
     * @return Initial state vector. May be null. In that case, the corresponding
     * ssf will provide the default one (=0).
     * When defined, the length of the initial state vector must be coherent with 
     * the dimension of the ssf.
     */
    double[] getInitialState();

    /**
     * Gets number of actual observations
     * @return The number of observations (excluding missing values)
     */
    int getObsCount();

    /**
     * Checks that this object has data. 
     * @return True if this object has data, false otherwise. Empty ISsfData 
     * could be used for generating the covariance matrices of a ssf. 
     */
    boolean hasData();

    /**
     * Checks that this object contains missing values.
     * @return The number of missing values.
     */
    boolean hasMissingValues();

    /**
     * Checks that a given observation is missing or not
     * @param pos The 0-based index of the considered observation. Should be 
     * in [0, getCount()[
     * @return True if the pos-th observation is missing (Double.NaN)
     */
    boolean isMissing(int pos);
}
