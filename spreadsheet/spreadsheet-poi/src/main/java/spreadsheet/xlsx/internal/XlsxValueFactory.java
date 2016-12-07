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
package spreadsheet.xlsx.internal;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import spreadsheet.xlsx.XlsxDateSystem;

/**
 *
 * @author Philippe Charles
 */
final class XlsxValueFactory {

    // http://openxmldeveloper.org/blog/b/openxmldeveloper/archive/2012/03/08/dates-in-strict-spreadsheetml-files.aspx
    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
    // http://msdn.microsoft.com/en-us/library/documentformat.openxml.spreadsheet.cellvalues.aspx
    static final String BOOLEAN_TYPE = "b";
    static final String NUMBER_TYPE = "n";
    static final String ERROR_TYPE = "e";
    static final String SHARED_STRING_TYPE = "s";
    static final String STRING_TYPE = "str";
    static final String INLINE_STRING_TYPE = "inlineStr";
    static final String DATE_TYPE = "d";

    private final XlsxDateSystem dateSystem;
    private final IntFunction<String> sharedStrings;
    private final IntPredicate dateFormats;
    private final Calendar calendar;
    private final DateFormat isoDateFormat;

    XlsxValueFactory(XlsxDateSystem dateSystem, IntFunction<String> sharedStrings, IntPredicate dateFormats) {
        this.dateSystem = dateSystem;
        this.sharedStrings = sharedStrings;
        this.dateFormats = dateFormats;
        // using default time-zone
        this.calendar = new GregorianCalendar();
        this.isoDateFormat = new SimpleDateFormat(ISO_DATE_FORMAT);
    }

    @Nullable
    private Object getNumberOrDate(@Nonnull String rawValue, @Nullable Integer rawStyleIndex) {
        try {
            double number = Double.parseDouble(rawValue);
            if (rawStyleIndex != null
                    && dateFormats.test(rawStyleIndex)
                    && dateSystem.isValidExcelDate(number)) {
                return dateSystem.getJavaDate(calendar, number);
            }
            return number;
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            return null;
        }
    }

    @Nullable
    public Object getValue(@Nonnull String rawValue, @Nullable String rawDataType, @Nullable Integer rawStyleIndex) {
        if (rawDataType == null) {
            return getNumberOrDate(rawValue, rawStyleIndex);
        }
        switch (rawDataType) {
            case NUMBER_TYPE:
                return getNumberOrDate(rawValue, rawStyleIndex);
            case SHARED_STRING_TYPE:
                try {
                    return sharedStrings.apply(Integer.parseInt(rawValue));
                } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                    return null;
                }
            case STRING_TYPE:
                return rawValue;
            case INLINE_STRING_TYPE:
                // TODO: rawValue might contain rich text
                return rawValue;
            case DATE_TYPE:
                try {
                    return isoDateFormat.parse(rawValue);
                } catch (ParseException ex) {
                    return null;
                }
            default:
                // BOOLEAN or ERROR or default
                return null;
        }
    }
}
