/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.Ramp;
import static org.junit.Assert.*;
import org.junit.Test;

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

        assertTrue(expected.isDefault());
        assertTrue(actual.isDefault());
        assertEquals(expected, actual);

        CalendarSpec calspec = new CalendarSpec();
        EasterSpec eastspec = new EasterSpec();
        eastspec.setOption(EasterSpec.Type.IncludeEaster);
        eastspec.setDuration(3);
        calspec.setEaster(eastspec);
        expected.setCalendar(calspec);
        info = expected.write(true);
        actual.read(info);
        assertEquals(3, actual.getCalendar().getEaster().getDuration());

        OutlierDefinition outDef = new OutlierDefinition(Day.toDay(), OutlierType.AO);
        OutlierDefinition[] outliers_ = new OutlierDefinition[]{outDef};
        expected.setOutliers(outliers_);
        info = expected.write(true);
        actual.read(info);
        assertEquals(1, actual.getOutliers().length);
        assertEquals(OutlierType.AO, actual.getOutlier(0).getType());

        TsVariableDescriptor vardesc = new TsVariableDescriptor("test");
        vardesc.setEffect(TsVariableDescriptor.UserComponentType.Seasonal);
        TsVariableDescriptor[] tsvardesc_ = new TsVariableDescriptor[]{vardesc};
        expected.setUserDefinedVariables(tsvardesc_);
        info = expected.write(true);
        actual.read(info);
        assertEquals(1, actual.getUserDefinedVariablesCount());
        assertEquals(1, actual.getUserDefinedVariables().length);
        assertEquals(TsVariableDescriptor.UserComponentType.Seasonal, actual.getUserDefinedVariable(0).getEffect());

        InterventionVariable intvar = new InterventionVariable();
        intvar.setDelta(1.0);
        InterventionVariable[] intvars = new InterventionVariable[]{intvar};
        expected.setInterventionVariables(intvars);
        info = expected.write(true);
        actual.read(info);
        assertEquals(1, actual.getInterventionVariablesCount());
        assertEquals(1, actual.getInterventionVariables().length);
        assertEquals(1.0, actual.getInterventionVariable(0).getDelta(), 0.0);
        
        Ramp rampvar = new Ramp();
        Ramp[] rampvars = new Ramp[]{rampvar};
        expected.setRamps(rampvars);
        info = expected.write(true);
        actual.read(info);
        assertEquals(1, actual.getRampsCount());
        assertEquals(1, actual.getRamps().length);
        
        expected.setFixedCoefficients("ftest",new double[]{10});
        expected.setCoefficients("test",new double[]{10});
        info = expected.write(true);
        actual.read(info);
        assertEquals(actual, expected);
    }
}
