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
public class DataSourceTest {

    static final String PNAME = "SPREADSHEET", VERSION = "20111209",
            K1 = "file", V1 = "c:\\data.txt",
            K2 = "locale", V2 = "fr_BE",
            K3 = "datePattern", V3 = "yyyy-MM-dd";

    static DataSource newSample() {
        return new DataSource(PNAME, VERSION, ImmutableSortedMap.of(K1, V1, K2, V2, K3, V3));
    }

    @Test
    public void testConstructor() {
        DataSource dataSource = newSample();
        Assert.assertEquals(PNAME, dataSource.getProviderName());
        Assert.assertEquals(VERSION, dataSource.getVersion());
        for (Entry<String, String> o : ImmutableSortedMap.of(K1, V1, K2, V2, K3, V3).entrySet()) {
            Assert.assertEquals(o.getValue(), dataSource.getParams().get(o.getKey()));
        }
    }

    @Test
    public void testEquals() {
        DataSource dataSource = newSample();
        Assert.assertEquals(dataSource, newSample());
        Assert.assertNotSame(dataSource, newSample());
        Assert.assertFalse(dataSource.equals(new DataSource(PNAME, VERSION, ImmutableSortedMap.<String, String>of())));

        DataSource d1 = new DataSource("", "", ImmutableSortedMap.of(K1, V1, K2, V2, K3, V3));
        DataSource d2 = new DataSource("", "", ImmutableSortedMap.of(K3, V3, K2, V2, K1, V1));
        Assert.assertEquals(d1, d2);
    }

    @Test
    public void testToString() {
        DataSource d1 = new DataSource("", "", ImmutableSortedMap.of(K1, V1, K2, V2, K3, V3));
        DataSource d2 = new DataSource("", "", ImmutableSortedMap.of(K3, V3, K2, V2, K1, V1));
        Assert.assertEquals(d1.toString(), d2.toString());
    }

    @Test
    public void testHashCode() {
        DataSource dataSource = newSample();
        Assert.assertEquals(dataSource.hashCode(), newSample().hashCode());
        Assert.assertFalse(dataSource.hashCode() == new DataSource(PNAME, VERSION, ImmutableSortedMap.<String, String>of()).hashCode());
    }

    @Test
    public void testGet() {
        DataSource dataSource = newSample();
        Assert.assertEquals(3, dataSource.getParams().size());
        Assert.assertEquals(V1, dataSource.get(K1));
        Assert.assertEquals(V2, dataSource.get(K2));
        Assert.assertEquals(V3, dataSource.get(K3));
        Assert.assertNull(dataSource.get("hello"));
    }

    @Test
    public void testBuilder() {
        DataSource dataSource = newSample();
        DataSource.Builder builder = DataSource.builder(dataSource);
        DataSource tmp = builder.build();
        Assert.assertEquals(dataSource, tmp);
        Assert.assertNotSame(dataSource, tmp);
        Assert.assertEquals(tmp, builder.build());
        Assert.assertNotSame(dataSource, builder.build());
        builder.put(K1, "hello");
        Assert.assertEquals("hello", builder.build().get(K1));
    }

    @Test
    public void testXmlFormatter() {
        DataSource dataSource = newSample();
        Formatters.Formatter<DataSource> formatter = DataSource.xmlFormatter(false);
        Assert.assertNotNull(formatter.format(dataSource));
        //System.out.println(formatter.format(dataSource));

        DataSource d1 = new DataSource("", "", ImmutableSortedMap.of(K1, V1, K2, V2, K3, V3));
        DataSource d2 = new DataSource("", "", ImmutableSortedMap.of(K3, V3, K2, V2, K1, V1));
        Assert.assertEquals(formatter.format(d1), formatter.format(d2));
    }

    @Test
    public void testXmlParser() {
        DataSource dataSource = newSample();
        Assert.assertEquals(dataSource, DataSource.xmlParser().parse(DataSource.xmlFormatter(false).tryFormat(dataSource).get()));
    }

    @Test
    public void testSerializable() throws IOException, ClassNotFoundException {

        DataSource dataSource = newSample();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(dataSource);
        }
        byte[] bytes = baos.toByteArray();

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            DataSource result = (DataSource) ois.readObject();
            Assert.assertEquals(dataSource, result);
            Assert.assertNotSame(dataSource, result);
        }
    }

    @Test
    public void testUriFormatter() {
        DataSource dataSource = newSample();
        Formatters.Formatter<DataSource> formatter = DataSource.uriFormatter();
        Assert.assertNotNull(formatter.format(dataSource));

        DataSource d1 = new DataSource("", "", ImmutableSortedMap.of(K1, V1, K2, V2, K3, V3));
        DataSource d2 = new DataSource("", "", ImmutableSortedMap.of(K3, V3, K2, V2, K1, V1));
        Assert.assertEquals(formatter.format(d1), formatter.format(d2));

        DataSource empty = new DataSource("", "", ImmutableSortedMap.<String, String>of());
        Assert.assertEquals("demetra://tsprovider//?", formatter.format(empty));
    }

    @Test
    public void testUriParser() {
        DataSource dataSource = newSample();
        Assert.assertEquals(dataSource, DataSource.uriParser().parse(DataSource.uriFormatter().tryFormat(dataSource).get()));

        DataSource empty = new DataSource("", "", ImmutableSortedMap.<String, String>of());
        Assert.assertEquals(empty, DataSource.uriParser().parse(DataSource.uriFormatter().tryFormat(empty).get()));
    }
}
