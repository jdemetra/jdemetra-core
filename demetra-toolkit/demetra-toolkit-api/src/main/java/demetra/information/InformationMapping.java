/*
 * Copyright 2021 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.information;

import nbbrd.design.Development;
import nbbrd.design.ThreadSafe;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import demetra.util.WildCards;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @param <S>
 * @since 2.2.0
 * @author Jean Palate
 */
@ThreadSafe
@Development(status = Development.Status.Release)
public abstract class InformationMapping<S> implements InformationExtractor<S> {

    private final LinkedHashMap<String, BasicInformationExtractor<S>> map = new LinkedHashMap<>();
    public <Q> void set(@NonNull final String name, final Class<Q> targetClass,
            final Function<S, Q> fn) {
        map.put(name, BasicInformationExtractor.extractor(name, targetClass, fn));
    }

    public <Q> void delegate(final String name, final Class<Q> target, final Function<S, Q> fn) {
        map.put(name, BasicInformationExtractor.delegate(name, target, fn));
    }

    public <Q> void delegateArray(final String name, final int start, final int end, 
            final Class<Q> target, final BiFunction<S, Integer, Q> fn) {
        map.put(name, BasicInformationExtractor.delegateArray(name, start, end, target, fn));
    }

    public <Q> void setArray(@NonNull final String name, final int start, final int end,
            final Class<Q> targetClass, final BiFunction<S, Integer, Q> fn) {
        map.put(name, BasicInformationExtractor.array(name, start, end, targetClass, fn));
    }

    public <Q> void setArray(@NonNull final String name, final int defparam,
            final Class<Q> targetClass, final BiFunction<S, Integer, Q> fn) {
        map.put(name, BasicInformationExtractor.array(name, defparam, targetClass, fn));
    }

    @Override
    public void fillDictionary(String prefix, Map<String, Class> dic, boolean compact) {
        synchronized (this) {
            map.forEach((key, extractor) -> extractor.fillDictionary(prefix, dic, compact));
        }
    }

    private BasicInformationExtractor<S> search(String id) {
        // fast search
        synchronized (this) {
            BasicInformationExtractor<S> extractor = map.get(id);
            if (extractor != null) {
                return extractor;
            } else {
                // slow search
                Optional<Entry<String, BasicInformationExtractor<S>>> findFirst = map.entrySet().stream().filter(entry -> entry.getValue().contains(id)).findFirst();
                if (findFirst.isPresent()) {
                    return findFirst.get().getValue();
                } else {
                    return null;
                }
            }
        }
    }

    @Override
    public boolean contains(String id) {
        BasicInformationExtractor<S> extractor = search(id);
        return extractor == null ? false : extractor.contains(id);
    }

    @Override
    public <T> T getData(S source, String id, Class<T> tclass) {
        BasicInformationExtractor<S> extractor = search(id);
        return extractor == null ? null : extractor.getData(source, id, tclass);
    }

    @Override
    public <T> void searchAll(S source, WildCards wc, Class<T> tclass, Map<String, T> all) {
        map.forEach((name, extractor) -> extractor.searchAll(source, wc, tclass, all));
    }
}
