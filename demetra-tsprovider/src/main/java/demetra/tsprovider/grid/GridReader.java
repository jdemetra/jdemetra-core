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

import static demetra.tsprovider.grid.GridLayout.HORIZONTAL;
import static demetra.tsprovider.grid.GridLayout.UNKNOWN;
import static demetra.tsprovider.grid.GridLayout.VERTICAL;
import demetra.tsprovider.util.MultiLineNameUtil;
import demetra.tsprovider.util.ObsFormat;
import demetra.tsprovider.util.ObsGathering;
import demetra.tsprovider.util.TsDataBuilder;
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
import internal.tsprovider.grid.ValueReader;
import java.util.function.IntFunction;

/**
 *
 * @author Philippe Charles
 */
@NotThreadSafe
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GridReader {

    @Nonnull
    public static GridReader of(@Nonnull GridImport options, @Nonnull GridInfo info) {
        return of(options, info, DEFAULT_NAMER);
    }

    @Nonnull
    public static GridReader of(@Nonnull GridImport options, @Nonnull GridInfo info, @Nonnull IntFunction<String> namer) {
        ValueReaders readers = ValueReaders.of(info, options.getObsFormat());
        ObsGathering gathering = options.getObsGathering();
        Collector<CharSequence, ?, String> nameJoiner = Collectors.joining(DEFAULT_NAME_SEPARATOR);
        return new GridReader(readers, gathering, namer, nameJoiner);
    }

    private static final IntFunction<String> DEFAULT_NAMER = i -> "S" + i;
    private static final String DEFAULT_NAME_SEPARATOR = MultiLineNameUtil.SEPARATOR;

    private final ValueReaders readers;
    private final ObsGathering gathering;
    private final IntFunction<String> namer;
    private final Collector<CharSequence, ?, String> nameJoiner;

    @Nonnull
    public TsCollectionGrid read(@Nonnull GridInput input) {
        DateHeader rowDates = getRowDates(input);
        DateHeader colDates = getColDates(input);

        TsCollectionGrid.Builder result = TsCollectionGrid.builder().name(input.getName());

        if (rowDates.isBetterThan(colDates)) {
            result.layout(VERTICAL);
            loadVertically(input, rowDates, result::item);
        } else if (colDates.isBetterThan(rowDates)) {
            result.layout(HORIZONTAL);
            loadVertically(InvGridInput.of(input), colDates, result::item);
        } else {
            result.layout(UNKNOWN);
        }

        return result.build();
    }

    private DateHeader getRowDates(GridInput grid) {
        DateHeader result = new DateHeader(grid.getRowCount());
        for (int i = 0; i < grid.getRowCount(); i++) {
            result.set(i, readers.readDateTime(grid, i, 0));
        }
        return result;
    }

    private DateHeader getColDates(GridInput grid) {
        DateHeader result = new DateHeader(grid.getColumnCount());
        for (int j = 0; j < grid.getColumnCount(); j++) {
            result.set(j, readers.readDateTime(grid, 0, j));
        }
        return result;
    }

    private void loadVertically(GridInput grid, DateHeader dates, Consumer<TsGrid> consumer) {
        List<String> names = new ArrayList<>();
        loadHorizontalNames(grid, dates.minIndex, names::add);

        TsDataBuilder<LocalDateTime> data = TsDataBuilder.byDateTime(gathering);
        for (int column = 0; column < names.size(); column++) {
            for (int row = dates.getMinIndex(); row <= dates.getMaxIndex(); row++) {
                Number value = readers.readNumber(grid, row, column + FIRST_DATA_COL_IDX);
                data.add(dates.get(row), value);
            }
            consumer.accept(TsGrid.of(names.get(column), data.build()));
            data.clear();
        }
    }

    private void loadHorizontalNames(GridInput grid, int level, Consumer<String> consumer) {
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

    private void loadHorizontalNamesNoheader(GridInput grid, Consumer<String> consumer) {
        for (int column = FIRST_DATA_COL_IDX; column < grid.getColumnCount(); column++) {
            if (readers.readNumber(grid, 0, column) == null) {
                break;
            }
            consumer.accept(namer.apply(column));
        }
    }

    private void loadHorizontalNamesSingleheader(GridInput grid, Consumer<String> consumer) {
        for (int column = FIRST_DATA_COL_IDX; column < grid.getColumnCount(); column++) {
            String name = readers.readString(grid, 0, column);
            if (name == null) {
                break;
            }
            consumer.accept(name);
        }
    }

    private void loadHorizontalNamesMultiHeaders(GridInput grid, int level, Consumer<String> consumer) {
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

        static ValueReaders of(GridInfo info, ObsFormat obsFormat) {
            ValueReader<String> toString;
            ValueReader<LocalDateTime> toDateTimeFallback;
            ValueReader<Number> toNumberFallback;

            if (info.isSupportedDataType(String.class)) {
                toString = ValueReader.onString();
                toDateTimeFallback = ValueReader.onStringParser(obsFormat.dateTimeParser(LocalDateTime::from));
                toNumberFallback = ValueReader.onStringParser(obsFormat.numberParser());
            } else {
                toString = ValueReader.onNull();
                toDateTimeFallback = ValueReader.onNull();
                toNumberFallback = ValueReader.onNull();
            }

            ValueReader<LocalDateTime> toDateTime
                    = info.isSupportedDataType(LocalDateTime.class)
                    ? ValueReader.onDateTime().or(toDateTimeFallback)
                    : toDateTimeFallback;

            ValueReader<Number> toNumber
                    = info.isSupportedDataType(Number.class)
                    ? ValueReader.onNumber().or(toNumberFallback)
                    : toNumberFallback;

            return new ValueReaders(toString, toDateTime, toNumber);
        }

        private final ValueReader<String> toString;
        private final ValueReader<LocalDateTime> toDateTime;
        private final ValueReader<Number> toNumber;

        public String readString(GridInput grid, int row, int column) {
            return toString.read(grid, row, column);
        }

        public LocalDateTime readDateTime(GridInput grid, int row, int column) {
            return toDateTime.read(grid, row, column);
        }

        public Number readNumber(GridInput grid, int row, int column) {
            return toNumber.read(grid, row, column);
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
