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
package jdplus.stats;

import demetra.stats.AutoCovariances;
import demetra.data.DoubleSeq;
import java.util.Random;
import java.util.function.IntToDoubleFunction;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class AutoCovariancesTest {
    
    public AutoCovariancesTest() {
    }

    @Test
    public void testPAC() {
        Random rnd =new Random();
        double[] data=new double[100];
        for (int i=0; i<data.length; ++i){
            data[i]=rnd.nextGaussian();
        }
        
        ec.tstoolkit.data.DescriptiveStatistics ds=new ec.tstoolkit.data.DescriptiveStatistics(data);
        ec.tstoolkit.data.AutoCorrelations oac=new ec.tstoolkit.data.AutoCorrelations(ds);
        oac.setKMax(20);
        double[] opac = oac.getPAC();
        DoubleSeq x=DoubleSeq.of(data);
        IntToDoubleFunction ac = AutoCovariances.autoCorrelationFunction(x, 0);
        double[] pac = AutoCovariances.partialAutoCorrelations(ac, 20);
        for(int i=0; i<20; ++i){
            assertEquals(pac[i], opac[i], 1e-9);
        }
    }
    
}
