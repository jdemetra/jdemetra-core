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
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@UtilityClass(IParam.class)
public final class Params {

    private Params() {
        // static class
    }

    @Nonnull
    public static <S extends IConfig> IParam<S, String> onString(@Nonnull String defaultValue, @Nonnull String key) {
        return new SingleParam<>(defaultValue, key, Parsers.stringParser(), Formatters.stringFormatter());
    }

    @Nonnull
    public static <S extends IConfig> IParam<S, File> onFile(@Nonnull File defaultValue, @Nonnull String key) {
        return new SingleParam<>(defaultValue, key, Parsers.fileParser(), Formatters.fileFormatter());
    }

    @Nonnull
    public static <S extends IConfig, X extends Enum<X>> IParam<S, X> onEnum(@Nonnull X defaultValue, @Nonnull String key) {
        Class<X> enumClass = (Class<X>) defaultValue.getClass();
        return new SingleParam<>(defaultValue, key, Parsers.enumParser(enumClass), Formatters.<X>enumFormatter());
    }

    @Nonnull
    public static <S extends IConfig> IParam<S, Integer> onInteger(@Nonnull Integer defaultValue, @Nonnull String key) {
        return new SingleParam<>(defaultValue, key, Parsers.intParser(), Formatters.intFormatter());
    }

    @Nonnull
    public static <S extends IConfig> IParam<S, Long> onLong(@Nonnull Long defaultValue, @Nonnull String key) {
        return new SingleParam<>(defaultValue, key, Parsers.longParser(), Formatters.longFormatter());
    }

    @Nonnull
    public static <S extends IConfig> IParam<S, Boolean> onBoolean(@Nonnull Boolean defaultValue, @Nonnull String key) {
        return new SingleParam<>(defaultValue, key, Parsers.boolParser(), Formatters.boolFormatter());
    }

    @Nonnull
    public static <S extends IConfig> IParam<S, Double> onDouble(@Nonnull Double defaultValue, @Nonnull String key) {
        return new SingleParam<>(defaultValue, key, Parsers.doubleParser(), Formatters.doubleFormatter());
    }

    @Nonnull
    public static <S extends IConfig> IParam<S, Charset> onCharset(@Nonnull Charset defaultValue, @Nonnull String key) {
        return new SingleParam<>(defaultValue, key, Parsers.charsetParser(), Formatters.charsetFormatter());
    }

    @Deprecated
    @Nonnull
    public static <S extends IConfig> IParam<S, DataFormat> onDataFormat(@Nonnull DataFormat defaultValue, @Nonnull String localeKey, @Nonnull String datePatternKey) {
        return onDataFormat(defaultValue, localeKey, datePatternKey, "numberPattern");
    }

    @Nonnull
    public static <S extends IConfig> IParam<S, DataFormat> onDataFormat(@Nonnull DataFormat defaultValue, @Nonnull String localeKey, @Nonnull String datePatternKey, @Nonnull String numberPatternKey) {
        return new DataFormatParam(defaultValue, localeKey, datePatternKey, numberPatternKey);
    }

    @Nonnull
    public static <S extends IConfig> IParam<S, double[]> onDoubleArray(@Nonnull String key, @Nonnull double... defaultValues) {
        return new SingleParam<>(defaultValues, key, Parsers.doubleArrayParser(), Formatters.doubleArrayFormatter());
    }

    @Nonnull
    public static <S extends IConfig> IParam<S, List<String>> onStringList(@Nonnull List<String> defaultValue, @Nonnull String key, @Nonnull Splitter splitter, @Nonnull Joiner joiner) {
        return new SingleParam<>(ImmutableList.copyOf(defaultValue), key, Parsers.onSplitter(splitter), Formatters.onJoiner(joiner));
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static final class SingleParam<S extends IConfig, P> implements IParam<S, P> {

        private final P defaultValue;
        private final String key;
        private final IParser<P> parser;
        private final IFormatter<P> formatter;

        private SingleParam(@Nonnull P defaultValue, @Nonnull String key, @Nonnull IParser<P> parser, @Nonnull IFormatter<P> formatter) {
            this.defaultValue = defaultValue;
            this.key = key;
            this.parser = parser;
            this.formatter = formatter;
        }

        private boolean isValid(@Nonnull String tmp) {
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

        public DataFormatParam(@Nonnull final DataFormat defaultValue, @Nonnull final String localeKey, @Nonnull final String datePatternKey, @Nonnull final String numberPatternKey) {
            this.defaultValue = defaultValue;
            this.localeKey = localeKey;
            this.datePatternKey = datePatternKey;
            this.numberPatternKey = numberPatternKey;
        }

        private boolean isValid(String locale, String datePattern) {
            return locale != null && datePattern != null;
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
            return isValid(locale, datePattern) ? DataFormat.create(locale, datePattern, numberPattern) : defaultValue;
        }

        @Override
        public void set(IConfig.Builder<?, S> builder, DataFormat value) {
            if (!defaultValue.equals(value)) {
                builder.put(localeKey, value.getLocaleString());
                builder.put(datePatternKey, value.getDatePattern());
                builder.put(numberPatternKey, value.getNumberPattern());
            }
        }
    }
    //</editor-fold>
}
