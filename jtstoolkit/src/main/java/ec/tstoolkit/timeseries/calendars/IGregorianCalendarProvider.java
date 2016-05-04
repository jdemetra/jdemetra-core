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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface IGregorianCalendarProvider {

    /**
     *
     * @param dtype
     * @param domain
     * @param buffer
     * @param start
     */
    @Deprecated
    void calendarData(TradingDaysType dtype,
            TsDomain domain, List<DataBlock> buffer, int start);

     void calendarData(TradingDaysType dtype,
            TsDomain domain, List<DataBlock> buffer);
    /**
     * 
     * @param dtype
     * @param domain
     * @return
     */
    List<DataBlock> holidays(TradingDaysType dtype, TsDomain domain);

    int count(TradingDaysType type);

    String getDescription(TradingDaysType dtype, int idx);


}
