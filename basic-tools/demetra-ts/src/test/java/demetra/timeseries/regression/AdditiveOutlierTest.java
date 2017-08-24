/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.timeseries.regression;

import demetra.data.DataBlock;
import demetra.timeseries.RegularDomain;
import demetra.timeseries.TsFrequency;
import demetra.timeseries.TsPeriod;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class AdditiveOutlierTest {

    public AdditiveOutlierTest() {
    }

    @Test
    public void testSimple() {
        final int pos = 25;
        DataBlock buffer = DataBlock.make(100);
        RegularDomain domain = RegularDomain.of(TsPeriod.monthly(2000, 1), 100);
        AdditiveOutlier ao = new AdditiveOutlier(domain.get(pos).start());
        ao.data(domain.getStartPeriod(), buffer);
        assertTrue(buffer.indexOf(x -> x != 0) == pos);
        assertTrue(buffer.lastIndexOf(x -> x == 1) == pos);
//        System.out.println(ao.getDescription(domain));
    }

    @Test
    public void testWeek() {
        final int pos = 25;
        DataBlock buffer = DataBlock.make(100);
        RegularDomain weeks = RegularDomain.of(TsPeriod.of(TsFrequency.of(7, ChronoUnit.DAYS), LocalDate.now()), buffer.length());
        AdditiveOutlier ao = new AdditiveOutlier(weeks.get(pos).start());
        ao.data(weeks.getStartPeriod(), buffer);
        assertTrue(buffer.indexOf(x -> x != 0) == pos);
        assertTrue(buffer.lastIndexOf(x -> x == 1) == pos);
//        System.out.println(ao.getDescription(weeks));
    }

    @Test
    public void testDay() {
        final int pos = 25;
        DataBlock buffer = DataBlock.make(100);
        RegularDomain days = RegularDomain.of(TsPeriod.of(TsFrequency.DAILY, LocalDate.now()), buffer.length());
        AdditiveOutlier ao = new AdditiveOutlier(days.get(pos).start());
        ao.data(days.getStartPeriod(), buffer);
        assertTrue(buffer.indexOf(x -> x != 0) == pos);
        assertTrue(buffer.lastIndexOf(x -> x == 1) == pos);
//        System.out.println(ao.getDescription(days));
    }

    @Test
    public void testInside() {
        final int pos = 25;
        DataBlock buffer = DataBlock.make(100);
        RegularDomain days = RegularDomain.of(TsPeriod.of(TsFrequency.DAILY, LocalDate.now()), buffer.length());
        AdditiveOutlier ao = new AdditiveOutlier(days.get(pos).start());
        ao.data(days.getStartPeriod(), buffer);
        assertEquals(1, buffer.sum(), 1e-9);
    }

    @Test
    public void testBefore() {
        DataBlock buffer = DataBlock.make(100);
        RegularDomain days = RegularDomain.of(TsPeriod.of(TsFrequency.DAILY, LocalDate.now()), buffer.length());
        for (int i = 1; i < 3; ++i) {
            AdditiveOutlier ao = new AdditiveOutlier(days.get(0).plus(-i).start());
            ao.data(days.getStartPeriod(), buffer);
            assertEquals(0, buffer.sum(), 1e-9);
            buffer.set(0);
        }
    }

    @Test
    public void testAfter() {
        DataBlock buffer = DataBlock.make(100);
        RegularDomain days = RegularDomain.of(TsPeriod.of(TsFrequency.DAILY, LocalDate.now()), buffer.length());
        for (int i = 1; i < 3; ++i) {
            AdditiveOutlier ao = new AdditiveOutlier(days.get(99).plus(i).start());
            ao.data(days.getStartPeriod(), buffer);
            assertEquals(0, buffer.sum(), 1e-9);
            buffer.set(0);
        }
    }

}
