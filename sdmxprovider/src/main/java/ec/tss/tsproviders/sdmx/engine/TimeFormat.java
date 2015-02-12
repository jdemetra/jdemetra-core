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
import ec.tss.tsproviders.utils.Parsers.Parser;
import static ec.tss.tsproviders.utils.StrangeParsers.yearFreqPosParser;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Date;
import java.util.Locale;

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
    P1Y(TsFrequency.Yearly, TsAggregationType.None) {
                @Override
                public Parser<Date> getParser() {
                    return onStrictDatePattern("yyyy")
                    .or(onStrictDatePattern("yyyy'-01'"))
                    .or(onStrictDatePattern("yyyy'-A1'"));
                }
            },
    /**
     * Semi-annual
     */
    P6M(TsFrequency.HalfYearly, TsAggregationType.None) {
                @Override
                public Parser<Date> getParser() {
                    return yearFreqPosParser().or(onStrictDatePattern("yyyy-MM"));
                }
            },
    /**
     * QuadriMonthly
     */
    P4M(TsFrequency.QuadriMonthly, TsAggregationType.None) {
                @Override
                public Parser<Date> getParser() {
                    return yearFreqPosParser().or(onStrictDatePattern("yyyy-MM"));
                }
            },
    /**
     * Quarterly
     */
    P3M(TsFrequency.Quarterly, TsAggregationType.None) {
                @Override
                public Parser<Date> getParser() {
                    return yearFreqPosParser().or(onStrictDatePattern("yyyy-MM"));
                }
            },
    /**
     * Monthly
     */
    P1M(TsFrequency.Monthly, TsAggregationType.None) {
                @Override
                public Parser<Date> getParser() {
                    return yearFreqPosParser().or(onStrictDatePattern("yyyy-MM"));
                }
            },
    /**
     * Weekly
     */
    P7D(TsFrequency.Monthly, TsAggregationType.Last) {
                @Override
                public Parser<Date> getParser() {
                    return onStrictDatePattern("yyyy-MM-dd");
                }
            },
    /**
     * Daily
     */
    P1D(TsFrequency.Monthly, TsAggregationType.Last) {
                @Override
                public Parser<Date> getParser() {
                    return onStrictDatePattern("yyyy-MM-dd");
                }
            },
    /**
     * Minutely
     */
    PT1M(TsFrequency.Monthly, TsAggregationType.Last) {
                @Override
                public Parser<Date> getParser() {
                    return onStrictDatePattern("yyyy-MM-dd");
                }
            },
    /**
     * Fallback
     */
    UNDEFINED(TsFrequency.Undefined, TsAggregationType.None) {
                @Override
                public Parser<Date> getParser() {
                    return onStrictDatePattern("yyyy-MM");
                }
            };

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

    abstract public Parser<Date> getParser();

    private static Parsers.Parser<Date> onStrictDatePattern(String datePattern) {
        return Parsers.onStrictDatePattern(datePattern, Locale.ROOT);
    }
}
