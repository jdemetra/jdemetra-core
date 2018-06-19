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

import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TsMonikerTest {

    @Test
    @SuppressWarnings("null")
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> TsMoniker.createProvidedMoniker(null, "123"));
        assertThatNullPointerException().isThrownBy(() -> TsMoniker.createProvidedMoniker("ABC", null));

        TsMoniker provided = TsMoniker.createProvidedMoniker("ABC", "123");
        assertThat(provided)
                .extracting("source", "id", "type")
                .containsExactly("ABC", "123", TsMoniker.Type.PROVIDED);

        TsMoniker anonymous = TsMoniker.createAnonymousMoniker();
        assertThat(anonymous)
                .extracting("source", "id", "type")
                .containsExactly(null, null, TsMoniker.Type.ANONYMOUS);

        TsMoniker dynamic = TsMoniker.createDynamicMoniker();
        assertThat(dynamic)
                .extracting("source", "id", "type")
                .containsExactly(Ts.DYNAMIC, null, TsMoniker.Type.DYNAMIC);
    }

    @Test
    public void testEquals() {
        TsMoniker provided = TsMoniker.createProvidedMoniker("ABC", "123");
        assertThat(provided)
                .isEqualTo(TsMoniker.createProvidedMoniker("ABC", "123"))
                .isNotSameAs(TsMoniker.createProvidedMoniker("ABC", "123"))
                .isNotEqualTo(TsMoniker.createProvidedMoniker("ABC", "xxx"))
                .isNotEqualTo(TsMoniker.createProvidedMoniker("xxx", "123"))
                .isNotEqualTo(TsMoniker.createAnonymousMoniker())
                .isNotEqualTo(TsMoniker.createDynamicMoniker())
                .isNotEqualTo(TsMoniker.createProvidedMoniker("", ""));

        TsMoniker anonymous = TsMoniker.createAnonymousMoniker();
        assertThat(anonymous)
                .isEqualTo(anonymous)
                .isNotEqualTo(TsMoniker.createAnonymousMoniker())
                .isNotEqualTo(TsMoniker.createDynamicMoniker())
                .isNotEqualTo(TsMoniker.createProvidedMoniker("", ""));

        TsMoniker dynamic = TsMoniker.createDynamicMoniker();
        assertThat(dynamic)
                .isEqualTo(dynamic)
                .isNotEqualTo(TsMoniker.createAnonymousMoniker())
                .isNotEqualTo(TsMoniker.createDynamicMoniker())
                .isNotEqualTo(TsMoniker.createProvidedMoniker("", ""));
    }

    @Test
    public void testHashcode() {
        TsMoniker provided = TsMoniker.createProvidedMoniker("ABC", "123");
        assertThat(provided.hashCode())
                .isEqualTo(TsMoniker.createProvidedMoniker("ABC", "123").hashCode())
                .isNotEqualTo(TsMoniker.createProvidedMoniker("ABC", "xxx").hashCode())
                .isNotEqualTo(TsMoniker.createProvidedMoniker("xxx", "123").hashCode())
                .isNotEqualTo(TsMoniker.createAnonymousMoniker().hashCode())
                .isNotEqualTo(TsMoniker.createDynamicMoniker().hashCode())
                .isNotEqualTo(TsMoniker.createProvidedMoniker("", "").hashCode());

        TsMoniker anonymous = TsMoniker.createAnonymousMoniker();
        assertThat(anonymous.hashCode())
                .isEqualTo(anonymous.hashCode())
                .isNotEqualTo(TsMoniker.createAnonymousMoniker().hashCode())
                .isNotEqualTo(TsMoniker.createDynamicMoniker().hashCode())
                .isNotEqualTo(TsMoniker.createProvidedMoniker("", "").hashCode());

        TsMoniker dynamic = TsMoniker.createDynamicMoniker();
        assertThat(dynamic.hashCode())
                .isEqualTo(dynamic.hashCode())
                .isNotEqualTo(TsMoniker.createAnonymousMoniker().hashCode())
                .isNotEqualTo(TsMoniker.createDynamicMoniker().hashCode())
                .isNotEqualTo(TsMoniker.createProvidedMoniker("", "").hashCode());
    }

    @Test
    public void testCompareTo() {
        TsMoniker p1 = TsMoniker.createProvidedMoniker("A", "123");
        TsMoniker p2 = TsMoniker.createProvidedMoniker("B", "123");
        TsMoniker p3 = TsMoniker.createProvidedMoniker("C", "123");

        assertThat(p1).isEqualByComparingTo(TsMoniker.createProvidedMoniker("A", "123"));
        assertThat(p2).isGreaterThan(p1);
        assertThat(p3).isGreaterThan(p2);
        assertThat(p3).isGreaterThan(p1);

        TsMoniker a1 = TsMoniker.createAnonymousMoniker();
        TsMoniker a2 = TsMoniker.createAnonymousMoniker();
        assertThat(a1).isEqualByComparingTo(a1);
        assertThat(a1.compareTo(a2)).isEqualTo(a2.compareTo(a1) * -1);
    }
}
