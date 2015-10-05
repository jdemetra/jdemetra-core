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
import ec.tstoolkit.information.InformationMapper;
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
    public Map<String, Class> getDictionary() {
        // TODO
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        mapper.fillDictionary(null, map);
        return map;
    }

    @Override
    public boolean contains(String id) {
        synchronized (mapper) {
            if (mapper.contains(id)) {
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
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        synchronized (mapper) {
            if (mapper.contains(id)) {
                return mapper.getData(this, id, tclass);
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
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return log_ == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(log_);
    }

    public static void fillDictionary(String prefix, Map<String, Class> map) {
        mapper.fillDictionary(prefix, map);
    }

    // MAPPERS
    public static <T> void addMapping(String name, InformationMapper.Mapper<SeatsResults, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }
    private static final InformationMapper<SeatsResults> mapper = new InformationMapper<>();

    static {
        mapper.add(ModellingDictionary.Y_LIN, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.Series, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.Y_LIN + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.Series, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.Y_LIN + SeriesInfo.EF_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.Series, ComponentInformation.StdevForecast);
            }
        });
        mapper.add(ModellingDictionary.T_LIN, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.Trend, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.T_LIN + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.Trend, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.T_LIN + SeriesInfo.E_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.Trend, ComponentInformation.Stdev);
            }
        });
        mapper.add(ModellingDictionary.T_LIN + SeriesInfo.EF_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.Trend, ComponentInformation.StdevForecast);
            }
        });
        mapper.add(ModellingDictionary.SA_LIN, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.SA_LIN + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.SA_LIN + SeriesInfo.E_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev);
            }
        });
        mapper.add(ModellingDictionary.SA_LIN + SeriesInfo.EF_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
            }
        });
        mapper.add(ModellingDictionary.S_LIN, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.S_LIN + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.S_LIN + SeriesInfo.E_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.Seasonal, ComponentInformation.Stdev);
            }
        });
        mapper.add(ModellingDictionary.S_LIN + SeriesInfo.EF_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast);
            }
        });
        mapper.add(ModellingDictionary.I_LIN, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.Irregular, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.I_LIN + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.Irregular, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.I_LIN + SeriesInfo.E_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.Irregular, ComponentInformation.Stdev);
            }
        });
        mapper.add(ModellingDictionary.I_LIN + SeriesInfo.EF_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.initialComponents.getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast);
            }
        });
        mapper.add(ModellingDictionary.Y_CMP, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.Series, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.Y_CMP + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.Series, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.T_CMP, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.Trend, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.T_CMP + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.Trend, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.SA_CMP, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.SA_CMP + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.S_CMP, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.S_CMP + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.I_CMP, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.Irregular, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.I_CMP + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.Irregular, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.I_CMP + SeriesInfo.E_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.Irregular, ComponentInformation.Stdev);
            }
        });
        mapper.add(ModellingDictionary.T_CMP + SeriesInfo.E_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.Trend, ComponentInformation.Stdev);
            }
        });
        mapper.add(ModellingDictionary.S_CMP + SeriesInfo.E_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.Seasonal, ComponentInformation.Stdev);
            }
        });
        mapper.add(ModellingDictionary.SA_CMP + SeriesInfo.E_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev);
            }
        });
        mapper.add(ModellingDictionary.I_CMP + SeriesInfo.EF_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast);
            }
        });
        mapper.add(ModellingDictionary.T_CMP + SeriesInfo.EF_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.Trend, ComponentInformation.StdevForecast);
            }
        });
        mapper.add(ModellingDictionary.S_CMP + SeriesInfo.EF_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast);
            }
        });
        mapper.add(ModellingDictionary.SA_CMP + SeriesInfo.EF_SUFFIX, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                return source.finalComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
            }
        });
        mapper.add(ModellingDictionary.SI_CMP, new InformationMapper.Mapper<SeatsResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SeatsResults source) {
                TsData i = source.finalComponents.getSeries(ComponentType.Irregular, ComponentInformation.Value);
                TsData s = source.finalComponents.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
                if (source.getSeriesDecomposition().getMode().isMultiplicative()) {
                    return TsData.multiply(s, i);
                } else {
                    return TsData.add(s, i);
                }
            }
        });
        mapper.add(ModellingDictionary.MODE, new InformationMapper.Mapper<SeatsResults, DecompositionMode>(DecompositionMode.class) {

            @Override
            public DecompositionMode retrieve(SeatsResults source) {
                return source.finalComponents.getMode();
            }
        });
        mapper.add("seasonality", new InformationMapper.Mapper<SeatsResults, Boolean>(Boolean.class) {

            @Override
            public Boolean retrieve(SeatsResults source) {
                return !source.decomposition.getComponent(1).isNull();
            }
        });
    }
}
