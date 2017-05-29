/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.leastsquares;

import demetra.data.DataBlock;
import demetra.data.Doubles;
import demetra.data.NeumaierAccumulator;
import demetra.maths.Constants;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixException;
import demetra.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class CholeskySolver implements LeastSquaresSolver {

    private Matrix L;
    private double[] b;
    private double err;

    @Override
    public boolean compute(Doubles y, Matrix x) {
        try {
            L = SymmetricMatrix.robustXtX(x, new NeumaierAccumulator());
            SymmetricMatrix.lcholesky(L, Constants.getEpsilon());
            // b=(X'X)^-1(X'Y)
            // X'y
            int n = x.getColumnsCount();
            DataBlock xy = DataBlock.make(n);
            DataBlock Y = DataBlock.copyOf(y);
            xy.robustProduct(x.columnsIterator(), Y, new NeumaierAccumulator());
            LowerTriangularMatrix.rsolve(L, xy, Constants.getEpsilon());
            LowerTriangularMatrix.lsolve(L, xy, Constants.getEpsilon());
            b=xy.getStorage();
            return true;
        } catch (MatrixException err) {
            return false;
        }
    }

    @Override
    public Matrix covariance() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body ofFunction generated methods, choose Tools | Templates.
    }

    @Override
    public Doubles coefficients() {
        return Doubles.ofInternal(b);
    }

    @Override
    public Doubles residuals() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body ofFunction generated methods, choose Tools | Templates.
    }

    @Override
    public double ssqerr() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body ofFunction generated methods, choose Tools | Templates.
    }

}
