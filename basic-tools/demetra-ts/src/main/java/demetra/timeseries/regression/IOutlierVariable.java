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
package demetra.timeseries.regression;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.maths.linearfilters.RationalBackFilter;
import demetra.timeseries.IRegularPeriod;
import demetra.timeseries.ITimeDomain;
import demetra.timeseries.ITimePeriod;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author Jean Palate
 * @param <E>
 */
@Development(status = Development.Status.Alpha)
public interface IOutlierVariable<E extends IRegularPeriod> extends ITsVariable<E> {

    public static class FilterRepresentation {

        public final RationalBackFilter filter;
        public final double correction;

        public FilterRepresentation(RationalBackFilter filter, double correction) {
            this.filter = filter;
            this.correction = correction;
        }
    }

    @Override
    default void data(ITimeDomain<E> domain, List<DataBlock> buffer) {
        E start=domain.get(0);
        data(start, buffer.get(0));
    }
    
    /**
     *
     * @param start
     * @param buffer
     */
    void data(E start, DataBlock buffer);
    

    /**
     *
     * @return
     */
    String getCode();

    /**
     *
     * @return
     */
    LocalDateTime getPosition();

    /**
     * 
     * @param freq
     * @return 
     */
    FilterRepresentation getFilterRepresentation();

}
