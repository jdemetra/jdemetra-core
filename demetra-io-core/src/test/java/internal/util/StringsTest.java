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
package internal.util;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class StringsTest {

    @Test
    public void testIterator() {
        assertThat(Strings.splitToIterator(',', "")).toIterable().containsExactly("");
        assertThat(Strings.splitToIterator(',', "hello")).toIterable().containsExactly("hello");
        assertThat(Strings.splitToIterator(',', "hello,world")).toIterable().containsExactly("hello", "world");
        assertThat(Strings.splitToIterator(',', ",world")).toIterable().containsExactly("", "world");
        assertThat(Strings.splitToIterator(',', "hello,")).toIterable().containsExactly("hello", "");
        assertThat(Strings.splitToIterator(',', ",")).toIterable().containsExactly("", "");
        assertThat(Strings.splitToIterator(',', ",,")).toIterable().containsExactly("", "", "");
    }
}
