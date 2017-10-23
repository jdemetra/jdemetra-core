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
package demetra.timeseries.calendars;

import demetra.timeseries.TsUnit;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class FixedDayTest {

    public FixedDayTest() {
    }

    @Test
    public void test1() {
        FixedDay fd = new FixedDay(7, 21);
        Iterable<IHolidayInfo> iterable = fd.getIterable(TsUnit.MONTHLY, LocalDate.now(), LocalDate.now().plus(3, ChronoUnit.YEARS));
        Stream<IHolidayInfo> stream = StreamSupport.stream(iterable.spliterator(), false);
        assertTrue(stream.count() == 3);
        stream = StreamSupport.stream(iterable.spliterator(), false);
        assertTrue(stream.findFirst().get().getDay().getYear() == 2018);
    }

    @Test
    public void test2() {
        FixedDay fd = new FixedDay(7, 21);
        Iterable<IHolidayInfo> iterable = fd.getIterable(TsUnit.MONTHLY, LocalDate.of(2017, 7, 21), LocalDate.now().plus(3, ChronoUnit.YEARS));
        Stream<IHolidayInfo> stream = StreamSupport.stream(iterable.spliterator(), false);
        assertTrue(stream.count() == 4);
        stream = StreamSupport.stream(iterable.spliterator(), false);
        assertTrue(stream.findFirst().get().getDay().getYear() == 2017);
    }

    @Test
    public void test3() {
        FixedDay fd = new FixedDay(7, 21);
        Iterable<IHolidayInfo> iterable = fd.getIterable(TsUnit.MONTHLY, LocalDate.of(2017, 7, 21), LocalDate.of(2017, 7, 22));
        Stream<IHolidayInfo> stream = StreamSupport.stream(iterable.spliterator(), false);
        assertTrue(stream.count() == 1);
        stream = StreamSupport.stream(iterable.spliterator(), false);
        assertTrue(stream.findFirst().get().getDay().getYear() == 2017);
    }

   @Test
    public void testEmpty() {
        FixedDay fd = new FixedDay(7, 21);
        Iterable<IHolidayInfo> iterable = fd.getIterable(TsUnit.MONTHLY, LocalDate.of(2017, 7, 23), LocalDate.of(2018, 6, 22));
        Stream<IHolidayInfo> stream = StreamSupport.stream(iterable.spliterator(), false);
        assertTrue(stream.count() == 0);
        stream = StreamSupport.stream(iterable.spliterator(), false);
        assertTrue(!stream.findFirst().isPresent());
    }
}
