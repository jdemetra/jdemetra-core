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
package ec.tss.tsproviders.utils;

import java.util.zip.Deflater;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ByteArrayConverterTest {

    @Test
    public void testDefault() {
        ByteArrayConverter converter = ByteArrayConverter.getDefault();

        double[] input0 = {};
        Assert.assertArrayEquals(input0, converter.toDoubleArray(converter.fromDoubleArray(input0)), 0);
        Assert.assertNotSame(input0, converter.toDoubleArray(converter.fromDoubleArray(input0)));

        double[] input1 = {0.0, 1.1, 2.2, 3.3};
        Assert.assertArrayEquals(input1, converter.toDoubleArray(converter.fromDoubleArray(input1)), 0);
        Assert.assertNotSame(input1, converter.toDoubleArray(converter.fromDoubleArray(input1)));

        double[] input2 = {Double.NaN, Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_NORMAL,
            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};
        Assert.assertArrayEquals(input2, converter.toDoubleArray(converter.fromDoubleArray(input2)), 0);
        Assert.assertNotSame(input2, converter.toDoubleArray(converter.fromDoubleArray(input2)));
    }

    @Test
    public void testDeflate() {
        ByteArrayConverter converter = ByteArrayConverter.getDeflate(Deflater.BEST_SPEED, true);

        double[] input0 = {};
        Assert.assertArrayEquals(input0, converter.toDoubleArray(converter.fromDoubleArray(input0)), 0);
        Assert.assertNotSame(input0, converter.toDoubleArray(converter.fromDoubleArray(input0)));

        double[] input1 = {0.0, 1.1, 2.2, 3.3};
        Assert.assertArrayEquals(input1, converter.toDoubleArray(converter.fromDoubleArray(input1)), 0);
        Assert.assertNotSame(input1, converter.toDoubleArray(converter.fromDoubleArray(input1)));

        double[] input2 = {Double.NaN, Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_NORMAL,
            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};
        Assert.assertArrayEquals(input2, converter.toDoubleArray(converter.fromDoubleArray(input2)), 0);
        Assert.assertNotSame(input2, converter.toDoubleArray(converter.fromDoubleArray(input2)));
    }

    @Test
    @SuppressWarnings("null")
    public void testInstance() {
        Assertions.assertThatThrownBy(() -> ByteArrayConverter.setInstance(null))
                .isInstanceOf(NullPointerException.class);

        ByteArrayConverter c1 = new ByteArrayConverter() {
            @Override
            public byte[] fromDoubleArray(double[] input) {
                throw new RuntimeException();
            }
        };
        Assertions.assertThatThrownBy(() -> ByteArrayConverter.setInstance(c1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasCauseInstanceOf(RuntimeException.class);

        ByteArrayConverter c2 = new ByteArrayConverter() {
            @Override
            public double[] toDoubleArray(byte[] input) {
                return new double[0];
            }
        };
        Assertions.assertThatThrownBy(() -> ByteArrayConverter.setInstance(c2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasNoCause();
    }
}
