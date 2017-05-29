/*
 * Copyright 2017 National Bank of Belgium
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

import static demetra.data.DoubleValues.EMPTY;
import internal.Demo;
import java.util.stream.DoubleStream;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class DoubleValuesTest {

    @Demo
    public static void main(String[] args) {
        DoubleValues values = DoubleValues.ofInternal(3.14, 3, 5, 7);
        System.out.println(DoubleValues.of(values.stream().skip(1).map(o -> o * 2)));

        double[] tmp = values.toArray();
        tmp[2] = 123;
        System.out.println(DoubleValues.ofInternal(tmp));

        System.out.println(DoubleValues.of(DoubleStream.concat(DoubleStream.of(777), values.stream())));

        double[] buffer = new double[values.length() + 1];
        values.copyTo(buffer, 1);
        buffer[0] = 777;
        System.out.println(DoubleValues.ofInternal(buffer));
    }

    @Test
    @SuppressWarnings("null")
    public void testFactories() {
        assertThat(DoubleValues.ofInternal().length()).isEqualTo(0);
        assertThat(DoubleValues.ofInternal(3.14).length()).isEqualTo(1);
        assertThatThrownBy(() -> DoubleValues.ofInternal(null)).isInstanceOf(NullPointerException.class);

        assertThat(DoubleValues.of().length()).isEqualTo(0);
        assertThat(DoubleValues.of(3.14).length()).isEqualTo(1);
        assertThatThrownBy(() -> DoubleValues.of((double[]) null)).isInstanceOf(NullPointerException.class);

        assertThat(DoubleValues.of(DoubleStream.of()).length()).isEqualTo(0);
        assertThat(DoubleValues.of(DoubleStream.of(3.14)).length()).isEqualTo(1);
        assertThatThrownBy(() -> DoubleValues.of((DoubleStream) null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testEquals() {
        assertThat(EMPTY).isEqualTo(EMPTY).isNotEqualTo(PI);
        assertThat(PI).isEqualTo(PI).isNotEqualTo(EMPTY);
    }

    @Test
    public void testHashcode() {
        assertThat(EMPTY.hashCode()).isEqualTo(EMPTY.hashCode()).isNotEqualTo(PI.hashCode());
        assertThat(PI.hashCode()).isEqualTo(PI.hashCode()).isNotEqualTo(EMPTY.hashCode());
    }

    @Test
    public void testLength() {
        assertThat(EMPTY.length()).isEqualTo(0);
        assertThat(PI.length()).isEqualTo(1);
    }

    @Test
    public void testDoubleAt() {
        assertThat(PI.get(0)).isEqualTo(Math.PI);
        assertThatThrownBy(() -> EMPTY.get(0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> EMPTY.get(-1)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testToArray() {
        assertThat(EMPTY.toArray()).isEmpty();
        assertThat(PI.toArray())
                .containsExactly(Math.PI)
                .isNotSameAs(PI.toArray());
    }

    @Test
    public void testCopyTo() {
        assertThat(copy(PI, new double[]{666}, 0)).containsExactly(Math.PI);
        assertThat(copy(PI, new double[]{666, 777}, 1)).containsExactly(666, Math.PI);
    }

    private static final DoubleValues PI = DoubleValues.ofInternal(Math.PI);

    private static double[] copy(DoubleValues o, double[] buffer, int offset) {
        o.copyTo(buffer, offset);
        return buffer;
    }
}
