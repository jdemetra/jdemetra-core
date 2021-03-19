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
package jdplus.regarima;

import demetra.data.DoubleSeq;
import demetra.math.matrices.MatrixType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jdplus.arima.IArimaModel;
import jdplus.arima.StationaryTransformation;
import jdplus.math.matrices.Matrix;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import nbbrd.design.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Description of a generic regarima model
 *
 * @param <M> Type of the stochastic component
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@Immutable(lazy = true)
public final class RegArimaModel<M extends IArimaModel> {

    @BuilderPattern(RegArimaModel.class)
    public static class Builder<M extends IArimaModel> {

        private DoubleSeq y;
        private M arima;
        private boolean mean;
        private final ArrayList<DoubleSeq> x = new ArrayList<>();
        private int[] missing;

        private Builder() {
        }

        public Builder y(DoubleSeq y) {
            this.y = y;
            return this;
        }

        public Builder arima(M arima) {
            this.arima = arima;
            return this;
        }

        public Builder meanCorrection(boolean mean) {
            this.mean = mean;
            return this;
        }

        public Builder addX(Matrix X) {
            if (X != null) {
                if (y.length() != X.getRowsCount()) {
                    throw new RuntimeException("Incompatible dimensions");
                }
                for (int i = 0; i < X.getColumnsCount(); ++i) {
                    x.add(X.column(i));
                }
            }
            return this;
        }

        public Builder addX(@NonNull DoubleSeq var) {
            if (var.length() != y.length()) {
                throw new RuntimeException("Incompatible dimensions");
            }
            x.add(var);
            return this;
        }

        public Builder addX(@NonNull DoubleSeq... vars) {
            for (DoubleSeq var : vars) {
                if (var.length() != y.length()) {
                    throw new RuntimeException("Incompatible dimensions");
                }
                x.add(var);
            }
            return this;
        }

        public Builder addX(@NonNull Collection<DoubleSeq> vars) {
            for (DoubleSeq var : vars) {
                if (var.length() != y.length()) {
                    throw new RuntimeException("Incompatible dimensions");
                }
                x.add(var);
            }
            return this;
        }

        public Builder removeX(int pos) {
            x.remove(pos);
            return this;
        }

        public Builder missing(int... missingPos) {
            this.missing = missingPos;
            return this;
        }

        public RegArimaModel<M> build() {
            if (y == null || arima == null) {
                throw new RuntimeException("Incomplete REGARIMA");
            }
            return new RegArimaModel<>(y, arima, mean, Collections.unmodifiableList(x), missing == null ? NOMISSING : missing, null);
        }
    }

    private final DoubleSeq y;
    private final M arima;
    private final boolean mean;
    private final List<DoubleSeq> x;
    private final int[] missing;
    private volatile RegArmaModel<M> dmodel;

    public static <M extends IArimaModel> Builder<M> builder() {
        return new Builder<>();
    }

    public static <M extends IArimaModel> RegArimaModel of(RegArimaModel<M> oldModel, M newArima) {
        RegArmaModel<M> dm = oldModel.dmodel;
        if (dm != null) {
            StationaryTransformation st = newArima.stationaryTransformation();
            if (st.getUnitRoots().equals(oldModel.arima.getNonStationaryAr())) {
                dm = RegArmaModel.of(dm, (M) st.getStationaryModel());
            } else {
                dm = null;
            }
        }
        return new RegArimaModel<>(oldModel.y, newArima, oldModel.mean, oldModel.x, oldModel.missing, dm);
    }

    /**
     *
     */
    private RegArimaModel(DoubleSeq y, final M arima, final boolean mean, final List<DoubleSeq> x, final int[] missing, final RegArmaModel<M> dmodel) {
        this.y = y;
        this.arima = arima;
        this.mean = mean;
        this.x = x;
        this.missing = missing;
        this.dmodel = dmodel;
    }

    /**
     * Gets the number of missing values
     *
     * @return
     */
    public int getMissingValuesCount() {
        return missing.length;
    }

    /**
     * Gets the number of observations (including missing values)
     *
     * @return
     */
    public int getObservationsCount() {
        return y.length();
    }

    /**
     * Gets the number of observations, excluding missing values (=
     * getObservationsCount()-getMissingValuesCount())
     *
     * @return
     */
    public int getActualObservationsCount() {
        return y.length() - missing.length;
    }

    /**
     * Gets the number of variables, including the mean but without the missing
     * values estimated by additive outliers.
     *
     * @return
     */
    public int getVariablesCount() {
        int nv = x.size();
        if (mean) {
            ++nv;
        }
        return nv;
    }

    /**
     * Gets the number of variables, excluding the mean and without the missing
     * values
     * estimated by additive outliers.
     *
     * @return
     */
    public int getXCount() {
        return x.size();
    }

    /**
     * @return the y
     */
    @NonNull
    public DoubleSeq getY() {
        return y;
    }

    /**
     * @return y not corrected for missing
     */
    @NonNull
    public DoubleSeq originalY() {
        if (missing.length == 0)
            return y;
        double[] z = y.toArray();
        for (int i=0; i<missing.length; ++i){
            z[missing[i]]=Double.NaN;
        }
        return DoubleSeq.of(z);
    }
    /**
     * @return the x
     */
    @NonNull
    public List<DoubleSeq> getX() {
        return x;
    }

    @NonNull
    public M arima() {
        return arima;
    }

    public boolean isMean() {
        return mean;
    }
    
    /**
     * Variables without AO corresponding to missing and without mean correction
     * @return 
     */
    public Matrix variables(){
        int n=y.length();
        Matrix m=Matrix.make(n, x.size());
        double[] storage = m.getStorage();
        int pos=0;
        for (DoubleSeq xcur:x){
            xcur.copyTo(storage, pos);
            pos+=n;
        }
        return m;
    }

    /**
     * All variables, including mean correction (but not AO for missing)
     * @return 
     */
    public MatrixType allVariables(){
        int n=y.length();
        int m=x.size();
        if (mean)
            ++m;
        double[] storage = new double[n*m];
        int pos=0;
        if (mean){
            RegArimaUtility.meanRegressionVariable(arima.getNonStationaryAr(), n, storage, 0);
            pos+=n;
        }
        for (DoubleSeq xcur:x){
            xcur.copyTo(storage, pos);
            pos+=n;
        }
        return MatrixType.of(storage, n, m);
    }

    @NonNull
    public int[] missing() {
        return missing.length == 0 ? NOMISSING : missing.clone();
    }

    public Builder<M> toBuilder() {
        Builder builder = new Builder();
        builder.y(y)
                .arima(arima)
                .meanCorrection(mean)
                .missing(missing);

        for (DoubleSeq v : x) {
            builder.addX(v);
        }
        return builder;
    }

    public RegArmaModel<M> differencedModel() {
        RegArmaModel<M> tmp = dmodel;
        if (tmp == null) {
            synchronized (this) {
                tmp = dmodel;
                if (tmp == null) {
                    tmp = RegArmaModel.of(this);
                    dmodel = tmp;
                }
            }
        }
        return tmp;
    }

    private static final int[] NOMISSING = new int[0];
}
