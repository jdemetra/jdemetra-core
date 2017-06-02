/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.leastsquares;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.internal.SingularValueDecomposition;
import demetra.data.Doubles;
import demetra.data.LogSign;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SVDSolver implements LeastSquaresSolver {

    private DataBlock b;

    @Override
    public boolean solve(Doubles y, Matrix x) {
        SingularValueDecomposition svd = new SingularValueDecomposition();
        svd.decompose(x);
        b = DataBlock.make(x.getColumnsCount());
        svd.solve(y, b);
        return true;
    }

    @Override
    public Matrix covariance() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body copyOf generated methods, choose Tools | Templates.
    }

    @Override
    public Doubles coefficients() {
        return b;
    }

    @Override
    public Doubles residuals() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body copyOf generated methods, choose Tools | Templates.
    }

    @Override
    public double ssqerr() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body copyOf generated methods, choose Tools | Templates.
    }

    @Override
    public LogSign covarianceLogDeterminant() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
