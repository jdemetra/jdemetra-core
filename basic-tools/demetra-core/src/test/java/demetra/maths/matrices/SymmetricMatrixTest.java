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
package demetra.maths.matrices;

import demetra.data.NeumaierAccumulator;
import demetra.random.IRandomNumberGenerator;
import demetra.random.MersenneTwister;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SymmetricMatrixTest {

    public SymmetricMatrixTest() {
    }

    @Test
    public void testRandom() {
        Matrix Q = Matrix.square(10);
        SymmetricMatrix.randomize(Q, MersenneTwister.fromSystemNanoTime());
        assertTrue(Q.isSymmetric());
    }

    @Test
    public void testXXt() {
        int N=20, M=10;
        Matrix X = Matrix.make(N, M);
        Random rnd = new Random(0);
        X.set((i, j) -> rnd.nextDouble());
        ec.tstoolkit.maths.matrices.Matrix O = new ec.tstoolkit.maths.matrices.Matrix(N, M);
        Random ornd = new Random(0);
        O.set((i, j) -> ornd.nextDouble());
        assertTrue(MatrixComparator.distance(X, O) == 0);
        assertTrue(MatrixComparator.distance(SymmetricMatrix.XXt(X), ec.tstoolkit.maths.matrices.SymmetricMatrix.XXt(O)) == 0);
        assertTrue(MatrixComparator.distance(SymmetricMatrix.XtX(X), ec.tstoolkit.maths.matrices.SymmetricMatrix.XtX(O)) == 0);
        Matrix R = SymmetricMatrix.robustXtX(X, new NeumaierAccumulator());
        assertTrue(MatrixComparator.distance(SymmetricMatrix.XtX(X), R) < 1e-9);
    }

    @Test
    public void testReenforce() {
        Matrix Q = Matrix.square(20);
        IRandomNumberGenerator rnd = MersenneTwister.fromSystemNanoTime();
        Q.set((i, j) -> rnd.nextDouble());
        SymmetricMatrix.reenforceSymmetry(Q);
        assertTrue(Q.isSymmetric(0));
    }

}
