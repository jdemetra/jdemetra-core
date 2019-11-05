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
package jdplus.modelling.regression;

import demetra.timeseries.regression.AdditiveOutlier;
import jdplus.modelling.regression.Regression;
import jdplus.data.DataBlock;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsPeriod;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;
import jdplus.maths.matrices.FastMatrix;

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
        TsDomain domain = TsDomain.of(TsPeriod.monthly(2000, 1), 100);
        AdditiveOutlier ao = new AdditiveOutlier(domain.get(pos).start());
        FastMatrix M = Regression.matrix(domain, ao);
        DataBlock buffer = M.column(0);
        assertTrue(buffer.indexOf(x -> x != 0) == pos);
        assertTrue(buffer.lastIndexOf(x -> x == 1) == pos);
//        System.out.println(ao.getDescription(domain));
    }

    @Test
    public void testWeek() {
        final int pos = 25;
        TsDomain weeks = TsDomain.of(TsPeriod.of(TsUnit.of(7, ChronoUnit.DAYS), LocalDate.now()), 100);
        AdditiveOutlier ao = new AdditiveOutlier(weeks.get(pos).start());
        DataBlock buffer = Regression.x(weeks, ao);
        assertTrue(buffer.indexOf(x -> x != 0) == pos);
        assertTrue(buffer.lastIndexOf(x -> x == 1) == pos);
//        System.out.println(ao.getDescription(weeks));
    }

    @Test
    public void testDay() {
        final int pos = 25;
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 100);
        AdditiveOutlier ao = new AdditiveOutlier(days.get(pos).start());
        DataBlock buffer = Regression.x(days, ao);
        assertTrue(buffer.indexOf(x -> x != 0) == pos);
        assertTrue(buffer.lastIndexOf(x -> x == 1) == pos);
//        System.out.println(ao.getDescription(days));
    }

    @Test
    public void testInside() {
        final int pos = 25;
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 100);
        AdditiveOutlier ao = new AdditiveOutlier(days.get(pos).start());
        DataBlock buffer = Regression.x(days, ao);
        assertEquals(1, buffer.sum(), 1e-9);
    }

    @Test
    public void testBefore() {
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 100);
        for (int i = 1; i < 3; ++i) {
            AdditiveOutlier ao = new AdditiveOutlier(days.get(0).plus(-i).start());
            DataBlock buffer = Regression.x(days, ao);
            assertEquals(0, buffer.sum(), 1e-9);
        }
    }

    @Test
    public void testAfter() {
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 100);
        for (int i = 1; i < 3; ++i) {
            AdditiveOutlier ao = new AdditiveOutlier(days.get(99).plus(i).start());
            DataBlock buffer = Regression.x(days, ao);
            assertEquals(0, buffer.sum(), 1e-9);
            buffer.set(0);
        }
    }

}
