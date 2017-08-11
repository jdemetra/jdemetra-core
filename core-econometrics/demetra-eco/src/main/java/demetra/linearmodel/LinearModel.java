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
import demetra.data.DataWindow;
import demetra.data.DoubleSequence;
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
public class LinearModel {

    public static class Builder {

        private final double[] y;
        private boolean mean;
        private final ArrayList<DoubleSequence> x = new ArrayList<>();

        private Builder(@Nonnull DoubleSequence y) {
            this.y = y.toArray();
        }

        public Builder meanCorrection(boolean mean) {
            this.mean = mean;
            return this;
        }

        public Builder addX(@Nonnull DoubleSequence var) {
            if (var.length() != y.length) {
                throw new RuntimeException("Incompatible dimensions");
            }
            x.add(var);
            return this;
        }

        public Builder addX(@Nonnull DoubleSequence... vars) {
            for (DoubleSequence var : vars) {
                if (var.length() != y.length) {
                    throw new RuntimeException("Incompatible dimensions");
                }
                x.add(var);
            }
            return this;
        }

        public LinearModel build() {
            int nx = x.size();
            if (mean) {
                ++nx;
            }
            Matrix X = Matrix.make(y.length, nx);
            if (!X.isEmpty()) {
                DataBlockIterator cols = X.columnsIterator();
                if (mean) {
                    cols.next().set(1);
                }
                for (DoubleSequence xcur : x) {
                    cols.next().copy(xcur);
                }
            }
            return new LinearModel(y, mean, X);
        }
    }

    private final double[] y;
    private final boolean mean;
    private final Matrix x;

    public static Builder of(DoubleSequence y) {
        return new Builder(y);
    }

    /**
     *
     * @param y
     * @param mean
     * @param x X already contains the mean !!
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
        DataBlockIterator columns = x.columnsIterator();
        if (mean){
            columns.next();
            res.sub(cell.next());
        }
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
        return x.getColumnsCount();
    }

    /**
     *
     * @return
     */
    public DoubleSequence getY() {
        return DoubleSequence.of(y);
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
    public DoubleSequence X(final int idx) {
        return x.column(idx);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("y~").append(mean ? '1' : '0');
        int nx = x.getColumnsCount();
        if (mean)
            --nx;
        if (nx>0) {
            builder.append("+x(").append(nx).append(')');
        }
        return builder.toString();
    }
}
