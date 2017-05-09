/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.linearmodel;

import demetra.data.CellReader;
import demetra.data.DataBlock;
import demetra.data.DataWindow;
import demetra.design.Immutable;
import demetra.maths.matrices.Matrix;
import java.util.ArrayList;
import java.util.Iterator;
import demetra.data.DataBlockIterator;
import demetra.data.Doubles;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Immutable
public class LinearModel {

    public static class Builder {

        private final Doubles y;
        private boolean mean;
        private final ArrayList<Doubles> x = new ArrayList<>();

        private Builder(Doubles y) {
            this.y = y;
        }

        public Builder meanCorrection(boolean mean) {
            this.mean = mean;
            return this;
        }
        
        public Builder addX(Doubles var){
            x.add(var);
            return this;
        }

        public LinearModel build() {
            int n=y.length();
            double[] Y = new double[n];
            y.copyTo(Y, 0);
            Matrix X = Matrix.make(Y.length, x.size());
            if (!X.isEmpty()) {
                DataBlockIterator cols = X.columnsIterator();
                for (Doubles xcur : x) {
                    cols.next().copy(xcur);
                }
            }
            return new LinearModel(Y, mean, X);
        }
    }

    private final double[] y;
    private final boolean mean;
    private final Matrix x;

    /**
     *
     */
    private LinearModel(double[] y, final boolean mean, final Matrix x) {
        this.y = y;
        this.mean = mean;
        this.x = x;
    }

    /**
     *
     * @param b
     * @return
     */
    public DataBlock calcRes(final Doubles b) {
        
        DataBlock res=DataBlock.make(y.length);
        res.copyFrom(y, 0);

        CellReader cell = b.reader();
        if (mean)
            res.add(cell.next());
        DataBlockIterator columns = x.columnsIterator();
        while (columns.hasNext()){
            res.addAY(cell.next(), columns.next());
        }
        return res;
    }


    /**
     *
     * @return
     */
    public int getObsCount() {
        return y.length;
    }

    /**
     *
     * @return
     */
    public int getVarsCount() {
        int n = x.getColumnsCount();
        if (mean) {
            ++n;
        }
        return n;
    }

    /**
     *
     * @return
     */
    public int getXCount() {
        return x.getColumnsCount();
    }

    /**
     *
     * @return
     */
    public Doubles getY() {
        return Doubles.of(y);
    }

    /**
     *
     * @return
     */
    public boolean isMeanCorrection() {
        return mean;
    }


    /**
     *
     * @return
     */
    public Matrix variables() {
        return x.deepClone();
    }

    /**
     *
     * @param idx
     * @return
     */
    public Doubles X(final int idx) {
        return x.column(idx);
    }
}
