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
package ec.demetra.xml.sa.tramoseats;

import ec.tstoolkit.modelling.arima.tramo.AutoModelSpec;
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
        spec.setAcceptDefault(true);
        spec.setAmiCompare(true);
        spec.setPc(.1);
        spec.setPcr(.88);
        spec.setCancel(.3);
        spec.setTsig(2);
        spec.setUb1(.9);
        spec.setUb2(.92);
        XmlAutoModellingSpec xspec=new XmlAutoModellingSpec();
        AutoModelSpec nspec=new AutoModelSpec();
        XmlAutoModellingSpec.MARSHALLER.marshal(spec, xspec);
        XmlAutoModellingSpec.UNMARSHALLER.unmarshal(xspec, nspec);
        assertTrue(spec.equals(nspec));
    }
    
}
