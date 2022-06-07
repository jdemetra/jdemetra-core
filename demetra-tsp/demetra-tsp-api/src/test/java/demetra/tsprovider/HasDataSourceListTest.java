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
package demetra.tsprovider;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 *
 * @author Philippe Charles
 */
public class HasDataSourceListTest {

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        assertThatThrownBy(() -> HasDataSourceList.of(null, Collections.emptyList())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> HasDataSourceList.of("name", null)).isInstanceOf(NullPointerException.class);

        DataSource.Builder b = DataSource.builder("name", "");
        List<DataSource> items = new ArrayList<>();
        items.add(b.parameter("key", "v1").build());

        HasDataSourceList list = HasDataSourceList.of("name", items);
        assertThatThrownBy(() -> list.addDataSourceListener(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> list.removeDataSourceListener(null)).isInstanceOf(NullPointerException.class);

        assertThat(list.getDataSources()).containsExactly(items.get(0));

        items.add(b.parameter("key", "v2").build());
        assertThat(list.getDataSources()).containsExactly(items.get(0));
    }
}
