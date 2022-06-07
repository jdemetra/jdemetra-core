/*
 * Copyright 2019 National Bank of Belgium
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
package demetra.tramo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class TramoSpecTest {
    
    @Test
    public void testClone() {
        TramoSpec spec = TramoSpec.builder().build();
        assertEquals(spec, spec.toBuilder().build());
    }
    
    @Test
    public void testCloneDefaults() {
        assertEquals(TramoSpec.TR0, TramoSpec.TR0.toBuilder().build());
        assertEquals(TramoSpec.TR1, TramoSpec.TR1.toBuilder().build());
        assertEquals(TramoSpec.TR2, TramoSpec.TR2.toBuilder().build());
        assertEquals(TramoSpec.TR3, TramoSpec.TR3.toBuilder().build());
        assertEquals(TramoSpec.TR4, TramoSpec.TR4.toBuilder().build());
        assertEquals(TramoSpec.TR5, TramoSpec.TR5.toBuilder().build());
        assertEquals(TramoSpec.TRfull, TramoSpec.TRfull.toBuilder().build());
    }

    @Test
    public void testAccessNestedProperty() {
        TramoSpec spec = TramoSpec.builder().build();
        RegressionSpec regression = spec.getRegression();
        CalendarSpec calendar = regression.getCalendar();
        EasterSpec easter = calendar.getEaster().toBuilder().duration(10).build();

        TramoSpec newSpec = spec.toBuilder()
                .regression(regression.toBuilder()
                        .calendar(calendar.toBuilder()
                                .easter(easter)
                                .build())
                        .build()
                )
                .build();

        assertNotEquals(newSpec, spec);
        assertTrue(newSpec.getRegression().getCalendar().getEaster().getDuration() == 10);
        assertTrue(spec.getRegression().getCalendar().getEaster().getDuration() == 6);
    }
}
