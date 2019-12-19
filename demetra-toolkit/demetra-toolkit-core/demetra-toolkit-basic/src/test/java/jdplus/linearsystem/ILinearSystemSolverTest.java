/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.linearsystem;

import java.util.Random;
import jdplus.data.DataBlock;
import jdplus.math.matrices.decomposition.CroutDoolittle;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.decomposition.Householder2;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ILinearSystemSolverTest {

    public ILinearSystemSolverTest() {
    }

    public static void testMethods() {

        QRLinearSystemSolver qr = QRLinearSystemSolver.builder().decomposer(A->new Householder2().decompose(A)).normalize(false).build();
        QRLinearSystemSolver pqr = QRLinearSystemSolver.builder().normalize(false).build();
        LUSolver igauss = LUSolver.builder().build();
        LUSolver icrout = LUSolver.builder().decomposer((A, eps)->CroutDoolittle.decompose(A, eps))
                .normalize(false).build();
        LUSolver2 sparse = new LUSolver2();
        for (int N = 1; N <= 50; ++N) {
            Matrix M = Matrix.square(N);
            Random rnd = new Random();
            DataBlock x = DataBlock.make(N);
            double[] del = new double[5];
            for (int K = 0; K < 50000; ++K) {
                M.set((i,j) -> (j+1)*(i+1)*rnd.nextDouble());
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
            q.div(50000);
            System.out.println(q);
        }
    }

    public static void main(String[] args) {
        testMethods();
    }

}
