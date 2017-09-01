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
import demetra.maths.linearfilters.RationalBackFilter;
import demetra.timeseries.RegularDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IRegularOutlier extends IOutlier<RegularDomain> {

    @Override
    default void data(RegularDomain domain, List<DataBlock> buffer) {
        TsPeriod start = domain.get(0);
        data(start, buffer.get(0));
    }

    /**
     *
     * @param start
     * @param buffer
     */
    void data(TsPeriod start, DataBlock buffer);

}
