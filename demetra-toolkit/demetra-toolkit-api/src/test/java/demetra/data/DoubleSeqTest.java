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

import demetra.design.Demo;
import java.util.stream.DoubleStream;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class DoubleSeqTest {

    @Demo
    public static void main(String[] args) {
        DoubleSeq values = DoubleSeq.copyOf(new double[]{3.14, 3, 5, 7});
        System.out.println(DoubleSeq.copyOf(values.stream().skip(1).map(o -> o * 2)));

        double[] tmp = values.toArray();
        tmp[2] = 123;
        System.out.println(DoubleSeq.of(tmp));

        System.out.println(DoubleSeq.copyOf(DoubleStream.concat(DoubleStream.of(777), values.stream())));

        double[] buffer = new double[values.length() + 1];
        values.copyTo(buffer, 1);
        buffer[0] = 777;
        System.out.println(DoubleSeq.of(buffer));
    }

    @Test
    @SuppressWarnings("null")
    public void testFactories() {
        assertThat(DoubleSeq.of(new double[]{}).length()).isEqualTo(0);
        assertThat(DoubleSeq.of(new double[]{3.14}).length()).isEqualTo(1);
        assertThatThrownBy(() -> DoubleSeq.of(null)).isInstanceOf(NullPointerException.class);

        assertThat(DoubleSeq.empty().length()).isEqualTo(0);
        assertThat(DoubleSeq.of(3.14).length()).isEqualTo(1);
        assertThatThrownBy(() -> DoubleSeq.copyOf((double[]) null)).isInstanceOf(NullPointerException.class);

        assertThat(DoubleSeq.copyOf(DoubleStream.of()).length()).isEqualTo(0);
        assertThat(DoubleSeq.copyOf(DoubleStream.of(3.14)).length()).isEqualTo(1);
        assertThatThrownBy(() -> DoubleSeq.copyOf((DoubleStream) null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testEquals() {
        assertThat(DoubleSeq.empty()).isEqualTo(DoubleSeq.empty()).isNotEqualTo(PI);
        assertThat(PI).isEqualTo(PI).isNotEqualTo(DoubleSeq.empty());
    }

    @Test
    public void testHashcode() {
        assertThat(DoubleSeq.empty().hashCode()).isEqualTo(DoubleSeq.empty().hashCode()).isNotEqualTo(PI.hashCode());
        assertThat(PI.hashCode()).isEqualTo(PI.hashCode()).isNotEqualTo(DoubleSeq.empty().hashCode());
    }

    @Test
    public void testLength() {
        assertThat(DoubleSeq.empty().length()).isEqualTo(0);
        assertThat(PI.length()).isEqualTo(1);
    }

    @Test
    public void testDoubleAt() {
        assertThat(PI.get(0)).isEqualTo(Math.PI);
        assertThatThrownBy(() -> DoubleSeq.empty().get(0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> DoubleSeq.empty().get(-1)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testToArray() {
        assertThat(DoubleSeq.empty().toArray()).isEmpty();
        assertThat(PI.toArray())
                .containsExactly(Math.PI)
                .isNotSameAs(PI.toArray());
    }

    @Test
    public void testCopyTo() {
        assertThat(copy(PI, new double[]{666}, 0)).containsExactly(Math.PI);
        assertThat(copy(PI, new double[]{666, 777}, 1)).containsExactly(666, Math.PI);
    }

    private static final DoubleSeq PI = DoubleSeq.of(Math.PI);

    private static double[] copy(DoubleSeq o, double[] buffer, int offset) {
        o.copyTo(buffer, offset);
        return buffer;
    }
}
