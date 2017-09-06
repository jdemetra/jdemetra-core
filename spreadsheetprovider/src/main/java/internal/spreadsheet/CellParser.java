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
package internal.spreadsheet;

import demetra.util.Parser;
import ec.util.spreadsheet.Cell;
import ec.util.spreadsheet.Sheet;
import java.util.Date;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public interface CellParser<T> {

    @Nullable
    T parse(@Nonnull Sheet sheet, int rowIndex, int columnIndex);

    @Nonnull
    default CellParser<T> or(@Nonnull CellParser<T> cellParser) {
        return (s, r, c) -> Util.or(this, cellParser, s, r, c);
    }

    @Nonnull
    static <X> CellParser<X> fromParser(@Nonnull Parser<X> parser) {
        return (s, r, c) -> Util.fromParser(parser, s, r, c);
    }

    @Nonnull
    static CellParser<Date> onDateType() {
        return Util::parseDate;
    }

    @Nonnull
    static CellParser<Number> onNumberType() {
        return Util::parseNumber;
    }

    @Nonnull
    static CellParser<String> onStringType() {
        return Util::parseString;
    }

    static final class Util {

        static <T> T or(CellParser<T> first, CellParser<T> second, Sheet sheet, int rowIndex, int columnIndex) {
            T result = first.parse(sheet, rowIndex, columnIndex);
            return result != null ? result : second.parse(sheet, rowIndex, columnIndex);
        }

        static <T> T fromParser(Parser<T> adaptee, Sheet sheet, int rowIndex, int columnIndex) {
            String input = Util.parseString(sheet, rowIndex, columnIndex);
            return input != null ? adaptee.parse(input) : null;
        }

        static Date parseDate(Sheet sheet, int rowIndex, int columnIndex) {
            Cell cell = sheet.getCell(rowIndex, columnIndex);
            return cell != null && cell.isDate() ? cell.getDate() : null;
        }

        static Number parseNumber(Sheet sheet, int rowIndex, int columnIndex) {
            Cell cell = sheet.getCell(rowIndex, columnIndex);
            return cell != null && cell.isNumber() ? cell.getNumber() : null;
        }

        static String parseString(Sheet sheet, int rowIndex, int columnIndex) {
            Cell cell = sheet.getCell(rowIndex, columnIndex);
            return cell != null && cell.isString() ? cell.getString() : null;
        }
    }
}
