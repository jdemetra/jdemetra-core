/*
 * Copyright 2015 National Bank of Belgium
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
/*
 */
package ec.tstoolkit.maths.matrices;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PCUser
 */
public class ElementaryTransformationsTest {

    static final int N = 10, R=20;
    static final Matrix M = Matrix.square(N), S=new Matrix(N, R), T=new Matrix(R,N);

    static {
        M.randomize(0);
        S.randomize(0);
        T.randomize(0);
    }

    public ElementaryTransformationsTest() {
    }

    @Test
    public void testGivens() {
        Matrix M1 = M.clone(), M2 = M.clone();
        ElementaryTransformations.rawGivensTriangularize(M1.subMatrix());
        ElementaryTransformations.givensTriangularize(M2.subMatrix());
        assertTrue(M1.minus(M2).nrm2() < 1e-9);
        Matrix S1 = S.clone(), S2 = S.clone();
        ElementaryTransformations.rawGivensTriangularize(S1.subMatrix());
        ElementaryTransformations.givensTriangularize(S2.subMatrix());
        assertTrue(S1.minus(S2).nrm2() < 1e-9);
        Matrix T1 = T.clone(), T2 = T.clone();
        ElementaryTransformations.rawGivensTriangularize(T1.subMatrix());
        ElementaryTransformations.givensTriangularize(T2.subMatrix());
        assertTrue(T1.minus(T2).nrm2() < 1e-9);
    }

    public void stressGivens() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; ++i) {
            Matrix M1 = M.clone();
            ElementaryTransformations.rawGivensTriangularize(M1.subMatrix());
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; ++i) {
            Matrix M1 = M.clone();
            ElementaryTransformations.givensTriangularize(M1.subMatrix());
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; ++i) {
            Matrix M1 = M.clone();
            ElementaryTransformations.householderTriangularize(M1.subMatrix());
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
