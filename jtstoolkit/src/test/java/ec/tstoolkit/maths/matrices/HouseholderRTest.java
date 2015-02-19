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
import ec.tstoolkit.data.DataBlockIterator;
import org.junit.Test;

/**
 *
 * @author pcuser
 */
public class HouseholderRTest {

    public HouseholderRTest() {
    }

//    @Test
    public void demoDecomposition() {

        int n = 200, p = 10, k = 10000;
        Matrix M = new Matrix(n, 2 * p);
        M.randomize();
        //M.subMatrix(0, n, p, 2*p).copy(M.subMatrix(0, n, 0, p));
        Householder h = new Householder(true);
        HouseholderR hr = new HouseholderR(true);
        h.setEpsilon(1e-9);
        hr.setEpsilon(1e-9);

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < k; ++i) {
            h.decompose(M);
        }
        long t1 = System.currentTimeMillis();
        System.out.print(t1 - t0);
        System.out.println();
        t0 = System.currentTimeMillis();
        for (int i = 0; i < k; ++i) {
            hr.decompose(M);
        }
        t1 = System.currentTimeMillis();
        System.out.print(t1 - t0);
        System.out.println();

        System.out.print(h.getRDiagonal());
        System.out.println();
        System.out.print(hr.getRDiagonal());
        System.out.println();
    }

 //   @Test
    public void demoQy() {

        int n = 100, p = 10;
        Matrix M = new Matrix(n, p);
        M.randomize();
        M.column(0).sum(M.column(2), M.column(7));
        M.column(5).sum(M.column(2), M.column(3));
        //M.subMatrix(0, n, p, 2*p).copy(M.subMatrix(0, n, 0, p));
        Matrix Mc = new Matrix(n, p - 2);
        int[] cols = new int[]{0, 1, 2, 3, 4, 6, 8, 9};
        for (int i = 0; i < cols.length; ++i) {
            Mc.column(i).copy(M.column(cols[i]));
        }

        Householder h = new Householder(true);
        Householder hc = new Householder(true);
        HouseholderR hr = new HouseholderR(true);
        h.setEpsilon(1e-12);
        hr.setEpsilon(1e-12);
        h.decompose(M);
        hr.decompose(M);
        hc.decompose(Mc);

        DataBlock y = new DataBlock(n);
        y.randomize(0);
        DataBlock yr = y.deepClone();

        DataBlock b = new DataBlock(h.getRank());
        DataBlock br = new DataBlock(hr.getRank());
        DataBlock bc = new DataBlock(hr.getRank());

        h.leastSquares(y, b, null);
        hr.leastSquares(y, br, null);
        hc.leastSquares(y, bc, null);

        System.out.print(b);
        System.out.println();
        System.out.print(br);
        System.out.println();
        System.out.print(bc);
        System.out.println();

        System.out.print(h.getR());
        System.out.println();
        System.out.print(hr.getR());
        int[] u = h.getUnused();
        System.out.println();
        if (u != null) {
            for (int i = 0; i < u.length; ++i) {
                System.out.print(u[i]);
                System.out.print('\t');
            }
        }
        System.out.println();
        int[] ur = hr.getUnused();
        if (ur != null) {
            for (int i = 0; i < ur.length; ++i) {
                System.out.print(ur[i]);
                System.out.print('\t');
            }
        }
        System.out.println();
        System.out.print(hc.getR());
        System.out.println();
    }

}
