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
import demetra.tsprovider.cube.BulkCubeConfig;
import demetra.util.List2;
import internal.tsprovider.util.*;
import nbbrd.design.ThreadSafe;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Tool that loads/stores values from/to a key-value structure. It provides a
 * best-effort retrieval behavior where a failure returns a default value
 * instead of an error. All implementations must be thread-safe.
 *
 * @param <S>
 * @param <P>
 * @author Philippe Charles
 * @since 1.0.0
 */
@ThreadSafe
public interface Param<S extends IConfig, P> {

    @NonNull
    P defaultValue();

    @NonNull
    P get(@NonNull S config);

    void set(IConfig.@NonNull Builder<?, S> builder, @Nullable P value);

    static @NonNull <S extends IConfig> Param<S, String> onString(@NonNull String defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parser.onString(), Formatter.onString());
    }

    static @NonNull <S extends IConfig> Param<S, File> onFile(@NonNull File defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parser.onFile(), Formatter.onFile());
    }

    static @NonNull <S extends IConfig, X extends Enum<X>> Param<S, X> onEnum(@NonNull X defaultValue, @NonNull String key) {
        Class<X> enumClass = (Class<X>) defaultValue.getClass();
        return new SingleParam<>(defaultValue, key, Parser.onEnum(enumClass), Formatter.<X>onEnum());
    }

    static @NonNull <S extends IConfig> Param<S, Integer> onInteger(@NonNull Integer defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parser.onInteger(), Formatter.onInteger());
    }

    static @NonNull <S extends IConfig> Param<S, Long> onLong(@NonNull Long defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parser.onLong(), Formatter.onLong());
    }

    static @NonNull <S extends IConfig> Param<S, Boolean> onBoolean(@NonNull Boolean defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parser.onBoolean(), Formatter.onBoolean());
    }

    static @NonNull <S extends IConfig> Param<S, Character> onCharacter(@NonNull Character defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parser.onCharacter(), Formatter.onCharacter());
    }

    static @NonNull <S extends IConfig> Param<S, Double> onDouble(@NonNull Double defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parser.onDouble(), Formatter.onDouble());
    }

    static @NonNull <S extends IConfig> Param<S, Charset> onCharset(@NonNull Charset defaultValue, @NonNull String key) {
        return new SingleParam<>(defaultValue, key, Parser.onCharset(), Formatter.onCharset());
    }

    static @NonNull <S extends IConfig> Param<S, ObsFormat> onObsFormat(@NonNull ObsFormat defaultValue, @NonNull String localeKey, @NonNull String datePatternKey, @NonNull String numberPatternKey) {
        return new ObsFormatParam(defaultValue, localeKey, datePatternKey, numberPatternKey);
    }

    static @NonNull <S extends IConfig> Param<S, double[]> onDoubleArray(@NonNull String key, @NonNull double... defaultValues) {
        return new SingleParam<>(defaultValues, key, Parser.onDoubleArray(), Formatter.onDoubleArray());
    }

    static @NonNull <S extends IConfig> Param<S, String[]> onStringArray(@NonNull String key, @NonNull String... defaultValues) {
        return new SingleParam<>(defaultValues, key, Parser.onStringArray(), Formatter.onStringArray());
    }

    static @NonNull <S extends IConfig> Param<S, List<String>> onStringList(@NonNull List<String> defaultValue, @NonNull String key,
                                                                            @NonNull Function<CharSequence, Stream<String>> splitter,
                                                                            @NonNull Function<Stream<CharSequence>, String> joiner) {
        return new SingleParam<>(List2.copyOf(defaultValue), key, Parser.onStringList(splitter), Formatter.onStringList(joiner));
    }

    static @NonNull <S extends IConfig> Param<S, ObsGathering> onObsGathering(@NonNull ObsGathering defaultValue, @NonNull String frequencyKey, @NonNull String aggregationKey, @NonNull String skipKey) {
        return new ObsGatheringParam(defaultValue, frequencyKey, aggregationKey, skipKey);
    }

    static @NonNull <S extends IConfig> Param<S, BulkCubeConfig> onBulkCubeConfig(@NonNull BulkCubeConfig defaultValue, @NonNull String ttlKey, @NonNull String depthKey) {
        return new BulkCubeConfigParam(defaultValue, ttlKey, depthKey);
    }
}
