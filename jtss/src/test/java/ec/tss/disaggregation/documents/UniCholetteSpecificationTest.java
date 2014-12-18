/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 * See the Licence for the specific language governing
 permissions and limitations under the Licence.
 */
package ec.tss.disaggregation.documents;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class UniCholetteSpecificationTest {
    
    public UniCholetteSpecificationTest() {
    }

    @Test
    public void testCloneEquals() {
        UniCholetteSpecification spec=new UniCholetteSpecification();
        spec.setAggregationFrequency(TsFrequency.Yearly);
        assertTrue(spec.equals(spec.clone()));
    }
    
    @Test
    public void testReadWrite(){
        UniCholetteSpecification spec=new UniCholetteSpecification();
        spec.setAggregationFrequency(TsFrequency.Yearly);
        spec.setLambda(.5);
        spec.setRho(-.5);
        InformationSet info = spec.write(true);
        UniCholetteSpecification nspec=new UniCholetteSpecification();
        nspec.read(info);
        assertTrue(spec.equals(nspec));
        
    }
    
}