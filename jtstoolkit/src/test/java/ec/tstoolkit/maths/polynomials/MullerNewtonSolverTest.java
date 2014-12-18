/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/

package ec.tstoolkit.maths.polynomials;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.Complex;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pcuser
 */
public class MullerNewtonSolverTest {

    public MullerNewtonSolverTest() {
    }

    @Test
    public void testSolver() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 3; ++i) {
            DataBlock q = new DataBlock(200);
            q.randomize();
            Polynomial p = Polynomial.fromData(q);
            MullerNewtonSolver s = new MullerNewtonSolver();
            //s.setLeastSquaresDivision(true);
            s.factorize(p);
            Complex[] roots = s.roots();
            Complex.lejaOrder(roots);
            Polynomial x = Polynomial.fromComplexRoots(roots, p.get(p.getDegree()));
            System.out.println(x.minus(p));
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
