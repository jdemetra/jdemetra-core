/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.implementations;

import demetra.ar.ArBuilder;
import demetra.arima.ArimaModel;
import demetra.arima.ssf.SsfArima;
import demetra.ssf.models.LocalLevel;
import demetra.ssf.models.LocalLinearTrend;
import demetra.ssf.multivariate.IMultivariateSsf;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class MultivariateCompositeSsfTest {
    
    public MultivariateCompositeSsfTest() {
    }

    @Test
    public void testDavid() {
        ArimaModel ar = new ArBuilder()
                .ar(.3, .2)
                .innovationVariance(1)
                .build();
        
        MultivariateCompositeSsf.Equation eq1=new  MultivariateCompositeSsf.Equation(.1);
        MultivariateCompositeSsf.Equation eq2=new  MultivariateCompositeSsf.Equation(.11);
        MultivariateCompositeSsf.Equation eq3=new  MultivariateCompositeSsf.Equation(.12);
        
        
        IMultivariateSsf mssf = MultivariateCompositeSsf.builder()
                .add("cycle", SsfArima.componentOf(ar))
                .add("tpi", LocalLevel.of(.3))
                .add("ty", LocalLinearTrend.of(.2, 0) )
                .add("tu", LocalLinearTrend.of(.2, 0))
                .add(eq3)
                .add(eq3)
                .add(eq3)
                .build();
                
                
    }
    
}
