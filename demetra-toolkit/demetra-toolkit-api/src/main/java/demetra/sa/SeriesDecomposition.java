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
import demetra.design.Development;
import demetra.design.Immutable;
import demetra.modelling.ComponentInformation;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@Immutable
@Development(status = Development.Status.Beta)
public final class SeriesDecomposition {

    public static class Builder {

        private final DecompositionMode mode;
        private final EnumMap<ComponentType, DoubleSeq> cmps = new EnumMap<>(ComponentType.class),
                fcmps = new EnumMap<>(ComponentType.class),
                ecmps = new EnumMap<>(ComponentType.class),
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
        public Builder add(DoubleSeq data, ComponentType cmp) {
            return add(data, cmp, ComponentInformation.Value);
        }

        public Builder add(DoubleSeq data, ComponentType cmp, ComponentInformation info) {
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
                default:
                    cmps.put(cmp, data);
                    break;
            }
            return this;
        }
        
        public SeriesDecomposition build(){
            return new SeriesDecomposition(this);
        }

    }
    
    public static Builder builder(DecompositionMode mode){
        return new Builder(mode);
    }

    private final DecompositionMode mode;
    private final Map<ComponentType, DoubleSeq> cmps , fcmps, ecmps, efcmps;

    /**
     *
     * @param mode
     */
    private SeriesDecomposition(Builder builder) {
        this.mode = builder.mode;
        this.cmps=Collections.unmodifiableMap(builder.cmps);
        this.fcmps=Collections.unmodifiableMap(builder.fcmps);
        this.ecmps=Collections.unmodifiableMap(builder.ecmps);
        this.efcmps=Collections.unmodifiableMap(builder.efcmps);
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
    public DoubleSeq getSeries(ComponentType cmp, ComponentInformation info) {

        switch (info) {
            case Stdev:
                return ecmps.get(cmp);
            case Forecast:
                return fcmps.get(cmp);
            case StdevForecast:
                return efcmps.get(cmp);
            default:
                return cmps.get(cmp);
        }
    }

}
