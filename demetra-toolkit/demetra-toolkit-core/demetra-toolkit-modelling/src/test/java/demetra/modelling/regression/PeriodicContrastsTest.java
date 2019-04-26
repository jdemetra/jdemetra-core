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

import demetra.maths.matrices.FastMatrix;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class PeriodicContrastsTest {

    public PeriodicContrastsTest() {
    }

    @Test
    public void testMonthly() {
        PeriodicContrasts vars = new PeriodicContrasts(12);
        TsDomain domain = TsDomain.of(TsPeriod.monthly(2017, 8), 120);
        FastMatrix M=Regression.matrix(domain, vars);
        //System.out.println(M);
        assertTrue(M.columnList().stream().allMatch(col->col.sum()==0));
    }

}
