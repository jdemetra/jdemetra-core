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

package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.RationalBackFilter;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IOutlierVariable extends ITsVariable {

    public static class FilterRepresentation{
        public final RationalBackFilter filter;
        public final double correction;

        public FilterRepresentation (RationalBackFilter filter, double correction){
            this.filter=filter;
            this.correction=correction;
        }

    }

    /**
     * 
     * @param start
     * @param buffer
     */
    void data(TsPeriod start, DataBlock buffer);

    /**
     * 
     * @return
     */
    @Deprecated
    OutlierType getOutlierType();

    /**
     * 
     * @return
     */
    String getCode();
    /**
     * 
     * @return
     */
    Day getPosition();

    /**
     *
     * @return
     */
    boolean isPrespecified();

    /**
     * 
     * @param value
     */
    void setPrespecified(boolean value);

    FilterRepresentation getFilterRepresentation(int freq);

}
