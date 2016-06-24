/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tstoolkit.utilities;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public final class GuavaCollectors {

    private GuavaCollectors() {
        // static class
    }

    @Nonnull
    public static <T> Collector<T, ImmutableList.Builder<T>, ImmutableList<T>> toImmutableList() {
        return new Collector<T, ImmutableList.Builder<T>, ImmutableList<T>>() {
            @Override
            public Supplier<ImmutableList.Builder<T>> supplier() {
                return ImmutableList.Builder::new;
            }

            @Override
            public BiConsumer<ImmutableList.Builder<T>, T> accumulator() {
                return (b, e) -> b.add(e);
            }

            @Override
            public BinaryOperator<ImmutableList.Builder<T>> combiner() {
                return (b1, b2) -> b1.addAll(b2.build());
            }

            @Override
            public Function<ImmutableList.Builder<T>, ImmutableList<T>> finisher() {
                return ImmutableList.Builder::build;
            }

            @Override
            public Set<Collector.Characteristics> characteristics() {
                return ImmutableSet.of();
            }
        };
    }
}
