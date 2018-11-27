/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.data.DoubleSequence;
import demetra.data.transformation.LogJacobian;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.simplets.TsDataTransformation;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.simplets.Transformations;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class LengthOfPeriodTransformationTest {
    
    public LengthOfPeriodTransformationTest() {
    }

    @Test
    public void testLengthOfPeriod() {
        DoubleSequence data=DoubleSequence.onMapping(300, i->1);
        TsPeriod start=TsPeriod.monthly(1980, 3);
        TsDataTransformation lp=Transformations.lengthOfPeriod(LengthOfPeriodType.LengthOfPeriod);
        LogJacobian lj=new LogJacobian(0, data.length());
        TsData s=TsData.of(start, data);
        TsData s1=lp.transform(s, lj);
        TsData s2=lp.converse().transform(s1, lj);
        assertTrue(s2.getValues().allMatch(x->Math.abs(x-1)<1e-12));
        assertEquals(lj.value, 0, 1e-12);
    }
    
    @Test
    public void testLengthOfPeriodQ() {
        DoubleSequence data=DoubleSequence.onMapping(300, i->1);
        TsPeriod start=TsPeriod.quarterly(1980, 2);
        TsDataTransformation lp=Transformations.lengthOfPeriod(LengthOfPeriodType.LengthOfPeriod);
        LogJacobian lj=new LogJacobian(0, data.length());
        TsData s=TsData.of(start, data);
        TsData s1=lp.transform(s, lj);
        TsData s2=lp.converse().transform(s1, lj);
        assertTrue(s2.getValues().allMatch(x->Math.abs(x-1)<1e-12));
        assertEquals(lj.value, 0, 1e-12);
    }

    @Test
    public void testLeapYear() {
        DoubleSequence data=DoubleSequence.onMapping(80, i->1);
        TsPeriod start=TsPeriod.quarterly(1980, 3);
        TsDataTransformation lp=Transformations.lengthOfPeriod(LengthOfPeriodType.LengthOfPeriod);
        LogJacobian lj=new LogJacobian(0, data.length());
        TsData s=TsData.of(start, data);
        TsData s1=lp.transform(s, lj);
        TsData s2=lp.converse().transform(s1, lj);
        assertTrue(s2.getValues().allMatch(x->Math.abs(x-1)<1e-12));
        assertEquals(lj.value, 0, 1e-12);
    }
}
