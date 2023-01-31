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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import ec.tstoolkit.design.UtilityClass;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@UtilityClass(IParam.class)
public final class Params {

    private Params() {
        // static class
    }

    @NonNull
    public static <S extends IConfig> IParam<S, String> onString(@NonNull String defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parsers.stringParser(), Formatters.stringFormatter());
    }

    @NonNull
    public static <S extends IConfig> IParam<S, File> onFile(@NonNull File defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parsers.fileParser(), Formatters.fileFormatter());
    }

    @NonNull
    public static <S extends IConfig, X extends Enum<X>> IParam<S, X> onEnum(@NonNull X defaultValue, @NonNull String key) {
        Class<X> enumClass = (Class<X>) defaultValue.getClass();
        return new SingleParam<>(defaultValue, key, Parsers.enumParser(enumClass), Formatters.<X>enumFormatter());
    }

    @NonNull
    public static <S extends IConfig> IParam<S, Integer> onInteger(@NonNull Integer defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parsers.intParser(), Formatters.intFormatter());
    }

    @NonNull
    public static <S extends IConfig> IParam<S, Long> onLong(@NonNull Long defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parsers.longParser(), Formatters.longFormatter());
    }

    @NonNull
    public static <S extends IConfig> IParam<S, Boolean> onBoolean(@NonNull Boolean defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parsers.boolParser(), Formatters.boolFormatter());
    }

    @NonNull
    public static <S extends IConfig> IParam<S, Character> onCharacter(@NonNull Character defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parsers.charParser(), Formatters.charFormatter());
    }

    @NonNull
    public static <S extends IConfig> IParam<S, Double> onDouble(@NonNull Double defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parsers.doubleParser(), Formatters.doubleFormatter());
    }

    @NonNull
    public static <S extends IConfig> IParam<S, Charset> onCharset(@NonNull Charset defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parsers.charsetParser(), Formatters.charsetFormatter());
    }

    @Deprecated
    @NonNull
    public static <S extends IConfig> IParam<S, DataFormat> onDataFormat(@NonNull DataFormat defaultValue, @NonNull String localeKey, @NonNull String datePatternKey) {
        return onDataFormat(defaultValue, localeKey, datePatternKey, "numberPattern");
    }

    @NonNull
    public static <S extends IConfig> IParam<S, DataFormat> onDataFormat(@NonNull DataFormat defaultValue, @NonNull String localeKey, @NonNull String datePatternKey, @NonNull String numberPatternKey) {
        return new DataFormatParam(defaultValue, localeKey, datePatternKey, numberPatternKey);
    }

    @NonNull
    public static <S extends IConfig> IParam<S, double[]> onDoubleArray(@NonNull String key, @NonNull double... defaultValues) {
        return new SingleParam<>(defaultValues, key, Parsers.doubleArrayParser(), Formatters.doubleArrayFormatter());
    }

    @NonNull
    public static <S extends IConfig> IParam<S, String[]> onStringArray(@NonNull String key, @NonNull String... defaultValues) {
        return new SingleParam<>(defaultValues, key, Parsers.stringArrayParser(), Formatters.stringArrayFormatter());
    }

    @NonNull
    public static <S extends IConfig> IParam<S, List<String>> onStringList(@NonNull List<String> defaultValue, @NonNull String key, @NonNull Splitter splitter, @NonNull Joiner joiner) {
        return new SingleParam<>(ImmutableList.copyOf(defaultValue), key, Parsers.onSplitter(splitter), Formatters.onJoiner(joiner));
    }

    @NonNull
    public static <S extends IConfig> IParam<S, ObsGathering> onObsGathering(@NonNull ObsGathering defaultValue, @NonNull String frequencyKey, @NonNull String aggregationKey, @NonNull String skipKey) {
        return new ObsGatheringParam(defaultValue, frequencyKey, aggregationKey, skipKey);
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static final class SingleParam<S extends IConfig, P> implements IParam<S, P> {

        private final P defaultValue;
        private final String key;
        private final IParser<P> parser;
        private final IFormatter<P> formatter;

        private SingleParam(
                @NonNull P defaultValue,
                @NonNull String key,
                @NonNull IParser<P> parser,
                @NonNull IFormatter<P> formatter) {
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

    private static final class DataFormatParam<S extends IConfig> implements IParam<S, DataFormat> {

        private final DataFormat defaultValue;
        private final String localeKey;
        private final String datePatternKey;
        private final String numberPatternKey;

        private DataFormatParam(
                @NonNull DataFormat defaultValue,
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

        @Nullable
        private Locale parseLocale(@NonNull String locale) {
            // Fix behavior change in Parsers#localeParser()
            Locale result = Parsers.localeParser().parse(locale);
            return Locale.ROOT.equals(result) && locale.isEmpty() ? null : result;
        }

        @Override
        public DataFormat defaultValue() {
            return defaultValue;
        }

        @Override
        public DataFormat get(IConfig config) {
            String locale = config.get(localeKey);
            String datePattern = config.get(datePatternKey);
            String numberPattern = config.get(numberPatternKey);
            return isValid(locale, datePattern)
                    ? DataFormat.of(parseLocale(locale), datePattern, numberPattern)
                    : defaultValue;
        }

        @Override
        public void set(IConfig.Builder<?, S> builder, DataFormat value) {
            Objects.requireNonNull(builder);
            if (!defaultValue.equals(value)) {
                builder.put(localeKey, value.getLocaleString());
                builder.put(datePatternKey, value.getDatePattern());
                builder.put(numberPatternKey, value.getNumberPattern());
            }
        }
    }

    private static final class ObsGatheringParam<S extends IConfig> implements IParam<S, ObsGathering> {

        private final ObsGathering defaultValue;
        private final IParam<S, TsFrequency> frequency;
        private final IParam<S, TsAggregationType> aggregationType;
        private final IParam<S, Boolean> skipMissingValues;

        private ObsGatheringParam(
                @NonNull ObsGathering defaultValue,
                @NonNull String frequencyKey,
                @NonNull String aggregationKey,
                @NonNull String skipKey) {
            this.defaultValue = defaultValue;
            this.frequency = onEnum(defaultValue.getFrequency(), frequencyKey);
            this.aggregationType = onEnum(defaultValue.getAggregationType(), aggregationKey);
            this.skipMissingValues = onBoolean(defaultValue.isSkipMissingValues(), skipKey);
        }

        @Override
        public ObsGathering defaultValue() {
            return defaultValue;
        }

        @Override
        public ObsGathering get(S config) {
            return skipMissingValues.get(config)
                    ? ObsGathering.excludingMissingValues(frequency.get(config), aggregationType.get(config))
                    : ObsGathering.includingMissingValues(frequency.get(config), aggregationType.get(config));
        }

        @Override
        public void set(IConfig.Builder<?, S> builder, ObsGathering value) {
            Objects.requireNonNull(builder);
            skipMissingValues.set(builder, value.isSkipMissingValues());
            frequency.set(builder, value.getFrequency());
            aggregationType.set(builder, value.getAggregationType());
        }
    }
    //</editor-fold>
}
