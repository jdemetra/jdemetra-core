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

import demetra.stl.StlSpecification;
import demetra.data.Data;
import demetra.data.DoubleSeq;
import demetra.data.Doubles;
import demetra.stl.LoessSpecification;
import demetra.stl.SeasonalSpecification;
import jdplus.data.DataBlock;
import java.util.Random;

import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class StlKernelTest {

    public StlKernelTest() {
    }

    @Test
//    @Ignore
    public void testDefault() {
        StlSpecification spec = StlSpecification.builder()
                .seasonalSpec(new SeasonalSpecification(12, 7))
                .trendSpec(LoessSpecification.of(7, 1))
                .build();
        StlKernel stl = new StlKernel(spec);
        stl.process(Doubles.of(Data.EXPORTS));
//        System.out.println(DoubleSeq.of(stl.trend));
//        System.out.println(DoubleSeq.of(stl.season[0]));
//        System.out.println(DoubleSeq.of(stl.irr));
    }

    @Test
//    @Ignore
    public void testLargeFilter() {
        StlSpecification spec = StlSpecification.builder()
                .seasonalSpec(new SeasonalSpecification(12, 7))
                .trendSpec(LoessSpecification.of(21, 1))
                .build();
        StlKernel stl = new StlKernel(spec);
        stl.process(Doubles.of(Data.EXPORTS));
//        System.out.println(DoubleSeq.of(stl.trend));
//        System.out.println(DoubleSeq.of(stl.season[0]));
//        System.out.println(DoubleSeq.of(stl.irr));
    }

    @Test
    //@Ignore
    public void testSpec() {

        StlSpecification spec = StlSpecification
                .createDefault(12, false);
        StlKernel stl = new StlKernel(spec);
        stl.process(Doubles.of(Data.EXPORTS));
//        System.out.println(DataBlock.of(stl.trend));
//        System.out.println(DataBlock.of(stl.season[0]));
//        System.out.println(DataBlock.of(stl.irr));
    }

    @Test
//    @Ignore
    public void testMul() {

        StlSpecification spec = StlSpecification .createDefault(12, false);
//        spec.setMultiplicative(true);
//        spec.setNumberOfOuterIterations(5);
        StlKernel stl = new StlKernel(spec);
        stl.process(Doubles.of(Data.EXPORTS));
//        System.out.println(DataBlock.of(stl.trend));
//        System.out.println(DataBlock.of(stl.season[0]));
//        System.out.println(DataBlock.of(stl.irr));
    }

    @Test
//    @Ignore
    public void testMissing() {

        StlSpecification spec = StlSpecification.createDefault(12, false);
//        spec.setMultiplicative(true);
//        spec.setNumberOfOuterIterations(5);
        StlKernel stl = new StlKernel(spec);
        DataBlock s = DataBlock.copyOf(Data.EXPORTS);
        Random rnd = new Random();
        for (int i = 0; i < 30; ++i) {
            s.set(rnd.nextInt(s.length()), Double.NaN);
        }
        stl.process(s);
//        System.out.println(DataBlock.of(stl.trend));
//        System.out.println(DataBlock.of(stl.season[0]));
//        System.out.println(DataBlock.of(stl.irr));
    }

    @Test
    @Ignore
    public void stressTest() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
//            StlPlusKernel stl = new StlPlusKernel(12, 7);
            StlSpecification spec = StlSpecification.createDefault(12, 7, false);
//            spec.setNumberOfOuterIterations(5);
        StlKernel stl = new StlKernel(spec);
            stl.process(Doubles.of(Data.EXPORTS));
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
