/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
import ec.tss.tsproviders.spreadsheet.facade.Cell;
import ec.tss.tsproviders.spreadsheet.facade.Sheet;
import ec.tss.tsproviders.utils.IParser;
import java.util.Date;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public abstract class CellParser<T> {

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
