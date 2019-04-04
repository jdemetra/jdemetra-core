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
package demetra.benchmarking.univariate;

import demetra.benchmarking.ssf.SsfCholette;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.SsfData;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class SsfCholetteTest {
    
    public SsfCholetteTest() {
    }

    @Test
    public void testStationary() {
        double[] data=new double[16];
        double[] w=new double[16];
        for (int i=0; i<data.length; ++i){
            data[i]=(i+1)%4 == 0 ? i : Double.NaN; 
            w[i]=i+1;
        }
        
        
        ISsf ssf = SsfCholette.builder(4).start(2).rho(.8).weights(DoubleSeq.of(w)).build();
        DefaultSmoothingResults rslts = DkToolkit.smooth(ssf, new SsfData(data), true, true);
        DoubleSeq c0 = rslts.getComponent(1);
        
        ec.benchmarking.ssf.SsfCholette ossf=new ec.benchmarking.ssf.SsfCholette(4, 2, .8, w);
        ec.tstoolkit.ssf.Smoother smoother=new ec.tstoolkit.ssf.Smoother();
        smoother.setSsf(ossf);
        ec.tstoolkit.ssf.SmoothingResults orslts=new ec.tstoolkit.ssf.SmoothingResults(true, true);
        smoother.process(new ec.tstoolkit.ssf.SsfData(data, null), orslts);
        double[] c1 = orslts.component(1);
        for (int i=0; i<16; ++i){
            assertEquals(c0.get(i), c1[i], 1e-9);
        }
    }
    
    @Test
    public void testNonStationary() {
        double[] data=new double[16];
        double[] w=new double[16];
        for (int i=0; i<data.length; ++i){
            data[i]=(i+1)%4 == 0 ? i : Double.NaN; 
            w[i]=i+1;
        }
        
        
        ISsf ssf = SsfCholette.builder(4).start(2).rho(1).weights(DoubleSeq.of(w)).build();
        DefaultSmoothingResults rslts = DkToolkit.smooth(ssf, new SsfData(data), true, true);
        DoubleSeq c0 = rslts.getComponent(1);
        
        ec.benchmarking.ssf.SsfDenton ossf=new ec.benchmarking.ssf.SsfDenton(4, 2, w);
        ec.tstoolkit.ssf.Smoother smoother=new ec.tstoolkit.ssf.Smoother();
        smoother.setSsf(ossf);
        ec.tstoolkit.ssf.SmoothingResults orslts=new ec.tstoolkit.ssf.SmoothingResults(true, true);
        smoother.process(new ec.tstoolkit.ssf.SsfData(data, null), orslts);
        double[] c1 = orslts.component(1);
        for (int i=0; i<16; ++i){
            assertEquals(c0.get(i), c1[i], 1e-9);
        }
    }
}
