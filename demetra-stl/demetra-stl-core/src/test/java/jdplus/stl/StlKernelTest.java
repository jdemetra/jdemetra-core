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

import demetra.stl.StlSpec;
import demetra.data.Data;
import demetra.data.Doubles;
import demetra.stl.LoessSpec;
import demetra.stl.SeasonalSpec;
import jdplus.data.DataBlock;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

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
        StlSpec spec = StlSpec.builder()
                .multiplicative(true)
                .seasonalSpec(new SeasonalSpec(12, 7, false))
                .trendSpec(LoessSpec.defaultTrend(12, 7, false))
                .innerLoopsCount(2)
                .outerLoopsCount(5)
                .build();
        StlKernel stl = new StlKernel(spec);
        double[] data=Data.EXPORTS.clone();
        data[13]=Double.NaN;
        data[14]=Double.NaN;
        stl.process(Doubles.of(data));
//        System.out.println(DoubleSeq.of(stl.getTrend()));
//        System.out.println(DoubleSeq.of(stl.getSeas()));
//        System.out.println(DoubleSeq.of(stl.getIrr()));
    }

    @Test
//    @Ignore
    public void testLargeFilter() {
        StlSpec spec = StlSpec.builder()
                .seasonalSpec(new SeasonalSpec(12, 7, false))
                .trendSpec(LoessSpec.of(21, 1, false))
                .build();
        StlKernel stl = new StlKernel(spec);
        StlResults rslt = stl.process(Doubles.of(Data.EXPORTS));
//        System.out.println(rslt.getTrend());
//        System.out.println(rslt.getSeasonal());
//        System.out.println(rslt.getIrregular());
    }

    @Test
    //@Ignore
    public void testSpec() {

        StlSpec spec = StlSpec
                .createDefault(12, false);
        StlKernel stl = new StlKernel(spec);
        StlResults rslt = stl.process(Doubles.of(Data.EXPORTS));
//        System.out.println(rslt.getSeries());
//        System.out.println(rslt.getTrend());
//        System.out.println(rslt.getSeasonal());
//        System.out.println(rslt.getIrregular());
    }

    @Test
//    @Ignore
    public void testMul() {

        StlSpec spec = StlSpec .createDefault(12, false);
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

        StlSpec spec = StlSpec.createDefault(12, false);
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
    @Disabled
    public void stressTest() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
//            StlPlusKernel stl = new StlPlusKernel(12, 7);
            StlSpec spec = StlSpec.createDefault(12, 7, false);
//            spec.setNumberOfOuterIterations(5);
        StlKernel stl = new StlKernel(spec);
            stl.process(Doubles.of(Data.EXPORTS));
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
