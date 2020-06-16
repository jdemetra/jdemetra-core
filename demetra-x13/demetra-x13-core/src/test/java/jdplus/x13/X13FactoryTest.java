/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.x13;

import demetra.data.Data;
import demetra.processing.ProcessingLog;
import demetra.x13.X13Spec;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class X13FactoryTest {
    
    public X13FactoryTest() {
    }

    @Test
    public void testUpdatePoint() {
        X13Kernel x13=X13Kernel.of(X13Spec.RSA4, null);
        ProcessingLog log=new ProcessingLog();
        X13Results rslt = x13.process(Data.TS_PROD, log);
        assertTrue(rslt.getFinals() != null);
        X13Spec nspec = X13Factory.INSTANCE.of(X13Spec.RSA4, rslt);
        log = new ProcessingLog();
        System.out.println(nspec);
        x13 = X13Kernel.of(nspec, null);
        X13Results rslt2 = x13.process(Data.TS_PROD, log);
        assertTrue(rslt2.getFinals() != null);
        X13Spec nspec2 = X13Factory.INSTANCE.of(nspec, rslt2);
        System.out.println(nspec2);
        assertEquals(rslt.getPreprocessing().getConcentratedLikelihood().logLikelihood(),
                rslt2.getPreprocessing().getConcentratedLikelihood().logLikelihood(), 1e-4);
    }
    
}
