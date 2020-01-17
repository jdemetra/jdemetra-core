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

import demetra.timeseries.util.ObsGathering;
import demetra.data.AggregationType;
import demetra.timeseries.TsUnit;
import demetra.tsprovider.cube.BulkCubeConfig;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.util.Parser;
import demetra.util.Formatter;
import demetra.util.List2;
import internal.util.InternalParser;
import internal.util.Strings;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Params {

    @NonNull
    public <S extends IConfig> IParam<S, String> onString(@NonNull String defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parser.onString(), Formatter.onString());
    }

    @NonNull
    public <S extends IConfig> IParam<S, File> onFile(@NonNull File defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parser.onFile(), Formatter.onFile());
    }

    @NonNull
    public <S extends IConfig, X extends Enum<X>> IParam<S, X> onEnum(@NonNull X defaultValue, @NonNull String key) {
        Class<X> enumClass = (Class<X>) defaultValue.getClass();
        return new SingleParam<>(defaultValue, key, Parser.onEnum(enumClass), Formatter.<X>onEnum());
    }

    @NonNull
    public <S extends IConfig> IParam<S, Integer> onInteger(@NonNull Integer defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parser.onInteger(), Formatter.onInteger());
    }

    @NonNull
    public <S extends IConfig> IParam<S, Long> onLong(@NonNull Long defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parser.onLong(), Formatter.onLong());
    }

    @NonNull
    public <S extends IConfig> IParam<S, Boolean> onBoolean(@NonNull Boolean defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parser.onBoolean(), Formatter.onBoolean());
    }

    @NonNull
    public <S extends IConfig> IParam<S, Character> onCharacter(@NonNull Character defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parser.onCharacter(), Formatter.onCharacter());
    }

    @NonNull
    public <S extends IConfig> IParam<S, Double> onDouble(@NonNull Double defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parser.onDouble(), Formatter.onDouble());
    }

    @NonNull
    public <S extends IConfig> IParam<S, Charset> onCharset(@NonNull Charset defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parser.onCharset(), Formatter.onCharset());
    }

    @NonNull
    public <S extends IConfig> IParam<S, ObsFormat> onObsFormat(@NonNull ObsFormat defaultValue, @NonNull String localeKey, @NonNull String datePatternKey, @NonNull String numberPatternKey) {
        return new ObsFormatParam(defaultValue, localeKey, datePatternKey, numberPatternKey);
    }

    @NonNull
    public <S extends IConfig> IParam<S, double[]> onDoubleArray(@NonNull String key, @NonNull double... defaultValues) {
        return new SingleParam<>(defaultValues, key, Parser.onDoubleArray(), Formatter.onDoubleArray());
    }

    @NonNull
    public <S extends IConfig> IParam<S, String[]> onStringArray(@NonNull String key, @NonNull String... defaultValues) {
        return new SingleParam<>(defaultValues, key, Parser.onStringArray(), Formatter.onStringArray());
    }

    @NonNull
    public <S extends IConfig> IParam<S, List<String>> onStringList(@NonNull List<String> defaultValue, @NonNull String key,
            @NonNull Function<CharSequence, Stream<String>> splitter,
            @NonNull Function<Stream<CharSequence>, String> joiner) {
        return new SingleParam<>(List2.copyOf(defaultValue), key, Parser.onStringList(splitter), Formatter.onStringList(joiner));
    }

    @NonNull
    public <S extends IConfig> IParam<S, ObsGathering> onObsGathering(@NonNull ObsGathering defaultValue, @NonNull String frequencyKey, @NonNull String aggregationKey, @NonNull String skipKey) {
        return new ObsGatheringParam(defaultValue, frequencyKey, aggregationKey, skipKey);
    }

    @NonNull
    public <S extends IConfig> IParam<S, BulkCubeConfig> onBulkCubeConfig(@NonNull BulkCubeConfig defaultValue, @NonNull String ttlKey, @NonNull String depthKey) {
        return new BulkCubeConfigParam(defaultValue, ttlKey, depthKey);
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static final class SingleParam<S extends IConfig, P> implements IParam<S, P> {

        private final P defaultValue;
        private final String key;
        private final Parser<P> parser;
        private final Formatter<P> formatter;

        private SingleParam(
                @NonNull P defaultValue,
                @NonNull String key,
                @NonNull Parser<P> parser,
                @NonNull Formatter<P> formatter) {
            this.defaultValue = Objects.requireNonNull(defaultValue);
            this.key = Objects.requireNonNull(key);
            this.parser = Objects.requireNonNull(parser);
            this.formatter = Objects.requireNonNull(formatter);
        }

        private boolean isValid(@NonNull String tmp) {
            return !tmp.isEmpty();
        }

        @Override
        public P defaultValue() {
            return defaultValue;
        }

        @Override
        public P get(IConfig config) {
            String tmp = config.get(key);
            if (tmp != null && isValid(tmp)) {
                P result = parser.parse(tmp);
                if (result != null) {
                    return result;
                }
            }
            return defaultValue;
        }

        @Override
        public void set(IConfig.Builder<?, S> builder, P value) {
            Objects.requireNonNull(builder);
            if (!defaultValue.equals(value) && value != null) {
                String valueAsString = formatter.formatAsString(value);
                if (valueAsString != null) {
                    builder.put(key, valueAsString);
                }
            }
        }
    }

    private static final class ObsFormatParam<S extends IConfig> implements IParam<S, ObsFormat> {

        private final ObsFormat defaultValue;
        private final String localeKey;
        private final String datePatternKey;
        private final String numberPatternKey;

        private ObsFormatParam(
                @NonNull ObsFormat defaultValue,
                @NonNull String localeKey,
                @NonNull String datePatternKey,
                @NonNull String numberPatternKey) {
            this.defaultValue = Objects.requireNonNull(defaultValue);
            this.localeKey = Objects.requireNonNull(localeKey);
            this.datePatternKey = Objects.requireNonNull(datePatternKey);
            this.numberPatternKey = Objects.requireNonNull(numberPatternKey);
        }

        private boolean isValid(String locale, String datePattern) {
            return locale != null && datePattern != null;
        }

        @Override
        public ObsFormat defaultValue() {
            return defaultValue;
        }

        @Override
        public ObsFormat get(IConfig config) {
            String locale = config.get(localeKey);
            String datePattern = config.get(datePatternKey);
            String numberPattern = config.get(numberPatternKey);
            return isValid(locale, datePattern)
                    ? ObsFormat.of(InternalParser.parseLocale(locale), datePattern, numberPattern)
                    : defaultValue;
        }

        @Override
        public void set(IConfig.Builder<?, S> builder, ObsFormat value) {
            Objects.requireNonNull(builder);
            if (!defaultValue.equals(value)) {
                builder.put(localeKey, getLocaleValue(value.getLocale()));
                builder.put(datePatternKey, getDateTimePatternValue(value.getDateTimePattern()));
                builder.put(numberPatternKey, getNumberPatternValue(value.getNumberPattern()));
            }
        }

        private static String getLocaleValue(Locale locale) {
            return locale != null ? locale.toString() : "";
        }

        private static String getDateTimePatternValue(String dateTimePattern) {
            return Strings.nullToEmpty(dateTimePattern);
        }

        private static String getNumberPatternValue(String numberPattern) {
            return Strings.nullToEmpty(numberPattern);
        }
    }

    private static final class ObsGatheringParam<S extends IConfig> implements IParam<S, ObsGathering> {

        private final ObsGathering defaultValue;
        private final IParam<S, String> unit;
        private final IParam<S, AggregationType> aggregationType;
        private final IParam<S, Boolean> skipMissingValues;

        private ObsGatheringParam(
                @NonNull ObsGathering defaultValue,
                @NonNull String frequencyKey,
                @NonNull String aggregationKey,
                @NonNull String skipKey) {
            this.defaultValue = defaultValue;
            this.unit = onString(defaultValue.getUnit().toIsoString(), frequencyKey);
            this.aggregationType = onEnum(defaultValue.getAggregationType(), aggregationKey);
            this.skipMissingValues = onBoolean(defaultValue.isSkipMissingValues(), skipKey);
        }

        @Override
        public ObsGathering defaultValue() {
            return defaultValue;
        }

        @Override
        public ObsGathering get(S config) {
            return ObsGathering.builder()
                    .unit(getUnit(config))
                    .aggregationType(aggregationType.get(config))
                    .skipMissingValues(skipMissingValues.get(config))
                    .build();
        }

        @Override
        public void set(IConfig.Builder<?, S> builder, ObsGathering value) {
            Objects.requireNonNull(builder);
            skipMissingValues.set(builder, value.isSkipMissingValues());
            setFreq(builder, value.getUnit());
            aggregationType.set(builder, value.getAggregationType());
        }

        private TsUnit getUnit(S config) {
            String text = unit.get(config);
            TsUnit value = freqToUnit(text);
            if (value != null) {
                return value;
            }
            try {
                return TsUnit.parse(text);
            } catch (DateTimeParseException ex) {
                return TsUnit.parse(unit.defaultValue());
            }
        }

        private void setFreq(IConfig.Builder<?, S> builder, TsUnit value) {
            String freq = unitToFreq(value);
            unit.set(builder, freq != null ? freq : value.toIsoString());
        }
    }

    private TsUnit freqToUnit(String freq) {
        switch (freq) {
            case "Yearly":
                return TsUnit.YEAR;
            case "HalfYearly":
                return TsUnit.HALF_YEAR;
            case "QuadriMonthly":
                return TsUnit.of(4, ChronoUnit.MONTHS);
            case "Quarterly":
                return TsUnit.QUARTER;
            case "BiMonthly":
                return TsUnit.of(2, ChronoUnit.MONTHS);
            case "Monthly":
                return TsUnit.MONTH;
        }
        return null;
    }

    private String unitToFreq(TsUnit unit) {
        switch (unit.getChronoUnit()) {
            case YEARS:
                if (unit.getAmount() == 1) {
                    return "Yearly";
                }
                break;
            case MONTHS:
                if (unit.getAmount() == 6) {
                    return "HalfYearly";
                }
                if (unit.getAmount() == 4) {
                    return "QuadriMonthly";
                }
                if (unit.getAmount() == 3) {
                    return "Quarterly";
                }
                if (unit.getAmount() == 2) {
                    return "BiMonthly";
                }
                if (unit.getAmount() == 1) {
                    return "Monthly";
                }
                break;
        }
        return null;
    }

    private static final class BulkCubeConfigParam<S extends IConfig> implements IParam<S, BulkCubeConfig> {

        private final BulkCubeConfig defaultValue;
        private final IParam<S, Long> cacheTtl;
        private final IParam<S, Integer> cacheDepth;

        private BulkCubeConfigParam(
                @NonNull BulkCubeConfig defaultValue,
                @NonNull String ttlKey,
                @NonNull String depthKey) {
            this.defaultValue = defaultValue;
            this.cacheTtl = onLong(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES), ttlKey);
            this.cacheDepth = onInteger(1, depthKey);
        }

        @Override
        public BulkCubeConfig defaultValue() {
            return defaultValue;
        }

        @Override
        public BulkCubeConfig get(S config) {
            return BulkCubeConfig.of(Duration.ofMillis(cacheTtl.get(config)), cacheDepth.get(config));
        }

        @Override
        public void set(IConfig.Builder<?, S> builder, BulkCubeConfig value) {
            Objects.requireNonNull(builder);
            cacheTtl.set(builder, value.getTtl().toMillis());
            cacheDepth.set(builder, value.getDepth());
        }
    }
    //</editor-fold>
}
