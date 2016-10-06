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

import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.HasDataDisplayName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class DataDisplayNameSupportTest {

    private final String providerName = "myprovider";
    private final DataSource goodDataSource = DataSource.builder("myprovider", "1234").build();
    private final DataSource badDataSource = DataSource.builder("xxx", "1234").build();
    private final DataSet goodDataSet = DataSet.builder(goodDataSource, DataSet.Kind.SERIES).build();
    private final DataSet badDataSet = DataSet.builder(badDataSource, DataSet.Kind.SERIES).build();

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        assertThat(DataDisplayNameSupport.usingUri(providerName)).isNotNull();
        assertThatThrownBy(() -> DataDisplayNameSupport.usingUri(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetDisplayDataSourceName() {
        HasDataDisplayName support = DataDisplayNameSupport.usingUri(providerName);
        assertThat(support.getDisplayName(goodDataSource)).isNotEmpty();
        assertThatThrownBy(() -> support.getDisplayName((DataSource) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.getDisplayName(badDataSource)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetDisplayDataSetName() {
        HasDataDisplayName support = DataDisplayNameSupport.usingUri(providerName);
        assertThat(support.getDisplayName(goodDataSet)).isNotEmpty();
        assertThatThrownBy(() -> support.getDisplayName((DataSet) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.getDisplayName(badDataSet)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetDisplayDataSetNodeName() {
        HasDataDisplayName support = DataDisplayNameSupport.usingUri(providerName);
        assertThat(support.getDisplayNodeName(goodDataSet)).isNotEmpty();
        assertThatThrownBy(() -> support.getDisplayNodeName(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.getDisplayName(badDataSet)).isInstanceOf(IllegalArgumentException.class);
    }
}
