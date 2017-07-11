/*
 * Copyright 2016 National Bank of Belgium
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
package ec.demetra.xml.sa.benchmarking;

import ec.benchmarking.simplets.TsCholette;
import ec.satoolkit.benchmarking.SaBenchmarkingSpec;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class XmlCholetteSpecTest {
    
    public XmlCholetteSpecTest() {
    }

    @Test
    public void testMarshalling() {
        SaBenchmarkingSpec spec=new SaBenchmarkingSpec();
        spec.setEnabled(true);
        test(spec);  
        spec.setRho(.9);
        spec.setLambda(.5);
        test(spec);
        spec.setBias(TsCholette.BiasCorrection.Multiplicative);
        test(spec);  
        spec.setTarget(SaBenchmarkingSpec.Target.Original);
        test(spec);  
    }
  
    private void test(SaBenchmarkingSpec spec) {
        XmlCholetteSpec xspec =XmlCholetteSpec.MARSHALLER.marshal(spec);
        assertTrue(xspec != null);
        SaBenchmarkingSpec nspec = new SaBenchmarkingSpec();
        XmlCholetteSpec.UNMARSHALLER.unmarshal(xspec, nspec);
        assertTrue(spec.equals(nspec));
    }
    
}
