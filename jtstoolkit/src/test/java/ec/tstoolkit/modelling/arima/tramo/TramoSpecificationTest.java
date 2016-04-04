/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.ParameterType;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class TramoSpecificationTest {
    
    @Test
    public void testInformation() {
        TramoSpecification spec = new TramoSpecification();
        InformationSet info = spec.write(true);
        TramoSpecification nspec = new TramoSpecification();
        nspec.read(info);
        assertEquals(spec, nspec);
        info = TramoSpecification.TR0.write(true);
        nspec = new TramoSpecification();
        nspec.read(info);
        assertEquals(TramoSpecification.TR0, nspec);
        info = TramoSpecification.TR1.write(true);
        nspec = new TramoSpecification();
        nspec.read(info);
        assertEquals(TramoSpecification.TR1, nspec);
        info = TramoSpecification.TR2.write(true);
        nspec = new TramoSpecification();
        nspec.read(info);
        assertEquals(TramoSpecification.TR2, nspec);
        info = TramoSpecification.TR3.write(true);
        nspec = new TramoSpecification();
        nspec.read(info);
        assertEquals(TramoSpecification.TR3, nspec);
        info = TramoSpecification.TR4.write(true);
        nspec = new TramoSpecification();
        nspec.read(info);
        assertEquals(TramoSpecification.TR4, nspec);
        info = TramoSpecification.TR5.write(true);
        nspec = new TramoSpecification();
        nspec.read(info);
        assertEquals(TramoSpecification.TR5, nspec);
    }
    
    @Test
    public void testInformationSet() {
        TramoSpecification expected = new TramoSpecification();
        TramoSpecification actual = new TramoSpecification();
        InformationSet info;
        assertEquals(expected, actual);
        ArimaSpec aSpec = new ArimaSpec();
        aSpec.setParameterType(ParameterType.Estimated);
        expected.setArima(aSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        
        expected = new TramoSpecification();
        actual = new TramoSpecification();
        assertEquals(expected, actual);
        TransformSpec tSpec = new TransformSpec();
        tSpec.setFunction(DefaultTransformationType.Log);
        expected.setTransform(tSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        
        expected = new TramoSpecification();
        actual = new TramoSpecification();
        assertEquals(expected, actual);
        AutoModelSpec amSpec = new AutoModelSpec();
        amSpec.setPc(.26);
        expected.setAutoModel(amSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.26, actual.getAutoModel().getPc(), .0);
        
        expected = new TramoSpecification();
        actual = new TramoSpecification();
        assertEquals(expected, actual);
        EstimateSpec eSpec = new EstimateSpec();
        eSpec.setTol(.0004);
        expected.setEstimate(eSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.0004, actual.getEstimate().getTol(), .0);
        
        expected = new TramoSpecification();
        actual = new TramoSpecification();
        assertEquals(expected, actual);
        OutlierSpec oSpec = new OutlierSpec();
        oSpec.setCriticalValue(2.5);
        expected.setOutliers(oSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(2.5, actual.getOutliers().getCriticalValue(), .0);
        
        expected = new TramoSpecification();
        actual = new TramoSpecification();
        assertEquals(expected, actual);
        RegressionSpec rSpec = new RegressionSpec();
        rSpec.setUserDefinedVariables(new TsVariableDescriptor[] {new TsVariableDescriptor("test")});
        expected.setRegression(rSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        
    }

//    @Test
//    public void testDictionay() {
//        TramoSpecification spec = new TramoSpecification();
//        ArrayList<String> dic = new ArrayList<String>();
//        spec.fillDictionary(null, dic);
//        for (String s : dic) {
//            System.out.println(s);
//        }
//    }
}
