/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.leastsquares;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.data.accumulator.NeumaierAccumulator;
import demetra.maths.Constants;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixException;
import demetra.maths.matrices.SymmetricMatrix;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@ServiceProvider(service = LeastSquaresSolver.class)
public class CholeskySolver implements LeastSquaresSolver {

    private Matrix L;
    private double[] b;
    private double err;

    @Override
    public boolean solve(DoubleSequence y, Matrix x) {
        try {
            L = SymmetricMatrix.robustXtX(x, new NeumaierAccumulator());
            SymmetricMatrix.lcholesky(L, Constants.getEpsilon());
            // b=(X'X)^-1(X'Y)
            // X'y
            int n = x.getColumnsCount();
            DataBlock xy = DataBlock.make(n);
            DataBlock Y = DataBlock.of(y);
            xy.robustProduct(x.columnsIterator(), Y, new NeumaierAccumulator());
            LowerTriangularMatrix.rsolve(L, xy, Constants.getEpsilon());
            LowerTriangularMatrix.lsolve(L, xy, Constants.getEpsilon());
            b = xy.getStorage();
//            DataBlock e=DataBlock.copyOf(Y);
//            DataBlockIterator rows = x.rowsIterator();
//            for (int i=0; i<b.length; ++i)
//                e.addAY(-b[i], rows.next());
//            err=e.ssq();
            return true;
        } catch (MatrixException err) {
            return false;
        }
    }

    @Override
    public DoubleSequence coefficients() {
        return DoubleSequence.ofInternal(b);
    }

    @Override
    public double ssqerr() {
        return err;
    }

    public Matrix L() {
        return L;
    }

    @Override
    public DoubleSequence residuals() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
