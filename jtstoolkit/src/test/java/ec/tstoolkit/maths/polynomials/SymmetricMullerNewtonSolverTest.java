/*
 * Copyright 2016 National Bank of Belgium
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

import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SymmetricMullerNewtonSolverTest {
    
    public SymmetricMullerNewtonSolverTest() {
    }

    @Test
    public void test1() {
        BackFilter f=BackFilter.of(new double[]{1, -.2});
        SymmetricFilter sf=SymmetricFilter.createFromFilter(f);
        SymmetricMullerNewtonSolver solver=new SymmetricMullerNewtonSolver();
        Polynomial P = Polynomial.copyOf(sf.getWeights());
        Complex[] roots = P.roots(solver);
//        for (int i=0; i<roots.length; ++i){
//            System.out.println(roots[i]);
//        }
    }
    
    @Test
    public void test2() {
        BackFilter f1=BackFilter.of(new double[]{1, -.7, .1});
        BackFilter f2=BackFilter.of(new double[]{1, .6, .5});
        BackFilter f=f1.times(f2);
        SymmetricFilter sf=SymmetricFilter.createFromFilter(f);
        SymmetricMullerNewtonSolver solver=new SymmetricMullerNewtonSolver();
        Polynomial P = Polynomial.copyOf(sf.getWeights());
        Complex[] roots = P.roots(solver);
//        for (int i=0; i<roots.length; ++i){
//            System.out.println(roots[i]);
//        }
    }
}
