/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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

import demetra.data.DoubleValues;
import internal.Demo;
import demetra.timeseries.ITimeSeries;
import java.time.LocalDate;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import internal.Tripwire;

/**
 *
 * @author Jean Palate
 */
public class TsDataTypeTest {

    @Demo
    public static void main(String[] args) {
        System.setProperty(Tripwire.TRIPWIRE_PROPERTY, Boolean.TRUE.toString());

        TsDataType ts = TsDataType.of(TsPeriod.year(2001), DoubleValues.ofInternal(3.14, 7));

        System.out.println("\n[Tests ...]");
        System.out.println(ts.toString());
        System.out.println(ts.getDomain());
        System.out.println(ts.getValues());

        System.out.println("\n[Test for]");
        for (int i = 0; i < ts.length(); i++) {
            System.out.println(ts.get(i));
        }

        System.out.println("\n[Test forEach]");
        ts.forEach(o -> System.out.println(o));

        System.out.println("\n[Test iterator]");
        for (TsObservation o : ts) {
            System.out.println(o);
        }

        System.out.println("\n[Test stream]");
        ts.stream()
                .filter(o -> o.getPeriod().isAfter(LocalDate.of(2001, 1, 1)))
                .forEach(System.out::println);

        System.out.println("\n[Test forEach(k, v)]");
        ts.forEach((k, v) -> System.out.println(k + ":" + v + " "));

        System.out.println("\n[Test getPeriod / getDoubleValue]");
        for (int i = 0; i < ts.length(); i++) {
            System.out.println(ts.getPeriod(i) + " -> " + ts.getDoubleValue(i));
        }

        System.out.println("\n[Test ITimeSeries]");
        {
            ITimeSeries<?, ?, ?> x = ts;
            for (int i = 0; i < x.length(); i++) {
                System.out.println(x.getPeriod(i).start() + " -> " + x.getValue(i).doubleValue());
            }
        }

        System.out.println("\n[Test ITimeSeries.OfDouble]");
        {
            ITimeSeries.OfDouble<?, ?> y = ts;
            for (int i = 0; i < y.length(); i++) {
                System.out.println(y.getPeriod(i).start() + " -> " + y.getDoubleValue(i));
            }
        }
    }

    @Test
    public void testEquals() {
        assertThat(TsDataType.of(TsPeriod.year(2001), DoubleValues.ofInternal(1, 2, 3)))
                .isEqualTo(TsDataType.of(TsPeriod.year(2001), DoubleValues.ofInternal(1, 2, 3)));
    }

    @Test
    public void testRandom() {
        TsDataType random = TsDataType.random(TsFrequency.Monthly, 0);
        assertTrue(random.getDomain().length() == random.getValues().length());
        assertTrue(random.getValues().allMatch(x -> x >= 100));
    }

}
