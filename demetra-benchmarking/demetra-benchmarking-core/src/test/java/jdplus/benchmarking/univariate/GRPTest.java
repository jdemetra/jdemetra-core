/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.univariate;

import demetra.benchmarking.univariate.DentonSpec;
import demetra.benchmarking.univariate.GrpSpec;
import demetra.data.AggregationType;
import demetra.data.Data;
import demetra.data.DoubleSeq;
import jdplus.data.DataBlock;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class GRPTest {

    public GRPTest() {
    }

    @Test
    public void testGRP() {
        DataBlock y = DataBlock.of(Data.PCRA);
        DataBlock x = DataBlock.of(Data.IND_PCR);
        GRP grp = new GRP(GrpSpec.DEFAULT, 4, 0);
        double[] rslt = grp.process(x, y);
        Matrix K4 = Matrix.make(4, 3);
        GRP.K(K4);
        double[] mg = GRP.mg(rslt, x.getStorage(), K4);
 //       System.out.println(DoubleSeq.of(rslt));
        assertTrue(DoubleSeq.of(mg).allMatch(w -> Math.abs(w) < 1e-6));
    }

    @Test
    public void testGRP2() {
        DataBlock y = DataBlock.of(Data.PCRA);
        DataBlock x = DataBlock.of(Data.IND_PCR);
        GRP grp = new GRP(GrpSpec.DEFAULT, 4, 0);
        double[] rslt = grp.process(x, y);
        y = y.drop(0, 2);
        rslt = grp.process(x, y);
    }

    @Test
    public void testK() {
        Matrix K4 = Matrix.make(4, 3);
        GRP.K(K4);
        Matrix XtX = SymmetricMatrix.XtX(K4);
        boolean identity = XtX.isDiagonal(1e-9) && XtX.diagonal().allMatch(x -> Math.abs(x - 1) < 1e-9);
        assertTrue(identity);
        Matrix K12 = Matrix.make(12, 11);
        GRP.K(K12);
        XtX = SymmetricMatrix.XtX(K4);
        identity = XtX.isDiagonal(1e-9) && XtX.diagonal().allMatch(x -> Math.abs(x - 1) < 1e-9);
        assertTrue(identity);
    }

    @Test
    public void testGradient() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(80);
        x.set(i -> (1 + i) * (1 + i));
        DentonSpec spec = DentonSpec.builder()
                .modified(true)
                .multiplicative(true)
                .differencing(1)
                .aggregationType(AggregationType.Sum)
                .buildWithoutValidation();
        MatrixDenton denton = new MatrixDenton(spec, 4, 0);
        double[] start = denton.process(x, y);

        double[] g = new double[x.length()];
        for (int i = 0; i < g.length; ++i) {
            g[i] = GRP.g(i, start, x.getStorage());
        }
        Matrix K4 = Matrix.make(4, 3);
        GRP.K(K4);
        double[] mg = GRP.mg(start, x.getStorage(), K4);
        double[] zx = GRP.Ztx(x.getStorage(), K4);

    }

    @Test
    public void testZ() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(80);
        x.set(i -> (1 + i) * (1 + i));
        DentonSpec spec = DentonSpec.builder()
                .modified(true)
                .multiplicative(true)
                .differencing(1)
                .aggregationType(AggregationType.Sum)
                .buildWithoutValidation();
        MatrixDenton denton = new MatrixDenton(spec, 4, 0);
        double[] start = denton.process(x, y);

        Matrix K = Matrix.make(4, 3);
        GRP.K(K);
        double[] z = GRP.Ztx(start, K);

        double[] zz = GRP.Zz(z, K);
        GRP.addXbar(zz, y.getStorage(), 4);
        assertTrue(DoubleSeq.of(zz).distance(DoubleSeq.of(start)) < 1e-9);
    }
}
