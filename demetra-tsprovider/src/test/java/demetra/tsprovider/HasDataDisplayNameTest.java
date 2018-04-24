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
package demetra.tsprovider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class HasDataDisplayNameTest {

    private final String providerName = "myprovider";
    private final DataSource goodDataSource = DataSource.of("myprovider", "1234");
    private final DataSource badDataSource = DataSource.of("xxx", "1234");
    private final DataSet goodDataSet = DataSet.of(goodDataSource, DataSet.Kind.SERIES);
    private final DataSet badDataSet = DataSet.of(badDataSource, DataSet.Kind.SERIES);

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        assertThat(HasDataDisplayName.usingUri(providerName)).isNotNull();
        assertThatThrownBy(() -> HasDataDisplayName.usingUri(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetDisplayDataSourceName() {
        HasDataDisplayName support = HasDataDisplayName.usingUri(providerName);
        assertThat(support.getDisplayName(goodDataSource)).isNotEmpty();
        assertThatThrownBy(() -> support.getDisplayName((DataSource) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.getDisplayName(badDataSource)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetDisplayDataSetName() {
        HasDataDisplayName support = HasDataDisplayName.usingUri(providerName);
        assertThat(support.getDisplayName(goodDataSet)).isNotEmpty();
        assertThatThrownBy(() -> support.getDisplayName((DataSet) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.getDisplayName(badDataSet)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetDisplayDataSetNodeName() {
        HasDataDisplayName support = HasDataDisplayName.usingUri(providerName);
        assertThat(support.getDisplayNodeName(goodDataSet)).isNotEmpty();
        assertThatThrownBy(() -> support.getDisplayNodeName(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.getDisplayName(badDataSet)).isInstanceOf(IllegalArgumentException.class);
    }
}
