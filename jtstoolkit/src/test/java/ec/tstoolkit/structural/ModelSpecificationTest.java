/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.structural;

import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.information.InformationSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class ModelSpecificationTest {
    
    public ModelSpecificationTest() {
    }

    @Test
    public void testInformationSet() {
        ModelSpecification expected = new ModelSpecification();
        ModelSpecification actual = new ModelSpecification();
        InformationSet info;
        assertEquals(expected, actual);
        Parameter param1 = new Parameter(1.5, ParameterType.Fixed);
        Parameter param2 = new Parameter(2.0, ParameterType.Estimated);
        expected.setCyclicalDumpingFactor(param1);
        expected.fixComponent(Component.Cycle);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(param1, actual.getCyclicalDumpingFactor());
        
        expected = new ModelSpecification();
        actual = new ModelSpecification();
        assertEquals(expected, actual);
        expected.setCyclicalPeriod(param2);
        expected.fixComponent(Component.Cycle);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(param2, actual.getCyclicalPeriod());
        
        expected = new ModelSpecification();
        actual = new ModelSpecification();
        assertEquals(expected, actual);
        expected.setSeasonalModel(SeasonalModel.Crude);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(SeasonalModel.Crude, actual.getSeasonalModel());
        
        expected = new ModelSpecification();
        actual = new ModelSpecification();
        assertEquals(expected, actual);
        expected.useLevel(ComponentUse.Fixed);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(ComponentUse.Fixed, actual.getLevelUse());
        
        expected = new ModelSpecification();
        actual = new ModelSpecification();
        assertEquals(expected, actual);
        expected.useNoise(ComponentUse.Fixed);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(ComponentUse.Fixed, actual.getNoiseUse());
        
        expected = new ModelSpecification();
        actual = new ModelSpecification();
        assertEquals(expected, actual);
        expected.useSlope(ComponentUse.Fixed);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(ComponentUse.Fixed, actual.getSlopeUse());
        
    }
    
}
