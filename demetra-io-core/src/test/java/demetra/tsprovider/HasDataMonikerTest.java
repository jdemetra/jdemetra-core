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

import demetra.timeseries.TsMoniker;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class HasDataMonikerTest {

    private final String providerName = "myprovider";
    private final DataSource goodDataSource = DataSource.of("myprovider", "1234");
    private final DataSource badDataSource = DataSource.of("xxx", "1234");
    private final DataSet goodDataSet = DataSet.of(goodDataSource, DataSet.Kind.SERIES);
    private final DataSet badDataSet = DataSet.of(badDataSource, DataSet.Kind.SERIES);
    private final TsMoniker goodDataSourceMoniker = TsMoniker.of(providerName, DataSource.uriFormatter().formatValueAsString(goodDataSource).orElseThrow(RuntimeException::new));
    private final TsMoniker badDataSourceMoniker = TsMoniker.of("xxx", DataSource.uriFormatter().formatValueAsString(badDataSource).orElseThrow(RuntimeException::new));
    private final TsMoniker goodDataSetMoniker = TsMoniker.of(providerName, DataSet.uriFormatter().formatValueAsString(goodDataSet).orElseThrow(RuntimeException::new));
    private final TsMoniker badDataSetMoniker = TsMoniker.of("xxx", DataSet.uriFormatter().formatValueAsString(badDataSet).orElseThrow(RuntimeException::new));

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        assertThat(HasDataMoniker.usingUri(providerName)).isNotNull();
        assertThatThrownBy(() -> HasDataMoniker.usingUri(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testToDataSource() {
        HasDataMoniker support = HasDataMoniker.usingUri(providerName);
        assertThat(support.toDataSource(goodDataSourceMoniker)).isEqualTo(goodDataSource);
        assertThat(support.toDataSource(goodDataSetMoniker)).isNull();
        assertThatThrownBy(() -> support.toDataSource(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.toDataSource(badDataSourceMoniker)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> support.toDataSource(badDataSetMoniker)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testToDataSet() {
        HasDataMoniker support = HasDataMoniker.usingUri(providerName);
        assertThat(support.toDataSet(goodDataSetMoniker)).isEqualTo(goodDataSet);
        assertThat(support.toDataSet(goodDataSourceMoniker)).isNull();
        assertThatThrownBy(() -> support.toDataSet(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.toDataSet(badDataSourceMoniker)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> support.toDataSet(badDataSetMoniker)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testFromDataSource() {
        HasDataMoniker support = HasDataMoniker.usingUri(providerName);
        assertThat(support.toMoniker(goodDataSource)).isEqualTo(goodDataSourceMoniker);
        assertThatThrownBy(() -> support.toMoniker((DataSource) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.toMoniker(badDataSource)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testFromDataSet() {
        HasDataMoniker support = HasDataMoniker.usingUri(providerName);
        assertThat(support.toMoniker(goodDataSet)).isEqualTo(goodDataSetMoniker);
        assertThatThrownBy(() -> support.toMoniker((DataSet) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.toMoniker(badDataSet)).isInstanceOf(IllegalArgumentException.class);
    }
}
