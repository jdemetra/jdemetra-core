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
package internal.tsprovider.grid;

import demetra.tsprovider.grid.GridExport;
import demetra.tsprovider.grid.GridFactory;
import demetra.tsprovider.grid.GridImport;
import demetra.tsprovider.grid.GridInput;
import demetra.tsprovider.grid.GridLayout;
import static demetra.tsprovider.grid.GridLayout.HORIZONTAL;
import static demetra.tsprovider.grid.GridLayout.UNKNOWN;
import static demetra.tsprovider.grid.GridLayout.VERTICAL;
import demetra.tsprovider.grid.GridOutput;
import demetra.tsprovider.grid.GridReader;
import demetra.tsprovider.grid.GridWriter;
import demetra.tsprovider.grid.TsCollectionGrid;
import demetra.tsprovider.grid.TsGrid;
import demetra.tsprovider.util.MultiLineNameUtil;
import demetra.tsprovider.util.ObsGathering;
import demetra.tsprovider.util.TsDataBuilder;
import demetra.util.Parser;
import internal.tsprovider.grid.TsDataTable.TsDataTableInfo;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
public enum GridFactoryImpl implements GridFactory {
    INSTANCE;

    @Override
    public GridReader getReader(GridImport options) {
        return new ReaderImpl(options);
    }

    @Override
    public GridWriter getWriter(GridExport options) {
        return new WriterImpl(options);
    }

    private static final class ReaderImpl implements GridReader {

        private final ValueParser<String> toName;
        private final ValueParser<Date> toDate;
        private final ValueParser<Number> toNumber;
        private final ObsGathering gathering;
        private final Calendar cal;

        private ReaderImpl(GridImport options) {
            this.toName = ValueParser.onString();
            this.toDate = ValueParser.onCalendar().or(ValueParser.onStringParser(options.getObsFormat().dateParser()));
            this.toNumber = ValueParser.onNumber().or(ValueParser.onStringParser(options.getObsFormat().numberParser()));
            this.gathering = options.getObsGathering();
            this.cal = new GregorianCalendar();
        }

        @Override
        public TsCollectionGrid read(GridInput input) {
            DateHeader rowDates = new DateHeader(input.getRowCount());
            for (int i = 0; i < input.getRowCount(); i++) {
                rowDates.set(i, toDate.parse(input, i, 0));
            }

            DateHeader colDates = new DateHeader(input.getColumnCount());
            for (int j = 0; j < input.getColumnCount(); j++) {
                colDates.set(j, toDate.parse(input, 0, j));
            }

            if (rowDates.isBetter(colDates)) {
                return loadVertically(input.getName(), VERTICAL, input, rowDates);
            }

            if (colDates.isBetter(rowDates)) {
                return loadVertically(input.getName(), HORIZONTAL, input.inv(), colDates);
            }

            return TsCollectionGrid.builder().name(input.getName()).layout(UNKNOWN).build();
        }

        @Override
        public void close() {
        }

        private static final int FIRST_DATA_COL_IDX = 1;
        private static final Collector<CharSequence, ?, String> NAME_JOINER = Collectors.joining(MultiLineNameUtil.SEPARATOR);

        private static String joinSkippingNulls(String[] items) {
            return Stream.of(items).filter(Objects::nonNull).collect(NAME_JOINER);
        }

        private List<String> getHorizontalNames(GridInput grid, int level) {
            List<String> result = new ArrayList<>();
            switch (level) {
                // no header
                case 0: {
                    for (int columnIdx = FIRST_DATA_COL_IDX; columnIdx < grid.getColumnCount(); columnIdx++) {
                        if (toNumber.parse(grid, 0, columnIdx) == null) {
                            break;
                        }
                        result.add("S" + columnIdx);
                    }
                    break;
                }
                // single header
                case 1: {
                    for (int columnIdx = FIRST_DATA_COL_IDX; columnIdx < grid.getColumnCount(); columnIdx++) {
                        String name = toName.parse(grid, 0, columnIdx);
                        if (name == null) {
                            break;
                        }
                        result.add(name);
                    }
                    break;
                }
                // multiple headers
                default: {
                    String[] path = new String[level];
                    for (int columnIdx = FIRST_DATA_COL_IDX; columnIdx < grid.getColumnCount(); columnIdx++) {
                        boolean hasHeader = false;
                        for (int rowIdx = 0; rowIdx < path.length; rowIdx++) {
                            String name = toName.parse(grid, rowIdx, columnIdx);
                            if (name != null) {
                                hasHeader = true;
                                path[rowIdx] = name;
                            } else if (hasHeader) {
                                path[rowIdx] = null;
                            }
                        }
                        if (!hasHeader) {
                            break;
                        }
                        result.add(joinSkippingNulls(path));
                    }
                    break;
                }
            }
            return result;
        }

        private TsCollectionGrid loadVertically(String name, GridLayout layout, GridInput grid, DateHeader dates) {
            List<String> names = getHorizontalNames(grid, dates.minIndex);

            List<TsGrid> list = new ArrayList<>();

            TsDataBuilder<Date> data = TsDataBuilder.byCalendar(cal, gathering);
            for (int columnIdx = 0; columnIdx < names.size(); columnIdx++) {
                for (int rowIdx = dates.getMinIndex(); rowIdx <= dates.getMaxIndex(); rowIdx++) {
                    Number value = toNumber.parse(grid, rowIdx, columnIdx + FIRST_DATA_COL_IDX);
                    data.add(dates.get(rowIdx), value);
                }
                list.add(TsGrid.of(names.get(columnIdx), data.build()));
                data.clear();
            }

            return TsCollectionGrid.builder().name(name).layout(layout).items(list).build();
        }

        private static final class DateHeader {

            private final Date[] dates;
            private int minIndex;
            private int maxIndex;

            private DateHeader(int maxSize) {
                dates = new Date[maxSize];
                minIndex = maxSize - 1;
                maxIndex = 0;
            }

            public void set(int i, Date value) {
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

            public Date get(int i) {
                return dates[i];
            }

            public int getMinIndex() {
                return minIndex;
            }

            public int getMaxIndex() {
                return maxIndex;
            }

            public boolean isBetter(DateHeader other) {
                int count = maxIndex - minIndex + 1;
                return count > 0 && count > (other.maxIndex - other.minIndex + 1);
            }
        }
    }

    private interface ValueParser<T> {

        @Nullable
        T parse(@Nonnull GridInput grid, int rowIndex, int columnIndex);

        @Nonnull
        default ValueParser<T> or(@Nonnull ValueParser<T> cellParser) {
            return (s, r, c) -> Util.or(this, cellParser, s, r, c);
        }

        @Nonnull
        static <X> ValueParser<X> onStringParser(@Nonnull Parser<X> parser) {
            return (s, r, c) -> Util.fromStringParser(parser, s, r, c);
        }

        @Nonnull
        static ValueParser<Date> onCalendar() {
            return Util::parseCalendar;
        }

        @Nonnull
        static ValueParser<Number> onNumber() {
            return Util::parseNumber;
        }

        @Nonnull
        static ValueParser<String> onString() {
            return Util::parseString;
        }

        static final class Util {

            static <T> T or(ValueParser<T> first, ValueParser<T> second, GridInput grid, int rowIndex, int columnIndex) {
                T result = first.parse(grid, rowIndex, columnIndex);
                return result != null ? result : second.parse(grid, rowIndex, columnIndex);
            }

            static <T> T fromStringParser(Parser<T> adaptee, GridInput grid, int rowIndex, int columnIndex) {
                String input = Util.parseString(grid, rowIndex, columnIndex);
                return input != null ? adaptee.parse(input) : null;
            }

            static Date parseCalendar(GridInput grid, int rowIndex, int columnIndex) {
                Object value = grid.getValue(rowIndex, columnIndex);
                return value instanceof Date ? (Date) value : null;
            }

            static Number parseNumber(GridInput grid, int rowIndex, int columnIndex) {
                Object value = grid.getValue(rowIndex, columnIndex);
                return value instanceof Number ? (Number) value : null;
            }

            static String parseString(GridInput grid, int rowIndex, int columnIndex) {
                Object value = grid.getValue(rowIndex, columnIndex);
                return value instanceof String ? (String) value : null;
            }
        }
    }

    @lombok.AllArgsConstructor
    private static final class WriterImpl implements GridWriter {

        private final GridExport options;

        @Override
        public void write(TsCollectionGrid value, GridOutput output) {
            output.name(value.getName());

            TsDataTable table = new TsDataTable();
            for (TsGrid o : value.getItems()) {
                table.insert(-1, o.getData().get());
            }

            if (table.getDomain() != null) {
                if (options.isShowTitle()) {
                    output.row(0, options.isShowDates() ? 1 : 0, value.getItems().stream().map(TsGrid::getName).iterator());
                }

                if (options.isShowDates()) {
                    output.column(options.isShowTitle() ? 1 : 0, 0, table.getDomain().stream().map(options.isBeginPeriod() ? o -> o.start() : o -> o.end()).iterator());
                }

                int firstRow = options.isShowTitle() ? 1 : 0;
                int firstColumn = options.isShowDates() ? 1 : 0;
                int rowCount = table.getDomain().getLength();
                int columnCount = value.getItems().size();
                for (int i = 0; i < rowCount; ++i) {
                    for (int j = 0; j < columnCount; ++j) {
                        if (table.getDataInfo(i, j) == TsDataTableInfo.Valid) {
                            output.value(firstRow + i, firstColumn + j, table.getData(i, j));
                        }
                    }
                }

                if (!options.getLayout().equals(VERTICAL)) {
                    output.inv();
                }
            }
        }

        @Override
        public void close() {
        }
    }
}
