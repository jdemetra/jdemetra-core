/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.math.matrices;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class MatrixTypeTest {

    public MatrixTypeTest() {
    }

    @Test
    public void testDiagonal() {
        int nr = 4, nc = 5;
        double[] data = new double[20];
        Matrix.Mutable M = Matrix.Mutable.ofInternal(data, nr, nc);
        for (int i = -3, j = 1; i < 5; ++i, ++j) {
            M.subDiagonal(i).set(j);
        }
//        System.out.println(M);

        for (int i = -3, j = 1; i < 5; ++i, ++j) {
            final int q = j;
            M.subDiagonal(i).apply(z -> z - q);
        }

        for (int i = 0; i < data.length; ++i) {
            assertTrue(data[i] == 0);
        }
    }

    @Test
    public void testRow() {
        int nr = 4, nc = 5;
        double[] data = new double[20];
        Matrix.Mutable M = Matrix.Mutable.ofInternal(data, nr, nc);
        for (int i = 0, j = 1; i < M.getRowsCount(); ++i, ++j) {
            M.row(i).set(j);
        }
//        System.out.println(Matrix.format(M, null));

        for (int i = 0, j = 1; i < M.getRowsCount(); ++i, ++j) {
            final int q = j;
            M.row(i).apply(z -> z - q);
        }

        for (int i = 0; i < data.length; ++i) {
            assertTrue(data[i] == 0);
        }
    }

    @Test
    public void testColumn() {
        int nr = 4, nc = 5;
        double[] data = new double[20];
        Matrix.Mutable M = Matrix.Mutable.ofInternal(data, nr, nc);

        for (int i = 0, j = 1; i < M.getColumnsCount(); ++i, ++j) {
            M.column(i).set(j);
        }
//        System.out.println(M);

        for (int i = 0, j = 1; i < M.getColumnsCount(); ++i, ++j) {
            final int q = j;
            M.column(i).apply(z -> z - q);
        }

        for (int i = 0; i < data.length; ++i) {
            assertTrue(data[i] == 0);
        }
    }
    
        @Test
    public void testSubDiagonal() {
        int NR = 14, NC = 15;
        double[] data = new double[NR*NC];
        Matrix.Mutable MM = Matrix.Mutable.ofInternal(data, NR, NC);
        
        Matrix.Mutable M = MM.extract(6, 4, 3, 5);
        
        for (int i = -3, j = 1; i < 5; ++i, ++j) {
            M.subDiagonal(i).set(j);
        }
//        System.out.println(MM);

        for (int i = -3, j = 1; i < 5; ++i, ++j) {
            final int q = j;
            M.subDiagonal(i).apply(z -> z - q);
        }

        for (int i = 0; i < data.length; ++i) {
            assertTrue(data[i] == 0);
        }
    }

    @Test
    public void testubRow() {
        int NR = 14, NC = 15;
        double[] data = new double[NR*NC];
        Matrix.Mutable MM = Matrix.Mutable.ofInternal(data, NR, NC);
        
        Matrix.Mutable M = MM.extract(3, 4, 6, 5);
        for (int i = 0, j = 1; i < M.getRowsCount(); ++i, ++j) {
            M.row(i).set(j);
        }
//        System.out.println(MM);

        for (int i = 0, j = 1; i < M.getRowsCount(); ++i, ++j) {
            final int q = j;
            M.row(i).apply(z -> z - q);
        }

        for (int i = 0; i < data.length; ++i) {
            assertTrue(data[i] == 0);
        }
    }

    @Test
    public void testSubColumn() {
        int NR = 14, NC = 15;
        double[] data = new double[NR*NC];
        Matrix.Mutable MM = Matrix.Mutable.ofInternal(data, NR, NC);
        
        Matrix.Mutable M = MM.extract(3, 4, 6, 5);

        for (int i = 0, j = 1; i < M.getColumnsCount(); ++i, ++j) {
            M.column(i).set(j);
        }
//        System.out.println(MM);

        for (int i = 0, j = 1; i < M.getColumnsCount(); ++i, ++j) {
            final int q = j;
            M.column(i).apply(z -> z - q);
        }

        for (int i = 0; i < data.length; ++i) {
            assertTrue(data[i] == 0);
        }
    }

}
