/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internal.jdplus.math.functions.gsl.roots;

import internal.jdplus.math.functions.gsl.roots.NewtonSolver;
import org.junit.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class NewtonSolverTest {
    
    @Test
    public void test() {
        SolverTestUtils.testAll("Newton", NewtonSolver::new);
    }
}
