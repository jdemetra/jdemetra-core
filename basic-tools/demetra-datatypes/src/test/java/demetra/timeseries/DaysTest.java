/*
 * Copyright 2017 National Bank create Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions create the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy create the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.timeseries;

import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class DaysTest {

    @Test
    public void testFactory() {
        LocalDate start = LocalDate.of(2017, 1, 1);
        LocalDate end = LocalDate.of(2017, 1, 31);
        Days d1 = Days.of(start, end);
        Days d2 = Days.of(start, 31);
        assertTrue(d1.length() == d2.length());
    }

    @Test
    public void testSearch() {
        LocalDate start = LocalDate.of(2017, 1, 1);
        Days d = Days.of(start, 59);
        for (int i = 0; i < d.length(); ++i) {
            assertTrue(d.search(d.get(i).firstDay()) == i);
        }
        assertTrue(d.search(LocalDate.of(2016, 12, 31)) == -1);
        assertTrue(d.search(LocalDate.of(2017, 3, 1)) == -d.length());
        for (int i = 0; i < d.length(); ++i) {
            assertTrue(d.search(d.get(i).firstDay().atStartOfDay().plusMinutes(655)) == i);
        }
        assertTrue(d.search(LocalDate.of(2016, 12, 31).atStartOfDay().plusMinutes(655)) == -1);
        assertTrue(d.search(LocalDate.of(2017, 3, 1).atStartOfDay().plusMinutes(655)) == -d.length());
    }
    
    @Test
    public void testStartEnd() {
        LocalDate start = LocalDate.of(2017, 1, 1);
        LocalDate end = LocalDate.of(2017, 1, 31);
        Days days = Days.of(start, end);
        assertThat(days.getStart()).isEqualTo(Day.of(start));
        assertThat(days.getLast()).isEqualTo(Day.of(end));
        assertThat(days.getEnd()).isEqualTo(Day.of(end).plus(1));
        assertThat(days.length()).isEqualTo(31);
    }
    
    @Test
    public void testIntersection() {
        Days domain1 = Days.of(LocalDate.of(2017, 1, 1), LocalDate.of(2017, 2, 1));
        Days domain2 = Days.of(LocalDate.of(2017, 1, 10), LocalDate.of(2017, 1, 15));
        assertThat(domain1.intersection(domain2)).isEqualTo(domain2);
    }
    
    @Test
    public void testNoIntersection() {
        Days domain1 = Days.of(LocalDate.of(2010, 1, 1), LocalDate.of(2010, 2, 1));
        Days domain2 = Days.of(LocalDate.of(2017, 1, 10), LocalDate.of(2017, 1, 15));
        assertThat(domain1.intersection(domain2).length()).isEqualTo(0);
    }
    
    @Test
    public void testIntersection2() {
        Days domain1 = Days.of(LocalDate.of(2010, 4, 5), LocalDate.of(2010, 8, 10));
        Days domain2 = Days.of(LocalDate.of(2010, 8, 4), LocalDate.of(2010, 12, 15));
        assertThat(domain1.intersection(domain2)).isEqualTo(Days.of(LocalDate.of(2010, 8, 4), LocalDate.of(2010, 8, 10)));
    }
    
    @Test
    public void testUnion1() {
        Days domain1 = Days.of(LocalDate.of(2010, 4, 5), LocalDate.of(2010, 8, 10));
        Days domain2 = Days.of(LocalDate.of(2010, 8, 4), LocalDate.of(2010, 12, 15));
        assertThat(domain1.union(domain2)).isEqualTo(Days.of(domain1.getStart().firstDay(), domain2.getLast().firstDay()));
    }
    
    @Test
    public void testUnion2() {
        Days domain1 = Days.of(LocalDate.of(2010, 1, 1), LocalDate.of(2015, 1, 1));
        Days domain2 = Days.of(LocalDate.of(2013, 1, 1), LocalDate.of(2014, 1, 1));
        assertThat(domain1.union(domain2)).isEqualTo(domain1);
        assertThat(domain2.union(domain1)).isEqualTo(domain1);
    }
    
    @Test
    public void testUnion3() {
        Days domain1 = Days.of(LocalDate.of(2010, 1, 1), 0);
        Days domain2 = Days.of(LocalDate.of(2013, 1, 1), LocalDate.of(2014, 1, 1));
        assertThat(domain1.union(domain2)).isEqualTo(domain2);
        assertThat(domain2.union(domain1)).isEqualTo(domain2);
    }
    
    @Test
    public void testLag() {
        Days d = Days.of(LocalDate.of(2017, 1, 1), 5);
        d = d.move(1);
        assertThat(d.getStart().firstDay()).isEqualTo(LocalDate.of(2017, 1, 2));
        assertThat(d.getEnd().firstDay()).isEqualTo(LocalDate.of(2017, 1, 7));
        d = d.move(-2);
        assertThat(d.getStart().firstDay()).isEqualTo(LocalDate.of(2016, 12, 31));
        assertThat(d.getLast().firstDay()).isEqualTo(LocalDate.of(2017, 1, 4));
        assertThat(d.getEnd().firstDay()).isEqualTo(LocalDate.of(2017, 1, 5));
    }
    
    @Test
    public void testSelectAll() {
        Days d = Days.of(LocalDate.of(2017, 1, 1), 31);
        assertThat(d.select(TsPeriodSelector.all())).isEqualTo(d);
    }
    
    @Test
    public void testSelectFrom() {
        Days d = Days.of(LocalDate.of(2017, 1, 1), 31);
        assertThat(d.select(TsPeriodSelector.from(LocalDate.of(2017, 1, 1).atStartOfDay()))).isEqualTo(d);
        assertThat(d.select(TsPeriodSelector.from(LocalDate.of(2010, 1, 1).atStartOfDay()))).isEqualTo(d);
        assertThat(d.select(TsPeriodSelector.from(LocalDate.of(2024, 1, 1).atStartOfDay())).length()).isZero();
    }
    
    @Test
    public void testSelectTo() {
        Days d = Days.of(LocalDate.of(2017, 1, 1), 31);
        assertThat(d.select(TsPeriodSelector.to(LocalDate.of(2017, 2, 1).atStartOfDay()))).isEqualTo(d);
        assertThat(d.select(TsPeriodSelector.to(LocalDate.of(2015, 1, 1).atStartOfDay())).length()).isZero();
        assertThat(d.select(TsPeriodSelector.to(LocalDate.of(2020, 1, 1).atStartOfDay()))).isEqualTo(d);
    }
    
    @Test
    public void testSelectExcluding() {
        Days d = Days.of(LocalDate.of(2017, 1, 1), 31);
        assertThat(d.select(TsPeriodSelector.excluding(0,4))).isEqualTo(Days.of(LocalDate.of(2017,1,1), 27));
        assertThat(d.select(TsPeriodSelector.excluding(10,0))).isEqualTo(Days.of(LocalDate.of(2017,1,11), 21));
        assertThat(d.select(TsPeriodSelector.excluding(31,0)).length()).isZero();
        assertThat(d.select(TsPeriodSelector.excluding(0,31)).length()).isZero();
    }
    
    @Test
    public void testSelectBetween() {
        Days d = Days.of(LocalDate.of(2017, 1, 1), 31);
        assertThat(d.select(TsPeriodSelector.between(LocalDate.of(2017, 1, 1).atStartOfDay(), 
                LocalDate.of(2017, 2, 1).atStartOfDay()))).isEqualTo(d);
        assertThat(d.select(TsPeriodSelector.between(LocalDate.of(2017, 1, 5).atStartOfDay(), 
                LocalDate.of(2017, 2, 1).atStartOfDay())).length()).isEqualTo(27);
        assertThat(d.select(TsPeriodSelector.between(LocalDate.of(2010, 1, 1).atStartOfDay(), 
                LocalDate.of(2017, 2, 1).atStartOfDay()))).isEqualTo(d);
    }
}
