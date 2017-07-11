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
package ec.satoolkit;

import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationMapping;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.simplets.TsData;
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
public class DefaultSeriesDecomposition implements ISeriesDecomposition, IProcResults {

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
        int icmp = cmp.intValue() - 1;
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
        int icmp = cmp.intValue() - 1;
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

    @Override
    public boolean contains(String id) {
        return MAPPING.contains(id);
    }

    @Override
    public Map<String, Class> getDictionary() {
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        MAPPING.fillDictionary(null, map, false);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return MAPPING.getData(this, id, tclass);
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    public static final String SPAN = "span", START = "start", END = "end", N = "n";
    // MAPPERS

    public static void fillDictionary(String prefix, Map<String, Class> map, boolean compact) {
        MAPPING.fillDictionary(prefix, map, compact);
    }

    // MAPPING
    public static InformationMapping<DefaultSeriesDecomposition> getMapping() {
        return MAPPING;
    }

    public static <T> void setMapping(String name, Class<T> tclass, Function<DefaultSeriesDecomposition, T> extractor) {
        MAPPING.set(name, tclass, extractor);
    }

    public static <T> void setTsData(String name, Function<DefaultSeriesDecomposition, TsData> extractor) {
        MAPPING.set(name, extractor);
    }

    private static final InformationMapping<DefaultSeriesDecomposition> MAPPING = new InformationMapping<>(DefaultSeriesDecomposition.class);

    static {
        MAPPING.set(ModellingDictionary.Y, source -> source.getSeries(ComponentType.Series, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.Y + SeriesInfo.F_SUFFIX, source -> source.getSeries(ComponentType.Series, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.T, source -> source.getSeries(ComponentType.Trend, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.T + SeriesInfo.F_SUFFIX, source -> source.getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.SA, source -> source.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.SA + SeriesInfo.F_SUFFIX, source -> source.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.S, source -> source.getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.S + SeriesInfo.F_SUFFIX, source -> source.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.I, source -> source.getSeries(ComponentType.Irregular, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.I + SeriesInfo.F_SUFFIX, source -> source.getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.MODE, DecompositionMode.class, source -> source.getMode());
    }
}
