/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class Arrays2Test {

    static final double NO_DELTA = 0;

    @Test
    public void testReverse() {
        double[] d1 = new double[]{1, 2, 3};
        Arrays2.reverse(d1);
        assertArrayEquals(new double[]{3, 2, 1}, d1, NO_DELTA);

        double[] d2 = new double[]{1, 2, 3, 4};
        Arrays2.reverse(d2);
        assertArrayEquals(new double[]{4, 3, 2, 1}, d2, NO_DELTA);

        double[] d3 = new double[]{};
        Arrays2.reverse(d3);
        assertArrayEquals(new double[]{}, d3, NO_DELTA);
    }

    @Test
    public void testConcatDoubles() {
        double[] d1 = {1, 2, 3};
        double[] d2 = {4, 5};
        double[] d3 = {6, 7};
        assertArrayEquals(new double[]{1, 2, 3, 4, 5, 6, 7}, Arrays2.concat(d1, d2, d3), NO_DELTA);
        assertArrayEquals(new double[]{}, Arrays2.concat(), NO_DELTA);
        assertArrayEquals(new double[]{4, 5}, Arrays2.concat(d2), NO_DELTA);
        assertNotSame(new double[]{4, 5}, Arrays2.concat(d2));
    }

    @Test
    public void testShift() {
        double[] t1 = {1, 2, 3, 4, 5};
        double[] r1 = {1, 1, 2, 3, 4};
        Arrays2.shift(t1, 1);
        assertArrayEquals(r1, t1, NO_DELTA);

        double[] t2 = {1, 2, 3, 4, 5};
        double[] r2 = {1, 2, 1, 2, 3};
        Arrays2.shift(t2, 2);
        assertArrayEquals(r2, t2, NO_DELTA);

        double[] t3 = {1, 2, 3, 4, 5};
        double[] r3 = {2, 3, 4, 5, 5};
        Arrays2.shift(t3, -1);
        assertArrayEquals(r3, t3, NO_DELTA);

        double[] t4 = {1, 2, 3, 4, 5};
        double[] r4 = {3, 4, 5, 4, 5};
        Arrays2.shift(t4, -2);
        assertArrayEquals(r4, t4, NO_DELTA);

        double[] t5 = {1, 2, 3, 4, 5};
        double[] r5 = {1, 2, 3, 4, 5};
        Arrays2.shift(t5, 0);
        assertArrayEquals(r5, t5, NO_DELTA);
    }

    @Test
    public void testShiftRange() {
        double[] t1 = {99, 88, 1, 2, 3, 4, 5, 77, 66};
        double[] r1 = {99, 88, 1, 1, 2, 3, 4, 77, 66};
        Arrays2.shift(t1, 1, 2, 7);
        assertArrayEquals(r1, t1, NO_DELTA);

        double[] t2 = {99, 88, 1, 2, 3, 4, 5, 77, 66};
        double[] r2 = {99, 88, 1, 2, 1, 2, 3, 77, 66};
        Arrays2.shift(t2, 2, 2, 7);
        assertArrayEquals(r2, t2, NO_DELTA);

        double[] t3 = {99, 88, 1, 2, 3, 4, 5, 77, 66};
        double[] r3 = {99, 88, 2, 3, 4, 5, 5, 77, 66};
        Arrays2.shift(t3, -1, 2, 7);
        assertArrayEquals(r3, t3, NO_DELTA);

        double[] t4 = {99, 88, 1, 2, 3, 4, 5, 77, 66};
        double[] r4 = {99, 88, 3, 4, 5, 4, 5, 77, 66};
        Arrays2.shift(t4, -2, 2, 7);
        assertArrayEquals(r4, t4, NO_DELTA);

        double[] t5 = {99, 88, 1, 2, 3, 4, 5, 77, 66};
        double[] r5 = {99, 88, 1, 2, 3, 4, 5, 77, 66};
        Arrays2.shift(t5, 0, 2, 7);
        assertArrayEquals(r5, t5, NO_DELTA);

        double[] t6 = {1, 2, 3, 4};
        double[] r6 = {1, 1, 2, 3};
        Arrays2.shift(t6, 1, 0, t6.length);
        assertArrayEquals(r6, t6, NO_DELTA);
    }

    @Test
    public void testIsArray() {
        assertTrue(Arrays2.isArray(new long[0]));
        assertTrue(Arrays2.isArray(new int[0]));
        assertTrue(Arrays2.isArray(new short[0]));
        assertTrue(Arrays2.isArray(new char[0]));
        assertTrue(Arrays2.isArray(new byte[0]));
        assertTrue(Arrays2.isArray(new boolean[0]));
        assertTrue(Arrays2.isArray(new double[0]));
        assertTrue(Arrays2.isArray(new float[0]));
        assertTrue(Arrays2.isArray(new Object[0]));
        assertFalse(Arrays2.isArray(123));
        assertFalse(Arrays2.isArray("123"));
    }

    @Test
    public void testArrayEquals() {
        assertTrue(Arrays2.arrayEquals(new int[]{1, 2, 3}, new int[]{1, 2, 3}));
        assertFalse(Arrays2.arrayEquals(new int[]{1, 2, 3}, new int[]{1, 2}));
    }
}
