/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.data.Data;
import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class AlgorithmsTest {

    public AlgorithmsTest() {
    }

    @Test
    public void testBsm() {
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.localLinearTrend("l", -1, -1));
        model.add(AtomicModels.seasonalComponent("s", "Dummy", 12, 1));
        model.add(AtomicModels.noise("n", -1));
        ModelEquation eq = ModelEquation.withFixedError("eq1", 0);
        eq.add("l");
        eq.add("s");
        eq.add("n");
        model.add(eq);
        model.build();

        int len = Data.ABS_RETAIL.length;
        Matrix M = Matrix.make(len, 1);
        M.column(0).copyFrom(Data.ABS_RETAIL, 0);

        double[] p = Algorithms.estimate(model, M);
        System.out.println(DataBlock.ofInternal(p));
    }

    @Test
    public void testBsm2() {
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.localLinearTrend("l", -1, -1));
        model.add(AtomicModels.seasonalComponent("s", "Crude", 12, -1));
        ModelEquation eq = ModelEquation.withFixedError("eq1", 1);
        eq.add("l");
        eq.add("s");
        model.add(eq);
        model.build();

        int len = Data.ABS_RETAIL.length;
        Matrix M = Matrix.make(len, 1);
        M.column(0).copyFrom(Data.ABS_RETAIL, 0);

        double[] p = Algorithms.estimate(model, M);
        System.out.println(DataBlock.ofInternal(p));
    }
}
