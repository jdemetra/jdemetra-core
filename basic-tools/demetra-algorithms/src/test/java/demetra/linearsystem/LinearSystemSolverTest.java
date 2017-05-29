/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.linearsystem;

import java.util.Random;
import demetra.data.DataBlock;
import demetra.data.NeumaierAccumulator;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.internal.CroutDoolittle;
import demetra.maths.matrices.internal.Gauss;
import demetra.maths.matrices.internal.Householder;
import demetra.maths.matrices.internal.HouseholderWithPivoting;
import demetra.maths.matrices.internal.RobustHouseholder;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class LinearSystemSolverTest {

    public LinearSystemSolverTest() {
    }

    @Test
    @Ignore
    public void testMethods() {
        double[] del = new double[8];

        QRSolver qr = QRSolver.builder(new Householder())
                .normalize(true).improve(true).build();
        QRSolver pqr = QRSolver.builder(new HouseholderWithPivoting())
                .normalize(true).improve(true).build();
        QRSolver rqr = QRSolver.builder(new RobustHouseholder())
                .normalize(true).improve(true).build();
        LUSolver gauss = LUSolver.builder(new Gauss())
                .normalize(true).build();
        LUSolver crout = LUSolver.builder(new CroutDoolittle())
                .normalize(true).build();
        LUSolver igauss = LUSolver.builder(new Gauss())
                .normalize(true).improve(true).build();
        LUSolver icrout = LUSolver.builder(new CroutDoolittle())
                .normalize(true).improve(true).build();
        SparseSystemSolver sparse = new SparseSystemSolver();
        for (int K = 0; K < 1000; ++K) {
            for (int N = 1; N <= 100; ++N) {
                Matrix M = Matrix.square(N);
                Random rnd = new Random();
                M.set(() -> rnd.nextDouble());
                DataBlock x = DataBlock.make(N);
                x.set(() -> rnd.nextDouble());
                DataBlock y = DataBlock.make(N);
                y.robustProduct(M.rowsIterator(), x, new NeumaierAccumulator());

                DataBlock tmp = DataBlock.copyOf(y);
                qr.solve(M, tmp);
                del[0] += x.distance(tmp);
                tmp = DataBlock.copyOf(y);
                pqr.solve(M, tmp);
                del[1] += x.distance(tmp);
                tmp = DataBlock.copyOf(y);
                rqr.solve(M, tmp);
                del[2] += x.distance(tmp);
                tmp = DataBlock.copyOf(y);
                gauss.solve(M, tmp);
                del[3] += x.distance(tmp);
                tmp = DataBlock.copyOf(y);
                crout.solve(M, tmp);
                del[4] += x.distance(tmp);
                tmp = DataBlock.copyOf(y);
                igauss.solve(M, tmp);
                del[5] += x.distance(tmp);
                tmp = DataBlock.copyOf(y);
                icrout.solve(M, tmp);
                del[6] += x.distance(tmp);
                tmp = DataBlock.copyOf(y);
                sparse.solve(M, tmp);
                del[7] += x.distance(tmp);
            }
            DataBlock q = DataBlock.copyOf(del);
            q.div(100 * (K + 1));
            System.out.println(q);
        }
    }

}
