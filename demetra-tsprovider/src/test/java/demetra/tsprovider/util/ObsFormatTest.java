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
package demetra.tsprovider.util;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ObsFormatTest {

    static final double VALUE = 1234.5d;
    static final Date PERIOD = new GregorianCalendar(2000, 0, 1).getTime();

    private void assertEquivalent(Date value, ObsFormat df, DateFormat r) throws ParseException {
        DateFormat l = df.newDateFormat();
        assertEquals(value, l.parse(r.format(value)));
        assertEquals(value, r.parse(l.format(value)));
    }

    @Test
    public void testNewNumberFormat() throws ParseException {

        NumberFormat f1 = new ObsFormat(Locale.FRANCE, null, null).newNumberFormat();
        assertEquals(VALUE, f1.parse("1234,5").doubleValue(), 0);
        // '\u00a0' -> non-breaking space
        assertEquals("1\u00a0234,5", f1.format(VALUE));

        NumberFormat f2 = new ObsFormat(Locale.US, null, null).newNumberFormat();
        assertEquals(VALUE, f2.parse("1,234.5").doubleValue(), 0);
        assertEquals("1,234.5", f2.format(VALUE));

        NumberFormat f3 = new ObsFormat(Locale.FRANCE, null, "#0.00").newNumberFormat();
        assertEquals("1234,50", f3.format(VALUE));

        NumberFormat f4 = new ObsFormat(Locale.US, null, "#0.00 €").newNumberFormat();
        assertEquals(VALUE, f4.parse("1234.50 €"));
        assertEquals("1234.50 €", f4.format(VALUE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewNumberFormat2() {
        new ObsFormat(Locale.US, null, ",.,.,.").newNumberFormat();
    }

    @Test
    public void testNewDateFormat() throws ParseException {
        DateFormat f1 = new ObsFormat(Locale.FRANCE, "yyyy-MMM", null).newDateFormat();
        assertEquals("2000-janv.", f1.format(PERIOD));
        assertEquals(PERIOD, f1.parse("2000-janv."));

        assertEquivalent(PERIOD, new ObsFormat(Locale.FRANCE, "yyyy-MMM", null), new SimpleDateFormat("yyyy-MMM", Locale.FRANCE));
        assertEquivalent(PERIOD, new ObsFormat(null, "yyyy-MMM", null), new SimpleDateFormat("yyyy-MMM"));
        assertEquivalent(PERIOD, new ObsFormat(Locale.FRANCE, null, null), SimpleDateFormat.getDateInstance(DateFormat.DEFAULT, Locale.FRANCE));
        assertEquivalent(PERIOD, new ObsFormat(null, null, null), SimpleDateFormat.getDateInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewDateFormat2() throws ParseException {
        new ObsFormat(Locale.FRENCH, "c", null).newDateFormat();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewDateFormat3() throws ParseException {
        new ObsFormat(null, "c", null).newDateFormat();
    }

    @Test
    public void testEquals() {
        assertEquals(new ObsFormat(null, null, null), new ObsFormat(null, null, null));
        assertEquals(new ObsFormat(Locale.JAPAN, null, null), new ObsFormat(Locale.JAPAN, null, null));
        assertNotEquals(new ObsFormat(Locale.JAPAN, null, null), new ObsFormat(Locale.FRANCE, null, null));
        assertEquals(new ObsFormat(Locale.JAPAN, "MMMdd", "#"), new ObsFormat(Locale.JAPAN, "MMMdd", "#"));
        assertNotEquals(new ObsFormat(Locale.JAPAN, "MMMdd", "#"), new ObsFormat(Locale.FRANCE, null, null));
    }
}
