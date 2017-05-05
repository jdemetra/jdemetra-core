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
package demetra.maths.matrices.impl;

import demetra.maths.matrices.impl.SingularValueDecomposition;
import demetra.maths.matrices.Matrix;
import java.util.Random;
import demetra.maths.matrices.MatrixComparator;
import demetra.maths.matrices.impl.SingularValueDecomposition;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SingularValueDecompositionTest {

    public SingularValueDecompositionTest() {
    }

    @Test
    public void testDecomposition() {
        int n = 130, m = 13;
        Matrix M = Matrix.make(n, m);
        Random rnd=new Random(0);
        M.set(()->rnd.nextDouble());
        SingularValueDecomposition svd = new SingularValueDecomposition();
        svd.decompose(M);
        Matrix V = svd.V();
        Matrix U = svd.U();
        Matrix D = Matrix.diagonal(svd.S());
        // M = U*D*V'
        Matrix Q = U.times(D).times(V.transpose());
        assertTrue(MatrixComparator.distance(M, Q) < 1e-9);
//        Matrix V2 = Matrix.make(m, m);
//        V2.subMatrix().product(V.subMatrix(), V.subMatrix().transpose());
//        del = V2.minus(Matrix.identity(m));
//        assertTrue(del.nrm2() < 1e-9);
//        Matrix U2 = new Matrix(m, m);
//        U2.subMatrix().product(U.subMatrix().transpose(), U.subMatrix());
//        del = U2.minus(Matrix.identity(m));
//        assertTrue(del.nrm2() < 1e-9);
    }

}
