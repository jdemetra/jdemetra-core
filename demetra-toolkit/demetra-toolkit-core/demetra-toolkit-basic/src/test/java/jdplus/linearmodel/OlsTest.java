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
package jdplus.linearmodel;

import jdplus.stats.linearmodel.LinearModel;
import jdplus.stats.linearmodel.LeastSquaresResults;
import jdplus.stats.linearmodel.Ols;
import demetra.data.DataSets;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;
import jdplus.math.matrices.GeneralMatrix;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixNorms;

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

        LinearModel model = LinearModel.builder()
                .y(DoubleSeq.of(y))
                .meanCorrection(true)
                .addX(DoubleSeq.of(DataSets.Longley.x1))
                .addX(DoubleSeq.of(DataSets.Longley.x2))
                .addX(DoubleSeq.of(DataSets.Longley.x3))
                .addX(DoubleSeq.of(DataSets.Longley.x4))
                .addX(DoubleSeq.of(DataSets.Longley.x5))
                .addX(DoubleSeq.of(DataSets.Longley.x6))
                .build();

        LeastSquaresResults rslts = Ols.compute(model);
//        System.out.println("Longley");
//        System.out.println(rslts);
        assertEquals(Math.abs(rslts.Ttest(0).getValue()), Math.sqrt(rslts.Ftest(0, 1).getValue()), 1e-9);
        assertEquals(rslts.Ftest().getValue(), rslts.Ftest(1, model.getVariablesCount() - 1).getValue(), 1e-9);
        assertEquals(rslts.Ftest().getValue(), DataSets.Longley.FTest, 1e-9);
        FastMatrix P = rslts.projectionMatrix();
        assertTrue(MatrixNorms.absNorm(P.minus(GeneralMatrix.AB(P, P))) < 1e-6);
    }

    @Test
    public void testFilip() {
        double[] y = DataSets.Filip.y;
        DoubleSeq x = DoubleSeq.of(DataSets.Filip.x);
        LinearModel model = LinearModel.builder()
                .y(DoubleSeq.of(y))
                .meanCorrection(true)
                .addX(x)
                .addX(x.map(a -> a * a))
                .addX(x.map(a -> a * a * a))
                .addX(x.map(a -> a * a * a * a))
                .addX(x.map(a -> a * a * a * a * a))
                .addX(x.map(a -> a * a * a * a * a * a))
                .addX(x.map(a -> a * a * a * a * a * a * a))
                .addX(x.map(a -> a * a * a * a * a * a * a * a))
                .addX(x.map(a -> a * a * a * a * a * a * a * a * a))
                .addX(x.map(a -> a * a * a * a * a * a * a * a * a * a))
                .build();

        LeastSquaresResults rslts = Ols.compute(model);
//        System.out.println("Filip");
//        System.out.println(rslts);
        assertEquals(Math.abs(rslts.Ttest(0).getValue()), Math.sqrt(rslts.Ftest(0, 1).getValue()), 1e-9);
//        assertEquals(rslts.Ftest().getValue(), rslts.Ftest(1, model.getVariablesCount()-1).getValue(), 1e-9 );
    }

    @Test
    public void testSingular() {
        double[] y = DataSets.Filip.y;
        DoubleSeq x = DoubleSeq.of(DataSets.Filip.x);
        LinearModel model = LinearModel.builder()
                .y(DoubleSeq.of(y))
                .meanCorrection(true)
                .addX(x)
                .addX(x.map(a -> a * a))
                .addX(x.map(a -> a * a * a))
                .addX(x.map(a -> -a * a * a))
                .addX(x.map(a -> 0))
                .addX(x.map(a -> a * a * a * a * a))
                .build();

        LeastSquaresResults rslts = Ols.compute(model);
        System.out.println(rslts.getR2());
    }

}
