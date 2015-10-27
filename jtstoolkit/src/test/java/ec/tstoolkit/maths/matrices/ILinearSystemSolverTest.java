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

package ec.tstoolkit.maths.matrices;

import ec.tstoolkit.data.DataBlock;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class ILinearSystemSolverTest {

    public ILinearSystemSolverTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testLU() {
        int n = 500;
        Matrix M = new Matrix(n, n);
        M.randomize();
        Matrix N = new Matrix(n, n / 2);
        N.randomize();

        ILinearSystemSolver solver = new CroutDoolittle();
        solver.decompose(M);
        Matrix rslt1 = solver.solve(N);

        solver = new Gauss();
        solver.decompose(M);
        Matrix rslt2 = solver.solve(N);

        Matrix del = rslt1.minus(rslt2);
        assertTrue(del.nrm2() < 1e-5);
    }

    @Test
    public void testQR() {
        int n = 5;
        Matrix M = new Matrix(n, n);
        M.randomize();
        Matrix N = new Matrix(n, n / 2);
        N.randomize();

        ILinearSystemSolver solver = new ThinHouseholder();
        solver.decompose(M);
        Matrix rslt1 = solver.solve(N);

        solver = new Householder(true);
        solver.decompose(M);
        Matrix rslt2 = solver.solve(N);

        Matrix del1 = rslt1.minus(rslt2);
        assertTrue(del1.nrm2()<1e-5);

        solver = new HouseholderR(true);
        solver.decompose(M);
        Matrix rslt3 = solver.solve(N);

        Matrix del2 = rslt2.minus(rslt3);
        assertTrue(del2.nrm2() < 1e-5);
    }

    @Test
    public void testLeastSquares() {
        int n = 100, m=10;
        Matrix M = new Matrix(n, m);
        M.randomize();
        DataBlock Z = new DataBlock(n);
        Z.randomize(0);
        DataBlock B1 = new DataBlock(M.getColumnsCount());
        DataBlock E1 = new DataBlock(M.getRowsCount() - M.getColumnsCount());
        IQrDecomposition solver = new ThinHouseholder();
        solver.decompose(M);
        solver.leastSquares(Z, B1, E1);

        DataBlock B2 = new DataBlock(M.getColumnsCount());
        DataBlock E2 = new DataBlock(M.getRowsCount() - M.getColumnsCount());
        solver = new Householder(true);
        solver.decompose(M);
        solver.leastSquares(Z, B2, E2);
        double eps = B1.distance(B2);
        assertTrue(eps < 1e-5);

        DataBlock B3 = new DataBlock(M.getColumnsCount());
        DataBlock E3 = new DataBlock(M.getRowsCount() - M.getColumnsCount());
        solver = new HouseholderR(true);
        solver.decompose(M);
        solver.leastSquares(Z, B3, E3);
        eps = B2.distance(B3);
        assertTrue(eps < 1e-5);
    }
}
