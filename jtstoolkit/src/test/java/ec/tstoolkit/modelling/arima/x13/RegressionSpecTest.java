/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.Ramp;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class RegressionSpecTest {
    
    public RegressionSpecTest() {
    }
    
    @Test
    public void testInformationSet() {
        RegressionSpec expected = new RegressionSpec();
        RegressionSpec actual = new RegressionSpec();
        InformationSet info;
        assertEquals(expected, actual);
        
        TradingDaysSpec tradDay = new TradingDaysSpec();
        tradDay.setTradingDaysType(TradingDaysType.WorkingDays);
        expected.setTradingDays(tradDay);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(TradingDaysType.WorkingDays, actual.getTradingDays().getTradingDaysType());
        
        MovingHolidaySpec moHoliday = new MovingHolidaySpec();
        moHoliday.setType(MovingHolidaySpec.Type.Easter);
        MovingHolidaySpec[] moHolidays = new MovingHolidaySpec[]{moHoliday};
        expected.setMovingHolidays(moHolidays);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(MovingHolidaySpec.Type.Easter, actual.getMovingHolidays()[0].getType());
        assertEquals(MovingHolidaySpec.Type.Easter, actual.getEaster().getType());
        actual.removeMovingHolidays(moHoliday);
        assertTrue(actual.getMovingHolidays().length == 0);
        expected.clearMovingHolidays();
        assertTrue(expected.getMovingHolidays().length == 0);
        
        OutlierDefinition outDef = new OutlierDefinition(Day.BEG, OutlierType.AO);
        OutlierDefinition[] outliers_ = new OutlierDefinition[]{outDef};
        expected.setOutliers(outliers_);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(1, actual.getOutliers().length);
        assertEquals(OutlierType.AO, actual.getOutliers()[0].getType());
        
        InterventionVariable intvar = new InterventionVariable();
        intvar.setDelta(1.0);
        InterventionVariable[] intvars = new InterventionVariable[]{intvar};
        expected.setInterventionVariables(intvars);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(1, actual.getInterventionVariablesCount());
        assertEquals(1, actual.getInterventionVariables().length);
        assertEquals(1.0, actual.getInterventionVariables()[0].getDelta(), 0.0);
        
        Ramp rampvar = new Ramp();
        Ramp[] rampvars = new Ramp[]{rampvar};
        expected.setRamps(rampvars);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(1, actual.getRampsCount());
        assertEquals(1, actual.getRamps().length);
        
        TsVariableDescriptor vardesc = new TsVariableDescriptor("test");
        vardesc.setEffect(TsVariableDescriptor.UserComponentType.Seasonal);
        TsVariableDescriptor[] tsvardesc_ = new TsVariableDescriptor[]{vardesc};
        expected.setUserDefinedVariables(tsvardesc_);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(1, actual.getUserDefinedVariablesCount());
        assertEquals(1, actual.getUserDefinedVariables().length);
        assertEquals(TsVariableDescriptor.UserComponentType.Seasonal, actual.getUserDefinedVariables()[0].getEffect());
        
        assertTrue(expected.isUsed());
        expected.clearOutliers();
        expected.clearInterventionVariables();
        expected.clearRamps();
        expected.clearUserDefinedVariables();
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.getOutliersCount() == 0);
        assertTrue(actual.getInterventionVariablesCount() == 0);
        assertTrue(actual.getRampsCount() == 0);
        assertTrue(actual.getUserDefinedVariablesCount() == 0);
    }
    
}
