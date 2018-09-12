/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.data.Data;
import demetra.data.DataBlock;
import demetra.maths.MatrixType;
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
        model.add(AtomicModels.localLinearTrend("l", .01, .01, false, false));
        model.add(AtomicModels.seasonalComponent("s", "Dummy", 12, 1, true));
        model.add(AtomicModels.noise("n", .01, false));
        ModelEquation eq = new ModelEquation("eq1", 0, true);
        eq.add("l");
        eq.add("s");
        eq.add("n");
        model.add(eq);
//        System.out.println(DataBlock.ofInternal(model.defaultParameters()));
//        System.out.println(DataBlock.ofInternal(model.fullDefaultParameters()));

        int len = Data.ABS_RETAIL.length;
        Matrix M = Matrix.make(len, 1);
        M.column(0).copyFrom(Data.ABS_RETAIL, 0);
        CompositeModel.Estimation rslt = model.estimate(M, 1e-12, false);

        double[] p = rslt.getFullParameters();
//        System.out.println("Dummy");
//        System.out.println(DataBlock.ofInternal(p));
//        System.out.println(rslt.getLikelihood().logLikelihood());
    }

    @Test
    public void testBsm2() {
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.localLinearTrend("l", .01, .01, false, false));
        model.add(AtomicModels.seasonalComponent("s", "Crude", 12, .01, false));
        ModelEquation eq = new ModelEquation("eq1", 1, true);
        eq.add("l");
        eq.add("s");
        model.add(eq);

        int len = Data.ABS_RETAIL.length;
        Matrix M = Matrix.make(len, 1);
        M.column(0).copyFrom(Data.ABS_RETAIL, 0);
        CompositeModel.Estimation rslt = model.estimate(M, 1e-12, false);

        double[] p = rslt.getFullParameters();
//        System.out.println("Crude");
//        System.out.println(DataBlock.ofInternal(p));
//        System.out.println(rslt.getLikelihood().logLikelihood());
    }
}
