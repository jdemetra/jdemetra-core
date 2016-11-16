/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package ec.tss.tsproviders;

import com.google.common.collect.ImmutableSortedMap;
import ec.tss.tsproviders.utils.Formatters;
import java.io.*;
import org.junit.Assert;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class DataSetTest {

    final DataSource id = DataSourceTest.newSample();
    final String k1 = "domain", v1 = "NB01";
    final ImmutableSortedMap<String, String> content = ImmutableSortedMap.of(k1, v1);
    final ImmutableSortedMap<String, String> emptyContent = ImmutableSortedMap.of();

    DataSet newSample() {
        return new DataSet(id, DataSet.Kind.DUMMY, content);
    }

    @Test
    public void testConstructor() {
        assertThat(newSample()).satisfies(o -> {
            assertThat(o.getDataSource()).isEqualTo(id);
            assertThat(o.getKind()).isEqualTo(DataSet.Kind.DUMMY);
            assertThat(o.getParams()).containsAllEntriesOf(content);
            content.forEach((k, v) -> assertThat(o.get(k)).isEqualTo(v));
        });
    }

    @Test
    public void testEquals() {
        assertThat(newSample())
                .isEqualTo(newSample())
                .isNotEqualTo(new DataSet(id, DataSet.Kind.SERIES, content))
                .isNotEqualTo(new DataSet(id, DataSet.Kind.DUMMY, emptyContent));
    }

    @Test
    public void testHashCode() {
        assertThat(newSample().hashCode())
                .isEqualTo(newSample().hashCode())
                .isNotEqualTo(new DataSet(id, DataSet.Kind.DUMMY, emptyContent).hashCode());
    }

    @Test
    public void testGet() {
        assertThat(newSample()).satisfies(o -> {
            assertThat(o.get(k1)).isEqualTo(v1);
            assertThat(o.get("hello")).isNull();
        });
    }

    @Test
    public void testGetParams() {
        assertThat(newSample().getParams()).containsAllEntriesOf(content);
    }

    @Test
    public void testXmlFormatter() {
        DataSet dataSet = newSample();
        Formatters.Formatter<DataSet> formatter = DataSet.xmlFormatter(false);
        Assert.assertNotNull(formatter.format(dataSet));
        //System.err.println(DataSet.xmlFormatter(false).format(dataSet));

        DataSet d1 = new DataSet(id, DataSet.Kind.COLLECTION, content);
        DataSet d2 = new DataSet(id, DataSet.Kind.COLLECTION, content);
        Assert.assertEquals(formatter.format(d1), formatter.format(d2));
    }

    @Test
    public void testXmlParser() {
        DataSet dataSet = newSample();
        Assert.assertEquals(dataSet, DataSet.xmlParser().parse(DataSet.xmlFormatter(false).formatValue(dataSet).get()));
    }

    @Test
    public void testSerializable() throws IOException, ClassNotFoundException {
        assertThat(Util.fromToBytes(newSample())).isEqualTo(newSample());
    }

    @Test
    public void testUriFormatter() {
        DataSet dataSet = newSample();
        Formatters.Formatter<DataSet> formatter = DataSet.uriFormatter();
        Assert.assertNotNull(formatter.format(dataSet));

        DataSet d1 = new DataSet(id, DataSet.Kind.COLLECTION, content);
        DataSet d2 = new DataSet(id, DataSet.Kind.COLLECTION, content);
        Assert.assertEquals(formatter.format(d1), formatter.format(d2));

        DataSet empty = new DataSet(id, DataSet.Kind.COLLECTION, emptyContent);
        Assert.assertEquals("demetra://tsprovider/SPREADSHEET/20111209/COLLECTION?datePattern=yyyy-MM-dd&file=c%3A%5Cdata.txt&locale=fr_BE#", formatter.format(empty));
    }

    @Test
    public void testUriParser() {
        DataSet dataSet = newSample();
        Assert.assertEquals(dataSet, DataSet.uriParser().parse(DataSet.uriFormatter().formatValue(dataSet).get()));

        DataSet empty = new DataSet(id, DataSet.Kind.COLLECTION, emptyContent);
        Assert.assertEquals(empty, DataSet.uriParser().parse(DataSet.uriFormatter().formatValue(empty).get()));
    }
}
