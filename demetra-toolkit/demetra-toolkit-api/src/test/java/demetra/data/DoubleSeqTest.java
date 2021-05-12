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

import nbbrd.design.Demo;
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
        DoubleSeq values = Doubles.of(new double[]{3.14, 3, 5, 7});
        System.out.println(Doubles.of(values.stream().skip(1).map(o -> o * 2)));

        double[] tmp = values.toArray();
        tmp[2] = 123;
        System.out.println(DoubleSeq.of(tmp));

        System.out.println(Doubles.of(DoubleStream.concat(DoubleStream.of(777), values.stream())));

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

        assertThat(Doubles.EMPTY.length()).isEqualTo(0);
        assertThat(Doubles.of(3.14).length()).isEqualTo(1);
        assertThatThrownBy(() -> Doubles.of((double[]) null)).isInstanceOf(NullPointerException.class);

        assertThat(Doubles.of(DoubleStream.of()).length()).isEqualTo(0);
        assertThat(Doubles.of(DoubleStream.of(3.14)).length()).isEqualTo(1);
        assertThatThrownBy(() -> Doubles.of((DoubleStream) null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testEquals() {
        assertThat(Doubles.EMPTY).isEqualTo(Doubles.EMPTY).isNotEqualTo(PI);
        assertThat(PI).isEqualTo(PI).isNotEqualTo(Doubles.EMPTY);
    }

    @Test
    public void testHashcode() {
        assertThat(Doubles.EMPTY.hashCode()).isEqualTo(Doubles.EMPTY.hashCode()).isNotEqualTo(PI.hashCode());
        assertThat(PI.hashCode()).isEqualTo(PI.hashCode()).isNotEqualTo(Doubles.EMPTY.hashCode());
    }

    @Test
    public void testLength() {
        assertThat(Doubles.EMPTY.length()).isEqualTo(0);
        assertThat(PI.length()).isEqualTo(1);
    }

    @Test
    public void testDoubleAt() {
        assertThat(PI.get(0)).isEqualTo(Math.PI);
        assertThatThrownBy(() -> Doubles.EMPTY.get(0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> Doubles.EMPTY.get(-1)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testToArray() {
        assertThat(Doubles.EMPTY.toArray()).isEmpty();
        assertThat(PI.toArray())
                .containsExactly(Math.PI)
                .isNotSameAs(PI.toArray());
    }

    @Test
    public void testCopyTo() {
        assertThat(copy(PI, new double[]{666}, 0)).containsExactly(Math.PI);
        assertThat(copy(PI, new double[]{666, 777}, 1)).containsExactly(666, Math.PI);
    }

    private static final Doubles PI = Doubles.of(Math.PI);

    private static double[] copy(DoubleSeq o, double[] buffer, int offset) {
        o.copyTo(buffer, offset);
        return buffer;
    }

    @Test
    public void testHasSameContentAs() {
        assertThat(PI.hasSameContentAs(Doubles.of(Math.PI))).isTrue();
        assertThat(Doubles.of(Double.NaN).hasSameContentAs(Doubles.of(Double.NaN))).isTrue();
    }

    @Test
    public void testCleanExtremities() {
        double[] a = new double[]{0, 1, 2, 3};
        DoubleSeq A = DoubleSeq.of(a);
        assertThat(A.cleanExtremities().length()).isEqualTo(4);

        double[] b = new double[]{Double.NaN, 1, Double.NaN, Double.NaN};
        DoubleSeq B = DoubleSeq.of(b);
        assertThat(B.cleanExtremities().length()).isEqualTo(1);

        double[] c = new double[]{Double.NaN, Double.NaN, Double.NaN};
        DoubleSeq C = DoubleSeq.of(c);
        assertThat(C.cleanExtremities().length()).isEqualTo(0);
    }

}
