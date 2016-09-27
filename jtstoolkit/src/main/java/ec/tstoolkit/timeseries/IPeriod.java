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
package ec.tstoolkit.timeseries;

import ec.tstoolkit.design.Development;
import java.util.Date;

/**
 * interface that defines a time period. The atomic time unit for a IPeriod is the day. 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IPeriod {

    /**
     * Checks that the period contains a given date
     * 
     * @param dt
     *            The considered date.
     * @return true if the date is inside the period, false otherwise.
     */
    public boolean contains(Date dt);

    /**
     *Gets the first day of the period
     * @return The first day of the period.
     */
    public Day firstday();

    /**
     * Gets the last day of the period
     * 
     * @return The last day of the period
     */
    public Day lastday();

}
