/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.linearsystem;

import jdplus.linearsystem.internal.SparseSystemSolver;
import jdplus.linearsystem.internal.QRLinearSystemSolver;
import jdplus.linearsystem.internal.LUSolver;
import java.util.Random;
import jdplus.data.DataBlock;
import jdplus.maths.matrices.Matrix;
import jdplus.maths.matrices.decomposition.CroutDoolittle;
import jdplus.maths.matrices.decomposition.Gauss;
import jdplus.maths.matrices.decomposition.Householder;
import jdplus.maths.matrices.decomposition.HouseholderWithPivoting;
import org.junit.Ignore;
import org.junit.Test;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ILinearSystemSolverTest {

    public ILinearSystemSolverTest() {
    }

    public static void testMethods() {

        QRLinearSystemSolver qr = QRLinearSystemSolver.builder(new Householder.Processor())
                .normalize(true).build();
        QRLinearSystemSolver pqr = QRLinearSystemSolver.builder(new HouseholderWithPivoting.Processor())
                .normalize(true).build();
        LUSolver igauss = LUSolver.builder(new Gauss())
                .normalize(true).build();
        LUSolver icrout = LUSolver.builder(new CroutDoolittle())
                .normalize(true).build();
        SparseSystemSolver sparse = new SparseSystemSolver();
        for (int N = 1; N <= 50; ++N) {
            FastMatrix M = Matrix.square(N);
            Random rnd = new Random();
            DataBlock x = DataBlock.make(N);
            double[] del = new double[5];
            for (int K = 0; K < 10000; ++K) {
                M.set(() -> rnd.nextDouble());
                x.set(() -> rnd.nextDouble());
                DataBlock y = DataBlock.make(N);
                y.product(M.rowsIterator(), x);

                DataBlock tmp = DataBlock.of(y);
                qr.solve(M, tmp);
                del[0] += x.distance(tmp);
                tmp = DataBlock.of(y);
                pqr.solve(M, tmp);
                del[1] += x.distance(tmp);
                tmp = DataBlock.of(y);
                igauss.solve(M, tmp);
                del[2] += x.distance(tmp);
                tmp = DataBlock.of(y);
                icrout.solve(M, tmp);
                del[3] += x.distance(tmp);
                tmp = DataBlock.of(y);
                sparse.solve(M, tmp);
                del[4] += x.distance(tmp);
            }
            DataBlock q = DataBlock.copyOf(del);
            q.div(10000);
            System.out.println(q);
        }
    }

    public static void main(String[] args) {
        testMethods();
    }

}
