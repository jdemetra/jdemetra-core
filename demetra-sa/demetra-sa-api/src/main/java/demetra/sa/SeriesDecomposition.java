package demetra.sa;

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
import demetra.information.GenericExplorable;
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
public final class SeriesDecomposition implements GenericExplorable{

    public static class Builder {

        private final DecompositionMode mode;
        private final EnumMap<ComponentType, TsData> cmps = new EnumMap<>(ComponentType.class),
                bcmps = new EnumMap<>(ComponentType.class),
                fcmps = new EnumMap<>(ComponentType.class),
                ecmps = new EnumMap<>(ComponentType.class),
                ebcmps = new EnumMap<>(ComponentType.class),
                efcmps = new EnumMap<>(ComponentType.class);

        private Builder(DecompositionMode mode) {
            this.mode = mode;
        }

        /**
         *
         * @param cmp
         * @param data
         * @return
         */
        public Builder add(TsData data, ComponentType cmp) {
            return add(data, cmp, ComponentInformation.Value);
        }

        public Builder add(TsData data, ComponentType cmp, ComponentInformation info) {
            switch (info) {
                case Stdev:
                    ecmps.put(cmp, data);
                    break;
                case Forecast:
                    fcmps.put(cmp, data);
                    break;
                case StdevForecast:
                    efcmps.put(cmp, data);
                    break;
                case Backcast:
                    bcmps.put(cmp, data);
                    break;
                case StdevBackcast:
                    ebcmps.put(cmp, data);
                    break;
                default:
                    cmps.put(cmp, data);
                    break;
            }
            return this;
        }

        public SeriesDecomposition build() {
            return new SeriesDecomposition(this);
        }

    }

    public static Builder builder(DecompositionMode mode) {
        return new Builder(mode);
    }

    private final DecompositionMode mode;
    private final Map<ComponentType, TsData> bcmps, cmps, fcmps, ebcmps, ecmps, efcmps;

    /**
     *
     * @param mode
     */
    private SeriesDecomposition(Builder builder) {
        this.mode = builder.mode;
        this.cmps = Collections.unmodifiableMap(builder.cmps);
        this.fcmps = Collections.unmodifiableMap(builder.fcmps);
        this.ecmps = Collections.unmodifiableMap(builder.ecmps);
        this.efcmps = Collections.unmodifiableMap(builder.efcmps);
        this.bcmps = Collections.unmodifiableMap(builder.bcmps);
        this.ebcmps = Collections.unmodifiableMap(builder.ebcmps);
    }

    /**
     *
     * @return
     */
    public DecompositionMode getMode() {
        return mode;
    }

    /**
     *
     * @param cmp
     * @param info
     * @return
     */
    public TsData getSeries(ComponentType cmp, ComponentInformation info) {

        switch (info) {
            case Stdev:
                return ecmps.get(cmp);
            case Forecast:
                return fcmps.get(cmp);
            case StdevForecast:
                return efcmps.get(cmp);
            case Backcast:
                return bcmps.get(cmp);
            case StdevBackcast:
                return ebcmps.get(cmp);
            default:
                return cmps.get(cmp);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        // components
        if (!bcmps.isEmpty()) {
            builder.append("backcasts").append("\r\n");
            write(bcmps, builder);
        }
        if (!cmps.isEmpty()) {
            builder.append("components").append("\r\n");
            write(cmps, builder);
        }
        if (!fcmps.isEmpty()) {
            builder.append("forecasts").append("\r\n");
            write(fcmps, builder);
        }
        if (!ebcmps.isEmpty()) {
            builder.append("backcasts errors").append("\r\n");
            write(ebcmps, builder);
        }
        if (!ecmps.isEmpty()) {
            builder.append("components errors").append("\r\n");
            write(ecmps, builder);
        }
        if (!efcmps.isEmpty()) {
            builder.append("forecasts errors").append("\r\n");
            write(efcmps, builder);
        }
        return builder.toString();
    }

    private void write(Map<ComponentType, TsData> cmps, StringBuilder builder) {
        List<TsData> all = new ArrayList<>();
        TsData s = cmps.get(ComponentType.Series);
        if (s != null) {
            all.add(s);
        }
        s = cmps.get(ComponentType.SeasonallyAdjusted);
        if (s != null) {
            all.add(s);
        }
        s = cmps.get(ComponentType.Trend);
        if (s != null) {
            all.add(s);
        }
        s = cmps.get(ComponentType.Seasonal);
        if (s != null) {
            all.add(s);
        }
        s = cmps.get(ComponentType.Irregular);
        if (s != null) {
            all.add(s);
        }
        builder.append(TsDataTable.of(all)).append("\r\n");
    }

}
