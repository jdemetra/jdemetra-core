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

import ec.tss.tsproviders.utils.OptionalTsData;
import static com.google.common.collect.Iterators.forArray;
import static com.google.common.collect.Iterators.singletonIterator;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TsCursorTest {

    private final OptionalTsData someData = OptionalTsData.present(TsData.random(TsFrequency.Monthly, 1));
    private final Optional<MetaData> someMeta = Optional.of(new MetaData());

    @Test
    public void testNoOp() throws IOException {
        TsCursor<?> cursor = TsCursor.noOp();
        assertThat(cursor.nextSeries()).isFalse();
    }

    @Test
    @SuppressWarnings("null")
    public void testSingleton() throws IOException {
        TsCursor<String> cursor = TsCursor.singleton("hello", someData, someMeta);
        assertThat(cursor.nextSeries()).isTrue();
        assertThat(cursor.getId()).isEqualTo("hello");
        assertThat(cursor.getData()).isEqualTo(someData);
        assertThat(cursor.getMetaData()).isEqualTo(someMeta);
        assertThat(cursor.nextSeries()).isFalse();

        assertThatThrownBy(() -> TsCursor.singleton(null, someData, someMeta)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsCursor.singleton("hello", null, someMeta)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsCursor.singleton("hello", someData, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testIterator() throws IOException {
        TsCursor<String> cursor;

        cursor = TsCursor.from(Collections.<String>emptyIterator(), String::toUpperCase);
        assertThat(cursor.nextSeries()).isFalse();

        cursor = TsCursor.from(singletonIterator("hello"), String::toUpperCase, o -> someData, o -> someMeta);
        assertThat(cursor.nextSeries()).isTrue();
        assertThat(cursor.getId()).isEqualTo("HELLO");
        assertThat(cursor.getData()).isEqualTo(someData);
        assertThat(cursor.getMetaData()).isEqualTo(someMeta);
        assertThat(cursor.nextSeries()).isFalse();

        cursor = TsCursor.from(forArray("hello", "world"), String::toUpperCase);
        List<String> ids = new ArrayList<>();
        while (cursor.nextSeries()) {
            ids.add(cursor.getId());
        }
        assertThat(ids).containsExactly("HELLO", "WORLD");

        assertThatThrownBy(() -> TsCursor.<String, String>from(null, String::toUpperCase, o -> someData, o -> someMeta)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsCursor.<String, String>from(singletonIterator("hello"), null, o -> someData, o -> someMeta)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsCursor.<String, String>from(singletonIterator("hello"), String::toUpperCase, null, o -> someMeta)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsCursor.<String, String>from(singletonIterator("hello"), String::toUpperCase, o -> someData, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testTransform() throws IOException {
        TsCursor<String> cursor;

        cursor = TsCursor.from(Collections.<String>emptyIterator(), o -> o).transform(String::toUpperCase);
        assertThat(cursor.nextSeries()).isFalse();

        cursor = TsCursor.from(forArray("hello", "world"), o -> o).transform(String::toUpperCase);
        List<String> ids = new ArrayList<>();
        while (cursor.nextSeries()) {
            ids.add(cursor.getId());
        }
        assertThat(ids).containsExactly("HELLO", "WORLD");

        assertThatThrownBy(() -> TsCursor.from(forArray("hello", "world"), o -> o).transform(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testFilter() throws IOException {
        TsCursor<String> cursor;

        cursor = TsCursor.from(Collections.<String>emptyIterator(), o -> o).filter(o -> o.contains("world"));
        assertThat(cursor.nextSeries()).isFalse();

        cursor = TsCursor.from(forArray("hello", "world"), o -> o).filter(o -> o.contains("world"));
        List<String> ids = new ArrayList<>();
        while (cursor.nextSeries()) {
            ids.add(cursor.getId());
        }
        assertThat(ids).containsExactly("world");

        assertThatThrownBy(() -> TsCursor.from(forArray("hello", "world"), o -> o).filter(null)).isInstanceOf(NullPointerException.class);
    }
}
