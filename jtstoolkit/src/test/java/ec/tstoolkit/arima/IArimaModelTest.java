/*
* Copyright 2013 National Bank of Belgium
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

package ec.tstoolkit.arima;

import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarmaSpecification;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author pcuser
 */
public class IArimaModelTest {
    
    public IArimaModelTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //@Test
    public void testAcf() {
        SarmaSpecification spec=new SarmaSpecification(7);
        spec.setP(2);
        spec.setQ(1);
        spec.setBP(1);
        spec.setBQ(1);
        
        // (2 0 1)(1 0 1) model ("seasonal lag" = 7, for instance for a weekly model)
        SarimaModel m1=new SarimaModel(spec);
        m1.setPhi(1, .6);
        m1.setPhi(1, .7);
        m1.setBPhi(1, -.5);
        m1.setTheta(1, .5);
        m1.setBTheta(1, -.5);
        AutoCovarianceFunction acf = m1.getAutoCovarianceFunction();
        double[] c=acf.values(35);
//        for (int i=0; i<c.length; ++i){
//            System.out.println(c[i]);
//        }
    }
    
     @Test
     public void testSum() {
         // Model with AR=1, MA=1, D=I(2)=(1-B)*(1-B)
         Polynomial D2=UnitRoots.D1.times(UnitRoots.D1);
         ArimaModel I2=new ArimaModel(null, new BackFilter(D2), null, 1);
         // White noise, with var=1600
         ArimaModel N= new ArimaModel(1600);
         
         // Aggregated model. The decomposition I2+N corresponds to the Hodrick-Prescott filter
         ArimaModel S=I2.plus(N);
         System.out.println(S);
         
         Spectrum.Minimizer min =new Spectrum.Minimizer();
         min.minimize(S.getSpectrum());
         double nvar=min.getMinimum();
         ArimaModel Sc=S.minus(nvar);
         System.out.println(Sc);
         
         // A small test to check the routines
         min.minimize(I2.getSpectrum());
         nvar=min.getMinimum();
         ArimaModel Sc2=I2.minus(nvar);
         assertTrue(ArimaModel.same(Sc, Sc2, 1e-6));
     }
}
