/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.arima.special.mixedfrequencies;

import ec.tstoolkit.ParameterType;
import ec.tstoolkit.arima.special.EasterSpec;
import ec.tstoolkit.arima.special.RegressionSpec;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.arima.DefaultArimaSpec;
import ec.tstoolkit.modelling.arima.tramo.ArimaSpec;
import ec.tstoolkit.timeseries.DataType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class MixedFrequenciesSpecificationTest {
    
    public MixedFrequenciesSpecificationTest() {
    }

    @Test
    public void testInformationSet() {
        MixedFrequenciesSpecification expected = new MixedFrequenciesSpecification();
        MixedFrequenciesSpecification actual = new MixedFrequenciesSpecification();
        InformationSet info;
        assertEquals(expected, actual);
        BasicSpec bSpec = new BasicSpec();
        bSpec.setDataType(DataType.Stock);
        expected.setBasic(bSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(DataType.Stock, actual.getBasic().getDataType());
        
        expected = new MixedFrequenciesSpecification();
        actual = new MixedFrequenciesSpecification();
        assertEquals(expected, actual);
        DefaultArimaSpec daSpec = new DefaultArimaSpec();
        daSpec.setParameterType(ParameterType.Estimated);
        expected.setArima(daSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        
        expected = new MixedFrequenciesSpecification();
        actual = new MixedFrequenciesSpecification();
        assertEquals(expected, actual);
        RegressionSpec rSpec = new RegressionSpec();
        EasterSpec eaSpec = new EasterSpec();
        eaSpec.setOption(ec.tstoolkit.modelling.arima.tramo.EasterSpec.Type.Standard);
        rSpec.setEaster(eaSpec);
        expected.setRegression(rSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        
        expected = new MixedFrequenciesSpecification();
        actual = new MixedFrequenciesSpecification();
        assertEquals(expected, actual);
        EstimateSpec eSpec = new EstimateSpec();
        eSpec.setMethod(EstimateSpec.Method.Cholesky);
        expected.setEstimate(eSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        
    }
    
}
