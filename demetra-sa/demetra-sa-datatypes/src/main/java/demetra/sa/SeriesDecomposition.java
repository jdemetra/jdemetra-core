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


import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.modelling.ComponentInformation;
import java.util.EnumMap;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SeriesDecomposition {

    private final DecompositionMode mode;
    private final EnumMap<ComponentType, DoubleSequence> cmps = new EnumMap<>(ComponentType.class),
            fcmps = new EnumMap<>(ComponentType.class),
            ecmps = new EnumMap<>(ComponentType.class),
            efcmps = new EnumMap<>(ComponentType.class);

    /**
     *
     * @param mode
     */
    public SeriesDecomposition(DecompositionMode mode) {
        this.mode = mode;
    }

    /**
     *
     * @param cmp
     * @param data
     */
    public void add(DoubleSequence data, ComponentType cmp) {
        add(data, cmp, ComponentInformation.Value);
    }

    public void add(DoubleSequence data, ComponentType cmp, ComponentInformation info) {
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
    public DoubleSequence getSeries(ComponentType cmp, ComponentInformation info) {
 
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
