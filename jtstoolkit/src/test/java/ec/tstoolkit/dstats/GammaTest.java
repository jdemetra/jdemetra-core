/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tstoolkit.dstats;

import ec.tstoolkit.random.MersenneTwister;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class GammaTest {
    
    public GammaTest() {
    }

    @Test
    public void testRandom() {
        double s = 0;
        double ssq=0;
        int N = 1000000;
        long t0 = System.currentTimeMillis();
        Gamma dist = new Gamma(7.48, 0.295);
        MersenneTwister rng = new MersenneTwister(0);
        for (int i = 0; i < N; ++i) {
            double r=dist.random(rng);
            s += r;
            ssq+=r*r;
        }
        long t1 = System.currentTimeMillis();
        assertEquals(dist.getExpectation(), s / N, .01);
        assertEquals(dist.getVariance(), ssq / N-s/N*s/N, .01);
//        System.out.println("New");
//        System.out.println(t1 - t0);
//        System.out.println(s / N);
//        System.out.println(ssq / N-s/N*s/N);
//        System.out.println(dist.getExpectation());
//        System.out.println(dist.getVariance());
    }
    
    @Test
    public void testGamma(){
        double g1=Gamma.gamma(10);
        double g2=SpecialFunctions.gamma(10);
         
        for (double d=0.1; d<10; d+=.01){
            assertEquals(Gamma.gamma(d), SpecialFunctions.gamma(d), 1e-3);
        }
    }
    
    @Test
    @Ignore
    public void stressTestGamma() {
        double s = 0;
        int N = 10000000;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            Gamma.gamma(1+i%10);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("New gamma");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            SpecialFunctions.gamma(1+i%10);
        }
        t1 = System.currentTimeMillis();
        System.out.println("Old gamma");
        System.out.println(t1 - t0);
    }
    
    @Test
    public void testDensity(){
        int nf=12;
        Gamma dist = new Gamma(nf/2,2);
        Chi2 chi=new Chi2(nf);
        for (double d=0; d<100; d+=.05){
            assertEquals(dist.getDensity(d),chi.getDensity(d), 1e-6);
        }
    }
    
    @Test
    public void testProperties(){
        Gamma dist = new Gamma(8,5);
        Gamma dist2 = new Gamma(8,1);
        assertEquals(5*dist.getDensity(10),dist2.getDensity(2), 1e-6);
        int nf=12;
        Gamma gamma = new Gamma(nf/2,2);
        Chi2 chi=new Chi2(nf);
        assertEquals(chi.getExpectation(),gamma.getExpectation(), 1e-6);
        assertEquals(chi.getVariance(),gamma.getVariance(), 1e-6);
        for (double d=.1; d<10; d+=.1){
//            System.out.print(chi.getProbability(d, ProbabilityType.Lower));
//            System.out.print('\t');
//            System.out.println(gamma.getProbability(d, ProbabilityType.Lower));
            assertEquals(gamma.getProbability(d, ProbabilityType.Lower), 
                    chi.getProbability(d, ProbabilityType.Lower), 1e-6);
        }
    }
    
}
