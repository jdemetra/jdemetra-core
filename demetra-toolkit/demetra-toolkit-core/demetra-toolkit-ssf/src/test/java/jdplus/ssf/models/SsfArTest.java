/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.models;

import jdplus.arima.ssf.SsfAr;
import jdplus.arima.AutoCovarianceFunction;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.polynomials.Polynomial;
import jdplus.ssf.SsfComponent;
import jdplus.ssf.StationaryInitialization;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SsfArTest {
    
    public SsfArTest() {
    }

    @Test
    public void testInitialization() {
                SsfComponent cmp = SsfAr.of(new double[]{1.2, -.6}, 1, 5);
        int dim = cmp.initialization().getStateDim();
        CanonicalMatrix I = StationaryInitialization.of(cmp.dynamics(), dim);
        CanonicalMatrix P = CanonicalMatrix.square(dim);
        cmp.initialization().Pf0(P);
        assertTrue(I.minus(P).frobeniusNorm() < 1e-12);

    }
    
    @Test
    public void testVar(){
        AutoCovarianceFunction fn=new AutoCovarianceFunction(Polynomial.ONE, Polynomial.valueOf(1,-.5, .8),1);
        System.out.println(fn.get(0)); 
    }
    
}
