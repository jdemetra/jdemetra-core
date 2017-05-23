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
package demetra.tsprovider.cursor;

import static internal.tsprovider.cursor.InternalTsCursor.NOT_REQUESTED;
import static internal.tsprovider.cursor.InternalTsCursor.NO_DATA;
import static internal.tsprovider.cursor.InternalTsCursor.NO_META;
import java.io.IOException;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TsCursorTest {

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
    @SuppressWarnings("null")
    public void testCachingFactory() {
        ConcurrentMap<String, Object> cache = new ConcurrentHashMap<>();
        assertThatThrownBy(() -> TsCursor.withCache(null, "", o -> TsCursor.empty())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsCursor.withCache(cache, null, o -> TsCursor.empty())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsCursor.withCache(cache, "", null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testDefaultMethods() throws IOException {
        try (TsCursor cursor = TsCursor.empty()) {
            assertThatThrownBy(() -> cursor.filter(null)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> cursor.map(null)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> cursor.onClose(null)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> cursor.withMetaData(null)).isInstanceOf(NullPointerException.class);
        }
    }
}
