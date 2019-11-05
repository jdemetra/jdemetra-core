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
package jdplus.maths.matrices;

import java.util.Random;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Jean Palate
 */
public class FastMatrixTest {

    public FastMatrixTest() {
    }

    @Test
    public void testBuilder() {
        double[] z = new double[200];
        SubMatrix A = SubMatrix.builder(z)
                .nrows(10)
                .ncolumns(15)
                .build();
        A.set((i, j) -> i + 10 * j);
//        System.out.println(A);
    }

    @Test
    public void testulshift() {
        int m=10, n=15;
        double[] z = new double[m*n];
        SubMatrix A = SubMatrix.builder(z)
                .nrows(m)
                .ncolumns(n)
                .build();
        A.set((i, j) -> i + 10 * j);
        Matrix B=A.deepClone();
        int del=3;
        A.upLeftShift(del);
        assertTrue(A.extract(0, m-del, 0, n-del).minus(B.extract(del, m, del, n)).isZero(1e15));
    }

    @Test
    public void testdrshift() {
        int m=8, n=6;
        double[] z = new double[m*n];
        SubMatrix A = SubMatrix.builder(z)
                .nrows(m)
                .ncolumns(n)
                .build();
        A.set((i, j) -> i + 10 * j);
        Matrix B=A.deepClone();
        int del=2;
        A.downRightShift(del);
        assertTrue(A.extract(del, m-del, del, n-del).minus(B.extract(0, m-del, 0, n-del)).isZero(1e15));
    }

   static  int N = 20, M = 50;
    static int K = 10000000, K2 = 1000000;

    public static void stressColumnsTest() {
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

     public static void stressRowsTest() {
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

    @Test
    public void testRC() {
        Matrix A = Matrix.make(10, 10);
        A.rows().forEach(row -> row.set(i -> row.getStartPosition() + i));
        System.out.println(A);
        A = Matrix.make(10, 10);
        A.columns().forEach(col -> col.set(i -> col.getStartPosition() - i));
        System.out.println(A);
    }

    @Test
    public void testProduct() {
        Matrix A = Matrix.make(3, 4);
        Matrix B = Matrix.make(4, 2);
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
        Matrix A = Matrix.make(3, 4);
        Matrix B = Matrix.make(2, 4);
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
        Matrix A = Matrix.make(3, 4);
        Matrix B = Matrix.make(4, 2);
        Matrix C = Matrix.make(A.getRowsCount(), B.getColumnsCount());
        A.set((i, j) -> i + j);
        B.set((i, j) -> (i + 1) * (j + 1));

        C.transpose().product(B.transpose(), A.transpose());

        ec.tstoolkit.maths.matrices.Matrix OA = MatrixComparator.toLegacy(A);
        ec.tstoolkit.maths.matrices.Matrix OB = MatrixComparator.toLegacy(B);
        assertTrue(MatrixComparator.distance(C, OA.times(OB)) < 1e-9);
    }

    public static void stressProductTest() {
        System.out.println("Product");
        Matrix A = Matrix.make(100, 10);
        Matrix B = Matrix.make(10, 20);
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
        System.out.println("New");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K2; ++k) {
            OC.all().product(OA.all(), OB.all());
        }
        t1 = System.currentTimeMillis();
        System.out.println("Old");
        System.out.println(t1 - t0);
    }
    
    
    @Test
    public void testDeterminant(){
        double[] x=new double[]{1, 5, 4, -3};
        SubMatrix X=SubMatrix.builder(x)
                .nrows(2)
                .ncolumns(2)
                .build();
        double d=FastMatrix.determinant(X);
        assertEquals(x[0]*x[3]-x[1]*x[2], d, 1e-9);
    }

    @Test
    public void testSingular(){
        double[] x=new double[]{1, 5, 4, 20};
        SubMatrix X=SubMatrix.builder(x)
                .nrows(2)
                .ncolumns(2)
                .build();
        double d=FastMatrix.determinant(X);
        assertTrue(d==0);
    }
    
    public static void main(String[] arg){
        stressRowsTest();
        stressColumnsTest();
        stressProductTest();
    }
}
