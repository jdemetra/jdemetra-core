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
        Matrix M = new Matrix(n, m);
        M.randomize();
        SingularValueDecomposition svd = new SingularValueDecomposition();
        svd.decompose(M);
        Matrix V = svd.V();
        Matrix U = svd.U();
        Matrix D = Matrix.diagonal(svd.S());
        // M = U*D*V'
        Matrix Q = U.times(D).times(V.transpose());
        Matrix del = M.minus(Q);
        assertTrue(del.nrm2() < 1e-9);
        Matrix V2 = new Matrix(m, m);
        V2.subMatrix().product(V.subMatrix(), V.subMatrix().transpose());
        del = V2.minus(Matrix.identity(m));
        assertTrue(del.nrm2() < 1e-9);
        Matrix U2 = new Matrix(m, m);
        U2.subMatrix().product(U.subMatrix().transpose(), U.subMatrix());
        del = U2.minus(Matrix.identity(m));
        assertTrue(del.nrm2() < 1e-9);
    }

    @Test
    public void testPca() {
        int n = 130, m = 13;
        Matrix M = new Matrix(n, m);
        M.randomize();
        SingularValueDecomposition svd = new SingularValueDecomposition();
        svd.decompose(M);
        Matrix V = svd.V();
        Matrix U = svd.U();
        Matrix D = Matrix.diagonal(svd.S());
        // M = U*D*V'
        double[] s = svd.S();
        for (int i = 0; i < s.length; ++i) {
            System.out.print(s[i]);
            System.out.print('\t');
        }
        System.out.println();
        
        Matrix M2=M.clone();
        for (int i=0; i<m; ++i){
            M2.column(i).addAY(-s[0]*V.get(i, 0),U.column(0));
        }
        SingularValueDecomposition svd2 = new SingularValueDecomposition();
        svd2.decompose(M2);
        double[] s2 = svd2.S();
         for (int i = 0; i < s2.length; ++i) {
            System.out.print(s2[i]);
            System.out.print('\t');
        }
        System.out.println();
       
    }
}
