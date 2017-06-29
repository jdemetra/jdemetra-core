/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ls;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.Doubles;
import demetra.leastsquares.IQRSolver;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixException;
import demetra.maths.matrices.UpperTriangularMatrix;
import demetra.maths.matrices.internal.GivensRotation;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@ServiceProvider(service = IQRSolver.class)
public class GRSolver implements IQRSolver {

    private Matrix R;
    private double[] b;
    private double err;

    @Override
    public boolean solve(Doubles y, Matrix x) {
        try {
            Matrix X = x.deepClone();
            int m = x.getColumnsCount(), n = x.getRowsCount();
            DataBlock Y = DataBlock.copyOf(y);
            for (int c = 0; c < m; ++c) {
                Matrix xcur = X.extract(c, n, c, m);
                DataBlock ycur=Y.range(c, n);
                DataBlockIterator cols = xcur.columnsIterator();
                for (int r = 1; r < xcur.getRowsCount(); ++r) {
                    cols.reset();
                    DataBlock cur = cols.next();
                    if (cur.get(r) != 0) {
                        GivensRotation rotation = GivensRotation.of(cur, r);
                        while (cols.hasNext()) {
                            rotation.transform(cols.next());
                        }
                        rotation.transform(ycur);
                    }
                }
            }
            R = X.extract(0, m, 0, m).deepClone();
            b = new double[m];
            Y.range(0, m).copyTo(b, 0);
            UpperTriangularMatrix.rsolve(R, DataBlock.ofInternal(b), 1e-9);
            err = Y.drop(m, 0).ssq();
            return true;
        } catch (MatrixException err) {
            return false;
        }
    }

    @Override
    public Doubles coefficients() {
        return Doubles.ofInternal(b);
    }

    @Override
    public double ssqerr() {
        return err;
    }

    @Override
    public Matrix R() {
        return R;
    }

}
