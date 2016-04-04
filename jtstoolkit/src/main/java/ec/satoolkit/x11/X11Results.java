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
package ec.satoolkit.x11;

import ec.tstoolkit.modelling.ComponentInformation;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.ISaResults;
import ec.satoolkit.ISeriesDecomposition;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class X11Results implements ISaResults {

    public X11Results(DecompositionMode mode, InformationSet info) {
        info_ = info;
        decomposition = new DefaultSeriesDecomposition(mode);
        TsData d10 = info.subSet(X11Kernel.D).get(X11Kernel.D10L, TsData.class);
        TsData d10a = info.subSet(X11Kernel.D).get(X11Kernel.D10aL, TsData.class);
        TsData b1 = info.subSet(X11Kernel.B).get(X11Kernel.B1, TsData.class);
        TsDomain cdom = d10.getDomain(), fdom = d10a.getDomain();
        decomposition.add(b1.fittoDomain(cdom), ComponentType.Series);
        TsData fb1 = b1.fittoDomain(fdom);
        // avoid missing values when b1 is "smaller" than fdom
        fb1=fb1.cleanExtremities();
        if (!fb1.isEmpty()) {
            decomposition.add(fb1, ComponentType.Series, ComponentInformation.Forecast);
        }
        decomposition.add(info.subSet(X11Kernel.D).get(X11Kernel.D11L, TsData.class),
                ComponentType.SeasonallyAdjusted);
        decomposition.add(info.subSet(X11Kernel.D).get(X11Kernel.D11aL, TsData.class),
                ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
        decomposition.add(info.subSet(X11Kernel.D).get(X11Kernel.D12L, TsData.class),
                ComponentType.Trend);
        decomposition.add(d10, ComponentType.Seasonal);
        decomposition.add(d10a, ComponentType.Seasonal, ComponentInformation.Forecast);
        decomposition.add(info.subSet(X11Kernel.D).get(X11Kernel.D12aL, TsData.class),
                ComponentType.Trend, ComponentInformation.Forecast);
        decomposition.add(info.subSet(X11Kernel.D).get(X11Kernel.D13L, TsData.class),
                ComponentType.Irregular);
    }
    private DefaultSeriesDecomposition decomposition;
    private InformationSet info_;

    public String getFinalSeasonalFilter() {
        InformationSet dtables = info_.getSubSet(X11Kernel.D);
        return dtables.get(X11Kernel.D9_FILTER, String.class);
    }

    public String getFinalTrendFilter() {
        InformationSet dtables = info_.getSubSet(X11Kernel.D);
        return dtables.get(X11Kernel.D12_FILTER, String.class);
    }

    @Override
    public ISeriesDecomposition getSeriesDecomposition() {
        return decomposition;
    }

    @Override
    public InformationSet getInformation() {
        return info_;
    }

    @Override
    public Map<String, Class> getDictionary() {
        LinkedHashMap<String, Class> dictionary = new LinkedHashMap<>();
        mapper.fillDictionary(null, dictionary);
        info_.fillDictionary(null, dictionary);
        return dictionary;
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
            if (!id.contains(InformationSet.STRSEP)) {
                return info_.deepSearch(id, tclass);
            } else {
                return info_.search(id, tclass);
            }
        }
    }
    
    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    public static void fillDictionary(String prefix, Map<String, Class> dic){
          mapper.fillDictionary(prefix, dic);
          // X11 dictionary:
          for (int i=0; i<X11Kernel.ALL_A.length; ++i){
              String code=InformationSet.concatenate(X11Kernel.A, X11Kernel.ALL_A[i]);
              dic.put(InformationSet.item(prefix, code), TsData.class);
          }
          for (int i=0; i<X11Kernel.ALL_B.length; ++i){
              String code=InformationSet.concatenate(X11Kernel.B, X11Kernel.ALL_B[i]);
              dic.put(InformationSet.item(prefix, code), TsData.class);
          }
          for (int i=0; i<X11Kernel.ALL_C.length; ++i){
              String code=InformationSet.concatenate(X11Kernel.C, X11Kernel.ALL_C[i]);
              dic.put(InformationSet.item(prefix, code), TsData.class);
          }
          for (int i=0; i<X11Kernel.ALL_D.length; ++i){
              String code=InformationSet.concatenate(X11Kernel.D, X11Kernel.ALL_D[i]);
              dic.put(InformationSet.item(prefix, code), TsData.class);
          }
         for (int i=0; i<X11Kernel.ALL_E.length; ++i){
              String code=InformationSet.concatenate(X11Kernel.E, X11Kernel.ALL_E[i]);
              dic.put(InformationSet.item(prefix, code), TsData.class);
          }
     }


    // MAPPERS
    public static <T> void addMapping(String name, InformationMapper.Mapper<X11Results, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }
    private static final InformationMapper<X11Results> mapper = new InformationMapper<>();

    static {
        mapper.add(ModellingDictionary.Y_CMP, new InformationMapper.Mapper<X11Results, TsData>(TsData.class) {
            @Override
            public TsData retrieve(X11Results source) {
                return source.decomposition.getSeries(ComponentType.Series, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.Y_CMP + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<X11Results, TsData>(TsData.class) {
            @Override
            public TsData retrieve(X11Results source) {
                return source.decomposition.getSeries(ComponentType.Series, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.T_CMP, new InformationMapper.Mapper<X11Results, TsData>(TsData.class) {
            @Override
            public TsData retrieve(X11Results source) {
                return source.decomposition.getSeries(ComponentType.Trend, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.T_CMP + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<X11Results, TsData>(TsData.class) {
            @Override
            public TsData retrieve(X11Results source) {
                return source.decomposition.getSeries(ComponentType.Trend, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.SA_CMP, new InformationMapper.Mapper<X11Results, TsData>(TsData.class) {
            @Override
            public TsData retrieve(X11Results source) {
                return source.decomposition.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.S_CMP, new InformationMapper.Mapper<X11Results, TsData>(TsData.class) {
            @Override
            public TsData retrieve(X11Results source) {
                return source.decomposition.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.S_CMP + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<X11Results, TsData>(TsData.class) {
            @Override
            public TsData retrieve(X11Results source) {
                return source.decomposition.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.I_CMP, new InformationMapper.Mapper<X11Results, TsData>(TsData.class) {
            @Override
            public TsData retrieve(X11Results source) {
                return source.decomposition.getSeries(ComponentType.Irregular, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.MODE, new InformationMapper.Mapper<X11Results, DecompositionMode>(DecompositionMode.class) {
            @Override
            public DecompositionMode retrieve(X11Results source) {
                return source.decomposition.getMode();
            }
        });
        mapper.add("seasonality", new InformationMapper.Mapper<X11Results, Boolean>(Boolean.class) {
            @Override
            public Boolean retrieve(X11Results source) {
                return !source.getFinalSeasonalFilter().equals(DummyFilter.NAME);
            }
        });
        mapper.add("trendfilter", new InformationMapper.Mapper<X11Results, String>(String.class) {
            @Override
            public String retrieve(X11Results source) {
                return source.getFinalTrendFilter();
            }
        });
        mapper.add("seasfilter", new InformationMapper.Mapper<X11Results, String>(String.class) {
            @Override
            public String retrieve(X11Results source) {
                return source.getFinalSeasonalFilter();
            }
        });
    }
}
