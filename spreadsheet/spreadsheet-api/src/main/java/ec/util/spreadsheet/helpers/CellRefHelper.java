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
package ec.util.spreadsheet.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
public final class CellRefHelper {

    private static final Pattern REF_PATTERN = Pattern.compile("(\\w+?)(\\d+?)");
    private static final int NO_MATCH_INDEX = -1;
    private Matcher m;

    public boolean parse(@Nullable String ref) {
        if (ref == null) {
            m = null;
            return false;
        }
        m = REF_PATTERN.matcher(ref);
        return m.matches();
    }

    /**
     * Returns a zero-based column index.
     *
     * @return
     */
    @Nonnegative
    public int getColumnIndex() {
        return m == null ? NO_MATCH_INDEX : getColumnIndex(m.group(1));
    }

    /**
     * Returns a zero-based row index.
     *
     * @return
     */
    @Nonnegative
    public int getRowIndex() {
        return m == null ? NO_MATCH_INDEX : (Integer.parseInt(m.group(2)) - 1);
    }

    //@VisibleForTesting
    static int getColumnIndex(String label) {
        int col = 0;
        for (int i = 0; i < label.length(); i++) {
            col = col * 26 + ((byte) label.charAt(i) - (byte) 'A') + 1;
        }
        col--;
        return col;
    }

    @Nonnull
    public static String getRowLabel(@Nonnegative int rowIndex) {
        return Integer.toString(rowIndex + 1);
    }

    @Nonnull
    public static String getColumnLabel(@Nonnegative int columnIndex) {
        int dividend = columnIndex + 1;
        String result = "";
        int modulo;

        while (dividend > 0) {
            modulo = (dividend - 1) % 26;
            result = (char) (65 + modulo) + result;
            dividend = (int) ((dividend - modulo) / 26);
        }

        return result;
    }

    @Nonnull
    public static String getCellRef(@Nonnegative int rowIndex, @Nonnegative int columnIndex) {
        return getColumnLabel(columnIndex) + getRowLabel(rowIndex);
    }
}
