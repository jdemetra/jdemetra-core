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
package ec.tstoolkit.maths.linearfilters;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.polynomials.Polynomial;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class SymmetricFrequencyResponse2Test {

    public SymmetricFrequencyResponse2Test() {
    }

    @Test
    public void testFrequencyResponse() {
        DataBlock rnd = new DataBlock(100);
        rnd.randomize();
        BackFilter f = BackFilter.of(rnd.getData());
        SymmetricFilter sf = SymmetricFilter.createFromFilter(f);
        SymmetricFrequencyResponse sfr = SymmetricFrequencyResponse.createFromFilter(sf);
        SymmetricFrequencyResponse2 sfr2 = SymmetricFrequencyResponse2.createFromFilter(sf);

        for (int i = 0; i < 10; ++i) {
            double x = i * .1 * Math.PI;
            assertEquals(sfr.evaluateAt(x), sfr2.evaluateAt(x), 1e-9);
        }
        assertEquals(sfr.getIntegral(), sfr2.getIntegral(), 1e-9);
    }

    @Test
    @Ignore
    public void testDecomposer() {
        DataBlock rnd = new DataBlock(60);
        rnd.randomize();
        BackFilter f = BackFilter.of(rnd.getData());
        Complex[] roots = f.roots();
        for (int i = 0; i < roots.length; ++i) {
            if (roots[i].absSquare() < 1) {
                roots[i] = roots[i].inv();
            }
        }
        f = new BackFilter(Polynomial.fromComplexRoots(roots));
        f = f.normalize();
        SymmetricFilter sf = SymmetricFilter.createFromFilter(f);

        SymmetricFrequencyResponseDecomposer decomposer = new SymmetricFrequencyResponseDecomposer();
        decomposer.decompose(sf);
        System.out.println(decomposer.getFactor());
        System.out.println(f);
        System.out.println(decomposer.getBFilter());
        SymmetricFrequencyResponseDecomposer2 decomposer2 = new SymmetricFrequencyResponseDecomposer2();
        decomposer2.decompose(sf);
        System.out.println(decomposer2.getBFilter());
        System.out.println(decomposer2.getFactor());
        SymmetricFrequencyResponseDecomposer3 decomposer3 = new SymmetricFrequencyResponseDecomposer3();
        decomposer3.decompose(sf);
        System.out.println(decomposer3.getBFilter());
         System.out.println(decomposer3.getFactor());
   }

    @Test
    @Ignore
    public void stressTestDecomposer() {
        DataBlock rnd = new DataBlock(30);
        rnd.randomize();
        BackFilter f = BackFilter.of(rnd.getData());
        Complex[] roots = f.roots();
        for (int i = 0; i < roots.length; ++i) {
            if (roots[i].absSquare() < 1) {
                roots[i] = roots[i].inv();
            }
        }
        f = new BackFilter(Polynomial.fromComplexRoots(roots));
        f = f.normalize();
        SymmetricFilter sf = SymmetricFilter.createFromFilter(f);

        int N = 10000;

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            SymmetricFrequencyResponseDecomposer decomposer = new SymmetricFrequencyResponseDecomposer();
            decomposer.decompose(sf);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
//        System.out.println(f);
//        System.out.println(decomposer.getBFilter());
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            SymmetricFrequencyResponseDecomposer2 decomposer2 = new SymmetricFrequencyResponseDecomposer2();
            decomposer2.decompose(sf);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
//        System.out.println(decomposer2.getBFilter());
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            SymmetricFrequencyResponseDecomposer3 decomposer3 = new SymmetricFrequencyResponseDecomposer3();
            decomposer3.decompose(sf);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
//        System.out.println(decomposer3.getBFilter());
    }

}
