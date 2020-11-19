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
package internal.tsprovider;

import demetra.tsprovider.TsMeta;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
@lombok.AllArgsConstructor
public final class DefaultTsMeta<T> implements TsMeta<T> {

    @lombok.Getter
    @lombok.NonNull
    private final String key;

    @lombok.NonNull
    private final Parser<T> parser;

    @lombok.NonNull
    private final Formatter<T> formatter;

    @Override
    public T load(Map<String, String> meta) {
        return load(meta::get);
    }

    @Override
    public T load(Function<String, String> meta) {
        String text = meta.apply(key);
        return text != null ? parser.parse(text) : null;
    }

    @Override
    public void store(Map<String, String> meta, T value) {
        store(meta::put, value);
    }

    @Override
    public void store(BiConsumer<String, String> meta, T value) {
        String text = formatter.formatAsString(value);
        meta.accept(key, text);
    }
}
