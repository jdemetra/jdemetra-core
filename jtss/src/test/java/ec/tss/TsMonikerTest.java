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
package ec.tss;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Philippe Charles
 */
public class TsMonikerTest {

    @Test
    public void testEquals() {
        assertThat(new TsMoniker("ABC", "123"))
                .isEqualTo(new TsMoniker("ABC", "123"))
                .isNotEqualTo(new TsMoniker("ABC", "xxx"))
                .isNotEqualTo(new TsMoniker("xxx", "123"));

        TsMoniker m3 = new TsMoniker();
        assertThat(m3)
                .isEqualTo(m3)
                .isNotEqualTo(new TsMoniker());
    }

    @Test
    public void testHashcode() {
        assertThat(new TsMoniker("ABC", "123").hashCode())
                .isEqualTo(new TsMoniker("ABC", "123").hashCode())
                .isNotEqualTo(new TsMoniker("ABC", "xxx").hashCode())
                .isNotEqualTo(new TsMoniker("xxx", "123").hashCode());

        TsMoniker m3 = new TsMoniker();
        assertThat(m3.hashCode())
                .isEqualTo(m3.hashCode())
                .isNotEqualTo(new TsMoniker());
    }

    @Test
    public void testCompareTo() {
        TsMoniker a = new TsMoniker("A", "123");
        TsMoniker b = new TsMoniker("B", "123");
        TsMoniker c = new TsMoniker("C", "123");
        TsMoniker a_bis = new TsMoniker("A", "123");

        // R1
        assertEquals(signum(a.compareTo(b)), -signum(b.compareTo(a)));
        // R2
        assertTrue(b.compareTo(a) > 0);
        assertTrue(c.compareTo(b) > 0);
        assertTrue(c.compareTo(a) > 0);
        // R3
        assertEquals(0, a.compareTo(a_bis));
        assertEquals(signum(a.compareTo(c)), signum(a_bis.compareTo(c)));
        // R4
        assertEquals(0, a.compareTo(a));

        TsMoniker x = new TsMoniker();
        TsMoniker y = new TsMoniker();
        assertThat(x.compareTo(x)).isEqualTo(0);
        assertThat(x.compareTo(y)).isEqualTo(y.compareTo(x) * -1);
    }

    static int signum(int value) {
        return value == 0 ? 0 : value > 0 ? 1 : -1;
    }
}
