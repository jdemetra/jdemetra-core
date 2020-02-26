/*
 * Copyright 2018 National Bank of Belgium
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
package demetra.tsprovider;

import demetra.tsprovider.util.ObsFormat;
import internal.tsprovider.DefaultTsMeta;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public interface TsMeta<T> {

    @NonNull
    String getKey();

    @Nullable
    T load(@NonNull Function<String, String> meta);

    @Nullable
    default T load(@NonNull Map<String, String> meta) {
        return load(meta::get);
    }

    void store(@NonNull BiConsumer<String, String> meta, @NonNull T value);

    default void store(@NonNull Map<String, String> meta, @NonNull T value) {
        store(meta::put, value);
    }

    @NonNull
    static <T> TsMeta<T> on(@NonNull String key, @NonNull Parser<T> parser, @NonNull Formatter<T> formatter) {
        return new DefaultTsMeta<>(key, parser, formatter);
    }

    @NonNull
    static TsMeta<String> onString(@NonNull String key) {
        return new DefaultTsMeta<>(key, Parser.onString(), Formatter.onString());
    }

    @NonNull
    static TsMeta<LocalDateTime> onDateTime(@NonNull String key, @NonNull String datePattern, @NonNull Locale locale) {
        ObsFormat obsFormat = ObsFormat.of(locale, datePattern, null);
        return new DefaultTsMeta<>(key, obsFormat.dateTimeParser(), obsFormat.dateTimeFormatter());
    }

    @NonNull
    static TsMeta<LocalDateTime> onTimestamp() {
        DateTimeFormatter main = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        ObsFormat legacy = ObsFormat.of(Locale.ROOT, "EEE MMM dd HH:mm:ss zzz yyyy", null);
        return new DefaultTsMeta<>("@timestamp",
                Parser.onDateTimeFormatter(main, LocalDateTime::from).orElse(legacy.dateTimeParser()),
                Formatter.onDateTimeFormatter(main));
    }

    // from ec.tss.Ts
    @Deprecated
    static final TsMeta<String> SOURCE_OLD = onString("tsmoniker.source");

    // from ec.tss.Ts
    @Deprecated
    static final TsMeta<String> ID_OLD = onString("tsmoniker.id");

    // from ec.tss.Ts
    @Deprecated
    static final TsMeta<String> DYNAMIC = onString("dynamic");

    // from ec.tss.Ts
    static final TsMeta<LocalDateTime> BEG = onDateTime("@beg", "yyyy-MM-dd", Locale.ROOT);

    // from ec.tss.Ts
    static final TsMeta<LocalDateTime> END = onDateTime("@end", "yyyy-MM-dd", Locale.ROOT);

    // from ec.tss.Ts
    static final TsMeta<?> CONFIDENTIAL = onString("@confidential");

    // from ec.tstoolkit.MetaData
    static final TsMeta<String> DESCRIPTION = onString("@description");

    // from ec.tstoolkit.MetaData
    static final TsMeta<String> OWNER = onString("@owner");

    // from ec.tstoolkit.MetaData
    static final TsMeta<String> SOURCE = onString("@source");

    // from ec.tstoolkit.MetaData
    static final TsMeta<String> ID = onString("@id");

    // from ec.tstoolkit.MetaData
    static final TsMeta<LocalDateTime> TIMESTAMP = onTimestamp();

    // from ec.tstoolkit.MetaData
    static final TsMeta<?> DOCUMENT = onString("@document");

    // from ec.tstoolkit.MetaData
    static final TsMeta<?> SUMMARY = onString("@summary");

    // from ec.tstoolkit.MetaData
    static final TsMeta<String> NOTE = onString("@note");

    // from ec.tstoolkit.MetaData
    static final TsMeta<?> TODO = onString("@todo");

    // from ec.tstoolkit.MetaData
    static final TsMeta<?> ALGORITHM = onString("@algorithm");

    // from ec.tstoolkit.MetaData
    static final TsMeta<?> QUALITY = onString("@quality");
}
