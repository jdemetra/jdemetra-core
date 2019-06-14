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

import demetra.design.LombokWorkaround;
import demetra.tsprovider.Ts;
import demetra.tsprovider.TsCollection;
import demetra.tsprovider.TsInformationType;
import static demetra.tsprovider.grid.GridLayout.HORIZONTAL;
import static demetra.tsprovider.grid.GridLayout.UNKNOWN;
import static demetra.tsprovider.grid.GridLayout.VERTICAL;
import demetra.tsprovider.util.MultiLineNameUtil;
import demetra.tsprovider.util.ObsFormat;
import demetra.timeseries.util.ObsGathering;
import demetra.timeseries.util.TsDataBuilder;
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
import lombok.AccessLevel;
import internal.tsprovider.grid.InternalValueReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public final class GridReader {

    public static final GridReader DEFAULT = builder().build();

    @lombok.NonNull
    private ObsFormat format;

    @lombok.NonNull
    private ObsGathering gathering;

    @lombok.NonNull
    private GridLayout layout;

    @lombok.NonNull
    private String namePattern;

    @lombok.NonNull
    private String nameSeparator;

    @LombokWorkaround
    public static Builder builder() {
        Builder result = new Builder();
        result.format = ObsFormat.DEFAULT;
        result.gathering = ObsGathering.DEFAULT;
        result.layout = GridLayout.UNKNOWN;
        result.namePattern = "S${index}";
        result.nameSeparator = MultiLineNameUtil.SEPARATOR;
        return result;
    }

    @Nonnull
    public TsCollection read(@Nonnull GridInput input) {
        ValueReaders readers = ValueReaders.of(
                input::isSupportedDataType,
                () -> format.dateTimeParser()::parse,
                () -> format.numberParser()::parse
        );

        TsCollection.Builder result = TsCollection.builder()
                .type(TsInformationType.Data)
                .name(input.getName());

        switch (layout) {
            case UNKNOWN:
                DateHeader rowDates = getRowDates(input, readers);
                DateHeader colDates = getColDates(input, readers);

                if (rowDates.isBetterThan(colDates)) {
                    result.meta(LAYOUT_KEY, VERTICAL.name());
                    loadVertically(input, readers, rowDates, result::data);
                } else if (colDates.isBetterThan(rowDates)) {
                    result.meta(LAYOUT_KEY, HORIZONTAL.name());
                    loadVertically(InvGridInput.of(input), readers, colDates, result::data);
                } else {
                    result.meta(LAYOUT_KEY, UNKNOWN.name());
                }

                break;
            case HORIZONTAL:
                result.meta(LAYOUT_KEY, HORIZONTAL.name());
                loadVertically(InvGridInput.of(input), readers, getColDates(input, readers), result::data);
                break;
            case VERTICAL:
                result.meta(LAYOUT_KEY, VERTICAL.name());
                loadVertically(input, readers, getRowDates(input, readers), result::data);
                break;
        }

        return result.build();
    }

    private DateHeader getRowDates(GridInput grid, ValueReaders readers) {
        DateHeader result = new DateHeader(grid.getRowCount());
        for (int i = 0; i < grid.getRowCount(); i++) {
            result.set(i, readers.readDateTime(grid, i, 0));
        }
        return result;
    }

    private DateHeader getColDates(GridInput grid, ValueReaders readers) {
        DateHeader result = new DateHeader(grid.getColumnCount());
        for (int j = 0; j < grid.getColumnCount(); j++) {
            result.set(j, readers.readDateTime(grid, 0, j));
        }
        return result;
    }

    private void loadVertically(GridInput grid, ValueReaders readers, DateHeader dates, Consumer<Ts> consumer) {
        List<String> names = new ArrayList<>();
        loadHorizontalNames(grid, readers, dates.minIndex, names::add);

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

    private void loadHorizontalNames(GridInput grid, ValueReaders readers, int level, Consumer<String> consumer) {
        switch (level) {
            case 0: {
                loadHorizontalNamesNoheader(grid, readers, consumer);
                break;
            }
            case 1: {
                loadHorizontalNamesSingleheader(grid, readers, consumer);
                break;
            }
            default: {
                loadHorizontalNamesMultiHeaders(grid, readers, level, consumer);
                break;
            }
        }
    }

    private void loadHorizontalNamesNoheader(GridInput grid, ValueReaders readers, Consumer<String> consumer) {
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

    private void loadHorizontalNamesSingleheader(GridInput grid, ValueReaders readers, Consumer<String> consumer) {
        for (int column = FIRST_DATA_COL_IDX; column < grid.getColumnCount(); column++) {
            String name = readers.readString(grid, 0, column);
            if (name == null) {
                break;
            }
            consumer.accept(name);
        }
    }

    private void loadHorizontalNamesMultiHeaders(GridInput grid, ValueReaders readers, int level, Consumer<String> consumer) {
        Collector<CharSequence, ?, String> nameJoiner = Collectors.joining(nameSeparator);

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
            consumer.accept(joinSkippingNulls(path, nameJoiner));
        }
    }

    private String joinSkippingNulls(String[] items, Collector<CharSequence, ?, String> nameJoiner) {
        return Stream.of(items).filter(Objects::nonNull).collect(nameJoiner);
    }

    private static final int FIRST_DATA_COL_IDX = 1;
    private static final String LAYOUT_KEY = "gridLayout";

    @lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ValueReaders {

        static ValueReaders of(
                Predicate<Class<?>> isSupportedDataType,
                Supplier<Function<String, LocalDateTime>> dateTimeParser,
                Supplier<Function<String, Number>> numberParser) {

            InternalValueReader<String> string;
            InternalValueReader<LocalDateTime> dateTimeFallback;
            InternalValueReader<Number> numberFallback;

            if (isSupportedDataType.test(String.class)) {
                string = InternalValueReader.onString();
                dateTimeFallback = InternalValueReader.onStringParser(dateTimeParser.get());
                numberFallback = InternalValueReader.onStringParser(numberParser.get());
            } else {
                string = InternalValueReader.onNull();
                dateTimeFallback = InternalValueReader.onNull();
                numberFallback = InternalValueReader.onNull();
            }

            InternalValueReader<LocalDateTime> dateTime
                    = isSupportedDataType.test(LocalDateTime.class)
                    ? InternalValueReader.onDateTime().or(dateTimeFallback)
                    : dateTimeFallback;

            InternalValueReader<Number> number
                    = isSupportedDataType.test(Number.class)
                    ? InternalValueReader.onNumber().or(numberFallback)
                    : numberFallback;

            return new ValueReaders(string, dateTime, number);
        }

        private final InternalValueReader<String> string;
        private final InternalValueReader<LocalDateTime> dateTime;
        private final InternalValueReader<Number> number;

        public String readString(GridInput grid, int row, int column) {
            return string.read(grid, row, column);
        }

        public LocalDateTime readDateTime(GridInput grid, int row, int column) {
            return dateTime.read(grid, row, column);
        }

        public Number readNumber(GridInput grid, int row, int column) {
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
