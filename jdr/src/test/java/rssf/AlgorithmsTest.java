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
import demetra.ssf.implementations.Loading;
import demetra.timeseries.TsDomain;
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
        model.add(AtomicModels.seasonalComponent("s", "Crude", 12, .01, false));
        model.add(AtomicModels.rawTdRegression("td", Data.TS_ABS_RETAIL.getDomain(), new int[]{1,1,1,1,1,0,0}, new double[]{0.01, 0.01}, false));
        ModelEquation eq = new ModelEquation("eq1", 1, true);
        eq.add("l");
        eq.add("s");
        eq.add("td");
        model.add(eq);
        ModelEquation eqs = new ModelEquation("eqs", 0, true);
        eqs.add("td", 1, true, Loading.sum());
        model.add(eqs);
//        System.out.println(DataBlock.ofInternal(model.defaultParameters()));
//        System.out.println(DataBlock.ofInternal(model.fullDefaultParameters()));

        int len = Data.ABS_RETAIL.length;
        Matrix M = Matrix.make(len, 2);
        M.column(0).copyFrom(Data.ABS_RETAIL, 0);
        M.column(0).apply(q->Math.log(q));
        CompositeModel.Estimation rslt = model.estimate(M, 1e-12, false, true, null);

        double[] p = rslt.getFullParameters();
        System.out.println("Crude+TD");
        System.out.println(DataBlock.ofInternal(p));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }

    //@Test
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
        CompositeModel.Estimation rslt = model.estimate(M, 1e-12, false, true, null);

        double[] p = rslt.getFullParameters();
        System.out.println("Crude");
        System.out.println(DataBlock.ofInternal(p));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }
    
    @Test
    public void testBsmBis() {
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.localLinearTrend("l", .01, .01, false, false));
        model.add(AtomicModels.seasonalComponent("s", "Dummy", 12, 1, false));
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
        CompositeModel.Estimation rslt = model.estimate(M, 1e-12, false, false, null);

        double[] p = rslt.getFullParameters();
        System.out.println("Dummy-non concentrated");
        System.out.println(DataBlock.ofInternal(p));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }

    @Test
    public void testBsm2Bis() {
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.localLinearTrend("l", .01, .01, false, false));
        model.add(AtomicModels.seasonalComponent("s", "Crude", 12, .01, false));
        ModelEquation eq = new ModelEquation("eq1", 1, false);
        eq.add("l");
        eq.add("s");
        model.add(eq);

        int len = Data.ABS_RETAIL.length;
        Matrix M = Matrix.make(len, 1);
        M.column(0).copyFrom(Data.ABS_RETAIL, 0);
        CompositeModel.Estimation rslt = model.estimate(M, 1e-12, false, false, null);

        double[] p = rslt.getFullParameters();
        System.out.println("Crude-Non concentrated");
        System.out.println(DataBlock.ofInternal(p));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }
}
