package demetra.tsprovider.util;

import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import internal.util.Strings;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface PropertyHandler<P> {

    default @NonNull P getDefaultValue() {
        return get(ignored -> null);
    }

    @NonNull P get(@NonNull Function<? super String, ? extends CharSequence> properties);

    void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable P value);

    default DataSource.@NonNull Converter<P> asDataSourceConverter() {
        return new DataSource.Converter<P>() {
            @Override
            public @NonNull P getDefaultValue() {
                return PropertyHandler.this.getDefaultValue();
            }

            @Override
            public @NonNull P get(@NonNull DataSource config) {
                return PropertyHandler.this.get(config::getParameter);
            }

            @Override
            public void set(DataSource.@NonNull Builder builder, @Nullable P value) {
                PropertyHandler.this.set(builder::parameter, value);
            }
        };
    }

    default DataSet.@NonNull Converter<P> asDataSetConverter() {
        return new DataSet.Converter<P>() {
            @Override
            public @NonNull P getDefaultValue() {
                return PropertyHandler.this.getDefaultValue();
            }

            @Override
            public @NonNull P get(@NonNull DataSet config) {
                return PropertyHandler.this.get(config::getParameter);
            }

            @Override
            public void set(DataSet.@NonNull Builder builder, @Nullable P value) {
                PropertyHandler.this.set(builder::parameter, value);
            }
        };
    }

    static @NonNull PropertyHandler<Boolean> onBoolean(String key, boolean defaultValue) {
        BooleanProperty delegate = BooleanProperty.of(key, defaultValue);
        return new PropertyHandler<Boolean>() {
            @Override
            public @NonNull Boolean getDefaultValue() {
                return delegate.isDefaultValue();
            }

            @Override
            public @NonNull Boolean get(@NonNull Function<? super String, ? extends CharSequence> properties) {
                return delegate.get(properties);
            }

            @Override
            public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable Boolean value) {
                if (value != null) {
                    delegate.set(properties, value);
                }
            }
        };
    }

    static PropertyHandler<Long> onLong(String key, long defaultValue) {
        return of(Property.of(key, defaultValue, Parser.onLong(), Formatter.onLong()));
    }

    static PropertyHandler<Integer> onInteger(String key, int defaultValue) {
        return of(Property.of(key, defaultValue, Parser.onInteger(), Formatter.onInteger()));
    }

    static <T extends Enum<T>> @NonNull PropertyHandler<T> onEnum(String key, T defaultValue) {
        return of(Property.of(key, defaultValue, Parser.onEnum((Class<T>) defaultValue.getClass()), Formatter.onEnum()));
    }

    static @NonNull PropertyHandler<String> onString(String key, String defaultValue) {
        return of(Property.of(key, defaultValue, Parser.onString(), Formatter.onString()));
    }

    static @NonNull PropertyHandler<File> onFile(String key, File defaultValue) {
        return of(Property.of(key, defaultValue, Parser.onFile(), Formatter.onFile()));
    }

    static @NonNull PropertyHandler<Locale> onLocale(String key, Locale defaultValue) {
        return of(Property.of(key, defaultValue, Parser.onLocale(), Formatter.of(Locale::toString)));
    }

    static PropertyHandler<List<String>> onStringList(String key, List<String> defaultValue, char separator) {
        Function<CharSequence, Stream<String>> splitter = o -> Strings.splitToStream(separator, o).map(String::trim).filter(Strings::isNotEmpty);
        Function<Stream<CharSequence>, String> joiner = o -> o.collect(Collectors.joining(String.valueOf(separator)));
        return of(Property.of(key, defaultValue, Parser.onStringList(splitter), Formatter.onStringList(joiner)));
    }

    static PropertyHandler<Duration> onDurationInMillis(String key, Duration defaultValue) {
        return of(Property.of(key, defaultValue, Parser.onLong().andThen(Duration::ofMillis), Formatter.onLong().compose(Duration::toMillis)));
    }

    static PropertyHandler<Charset> onCharset(String key, Charset defaultValue) {
        return of(Property.of(key, defaultValue, Parser.onCharset(), Formatter.onCharset()));
    }

    static <T> @NonNull PropertyHandler<T> of(String key, T defaultValue, Function<? super CharSequence, T> parser, Function<T, ? extends CharSequence> formatter) {
        return of(Property.of(key, defaultValue, Parser.of(parser), Formatter.of(formatter)));
    }

    @Deprecated
    static <T> @NonNull PropertyHandler<T> of(Property<T> delegate) {
        return new PropertyHandler<T>() {
            @Override
            public @NonNull T getDefaultValue() {
                return delegate.getDefaultValue();
            }

            @Override
            public @NonNull T get(@NonNull Function<? super String, ? extends CharSequence> properties) {
                return delegate.get(properties);
            }

            @Override
            public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable T value) {
                delegate.set(properties, value);
            }
        };
    }

    default PropertyHandler<P> withPrefix(String prefix) {
        PropertyHandler<P> delegate = this;
        return new PropertyHandler<P>() {
            @Override
            public @NonNull P getDefaultValue() {
                return delegate.getDefaultValue();
            }

            @Override
            public @NonNull P get(@NonNull Function<? super String, ? extends CharSequence> properties) {
                return delegate.get(key -> properties.apply(prefix + key));
            }

            @Override
            public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable P value) {
                delegate.set((key, text) -> properties.accept(prefix + key, text), value);
            }
        };
    }
}
