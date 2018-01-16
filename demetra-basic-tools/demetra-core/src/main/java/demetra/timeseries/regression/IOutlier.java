/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
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
import demetra.design.ServiceDefinition;
import demetra.maths.linearfilters.RationalBackFilter;
import demetra.timeseries.RegularDomain;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IOutlier<D extends TsDomain<?>> extends ITsVariable<D> {

    public static class FilterRepresentation {

        public final RationalBackFilter filter;
        public final double correction;

        public FilterRepresentation(RationalBackFilter filter, double correction) {
            this.filter = filter;
            this.correction = correction;
        }
    }

    /**
     * Interface for the creation of outlier variable
     *
     * @author Jean Palate
     */
    @Development(status = Development.Status.Release)
    @ServiceDefinition
    public interface IOutlierFactory {

        /**
         * Creates an outlier at the given position
         *
         * @param position The position of the outlier.
         * @return A new variable is returned.
         */
        IOutlier make(LocalDateTime position);

        /**
         * Fills the buffer with an outlier positioned at outlierPosition. The
         * position should be insied the buffer
         *
         * @param outlierPosition
         * @param buffer
         */
        void fill(int outlierPosition, DataBlock buffer);

        /**
         * Filter representation of this type of outlier.
         * @return
         */
        FilterRepresentation getFilterRepresentation();

        /**
         * Some outliers cannot be identified at the beginning of a series. This
         * method returns the number of such periods
         *
         * @return A positive or zero integer
         */
        int excludingZoneAtStart();

        /**
         * Some outliers cannot be identified at the end of a series. This
         * method returns the number of such periods
         *
         * @return A positive or zero integer
         */
        int excludingZoneAtEnd();

        /**
         * The code that represents the outlier
         *
         * @return
         */
        String getCode();
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
    LocalDateTime getPosition();

}
