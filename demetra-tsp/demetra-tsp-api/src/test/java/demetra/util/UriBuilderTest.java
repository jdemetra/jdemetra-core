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
package demetra.util;

import com.google.common.collect.ImmutableSortedMap;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import org.assertj.core.api.Assertions;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Philippe Charles
 */
public class UriBuilderTest {

    final String scheme = "http";
    final String host = "www.nbb.be";
    final SortedMap<String, String> query = ImmutableSortedMap.<String, String>naturalOrder()
            .put("email", "contact@nbb.be")
            .put("*", "[1.2, 2.6]")
            .put("file", "C:\\Program Files\\data.xls").build();
    final String[] path = {"dq/rd", "demetra+"};
    //
    final String rawScheme = "http";
    final String rawHost = "www.nbb.be";
    final String rawPath = "/dq%2Frd/demetra%2B";
    final String rawQuery = "*=%5B1.2%2C+2.6%5D&email=contact%40nbb.be&file=C%3A%5CProgram+Files%5Cdata.xls";

    @Test
    public void testBuildString() {
        UriBuilder builder = new UriBuilder(scheme, host).path(path).query(query);
        assertEquals(rawScheme + "://" + rawHost + rawPath + "?" + rawQuery, builder.buildString());
    }

    @Test
    public void testUriVsString() {
        UriBuilder builder = new UriBuilder(scheme, host).path(path).query(query);
        assertEquals(builder.buildString(), builder.build().toString());
    }

    @Test
    public void testScheme() {
        URI uri = new UriBuilder(scheme, host).path(path).query(query).build();
        assertEquals(scheme, uri.getScheme());
    }

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void testSchemeEmpty() {
        new UriBuilder("", host).build();
    }

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void testSchemeNull() {
        new UriBuilder(null, host).build();
    }

    @Test
    public void testHost() {
        URI uri = new UriBuilder(scheme, host).path(path).query(query).build();
        assertEquals(host, uri.getHost());
    }

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void testHostEmpty() {
        new UriBuilder(scheme, "").build();
    }

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void testHostNull() {
        new UriBuilder("", null).build();
    }

    @Test
    public void testPath() {
        // FIXME: what to do with empty or null ?
//        assertEquals("", new UriBuilder(scheme, host).path("").build().getRawPath());
//        assertEquals("", new UriBuilder(scheme, host).path((String)null).build().getRawPath());
//        assertEquals("", new UriBuilder(scheme, host).path((String[])null).build().getRawPath());

        Assertions.assertThat(new UriBuilder(scheme, host).build().getRawPath()).isEmpty();
        Assertions.assertThat(new UriBuilder(scheme, host).path(path).build().getRawPath()).isEqualTo(rawPath);
    }

    @Test
    public void testQuery() {
        URI uri = new UriBuilder(scheme, host).path(path).query(query).build();
        assertEquals(rawQuery, uri.getRawQuery());
        Map<String, String> tmp = UriBuilder.getQueryMap(uri);
        assertNotNull(tmp);
        assertEquals(query.size(), tmp.size());
        for (Entry<String, String> o : tmp.entrySet()) {
            assertTrue(query.containsKey(o.getKey()));
            assertEquals(o.getValue(), query.get(o.getKey()));
        }
    }

    @Test
    public void testGetPathArray() {
        Assertions.assertThat(new UriBuilder(scheme, host).build()).satisfies(o -> {
            Assertions.assertThat(UriBuilder.getPathArray(o, 0)).isNull();
            Assertions.assertThat(UriBuilder.getPathArray(o, 1)).isNull();
        });

        Assertions.assertThat(new UriBuilder(scheme, host).path(path).build()).satisfies(o -> {
            Assertions.assertThat(UriBuilder.getPathArray(o, 0)).isNull();
            Assertions.assertThat(UriBuilder.getPathArray(o, 1)).isNull();
            Assertions.assertThat(UriBuilder.getPathArray(o, 2)).containsExactly(path);
            Assertions.assertThat(UriBuilder.getPathArray(o, 3)).isNull();
        });
    }
}
