/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.stats.linearmodel;

import jdplus.data.DataBlock;
import nbbrd.design.Immutable;
import java.util.ArrayList;
import jdplus.data.DataBlockIterator;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.data.DoubleSeqCursor;
import nbbrd.design.Internal;
import demetra.data.DoubleSeq;
import jdplus.math.matrices.FastMatrix;
import demetra.math.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Immutable
public final class LinearModel {

    public static class Builder {

        private double[] y;
        private boolean mean;
        private final ArrayList<DoubleSeq> x = new ArrayList<>();

        private Builder() {
        }

        public Builder meanCorrection(boolean mean) {
            this.mean = mean;
            return this;
        }

        public Builder y(@NonNull DoubleSeq y) {
            this.y = y.toArray();
            return this;
        }

        public Builder addX(@NonNull DoubleSeq var) {
            x.add(var);
            return this;
        }

        public Builder addX(@NonNull Matrix X) {
            for (int i = 0; i < X.getColumnsCount(); ++i) {
                x.add(X.column(i));
            }
            return this;
        }

        public Builder addX(@NonNull DoubleSeq... vars) {
            for (DoubleSeq var : vars) {
                x.add(var);
            }
            return this;
        }

        public LinearModel build() {
            if (y == null) {
                throw new RuntimeException("Missing y");
            }

            FastMatrix X = FastMatrix.make(y.length, x.size());
            if (!X.isEmpty()) {
                DataBlockIterator cols = X.columnsIterator();
                for (DoubleSeq xcur : x) {
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
    private final FastMatrix x;

    public static Builder builder() {
        return new Builder();
    }

    /**
     *
     * @param y
     * @param mean Mean correction
     * @param x X. Should not contain the mean if mean is set to true !!
     */
    @Internal
    public LinearModel(double[] y, final boolean mean, final FastMatrix x) {
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
    public DataBlock calcResiduals(final DoubleSeq b) {
        if (getVariablesCount() != b.length()) {
            throw new RuntimeException("Incompatible dimensions");
        }

        DataBlock res = DataBlock.make(y.length);
        res.copyFrom(y, 0);

        DoubleSeqCursor cell = b.cursor();
        if (mean) {
            res.add(-cell.getAndNext());
        }
        DataBlockIterator columns = x.columnsIterator();
        while (columns.hasNext()) {
            res.addAY(-cell.getAndNext(), columns.next());
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
    public DoubleSeq getY() {
        return DoubleSeq.of(y);
    }

    /**
     *
     * @return
     */
    public boolean isMeanCorrection() {
        return mean;
    }

    public FastMatrix getX() {
        return x;
    }

    /**
     *
     * @return
     */
    public FastMatrix variables() {
        if (!mean) {
            return x.deepClone();
        } else {
            int m = x.getRowsCount(), n = x.getColumnsCount();
            FastMatrix vars = FastMatrix.make(m, n + 1);
            vars.column(0).set(1);
            vars.extract(0, m, 1, n).copy(x);
            return vars;
        }
    }

    /**
     *
     * @param idx
     * @return
     */
    public DoubleSeq X(final int idx) {
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
