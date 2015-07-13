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
public class MatrixTest {

    public MatrixTest() {
    }

    @Test
    public void testSelection() {
        Matrix M = new Matrix(11, 10);
        M.randomize();
        boolean[] rsel = new boolean[M.getRowsCount()];
        boolean[] csel = new boolean[M.getColumnsCount()];
        for (int i = 0; i < rsel.length; ++i) {
            if (i % 3 == 0) {
                rsel[i] = true;
            }
        }
        for (int i = 0; i < csel.length; ++i) {
            if (i % 2 == 0) {
                csel[i] = true;
            }
        }
        Matrix mr = Matrix.selectRows(M.subMatrix(), rsel);
        Matrix mrc = Matrix.selectColumns(mr.subMatrix(), csel);

        assertTrue(Matrix.select(M.subMatrix(), rsel, csel).equals(mrc));
        DataBlock m0 = DataBlock.select(M.column(0), rsel);
        assertTrue(mrc.column(0).distance(m0) == 0);

        int[] irsel = new int[]{1, 2, 3, 5};
        int[] icsel = new int[]{1, 3, 5, 8};

        mr = Matrix.selectRows(M.subMatrix(), irsel);
        mrc = Matrix.selectColumns(mr.subMatrix(), icsel);

        assertTrue(Matrix.select(M.subMatrix(), irsel, icsel).equals(mrc));
        m0 = DataBlock.select(M.column(icsel[0]), irsel);
        assertTrue(mrc.column(0).distance(m0) == 0);
    }

    @Test
    public void testcopy() {
        Matrix M = new Matrix(11, 10);
        M.randomize();
        boolean[] rsel = new boolean[M.getRowsCount()];
        boolean[] csel = new boolean[M.getColumnsCount()];
        for (int i = 0; i < rsel.length; ++i) {
            if (i % 3 == 0) {
                rsel[i] = true;
            }
        }
        for (int i = 0; i < csel.length; ++i) {
            if (i % 2 == 0) {
                csel[i] = true;
            }
        }
        Matrix M2 = M.clone();

        int[] irsel = new int[]{1, 2, 3, 5};
        int[] icsel = new int[]{1, 3, 5, 8};

        Matrix mr = Matrix.selectRows(M.subMatrix(), rsel);
        M2.subMatrix().copyRows(mr.subMatrix(), rsel);
        assertTrue(M2.equals(M));

        Matrix mc = Matrix.selectColumns(M.subMatrix(), csel);
        M2.subMatrix().copyColumns(mc.subMatrix(), csel);
        assertTrue(M2.equals(M));

        Matrix m = Matrix.select(M.subMatrix(), rsel, csel);
        M2.subMatrix().copy(m.subMatrix(), rsel, csel);
        assertTrue(M2.equals(M));

        mr = Matrix.selectRows(M.subMatrix(), irsel);
        M2.subMatrix().copyRows(mr.subMatrix(), irsel);
        assertTrue(M2.equals(M));

        mc = Matrix.selectColumns(M.subMatrix(), icsel);
        M2.subMatrix().copyColumns(mc.subMatrix(), icsel);
        assertTrue(M2.equals(M));

        m = Matrix.select(M.subMatrix(), irsel, icsel);
        M2.subMatrix().copy(m.subMatrix(), irsel, icsel);
        assertTrue(M2.equals(M));
    }

    @Test
    public void testDiagonal() {
        Matrix D = Matrix.square(20);
        D.diagonal().randomize();
        assertTrue(D.isDiagonal());
        assertTrue(D.isDiagonal(.001));

        D.set(18, 19, .001);
        assertTrue(!D.isDiagonal());
        assertTrue(D.isDiagonal(.001));
        assertTrue(!D.isDiagonal(.000999));
    }

}
