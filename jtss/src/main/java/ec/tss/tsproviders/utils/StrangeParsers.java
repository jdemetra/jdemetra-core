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
package ec.tss.tsproviders.utils;

import ec.tstoolkit.design.UtilityClass;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

@UtilityClass(IParser.class)
public final class StrangeParsers {

    private StrangeParsers() {
        // static class
    }

    /**
     * Standard reporting periods are periods of time in relation to a reporting
     * year. Each of these standard reporting periods has a duration (based on
     * the ISO 8601 definition) associated with it.
     *
     * @return a new parser
     */
    @Nonnull
    public static Parsers.Parser<Date> yearFreqPosParser() {
        return YearFreqPosParser.INSTANCE;
    }

    @Deprecated
    @Nonnull
    public static Parsers.Parser<Number> onDoubleValueOf() {
        return DoubleValueOf.INSTANCE;
    }

    @Deprecated
    @Nonnull
    public static Parsers.Parser<Number> onIntegerValueOf() {
        return IntegerValueOf.INSTANCE;
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static final class YearFreqPosParser extends Parsers.FailSafeParser<Date> {

        static final YearFreqPosParser INSTANCE = new YearFreqPosParser();

        @Override
        protected Date doParse(CharSequence input) throws Exception {
            Matcher m = regex.matcher(input);
            return m.matches() ? toDate(toInt(m.group(YEAR)), toFreq(m.group(FREQ)), toInt(m.group(POS))) : null;
        }

        private static Date toDate(int year, TsFrequency freq, int pos) throws TsException {
            return new TsPeriod(freq, year, pos - 1).firstday().getTime();
        }

        private static final Pattern regex = Pattern.compile("(\\d+)-?([QMYST])(\\d+)");
        private static final int YEAR = 1, FREQ = 2, POS = 3;

        private static int toInt(String input) {
            return Integer.parseInt(input);
        }

        private static TsFrequency toFreq(String input) {
            switch (input) {
                case "Q":
                    return TsFrequency.Quarterly;
                case "M":
                    return TsFrequency.Monthly;
                case "Y":
                    return TsFrequency.Yearly;
                case "S":
                    return TsFrequency.HalfYearly;
                case "T":
                    return TsFrequency.QuadriMonthly;
                default:
                    return TsFrequency.Undefined;
            }
        }
    }

    private static final class DoubleValueOf extends Parsers.FailSafeParser<Number> {

        static final DoubleValueOf INSTANCE = new DoubleValueOf();

        @Override
        protected Number doParse(CharSequence input) throws Exception {
            return Double.valueOf(input.toString());
        }
    }

    private static final class IntegerValueOf extends Parsers.FailSafeParser<Number> {

        static final IntegerValueOf INSTANCE = new IntegerValueOf();

        @Override
        protected Number doParse(CharSequence input) throws Exception {
            return Integer.valueOf(input.toString());
        }
    }
    //</editor-fold>
}
