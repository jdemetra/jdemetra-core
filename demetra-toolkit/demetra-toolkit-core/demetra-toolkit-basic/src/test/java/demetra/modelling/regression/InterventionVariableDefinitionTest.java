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
package demetra.modelling.regression;

import demetra.data.Range;
import demetra.timeseries.regression.InterventionVariable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class InterventionVariableDefinitionTest {
    
    public InterventionVariableDefinitionTest() {
    }

    @Test
    public void testBuilder() {
        InterventionVariable var = InterventionVariable.builder()
                .sequence(Range.of(LocalDateTime.now(), LocalDateTime.now().plus(1, ChronoUnit.WEEKS)))
                .build();
    }
    
}
