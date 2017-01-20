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
package ec.satoolkit.seats;

import ec.satoolkit.*;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationMapping;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.tstoolkit.ucarima.WienerKolmogorovEstimators;
import ec.tstoolkit.utilities.Arrays2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SeatsResults implements ISaResults {

    public static final ComponentDescriptor SeasonallyAdjusted = new ComponentDescriptor("sa", 1, false, true);
    public static final ComponentDescriptor Trend = new ComponentDescriptor("trend", 0, true, true);
    public static final ComponentDescriptor Seasonal = new ComponentDescriptor("seasonal", 1, true, false);
    public static final ComponentDescriptor Transitory = new ComponentDescriptor("transitory", 2, true, false);
    public static final ComponentDescriptor Irregular = new ComponentDescriptor("irregular", 3, true, false);
    public static final List<ComponentDescriptor> descriptors = Arrays2.unmodifiableList(
            SeasonallyAdjusted,
            Trend,
            Seasonal,
            Transitory,
            Irregular);
    private static final ComponentDescriptor aIrregular = new ComponentDescriptor("irregular", 2, true, false);
    public static final List<ComponentDescriptor> airlineDescriptors = Arrays2.unmodifiableList(
            SeasonallyAdjusted,
            Trend,
            Seasonal,
            aIrregular);

    public static String[] getComponentsName(UcarimaModel ucm) {
        List<ComponentDescriptor> d = ucm.getComponentsCount() == 4
                ? descriptors : airlineDescriptors;
        String[] names = new String[d.size()];
        for (int i = 0; i < names.length; ++i) {
            names[i] = d.get(i).name;
        }
        return names;
    }

    public static ArimaModel[] getComponents(UcarimaModel ucm) {
        List<ComponentDescriptor> d = ucm.getComponentsCount() == 4
                ? descriptors : airlineDescriptors;
        ArimaModel[] cmps = new ArimaModel[d.size()];
        for (int i = 0; i < cmps.length; ++i) {
            cmps[i] = d.get(i).signal ? ucm.getComponent(d.get(i).cmp) : ucm.getComplement(d.get(i).cmp);
        }
        return cmps;
    }

    public SeatsResults() {
    }
    private WienerKolmogorovEstimators wk_;
    SeatsModel model;
    UcarimaModel decomposition;
    DefaultSeriesDecomposition initialComponents, finalComponents;
    InformationSet info_;
    private List<ProcessingInformation> log_ = new ArrayList<>();

    void addProcessingInformation(ProcessingInformation info) {
        log_.add(info);
    }

    void addProcessingInformation(Collection<ProcessingInformation> info) {
        if (log_ != null && info != null) {
            log_.addAll(info);
        }
    }

    @Override
    public DefaultSeriesDecomposition getSeriesDecomposition() {
        return finalComponents;
    }

    @Override
    public InformationSet getInformation() {
        return info_;
    }

    public UcarimaModel getUcarimaModel() {
        return decomposition;
    }

    public SeatsModel getModel() {
        return model;
    }

    public ISeriesDecomposition getComponents() {
        return initialComponents;
    }

    public WienerKolmogorovEstimators getWienerKolmogorovEstimators() {
        if (decomposition == null) {
            return null;
        }
        if (wk_ == null) {
            wk_ = new WienerKolmogorovEstimators(decomposition);
        }
        return wk_;
    }

    @Override
    public Map<String, Class> getDictionary(boolean compact) {
        // TODO
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        MAPPING.fillDictionary(null, map, compact);
        return map;
    }

    @Override
    public boolean contains(String id) {
        if (MAPPING.contains(id)) {
            return true;
        }
        if (info_ != null) {
            if (!id.contains(InformationSet.STRSEP)) {
                return info_.deepSearch(id, Object.class) != null;
            } else {
                return info_.search(id, Object.class) != null;
            }

        } else {
            return false;
        }
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        if (MAPPING.contains(id)) {
            return MAPPING.getData(this, id, tclass);
        }
        if (info_ != null) {
            if (!id.contains(InformationSet.STRSEP)) {
                return info_.deepSearch(id, tclass);
            } else {
                return info_.search(id, tclass);
            }
        } else {
            return null;
        }
    }

    @Override
    public <T> Map<String, T> searchAll(String wc, Class<T> tclass) {
        Map<String, T> all = MAPPING.searchAll(this, wc, tclass);
        if (info_ != null) {
            List<Information<T>> sel = info_.select(wc, tclass);
            for (Information<T> info: sel){
                all.put(info.name, info.value);
            }
        } 
        return all;
    }
    
    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return log_ == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(log_);
    }

    public static void fillDictionary(String prefix, Map<String, Class> map, boolean compact) {
        MAPPING.fillDictionary(prefix, map, compact);
    }

    // MAPPERS
    public static <T> void setMapping(String name, Class<T> tclass, Function<SeatsResults, T> extractor) {
        MAPPING.set(name, tclass, extractor);
    }

    public static InformationMapping<SeatsResults> getMapping() {
        return MAPPING;
    }

    public static <T> void set(String name, Function<SeatsResults, TsData> extractor) {
        MAPPING.set(name, extractor);
    }
    
    private static final InformationMapping<SeatsResults> MAPPING = new InformationMapping<>(SeatsResults.class);

    static {
        MAPPING.set(ModellingDictionary.Y_LIN, source -> source.initialComponents.getSeries(ComponentType.Series, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.Y_LIN + SeriesInfo.F_SUFFIX,
                source -> source.initialComponents.getSeries(ComponentType.Series, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.Y_LIN + SeriesInfo.EF_SUFFIX,
                source -> source.initialComponents.getSeries(ComponentType.Series, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.T_LIN, source -> source.initialComponents.getSeries(ComponentType.Trend, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.T_LIN + SeriesInfo.F_SUFFIX,
                source -> source.initialComponents.getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.T_LIN + SeriesInfo.E_SUFFIX,
                source -> source.initialComponents.getSeries(ComponentType.Trend, ComponentInformation.Stdev));
        MAPPING.set(ModellingDictionary.T_LIN + SeriesInfo.EF_SUFFIX,
                source -> source.initialComponents.getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.SA_LIN, source -> source.initialComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.SA_LIN + SeriesInfo.F_SUFFIX,
                source -> source.initialComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.SA_LIN + SeriesInfo.E_SUFFIX,
                source -> source.initialComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev));
        MAPPING.set(ModellingDictionary.SA_LIN + SeriesInfo.EF_SUFFIX,
                source -> source.initialComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.S_LIN, source -> source.initialComponents.getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.S_LIN + SeriesInfo.F_SUFFIX,
                source -> source.initialComponents.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.S_LIN + SeriesInfo.E_SUFFIX,
                source -> source.initialComponents.getSeries(ComponentType.Seasonal, ComponentInformation.Stdev));
        MAPPING.set(ModellingDictionary.S_LIN + SeriesInfo.EF_SUFFIX,
                source -> source.initialComponents.getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.I_LIN, source -> source.initialComponents.getSeries(ComponentType.Irregular, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.I_LIN + SeriesInfo.F_SUFFIX,
                source -> source.initialComponents.getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.I_LIN + SeriesInfo.E_SUFFIX,
                source -> source.initialComponents.getSeries(ComponentType.Irregular, ComponentInformation.Stdev));
        MAPPING.set(ModellingDictionary.I_LIN + SeriesInfo.EF_SUFFIX,
                source -> source.initialComponents.getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.Y_CMP, source -> source.finalComponents.getSeries(ComponentType.Series, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.Y_CMP + SeriesInfo.F_SUFFIX,
                source -> source.finalComponents.getSeries(ComponentType.Series, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.T_CMP, source -> source.finalComponents.getSeries(ComponentType.Trend, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.T_CMP + SeriesInfo.F_SUFFIX,
                source -> source.finalComponents.getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.SA_CMP, source -> source.finalComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.SA_CMP + SeriesInfo.F_SUFFIX,
                source -> source.finalComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.S_CMP, source -> source.finalComponents.getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.S_CMP + SeriesInfo.F_SUFFIX,
                source -> source.finalComponents.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.I_CMP, source -> source.finalComponents.getSeries(ComponentType.Irregular, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.I_CMP + SeriesInfo.F_SUFFIX,
                source -> source.finalComponents.getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.I_CMP + SeriesInfo.E_SUFFIX,
                source -> source.finalComponents.getSeries(ComponentType.Irregular, ComponentInformation.Stdev));
        MAPPING.set(ModellingDictionary.T_CMP + SeriesInfo.E_SUFFIX,
                source -> source.finalComponents.getSeries(ComponentType.Trend, ComponentInformation.Stdev));
        MAPPING.set(ModellingDictionary.S_CMP + SeriesInfo.E_SUFFIX,
                source -> source.finalComponents.getSeries(ComponentType.Seasonal, ComponentInformation.Stdev));
        MAPPING.set(ModellingDictionary.SA_CMP + SeriesInfo.E_SUFFIX,
                source -> source.finalComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev));
        MAPPING.set(ModellingDictionary.I_CMP + SeriesInfo.EF_SUFFIX,
                source -> source.finalComponents.getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.T_CMP + SeriesInfo.EF_SUFFIX,
                source -> source.finalComponents.getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.S_CMP + SeriesInfo.EF_SUFFIX,
                source -> source.finalComponents.getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.SA_CMP + SeriesInfo.EF_SUFFIX,
                source -> source.finalComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.SI_CMP, source -> {
            TsData i = source.finalComponents.getSeries(ComponentType.Irregular, ComponentInformation.Value);
            TsData s = source.finalComponents.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
            if (source.getSeriesDecomposition().getMode().isMultiplicative()) {
                return TsData.multiply(s, i);
            } else {
                return TsData.add(s, i);
            }
        });
        MAPPING.set(ModellingDictionary.MODE, DecompositionMode.class, source -> source.finalComponents.getMode());
        MAPPING.set("seasonality", Boolean.class, source -> !source.decomposition.getComponent(1).isNull());
    }
}
