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

package ec.tstoolkit.timeseries.calendars;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public interface ISpecialDay {
    /**
     * Special Day for the given year
     * @param freq
     * @param start
     * @param end
     * @return
     */
    Iterable<IDayInfo> getIterable(TsFrequency freq, Day start, Day end);

    /**
     * Gives the long term mean effect on each day of week for each period of a
     * given frequency
     * @param freq
     * @return The first dimension identifies the period (in [0, freq[,
     * the second dimension identifies the day (from Monday to Sunday).
     * Could be null or could contain null arrays.
     */
    double[][] getLongTermMeanEffect(int freq);
    
    /**
     * Gets the domain where the special day is significant (not zero).
     * @param freq The frequency of the domain
     * @param start The start of the validity period (included)
     * @param end The end of the validity period (included)
     * @return 
     */
    TsDomain getSignificantDomain(TsFrequency freq, Day start, Day end);
    
    double getWeight();
}
