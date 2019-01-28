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

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class List2Test {

    @Test
    public void testOf() {
        assertThatNullPointerException().isThrownBy(() -> List2.of((Object[]) null));

        String[] empty = {};
        assertThat(List2.of(empty)).isEmpty();
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> List2.of(empty).add("other"));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> List2.of(empty).set(0, "other"));

        String[] single = {"hello"};
        assertThat(List2.of(single)).containsExactly("hello");
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> List2.of(single).add("other"));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> List2.of(single).set(0, "other"));
        assertThatNullPointerException().isThrownBy(() -> List2.of(new Object[]{null}));

        String[] multi = {"hello", "world"};
        assertThat(List2.of(multi)).containsExactly("hello", "world");
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> List2.of(multi).add("other"));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> List2.of(multi).set(0, "other"));
        assertThatNullPointerException().isThrownBy(() -> List2.of(new Object[]{null, null}));
    }

    @Test
    @SuppressWarnings("null")
    public void testCopyOf() {
        assertThatNullPointerException().isThrownBy(() -> List2.copyOf(null));

        List<String> empty = Arrays.asList();
        assertThat(List2.copyOf(empty)).isEmpty();
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> List2.copyOf(empty).add("other"));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> List2.copyOf(empty).set(0, "other"));

        List<String> single = Arrays.asList("hello");
        assertThat(List2.copyOf(single)).containsExactly("hello");
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> List2.copyOf(single).add("other"));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> List2.copyOf(single).set(0, "other"));
        assertThatNullPointerException().isThrownBy(() -> List2.copyOf(Arrays.asList((Object) null)));

        List<String> multi = Arrays.asList("hello", "world");
        assertThat(List2.copyOf(multi)).containsExactly("hello", "world");
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> List2.copyOf(multi).add("other"));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> List2.copyOf(multi).set(0, "other"));
        assertThatNullPointerException().isThrownBy(() -> List2.copyOf(Arrays.asList(null, null)));
    }

    @Test
    public void testToUnmodifiableList() {
        Supplier<Stream<String>> empty = () -> Stream.of();
        assertThat(empty.get().collect(List2.toUnmodifiableList())).isEmpty();
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> empty.get().collect(List2.toUnmodifiableList()).add("other"));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> empty.get().collect(List2.toUnmodifiableList()).set(0, "other"));

        Supplier<Stream<String>> single = () -> Stream.of("hello");
        assertThat(single.get().collect(List2.toUnmodifiableList())).containsExactly("hello");
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> single.get().collect(List2.toUnmodifiableList()).add("other"));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> single.get().collect(List2.toUnmodifiableList()).set(0, "other"));
        assertThatNullPointerException().isThrownBy(() -> Stream.of((Object) null).collect(List2.toUnmodifiableList()));

        Supplier<Stream<String>> multi = () -> Stream.of("hello", "world");
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> multi.get().collect(List2.toUnmodifiableList()).add("other"));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> multi.get().collect(List2.toUnmodifiableList()).set(0, "other"));
        assertThatNullPointerException().isThrownBy(() -> Stream.of(null, null).collect(List2.toUnmodifiableList()));
    }
}
