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
package ec.util.spreadsheet.poi;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 *
 * @author Philippe Charles
 */
final class FastPoiValueFactory {

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

    private final FastPoiContext context;
    private final Calendar calendar;
    private final DateFormat isoDateFormat;

    public FastPoiValueFactory(@Nonnull FastPoiContext context) {
        this.context = context;
        // using default time-zone
        this.calendar = new GregorianCalendar();
        this.isoDateFormat = new SimpleDateFormat(ISO_DATE_FORMAT);
    }

    private boolean isADateFormat(@Nullable String rawStyleIndex) throws IndexOutOfBoundsException, NumberFormatException {
        return rawStyleIndex != null ? context.isADateFormat(Integer.parseInt(rawStyleIndex)) : false;
    }

    @Nullable
    private Object getNumberOrDate(@Nonnull String rawValue, @Nullable String rawStyleIndex) {
        try {
            double number = Double.parseDouble(rawValue);
            if (DateUtil.isValidExcelDate(number) && isADateFormat(rawStyleIndex)) {
                return getJavaDate(calendar, number, context.isDate1904());
            }
            return number;
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            return null;
        }
    }

    @Nullable
    public Object getValue(@Nonnull String rawValue, @Nullable String rawDataType, @Nullable String rawStyleIndex) {
        if (rawDataType == null) {
            return getNumberOrDate(rawValue, rawStyleIndex);
        }
        switch (rawDataType) {
            case NUMBER_TYPE:
                return getNumberOrDate(rawValue, rawStyleIndex);
            case SHARED_STRING_TYPE:
                try {
                    return context.getSharedString(Integer.parseInt(rawValue));
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

    /**
     * Same as {@link DateUtil#getJavaDate(double, boolean)} but with calendar
     * as first parameter to reduce memory usage.
     *
     * @param calendar
     * @param date
     * @param use1904windowing
     * @return
     */
    private static Date getJavaDate(Calendar calendar, double date, boolean use1904windowing) {
        int wholeDays = (int) Math.floor(date);
        int millisecondsInDay = (int) ((date - wholeDays) * DateUtil.DAY_MILLISECONDS + 0.5);
        DateUtil.setCalendar(calendar, wholeDays, millisecondsInDay, use1904windowing);
        return calendar.getTime();
    }
}
