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
package demetra.data;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class DoubleListTest {

    @Test
    public void testStream() {
        DoubleList list = new DoubleList();
        assertThat(list.stream().toArray()).isEmpty();
        list.add(10d);
        list.add(5d);
        assertThat(list.stream().toArray()).containsExactly(10d, 5d);
        list.clear();
        assertThat(list.stream().toArray()).isEmpty();
    }

    @Test
    public void testReplaceAll() {
        DoubleList list = new DoubleList();
        list.replaceAll(o -> o + 1);
        assertThat(list.toArray()).isEmpty();
        list.add(1d);
        list.add(2d);
        list.replaceAll(o -> o + 1);
        assertThat(list.toArray()).containsExactly(2d, 3d);
    }

    @Test
    public void testSort() {
        DoubleList list = new DoubleList();
        list.sort();
        assertThat(list.toArray()).isEmpty();
        list.add(10d);
        list.add(5d);
        list.sort();
        assertThat(list.toArray()).containsExactly(5d, 10d);
    }

    @Test
    public void testRemoveIf() {
        DoubleList list = new DoubleList();
        list.removeIf(o -> o < 7);
        assertThat(list.toArray()).isEmpty();
        list.add(10d);
        list.add(5d);
        list.removeIf(o -> o < 7);
        assertThat(list.toArray()).containsExactly(10d);
    }

    @Test
    public void testForEach() {
        DoubleList list = new DoubleList();
        DoubleList other = new DoubleList();
        list.forEach(other::add);
        assertThat(other.toArray()).isEmpty();
        list.add(10);
        list.add(5);
        list.forEach(other::add);
        assertThat(list.toArray()).containsExactly(10, 5);
    }
}
