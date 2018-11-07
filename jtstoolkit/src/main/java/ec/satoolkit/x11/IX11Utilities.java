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

package ec.satoolkit.x11;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 * Defines some utility functions used in a normal X11 processing. 
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IX11Utilities extends IX11Algorithm{

    /**
     * Replace negative values in the given series
     * @param s The considered series (could be changed)
     * @return False if the series is not changed, True if some data of the 
     * series have been changed)
     */
    boolean checkPositivity(TsData s);

    /**
     * Corrects a series, using weights attached to the observations and 
     * a given alternative value. The default implementation simply consists in
     * replacing the values corresponding to a weight equal to 0 by the alternative
     * value (see DefaultX11Utilities)
     * @param sorig The original series
     * @param sweights The weights of the values of the initial series
     * @param dalternative the alternative value
     * @return The corrected series
     */
    TsData correctSeries(TsData sorig, TsData sweights, double dalternative);

    /**
     * Corrects a series, using weights attached to the observations and 
     * an alternative series. The default implementation simply consists in
     * replacing the values corresponding to a weight equal to 0 by the corresponding
     * value in the alternative series (see DefaultX11Utilities).
     * 
     * @param sorig The series being corrected
     * @param sweights The weights of the different data of the series
     * @param salternative The series containing the alternative values
     * @return The corrected series
     */
    TsData correctSeries(TsData sorig, TsData sweights, TsData salternative);

    /**
     *
     * @param t
     * @param s
     * @param i
     * @param bias
     * @return
     */
    TsData correctTrendBias(TsData t, TsData s, TsData i, BiasCorrection bias);

    /**
     *
     * @param t
     * @param s
     * @param i
     * @return
     */
    default TsData correctTrendBias(TsData t, TsData s, TsData i){
        return correctTrendBias(t, s, i, BiasCorrection.Legacy);
    }
    /**
     *
     * @param l
     * @param r
     * @return
     */
    TsData differences(TsData l, TsData r);
}
