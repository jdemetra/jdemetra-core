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
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class DataFormatTest {

    static final double VALUE = 1234.5d;
    static final Date PERIOD = new TsPeriod(TsFrequency.Monthly, 2000, 0).firstday().getTime();

    private void assertEquivalent(Date value, DataFormat df, DateFormat r) throws ParseException {
        DateFormat l = df.newDateFormat();
        assertEquals(value, l.parse(r.format(value)));
        assertEquals(value, r.parse(l.format(value)));
    }

    @Test
    public void testNewNumberFormat() throws ParseException {
        NumberFormat f1 = new DataFormat(Locale.FRANCE, null, null).newNumberFormat();
        assertEquals(VALUE, f1.parse("1234,5").doubleValue(), 0);
        char groupingSep = (((DecimalFormat) f1).getDecimalFormatSymbols()).getGroupingSeparator();
        assertEquals("1" + groupingSep + "234,5", f1.format(VALUE));

        NumberFormat f2 = new DataFormat(Locale.US, null, null).newNumberFormat();
        assertEquals(VALUE, f2.parse("1,234.5").doubleValue(), 0);
        assertEquals("1,234.5", f2.format(VALUE));

        NumberFormat f3 = new DataFormat(Locale.FRANCE, null, "#0.00").newNumberFormat();
        assertEquals("1234,50", f3.format(VALUE));

        NumberFormat f4 = new DataFormat(Locale.US, null, "#0.00 €").newNumberFormat();
        assertEquals(VALUE, f4.parse("1234.50 €"));
        assertEquals("1234.50 €", f4.format(VALUE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewNumberFormat2() {
        new DataFormat(Locale.US, null, ",.,.,.").newNumberFormat();
    }

    @Test
    public void testNewDateFormat() throws ParseException {
        DateFormat f1 = new DataFormat(Locale.FRANCE, "yyyy-MMM", null).newDateFormat();
        assertEquals("2000-janv.", f1.format(PERIOD));
        assertEquals(PERIOD, f1.parse("2000-janv."));

        assertEquivalent(PERIOD, new DataFormat(Locale.FRANCE, "yyyy-MMM", null), new SimpleDateFormat("yyyy-MMM", Locale.FRANCE));
        assertEquivalent(PERIOD, new DataFormat(null, "yyyy-MMM", null), new SimpleDateFormat("yyyy-MMM"));
        assertEquivalent(PERIOD, new DataFormat(Locale.FRANCE, null, null), SimpleDateFormat.getDateInstance(DateFormat.DEFAULT, Locale.FRANCE));
        assertEquivalent(PERIOD, new DataFormat(null, null, null), SimpleDateFormat.getDateInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewDateFormat2() throws ParseException {
        new DataFormat(Locale.FRENCH, "c", null).newDateFormat();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewDateFormat3() throws ParseException {
        new DataFormat(null, "c", null).newDateFormat();
    }

    @Test
    public void testEquals() {
        assertEquals(new DataFormat(null, null, null), new DataFormat(null, null, null));
        assertEquals(new DataFormat(Locale.JAPAN, null, null), new DataFormat(Locale.JAPAN, null, null));
        assertNotEquals(new DataFormat(Locale.JAPAN, null, null), new DataFormat(Locale.FRANCE, null, null));
        assertEquals(new DataFormat(Locale.JAPAN, "MMMdd", "#"), new DataFormat(Locale.JAPAN, "MMMdd", "#"));
        assertNotEquals(new DataFormat(Locale.JAPAN, "MMMdd", "#"), new DataFormat(Locale.FRANCE, null, null));
    }

    @Test
    public void testToLocale() {
        Locale locale;
        locale = DataFormat.toLocale("fr");
        assertEquals("fr", locale.getLanguage());
        locale = DataFormat.toLocale("fr_BE");
        assertEquals("fr", locale.getLanguage());
        locale = DataFormat.toLocale("fr_BE_WIN");
        assertEquals("fr", locale.getLanguage());
        assertEquals("BE", locale.getCountry());
        assertEquals("WIN", locale.getVariant());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale2() {
        DataFormat.toLocale("helloworld");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale3() {
        DataFormat.toLocale("fr_");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale4() {
        DataFormat.toLocale("fr_BE_");
    }
}
