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
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.RationalBackFilter;
import java.time.LocalDateTime;
import java.util.List;
import demetra.timeseries.TimeSeriesDomain;

/**
 *
 * @author Jean Palate
 * @param <D>
 */
public class AdditiveOutlier<D extends TimeSeriesDomain<?>> extends BaseOutlier implements IOutlier<D> {

    public static final String CODE = "AO";

    public static final Factory FACTORY = new Factory();

    public static class Factory implements IOutlierFactory {

        @Override
        public IOutlier make(LocalDateTime position) {
            return new AdditiveOutlier(position);
        }

        @Override
        public void fill(int outlierPosition, DataBlock buffer) {
            buffer.set(outlierPosition, 1);
        }

        @Override
        public FilterRepresentation getFilterRepresentation() {
            return new FilterRepresentation(new RationalBackFilter(
                    BackFilter.ONE, BackFilter.ONE, 0), 0);
        }

        @Override
        public int excludingZoneAtStart() {
            return 0;
        }

        @Override
        public int excludingZoneAtEnd() {
            return 0;
        }

        @Override
        public String getCode() {
            return CODE;
        }

    }

    public AdditiveOutlier(LocalDateTime pos) {
        super(pos, defaultName(CODE, pos, null));
    }

    public AdditiveOutlier(LocalDateTime pos, String name) {
        super(pos, name);
    }

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public LocalDateTime getPosition() {
        return position;
    }

    @Override
    public void data(D domain, List<DataBlock> data) {
        long pos = domain.indexOf(position);
        if (pos >= 0) {
            FACTORY.fill((int) pos, data.get(0));
        }
    }

    @Override
    public String getDescription(D context) {
        return defaultName(CODE, position, context);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ITsVariable<D> rename(String nname) {
        return new AdditiveOutlier(position, nname);
    }

}
