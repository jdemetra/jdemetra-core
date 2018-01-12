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
package demetra.utilities;

import demetra.utilities.ServiceLookup.Loader;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ServiceLookupTest {

    <S> Iterable<S> load(Class<S> type) {
        if (type.equals(String.class)) {
            return (Iterable<S>) Arrays.asList("hello", "world");
        }
        return Collections.emptyList();
    }

    @Test
    public void testLoadFirst() {
        Loader loader = this::load;
        Logger logger = Logger.getGlobal();
        assertThat(ServiceLookup.loadFirst(String.class, loader, logger)).isEqualTo("hello");
        assertThatThrownBy(() -> ServiceLookup.loadFirst(Integer.class, loader, logger)).isInstanceOf(ServiceException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testFirst() {
        assertThatThrownBy(() -> ServiceLookup.first(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ServiceLookup.firstDynamic(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ServiceLookup.firstMutable(null)).isInstanceOf(NullPointerException.class);
    }
}
