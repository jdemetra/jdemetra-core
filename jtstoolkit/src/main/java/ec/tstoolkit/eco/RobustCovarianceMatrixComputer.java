/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.eco;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.WindowType;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class RobustCovarianceMatrixComputer {

    private WindowType winType = WindowType.Bartlett;
    private int truncationLag = 12;
    private Matrix xe, s, xx, v;

    /**
     * Computes a robust covariance estimate of x'e, following the Newey-West
     * method
     *
     * @param x Regression matrix
     * @param e Residuals
     * @return
     */
    public void compute(SubMatrix x, IReadDataBlock e) {
        s = null;
        xx = null;
        v = null;
        // compute x*f
        xe = xe(x, e);
        int n = x.getRowsCount(), nx = x.getColumnsCount();
        double[] w = winType.window(truncationLag + 1);
        xx = Matrix.square(nx);
        SymmetricMatrix.XtX(x, xx.all());
        Matrix xex = SymmetricMatrix.XtX(xe);
        s = xex.clone();
        s.mul(w[0]);
        SubMatrix ol = Matrix.square(nx).all();
        for (int l = 1; l <= truncationLag; ++l) {
            SubMatrix m = xe.subMatrix(0, n - l, 0, nx);
            SubMatrix ml = xe.subMatrix(l, n, 0, nx);
            ol.product(m.transpose(), ml);
            s.all().addAY(w[l], ol);
            s.all().addAY(w[l], ol.transpose());
        }
        s.mul(1.0 / n);
    }

    // compute x*f
    private Matrix xe(SubMatrix x, IReadDataBlock e) {
        Matrix xe = new Matrix(x);
        DataBlockIterator columns = xe.columns();
        DataBlock column = columns.getData();
        do {
            column.apply((a, b) -> a * b, e);
        } while (columns.next());
        return xe;
    }

    public Matrix getXe() {
        return xe;
    }

    public Matrix getOmega() {
        return s;
    }

    public Matrix getRobustCovariance() {

        Matrix Lo = s.clone();
        SymmetricMatrix.lcholesky(Lo);
        Matrix Lx = xx.clone();
        SymmetricMatrix.lcholesky(Lx);
        LowerTriangularMatrix.rsolve(Lx, Lo.all());
        LowerTriangularMatrix.lsolve(Lx, Lo.all().transpose());

        Matrix XXt = SymmetricMatrix.XXt(Lo);
        XXt.mul(xe.getRowsCount());
        return XXt;
    }

    /**
     * @return the winType
     */
    public WindowType getWindowType() {
        return winType;
    }

    /**
     * @param winType the winType to set
     */
    public void setWindowType(WindowType winType) {
        this.winType = winType;
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
