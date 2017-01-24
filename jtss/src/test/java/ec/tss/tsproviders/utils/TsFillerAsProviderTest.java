/*
 * Copyright 2016 National Bank of Belgium
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

import ec.tss.TsAsyncMode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TsFillerAsProviderTest {

    private final Runnable doNothing = () -> {
    };

    @Test
    @SuppressWarnings("null")
    public void testFactories() {
        assertThatThrownBy(() -> TsFillerAsProvider.of(null, TsAsyncMode.None, TsFiller.noOp())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsFillerAsProvider.of("provider", null, TsFiller.noOp())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsFillerAsProvider.of("provider", TsAsyncMode.None, null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> TsFillerAsProvider.of(null, TsAsyncMode.None, TsFiller.noOp(), doNothing)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsFillerAsProvider.of("provider", null, TsFiller.noOp(), doNothing)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsFillerAsProvider.of("provider", TsAsyncMode.None, null, doNothing)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TsFillerAsProvider.of("provider", TsAsyncMode.None, TsFiller.noOp(), null)).isInstanceOf(NullPointerException.class);
    }
}
