/*
 * Copyright 2013 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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

package ec.satoolkit.special;

import ec.satoolkit.benchmarking.SaBenchmarkingSpec;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.structural.BsmSpecification;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class StmSpecificationTest {
    
    public StmSpecificationTest() {
    }
    
    @Test
    public void testClone(){
        StmSpecification spec=new StmSpecification();
        spec=spec.clone();
    } 
    
    @Test
    public void testInformationSet(){
        StmSpecification expected = new StmSpecification();
        StmSpecification actual = new StmSpecification();
        InformationSet info;
        assertEquals(expected, actual);
        
        PreprocessingSpecification pSpec = new PreprocessingSpecification();
        pSpec.dtype = TradingDaysType.WorkingDays;
        expected.setPreprocessingSpec(pSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(TradingDaysType.WorkingDays, actual.getPreprocessingSpec().dtype);
        
        BsmSpecification bSpec = new BsmSpecification();
        bSpec.setDiffuseRegressors(true);
        assertFalse(expected.getDecompositionSpec().isDiffuseRegressors());
        expected.setDecompositionSpec(bSpec);
        assertTrue(expected.getDecompositionSpec().isDiffuseRegressors());
        info = expected.write(true);
        assertFalse(actual.getDecompositionSpec().isDiffuseRegressors());
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.getDecompositionSpec().isDiffuseRegressors());
        
        SaBenchmarkingSpec sbSpec = new SaBenchmarkingSpec();
        sbSpec.setEnabled(true);
        sbSpec.setTarget(SaBenchmarkingSpec.Target.Original);
        expected.setBenchmarkingSpec(sbSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(SaBenchmarkingSpec.Target.Original, actual.getBenchmarkingSpec().getTarget());
        
    }
}
