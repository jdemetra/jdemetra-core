/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.arima.ssf;

import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SsfArimaTest {

    public SsfArimaTest() {
    }

    @Test
    public void testArma() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.setQ(1);
        spec.setBQ(1);
        SarimaModel arima = SarimaModel.builder(spec).theta(1, -.9).btheta(1, -.6).build();
        SsfArima ssf = SsfArima.of(arima);
    }

}
