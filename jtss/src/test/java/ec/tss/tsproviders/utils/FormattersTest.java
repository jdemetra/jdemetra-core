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
public class FormattersTest {

    @Test
    public void testBoolFormatter() {
        assertEquals("true", Formatters.boolFormatter().format(Boolean.TRUE));
        assertEquals("false", Formatters.boolFormatter().format(Boolean.FALSE));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testBoolFormatterNull() {
        Formatters.boolFormatter().format(null);
    }

    @Test
    public void testCharsetFormatter() {
        assertEquals("UTF-8", Formatters.charsetFormatter().format(StandardCharsets.UTF_8));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testCharsetFormatterNull() {
        Formatters.charsetFormatter().format(null);
    }

    @Test
    public void testOfInstance() {
        assertEquals("hello", Formatters.ofInstance("hello").format("lkj"));
        assertEquals(null, Formatters.ofInstance(null).format("lkj"));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testOfInstanceNull() {
        Formatters.ofInstance("hello").format(null);
    }

    @Test
    public void testDoubleArrayFormatter() {
        assertEquals("[0.4, -4.5]", Formatters.doubleArrayFormatter().format(new double[]{0.4, -4.5}));
        assertEquals("[]", Formatters.doubleArrayFormatter().format(new double[]{}));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testDoubleArrayFormatterNull() {
        Formatters.doubleArrayFormatter().format(null);
    }

    @Test
    public void testEnumFormatter() {
        assertEquals("Monthly", Formatters.<TsFrequency>enumFormatter().format(TsFrequency.Monthly));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testEnumFormatterNull() {
        Formatters.<TsFrequency>enumFormatter().format(null);
    }

    @Test
    public void testFileFormatter() {
        assertEquals("test.xml", Formatters.fileFormatter().format(new File("test.xml")));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testFileFormatterNull() {
        Formatters.fileFormatter().format(null);
    }

    @Test
    public void testIntFormatter() {
        assertEquals("42", Formatters.intFormatter().format(42));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testIntFormatterNull() {
        Formatters.intFormatter().format(null);
    }

    @Test
    public void testStringFormatter() {
        assertEquals("hello", Formatters.stringFormatter().format("hello"));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testStringFormatterNull() {
        Formatters.stringFormatter().format(null);
    }

    @Test
    public void testFormatterTryFormat() {
        assertEquals("123", Formatters.ofInstance("123").tryFormat(new Object()).get());
        assertFalse(Formatters.ofInstance(null).tryFormat(new Object()).isPresent());
    }

    @Test
    public void testFormatterFormatAsString() {
        assertEquals("123", Formatters.ofInstance("123").formatAsString(new Object()));
        assertNull(Formatters.ofInstance(null).formatAsString(new Object()));
    }

    @Test
    public void testFormatterTryFormatAsString() {
        assertEquals("123", Formatters.ofInstance("123").tryFormatAsString(new Object()).get());
        assertFalse(Formatters.ofInstance(null).tryFormatAsString(new Object()).isPresent());
    }
}
