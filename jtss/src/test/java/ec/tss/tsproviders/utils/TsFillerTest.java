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

import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class TsFillerTest {

    @Test
    @SuppressWarnings("null")
    public void testNoOpFactory() {
        assertThat(TsFiller.noOp()).isNotNull();
    }

    @Test
    @SuppressWarnings("null")
    public void testNoOp() {
        TsFiller filler = TsFiller.noOp();

        assertThatThrownBy(() -> filler.fillCollection(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> filler.fillSeries(null)).isInstanceOf(NullPointerException.class);

        assertThat(filler.fillCollection(new TsCollectionInformation())).isTrue();
        assertThat(filler.fillSeries(new TsInformation())).isTrue();
    }
}
