/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package demetra.arima.regarima;

import demetra.arima.IArimaModel;
import demetra.arima.StationaryTransformation;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.design.Immutable;
import demetra.linearmodel.LinearModel;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.matrices.Matrix;
import java.util.ArrayList;
import java.util.function.IntToDoubleFunction;
import javax.annotation.Nonnull;

/**
 *
 * @param <M>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class RegArimaModel<M extends IArimaModel> {

    public static class Builder<M extends IArimaModel> implements IBuilder<RegArimaModel<M>> {

        private final double[] y;
        private final M arima;
        private boolean mean;
        private final ArrayList<DoubleSequence> x = new ArrayList<>();
        private int[] missing;

        private Builder(@Nonnull DoubleSequence y, @Nonnull M arima) {
            this.y = y.toArray();
            this.arima = arima;
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

        public Builder missing(int[] missingPos) {
            this.missing = missingPos;
            return this;
        }

        @Override
        public RegArimaModel<M> build() {
            Matrix X = Matrix.make(y.length, x.size());
            if (!X.isEmpty()) {
                DataBlockIterator cols = X.columnsIterator();
                for (DoubleSequence xcur : x) {
                    cols.next().copy(xcur);
                }
            }
            return new RegArimaModel<>(y, arima, mean, X, missing);
        }
    }

    private final double[] y;
    private final M arima;
    private final boolean mean;
    private final Matrix x;
    private int[] missing;

    private final LinearModel dmodel;
    private final M arma;
    private final BackFilter ur;

    public static <M extends IArimaModel> Builder<M> builder(DoubleSequence y, M arima) {
        return new Builder<>(y, arima);
    }

    /**
     *
     */
    private RegArimaModel(double[] y, final M arima, final boolean mean, final Matrix x, final int[] missing) {
        this.y = y;
        this.arima = arima;
        this.mean = mean;
        this.x = x;
        this.missing = missing;
        StationaryTransformation<M> st = (StationaryTransformation<M>) arima.stationaryTransformation();
        arma = st.getStationaryModel();
        ur = st.getUnitRoots();
        int d = ur.length() - 1;
        int ndy = y.length - d;
        if (ndy <= 0) {
            dmodel = null;
            return;
        }
        int nx = x == null ? 0 : x.getColumnsCount();
        int nv = nx;
        if (missing != null) {
            nv += missing.length;
        }
        if (mean) {
            ++nv;
        }
        if (d == 0 && nv == nx) { // nothing to do
            dmodel = new LinearModel(y, false, x);
            return;
        }
        double[] dy;
        if (d > 0) {
            dy = new double[y.length - d];
            ur.apply(DataBlock.ofInternal(y), DataBlock.ofInternal(dy));
        } else {
            dy = y;
        }
        if (nv > 0) {
            Matrix dx = Matrix.make(dy.length, nv);
            DataBlockIterator cols = dx.columnsIterator();
            if (d > 0) {
                if (missing != null) {
                    DoubleSequence coeff = ur.asPolynomial().coefficients().reverse();
                    for (int i = 0; i < missing.length; ++i) {
                        DataBlock col = cols.next();
                        if (missing[i] >= dy.length) {
                            col.range(missing[i] - d, dy.length).copy(coeff.drop(0, y.length - missing[i]));
                        } else if (missing[i] >= d) {
                            col.range(missing[i] - d, missing[i] + 1).copy(coeff);
                        } else {
                            col.range(0, missing[i] + 1).copy(coeff.drop(d - missing[i], 0));
                        }
                    }
                }
                if (mean) {
                    cols.next().set(1);
                }
                if (x != null) {
                    DataBlockIterator xcols = x.columnsIterator();
                    while (xcols.hasNext()) {
                        ur.apply(xcols.next(), cols.next());
                    }
                }
            } else {
                if (missing != null) {
                    for (int i = 0; i < missing.length; ++i) {
                        cols.next().set(missing[i], 1);
                    }
                }
                if (mean) {
                    cols.next().set(1);
                }
                if (x != null) {
                    DataBlockIterator xcols = x.columnsIterator();
                    while (xcols.hasNext()) {
                        cols.next().copy(xcols.next());
                    }
                }
            }
            dmodel = new LinearModel(dy, mean, dx);
        } else {
            dmodel = new LinearModel(dy, mean, null);
        }
    }

    public int getDifferencingOrder() {
        return ur.length() - 1;
    }

    public int getMissingValuesCount() {
        return missing == null ? 0 : missing.length;
    }

    public int getObservationsCount() {
        return y.length;
    }

    public int getVariablesCount() {
        int nv = mean ? 1 : 0;
        if (x != null) {
            nv += x.getColumnsCount();
        }
        return nv;
    }

    public RegArmaModel differencedModel() {
        return new RegArmaModel(dmodel, arma, missing == null ? 0 : missing.length);
    }

    public M arima() {
        return arima;
    }

    public M arma() {
        return arma;
    }

    public boolean isMean() {
        return mean;
    }

    public int[] missing() {
        return missing == null ? NOMISSING  : missing.clone();
    }
    
    private static final  int[] NOMISSING=new int[0];
}
