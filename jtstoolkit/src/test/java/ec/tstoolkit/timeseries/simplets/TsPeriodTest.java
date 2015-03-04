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
package ec.tstoolkit.timeseries.simplets;

import ec.tstoolkit.timeseries.Day;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TsPeriodTest {

    static final Date DATE_2001_01_10 = new GregorianCalendar(2001, Calendar.JANUARY, 10).getTime();
    static final Date DATE_2001_03_10 = new GregorianCalendar(2001, Calendar.MARCH, 10).getTime();
    static final Day DAY_2001_01_10 = new Day(DATE_2001_01_10);
    static final Day DAY_2001_03_10 = new Day(DATE_2001_03_10);
    TsPeriod period_2001_03;

    @Before
    public void before() {
        period_2001_03 = new TsPeriod(TsFrequency.Monthly, 2001, 2);
    }

    @Test
    public void testContainsDate() {
        assertTrue(period_2001_03.contains(DATE_2001_03_10));
        assertFalse(period_2001_03.contains(DATE_2001_01_10));
    }

    @Test
    public void testIsAfterDate() {
        assertTrue(period_2001_03.isAfter(DATE_2001_01_10));
        assertFalse(period_2001_03.isAfter(DATE_2001_03_10));
    }

    @Test
    public void testIsBeforeDate() {
        assertFalse(period_2001_03.isBefore(DATE_2001_01_10));
        assertFalse(period_2001_03.isBefore(DATE_2001_03_10));
    }

    @Test
    public void testMiddle() {
        Calendar cal = Calendar.getInstance();

        cal.setTime(period_2001_03.middle());
        assertEquals(2001, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(23, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));

        cal.setTime(period_2001_03.plus(1).middle());
        assertEquals(2001, cal.get(Calendar.YEAR));
        assertEquals(Calendar.APRIL, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test
    public void testSetDate() {
        period_2001_03.set(DATE_2001_01_10);
        assertEquals(2001, period_2001_03.getYear());
        assertEquals(0, period_2001_03.getPosition());
        period_2001_03.set(DATE_2001_03_10);
        assertEquals(2001, period_2001_03.getYear());
        assertEquals(2, period_2001_03.getPosition());
    }

    @Test
    public void testSetDay() {
        period_2001_03.set(DAY_2001_01_10);
        assertEquals(2001, period_2001_03.getYear());
        assertEquals(0, period_2001_03.getPosition());
        period_2001_03.set(DAY_2001_03_10);
        assertEquals(2001, period_2001_03.getYear());
        assertEquals(2, period_2001_03.getPosition());
    }
    
    @Test
    public void testFirstLast(){
        TsPeriod p=new TsPeriod(TsFrequency.HalfYearly, period_2001_03);
        assertEquals(p.firstPeriod(TsFrequency.Monthly), new TsPeriod(TsFrequency.Monthly, p.firstday()));
        assertEquals(p.lastPeriod(TsFrequency.Monthly), new TsPeriod(TsFrequency.Monthly, p.lastday()));
        p.move(1);
        assertEquals(p.firstPeriod(TsFrequency.Monthly), new TsPeriod(TsFrequency.Monthly, p.firstday()));
        assertEquals(p.lastPeriod(TsFrequency.Monthly), new TsPeriod(TsFrequency.Monthly, p.lastday()));
    }
    
    @Test
    public void testNewFreq(){
        TsPeriod p=new TsPeriod(TsFrequency.Monthly, 1945, 0);
        TsPeriod P=new TsPeriod(TsFrequency.Quarterly, p);
        assertTrue(P.contains(p.firstday()));
        p=new TsPeriod(TsFrequency.Monthly, 1945, 1);
        P=new TsPeriod(TsFrequency.Quarterly, p);
        assertTrue(P.contains(p.firstday()));
        p=new TsPeriod(TsFrequency.Monthly, 1945, 2);
        P=new TsPeriod(TsFrequency.Quarterly, p);
        assertTrue(P.contains(p.firstday()));
    }
}
