/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.maths.polynomials;

import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.matrices.lapack.Dlamch;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PCUser
 */
public class GrantHitchinsSolverTest {
    
    public GrantHitchinsSolverTest() {
    }

    @Test
    public void testUnitRootd() {
        int N=12;
        double[] c=new double[N+1];
        c[0]=1;
        c[N]=-1;
        GrantHitchinsSolver solver=new GrantHitchinsSolver();
        assertTrue(solver.factorize(Polynomial.of(c)));
    }
    
}
