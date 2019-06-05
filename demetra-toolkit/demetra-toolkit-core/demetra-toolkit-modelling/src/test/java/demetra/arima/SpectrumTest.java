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
package demetra.arima;

import jdplus.maths.linearfilters.BackFilter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SpectrumTest {
    
    public static void main(String[] args){
        double[] a=new double[2];
        double[] b=new double[2];
        double A=.6, B=.1;
        a[0]=1;
        a[1]=A;
        b[0]=1;
        b[1]=B;
        for (int i=2; i<a.length; ++i){
            a[i]=a[i-1]*A;
            b[i]=b[i-1]*B;
        }
        BackFilter ar=BackFilter.ofInternal(a);
        BackFilter d=BackFilter.ofInternal(1);
        BackFilter ma=BackFilter.ofInternal(b);
       
        ArimaModel arima=new ArimaModel(ar, d, ma, 1);
        Spectrum spectrum = arima.getSpectrum();
        for (int freq=0; freq<=120; ++freq){
            System.out.println(spectrum.get((freq*Math.PI)/(120.0)));
        }
    }
    
    public SpectrumTest() {
    }

    @Test
    public void testSomeMethod() {
    }
    
}
