/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.timeseries.simplets;

import demetra.data.Data;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.data.Doubles;
import static demetra.data.Doubles.average;
import demetra.timeseries.CalendarTsData;
import demetra.timeseries.IDateDomain;
import static demetra.timeseries.simplets.CalendarTsDataToolkit.add;
import static demetra.timeseries.simplets.CalendarTsDataToolkit.delta;
import static demetra.timeseries.simplets.CalendarTsDataToolkit.drop;
import static demetra.timeseries.simplets.CalendarTsDataToolkit.fastFn;
import static demetra.timeseries.simplets.CalendarTsDataToolkit.fn;
import static demetra.timeseries.simplets.CalendarTsDataToolkit.log;
import static demetra.timeseries.simplets.CalendarTsDataToolkit.normalize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.Assert.*;
import org.junit.Test;
import static demetra.timeseries.simplets.CalendarTsDataToolkit.add;
import static demetra.timeseries.simplets.CalendarTsDataToolkit.drop;
import static demetra.timeseries.simplets.CalendarTsDataToolkit.fn;

/**
 *
 * @author Mats Maggi
 */
public class CalendarTsDataToolkitTest {

    static CalendarTsData continuous = Data.DAILY_CONTINUOUS;

    @Test
    public void testUnaryOperator() {
        CalendarTsData t1 = fn(continuous, x -> Math.log(x));
        CalendarTsData t2 = fastFn(continuous, x -> Math.log(x));
        CalendarTsData t3 = log(continuous);
        assertThat(Doubles.distance(t1.values(), t2.values())).isZero();
        assertThat(Doubles.distance(t1.values(), t3.values())).isZero();
    }

    @Test
    public void testDropContinuous() {
        IDateDomain dom = drop(continuous.domain(), 0, 0);
        assertEquals(continuous.domain(), dom);

        dom = drop(continuous.domain(), 5, 0);
        assertThat(dom.length()).isEqualTo(continuous.domain().length() - 5);
        assertThat(dom.getEnd()).isEqualTo(continuous.domain().getEnd());
        assertThat(dom.getStart()).isEqualTo(continuous.domain().getStart().plus(5));

        dom = drop(continuous.domain(), 0, 3);
        assertThat(dom.getEnd()).isEqualTo(continuous.domain().getEnd().plus(-3));
        assertThat(dom.getStart()).isEqualTo(continuous.domain().getStart());
        assertThat(dom.length()).isEqualTo(continuous.domain().length() - 3);

        dom = drop(continuous.domain(), continuous.length() + 1, 0);
        assertTrue(dom.length() == 0);
    }

    @Test
    public void testNormalize() {
        CalendarTsData normalized = normalize(continuous);
        double[] data = normalized.values().toArray();
        DataBlock values = DataBlock.ofInternal(data);
        assertThat(values.average()).isCloseTo(0, within(1E-10));
    }

    @Test
    public void stressTest() {
        int K = 2000000;
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            CalendarTsData s = delta(normalize(log(add(continuous, drop(continuous, 20, 50)))), 12);
            DoubleSequence values = DoubleSequence.of(s.values());
            double v = average(values);
            if (k == 0) {
                System.out.println(v);
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
    
    @Test
    public void testAdd() {
        CalendarTsData after = add(continuous, 20);
        assertThat(average(after.values())).isCloseTo(20 + average(continuous.values()), within(1E-10));
    }
}
