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
package jdplus.sa;

import demetra.design.Development;
import demetra.modelling.ComponentInformation;
import demetra.sa.ComponentType;
import demetra.sa.DecompositionMode;
import demetra.timeseries.TsData;
import demetra.timeseries.TsException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class DefaultSeriesDecomposition implements SeriesDecomposition {

    private final DecompositionMode mode;
    private static final int ncmps = EnumSet.allOf(ComponentType.class).size() - 1;
    private final TsData[] cmps_, fcmps_, ecmps_, efcmps_;

    /**
     *
     * @param mode
     */
    public DefaultSeriesDecomposition(DecompositionMode mode) {
        this.mode = mode;
        cmps_ = new TsData[ncmps];
        fcmps_ = new TsData[ncmps];
        ecmps_ = new TsData[ncmps];
        efcmps_ = new TsData[ncmps];
    }

    /**
     *
     * @param cmp
     * @param data
     */
    public void add(TsData data, ComponentType cmp) {
        add(data, cmp, ComponentInformation.Value);
    }

    public void add(TsData data, ComponentType cmp, ComponentInformation info) {
        int icmp = cmp.getAsInt()- 1;
        if (icmp < 0) {
            throw new TsException("Invalid component type");
        }

        switch (info) {
            case Stdev:
                ecmps_[icmp] = data;
                break;
            case Forecast:
                fcmps_[icmp] = data;
                break;
            case StdevForecast:
                efcmps_[icmp] = data;
                break;
            default:
                cmps_[icmp] = data;
                break;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public DecompositionMode getMode() {
        return mode;
    }

    /**
     *
     * @param cmp
     * @param info
     * @return
     */
    @Override
    public TsData getSeries(ComponentType cmp, ComponentInformation info) {
        int icmp = cmp.getAsInt() - 1;
        if (icmp < 0) {
            return null;
        }

        switch (info) {
            case Stdev:
                return ecmps_[icmp];
            case Forecast:
                return fcmps_[icmp];
            case StdevForecast:
                return efcmps_[icmp];
            default:
                return cmps_[icmp];
        }
    }

}
