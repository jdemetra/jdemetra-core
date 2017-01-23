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
package ec.tss.tsproviders.cursor;

import _util.IOExceptionUtil.FirstIO;
import _util.IOExceptionUtil.SecondIO;
import static _util.IOExceptionUtil.asCloseable;
import _util.tsproviders.ResourceWatcher;
import static _util.tsproviders.TsCursorUtil.forEachId;
import static _util.tsproviders.TsCursorUtil.readAllAndClose;
import ec.tss.tsproviders.utils.OptionalTsData;
import static com.google.common.collect.Iterators.forArray;
import static com.google.common.collect.Iterators.singletonIterator;
import com.google.common.collect.Maps;
import ec.tss.tsproviders.cursor.TsCursors.EmptyCursor;
import ec.tss.tsproviders.cursor.TsCursors.FilteringCursor;
import ec.tss.tsproviders.cursor.TsCursors.InMemoryCursor;
import ec.tss.tsproviders.cursor.TsCursors.IteratingCursor;
import static ec.tss.tsproviders.cursor.TsCursors.NOT_REQUESTED;
import static ec.tss.tsproviders.cursor.TsCursors.NO_DATA;
import static ec.tss.tsproviders.cursor.TsCursors.NO_META;
import ec.tss.tsproviders.cursor.TsCursors.OnCloseCursor;
import ec.tss.tsproviders.cursor.TsCursors.SingletonCursor;
import ec.tss.tsproviders.cursor.TsCursors.TransformingCursor;
import ec.tss.tsproviders.cursor.TsCursors.WithMetaDataCursor;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import static java.util.function.Function.identity;
import java.util.function.Supplier;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TsCursorTest {

    private final String someKey = "hello";
    private final OptionalTsData someData = OptionalTsData.present(TsData.random(TsFrequency.Monthly, 1));
    private final MetaData someMeta = new MetaData();

    private final Function<String, String> goodIdFunc = String::toUpperCase;
    private final Function<String, OptionalTsData> goodDataFunc = o -> someData;
    private final Function<String, Map<String, String>> goodMetaFunc = o -> someMeta;
    private final Function<String, String> badIdFunc = o -> null;
    private final Function<String, OptionalTsData> badDataFunc = o -> null;
    private final Function<String, Map<String, String>> badMetaFunc = o -> null;

    @Test
    @SuppressWarnings("null")
    public void testSingletonFactories() {
        assertThatThrownBy(() -> TsCursor.singleton(null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> TsCursor.singleton(null, NOT_REQUESTED)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsCursor.singleton("", null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> TsCursor.singleton(null, NOT_REQUESTED, emptyMap())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsCursor.singleton("", null, emptyMap())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsCursor.singleton("", NOT_REQUESTED, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testIteratorFactories() {
        assertThatThrownBy(() -> TsCursor.from(null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> TsCursor.from(null, NO_DATA)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsCursor.from(emptyIterator(), null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> TsCursor.from(null, NO_DATA, NO_META)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsCursor.from(emptyIterator(), null, NO_META)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsCursor.from(emptyIterator(), NO_DATA, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testEmptyCursor() {
        try (EmptyCursor cursor = EmptyCursor.INSTANCE) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertThat(cursor.nextSeries()).isFalse();
            assertThatThrownBy(() -> cursor.getSeriesId()).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> cursor.getSeriesData()).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> cursor.getSeriesMetaData()).isInstanceOf(IllegalStateException.class);
            assertInputNotNull(cursor);
        }

        try (EmptyCursor cursor = EmptyCursor.INSTANCE.filter(o -> false)) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertThat(cursor.nextSeries()).isFalse();
        }

        try (EmptyCursor cursor = EmptyCursor.INSTANCE.filter(o -> true)) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertThat(cursor.nextSeries()).isFalse();
        }

        try (EmptyCursor cursor = EmptyCursor.INSTANCE.transform(identity())) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertThat(cursor.nextSeries()).isFalse();
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testSingletonCursor() {
        Supplier<SingletonCursor<String>> example = () -> new SingletonCursor<>(someKey, someData, someMeta);

        try (SingletonCursor<String> cursor = example.get()) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertNextSeries(cursor, someKey, someData, someMeta);
            assertThat(cursor.nextSeries()).isFalse();
            assertInputNotNull(cursor);
        }

        try (SingletonCursor<String> cursor = example.get().filter(o -> false)) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertThat(cursor.nextSeries()).isFalse();
        }

        try (SingletonCursor<String> cursor = example.get().filter(o -> true)) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertThat(cursor.nextSeries()).isTrue();
        }

        try (SingletonCursor<String> cursor = example.get().transform(String::toUpperCase)) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertNextSeries(cursor, "HELLO", someData, someMeta);
            assertThat(cursor.nextSeries()).isFalse();
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testIteratingCursor() {
        Supplier<IteratingCursor<String, String>> example = () -> new IteratingCursor<>(forArray("hello", "world"), goodIdFunc, goodDataFunc, goodMetaFunc);

        try (IteratingCursor<String, String> cursor = example.get()) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertNextSeries(cursor, "HELLO", someData, someMeta);
            assertNextSeries(cursor, "WORLD", someData, someMeta);
            assertThat(cursor.nextSeries()).isFalse();
            assertInputNotNull(cursor);
        }

        try (IteratingCursor<String, String> cursor = example.get().filter(o -> false)) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertThat(cursor.nextSeries()).isFalse();
            assertInputNotNull(cursor);
        }

        try (IteratingCursor<String, String> cursor = example.get().filter(o -> o.startsWith("HE"))) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertNextSeries(cursor, "HELLO", someData, someMeta);
            assertThat(cursor.nextSeries()).isFalse();
            assertInputNotNull(cursor);
        }

        try (IteratingCursor<String, String> cursor = example.get().transform(String::toLowerCase)) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertNextSeries(cursor, "hello", someData, someMeta);
            assertNextSeries(cursor, "world", someData, someMeta);
            assertThat(cursor.nextSeries()).isFalse();
            assertInputNotNull(cursor);
        }

        try (IteratingCursor<String, String> cursor = example.get().filter(o -> o.startsWith("HE")).transform(String::toLowerCase)) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertNextSeries(cursor, "hello", someData, someMeta);
            assertThat(cursor.nextSeries()).isFalse();
        }

        try (IteratingCursor<String, String> cursor = example.get().transform(String::toLowerCase).filter(o -> o.startsWith("he"))) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertNextSeries(cursor, "hello", someData, someMeta);
            assertThat(cursor.nextSeries()).isFalse();
        }

        assertThatThrownBy(() -> readAllAndClose(new IteratingCursor<>(singletonIterator(someKey), badIdFunc, goodDataFunc, goodMetaFunc)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("id");
        assertThatThrownBy(() -> readAllAndClose(new IteratingCursor<>(singletonIterator(someKey), goodIdFunc, badDataFunc, goodMetaFunc)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("data");
        assertThatThrownBy(() -> readAllAndClose(new IteratingCursor<>(singletonIterator(someKey), goodIdFunc, goodDataFunc, badMetaFunc)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("meta");
    }

    @Test
    @SuppressWarnings("null")
    public void testTransformingCursor() throws IOException {
        Supplier<TsCursor<String>> delegateFactory = () -> TsCursor.from(forArray("hello", "world"));

        assertThatThrownBy(() -> delegateFactory.get().transform(null)).isInstanceOf(NullPointerException.class);

        try (TransformingCursor<String, String> cursor = new TransformingCursor<>(delegateFactory.get(), goodIdFunc)) {
            List<String> ids = new ArrayList<>();
            forEachId(cursor, ids::add);
            assertThat(ids).containsExactly("HELLO", "WORLD");
        }

        assertThatThrownBy(() -> readAllAndClose(new TransformingCursor<>(delegateFactory.get(), badIdFunc)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("id");
    }

    @Test
    @SuppressWarnings("null")
    public void testFilteringCursor() throws IOException {
        Supplier<TsCursor<String>> delegateFactory = () -> TsCursor.from(forArray("hello", "world"));

        assertThatThrownBy(() -> delegateFactory.get().filter(null)).isInstanceOf(NullPointerException.class);

        try (FilteringCursor<String> cursor = new FilteringCursor<>(delegateFactory.get(), o -> o.startsWith("w"))) {
            List<String> ids = new ArrayList<>();
            forEachId(cursor, ids::add);
            assertThat(ids).containsExactly("world");
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testMetaDataCursor() throws IOException {
        Supplier<TsCursor<String>> delegateFactory = () -> TsCursor.from(forArray("hello", "world"));

        assertThatThrownBy(() -> delegateFactory.get().withMetaData(null)).isInstanceOf(NullPointerException.class);

        try (WithMetaDataCursor<String> cursor = new WithMetaDataCursor<>(delegateFactory.get(), Collections.singletonMap("key", "value"))) {
            assertThat(cursor.getMetaData()).containsExactly(Maps.immutableEntry("key", "value"));
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testOnCloseCursor() throws IOException {
        Supplier<TsCursor<String>> delegateFactory = () -> TsCursor.from(forArray("hello", "world"));

        assertThatThrownBy(() -> delegateFactory.get().onClose(null)).isInstanceOf(NullPointerException.class);

        ResourceWatcher<?> watcher = ResourceWatcher.usingId();
        try (OnCloseCursor<?> cursor = new OnCloseCursor<>(delegateFactory.get(), watcher.watchAsCloseable("test"))) {
            assertThat(watcher.isLeakingResources()).isTrue();
        }
        assertThat(watcher.isLeakingResources()).isFalse();

        assertThatThrownBy(() -> TsCursor.empty().onClose(asCloseable(FirstIO::new)).onClose(asCloseable(SecondIO::new)).close())
                .isInstanceOf(FirstIO.class)
                .satisfies(o -> {
                    assertThat(o.getSuppressed())
                            .hasSize(1)
                            .hasAtLeastOneElementOfType(SecondIO.class);
                });
    }

    private static <ID> void assertNextSeries(InMemoryCursor<ID> cursor, ID id, OptionalTsData data, Map<String, String> meta) {
        assertThat(cursor.nextSeries()).isTrue();
        assertThat(cursor.getSeriesId()).isEqualTo(id);
        assertThat(cursor.getSeriesData()).isEqualTo(data);
        assertThat(cursor.getSeriesMetaData()).isEqualTo(meta);
    }

    @SuppressWarnings("null")
    private static void assertInputNotNull(TsCursor<?> cursor) {
        assertThatThrownBy(() -> cursor.filter(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> cursor.onClose(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> cursor.transform(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> cursor.withMetaData(null)).isInstanceOf(NullPointerException.class);
    }
}
