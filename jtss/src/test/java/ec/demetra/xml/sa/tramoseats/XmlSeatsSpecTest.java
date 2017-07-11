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

import ec.satoolkit.seats.SeatsSpecification;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class XmlSeatsSpecTest {

    public XmlSeatsSpecTest() {
    }

    @Test
    public void testMarshalling() {
        SeatsSpecification spec1 = new SeatsSpecification();
        test(spec1);
        spec1.setApproximationMode(SeatsSpecification.ApproximationMode.None);
        spec1.setMethod(SeatsSpecification.EstimationMethod.KalmanSmoother);
        test(spec1);
        spec1.setSeasBoundary(0.5);
        spec1.setSeasBoundary1(0.6);
        test(spec1);
        spec1.setSeasTolerance(0);
        spec1.setTrendBoundary(.8);
        spec1.setXlBoundary(.9);
        test(spec1);
    }

    private void test(SeatsSpecification spec) {
        XmlSeatsSpec xspec = new XmlSeatsSpec();
        XmlSeatsSpec.MARSHALLER.marshal(spec, xspec);
        SeatsSpecification nspec = new SeatsSpecification();
        XmlSeatsSpec.UNMARSHALLER.unmarshal(xspec, nspec);
        assertTrue(spec.equals(nspec));
    }
}
