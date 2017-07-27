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
import demetra.data.Doubles;
import org.junit.Test;
import static org.junit.Assert.*;

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

        LinearModel model = LinearModel.of(Doubles.ofInternal(y))
                .meanCorrection(true)
                .addX(Doubles.ofInternal(DataSets.Longley.x1))
                .addX(Doubles.ofInternal(DataSets.Longley.x2))
                .addX(Doubles.ofInternal(DataSets.Longley.x3))
                .addX(Doubles.ofInternal(DataSets.Longley.x4))
                .addX(Doubles.ofInternal(DataSets.Longley.x5))
                .addX(Doubles.ofInternal(DataSets.Longley.x6))
                .build();

        Ols ols = new Ols();
        OlsResults rslts = ols.compute(model);
        System.out.println("Longley");
        System.out.println(rslts);
    }

    @Test
    public void testFilip() {
        double[] y = DataSets.Filip.y;
        Doubles x=Doubles.ofInternal(DataSets.Filip.x);
        LinearModel model = LinearModel.of(Doubles.ofInternal(y))
                .meanCorrection(true)
                .addX(x)
                .addX(Doubles.transformation(x, a -> a * a))
                .addX(Doubles.transformation(x, a -> a * a * a))
                .addX(Doubles.transformation(x, a -> a * a * a * a))
                .addX(Doubles.transformation(x, a -> a * a * a * a * a))
                .addX(Doubles.transformation(x, a -> a * a * a * a * a * a))
                .addX(Doubles.transformation(x, a -> a * a * a * a * a * a * a))
                .addX(Doubles.transformation(x, a -> a * a * a * a * a * a * a * a))
                .addX(Doubles.transformation(x, a -> a * a * a * a * a * a * a * a * a))
                .addX(Doubles.transformation(x, a -> a * a * a * a * a * a * a * a * a * a))
                .build();

        Ols ols = new Ols();
        OlsResults rslts = ols.compute(model);
        System.out.println("Filip");
        System.out.println(rslts);
    }
}
