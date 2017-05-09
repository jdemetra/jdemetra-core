/*
 * Copyright 2016 National Bank copyOf Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
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

import demetra.maths.matrices.Matrix;
import demetra.data.DataWindow;
import java.util.Random;
import demetra.maths.matrices.Matrix;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class MatrixTest {

    public MatrixTest() {
    }

    @Test
    public void testBuilder() {
        double[] z = new double[200];
        Matrix M = Matrix.builder(z)
                .nrows(10)
                .ncolumns(15)
                .build();
        M.set((i, j) -> i + 10 * j);
//        System.out.println(M);
    }

    int N = 20, M = 50;
    int K = 10000000, K2 = 1000000;

    //@Test
    public void stressColumnsTest() {
        System.out.println("Columns");
        Matrix m = Matrix.make(N, M);
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            m.applyByColumns(x -> x.set(10));
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Inline DataBlock");
        System.out.println(t1 - t0);

        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            m.columns().forEach(col -> col.set(10));
        }
        t1 = System.currentTimeMillis();
        System.out.println("Iterator of DataBlock");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            m.fastColumns().forEach(col -> col.set(10));
        }
        t1 = System.currentTimeMillis();
        System.out.println("Fast DataBlock");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        ec.tstoolkit.maths.matrices.Matrix O = new ec.tstoolkit.maths.matrices.Matrix(N, M);
        for (int k = 0; k < K; ++k) {
            ec.tstoolkit.data.DataBlockIterator columns = O.columns();
            ec.tstoolkit.data.DataBlock data = columns.getData();
            do {
                data.set(10);
            } while (columns.next());
        }
        System.out.println("Old DataBlock");
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    //@Test
    public void stressRowsTest() {
        System.out.println("Rows");
        Matrix m = Matrix.make(N, M);
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            m.applyByRows(x -> x.set(0));
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Inline DataBlock");
        System.out.println(t1 - t0);

        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            m.rows().forEach(col -> col.set(10));
        }
        t1 = System.currentTimeMillis();
        System.out.println("Iterator of DataBlock");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            m.fastRows().forEach(col -> col.set(10));
        }
        t1 = System.currentTimeMillis();
        System.out.println("Fast DataBlock");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        ec.tstoolkit.maths.matrices.Matrix O = new ec.tstoolkit.maths.matrices.Matrix(N, M);
        for (int k = 0; k < K; ++k) {
            ec.tstoolkit.data.DataBlockIterator rows = O.rows();
            ec.tstoolkit.data.DataBlock data = rows.getData();
            do {
                data.set(10);
            } while (rows.next());
        }
        t1 = System.currentTimeMillis();
        System.out.println("Old DataBlock");
        System.out.println(t1 - t0);
    }

    //@Test
    public void testRC() {
        Matrix M = Matrix.make(10, 10);
        M.rows().forEach(row -> row.set(i -> row.getStartPosition() + i));
        System.out.println(M);
        M = Matrix.make(10, 10);
        M.columns().forEach(col -> col.set(i -> col.getStartPosition() - i));
        System.out.println(M);
    }

    @Test
    public void testProduct() {
        Matrix A = Matrix.make(3, 4), B = Matrix.make(4, 2);
        Matrix C = Matrix.make(A.getRowsCount(), B.getColumnsCount());
        A.set((i, j) -> i + j);
        B.set((i, j) -> (i + 1) * (j + 1));

        C.product(A, B);

        ec.tstoolkit.maths.matrices.Matrix OA = MatrixComparator.toLegacy(A);
        ec.tstoolkit.maths.matrices.Matrix OB = MatrixComparator.toLegacy(B);
        assertTrue(MatrixComparator.distance(C, OA.times(OB)) < 1e-9);
    }

    @Test
    public void testProduct2() {
        Matrix A = Matrix.make(3, 4), B = Matrix.make(2, 4);
        Matrix C = Matrix.make(A.getRowsCount(), B.getRowsCount());
        A.set((i, j) -> i + j);
        B.set((j, i) -> (i + 1) * (j + 1));

        C.product(A, B.transpose());

        ec.tstoolkit.maths.matrices.Matrix OA = MatrixComparator.toLegacy(A);
        ec.tstoolkit.maths.matrices.Matrix OB = MatrixComparator.toLegacy(B.transpose());
        assertTrue(MatrixComparator.distance(C, OA.times(OB)) < 1e-9);
    }

    @Test
    public void testProduct3() {
        Matrix A = Matrix.make(3, 4), B = Matrix.make(4, 2);
        Matrix C = Matrix.make(A.getRowsCount(), B.getColumnsCount());
        A.set((i, j) -> i + j);
        B.set((i, j) -> (i + 1) * (j + 1));

        C.transpose().product(B.transpose(), A.transpose());

        ec.tstoolkit.maths.matrices.Matrix OA = MatrixComparator.toLegacy(A);
        ec.tstoolkit.maths.matrices.Matrix OB = MatrixComparator.toLegacy(B);
        assertTrue(MatrixComparator.distance(C, OA.times(OB)) < 1e-9);
    }

    @Test
    @Ignore
    public void stressTestProduct() {
        Matrix A = Matrix.make(200, 5), B = Matrix.make(5, 10);
        Matrix C = Matrix.make(A.getRowsCount(), B.getColumnsCount());
        Random rnd = new Random(0);
        A.set((i, j) -> rnd.nextDouble());
        B.set((i, j) -> rnd.nextDouble());
        ec.tstoolkit.maths.matrices.Matrix OA = new ec.tstoolkit.maths.matrices.Matrix(A.getRowsCount(), A.getColumnsCount());
        ec.tstoolkit.maths.matrices.Matrix OB = new ec.tstoolkit.maths.matrices.Matrix(B.getRowsCount(), B.getColumnsCount());
        ec.tstoolkit.maths.matrices.Matrix OC = new ec.tstoolkit.maths.matrices.Matrix(OA.getRowsCount(), OB.getColumnsCount());
        Random ornd = new Random(0);
        OA.set((i, j) -> ornd.nextDouble());
        OB.set((i, j) -> ornd.nextDouble());

        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K2; ++k) {
            C.product(A, B);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K2; ++k) {
            OC.all().product(OA.all(), OB.all());
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
