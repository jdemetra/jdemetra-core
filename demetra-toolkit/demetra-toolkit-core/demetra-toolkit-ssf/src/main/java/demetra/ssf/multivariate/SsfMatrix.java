/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.multivariate;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.data.DoubleSeq;


/**
 *
 * @author Jean Palate
 */
public class SsfMatrix implements IMultivariateSsfData {

    private final Matrix x_;

    public SsfMatrix(Matrix x) {
        x_ = x;
    }

    @Override
    public double get(int pos, int v) {
        return pos < x_.getRowsCount() ? x_.get(pos, v) : Double.NaN;
    }

    @Override
    public boolean isMissing(int pos, int v) {
        if (pos >= x_.getRowsCount()) {
            return true;
        }
        double y = x_.get(pos, v);
        return !Double.isFinite(y);
    }

    @Override
    public boolean hasData() {
        return true;
    }

    @Override
    public int getObsCount() {
        return x_.getRowsCount();
    }

    @Override
    public int getVarsCount() {
        return x_.getColumnsCount();
    }


    @Override
    public DoubleSeq get(int pos) {
        return pos < x_.getRowsCount() ? x_.row(pos) : DataBlock.EMPTY;
    }
}
