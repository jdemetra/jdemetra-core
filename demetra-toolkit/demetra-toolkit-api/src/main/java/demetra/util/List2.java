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
package demetra.util;

import demetra.design.NextJdk;
import demetra.design.ReturnImmutable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collector;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class List2 {

    @NextJdk(NextJdk.JDK9)
    @ReturnImmutable
    @SafeVarargs
    @Nonnull
    public <E> List<E> of​(@Nonnull E... elements) {
        switch (elements.length) {
            case 0:
                return Collections.emptyList();
            case 1:
                return Collections.singletonList(Objects.requireNonNull(elements[0]));
            default:
                List<E> result = new ArrayList<>();
                for (E element : elements) {
                    result.add(Objects.requireNonNull(element));
                }
                return Collections.unmodifiableList(result);
        }
    }

    @NextJdk(NextJdk.JDK10)
    @ReturnImmutable
    @Nonnull
    public <E> List<E> copyOf​(@Nonnull Collection<? extends E> coll) {
        switch (coll.size()) {
            case 0:
                return Collections.emptyList();
            case 1:
                return Collections.singletonList(Objects.requireNonNull(coll.iterator().next()));
            default:
                List<E> result = new ArrayList<>();
                for (E element : coll) {
                    result.add(Objects.requireNonNull(element));
                }
                return Collections.unmodifiableList(result);
        }
    }

    @NextJdk(NextJdk.JDK10)
    @ReturnImmutable
    @Nonnull
    public <T> Collector<T, ?, List<T>> toUnmodifiableList() {
        return Collector.of((Supplier<List<T>>) ArrayList::new, List2::accumulate, List2::combine, Collections::unmodifiableList);
    }

    private <T> void accumulate(List<T> r, T t) {
        r.add(Objects.requireNonNull(t));
    }

    private <T> List<T> combine(List<T> l, List<T> r) {
        l.addAll(r);
        return l;
    }
}
