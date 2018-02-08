/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved 
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
package demetra.data;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SmoothedPeriodogramTest {
    
    double[] data;
    
    public SmoothedPeriodogramTest() {
        data=new double[Data.ABS_RETAIL.length-1];
        for (int i=0; i<data.length; ++i){
            data[i]=Math.log(Data.ABS_RETAIL[i+1])-Math.log(Data.ABS_RETAIL[i]);
        }
    }
    
    @Test
    public void testBlackmanTukey() {
        SmoothedPeriodogram periodogram = SmoothedPeriodogram.builder()
                .data(DoubleSequence.of(data))
                .taper(new TukeyHanningTaper(.1))
                .windowLength(85)
                .relativeResolution(.5)
                .windowFunction(DiscreteWindowFunction.Bartlett)
                .build();
        
//        for (int i = 0; i < 200; ++i) {
//            System.out.println(periodogram.getSpectrumValue(Math.PI * i / 200));
//        }        
    }
    
    @Test
    public void testLegacyBlackmanTukey() {
        ec.tstoolkit.data.BlackmanTukeySpectrum bts=new ec.tstoolkit.data.BlackmanTukeySpectrum();
        bts.setTaper(new ec.tstoolkit.data.TukeyHanningTaper());
        bts.setWindowLength(85);
        bts.setData(data);
        
//        System.out.println("legacy");
//        for (int i = 0; i < 200; ++i) {
//            System.out.println(bts.getSpectrumValue(Math.PI * i / 200));
//        }        
    }
}
