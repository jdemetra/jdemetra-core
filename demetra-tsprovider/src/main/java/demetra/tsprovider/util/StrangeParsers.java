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
package demetra.tsprovider.util;

import demetra.timeseries.TsException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import demetra.util.Parser;
import java.time.LocalDate;

@lombok.experimental.UtilityClass
public class StrangeParsers {

    /**
     * Standard reporting periods are periods of time in relation to a reporting
     * year. Each of these standard reporting periods has a duration (based on
     * the ISO 8601 definition) associated with it.
     *
     * @return a new parser
     */
    @Nonnull
    public Parser<LocalDate> yearFreqPosParser() {
        return StrangeParsers::parseYearFreqPos;
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private LocalDate parseYearFreqPos(CharSequence input) {
        Matcher m = REGEX.matcher(input);
        if (m.matches()) {
            try {
                return toDate(Integer.parseInt(m.group(YEAR)), m.group(FREQ).charAt(0), Integer.parseInt(m.group(POS)));
            } catch (TsException | NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private static final Pattern REGEX = Pattern.compile("(\\d+)-?([QMYST])(\\d+)");
    private static final int YEAR = 1, FREQ = 2, POS = 3;

    private static LocalDate toDate(int year, char freq, int pos) {
        switch (freq) {
            case 'Q':
                return LocalDate.of(year, ((pos - 1) * 3) + 1, 1);
            case 'M':
                return LocalDate.of(year, pos, 1);
            case 'Y':
                return LocalDate.of(year, 1, 1);
            case 'S':
                return LocalDate.of(year, ((pos - 1) * 6) + 1, 1);
            case 'T':
                return LocalDate.of(year, ((pos - 1) * 4) + 1, 1);
            default:
                throw new RuntimeException();
        }
    }
    //</editor-fold>
}
