/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.structural;

import ec.tstoolkit.information.InformationSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class BsmSpecificationTest {
    
    public BsmSpecificationTest() {
    }

    @Test
    public void testInformationSet() {
        BsmSpecification expected = new BsmSpecification();
        BsmSpecification actual = new BsmSpecification();
        InformationSet info;
        assertEquals(expected, actual);
        expected.setOptimizer(BsmSpecification.Optimizer.LBFGS);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(BsmSpecification.Optimizer.LBFGS, actual.getOptimizer());
        
        expected = new BsmSpecification();
        actual = new BsmSpecification();
        assertEquals(expected, actual);
        expected.setPrecision(0.1);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(0.1, actual.getPrecision(), .0);
        
        expected = new BsmSpecification();
        actual = new BsmSpecification();
        assertEquals(expected, actual);
        expected.setDiffuseRegressors(true);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isDiffuseRegressors());
        
        expected = new BsmSpecification();
        actual = new BsmSpecification();
        assertEquals(expected, actual);
        ModelSpecification mSpec = new ModelSpecification();
        mSpec.setSeasonalModel(SeasonalModel.Crude);
        expected.setModelSpecification(mSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(SeasonalModel.Crude, actual.getModelSpecification().getSeasonalModel());
    }
    
}
