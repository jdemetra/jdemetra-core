/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.math.matrices;

import jdplus.math.matrices.decomposition.ElementaryTransformations;
import jdplus.data.DataBlock;
import jdplus.leastsquares.QRSolution;
import jdplus.leastsquares.QRSolver;
import jdplus.random.JdkRNG;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class ElementaryTransformationsTest {

    public ElementaryTransformationsTest() {
    }

    @Test
    public void testQRGivens() {
        JdkRNG rng = JdkRNG.newRandom(0);
        Matrix M = Matrix.make(20, 5);
        MatrixUtility.randomize(M, rng);
        Matrix cur = M;
        for (int i = 0; i < M.getColumnsCount() - 1; ++i) {
            ElementaryTransformations.columnGivens(cur);
            cur = cur.extract(1, cur.getRowsCount() - 1, 1, cur.getColumnsCount() - 1);
        }
        DataBlock b = M.column(4).range(0, 4);
        UpperTriangularMatrix.solveUx(M.extract(0, 4, 0, 4), b);
        System.out.println(b);

        M = Matrix.make(20, 5);
        MatrixUtility.randomize(M, rng);

        QRSolution ls = QRSolver.fastLeastSquares(M.column(4), M.extract(0, 20, 0, 4));
        DataBlock b2 = DataBlock.of(ls.getB());
//        System.out.println(b2);
    }

}
