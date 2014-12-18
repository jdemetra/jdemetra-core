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
import java.util.Map.Entry;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class DataSetTest {

    static final DataSource ID = DataSourceTest.newSample();
    static final String K1 = "domain", V1 = "NB01";

    static DataSet newSample() {
        return new DataSet(ID, DataSet.Kind.DUMMY, ImmutableSortedMap.of(K1, V1));
    }

    @Test
    public void testConstructor() {
        DataSet dataSet = newSample();
        Assert.assertEquals(ID, dataSet.getDataSource());
        for (Entry<String, String> o : ImmutableSortedMap.of(K1, V1).entrySet()) {
            Assert.assertEquals(o.getValue(), dataSet.getParams().get(o.getKey()));
        }
    }

    @Test
    public void testEquals() {
        DataSet dataSet = newSample();
        Assert.assertEquals(dataSet, newSample());
        Assert.assertNotSame(dataSet, newSample());
        Assert.assertFalse(dataSet.equals(new DataSet(ID, DataSet.Kind.DUMMY, ImmutableSortedMap.<String, String>of())));
    }

    @Test
    public void testHashCode() {
        DataSet dataSet = newSample();
        Assert.assertEquals(dataSet.hashCode(), newSample().hashCode());
        Assert.assertFalse(dataSet.hashCode() == new DataSet(ID, DataSet.Kind.DUMMY, ImmutableSortedMap.<String, String>of()).hashCode());
    }

    @Test
    public void testGet() {
        DataSet dataSet = newSample();
        Assert.assertEquals(1, dataSet.getParams().size());
        Assert.assertEquals(V1, dataSet.get(K1));
        Assert.assertNull(dataSet.get("hello"));
    }

    @Test
    public void testXmlFormatter() {
        DataSet dataSet = newSample();
        Formatters.Formatter<DataSet> formatter = DataSet.xmlFormatter(false);
        Assert.assertNotNull(formatter.format(dataSet));
        //System.err.println(DataSet.xmlFormatter(false).format(dataSet));

        DataSet d1 = new DataSet(ID, DataSet.Kind.COLLECTION, ImmutableSortedMap.of(K1, V1));
        DataSet d2 = new DataSet(ID, DataSet.Kind.COLLECTION, ImmutableSortedMap.of(K1, V1));
        Assert.assertEquals(formatter.format(d1), formatter.format(d2));
    }

    @Test
    public void testXmlParser() {
        DataSet dataSet = newSample();
        Assert.assertEquals(dataSet, DataSet.xmlParser().parse(DataSet.xmlFormatter(false).tryFormat(dataSet).get()));
    }

    @Test
    public void testSerializable() throws IOException, ClassNotFoundException {

        DataSet dataSet = newSample();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(dataSet);
        }
        byte[] bytes = baos.toByteArray();

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            DataSet result = (DataSet) ois.readObject();
            Assert.assertEquals(dataSet, result);
            Assert.assertNotSame(dataSet, result);
        }
    }

    @Test
    public void testUriFormatter() {
        DataSet dataSet = newSample();
        Formatters.Formatter<DataSet> formatter = DataSet.uriFormatter();
        Assert.assertNotNull(formatter.format(dataSet));

        DataSet d1 = new DataSet(ID, DataSet.Kind.COLLECTION, ImmutableSortedMap.of(K1, V1));
        DataSet d2 = new DataSet(ID, DataSet.Kind.COLLECTION, ImmutableSortedMap.of(K1, V1));
        Assert.assertEquals(formatter.format(d1), formatter.format(d2));

        DataSet empty = new DataSet(ID, DataSet.Kind.COLLECTION, ImmutableSortedMap.<String, String>of());
        Assert.assertEquals("demetra://tsprovider/SPREADSHEET/20111209/COLLECTION?datePattern=yyyy-MM-dd&file=c%3A%5Cdata.txt&locale=fr_BE#", formatter.format(empty));
    }

    @Test
    public void testUriParser() {
        DataSet dataSet = newSample();
        Assert.assertEquals(dataSet, DataSet.uriParser().parse(DataSet.uriFormatter().tryFormat(dataSet).get()));

        DataSet empty = new DataSet(ID, DataSet.Kind.COLLECTION, ImmutableSortedMap.<String, String>of());
        Assert.assertEquals(empty, DataSet.uriParser().parse(DataSet.uriFormatter().tryFormat(empty).get()));
    }
}
