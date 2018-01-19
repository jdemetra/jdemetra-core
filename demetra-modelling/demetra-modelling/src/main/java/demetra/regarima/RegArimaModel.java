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
package demetra.regarima;

import demetra.arima.IArimaModel;
import demetra.arima.StationaryTransformation;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.design.Immutable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

        private DoubleSequence y;
        private M arima;
        private boolean mean;
        private final ArrayList<DoubleSequence> x = new ArrayList<>();
        private int[] missing;

        private Builder() {
        }

        public Builder y(DoubleSequence y) {
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

        public Builder addX(@Nonnull DoubleSequence var) {
            if (var.length() != y.length()) {
                throw new RuntimeException("Incompatible dimensions");
            }
            x.add(var);
            return this;
        }

        public Builder addX(@Nonnull DoubleSequence... vars) {
            for (DoubleSequence var : vars) {
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

        @Override
        public RegArimaModel<M> build() {
            if (y == null || arima == null) {
                throw new RuntimeException("Incomplete REGARIMA");
            }
            return new RegArimaModel<>(y, arima, mean, Collections.unmodifiableList(x), missing == null ? NOMISSING : missing, null);
        }
    }

    private final DoubleSequence y;
    private final M arima;
    private final boolean mean;
    private final List<DoubleSequence> x;
    private final int[] missing;
    private volatile RegArmaModel<M> dmodel;

    public static <M extends IArimaModel> Builder<M> builder() {
        return new Builder<>();
    }

    public static <M extends IArimaModel> RegArimaModel of(RegArimaModel<M> oldModel, M newArima) {
        RegArmaModel<M> dm = oldModel.dmodel;
        if (dm != null) {
            StationaryTransformation st = newArima.stationaryTransformation();
            if (st.getUnitRoots().equals(oldModel.arima.getNonStationaryAR())) {
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
    private RegArimaModel(DoubleSequence y, final M arima, final boolean mean, final List<DoubleSequence> x, final int[] missing, final RegArmaModel<M> dmodel) {
        this.y = y;
        this.arima = arima;
        this.mean = mean;
        this.x = x;
        this.missing = missing;
        this.dmodel = dmodel;
    }

    public int getMissingValuesCount() {
        return missing.length;
    }

    public int getObservationsCount() {
        return y.length();
    }

    public int getVariablesCount() {
        int nv = x.size();
        if (mean) {
            ++nv;
        }
        return nv;
    }

    /**
     * @return the y
     */
    @Nonnull
    public DoubleSequence getY() {
        return y;
    }

    /**
     * @return the x
     */
    @Nonnull
    public List<DoubleSequence> getX() {
        return x;
    }

    @Nonnull
    public M arima() {
        return arima;
    }

    public boolean isMean() {
        return mean;
    }

    @Nonnull
    public int[] missing() {
        return missing.length == 0 ? NOMISSING : missing.clone();
    }

    public Builder<M> toBuilder() {
        Builder builder = new Builder();
        builder.y(y)
                .arima(arima)
                .meanCorrection(mean)
                .missing(missing);

        for (DoubleSequence v : x) {
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
