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
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class CompositeModelTest {
    
    public CompositeModelTest() {
    }
    
    @Test
    public void testBsm() {
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.localLinearTrend("l", 1, .01, true, false));
        model.add(AtomicModels.seasonalComponent("s", "Crude", 12, 1, true));
        model.add(AtomicModels.noise("n", 1, true));
        ModelEquation eq = new ModelEquation("eq1", 0, true);
        eq.add("l",1, true, null);
        eq.add("s",.1, false, null);
        eq.add("n",.1, false, null);
        model.add(eq);
        int len = Data.ABS_RETAIL.length;
        Matrix M = Matrix.make(len, 1);
        M.column(0).copyFrom(Data.ABS_RETAIL, 0);
        CompositeModel.Estimation rslt = model.estimate(M, 1e-15, false);
        System.out.println(DataBlock.ofInternal(rslt.getFullParameters()));
        System.out.println(rslt.getSmoothedStates().getComponent(0));
        System.out.println(rslt.getSmoothedStates().getComponentVariance(0));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }
    
    //@Test
    public void testSimple() {
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.localLinearTrend("tu", 0, .01, true, false));
        model.add(AtomicModels.localLinearTrend("ty", 0, .01, true, false));
        model.add(AtomicModels.localLevel("tpicore", 0, true));
        model.add(AtomicModels.localLevel("tpi", 0, true));
        model.add(AtomicModels.ar("cycle", new double[]{.8,-.5}, false, 1, true, 5));
        
        ModelEquation eq1 = new ModelEquation("eq1", 1, true);
        eq1.add("tu");
        eq1.add("cycle", 0, false, null);
        model.add(eq1);
        
        ModelEquation eq2 = new ModelEquation("eq2", .01, false);
        eq2.add("ty");
        eq2.add("cycle", 0, false, null);
        model.add(eq2);

        ModelEquation eq3 = new ModelEquation("eq3", .01, false);
        eq3.add("tpicore");
        eq3.add("cycle", 0, false, Loading.fromPosition(4));
        model.add(eq3);
        
        ModelEquation eq4 = new ModelEquation("eq4", .01, false);
        eq4.add("tpi");
        eq4.add("cycle", 0, false, null);
        model.add(eq4);
        
        model.build();
        
        Matrix M=Matrix.make(40, 4);
        Random rnd=new Random(0);
        M.set(rnd::nextDouble);
        
//        CompositeModel.Estimation rslt2 = model.estimate(M, 1e-12, false);
//        System.out.println(rslt2.getSmoothedStates().getComponent(0));
//        System.out.println(rslt2.getSmoothedStates().getComponentVariance(0));
    }
}
