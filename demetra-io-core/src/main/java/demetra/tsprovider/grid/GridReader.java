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
package demetra.tsprovider.grid;

import demetra.tsprovider.Ts;
import demetra.tsprovider.TsCollection;
import demetra.tsprovider.TsInformationType;
import static demetra.tsprovider.grid.GridLayout.HORIZONTAL;
import static demetra.tsprovider.grid.GridLayout.UNKNOWN;
import static demetra.tsprovider.grid.GridLayout.VERTICAL;
import demetra.tsprovider.util.MultiLineNameUtil;
import demetra.tsprovider.util.ObsFormat;
import demetra.tsprovider.util.ObsGathering;
import demetra.tsprovider.util.TsDataBuilder;
import demetra.util.Substitutor;
import internal.tsprovider.grid.InvGridInput;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import lombok.AccessLevel;
import internal.tsprovider.grid.InternalValueReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Philippe Charles
 */
@NotThreadSafe
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GridReader {

    @Nonnull
    public static GridReader of(@Nonnull GridImport options, @Nonnull GridInfo info) {
        ValueReaders readers = ValueReaders.of(info, options.getFormat());
        ObsGathering gathering = options.getGathering();
        Collector<CharSequence, ?, String> nameJoiner = Collectors.joining(DEFAULT_NAME_SEPARATOR);
        return new GridReader(readers, gathering, options.getNamePattern(), nameJoiner);
    }

    private static final String DEFAULT_NAME_SEPARATOR = MultiLineNameUtil.SEPARATOR;

    private final ValueReaders readers;
    private final ObsGathering gathering;
    private final String namePattern;
    private final Collector<CharSequence, ?, String> nameJoiner;

    @Nonnull
    public TsCollection read(@Nonnull GridInput input) throws IOException {
        DateHeader rowDates = getRowDates(input);
        DateHeader colDates = getColDates(input);

        TsCollection.Builder result = TsCollection.builder()
                .type(TsInformationType.Data)
                .name(input.getName());

        if (rowDates.isBetterThan(colDates)) {
            result.meta("gridLayout", VERTICAL.name());
            loadVertically(input, rowDates, result::data);
        } else if (colDates.isBetterThan(rowDates)) {
            result.meta("gridLayout", HORIZONTAL.name());
            loadVertically(InvGridInput.of(input), colDates, result::data);
        } else {
            result.meta("gridLayout", UNKNOWN.name());
        }

        return result.build();
    }

    private DateHeader getRowDates(GridInput grid) throws IOException {
        DateHeader result = new DateHeader(grid.getRowCount());
        for (int i = 0; i < grid.getRowCount(); i++) {
            result.set(i, readers.readDateTime(grid, i, 0));
        }
        return result;
    }

    private DateHeader getColDates(GridInput grid) throws IOException {
        DateHeader result = new DateHeader(grid.getColumnCount());
        for (int j = 0; j < grid.getColumnCount(); j++) {
            result.set(j, readers.readDateTime(grid, 0, j));
        }
        return result;
    }

    private void loadVertically(GridInput grid, DateHeader dates, Consumer<Ts> consumer) throws IOException {
        List<String> names = new ArrayList<>();
        loadHorizontalNames(grid, dates.minIndex, names::add);

        TsDataBuilder<LocalDateTime> data = TsDataBuilder.byDateTime(gathering);
        for (int column = 0; column < names.size(); column++) {
            for (int row = dates.getMinIndex(); row <= dates.getMaxIndex(); row++) {
                Number value = readers.readNumber(grid, row, column + FIRST_DATA_COL_IDX);
                data.add(dates.get(row), value);
            }
            consumer.accept(Ts.builder()
                    .type(TsInformationType.Data)
                    .name(names.get(column))
                    .data(data.build())
                    .build());
            data.clear();
        }
    }

    private void loadHorizontalNames(GridInput grid, int level, Consumer<String> consumer) throws IOException {
        switch (level) {
            case 0: {
                loadHorizontalNamesNoheader(grid, consumer);
                break;
            }
            case 1: {
                loadHorizontalNamesSingleheader(grid, consumer);
                break;
            }
            default: {
                loadHorizontalNamesMultiHeaders(grid, level, consumer);
                break;
            }
        }
    }

    private void loadHorizontalNamesNoheader(GridInput grid, Consumer<String> consumer) throws IOException {
        Map<String, Object> mapper = new HashMap<>();
        Substitutor substitutor = Substitutor.of(mapper);
        for (int column = FIRST_DATA_COL_IDX; column < grid.getColumnCount(); column++) {
            if (readers.readNumber(grid, 0, column) == null) {
                break;
            }
            mapper.put("index", column - FIRST_DATA_COL_IDX);
            consumer.accept(substitutor.replace(namePattern));
        }
    }

    private void loadHorizontalNamesSingleheader(GridInput grid, Consumer<String> consumer) throws IOException {
        for (int column = FIRST_DATA_COL_IDX; column < grid.getColumnCount(); column++) {
            String name = readers.readString(grid, 0, column);
            if (name == null) {
                break;
            }
            consumer.accept(name);
        }
    }

    private void loadHorizontalNamesMultiHeaders(GridInput grid, int level, Consumer<String> consumer) throws IOException {
        String[] path = new String[level];
        for (int column = FIRST_DATA_COL_IDX; column < grid.getColumnCount(); column++) {
            boolean hasHeader = false;
            for (int row = 0; row < path.length; row++) {
                String name = readers.readString(grid, row, column);
                if (name != null) {
                    hasHeader = true;
                    path[row] = name;
                } else if (hasHeader) {
                    path[row] = null;
                }
            }
            if (!hasHeader) {
                break;
            }
            consumer.accept(joinSkippingNulls(path));
        }
    }

    private String joinSkippingNulls(String[] items) {
        return Stream.of(items).filter(Objects::nonNull).collect(nameJoiner);
    }

    private static final int FIRST_DATA_COL_IDX = 1;

    @lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ValueReaders {

        static ValueReaders of(GridInfo info, ObsFormat format) {
            InternalValueReader<String> string;
            InternalValueReader<LocalDateTime> dateTimeFallback;
            InternalValueReader<Number> numberFallback;

            if (info.isSupportedDataType(String.class)) {
                string = InternalValueReader.onString();
                dateTimeFallback = InternalValueReader.onStringParser(format.dateTimeParser(LocalDateTime::from));
                numberFallback = InternalValueReader.onStringParser(format.numberParser());
            } else {
                string = InternalValueReader.onNull();
                dateTimeFallback = InternalValueReader.onNull();
                numberFallback = InternalValueReader.onNull();
            }

            InternalValueReader<LocalDateTime> dateTime
                    = info.isSupportedDataType(LocalDateTime.class)
                    ? InternalValueReader.onDateTime().or(dateTimeFallback)
                    : dateTimeFallback;

            InternalValueReader<Number> number
                    = info.isSupportedDataType(Number.class)
                    ? InternalValueReader.onNumber().or(numberFallback)
                    : numberFallback;

            return new ValueReaders(string, dateTime, number);
        }

        private final InternalValueReader<String> string;
        private final InternalValueReader<LocalDateTime> dateTime;
        private final InternalValueReader<Number> number;

        public String readString(GridInput grid, int row, int column) throws IOException {
            return string.read(grid, row, column);
        }

        public LocalDateTime readDateTime(GridInput grid, int row, int column) throws IOException {
            return dateTime.read(grid, row, column);
        }

        public Number readNumber(GridInput grid, int row, int column) throws IOException {
            return number.read(grid, row, column);
        }
    }

    private static final class DateHeader {

        private final LocalDateTime[] dates;
        private int minIndex;
        private int maxIndex;

        private DateHeader(int maxSize) {
            dates = new LocalDateTime[maxSize];
            minIndex = maxSize - 1;
            maxIndex = 0;
        }

        public void set(int i, LocalDateTime value) {
            dates[i] = value;
            if (value != null) {
                if (i < minIndex) {
                    minIndex = i;
                }
                if (i > maxIndex) {
                    maxIndex = i;
                }
            }
        }

        public LocalDateTime get(int i) {
            return dates[i];
        }

        public int getMinIndex() {
            return minIndex;
        }

        public int getMaxIndex() {
            return maxIndex;
        }

        public boolean isBetterThan(DateHeader other) {
            int count = maxIndex - minIndex + 1;
            return count > 0 && count > (other.maxIndex - other.minIndex + 1);
        }
    }
}
