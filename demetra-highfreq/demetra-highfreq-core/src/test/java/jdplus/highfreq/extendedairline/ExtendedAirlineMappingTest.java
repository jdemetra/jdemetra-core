/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.highfreq.extendedairline;

import demetra.data.DoubleSeq;
import demetra.data.Parameter;
import demetra.highfreq.ExtendedAirlineSpec;
import jdplus.arima.ArimaModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class ExtendedAirlineMappingTest {

    public ExtendedAirlineMappingTest() {
    }

    @Test
    public void testAR_int() {
        ExtendedAirlineSpec spec = ExtendedAirlineSpec.builder()
                .adjustToInt(true)
                .differencingOrder(-1)
                .phi(Parameter.undefined())
                .periodicities(new double[]{7, 365.25})
                .stheta(Parameter.make(2))
                .build();
        ExtendedAirlineMapping mapping = ExtendedAirlineMapping.of(spec);
        DoubleSeq p = mapping.getDefaultParameters();
        ArimaModel m = mapping.map(p);
        System.out.println(m);
        DoubleSeq np = mapping.parametersOf(m);
        assertEquals(p, np);
    }

    @Test
    public void testAR_noint() {
        ExtendedAirlineSpec spec = ExtendedAirlineSpec.builder()
                .adjustToInt(false)
                .differencingOrder(-1)
                .phi(Parameter.undefined())
                .periodicities(new double[]{7, 365.25})
                .stheta(Parameter.make(2))
                .build();
        ExtendedAirlineMapping mapping = ExtendedAirlineMapping.of(spec);
        DoubleSeq p = mapping.getDefaultParameters();
        ArimaModel m = mapping.map(p);
        System.out.println(m);
        DoubleSeq np = mapping.parametersOf(m);
        assertTrue(p.distance(np) < 1.e-9);
    }

    @Test
    public void test_int() {
        ExtendedAirlineSpec spec = ExtendedAirlineSpec.builder()
                .adjustToInt(true)
                .differencingOrder(-1)
                .theta(Parameter.undefined())
                .periodicities(new double[]{7, 365.25})
                .stheta(Parameter.make(2))
                .build();
        ExtendedAirlineMapping mapping = ExtendedAirlineMapping.of(spec);
        DoubleSeq p = mapping.getDefaultParameters();
        ArimaModel m = mapping.map(p);
        System.out.println(m);
        DoubleSeq np = mapping.parametersOf(m);
        assertEquals(p, np);
    }

    @Test
    public void test_noint() {
        ExtendedAirlineSpec spec = ExtendedAirlineSpec.builder()
                .adjustToInt(false)
                .differencingOrder(-1)
                .theta(Parameter.undefined())
                .periodicities(new double[]{7, 365.25})
                .stheta(Parameter.make(2))
                .build();
        ExtendedAirlineMapping mapping = ExtendedAirlineMapping.of(spec);
        DoubleSeq p = mapping.getDefaultParameters();
        ArimaModel m = mapping.map(p);
        System.out.println(m);
        DoubleSeq np = mapping.parametersOf(m);
        assertTrue(p.distance(np) < 1.e-9);
    }
}
