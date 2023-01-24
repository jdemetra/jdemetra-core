/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.multivariate;

import jdplus.data.DataBlock;
import demetra.data.DoubleSeq;
import jdplus.math.matrices.FastMatrix;


/**
 *
 * @author Jean Palate
 */
public class SsfMatrix implements IMultivariateSsfData {

    private final FastMatrix x;
    private final boolean[] constraints;

    public SsfMatrix(FastMatrix x) {
        this.x = x;
        constraints=null;
    }

     public SsfMatrix(FastMatrix x, boolean[] constraints) {
        this.x = x;
        this.constraints=constraints;
    }

   @Override
    public double get(int pos, int v) {
        return pos < x.getRowsCount() ? x.get(pos, v) : Double.NaN;
    }

    @Override
    public boolean isMissing(int pos, int v) {
        if (pos >= x.getRowsCount()) {
            return true;
        }
        double y = x.get(pos, v);
        return !Double.isFinite(y);
    }

    @Override
    public boolean isConstraint(int pos, int v) {
        if (constraints == null)
            return false;
        else
            return constraints[v];
    }

    @Override
    public int getObsCount() {
        return x.getRowsCount();
    }

    @Override
    public int getVarsCount() {
        return x.getColumnsCount();
    }


    @Override
    public DoubleSeq get(int pos) {
        return pos < x.getRowsCount() ? x.row(pos) : DataBlock.EMPTY;
    }
}
