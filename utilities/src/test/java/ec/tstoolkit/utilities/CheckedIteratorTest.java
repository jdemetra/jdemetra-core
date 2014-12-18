/*
 * Copyright 2013 National Bank of Belgium
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

import com.google.common.base.Enums;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class CheckedIteratorTest {

    private static Path getResource(String name) throws URISyntaxException {
        return Paths.get(CheckedIteratorTest.class.getResource(name).toURI());
    }

    private static <X> CheckedIterator<X, RuntimeException> create(X... array) {
        return CheckedIterator.fromIterator(Iterators.forArray(array));
    }

    @Test
    public void testNextWithDefault() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create();
        assertEquals("hello", iterator.next("hello"));

        iterator = create("one");
        assertEquals("one", iterator.next("hello"));
        assertEquals("hello", iterator.next("hello"));

        iterator = create("one");
        iterator.next();
        assertEquals("hello", iterator.next("hello"));
    }

    @Test
    public void testCount() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create();
        assertEquals(0, iterator.count());

        iterator = create("one");
        assertEquals(1, iterator.count());

        iterator = create("one", "two");
        iterator.next();
        assertEquals(1, iterator.count());
    }

    @Test
    public void testCopyInto() {
        List<String> tmp = new ArrayList<>();
        CheckedIterator<String, RuntimeException> iterator;

        tmp.clear();
        iterator = create("one", "two", "three");
        iterator.copyInto(tmp);
        assertArrayEquals(new String[]{"one", "two", "three"}, tmp.toArray());

        tmp.clear();
        iterator = create("one", "two", "three");
        iterator.next();
        iterator.copyInto(tmp);
        assertArrayEquals(new String[]{"two", "three"}, tmp.toArray());

        tmp.clear();
        iterator = create();
        iterator.copyInto(tmp);
        assertArrayEquals(new String[]{}, tmp.toArray());
    }

    @Test
    public void testToList() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one", "two", "three");
        assertArrayEquals(new String[]{"one", "two", "three"}, iterator.toList().toArray());

        iterator = create("one", "two", "three");
        iterator.next();
        assertArrayEquals(new String[]{"two", "three"}, iterator.toList().toArray());

        iterator = create();
        assertArrayEquals(new String[]{}, iterator.toList().toArray());
    }

    @Test
    public void testToArray() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one", "two", "three");
        assertArrayEquals(new String[]{"one", "two", "three"}, iterator.toArray(String.class));

        iterator = create("one", "two", "three");
        iterator.next();
        assertArrayEquals(new String[]{"two", "three"}, iterator.toArray(String.class));

        iterator = create();
        assertArrayEquals(new String[]{}, iterator.toArray(String.class));
    }

    @Test
    public void testToMap1() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one", "two");
        assertEquals(ImmutableMap.of("one", "hello", "two", "hello"), iterator.toMap(Functions.constant("hello")));
    }

    @Test
    public void testToMap2() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("READ", "WRITE");
        assertEquals(ImmutableMap.of(AccessMode.READ, "hello", AccessMode.WRITE, "hello"), iterator.toMap(Enums.stringConverter(AccessMode.class), Functions.constant("hello")));
    }

    @Test
    public void testGetLast() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one", "two");
        assertEquals("two", iterator.getLast());

        iterator = create("one", "two");
        iterator.next();
        assertEquals("two", iterator.getLast());
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetLastNoSuchElementException() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one");
        iterator.next();
        iterator.getLast();
    }

    @Test
    public void testGetLastWithDefault() {
        CheckedIterator<String, RuntimeException> iterator;
        iterator = create("one", "two");
        iterator.next();
        assertEquals("two", iterator.getLast("hello"));

        iterator = create("one", "two");
        iterator.next();
        iterator.next();
        assertEquals("hello", iterator.getLast("hello"));
    }

    @Test
    public void testAdvance() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one");
        assertEquals(1, iterator.advance(1));

        iterator = create("one", "two");
        assertEquals(2, iterator.advance(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdvanceIllegalArgumentException() {
        create("one", "two").advance(-1);
    }

    @Test
    public void testGet() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one");
        assertEquals("one", iterator.get(0));

        iterator = create("one", "two");
        assertEquals("two", iterator.get(1));

        iterator = create("one", "two");
        assertEquals("one", iterator.get(0));
        assertEquals("two", iterator.get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIllegalArgumentException() {
        create("one", "two").get(-1);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetNoSuchElementException() {
        create("one", "two").get(3);
    }

    @Test
    public void testGetWithDefault() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one");
        assertEquals("one", iterator.get(0, "hello"));

        iterator = create("one", "two");
        assertEquals("two", iterator.get(1, "hello"));

        iterator = create("one", "two");
        assertEquals("one", iterator.get(0, "hello"));
        assertEquals("two", iterator.get(0, "hello"));

        iterator = create("one", "two");
        assertEquals("hello", iterator.get(3, "hello"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithDefaultIllegalArgumentException() {
        create("one", "two").get(-1, "hello");
    }

    @Test
    public void testAll() {
        assertTrue(create("one", "two").all(Predicates.notNull()));
        assertFalse(create("one", "two").all(Predicates.equalTo("one")));
    }

    @Test
    public void testAny() {
        assertFalse(create("one", "two").any(Predicates.equalTo("hello")));
        assertTrue(create("one", "two").any(Predicates.equalTo("one")));
    }

    @Test
    public void testIndexOf() {
        assertEquals(-1, create("one", "two").indexOf(Predicates.equalTo("hello")));
        assertEquals(0, create("one", "two").indexOf(Predicates.equalTo("one")));
        assertEquals(1, create("one", "two").indexOf(Predicates.equalTo("two")));
    }

    @Test
    public void testContains() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one");
        assertTrue(iterator.contains("one"));

        iterator = create("one");
        assertFalse(iterator.contains("two"));

        iterator = create("one");
        iterator.next();
        assertFalse(iterator.contains("one"));
    }

    @Test
    public void testElementsEqual() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one", "two");
        assertTrue(iterator.elementsEqual(create("one", "two")));

        iterator = create("one", "two");
        assertFalse(iterator.elementsEqual(create("one", "two", "three")));

        iterator = create("one", "two");
        iterator.next();
        assertFalse(iterator.elementsEqual(create("one", "two")));

        iterator = create();
        CheckedIterator<String, RuntimeException> tmp = create();
        assertTrue(iterator.elementsEqual(tmp));
    }

    @Test
    public void testFind() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one", "two");
        assertEquals("one", iterator.find(Predicates.notNull()));

        iterator = create("one", "two");
        iterator.next();
        assertEquals("two", iterator.find(Predicates.notNull()));
    }

    @Test(expected = NoSuchElementException.class)
    public void testFindNoSuchElementException() {
        create("one", "two").find(Predicates.equalTo("three"));
    }

    @Test
    public void testFindWithDefault() {
        assertEquals("hello", create("one", "two").find(Predicates.equalTo("three"), "hello"));
    }

    @Test
    public void testFrequency() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one", "two");
        assertEquals(0, iterator.frequency("three"));

        iterator = create("one", "two");
        assertEquals(1, iterator.frequency("one"));

        iterator = create("one", "two", "one");
        assertEquals(2, iterator.frequency("one"));
    }

    @Test
    public void testFilter() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one", "two", "one").filter(Predicates.equalTo("one"));
        assertArrayEquals(new String[]{"one", "one"}, iterator.toList().toArray());

        iterator = create("one", "two", "one").filter(Predicates.alwaysFalse());
        assertArrayEquals(new String[]{}, iterator.toList().toArray());
    }

    @Test
    public void testFilterByClass() {
        CheckedIterator<?, RuntimeException> iterator;

        iterator = create(30, "two", "one").filter(String.class);
        assertArrayEquals(new String[]{"two", "one"}, iterator.toList().toArray());
    }

    @Test
    public void testSkip() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one");
        assertArrayEquals(new String[]{}, iterator.skip(1).toArray(String.class));

        iterator = create("one", "two");
        assertArrayEquals(new String[]{"two"}, iterator.skip(1).toArray(String.class));

        iterator = create("one", "two");
        assertArrayEquals(new String[]{}, iterator.skip(3).toArray(String.class));
    }

    @Test
    public void testLimit() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one", "two", "one").limit(2);
        assertEquals(2, iterator.count());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLimitIllegalArgumentException() {
        create("one", "two", "one").limit(-1);
    }

    @Test
    public void testTransform() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one", "two", "three").transform(Functions.constant("hello"));
        assertArrayEquals(new String[]{"hello", "hello", "hello"}, iterator.toList().toArray());
    }

    @Test
    public void testConcat() {
        CheckedIterator<String, RuntimeException> iterator;

        iterator = create("one", "two").concat(create("three"));
        assertArrayEquals(new String[]{"one", "two", "three"}, iterator.toList().toArray());
    }

    // FACTORIES
    @Test
    public void testFromBufferedReader() throws IOException, URISyntaxException {
        Path file = getResource("Values.txt");
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            int count = CheckedIterator.fromBufferedReader(reader)
                    .transform(Ints.stringConverter())
                    .filter(Range.atMost(100))
                    .count();
            assertEquals(3, count);
        }
    }
}
