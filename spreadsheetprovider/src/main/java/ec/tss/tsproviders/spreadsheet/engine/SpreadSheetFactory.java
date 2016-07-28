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
package ec.tss.tsproviders.spreadsheet.engine;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollection.AlignType.HORIZONTAL;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollection.AlignType.UNKNOWN;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollection.AlignType.VERTICAL;
import ec.tss.tsproviders.utils.IParser;
import ec.tss.tsproviders.utils.MultiLineNameUtil;
import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tstoolkit.data.Table;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.timeseries.simplets.TsDataTableInfo;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Cell;
import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.helpers.ArraySheet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
public abstract class SpreadSheetFactory {

    @Nonnull
    abstract public SpreadSheetSource toSource(@Nonnull Book book, @Nonnull TsImportOptions options) throws IOException;

    @Nonnull
    abstract public TsCollectionInformation toTsCollectionInfo(@Nonnull Sheet sheet, @Nonnull TsImportOptions options);

    @Nonnull
    abstract public Table<?> toTable(@Nonnull Sheet sheet);

    @Nonnull
    abstract public ArraySheet fromTsCollectionInfo(@Nonnull TsCollectionInformation col, @Nonnull TsExportOptions options);

    @Nonnull
    abstract public ArraySheet fromMatrix(@Nonnull Matrix matrix);

    @Nonnull
    abstract public ArraySheet fromTable(@Nonnull Table<?> table);

    @Nonnull
    public static SpreadSheetFactory getDefault() {
        return DefaultImpl.INSTANCE;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    @VisibleForTesting
    static final class DefaultImpl extends SpreadSheetFactory {

        private static final DefaultImpl INSTANCE = new DefaultImpl();

        @Override
        public SpreadSheetSource toSource(Book book, TsImportOptions options) throws IOException {
            return parseSource(book, Context.create(options));
        }

        @Override
        public ArraySheet fromTsCollectionInfo(TsCollectionInformation col, TsExportOptions options) {
            TsDataTable table = new TsDataTable();
            for (TsInformation o : col.items) {
                table.insert(-1, o.data);
            }

            if (table.getDomain() != null) {
                ArraySheet.Builder builder = ArraySheet.builder().name("dnd");

                if (options.isShowTitle()) {
                    builder.row(0, options.isShowDates() ? 1 : 0, col.items.stream().map(o -> o.name).iterator());
                }

                if (options.isShowDates()) {
                    builder.column(options.isShowTitle() ? 1 : 0, 0, StreamSupport.stream(table.getDomain().spliterator(), false).map(options.isBeginPeriod() ? o -> o.firstday().getTime() : o -> o.lastday().getTime()).iterator());
                }

                int firstRow = options.isShowTitle() ? 1 : 0;
                int firstColumn = options.isShowDates() ? 1 : 0;
                int rowCount = table.getDomain().getLength();
                int columnCount = col.items.size();
                for (int i = 0; i < rowCount; ++i) {
                    for (int j = 0; j < columnCount; ++j) {
                        if (table.getDataInfo(i, j) == TsDataTableInfo.Valid) {
                            builder.value(firstRow + i, firstColumn + j, table.getData(i, j));
                        }
                    }
                }

                ArraySheet sheet = builder.build();

                if (!options.isVertical()) {
                    sheet = sheet.inv();
                }

                return sheet;
            }

            return ArraySheet.builder().name("dnd").build();
        }

        @Override
        public TsCollectionInformation toTsCollectionInfo(Sheet sheet, TsImportOptions options) {
            TsCollectionInformation result = new TsCollectionInformation();
            result.name = sheet.getName();
            result.type = TsInformationType.All;
            for (SpreadSheetSeries s : parseCollection(sheet, 0, Context.create(options)).series) {
                TsInformation tsInfo = new TsInformation();
                tsInfo.name = s.seriesName;
                tsInfo.type = TsInformationType.All;
                if (s.data.isPresent()) {
                    tsInfo.data = s.data.get();
                    tsInfo.invalidDataCause = null;
                } else {
                    tsInfo.data = null;
                    tsInfo.invalidDataCause = s.data.getCause();
                }
                result.items.add(tsInfo);
            }
            return result;
        }

        @Override
        public ArraySheet fromMatrix(Matrix matrix) {
            return newArraySheet("dnd", matrix);
        }

        @Override
        public Table<?> toTable(Sheet sheet) {
            Table<Object> result = new Table<>(sheet.getRowCount(), sheet.getColumnCount());
            sheet.forEachValue(result::set);
            return result;
        }

        @Override
        public ArraySheet fromTable(Table<?> table) {
            return newArraySheet("dnd", table);
        }

        @VisibleForTesting
        static SpreadSheetSource parseSource(Book book, Context context) throws IOException {
            int sheetCount = book.getSheetCount();
            List<SpreadSheetCollection> result = new ArrayList<>(sheetCount);
            book.forEach((sheet, i) -> result.add(parseCollection(sheet, i, context)));
            return new SpreadSheetSource(result, "?");
        }

        @VisibleForTesting
        static SpreadSheetCollection parseCollection(Sheet sheet, int ordering, Context context) {
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

            return new SpreadSheetCollection(sheet.getName(), ordering, UNKNOWN, ImmutableList.<SpreadSheetSeries>of());
        }

        private static final int FIRST_DATA_COL_IDX = 1;
        private static final Joiner NAME_JOINER = Joiner.on(MultiLineNameUtil.SEPARATOR).skipNulls();

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
                        result.add(NAME_JOINER.join(path));
                    }
                    break;
                }
            }
            return result;
        }

        private static SpreadSheetCollection loadVertically(SpreadSheetCollection.AlignType alignType, int ordering, Sheet sheet, Context context, DateHeader dates) {
            List<String> names = getHorizontalNames(sheet, context, dates.minIndex);

            ImmutableList.Builder<SpreadSheetSeries> list = ImmutableList.builder();

            OptionalTsData.Builder2<Date> data = OptionalTsData.builderByDate(context.frequency, context.aggregationType, context.clean, false, context.cal);
            for (int columnIdx = 0; columnIdx < names.size(); columnIdx++) {
                for (int rowIdx = dates.getMinIndex(); rowIdx <= dates.getMaxIndex(); rowIdx++) {
                    Number value = context.toNumber.parse(sheet, rowIdx, columnIdx + FIRST_DATA_COL_IDX);
                    data.add(dates.get(rowIdx), value);
                }
                list.add(new SpreadSheetSeries(names.get(columnIdx), columnIdx, alignType, data.build()));
                data.clear();
            }

            return new SpreadSheetCollection(sheet.getName(), ordering, alignType, list.build());
        }
    }

    @VisibleForTesting
    static final class Context {

        public final CellParser<String> toName;
        public final CellParser<Date> toDate;
        public final CellParser<Number> toNumber;
        public final TsFrequency frequency;
        public final TsAggregationType aggregationType;
        public final boolean clean;
        public final Calendar cal;

        public Context(CellParser<String> toName, CellParser<Date> toDate, CellParser<Number> toNumber, TsFrequency frequency, TsAggregationType aggregationType, boolean clean) {
            this.toName = toName;
            this.toDate = toDate;
            this.toNumber = toNumber;
            this.frequency = frequency;
            this.aggregationType = aggregationType;
            this.clean = clean;
            this.cal = new GregorianCalendar();
        }

        private static Context create(TsImportOptions options) {
            return new Context(
                    CellParser.onStringType(),
                    CellParser.onDateType().or(CellParser.fromParser(options.getDataFormat().dateParser())),
                    CellParser.onNumberType().or(CellParser.fromParser(options.getDataFormat().numberParser())),
                    options.getFrequency(), options.getAggregationType(), options.isCleanMissing());
        }
    }

    @VisibleForTesting
    static abstract class CellParser<T> {

        @Nullable
        abstract public T parse(@Nonnull Sheet sheet, int rowIndex, int columnIndex);

        @Nonnull
        public Optional<T> tryParse(@Nonnull Sheet sheet, int rowIndex, int columnIndex) {
            return Optional.fromNullable(parse(sheet, rowIndex, columnIndex));
        }

        @Nonnull
        public CellParser<T> or(@Nonnull CellParser<T>... cellParser) {
            switch (cellParser.length) {
                case 0:
                    return this;
                case 1:
                    return firstNotNull(ImmutableList.of(this, cellParser[0]));
                default:
                    return firstNotNull(ImmutableList.<CellParser<T>>builder().add(this).add(cellParser).build());
            }
        }

        @Nonnull
        public static <X> CellParser<X> firstNotNull(@Nonnull ImmutableList<? extends CellParser<X>> list) {
            return new FirstNotNull(list);
        }

        @Nonnull
        public static <X> CellParser<X> fromParser(@Nonnull IParser<X> parser) {
            return new Adapter(parser);
        }

        @Nonnull
        public static CellParser<Date> onDateType() {
            return DateCellFunc.INSTANCE;
        }

        @Nonnull
        public static CellParser<Number> onNumberType() {
            return NumberCellFunc.INSTANCE;
        }

        @Nonnull
        public static CellParser<String> onStringType() {
            return StringCellFunc.INSTANCE;
        }

        //<editor-fold defaultstate="collapsed" desc="Internal implementation">
        private static final class FirstNotNull<X> extends CellParser<X> {

            private final ImmutableList<? extends CellParser<X>> list;

            FirstNotNull(ImmutableList<? extends CellParser<X>> list) {
                this.list = list;
            }

            @Override
            public X parse(Sheet sheet, int rowIndex, int columnIndex) {
                for (CellParser<X> o : list) {
                    X result = o.parse(sheet, rowIndex, columnIndex);
                    if (result != null) {
                        return result;
                    }
                }
                return null;
            }
        }

        private static final class Adapter<X> extends CellParser<X> {

            private final IParser<X> adaptee;

            Adapter(IParser<X> parser) {
                this.adaptee = parser;
            }

            @Override
            public X parse(Sheet sheet, int rowIndex, int columnIndex) {
                String input = StringCellFunc.INSTANCE.parse(sheet, rowIndex, columnIndex);
                return input != null ? adaptee.parse(input) : null;
            }
        }

        private static final class DateCellFunc extends CellParser<Date> {

            static final DateCellFunc INSTANCE = new DateCellFunc();

            @Override
            public Date parse(Sheet sheet, int rowIndex, int columnIndex) {
                Cell cell = sheet.getCell(rowIndex, columnIndex);
                return cell != null && cell.isDate() ? cell.getDate() : null;
            }
        }

        private static final class NumberCellFunc extends CellParser<Number> {

            static final NumberCellFunc INSTANCE = new NumberCellFunc();

            @Override
            public Number parse(Sheet sheet, int rowIndex, int columnIndex) {
                Cell cell = sheet.getCell(rowIndex, columnIndex);
                return cell != null && cell.isNumber() ? cell.getNumber() : null;
            }
        }

        private static final class StringCellFunc extends CellParser<String> {

            static final StringCellFunc INSTANCE = new StringCellFunc();

            @Override
            public String parse(Sheet sheet, int rowIndex, int columnIndex) {
                Cell cell = sheet.getCell(rowIndex, columnIndex);
                return cell != null && cell.isString() ? cell.getString() : null;
            }
        }
        //</editor-fold>
    }

    private static final class DateHeader {

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

    private static ArraySheet newArraySheet(String name, Matrix matrix) {
        ArraySheet.Builder result = ArraySheet.builder(matrix.getRowsCount(), matrix.getColumnsCount()).name(name);
        for (int i = 0; i < matrix.getRowsCount(); i++) {
            for (int j = 0; j < matrix.getColumnsCount(); j++) {
                result.value(i, j, matrix.get(i, j));
            }
        }
        return result.build();
    }

    private static ArraySheet newArraySheet(String name, Table<?> table) {
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
