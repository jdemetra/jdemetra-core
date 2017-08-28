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
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value(staticConstructor = "of")
public class IrregularDomain implements TsDomain<LocalDateTime> {

    @lombok.NonNull
    List<LocalDateTime> timestamps;

    @Override
    public int length() {
        return timestamps.size();
    }

    @Override
    public LocalDateTime get(int index) throws IndexOutOfBoundsException {
        return timestamps.get(index);
    }

    @Override
    public LocalDateTime start() {
        return timestamps.get(0);
    }

    @Override
    public LocalDateTime end() {
        return timestamps.get(timestamps.size() - 1).plusNanos(1);
    }

    @Override
    public boolean contains(LocalDateTime date) {
        return timestamps.contains(date);
    }

    @Override
    public int indexOf(LocalDateTime date) {
        return timestamps.indexOf(date);
    }

    public static IrregularDomain of(LocalDateTime... values) {
        return of(Arrays.asList(values));
    }
}
