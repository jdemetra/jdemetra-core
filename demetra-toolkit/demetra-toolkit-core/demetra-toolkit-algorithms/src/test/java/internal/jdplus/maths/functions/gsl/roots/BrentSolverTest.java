/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internal.jdplus.maths.functions.gsl.roots;

import internal.jdplus.maths.functions.gsl.roots.BrentSolver;
import org.junit.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class BrentSolverTest {
    
    @Test
    public void test() {
        SolverTestUtils.testAll("Brent", BrentSolver::new);
    }
}
