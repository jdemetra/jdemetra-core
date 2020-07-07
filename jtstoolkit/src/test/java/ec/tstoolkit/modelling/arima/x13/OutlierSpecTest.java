/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.regression.OutlierType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class OutlierSpecTest {

    public OutlierSpecTest() {
    }

    @Test
    public void testInformationSet() {
        OutlierSpec expected = new OutlierSpec();
        OutlierSpec actual = new OutlierSpec();
        InformationSet info;

        assertEquals(expected, actual);
        assertFalse(expected.isUsed());

        SingleOutlierSpec[] soSpecs;
        SingleOutlierSpec soSpec1 = new SingleOutlierSpec(OutlierType.AO);
        SingleOutlierSpec soSpec2 = new SingleOutlierSpec(OutlierType.LS);
        SingleOutlierSpec soSpec3 = new SingleOutlierSpec(OutlierType.TC, 1.2);
        SingleOutlierSpec soSpec4 = new SingleOutlierSpec(OutlierType.SLS, .5);
        soSpecs = new SingleOutlierSpec[]{soSpec1, soSpec2, soSpec3};
        
        expected.setTypes(soSpecs);
        assertEquals(3, expected.getTypesCount());
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(3, actual.getTypesCount());
        assertEquals(1.2, actual.getTypes()[2].getCriticalValue(),.0);
        
        expected.add(soSpec4);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(OutlierType.SLS, actual.getTypes()[3].getType());
        assertEquals(.5, actual.search(OutlierType.SLS).getCriticalValue(),.0);
        
        expected.remove(OutlierType.TC);
        assertNull(expected.search(OutlierType.TC));
        assertNotNull(actual.search(OutlierType.TC));
        info = expected.write(true);
        actual.read(info);
        assertNull(actual.search(OutlierType.TC));
        
        expected.setLSRun(2);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(2, actual.getLSRun());
        
        expected.setMaxIter(2);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(2, actual.getMaxIter());
        
        expected.setMethod(OutlierSpec.Method.AddOne);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(OutlierSpec.Method.AddOne, actual.getMethod());
        
        TsPeriodSelector tsPeriodSelector = new TsPeriodSelector();
        tsPeriodSelector.first(5);
        expected.setSpan(tsPeriodSelector);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(tsPeriodSelector, actual.getSpan());
        
        expected.setMonthlyTCRate(0.5);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(0.5, actual.getMonthlyTCRate(),.0);

        expected.setDefaultCriticalValue(2.1);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(2.1, actual.getDefaultCriticalValue(),.0);
        assertEquals(2.1, actual.search(OutlierType.SLS).getCriticalValue(),.0);

        expected.reset();
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertFalse(actual.isUsed());
    }

}
