/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.msts.AtomicModels;
import demetra.msts.ModelEquation;
import demetra.data.Data;
import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.msts.CompositeModel;
import demetra.msts.CompositeModelEstimation;
import demetra.ssf.implementations.Loading;
import demetra.timeseries.TsDomain;
import java.util.Random;
import org.junit.Test;

/**
 *
 * @author PALATEJ
 */
public class AlgorithmsTest {

    public AlgorithmsTest() {
    }

    @Test
    public void testBsm() {
        int len = Data.ABS_RETAIL.length;
        Matrix M = Matrix.make(len, 1);
        M.column(0).copyFrom(Data.ABS_RETAIL, 0);
        M.column(0).apply(q->Math.log(q));
        
        double[] w=new double[len];
        w[0]=1;
        Random rnd=new Random();
        for (int i=1; i<len; ++i){
            w[i]=.9*w[i-1]+rnd.nextGaussian()*.01;
        }
        
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.localLinearTrend("l", .01, .01, false, false));
        model.add(AtomicModels.seasonalComponent("s", "Crude", 12, .01, false));
//        model.add(AtomicModels.rawTdRegression("td", Data.TS_ABS_RETAIL.getDomain(), new int[]{1,1,1,1,1,0,0}, new double[]{0.01, 0.01}, false));
        model.add(AtomicModels.sae("n", new double[]{0.5, 0.2}, false, 0.0001, true, 1, false));
        ModelEquation eq = new ModelEquation("eq1", 1, false);
        eq.add("l");
        eq.add("s");
//        eq.add("td");
        eq.add("n");//, 1, true, Loading.rescale(Loading.fromPosition(0), w));
        model.add(eq);
//        ModelEquation eqs = new ModelEquation("eqs", 0, true);
//        eqs.add("td", 1, true, Loading.sum());
//        model.add(eqs);
//        System.out.println(DataBlock.ofInternal(model.defaultParameters()));
//        System.out.println(DataBlock.ofInternal(model.fullDefaultParameters()));

        CompositeModelEstimation rslt = model.estimate(M, 1e-12, false, true, null);

        double[] p = rslt.getFullParameters();
        System.out.println("SAE+TD");
        System.out.println(DataBlock.ofInternal(p));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }
    
    //@Test
    public void testAirline() {
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.sarima("air", 12, new int[]{0,1,1}, new int[]{0,1,1}, new double[]{-.2, -.2}));
        model.add(AtomicModels.tdRegression("td", Data.TS_ABS_RETAIL.getDomain(), new int[]{1,1,1,1,1,0,0}, false, 0.01, false));
        ModelEquation eq = new ModelEquation("eq1", 1, true);
        eq.add("air");
        eq.add("td");
        model.add(eq);
//        System.out.println(DataBlock.ofInternal(model.defaultParameters()));
//        System.out.println(DataBlock.ofInternal(model.fullDefaultParameters()));

        int len = Data.ABS_RETAIL.length;
        Matrix M = Matrix.make(len, 1);
        M.column(0).copyFrom(Data.ABS_RETAIL, 0);
        M.column(0).apply(q->Math.log(q));
        CompositeModelEstimation rslt = model.estimate(M, 1e-12, false, true, null);

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
        CompositeModelEstimation rslt = model.estimate(M, 1e-12, false, true, null);

        double[] p = rslt.getFullParameters();
        System.out.println("Crude");
        System.out.println(DataBlock.ofInternal(p));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }
    
    //@Test
    public void testBsmBis() {
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.localLinearTrend("l", .1, .1, false, false));
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
        CompositeModelEstimation rslt = model.estimate(M, 1e-12, false, false, null);

        double[] p = rslt.getFullParameters();
        System.out.println("Dummy-non concentrated");
        System.out.println(DataBlock.ofInternal(p));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }

    //@Test
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
        CompositeModelEstimation rslt = model.estimate(M, 1e-12, false, false, null);

        double[] p = rslt.getFullParameters();
        System.out.println("Crude-Non concentrated");
        System.out.println(DataBlock.ofInternal(p));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }
}
