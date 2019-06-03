/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.data.transformation.LogJacobian;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import jdplus.timeseries.simplets.TsDataTransformation;
import demetra.timeseries.calendars.LengthOfPeriodType;
import jdplus.timeseries.simplets.Transformations;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;
import demetra.data.Doubles;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class LengthOfPeriodTransformationTest {
    
    public LengthOfPeriodTransformationTest() {
    }

    @Test
    public void testLengthOfPeriod() {
        DoubleSeq data=DoubleSeq.onMapping(300, i->1);
        TsPeriod start=TsPeriod.monthly(1980, 3);
        TsDataTransformation lp=Transformations.lengthOfPeriod(LengthOfPeriodType.LengthOfPeriod);
        LogJacobian lj=new LogJacobian(0, data.length());
        TsData s=TsData.of(start, Doubles.of(data));
        TsData s1=lp.transform(s, lj);
        TsData s2=lp.converse().transform(s1, lj);
        assertTrue(s2.getValues().allMatch(x->Math.abs(x-1)<1e-12));
        assertEquals(lj.value, 0, 1e-12);
    }
    
    @Test
    public void testLengthOfPeriodQ() {
        DoubleSeq data=DoubleSeq.onMapping(300, i->1);
        TsPeriod start=TsPeriod.quarterly(1980, 2);
        TsDataTransformation lp=Transformations.lengthOfPeriod(LengthOfPeriodType.LengthOfPeriod);
        LogJacobian lj=new LogJacobian(0, data.length());
        TsData s=TsData.of(start, Doubles.of(data));
        TsData s1=lp.transform(s, lj);
        TsData s2=lp.converse().transform(s1, lj);
        assertTrue(s2.getValues().allMatch(x->Math.abs(x-1)<1e-12));
        assertEquals(lj.value, 0, 1e-12);
    }

    @Test
    public void testLeapYear() {
        DoubleSeq data=DoubleSeq.onMapping(80, i->1);
        TsPeriod start=TsPeriod.quarterly(1980, 3);
        TsDataTransformation lp=Transformations.lengthOfPeriod(LengthOfPeriodType.LengthOfPeriod);
        LogJacobian lj=new LogJacobian(0, data.length());
        TsData s=TsData.of(start, Doubles.of(data));
        TsData s1=lp.transform(s, lj);
        TsData s2=lp.converse().transform(s1, lj);
        assertTrue(s2.getValues().allMatch(x->Math.abs(x-1)<1e-12));
        assertEquals(lj.value, 0, 1e-12);
    }
}
