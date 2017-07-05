/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.linearsystem;

import demetra.linearsystem.internal.SparseSystemSolver;
import demetra.linearsystem.internal.QRLinearSystemSolver;
import demetra.linearsystem.internal.LUSolver;
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
public class ILinearSystemSolverTest {

    public ILinearSystemSolverTest() {
    }

    @Test
    @Ignore
    public void testMethods() {

        QRLinearSystemSolver qr = QRLinearSystemSolver.builder(new Householder())
                .normalize(true).improve(true).build();
        QRLinearSystemSolver pqr = QRLinearSystemSolver.builder(new HouseholderWithPivoting())
                .normalize(true).improve(true).build();
        QRLinearSystemSolver rqr = QRLinearSystemSolver.builder(new RobustHouseholder())
                .normalize(true).improve(true).build();
        LUSolver igauss = LUSolver.builder(new Gauss())
                .normalize(true).improve(true).build();
        LUSolver icrout = LUSolver.builder(new CroutDoolittle())
                .normalize(true).improve(true).build();
        SparseSystemSolver sparse = new SparseSystemSolver();
        for (int N = 1; N <= 50; ++N) {
            Matrix M = Matrix.square(N);
            Random rnd = new Random();
            DataBlock x = DataBlock.make(N);
            double[] del = new double[6];
            for (int K = 0; K < 10000; ++K) {
                M.set(() -> rnd.nextDouble());
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
                igauss.solve(M, tmp);
                del[3] += x.distance(tmp);
                tmp = DataBlock.copyOf(y);
                icrout.solve(M, tmp);
                del[4] += x.distance(tmp);
                tmp = DataBlock.copyOf(y);
                sparse.solve(M, tmp);
                del[5] += x.distance(tmp);
            }
            DataBlock q = DataBlock.copyOf(del);
            q.div(10000);
            System.out.println(q);
        }
    }

}
