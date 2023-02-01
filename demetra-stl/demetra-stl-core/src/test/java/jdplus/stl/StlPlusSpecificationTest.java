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
import demetra.stl.LoessSpec;
import demetra.stl.SeasonalSpec;
import demetra.data.Doubles;
import jdplus.math.matrices.FastMatrix;
import org.junit.jupiter.api.Test;
import demetra.data.WeeklyData;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class StlPlusSpecificationTest {

    public StlPlusSpecificationTest() {
    }

    @Test
    public void testDefault() {
        DoubleSeq data = Doubles.of(WeeklyData.US_CLAIMS2);
        // Creates a default stl specification
        StlSpec spec = StlSpec.robustBuilder()
                .multiplicative(false)
                .trendSpec(LoessSpec.defaultTrend(52, 7, false))
                .seasonalSpec(new SeasonalSpec(52, 7, false))
                .build();

        RawStlKernel stl = new RawStlKernel(spec);
        stl.process(data);
//        FastMatrix m = FastMatrix.make(data.length(), 4);
//        m.column(0).copyFrom(stl.getY(), 0);
//        m.column(1).copyFrom(stl.getTrend(), 0);
//        m.column(2).copyFrom(stl.getSeas(), 0);
//        m.column(3).copyFrom(stl.getIrr(), 0);
//        System.out.println(m);
    }

    @Test
    public void testDefaultMul() {
        DoubleSeq data = Doubles.of(WeeklyData.US_CLAIMS);
        // Creates a default stl specification
        StlSpec spec = StlSpec.robustBuilder()
                .multiplicative(true)
                .trendSpec(LoessSpec.defaultTrend(52, 7, false))
                .seasonalSpec(new SeasonalSpec(52, 7, false))
                .build();

        RawStlKernel stl = new RawStlKernel(spec);

        stl.process(data);
//        FastMatrix m = FastMatrix.make(data.length(), 4);
//        m.column(0).copyFrom(stl.getY(), 0);
//        m.column(1).copyFrom(stl.getTrend(), 0);
//        m.column(2).copyFrom(stl.getSeas(), 0);
//        m.column(3).copyFrom(stl.getIrr(), 0);
//        System.out.println(m);
    }

    @Test
    public void testCustom() {
        DoubleSeq data = Doubles.of(WeeklyData.US_CLAIMS);
        // Creates an empty robust stl specification (robust means 15 outer loops, 1 inner loop).
        SeasonalSpec sspec = new SeasonalSpec(52, LoessSpec.defaultSeasonal(9, false), LoessSpec.defaultLowPass(52, false));
        StlSpec spec = StlSpec.robustBuilder()
                .multiplicative(true)
                .trendSpec(LoessSpec.of(105, 1, 1, null))
                .seasonalSpec(sspec)
                .build();
        RawStlKernel stl = new RawStlKernel(spec);
        stl.process(data);

//        FastMatrix m = FastMatrix.make(data.length(), 4);
//        m.column(0).copyFrom(stl.getY(), 0);
//        m.column(1).copyFrom(stl.getTrend(), 0);
//        m.column(2).copyFrom(stl.getSeas(), 0);
//        m.column(3).copyFrom(stl.getIrr(), 0);
//        System.out.println("specific processing");
//        System.out.println(m);
    }

}
