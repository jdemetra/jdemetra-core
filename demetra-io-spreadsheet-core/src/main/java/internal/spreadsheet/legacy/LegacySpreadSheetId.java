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
package internal.spreadsheet.legacy;

import demetra.design.DemetraPlusLegacy;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@DemetraPlusLegacy
@lombok.Value
class LegacySpreadSheetId {

    private static final String BSEP = "<<";
    private static final String ESEP = ">>";

    @lombok.NonNull
    private String file;

    @lombok.NonNull
    private String sheetName;

    private String seriesName;

    private int indexSeries;

    @NonNull
    public static LegacySpreadSheetId collection(String file, String name) {
        return new LegacySpreadSheetId(file, name, null, -1);
    }

    @NonNull
    public static LegacySpreadSheetId series(String file, String sheetName, int spos) {
        return new LegacySpreadSheetId(file, sheetName, null, spos);
    }

    @NonNull
    public static LegacySpreadSheetId series(String file, String sheetName, String sname) {
        return new LegacySpreadSheetId(file, sheetName, sname, -1);
    }

    @Nullable
    public static LegacySpreadSheetId parse(@NonNull String input) {
        int beg = input.indexOf(BSEP);
        if (beg != 0) {
            return null;
        }
        beg += BSEP.length();
        int end = input.indexOf(ESEP, beg);
        if (end < 0) {
            return null;
        }
        String fname = input.substring(beg, end);
        beg = end + ESEP.length();
        beg = input.indexOf(BSEP, beg);
        if (beg < 0) {
            return null;
        }
        beg += BSEP.length();
        end = input.indexOf(ESEP, beg);
        if (end < 0) {
            return null;
        }
        String sheetname = input.substring(beg, end);
        beg = end + ESEP.length();
        if (beg < input.length()) {
            beg = input.indexOf(BSEP, beg);
            if (beg < 0) {
                return null;
            }
            beg += BSEP.length();
            end = input.indexOf(ESEP, beg);
            if (end < 0) {
                return null;
            }
            String s = input.substring(beg, end);
            try {
                int sid = Short.parseShort(s);
                return series(fname, sheetname, sid);
            } catch (NumberFormatException err) {
            }
            return series(fname, sheetname, s);
        } else {
            return collection(fname, sheetname);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(BSEP).append(file).append(ESEP).append(BSEP).append(sheetName).append(ESEP);
        if (isSeries()) {
            if (indexSeries >= 0) {
                builder.append(BSEP).append(indexSeries).append(ESEP);
            } else {
                builder.append(BSEP).append(seriesName).append(ESEP);
            }
        }
        return builder.toString();
    }

    public boolean isCollection() {
        return -1 == indexSeries && seriesName == null;
    }

    public boolean isSeries() {
        return indexSeries >= 0 || seriesName != null;
    }

    public boolean isSeriesByIndex() {
        return indexSeries >= 0;
    }
}
