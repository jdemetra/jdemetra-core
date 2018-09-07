/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.data.Data;
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
        model.add(AtomicModels.localLinearTrend("l", -1, -1));
        model.add(AtomicModels.seasonalComponent("s", "Crude", 12, -1));
        ModelEquation eq = ModelEquation.withFixedError("eq1", 1);
        eq.add("l");
        eq.add("s");
        model.add(eq);
        int len = Data.ABS_RETAIL.length;
        Matrix M = Matrix.make(len, 1);
        M.column(0).copyFrom(Data.ABS_RETAIL, 0);
        CompositeModel.Estimation rslt = model.estimate(M, false);
        System.out.println(rslt.getSmoothedStates().getComponent(0));
    }
    
    @Test
    public void testSimple() {
        CompositeModel model = new CompositeModel();
        model.add(AtomicModels.localLinearTrend("tu", 0, -1));
        model.add(AtomicModels.localLinearTrend("ty", 0, -1));
        model.add(AtomicModels.localLevel("tpicore", 0));
        model.add(AtomicModels.localLevel("tpi", 0));
        model.add(AtomicModels.ar("cycle", 2, new double[]{.8,-.5}, -1, 5, false));
        
        ModelEquation eq1 = ModelEquation.withFixedError("eq1", 1);
        eq1.add("tu");
        eq1.add("cycle", null, null);
        model.add(eq1);
        
        ModelEquation eq2 = ModelEquation.withError("eq2");
        eq2.add("ty");
        eq2.add("cycle", null, null);
        model.add(eq2);

        ModelEquation eq3 = ModelEquation.withError("eq3");
        eq3.add("tpicore");
        eq3.add("cycle", null, Loading.fromPosition(4));
        model.add(eq3);
        
        ModelEquation eq4 = ModelEquation.withError("eq4");
        eq4.add("tpi");
        eq4.add("cycle", null, null);
        model.add(eq4);
        
        model.build();
        
        Matrix M=Matrix.make(40, 4);
        Random rnd=new Random(0);
        M.set(rnd::nextDouble);
        
        CompositeModel.Estimation rslt2 = model.estimate(M, false);
    }
}
