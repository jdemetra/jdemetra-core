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
package demetra.x11;

import demetra.modelling.ComponentInformation;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.ISaResults;
import ec.satoolkit.ISeriesDecomposition;
import demetra.algorithm.ProcessingInformation;
import demetra.modelling.ModellingDictionary;
import demetra.design.Development;
import demetra.information.Information;
import demetra.information.InformationMapping;
import demetra.information.InformationSet;
import demetra.modelling.ComponentType;
import demetra.modelling.SeriesInfo;
import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsDomain;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class X11Results implements ISaResults {

    public static final String SEASONALITY = "seasonality", TRENDFILTER = "trendfilter", SEASFILTER = "seasfilter";

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
        fb1 = fb1.cleanExtremities();
        if (!fb1.isEmpty()) {
            decomposition.add(fb1, ComponentType.Series, ComponentInformation.Forecast);
        }
        decomposition.add(info.subSet(X11Kernel.D).get(X11Kernel.D11L, TsData.class),
                ComponentType.SeasonallyAdjusted);
        TsData sal=info.subSet(X11Kernel.D).get(X11Kernel.D11aL, TsData.class);
        decomposition.add(sal, ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
        decomposition.add(info.subSet(X11Kernel.D).get(X11Kernel.D12L, TsData.class),
                ComponentType.Trend);
        decomposition.add(d10, ComponentType.Seasonal);
        decomposition.add(d10a, ComponentType.Seasonal, ComponentInformation.Forecast);
        TsData tl=info.subSet(X11Kernel.D).get(X11Kernel.D12aL, TsData.class);
        decomposition.add(tl, ComponentType.Trend, ComponentInformation.Forecast);
        decomposition.add(info.subSet(X11Kernel.D).get(X11Kernel.D13L, TsData.class),
                ComponentType.Irregular);
        // implicit forecast of the irregular: SA = T + I or I = SA - T
        TsData il=decomposition.getMode().isMultiplicative() ? TsData.divide(sal, tl) : TsData.subtract(sal, tl);
       decomposition.add(il, ComponentType.Irregular, ComponentInformation.Forecast);
    }
    private DefaultSeriesDecomposition decomposition;
    private InformationSet info_;

    public String getFinalSeasonalFilter() {
        InformationSet dtables = info_.getSubSet(X11Kernel.D);
        return dtables.get(X11Kernel.D9_FILTER, String.class);
    }

    public DefaultSeasonalFilteringStrategy[] getFinalSeasonalFilterComposit() {
        InformationSet dtables = info_.getSubSet(X11Kernel.D);
        return dtables.get(X11Kernel.D9_FILTER_COMPOSIT, DefaultSeasonalFilteringStrategy[].class);
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
        MAPPING.fillDictionary(null, dictionary, false);
        info_.fillDictionary(null, dictionary);
        return dictionary;
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
        if (!id.contains(InformationSet.STRSEP)) {
            return info_.deepSearch(id, tclass);
        } else {
            return info_.search(id, tclass);
        }
    }

    @Override
    public <T> Map<String, T> searchAll(String wc, Class<T> tclass) {
        Map<String, T> all = MAPPING.searchAll(this, wc, tclass);
        if (info_ != null) {
            List<Information<T>> sel = info_.select(wc, tclass);
            for (Information<T> info : sel) {
                all.put(info.name, info.value);
            }
        }
        return all;
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    public static void fillDictionary(String prefix, Map<String, Class> dic, boolean compact) {
        MAPPING.fillDictionary(prefix, dic, compact);
        // X11 dictionary:
        for (int i = 0; i < X11Kernel.ALL_A.length; ++i) {
            String code = InformationSet.concatenate(X11Kernel.A, X11Kernel.ALL_A[i]);
            dic.put(InformationSet.item(prefix, code), TsData.class);
        }
        for (int i = 0; i < X11Kernel.ALL_B.length; ++i) {
            String code = InformationSet.concatenate(X11Kernel.B, X11Kernel.ALL_B[i]);
            dic.put(InformationSet.item(prefix, code), TsData.class);
        }
        for (int i = 0; i < X11Kernel.ALL_C.length; ++i) {
            String code = InformationSet.concatenate(X11Kernel.C, X11Kernel.ALL_C[i]);
            dic.put(InformationSet.item(prefix, code), TsData.class);
        }
        for (int i = 0; i < X11Kernel.ALL_D.length; ++i) {
            String code = InformationSet.concatenate(X11Kernel.D, X11Kernel.ALL_D[i]);
            dic.put(InformationSet.item(prefix, code), TsData.class);
        }
        for (int i = 0; i < X11Kernel.ALL_E.length; ++i) {
            String code = InformationSet.concatenate(X11Kernel.E, X11Kernel.ALL_E[i]);
            dic.put(InformationSet.item(prefix, code), TsData.class);
        }
    }

    // MAPPING
    public static InformationMapping<X11Results> getMapping() {
        return MAPPING;
    }

    public static <T> void setMapping(String name, Class<T> tclass, Function<X11Results, T> extractor) {
        MAPPING.set(name, tclass, extractor);
    }

    public static <T> void setTsData(String name, Function<X11Results, TsData> extractor) {
        MAPPING.set(name, extractor);
    }

    private static final InformationMapping<X11Results> MAPPING = new InformationMapping<>(X11Results.class);

    static {
        MAPPING.set(ModellingDictionary.Y_CMP,
                source -> source.decomposition.getSeries(ComponentType.Series, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.Y_CMP + SeriesInfo.F_SUFFIX,
                source -> source.decomposition.getSeries(ComponentType.Series, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.T_CMP,
                source -> source.decomposition.getSeries(ComponentType.Trend, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.T_CMP + SeriesInfo.F_SUFFIX,
                source -> source.decomposition.getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.SA_CMP,
                source -> source.decomposition.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.S_CMP,
                source -> source.decomposition.getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.S_CMP + SeriesInfo.F_SUFFIX,
                source -> source.decomposition.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.I_CMP,
                source -> source.decomposition.getSeries(ComponentType.Irregular, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.MODE, DecompositionMode.class, source -> source.decomposition.getMode());
        MAPPING.set(SEASONALITY, Boolean.class, source -> !source.getFinalSeasonalFilter().equals(DummyFilter.NAME));
        MAPPING.set(TRENDFILTER, String.class, source -> source.getFinalTrendFilter());
        MAPPING.set(SEASFILTER, String.class, source -> source.getFinalSeasonalFilter());
    }
}
