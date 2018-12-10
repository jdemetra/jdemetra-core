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
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsPeriod;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class LevelShiftTest {

    public LevelShiftTest() {
    }

    @Test
    public void testInside() {
        final int pos = 25;
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 100);
        LevelShift ls = new LevelShift(days.get(pos).start(), true);
        DataBlock buffer = Regression.x(days, ls);
        assertEquals(-pos, buffer.sum(), 1e-9);
    }

    @Test
    public void testInside99() {
        final int pos = 99;
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 100);
        LevelShift ls = new LevelShift(days.get(pos).start(), true);
        DataBlock buffer = Regression.x(days, ls);
        assertEquals(-pos, buffer.sum(), 1e-9);
    }

    @Test
    public void testInside0() {
        final int pos = 0;
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 100);
        LevelShift ls = new LevelShift(days.get(pos).start(), true);
        DataBlock buffer = Regression.x(days, ls);
        assertEquals(-pos, buffer.sum(), 1e-9);
    }

    @Test
    public void testBefore() {
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 100);
        for (int i = 1; i < 3; ++i) {
            LevelShift ls = new LevelShift(days.get(0).plus(-i).start(), true);
            DataBlock buffer = Regression.x(days, ls);
            assertEquals(0, buffer.sum(), 1e-9);
        }
    }

    @Test
    public void testAfter() {
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 100);
        for (int i = 1; i < 3; ++i) {
            LevelShift ls = new LevelShift(days.get(99).plus(i).start(), true);
            DataBlock buffer = Regression.x(days, ls);
            assertEquals(100, -buffer.sum(), 1e-9);
        }
    }

    @Test
    public void testInside2() {
        final int pos = 25;
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 100);
        LevelShift ls = new LevelShift(days.get(pos).start(), false);
        DataBlock buffer = Regression.x(days, ls);
        assertEquals(buffer.length() - pos, buffer.sum(), 1e-9);
    }

    @Test
    public void testInside299() {
        final int pos = 99;
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 100);
        LevelShift ls = new LevelShift(days.get(pos).start(), false);
        DataBlock buffer = Regression.x(days, ls);
        assertEquals(buffer.length() - pos, buffer.sum(), 1e-9);
    }

    @Test
    public void testInside20() {
        final int pos = 0;
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 100);
        LevelShift ls = new LevelShift(days.get(pos).start(), false);
        DataBlock buffer = Regression.x(days, ls);
        assertEquals(buffer.length() - pos, buffer.sum(), 1e-9);
    }

    @Test
    public void testBefore2() {
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 100);
        for (int i = 1; i < 3; ++i) {
            LevelShift ls = new LevelShift(days.get(0).plus(-i).start(), false);
            DataBlock buffer = Regression.x(days, ls);
            assertEquals(buffer.length(), buffer.sum(), 1e-9);
        }
    }

    @Test
    public void testAfter2() {
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 100);
        for (int i = 1; i < 3; ++i) {
            LevelShift ls = new LevelShift(days.get(99).plus(i).start(), false);
            DataBlock buffer = Regression.x(days, ls);
            assertEquals(0, -buffer.sum(), 1e-9);
            buffer.set(0);
        }
    }

}
