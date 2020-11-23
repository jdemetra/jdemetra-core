/*
 * Copyright 2018 National Bank of Belgium
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
package demetra.util;

import com.google.common.collect.ImmutableMap;
import java.beans.IntrospectionException;
import java.io.StringWriter;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SubstitutorTest {

    @Test
    @SuppressWarnings("null")
    public void testReplace() {
        Substitutor x = Substitutor.of(ImmutableMap.of("user.name", "charphi"));

        assertThatNullPointerException().isThrownBy(() -> x.replace(null));

        assertThat(x.replace("")).isEqualTo("");
        assertThat(x.replace("${user.name}")).isEqualTo("charphi");
        assertThat(x.replace("hello ${user.name}")).isEqualTo("hello charphi");
        assertThat(x.replace("${user.name}'s book")).isEqualTo("charphi's book");

        assertThat(x.replace("${username}")).isEqualTo("null");
        assertThat(x.replace("${user.name")).isEqualTo("${user.name");
        assertThat(x.replace("user.name}")).isEqualTo("user.name}");
        assertThat(x.replace("$${user.name}")).isEqualTo("$charphi");
        assertThat(x.replace("${${user.name}}")).isEqualTo("null}");

        assertThat(x.replace("${other}")).isEqualTo("null");
    }

    @Test
    @SuppressWarnings("null")
    public void testReplaceIntoAppendable() {
        Substitutor x = Substitutor.of(ImmutableMap.of("user.name", "charphi"));

        assertThatNullPointerException().isThrownBy(() -> x.replaceInto(null, new StringWriter()));
        assertThatNullPointerException().isThrownBy(() -> x.replaceInto("", (Appendable) null));
    }

    @Test
    @SuppressWarnings("null")
    public void testReplaceIntoStringBuilder() {
        Substitutor x = Substitutor.of(ImmutableMap.of("user.name", "charphi"));

        assertThatNullPointerException().isThrownBy(() -> x.replaceInto(null, new StringBuilder()));
        assertThatNullPointerException().isThrownBy(() -> x.replaceInto("", (StringBuilder) null));
    }

    @Test
    public void testPrefixAndSuffix() {
        Substitutor mustache = Substitutor
                .builder()
                .prefix("{{")
                .suffix("}}")
                .mapper(ImmutableMap.of("user.name", "charphi")::get)
                .build();

        assertThat(mustache.replace("hello {{user.name}}")).isEqualTo("hello charphi");

        Substitutor dos = Substitutor
                .builder()
                .prefix("%")
                .suffix("%")
                .mapper(ImmutableMap.of("user.name", "charphi")::get)
                .build();

        assertThat(dos.replace("hello %user.name%")).isEqualTo("hello charphi");
    }

    @Test
    @SuppressWarnings("null")
    public void testOfBean() throws IntrospectionException {
        MyBean bean = new MyBean();
        bean.setIndex(10);
        bean.setRef("A4");

        assertThatNullPointerException().isThrownBy(() -> Substitutor.ofBean(null));

        assertThat(Substitutor.ofBean(bean).replace("${index} -> ${ref}")).isEqualTo("10 -> A4");
        assertThat(Substitutor.ofBean(bean).replace("${hello} -> ${ref}")).isEqualTo("null -> A4");
    }

    @lombok.Data
    public static class MyBean {

        int index;
        String ref;
    }
}
