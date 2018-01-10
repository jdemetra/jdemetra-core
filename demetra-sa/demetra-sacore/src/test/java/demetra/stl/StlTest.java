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
package demetra.stl;

import demetra.data.Data;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import java.util.Random;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class StlTest {

    public StlTest() {
    }

    @Test
    public void testDefault() {
        StlSpecification spec = StlSpecification.defaultSpec(12, 7, false);
        Stl stl = new Stl(spec);
        spec.setNo(5);
        spec.setMultiplicative(true);
        stl.process(DoubleSequence.of(Data.EXPORTS));
//        System.out.println(new DataBlock(stl.trend));
//        System.out.println(new DataBlock(stl.season));
//        System.out.println(new DataBlock(stl.irr));
    }

    @Test
    public void testMissing() {
        StlSpecification spec = StlSpecification.defaultSpec(12, 7, false);
        Stl stl = new Stl(spec);
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

        StlSpecification spec = StlSpecification.defaultSpec(12, 21, false);
        Stl stl = new Stl(spec);
        stl.process(DoubleSequence.of(Data.EXPORTS));
//        System.out.println(new DataBlock(stl.trend));
//        System.out.println(new DataBlock(stl.season));
//        System.out.println(new DataBlock(stl.irr));
    }

    @Test
    public void testMul() {
        StlSpecification spec = StlSpecification.defaultSpec(12, 7, false);
        spec.setMultiplicative(true);
        Stl stl = new Stl(spec);
        spec.setNo(5);
        stl.process(DoubleSequence.of(Data.EXPORTS));
//        System.out.println(new DataBlock(stl.trend));
//        System.out.println(new DataBlock(stl.season));
//        System.out.println(new DataBlock(stl.irr));
    }

    @Test
    @Ignore
    public void stressTest() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            StlSpecification spec = StlSpecification.defaultSpec(12, 7, false);
            spec.setNo(5);
            Stl stl = new Stl(spec);
            stl.process(DoubleSequence.of(Data.EXPORTS));
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
//    @Ignore
    public void testInner() {
        StlSpecification spec = StlSpecification.defaultSpec(12, 9, true);
        Stl stl = new Stl(spec);
        stl.process(DoubleSequence.of(Data.EXPORTS));
//        System.out.println(new DataBlock(stl.trend));
//        System.out.println(new DataBlock(stl.season));
//        System.out.println(new DataBlock(stl.irr));
    }

}
