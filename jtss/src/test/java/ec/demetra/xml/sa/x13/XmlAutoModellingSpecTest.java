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
package ec.demetra.xml.sa.x13;

import ec.tstoolkit.modelling.arima.x13.AutoModelSpec;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class XmlAutoModellingSpecTest {
    
    public XmlAutoModellingSpecTest() {
    }

    @Test
    public void testMarshaller() {
        AutoModelSpec spec=new AutoModelSpec();
        spec.setEnabled(true);
        test(spec);
        spec.setAcceptDefault(true);
        test(spec);
        spec.setCancelationLimit(.2);
        test(spec);
        spec.setFinalUnitRootLimit(.9);
        test(spec);
        spec.setInitialUnitRootLimit(1.3);
        test(spec);
        spec.setPercentReductionCV(0.122);
        test(spec);
        spec.setLjungBoxLimit(.9);
        test(spec);
        spec.setUnitRootLimit(1.1);
        test(spec);
        spec.setMixed(false);
        test(spec);
        spec.setBalanced(true);
        test(spec);
    }
    
    private void test(AutoModelSpec spec){
        XmlAutoModellingSpec xspec=new XmlAutoModellingSpec();
        AutoModelSpec nspec=new AutoModelSpec();
        XmlAutoModellingSpec.MARSHALLER.marshal(spec, xspec);
        XmlAutoModellingSpec.UNMARSHALLER.unmarshal(xspec, nspec);
        assertTrue(spec.equals(nspec));
    }
}
