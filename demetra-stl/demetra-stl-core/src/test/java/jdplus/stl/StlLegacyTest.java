/*
 * Copyright 2016 National Bank copyOf Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.stl;

import demetra.stl.StlLegacySpec;
import demetra.data.Doubles;
import demetra.data.Data;
import jdplus.data.DataBlock;
import java.util.Random;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class StlLegacyTest {

    public StlLegacyTest() {
    }

    @Test
    public void testDefault() {
        StlLegacySpec spec = StlLegacySpec.defaultSpec(12, 7, false);
        StlLegacy stl = new StlLegacy(spec);
        spec.setNo(5);
        spec.setMultiplicative(false);
        stl.process(Doubles.of(Data.EXPORTS));
//        System.out.println(DataBlock.of(stl.trend));
//        System.out.println(DataBlock.of(stl.season));
//        System.out.println(DataBlock.of(stl.irr));
    }

    @Test
    public void testMissing() {
        StlLegacySpec spec = StlLegacySpec.defaultSpec(12, 7, false);
        StlLegacy stl = new StlLegacy(spec);
        spec.setNo(5);
        spec.setMultiplicative(true);
        DataBlock s = DataBlock.copyOf(Data.EXPORTS);
        Random rnd = new Random();
        for (int i = 0; i < 10; ++i) {
            s.set(rnd.nextInt(s.length()), Double.NaN);
        }
        stl.process(s);
//        System.out.println(new DataBlock(stl.trend));
//        System.out.println(new DataBlock(stl.season));
//        System.out.println(new DataBlock(stl.irr));
    }

    @Test
//    @Ignore
    public void testLargeFilter() {

        StlLegacySpec spec = StlLegacySpec.defaultSpec(12, 21, false);
        StlLegacy stl = new StlLegacy(spec);
        stl.process(Doubles.of(Data.EXPORTS));
//        System.out.println(new DataBlock(stl.trend));
//        System.out.println(new DataBlock(stl.season));
//        System.out.println(new DataBlock(stl.irr));
    }

    @Test
    public void testMul() {
        StlLegacySpec spec = StlLegacySpec.defaultSpec(12, 7, false);
        spec.setMultiplicative(true);
        StlLegacy stl = new StlLegacy(spec);
        spec.setNo(5);
        stl.process(Doubles.of(Data.EXPORTS));
//        System.out.println(new DataBlock(stl.trend));
//        System.out.println(new DataBlock(stl.season));
//        System.out.println(new DataBlock(stl.irr));
    }

    @Test
    @Disabled
    public void stressTest() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            StlLegacySpec spec = StlLegacySpec.defaultSpec(12, 7, false);
            spec.setNo(5);
            StlLegacy stl = new StlLegacy(spec);
            stl.process(Doubles.of(Data.EXPORTS));
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
//    @Ignore
    public void testInner() {
        StlLegacySpec spec = StlLegacySpec.defaultSpec(12, 9, true);
        StlLegacy stl = new StlLegacy(spec);
        stl.process(Doubles.of(Data.EXPORTS));
//        System.out.println(new DataBlock(stl.trend));
//        System.out.println(new DataBlock(stl.season));
//        System.out.println(new DataBlock(stl.irr));
    }

}
