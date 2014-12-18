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

package ec.tss.tsproviders.utils;

import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.File;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ParsersTest {

    @Test
    public void testBoolParser() {
        assertEquals(Boolean.TRUE, Parsers.boolParser().parse("true"));
        assertEquals(Boolean.FALSE, Parsers.boolParser().parse("false"));
        assertEquals(Boolean.TRUE, Parsers.boolParser().parse("TRUE"));
        assertEquals(Boolean.FALSE, Parsers.boolParser().parse("FALSE"));
        assertEquals(null, Parsers.boolParser().parse("hello"));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testBoolParserNull() {
        Parsers.boolParser().parse(null);
    }

    @Test
    public void testCharsetParser() {
        assertEquals(StandardCharsets.UTF_8, Parsers.charsetParser().parse("UTF-8"));
        assertEquals(null, Parsers.charsetParser().parse("hello"));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testCharsetParserNull() {
        Parsers.charsetParser().parse(null);
    }

    @Test
    public void testDoubleArrayParser() {
        assertArrayEquals(new double[]{3.5, 6.1}, Parsers.doubleArrayParser().parse("[3.5,6.1]"), 0);
        assertArrayEquals(new double[]{3.5, 6.1}, Parsers.doubleArrayParser().parse("[ 3.5  ,     6.1 ]"), 0);
        assertEquals(null, Parsers.doubleArrayParser().parse("[3.5;6.1]"));
        assertEquals(null, Parsers.doubleArrayParser().parse("3.5,6.1]"));
        assertEquals(null, Parsers.doubleArrayParser().parse("hello"));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testDoubleArrayParserNull() {
        Parsers.doubleArrayParser().parse(null);
    }

    @Test
    public void testFileParser() {
        assertEquals(new File("test.xml"), Parsers.fileParser().parse("test.xml"));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testFileParserNull() {
        Parsers.fileParser().parse(null);
    }

    @Test
    public void testEnumParser() {
        assertEquals(TsFrequency.Monthly, Parsers.enumParser(TsFrequency.class).parse("Monthly"));
        assertEquals(null, Parsers.enumParser(TsFrequency.class).parse("hello"));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testEnumParserNull() {
        Parsers.enumParser(TsFrequency.class).parse(null);
    }

    @Test
    public void testIntParser() {
        assertEquals((long) 123, Parsers.intParser().tryParse("123").get().longValue());
        assertEquals(null, Parsers.intParser().parse("123.3"));
        assertEquals(null, Parsers.intParser().parse("hello"));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testIntParserNull() {
        Parsers.intParser().parse(null);
    }

    @Test
    public void testStringParser() {
        assertEquals("hello", Parsers.stringParser().parse("hello"));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testStringParserNull() {
        Parsers.stringParser().parse(null);
    }

    @Test
    public void testOptional() {
        Parsers.Parser<Integer> p = Parsers.intParser();
        assertFalse(p.tryParse("hello").isPresent());
        assertTrue(p.tryParse("123").isPresent());
        assertEquals((Integer) 123, p.tryParse("123").get());
    }
}
