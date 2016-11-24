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

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class IntListTest {

    @Test
    public void testStream() {
        IntList list = new IntList();
        assertThat(list.stream().toArray()).isEmpty();
        list.add(10);
        list.add(5);
        assertThat(list.stream().toArray()).containsExactly(10, 5);
        list.clear();
        assertThat(list.stream().toArray()).isEmpty();
    }

    @Test
    public void testReplaceAll() {
        IntList list = new IntList();
        list.replaceAll(o -> o + 1);
        assertThat(list.toArray()).isEmpty();
        list.add(1);
        list.add(2);
        list.replaceAll(o -> o + 1);
        assertThat(list.toArray()).containsExactly(2, 3);
    }

    @Test
    public void testSort() {
        IntList list = new IntList();
        list.sort();
        assertThat(list.toArray()).isEmpty();
        list.add(10);
        list.add(5);
        list.sort();
        assertThat(list.toArray()).containsExactly(5, 10);
    }

    @Test
    public void testRemoveIf() {
        IntList list = new IntList();
        list.removeIf(o -> o < 7);
        assertThat(list.toArray()).isEmpty();
        list.add(10);
        list.add(5);
        list.removeIf(o -> o < 7);
        assertThat(list.toArray()).containsExactly(10);
    }

    @Test
    public void testForEach() {
        IntList list = new IntList();
        IntList other = new IntList();
        list.forEach(other::add);
        assertThat(other.toArray()).isEmpty();
        list.add(10);
        list.add(5);
        list.forEach(other::add);
        assertThat(list.toArray()).containsExactly(10, 5);
    }
}
