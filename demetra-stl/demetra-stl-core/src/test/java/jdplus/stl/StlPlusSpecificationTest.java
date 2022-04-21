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

import demetra.stl.StlPlusSpecification;
import demetra.stl.LoessSpecification;
import demetra.stl.SeasonalSpecification;
import demetra.data.Doubles;
import jdplus.math.matrices.FastMatrix;
import org.junit.Test;
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
        StlPlusSpecification spec = StlPlusSpecification.robustBuilder()
                .multiplicative(false)
                .trendSpec(LoessSpecification.defaultTrend(52, 7))
                .seasonalSpec(new SeasonalSpecification(52, 7))
                .build();

        StlPlusKernel stl = new StlPlusKernel(spec);
        stl.process(data);
        FastMatrix m = FastMatrix.make(data.length(), 4);
        m.column(0).copyFrom(stl.getY(), 0);
        m.column(1).copyFrom(stl.getTrend(), 0);
        m.column(2).copyFrom(stl.getSeason(0), 0);
        m.column(3).copyFrom(stl.getIrr(), 0);
//        System.out.println(m);
    }

    @Test
    public void testDefaultMul() {
        DoubleSeq data = Doubles.of(WeeklyData.US_CLAIMS);
        // Creates a default stl specification
        StlPlusSpecification spec = StlPlusSpecification.robustBuilder()
                .multiplicative(true)
                .trendSpec(LoessSpecification.defaultTrend(52, 7))
                .seasonalSpec(new SeasonalSpecification(52, 7))
                .build();

        StlPlusKernel stl = new StlPlusKernel(spec);

        stl.process(data);
        FastMatrix m = FastMatrix.make(data.length(), 4);
        m.column(0).copyFrom(stl.getY(), 0);
        m.column(1).copyFrom(stl.getTrend(), 0);
        m.column(2).copyFrom(stl.getSeason(0), 0);
        m.column(3).copyFrom(stl.getIrr(), 0);
        //System.out.println(m);
    }

    @Test
    public void testCustom() {
        DoubleSeq data = Doubles.of(WeeklyData.US_CLAIMS);
        // Creates an empty robust stl specification (robust means 15 outer loops, 1 inner loop).
        SeasonalSpecification sspec = new SeasonalSpecification(52, LoessSpecification.defaultSeasonal(9), LoessSpecification.defaultLowPass(52));
        StlPlusSpecification spec = StlPlusSpecification.robustBuilder()
                .multiplicative(true)
                .trendSpec(LoessSpecification.of(105, 1, 1, null))
                .seasonalSpec(sspec)
                .build();
        StlPlusKernel stl = new StlPlusKernel(spec);
        stl.process(data);

        FastMatrix m = FastMatrix.make(data.length(), 4);
        m.column(0).copyFrom(stl.getY(), 0);
        m.column(1).copyFrom(stl.getTrend(), 0);
        m.column(2).copyFrom(stl.getSeason(0), 0);
        m.column(3).copyFrom(stl.getIrr(), 0);
        System.out.println("specific processing");
        //System.out.println(m);
    }

}
