package demetra.sts;

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
import nbbrd.design.Development;
import nbbrd.design.Immutable;
import demetra.modelling.ComponentInformation;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDataTable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Immutable
@Development(status = Development.Status.Beta)
public final class BsmDecomposition {

    public static class Builder {

        private final EnumMap<Component, TsData> cmps = new EnumMap<>(Component.class),
                ecmps = new EnumMap<>(Component.class);

        private Builder() {
        }

        /**
         *
         * @param cmp
         * @param data
         * @return
         */
        public Builder add(TsData data, Component cmp) {
                      cmps.put(cmp, data);
                      return this;
        }

        public Builder addStde(TsData data, Component cmp) {
                     ecmps.put(cmp, data);
            return this;
        }

        public BsmDecomposition build() {
            return new BsmDecomposition(this);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

     private final Map<Component, TsData> cmps, ecmps;

    /**
     *
     * @param mode
     */
    private BsmDecomposition(Builder builder) {
         this.cmps = Collections.unmodifiableMap(builder.cmps);
        this.ecmps = Collections.unmodifiableMap(builder.ecmps);
     }


    /**
     *
     * @param cmp
     * @param stde
     * @return
     */
    public TsData getSeries(Component cmp, boolean stde) {
              return stde ?ecmps.get(cmp) : cmps.get(cmp);
     }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        // components
        if (!cmps.isEmpty()) {
            builder.append("components").append("\r\n");
            write(cmps, builder);
        }
        if (!ecmps.isEmpty()) {
            builder.append("components errors").append("\r\n");
            write(ecmps, builder);
        }
        return builder.toString();
    }

    private void write(Map<Component, TsData> cmps, StringBuilder builder) {
        List<TsData> all = new ArrayList<>();
        TsData s = cmps.get(Component.Series);
        if (s != null) {
            all.add(s);
        }
        s = cmps.get(Component.Level);
        if (s != null) {
            all.add(s);
        }
        s = cmps.get(Component.Slope);
        if (s != null) {
            all.add(s);
        }
        s = cmps.get(Component.Seasonal);
        if (s != null) {
            all.add(s);
        }
        s = cmps.get(Component.Cycle);
        if (s != null) {
            all.add(s);
        }
        s = cmps.get(Component.Noise);
        if (s != null) {
            all.add(s);
        }
        builder.append(TsDataTable.of(all)).append("\r\n");
    }

}
