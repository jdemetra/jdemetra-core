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
import ec.tstoolkit.design.PrototypePattern;
import ec.tstoolkit.maths.linearfilters.RationalBackFilter;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IOutlierVariable extends ITsVariable {

    public static class FilterRepresentation {

        public final RationalBackFilter filter;
        public final double correction;

        public FilterRepresentation(RationalBackFilter filter, double correction) {
            this.filter = filter;
            this.correction = correction;
        }

    }

    @Override
    default void data(TsDomain domain, List<DataBlock> data) {
        data(domain.getStart(), data.get(0));
    }

    @Override
    default TsDomain getDefinitionDomain(){
        return null;
    }
    
    @Override
    default TsFrequency getDefinitionFrequency(){
        return TsFrequency.Undefined;
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
    default OutlierType getOutlierType() {
        try {
            return OutlierType.valueOf(getCode());
        } catch (IllegalArgumentException err) {
            return OutlierType.Undefined;
        }
    }

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
     * @param freq
     * @return 
     */
    FilterRepresentation getFilterRepresentation(int freq);

}
