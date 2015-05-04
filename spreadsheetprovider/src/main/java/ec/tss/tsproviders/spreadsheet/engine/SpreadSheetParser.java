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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollection.AlignType.HORIZONTAL;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollection.AlignType.UNKNOWN;
import static ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollection.AlignType.VERTICAL;
import ec.tss.tsproviders.utils.IParser;
import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tss.tsproviders.utils.Parsers.Parser;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Cell;
import ec.util.spreadsheet.Sheet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
public abstract class SpreadSheetParser {

    @Nonnull
    abstract public SpreadSheetSource parse(Book book, Parser<Date> dateParser, Parser<Number> numberParser, TsFrequency freq, TsAggregationType aggregation, boolean clean) throws IOException;

    @Nonnull
    public static SpreadSheetParser getDefault() {
        return DefaultImpl.INSTANCE;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    @VisibleForTesting
    static final class DefaultImpl extends SpreadSheetParser {

        private static final DefaultImpl INSTANCE = new DefaultImpl();

        @Override
        public SpreadSheetSource parse(Book book, Parser<Date> dateParser, Parser<Number> numberParser, TsFrequency freq, TsAggregationType aggregation, boolean clean) throws IOException {
            Context context = new Context(
                    CellParser.onStringType(),
                    CellParser.onDateType().or(CellParser.fromParser(dateParser)),
                    CellParser.onNumberType().or(CellParser.fromParser(numberParser)),
                    freq, aggregation, clean);
            return parseSource(book, context);
        }

        @VisibleForTesting
        static SpreadSheetSource parseSource(Book book, Context context) throws IOException {
            int sheetCount = book.getSheetCount();
            List<SpreadSheetCollection> result = new ArrayList<>(sheetCount);
            for (int i = 0; i < sheetCount; i++) {
                result.add(parseCollection(book.getSheet(i), i, context));
            }
            return new SpreadSheetSource(result, "?");
        }

        @VisibleForTesting
        static SpreadSheetCollection parseCollection(Sheet sheet, int ordering, Context context) {
            switch (parseAlignType(sheet, context.toName, context.toDate)) {
                case VERTICAL:
                    return loadVertically(SpreadSheetCollection.AlignType.VERTICAL, ordering, sheet, context);
                case HORIZONTAL:
                    return loadVertically(SpreadSheetCollection.AlignType.HORIZONTAL, ordering, sheet.inv(), context);
                case UNKNOWN:
                    return new SpreadSheetCollection(sheet.getName(), ordering, SpreadSheetCollection.AlignType.UNKNOWN, ImmutableList.<SpreadSheetSeries>of());
            }
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @VisibleForTesting
        static SpreadSheetCollection.AlignType parseAlignType(Sheet sheet, CellParser<String> toName, CellParser<Date> toDate) {
            if (sheet.getRowCount() < 2 || sheet.getColumnCount() < 2) {
                return SpreadSheetCollection.AlignType.UNKNOWN;
            }

            if (toName.tryParse(sheet, 0, 1).isPresent() && toDate.tryParse(sheet, 1, 0).isPresent()) {
                return SpreadSheetCollection.AlignType.VERTICAL;
            }

            if (toDate.tryParse(sheet, 0, 1).isPresent() && toName.tryParse(sheet, 1, 0).isPresent()) {
                return SpreadSheetCollection.AlignType.HORIZONTAL;
            }

            return SpreadSheetCollection.AlignType.UNKNOWN;
        }

        private static final int FIRST_DATA_ROW_IDX = 1;
        private static final int FIRST_DATA_COL_IDX = 1;
        private static final int DATE_COL_IDX = 0;
        private static final int NAME_ROW_IDX = 0;

        private static List<Date> getVerticalDates(Sheet sheet, CellParser<Date> toDate) {
            List<Date> result = new ArrayList<>();
            for (int rowIdx = FIRST_DATA_ROW_IDX; rowIdx < sheet.getRowCount(); rowIdx++) {
                Date date = toDate.parse(sheet, rowIdx, DATE_COL_IDX);
                if (date == null) {
                    break;
                }
                result.add(date);
            }
            return result;
        }

        private static List<String> getHorizontalNames(Sheet sheet, CellParser<String> toName) {
            List<String> result = new ArrayList<>();
            for (int columnIdx = FIRST_DATA_COL_IDX; columnIdx < sheet.getColumnCount(); columnIdx++) {
                String name = toName.parse(sheet, NAME_ROW_IDX, columnIdx);
                if (name == null) {
                    break;
                }
                result.add(name);
            }
            return result;
        }

        private static SpreadSheetCollection loadVertically(SpreadSheetCollection.AlignType alignType, int ordering, Sheet sheet, Context context) {
            List<Date> dates = getVerticalDates(sheet, context.toDate);
            List<String> names = getHorizontalNames(sheet, context.toName);

            ImmutableList.Builder<SpreadSheetSeries> list = ImmutableList.builder();

            OptionalTsData.Builder data = new OptionalTsData.Builder(context.frequency, context.aggregationType, context.clean);
            for (int columnIdx = 0; columnIdx < names.size(); columnIdx++) {
                for (int rowIdx = 0; rowIdx < dates.size(); rowIdx++) {
                    Number value = context.toNumber.parse(sheet, rowIdx + FIRST_DATA_ROW_IDX, columnIdx + FIRST_DATA_COL_IDX);
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

        public Context(CellParser<String> toName, CellParser<Date> toDate, CellParser<Number> toNumber, TsFrequency frequency, TsAggregationType aggregationType, boolean clean) {
            this.toName = toName;
            this.toDate = toDate;
            this.toNumber = toNumber;
            this.frequency = frequency;
            this.aggregationType = aggregationType;
            this.clean = clean;
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
    //</editor-fold>
}
