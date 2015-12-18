/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.arima.special;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
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
        assertTrue(expected.isDefault());

        TradingDaysSpec tdSpec = new TradingDaysSpec();
        tdSpec.setTradingDaysType(TradingDaysType.TradingDays);
        expected.setTradingDays(tdSpec);
        assertNotEquals(expected, actual);
        assertTrue(expected.isUsed());
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isUsed());
        assertFalse(actual.isDefault());
        assertEquals(TradingDaysType.TradingDays, actual.getTradingDays().getTradingDaysType());

        expected = new RegressionSpec();
        actual = new RegressionSpec();
        EasterSpec eSpec = new EasterSpec();
        eSpec.setOption(ec.tstoolkit.modelling.arima.tramo.EasterSpec.Type.Standard);
        assertTrue(eSpec.isUsed());
        assertFalse(eSpec.isDefault());
        expected.setEaster(eSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(ec.tstoolkit.modelling.arima.tramo.EasterSpec.Type.Standard, actual.getEaster().getOption());
        assertTrue(actual.getEaster().isUsed());
        assertFalse(actual.getEaster().isDefault());

        expected = new RegressionSpec();
        actual = new RegressionSpec();
        OutlierDefinition oDef1 = new OutlierDefinition(new Day(2015, Month.March, 15), OutlierType.AO, true);
        OutlierDefinition oDef2 = new OutlierDefinition(new Day(2013, Month.April, 27), OutlierType.SLS, false);
        OutlierDefinition[] oDef = new OutlierDefinition[]{oDef1, oDef2};
        assertEquals(expected, actual);
        expected.setOutliers(oDef);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(OutlierType.AO, actual.getOutlier(0).getType());
        assertEquals(new Day(2013, Month.April, 27), actual.getOutlier(1).getPosition());
        assertEquals(2, actual.getOutliersCount());
        assertTrue(actual.contains(oDef2));
        actual.clearOutliers();
        assertEquals(0, actual.getOutliersCount());
        assertNotEquals(expected, actual);
        expected.reset();
        assertEquals(expected, actual);

        expected = new RegressionSpec();
        actual = new RegressionSpec();
        InterventionVariable iVar1 = new InterventionVariable();
        InterventionVariable iVar2 = new InterventionVariable();
        iVar1.setDelta(2.0);
        iVar2.setDelta(1.5);
        assertEquals(expected, actual);
        expected.setInterventionVariables(new InterventionVariable[]{iVar1, iVar2});
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isUsed());
        assertEquals(2.0, actual.getInterventionVariable(0).getDelta(), .0);
        assertEquals(1.5, actual.getInterventionVariables()[1].getDelta(), .0);
        assertEquals(2, actual.getInterventionVariablesCount());
        actual.clearInterventionVariables();
        assertEquals(0, actual.getInterventionVariablesCount());
        assertNotEquals(expected, actual);
        assertFalse(actual.isUsed());
        assertTrue(actual.isDefault());

        expected = new RegressionSpec();
        actual = new RegressionSpec();
        Ramp r1 = new Ramp(new Day(2015, Month.March, 10), new Day(2015, Month.December, 15));
        Ramp r2 = new Ramp(new Day(2015, Month.January, 15), new Day(2015, Month.March, 21));
        assertEquals(expected, actual);
        assertTrue(expected.isDefault());
        assertFalse(expected.isUsed());
        expected.setRamps(new Ramp[]{r1, r2});
        assertFalse(expected.isDefault());
        assertTrue(expected.isUsed());
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertFalse(actual.isDefault());
        assertTrue(actual.isUsed());
        assertEquals(2, actual.getRampsCount());
        assertEquals(new Day(2015, Month.March, 10), actual.getRamp(0).getStart());
        assertEquals(new Day(2015, Month.March, 21), actual.getRamps()[1].getEnd());
        actual.clearRamps();
        assertEquals(0, actual.getRampsCount());
        assertNotEquals(expected, actual);
        assertFalse(actual.isUsed());
        assertTrue(actual.isDefault());

        expected = new RegressionSpec();
        actual = new RegressionSpec();
        TsVariableDescriptor tvDesc1 = new TsVariableDescriptor("tvDesc1");
        TsVariableDescriptor tvDesc2 = new TsVariableDescriptor("tvDesc2");
        tvDesc1.setEffect(TsVariableDescriptor.UserComponentType.Seasonal);
        tvDesc1.setLags(1, 5);
        tvDesc2.setEffect(TsVariableDescriptor.UserComponentType.Trend);
        tvDesc2.setLags(2, 8);
        assertEquals(expected, actual);
        assertTrue(expected.isDefault());
        assertFalse(expected.isUsed());
        expected.setUserDefinedVariables(new TsVariableDescriptor[]{tvDesc1, tvDesc2});
        assertFalse(expected.isDefault());
        assertTrue(expected.isUsed());
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertFalse(actual.isDefault());
        assertTrue(actual.isUsed());
        assertEquals(2, actual.getUserDefinedVariablesCount());
        assertEquals(tvDesc1, actual.getUserDefinedVariable(0));
        assertEquals(tvDesc2, actual.getUserDefinedVariables()[1]);
        actual.clearUserDefinedVariables();
        assertNotEquals(expected, actual);
        assertTrue(actual.isDefault());
        assertFalse(actual.isUsed());
    }

}
