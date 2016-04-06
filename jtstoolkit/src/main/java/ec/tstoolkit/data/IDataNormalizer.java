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

package ec.tstoolkit.data;

import ec.tstoolkit.design.Development;

/**
 * This interface is aimed at providing normalizing procedure, which re-scales
 * An array of data. It should be used in any sensitive numerical computation.
 * We have that y(normalized) = factor * y(initial), where 
 * - y(initial) is the input parameter of the "process" method,
 * - factor is given by getFactor();
 * - y(normalized) are the normalized data, given by getNormalizedData();
 * So factor is usually the inverse of some measure of the mean size of the data.
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Deprecated
public interface IDataNormalizer {
    /**
     * Returns the scaling factor
     * @return
     */
    double getFactor();

    // / <summary></summary>

    /**
     * Returns the normalized data.
     * @return The internal data are returned. The user should make a copy 
     * if he intends to change data.
     */
    double[] getNormalizedData();

    /**
     * Computes the actual normalization. 
     * @param data The data being normalized. The initial data are copied 
     * @return True if the scaling succeeded. Usually, the scaling will not succeed 
     * when all the data are 0 (or identical in some cases)
     */
    boolean process(IReadDataBlock data);
    // / <summary>The normalizing factor</summary>
}
