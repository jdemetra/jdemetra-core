/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts.survey;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class WaveSpecificSurveyErrorsTest {
    
    public WaveSpecificSurveyErrorsTest() {
    }

    @Test
    public void testTx() {
        WaveSpecificSurveyErrors.Data data=new WaveSpecificSurveyErrors.Data(.4,.3,.2,8);
        WaveSpecificSurveyErrors.Dynamics dyn=new WaveSpecificSurveyErrors.Dynamics(data);
        Matrix T=Matrix.square(16);
        dyn.T(0, T);
        
        DataBlock x=DataBlock.make(16);
        Random rnd=new Random();
        x.set(rnd::nextDouble);
        
        DataBlock y=DataBlock.make(16);
        y.product(T.rowsIterator(), x);
        dyn.TX(0, x);
        
        assertTrue(y.distance(x)<1e-9);
        
    }
    
}
