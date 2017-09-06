/*
 * Copyright 2013 National Bank of Belgium
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
package internal.spreadsheet;

import internal.spreadsheet.grid.GridExport;
import internal.spreadsheet.grid.GridImport;
import internal.spreadsheet.grid.GridSheet;
import internal.spreadsheet.grid.GridSeries;
import internal.spreadsheet.grid.GridType;
import internal.spreadsheet.grid.GridBook;
import demetra.design.VisibleForTesting;
import demetra.tsprovider.Ts;
import demetra.tsprovider.TsCollection;
import demetra.tsprovider.TsInformationType;
import demetra.tsprovider.util.MultiLineNameUtil;
import demetra.tsprovider.util.ObsGathering;
import demetra.tsprovider.util.TsDataBuilder;
import static internal.spreadsheet.grid.GridType.HORIZONTAL;
import static internal.spreadsheet.grid.GridType.UNKNOWN;
import static internal.spreadsheet.grid.GridType.VERTICAL;
import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.helpers.ArraySheet;
import internal.spreadsheet.Fixme.Matrix;
import internal.spreadsheet.Fixme.Table;
import internal.spreadsheet.Fixme.TsDataTable;
import internal.spreadsheet.Fixme.TsDataTableInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public interface SpreadSheetFactory {

    @Nonnull
    GridBook toSource(@Nonnull Book book, @Nonnull GridImport options) throws IOException;

    @Nonnull
    TsCollection toTsCollectionInfo(@Nonnull Sheet sheet, @Nonnull GridImport options);

    @Nonnull
    Table<?> toTable(@Nonnull Sheet sheet);

    @Nonnull
    ArraySheet fromTsCollectionInfo(@Nonnull TsCollection col, @Nonnull GridExport options);

    @Nonnull
    ArraySheet fromMatrix(@Nonnull Matrix matrix);

    @Nonnull
    ArraySheet fromTable(@Nonnull Table<?> table);

    @Nonnull
    static SpreadSheetFactory getDefault() {
        return DefaultImpl.INSTANCE;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    @VisibleForTesting
    static final class DefaultImpl implements SpreadSheetFactory {

        private static final DefaultImpl INSTANCE = new DefaultImpl();

        @Override
        public GridBook toSource(Book book, GridImport options) throws IOException {
            return parseSource(book, Context.create(options));
        }

        @Override
        public ArraySheet fromTsCollectionInfo(TsCollection col, GridExport options) {
            TsDataTable table = new TsDataTable();
            for (Ts o : col.getItems()) {
                table.insert(-1, o.getData().get());
            }

            if (table.getDomain() != null) {
                ArraySheet.Builder builder = ArraySheet.builder().name("dnd");

                if (options.isShowTitle()) {
                    builder.row(0, options.isShowDates() ? 1 : 0, col.getItems().stream().map(o -> o.getName()).iterator());
                }

                if (options.isShowDates()) {
                    builder.column(options.isShowTitle() ? 1 : 0, 0, table.getDomain().stream().map(options.isBeginPeriod() ? o -> o.start() : o -> o.end()).iterator());
                }

                int firstRow = options.isShowTitle() ? 1 : 0;
                int firstColumn = options.isShowDates() ? 1 : 0;
                int rowCount = table.getDomain().getLength();
                int columnCount = col.getItems().size();
                for (int i = 0; i < rowCount; ++i) {
                    for (int j = 0; j < columnCount; ++j) {
                        if (table.getDataInfo(i, j) == TsDataTableInfo.Valid) {
                            builder.value(firstRow + i, firstColumn + j, table.getData(i, j));
                        }
                    }
                }

                ArraySheet sheet = builder.build();

                if (!options.getGridType().equals(VERTICAL)) {
                    sheet = sheet.inv();
                }

                return sheet;
            }

            return ArraySheet.builder().name("dnd").build();
        }

        @Override
        public TsCollection toTsCollectionInfo(Sheet sheet, GridImport options) {
            TsCollection.Builder result = TsCollection.builder();
            result.name(sheet.getName());
            result.type(TsInformationType.All);
            for (GridSeries s : parseCollection(sheet, 0, Context.create(options)).getRanges()) {
                Ts.Builder tsInfo = Ts.builder();
                tsInfo.name(s.getSeriesName());
                tsInfo.type(TsInformationType.All);
                tsInfo.data(s.getData());
                result.item(tsInfo.build());
            }
            return result.build();
        }

        @Override
        public ArraySheet fromMatrix(Matrix matrix) {
            return newArraySheet("dnd", matrix);
        }

        @Override
        public Table<?> toTable(Sheet sheet) {
            Table<Object> result = Table.of(sheet.getRowCount(), sheet.getColumnCount());
            sheet.forEachValue(result::set);
            return result;
        }

        @Override
        public ArraySheet fromTable(Table<?> table) {
            return newArraySheet("dnd", table);
        }

        @VisibleForTesting
        static GridBook parseSource(Book book, Context context) throws IOException {
            int sheetCount = book.getSheetCount();
            Map<String, GridSheet> result = new HashMap<>(sheetCount);
            book.forEach((sheet, i) -> {
                GridSheet data = parseCollection(sheet, i, context);
                result.put(data.getSheetName(), data);
            });
            return GridBook.of(result);
        }

        @VisibleForTesting
        static GridSheet parseCollection(Sheet sheet, int ordering, Context context) {
            DateHeader rowDates = new DateHeader(sheet.getRowCount());
            for (int i = 0; i < sheet.getRowCount(); i++) {
                rowDates.set(i, context.toDate.parse(sheet, i, 0));
            }

            DateHeader colDates = new DateHeader(sheet.getColumnCount());
            for (int j = 0; j < sheet.getColumnCount(); j++) {
                colDates.set(j, context.toDate.parse(sheet, 0, j));
            }

            if (rowDates.isBetter(colDates)) {
                return loadVertically(VERTICAL, ordering, sheet, context, rowDates);
            }

            if (colDates.isBetter(rowDates)) {
                return loadVertically(HORIZONTAL, ordering, sheet.inv(), context, colDates);
            }

            return GridSheet.of(sheet.getName(), ordering, UNKNOWN, Collections.emptyList());
        }

        private static final int FIRST_DATA_COL_IDX = 1;
        private static final Collector<CharSequence, ?, String> NAME_JOINER = Collectors.joining(MultiLineNameUtil.SEPARATOR);

        private static String joinSkippingNulls(String[] items) {
            return Stream.of(items).filter(Objects::nonNull).collect(NAME_JOINER);
        }

        private static List<String> getHorizontalNames(Sheet sheet, Context context, int level) {
            List<String> result = new ArrayList<>();
            switch (level) {
                // no header
                case 0: {
                    for (int columnIdx = FIRST_DATA_COL_IDX; columnIdx < sheet.getColumnCount(); columnIdx++) {
                        if (context.toNumber.parse(sheet, 0, columnIdx) == null) {
                            break;
                        }
                        result.add("S" + columnIdx);
                    }
                    break;
                }
                // single header
                case 1: {
                    for (int columnIdx = FIRST_DATA_COL_IDX; columnIdx < sheet.getColumnCount(); columnIdx++) {
                        String name = context.toName.parse(sheet, 0, columnIdx);
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
                    for (int columnIdx = FIRST_DATA_COL_IDX; columnIdx < sheet.getColumnCount(); columnIdx++) {
                        boolean hasHeader = false;
                        for (int rowIdx = 0; rowIdx < path.length; rowIdx++) {
                            String name = context.toName.parse(sheet, rowIdx, columnIdx);
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

        private static GridSheet loadVertically(GridType gridType, int ordering, Sheet sheet, Context context, DateHeader dates) {
            List<String> names = getHorizontalNames(sheet, context, dates.minIndex);

            List<GridSeries> list = new ArrayList<>();

            TsDataBuilder<Date> data = TsDataBuilder.byCalendar(context.cal, context.gathering);
            for (int columnIdx = 0; columnIdx < names.size(); columnIdx++) {
                for (int rowIdx = dates.getMinIndex(); rowIdx <= dates.getMaxIndex(); rowIdx++) {
                    Number value = context.toNumber.parse(sheet, rowIdx, columnIdx + FIRST_DATA_COL_IDX);
                    data.add(dates.get(rowIdx), value);
                }
                list.add(GridSeries.of(names.get(columnIdx), columnIdx, gridType, data.build()));
                data.clear();
            }

            return GridSheet.of(sheet.getName(), ordering, gridType, list);
        }
    }

    @VisibleForTesting
    static final class Context {

        public final CellParser<String> toName;
        public final CellParser<Date> toDate;
        public final CellParser<Number> toNumber;
        public final ObsGathering gathering;
        public final Calendar cal;

        public Context(CellParser<String> toName, CellParser<Date> toDate, CellParser<Number> toNumber, ObsGathering gathering) {
            this.toName = toName;
            this.toDate = toDate;
            this.toNumber = toNumber;
            this.gathering = gathering;
            this.cal = new GregorianCalendar();
        }

        private static Context create(GridImport options) {
            return new Context(
                    CellParser.onStringType(),
                    CellParser.onDateType().or(CellParser.fromParser(options.getObsFormat().dateParser())),
                    CellParser.onNumberType().or(CellParser.fromParser(options.getObsFormat().numberParser())),
                    options.getObsGathering());
        }
    }

    static final class DateHeader {

        private final Date[] dates;
        private int minIndex;
        private int maxIndex;

        public DateHeader(int maxSize) {
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

    static ArraySheet newArraySheet(String name, Matrix matrix) {
        ArraySheet.Builder result = ArraySheet.builder(matrix.getRowsCount(), matrix.getColumnsCount()).name(name);
        for (int i = 0; i < matrix.getRowsCount(); i++) {
            for (int j = 0; j < matrix.getColumnsCount(); j++) {
                result.value(i, j, matrix.get(i, j));
            }
        }
        return result.build();
    }

    static ArraySheet newArraySheet(String name, Table<?> table) {
        ArraySheet.Builder result = ArraySheet.builder(table.getRowsCount(), table.getColumnsCount()).name(name);
        for (int i = 0; i < table.getRowsCount(); i++) {
            for (int j = 0; j < table.getColumnsCount(); j++) {
                result.value(i, j, table.get(i, j));
            }
        }
        return result.build();
    }
    //</editor-fold>
}
