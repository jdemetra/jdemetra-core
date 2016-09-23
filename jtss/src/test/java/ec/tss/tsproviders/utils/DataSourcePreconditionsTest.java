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

import ec.tss.TsMoniker;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import static ec.tss.tsproviders.utils.DataSourcePreconditions.checkProvider;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class DataSourcePreconditionsTest {

    @Test
    @SuppressWarnings("null")
    public void testDataSource() {
        DataSource input = DataSource.builder("myprovider", "1234").build();
        assertThat(checkProvider("myprovider", input))
                .isSameAs(input);
        assertThatThrownBy(() -> checkProvider("xxx", input))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> checkProvider(null, input))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> checkProvider("myprovider", (DataSource) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testDataSet() {
        DataSet input = DataSet.builder(DataSource.builder("myprovider", "1234").build(), DataSet.Kind.SERIES).build();
        assertThat(checkProvider("myprovider", input))
                .isSameAs(input);
        assertThatThrownBy(() -> checkProvider("xxx", input))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> checkProvider(null, input))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> checkProvider("myprovider", (DataSource) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testTsMoniker() {
        TsMoniker input = new TsMoniker("myprovider", "id");
        assertThat(checkProvider("myprovider", input))
                .isSameAs(input);
        assertThatThrownBy(() -> checkProvider("xxx", input))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> checkProvider(null, input))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> checkProvider("myprovider", (DataSource) null))
                .isInstanceOf(NullPointerException.class);
    }
}
