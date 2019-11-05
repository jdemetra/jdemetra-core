/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.leastsquares.internal;

import jdplus.leastsquares.internal.DefaultQRSolver;
import jdplus.data.DataBlock;
import demetra.data.DataSets;
import static demetra.data.DataSets.lre;
import jdplus.maths.matrices.Matrix;
import jdplus.maths.matrices.decomposition.Householder;
import jdplus.maths.matrices.decomposition.HouseholderWithPivoting;
import org.junit.Test;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class QRSolverTest {

     static DefaultQRSolver solver = new DefaultQRSolver(new Householder());
    public QRSolverTest() {
    }

    public static void testNorris() {
        double[] y=DataSets.Norris.y;
        Matrix M = Matrix.make(y.length, 2);
        DataBlock x = DataBlock.of(DataSets.Norris.x);
        M.column(0).set(1);
        M.column(1).copy(x);

        solver.solve(DataBlock.of(y), M);
        DoubleSeq beta = solver.coefficients();
        System.out.println("Norris");
        System.out.println(beta);
        for (int i = 0; i < beta.length(); ++i) {
            System.out.print(lre(beta.get(i), DataSets.Norris.expectedBeta[i]));
            System.out.print('\t');
        }
        System.out.println("");
    }

     public static void testPontius() {
        double[] y=DataSets.Pontius.y;
        Matrix M = Matrix.make(y.length, 3);
        DataBlock x = DataBlock.of(DataSets.Pontius.x);
        M.column(0).set(1);
        M.column(1).copy(x);
        M.column(2).set(x, a -> a * a);

        solver.solve(DataBlock.of(y), M);
        DoubleSeq beta = solver.coefficients();
        System.out.println("Pontius");
        System.out.println(beta);
        for (int i = 0; i < beta.length(); ++i) {
            System.out.print(lre(beta.get(i), DataSets.Pontius.expectedBeta[i]));
            System.out.print('\t');
        }
        System.out.println("");
    }

    
    public static void testNoInt1() {
        double[] y=DataSets.NoInt1.y;
        Matrix M = Matrix.make(y.length, 1);
        M.column(0).copyFrom(DataSets.NoInt1.x, 0);

         solver.solve(DataBlock.of(y), M);
        DoubleSeq beta = solver.coefficients();
        System.out.println("NoInt1");
        System.out.println(beta);
        for (int i = 0; i < beta.length(); ++i) {
            System.out.print(lre(beta.get(i), DataSets.NoInt1.expectedBeta[i]));
            System.out.print('\t');
        }
        System.out.println("");
    }

    public static void testNoInt2() {
        double[] y=DataSets.NoInt2.y;
        Matrix M = Matrix.make(y.length, 1);
        DataBlock x = DataBlock.of(DataSets.NoInt2.x);
        M.column(0).copy(x);

         solver.solve(DataBlock.of(y), M);
        DoubleSeq beta = solver.coefficients();
        System.out.println("NoInt2");
        System.out.println(beta);
        for (int i = 0; i < beta.length(); ++i) {
            System.out.print(lre(beta.get(i), DataSets.NoInt2.expectedBeta[i]));
            System.out.print('\t');
        }
        System.out.println("");
    }

     public static void testFilip() {
        double[] y=DataSets.Filip.y;
        Matrix M = Matrix.make(y.length, 11);
        DataBlock x = DataBlock.of(DataSets.Filip.x);
        M.column(0).set(1);
        M.column(1).copy(x);
        M.column(2).set(x, a -> a * a);
        M.column(3).set(x, a -> a * a * a);
        M.column(4).set(x, a -> a * a * a * a);
        M.column(5).set(x, a -> a * a * a * a * a);
        M.column(6).set(x, a -> a * a * a * a * a * a);
        M.column(7).set(x, a -> a * a * a * a * a * a * a);
        M.column(8).set(x, a -> a * a * a * a * a * a * a * a);
        M.column(9).set(x, a -> a * a * a * a * a * a * a * a * a);
        M.column(10).set(x, a -> a * a * a * a * a * a * a * a * a * a);

        solver.solve(DataBlock.of(y), M);
        DoubleSeq beta = solver.coefficients();
        System.out.println("Filip");
        System.out.println(beta);
        for (int i = 0; i < beta.length(); ++i) {
            System.out.print(lre(beta.get(i), DataSets.Filip.expectedBeta[i]));
            System.out.print('\t');
        }
        System.out.println("");
    }

    public static void testLongley() {
        double[] y=DataSets.Longley.y;
        Matrix M = Matrix.make(y.length, 7);
        M.column(0).set(1);
        M.column(1).copyFrom(DataSets.Longley.x1, 0);
        M.column(2).copyFrom(DataSets.Longley.x2, 0);
        M.column(3).copyFrom(DataSets.Longley.x3, 0);
        M.column(4).copyFrom(DataSets.Longley.x4, 0);
        M.column(5).copyFrom(DataSets.Longley.x5, 0);
        M.column(6).copyFrom(DataSets.Longley.x6, 0);

        solver.solve(DataBlock.of(y), M);
        DoubleSeq beta = solver.coefficients();
        System.out.println("Longley");
        System.out.println(beta);
        for (int i = 0; i < beta.length(); ++i) {
            System.out.print(lre(beta.get(i), DataSets.Longley.expectedBeta[i]));
            System.out.print('\t');
        }
        System.out.println("");
    }

    public static void testWampler1() {
        double[] y=DataSets.Wampler1.y;
        Matrix M = Matrix.make(y.length, 6);
        DataBlock x = DataBlock.of(DataSets.Wampler1.x);
        M.column(0).set(1);
        M.column(1).copy(x);
        M.column(2).set(x, a -> a * a);
        M.column(3).set(x, a -> a * a * a);
        M.column(4).set(x, a -> a * a * a * a);
        M.column(5).set(x, a -> a * a * a * a * a);

        solver.solve(DataBlock.of(y), M);
        DoubleSeq beta = solver.coefficients();
        System.out.println("Wampler1");
        System.out.println(beta);
        for (int i = 0; i < beta.length(); ++i) {
            System.out.print(lre(beta.get(i), DataSets.Wampler1.expectedBeta[i]));
            System.out.print('\t');
        }
        System.out.println("");
    }

     public static void testWampler2() {
        double[] y=DataSets.Wampler2.y;
        Matrix M = Matrix.make(y.length, 6);
        DataBlock x = DataBlock.of(DataSets.Wampler2.x);
        M.column(0).set(1);
        M.column(1).copy(x);
        M.column(2).set(x, a -> a * a);
        M.column(3).set(x, a -> a * a * a);
        M.column(4).set(x, a -> a * a * a * a);
        M.column(5).set(x, a -> a * a * a * a * a);

        solver.solve(DataBlock.of(y), M);
        DoubleSeq beta = solver.coefficients();

        System.out.println("Wampler2");
        System.out.println(beta);
        for (int i = 0; i < beta.length(); ++i) {
            System.out.print(lre(beta.get(i), DataSets.Wampler2.expectedBeta[i]));
            System.out.print('\t');
        }
        System.out.println("");
    }

    public static void testWampler3() {
        double[] y=DataSets.Wampler3.y;
        Matrix M = Matrix.make(y.length, 6);
        DataBlock x = DataBlock.of(DataSets.Wampler3.x);
        M.column(0).set(1);
        M.column(1).copy(x);
        M.column(2).set(x, a -> a * a);
        M.column(3).set(x, a -> a * a * a);
        M.column(4).set(x, a -> a * a * a * a);
        M.column(5).set(x, a -> a * a * a * a * a);

        solver.solve(DataBlock.of(y), M);
        DoubleSeq beta = solver.coefficients();
        System.out.println("Wampler3");
        System.out.println(beta);
        for (int i = 0; i < beta.length(); ++i) {
            System.out.print(lre(beta.get(i), DataSets.Wampler3.expectedBeta[i]));
            System.out.print('\t');
        }
        System.out.println("");
    }

    public static void testWampler4() {
        double[] y=DataSets.Wampler4.y;
        Matrix M = Matrix.make(y.length, 6);
        DataBlock x = DataBlock.of(DataSets.Wampler4.x);
        M.column(0).set(1);
        M.column(1).copy(x);
        M.column(2).set(x, a -> a * a);
        M.column(3).set(x, a -> a * a * a);
        M.column(4).set(x, a -> a * a * a * a);
        M.column(5).set(x, a -> a * a * a * a * a);

        solver.solve(DataBlock.of(y), M);
        DoubleSeq beta = solver.coefficients();
        System.out.println("Wampler4");
        System.out.println(beta);
        for (int i = 0; i < beta.length(); ++i) {
            System.out.print(lre(beta.get(i), DataSets.Wampler4.expectedBeta[i]));
            System.out.print('\t');
        }
        System.out.println("");
    }

    public static void testWampler5() {
        double[] y=DataSets.Wampler5.y;
        Matrix M = Matrix.make(y.length, 6);
        DataBlock x = DataBlock.of(DataSets.Wampler5.x);
        M.column(0).set(1);
        M.column(1).copy(x);
        M.column(2).set(x, a -> a * a);
        M.column(3).set(x, a -> a * a * a);
        M.column(4).set(x, a -> a * a * a * a);
        M.column(5).set(x, a -> a * a * a * a * a);

        solver.solve(DataBlock.of(y), M);
        DoubleSeq beta = solver.coefficients();

        System.out.println("Wampler5");
        System.out.println(beta);
        for (int i = 0; i < beta.length(); ++i) {
            System.out.print(lre(beta.get(i), DataSets.Wampler5.expectedBeta[i]));
            System.out.print('\t');
        }
        System.out.println("");
    }
    
    public static void main(String[] arg){
        testNorris();
        testPontius();
        testNoInt1();
        testNoInt2();
        testFilip();
        testLongley();
        testWampler1();
        testWampler2();
        testWampler3();
        testWampler4();
        testWampler5();
    }

}
