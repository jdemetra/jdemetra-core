/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.timeseries;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value(staticConstructor = "of")
public class IrregularDomain implements TimeSeriesDomain<IrregularPeriod> {

    @lombok.NonNull
    LocalDateTime[] timestamps;

    @Override
    public int length() {
        return timestamps.length;
    }

    @Override
    public IrregularPeriod get(int index) throws IndexOutOfBoundsException {
        return IrregularPeriod.of(timestamps[index]);
    }

    @Override
    public LocalDateTime start() {
        return timestamps[0];
    }

    @Override
    public LocalDateTime end() {
        return timestamps[timestamps.length - 1].plusNanos(1);
    }

    @Override
    public boolean contains(LocalDateTime date) {
        return Arrays.binarySearch(timestamps, date) >= 0;
    }

    @Override
    public int indexOf(LocalDateTime date) {
        return Arrays.binarySearch(timestamps, date);
    }

    public static IrregularDomain of(List<LocalDateTime> values) {
        return of(values.toArray(new LocalDateTime[values.size()]));
    }

    @Override
    public int indexOf(IrregularPeriod point) {
        return Arrays.binarySearch(timestamps, point.start());
    }

    @Override
    public boolean contains(IrregularPeriod period) {
        return Arrays.binarySearch(timestamps, period.start()) >= 0;
    }

    @Override
    public Iterator<IrregularPeriod> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TimeSeriesDomain<IrregularPeriod> select(TimeSeriesSelector selector) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
