/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.timeseries.simplets;

import demetra.data.DoubleSeq;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.util.IntList;
import jdplus.data.interpolation.ConstInterpolator;
import jdplus.data.transformation.LogJacobian;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class LengthOfPeriodTransformationTest {

    public LengthOfPeriodTransformationTest() {
    }

    @Test
    public void testMonthly() {
        TsPeriod start = TsPeriod.monthly(1980, 1);
        double[] data = new double[120];
        data[0] = data[1] = data[33] = data[35] = Double.NaN;
        ConstInterpolator interpolator = new ConstInterpolator(0);
        IntList missing = new IntList();
        double[] datac = interpolator.interpolate(DoubleSeq.of(data), missing);
        TsData s = TsData.ofInternal(start, data);
        TsData sc = TsData.ofInternal(start, data);
        LengthOfPeriodTransformation t = new LengthOfPeriodTransformation(LengthOfPeriodType.LeapYear);
        LogJacobian lj = new LogJacobian(3, 144, null);
        LogJacobian ljc = new LogJacobian(3, 120, missing.toArray());
        t.transform(s, lj);
        t.transform(sc, ljc);
        assertEquals(lj.value, ljc.value, 1e-9);
        t.converse().transform(s, lj);
        assertEquals(lj.value, 0, 1e-9);
        t.converse().transform(sc, ljc);
        assertEquals(ljc.value, 0, 1e-9);
        t = new LengthOfPeriodTransformation(LengthOfPeriodType.LengthOfPeriod);
        lj = new LogJacobian(3, 120, null);
        ljc = new LogJacobian(3, 120, missing.toArray());
        t.transform(s, lj);
        t.transform(sc, ljc);
        assertEquals(lj.value, ljc.value, 1e-9);
        t.converse().transform(s, lj);
        assertEquals(lj.value, 0, 1e-9);
        t.converse().transform(sc, ljc);
        assertEquals(ljc.value, 0, 1e-9);
    }

    @Test
    public void testMonthlyNoMissing() {
        TsPeriod start = TsPeriod.monthly(1980, 1);
        double[] data = new double[120];
        ConstInterpolator interpolator = new ConstInterpolator(0);
        IntList missing = new IntList();
        double[] datac = interpolator.interpolate(DoubleSeq.of(data), missing);
        TsData s = TsData.ofInternal(start, data);
        TsData sc = TsData.ofInternal(start, data);
        LengthOfPeriodTransformation t = new LengthOfPeriodTransformation(LengthOfPeriodType.LeapYear);
        LogJacobian lj = new LogJacobian(3, 144, null);
        LogJacobian ljc = new LogJacobian(3, 120, missing.toArray());
        t.transform(s, lj);
        t.transform(sc, ljc);
        assertEquals(lj.value, ljc.value, 1e-9);
        t.converse().transform(s, lj);
        assertEquals(lj.value, 0, 1e-9);
        t.converse().transform(sc, ljc);
        assertEquals(ljc.value, 0, 1e-9);
        t = new LengthOfPeriodTransformation(LengthOfPeriodType.LengthOfPeriod);
        lj = new LogJacobian(3, 120, null);
        ljc = new LogJacobian(3, 120, missing.toArray());
        t.transform(s, lj);
        t.transform(sc, ljc);
        assertEquals(lj.value, ljc.value, 1e-9);
        t.converse().transform(s, lj);
        assertEquals(lj.value, 0, 1e-9);
        t.converse().transform(sc, ljc);
        assertEquals(ljc.value, 0, 1e-9);
    }

    @Test
    public void testQuarterly() {
        TsPeriod start = TsPeriod.quarterly(1980, 1);
        double[] data = new double[120];
        data[0] = data[4] = data[5] = data[33] = data[36] = Double.NaN;
        ConstInterpolator interpolator = new ConstInterpolator(0);
        IntList missing = new IntList();
        double[] datac = interpolator.interpolate(DoubleSeq.of(data), missing);
        TsData s = TsData.ofInternal(start, data);
        TsData sc = TsData.ofInternal(start, data);
        LengthOfPeriodTransformation t = new LengthOfPeriodTransformation(LengthOfPeriodType.LeapYear);
        LogJacobian lj = new LogJacobian(3, 144, null);
        LogJacobian ljc = new LogJacobian(3, 120, missing.toArray());
        t.transform(s, lj);
        t.transform(sc, ljc);
        assertEquals(lj.value, ljc.value, 1e-9);
        t.converse().transform(s, lj);
        assertEquals(lj.value, 0, 1e-9);
        t.converse().transform(sc, ljc);
        assertEquals(ljc.value, 0, 1e-9);
        t = new LengthOfPeriodTransformation(LengthOfPeriodType.LengthOfPeriod);
        lj = new LogJacobian(3, 120, null);
        ljc = new LogJacobian(3, 120, missing.toArray());
        t.transform(s, lj);
        t.transform(sc, ljc);
        assertEquals(lj.value, ljc.value, 1e-9);
        t.converse().transform(s, lj);
        assertEquals(lj.value, 0, 1e-9);
        t.converse().transform(sc, ljc);
        assertEquals(ljc.value, 0, 1e-9);
    }
}
