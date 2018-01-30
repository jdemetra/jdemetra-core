/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats;

import demetra.data.DoubleSequence;
import demetra.data.WindowFunction;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;


/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class RobustCovarianceMatrixComputer {

    private final WindowFunction winFunction;
    private int truncationLag;
    private Matrix xe, s, xx, v;
    
    public RobustCovarianceMatrixComputer(WindowFunction win, final int truncationLag){
        this.winFunction=win;
        this.truncationLag=truncationLag;
    }

    /**
     * Computes a robust covariance estimate of x'e, following the Newey-West
     * method
     *
     * @param x Regression matrix
     * @param e Residuals
     * @return
     */
    public void compute(Matrix x, DoubleSequence e) {
        s = null;
        xx = null;
        v = null;
        // compute x*f
        xe = xe(x, e);
        int n = x.getRowsCount(), nx = x.getColumnsCount();
        double[] w = winFunction.discreteWindow(truncationLag + 1);
        xx = Matrix.square(nx);
        SymmetricMatrix.XtX(x, xx);
        Matrix xex = SymmetricMatrix.XtX(xe);
        s = xex.deepClone();
        s.mul(w[0]);
        Matrix ol = Matrix.square(nx);
        for (int l = 1; l <= truncationLag; ++l) {
            Matrix m = xe.extract(0, n - l, 0, nx);
            Matrix ml = xe.extract(l, n-1, 0, nx);
            ol.product(m.transpose(), ml);
            s.addAY(w[l], ol);
            s.addAY(w[l], ol.transpose());
        }
        s.mul(1.0 / n);
    }

    // compute x*f
    private Matrix xe(Matrix x, DoubleSequence e) {
        Matrix xe = x.deepClone();
        xe.applyByColumns(column->column.apply(e, (a, b) -> a * b));
        return xe;
    }

    public Matrix getXe() {
        return xe;
    }

    public Matrix getOmega() {
        return s;
    }

    public Matrix getRobustCovariance() {

        Matrix Lo = s.deepClone();
        SymmetricMatrix.lcholesky(Lo);
        Matrix Lx = xx.deepClone();
        SymmetricMatrix.lcholesky(Lx);
        LowerTriangularMatrix.rsolve(Lx, Lo);
        LowerTriangularMatrix.lsolve(Lx, Lo.transpose());

        Matrix XXt = SymmetricMatrix.XXt(Lo);
        XXt.mul(xe.getRowsCount());
        return XXt;
    }

    /**
     * @return the winType
     */
    public WindowFunction getWindowFunction() {
        return winFunction;
    }

 
    /**
     * @return the truncationLag
     */
    public int getTruncationLag() {
        return truncationLag;
    }

    /**
     * @param truncationLag the truncationLag to set
     */
    public void setTruncationLag(int truncationLag) {
        this.truncationLag = truncationLag;
    }

}
