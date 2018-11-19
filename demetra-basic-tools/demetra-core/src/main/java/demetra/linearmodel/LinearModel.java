/*
 * Copyright 2017 National Bank copyOf Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.linearmodel;

import demetra.data.DataBlock;
import demetra.design.Immutable;
import demetra.maths.matrices.Matrix;
import java.util.ArrayList;
import demetra.data.DataBlockIterator;
import demetra.maths.matrices.MatrixWindow;
import javax.annotation.Nonnull;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.design.Internal;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Immutable
public class LinearModel implements LinearModelType{

    public static class Builder {

        private double[] y;
        private boolean mean;
        private final ArrayList<DoubleSequence> x = new ArrayList<>();

        private Builder() {
        }

        public Builder meanCorrection(boolean mean) {
            this.mean = mean;
            return this;
        }

        public Builder y(@Nonnull DoubleSequence y) {
            this.y = y.toArray();
            return this;
        }

        public Builder addX(@Nonnull DoubleSequence var) {
            x.add(var);
            return this;
        }

        public Builder addX(@Nonnull Matrix X) {
            X.columns().forEach(col -> x.add(col));
            return this;
        }

        public Builder addX(@Nonnull DoubleSequence... vars) {
            for (DoubleSequence var : vars) {
                x.add(var);
            }
            return this;
        }

        public LinearModel build() {
            if (y == null) {
                throw new RuntimeException("Missing y");
            }

            Matrix X = Matrix.make(y.length, x.size());
            if (!X.isEmpty()) {
                DataBlockIterator cols = X.columnsIterator();
                for (DoubleSequence xcur : x) {
                    if (xcur.length() != y.length) {
                        throw new RuntimeException("Incompatible dimensions");
                    }
                    cols.next().copy(xcur);
                }
            }
            return new LinearModel(y, mean, X);
        }
    }

    private final double[] y;
    private final boolean mean;
    private final Matrix x;

    public static Builder builder() {
        return new Builder();
    }
    
    public static LinearModel of(LinearModelType model){
        return new LinearModel(model.getY().toArray(), model.isMeanCorrection(), Matrix.of(model.getX()));
    }

    /**
     *
     * @param y
     * @param mean
     * @param x X doesn't contain the mean !!
     */
    @Internal
    public LinearModel(double[] y, final boolean mean, final Matrix x) {
        this.y = y;
        this.mean = mean;
        this.x = x;
    }

    /**
     * Computes y-Xb.
     *
     * @param b The coefficients of the mean and of the regression variables
     * @return
     */
    public DataBlock calcResiduals(final DoubleSequence b) {
        if (getVariablesCount() != b.length()) {
            throw new RuntimeException("Incompatible dimensions");
        }

        DataBlock res = DataBlock.make(y.length);
        res.copyFrom(y, 0);

        DoubleReader cell = b.reader();
        if (mean) {
            res.add(-cell.next());
        }
        DataBlockIterator columns = x.columnsIterator();
        while (columns.hasNext()) {
            res.addAY(-cell.next(), columns.next());
        }
        return res;
    }

    /**
     *
     * @return
     */
    public int getObservationsCount() {
        return y.length;
    }

    /**
     *
     * @return
     */
    public int getVariablesCount() {
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
    @Override
    public DoubleSequence getY() {
        return DoubleSequence.of(y);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isMeanCorrection() {
        return mean;
    }
    
    public Matrix getX(){
        return x;
    }

    /**
     *
     * @return
     */
    public Matrix variables() {
        if (!mean) {
            return x.deepClone();
        } else {
            Matrix vars = Matrix.make(x.getRowsCount(), x.getColumnsCount() + 1);
            MatrixWindow left = vars.left(1);
            left.set(1);
            left.hnext(x.getColumnsCount());
            left.copy(x);
            return vars;
        }
    }

    /**
     *
     * @param idx
     * @return
     */
    public DoubleSequence X(final int idx) {
        return x.column(idx);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("y~").append(mean ? '1' : '0');
        if (!x.isEmpty()) {
            builder.append("+x(").append(x.getColumnsCount()).append(')');
        }
        return builder.toString();
    }
   
}
