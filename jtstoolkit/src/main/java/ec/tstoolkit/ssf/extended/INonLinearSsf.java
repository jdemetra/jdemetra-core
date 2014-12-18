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


package ec.tstoolkit.ssf.extended;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.ssf.ISsf;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public interface INonLinearSsf extends Cloneable {
    // / <summary>
    // / Linear approximation, at a given states array.
    // / </summary>
    // / <param name="observations">The observations are modified to correspond
    // to the liear approximation</param>
    // / <param name="states"></param>
    // / <returns></returns>
    /**
     *
     * @param observations
     * @param states
     * @return
     */
    ISsf linearApproximation(DataBlock observations, DataBlockStorage states);

    /**
     *
     * @param pos
     * @param x
     */
    void TX(int pos, DataBlock x);

    /**
     *
     * @param pos
     * @param x
     * @return
     */
    double Z(int pos, DataBlock x);

}
