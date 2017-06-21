/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.leastsquares;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.internal.SingularValueDecomposition;
import demetra.data.Doubles;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SVDSolver implements LeastSquaresSolver {

    private DataBlock b;
    private Matrix U, V;
    private Doubles S;
    private double err;

    @Override
    public boolean solve(Doubles y, Matrix x) {
        SingularValueDecomposition svd = new SingularValueDecomposition();
        svd.decompose(x);
        U = svd.U();
        S = svd.S();
        V = svd.V();
        b = DataBlock.make(x.getColumnsCount());
        svd.solve(y, b);
        DataBlock e = DataBlock.copyOf(y);
        DataBlockIterator rows = x.rowsIterator();
        for (int i = 0; i < b.length(); ++i) {
            e.addAY(-b.get(i), rows.next());
        }
        err = e.ssq();
        return true;
    }

    @Override
    public Doubles coefficients() {
        return b;
    }

    @Override
    public double ssqerr() {
        return err;
    }

    /**
     * @return the U
     */
    public Matrix getU() {
        return U;
    }

    /**
     * @return the V
     */
    public Matrix getV() {
        return V;
    }

    /**
     * @return the S
     */
    public Doubles getS() {
        return S;
    }

}
