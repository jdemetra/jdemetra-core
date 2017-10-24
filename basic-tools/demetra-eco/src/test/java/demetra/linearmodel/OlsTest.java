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
package demetra.linearmodel;

import demetra.data.DataSets;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate
 */
public class OlsTest {

    public OlsTest() {
    }

    @Test
    public void testLongley() {
        double[] y = DataSets.Longley.y;

        LinearModel model = LinearModel.of(DoubleSequence.ofInternal(y))
                .meanCorrection(true)
                .addX(DoubleSequence.ofInternal(DataSets.Longley.x1))
                .addX(DoubleSequence.ofInternal(DataSets.Longley.x2))
                .addX(DoubleSequence.ofInternal(DataSets.Longley.x3))
                .addX(DoubleSequence.ofInternal(DataSets.Longley.x4))
                .addX(DoubleSequence.ofInternal(DataSets.Longley.x5))
                .addX(DoubleSequence.ofInternal(DataSets.Longley.x6))
                .build();

        Ols ols = new Ols();
        LeastSquaresResults rslts = ols.compute(model);
        System.out.println("Longley");
        System.out.println(rslts);
        
        assertEquals(Math.abs(rslts.Ttest(0).getValue()), Math.sqrt(rslts.Ftest(0, 1).getValue()), 1e-9 );
        assertEquals(rslts.Ftest().getValue(), rslts.Ftest(1, model.getVariablesCount()-1).getValue(), 1e-9 );
    }

    @Test
    public void testFilip() {
        double[] y = DataSets.Filip.y;
        DoubleSequence x=DoubleSequence.ofInternal(DataSets.Filip.x);
        LinearModel model = LinearModel.of(DoubleSequence.ofInternal(y))
                .meanCorrection(true)
                .addX(x)
                .addX(DoubleSequence.transformation(x, a -> a * a))
                .addX(DoubleSequence.transformation(x, a -> a * a * a))
                .addX(DoubleSequence.transformation(x, a -> a * a * a * a))
                .addX(DoubleSequence.transformation(x, a -> a * a * a * a * a))
                .addX(DoubleSequence.transformation(x, a -> a * a * a * a * a * a))
                .addX(DoubleSequence.transformation(x, a -> a * a * a * a * a * a * a))
                .addX(DoubleSequence.transformation(x, a -> a * a * a * a * a * a * a * a))
                .addX(DoubleSequence.transformation(x, a -> a * a * a * a * a * a * a * a * a))
                .addX(DoubleSequence.transformation(x, a -> a * a * a * a * a * a * a * a * a * a))
                .build();

        Ols ols = new Ols();
        LeastSquaresResults rslts = ols.compute(model);
        System.out.println("Filip");
        System.out.println(rslts);
        assertEquals(Math.abs(rslts.Ttest(0).getValue()), Math.sqrt(rslts.Ftest(0, 1).getValue()), 1e-9 );
//        assertEquals(rslts.Ftest().getValue(), rslts.Ftest(1, model.getVariablesCount()-1).getValue(), 1e-9 );
    }
}
