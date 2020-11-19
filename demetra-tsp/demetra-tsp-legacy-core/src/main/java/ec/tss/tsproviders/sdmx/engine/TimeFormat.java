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
package ec.tss.tsproviders.sdmx.engine;

import ec.tss.tsproviders.utils.Parsers;
import static ec.tss.tsproviders.utils.StrangeParsers.yearFreqPosParser;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Date;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * http://stats.oecd.org/SDMXWS/sdmx.asmx
 * http://www.sdmxusers.org/forum/index.php?topic=22.0
 *
 * @author Philippe Charles
 */
public enum TimeFormat {

    /**
     * Annual
     */
    P1Y(TsFrequency.Yearly, TsAggregationType.None),
    /**
     * Semi-annual
     */
    P6M(TsFrequency.HalfYearly, TsAggregationType.None),
    /**
     * QuadriMonthly
     */
    P4M(TsFrequency.QuadriMonthly, TsAggregationType.None),
    /**
     * Quarterly
     */
    P3M(TsFrequency.Quarterly, TsAggregationType.None),
    /**
     * Monthly
     */
    P1M(TsFrequency.Monthly, TsAggregationType.None),
    /**
     * Weekly
     */
    P7D(TsFrequency.Monthly, TsAggregationType.Last),
    /**
     * Daily
     */
    P1D(TsFrequency.Monthly, TsAggregationType.Last),
    /**
     * Minutely
     */
    PT1M(TsFrequency.Monthly, TsAggregationType.Last),
    /**
     * Fallback
     */
    UNDEFINED(TsFrequency.Undefined, TsAggregationType.None);

    private final TsFrequency frequency;
    private final TsAggregationType aggregationType;

    private TimeFormat(TsFrequency frequency, TsAggregationType aggregationType) {
        this.frequency = frequency;
        this.aggregationType = aggregationType;
    }

    public TsFrequency getFrequency() {
        return frequency;
    }

    public TsAggregationType getAggregationType() {
        return aggregationType;
    }

    public final Parsers.@NonNull Parser<Date> getParser() {
        switch (this) {
            case P1Y:
                return onStrictDatePattern("yyyy").or(onStrictDatePattern("yyyy'-01'")).or(onStrictDatePattern("yyyy'-A1'"));
            case P6M:
                return yearFreqPosParser().or(onStrictDatePattern("yyyy-MM"));
            case P4M:
                return yearFreqPosParser().or(onStrictDatePattern("yyyy-MM"));
            case P3M:
                return yearFreqPosParser().or(onStrictDatePattern("yyyy-MM"));
            case P1M:
                return yearFreqPosParser().or(onStrictDatePattern("yyyy-MM"));
            case P7D:
                return onStrictDatePattern("yyyy-MM-dd");
            case P1D:
                return onStrictDatePattern("yyyy-MM-dd");
//            case HOURLY:
//                return onStrictDatePattern("yyyy-MM-dd");
            case PT1M:
                return onStrictDatePattern("yyyy-MM-dd");
            default:
                return onStrictDatePattern("yyyy-MM");
        }
    }

    private static Parsers.Parser<Date> onStrictDatePattern(String datePattern) {
        return Parsers.onStrictDatePattern(datePattern, Locale.ROOT);
    }

    @NonNull
    public static TimeFormat parseByFrequencyCodeId(@NonNull String input) {
        switch (input) {
            case "A":
                return P1Y;
            case "S":
                return P6M;
            case "T":
                return P4M;
            case "Q":
                return P3M;
            case "M":
                return P1M;
            case "W":
                return P7D;
            case "D":
                return P1D;
//            case "H":
//                return HOURLY;
            case "I":
                return PT1M;
            default:
                return UNDEFINED;
        }
    }

    @NonNull
    public static TimeFormat parseByTimeFormat(@NonNull String input) {
        switch (input) {
            case "P1Y":
                return P1Y;
            case "P6M":
                return P6M;
            case "P4M":
                return P4M;
            case "P3M":
                return P3M;
            case "P1M":
                return P1M;
            case "P7D":
                return P7D;
            case "P1D":
                return P1D;
//            case "???":
//                return HOURLY;
            case "PT1M":
                return PT1M;
            default:
                return UNDEFINED;
        }
    }
}
