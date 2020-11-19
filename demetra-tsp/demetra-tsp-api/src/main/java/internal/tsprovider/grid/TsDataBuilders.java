/*
 * Copyright 2020 National Bank of Belgium
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
package internal.tsprovider.grid;

import demetra.timeseries.TsData;
import demetra.timeseries.util.ObsCharacteristics;
import demetra.timeseries.util.ObsGathering;
import demetra.timeseries.util.TsDataBuilder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 * @param <DATE>
 */
//@MightBePromoted
@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TsDataBuilders<DATE> {

    @NonNull
    public static TsDataBuilders<LocalDateTime> byDateTime(@NonNegative int count, @NonNull ObsGathering gathering, @NonNull ObsCharacteristics... characteristics) {
        return new TsDataBuilders<>(IntStream.range(0, count)
                .mapToObj(i -> TsDataBuilder.byDateTime(gathering, characteristics))
                .collect(Collectors.toList()));
    }

    private final List<TsDataBuilder<DATE>> builders;

    @NonNegative
    public int getCount() {
        return builders.size();
    }

    @NonNull
    public TsDataBuilders<DATE> clear() {
        builders.forEach(TsDataBuilder::clear);
        return this;
    }

    @NonNull
    public TsDataBuilders<DATE> add(@NonNegative int index, @Nullable DATE date, @Nullable Number value) {
        builders.get(index).add(date, value);
        return this;
    }

    @NonNull
    public TsData build(@NonNegative int index) {
        return builders.get(index).build();
    }
}
