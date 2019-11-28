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
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.tstoolkit.ucarima.WienerKolmogorovDiagnostics;
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
    private WienerKolmogorovDiagnostics diagnostics;

    void addProcessingInformation(ProcessingInformation info) {
        log_.add(info);
    }

    void addProcessingInformation(Collection<ProcessingInformation> info) {
        if (log_ != null && info != null) {
            log_.addAll(info);
        }
    }

    public WienerKolmogorovDiagnostics diagnostics() {
        if (diagnostics == null) {
            try {
                UcarimaModel ucm = decomposition.clone();
                if (ucm.getComponentsCount() > 3) {
                    ucm.compact(2, 2);
                }
                int[] cmps = new int[]{1, -2, 2, 3};
                double err = model.getSer();
                TsData t = initialComponents.getSeries(ComponentType.Trend, ComponentInformation.Value);
                TsData s = initialComponents.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
                TsData i = initialComponents.getSeries(ComponentType.Irregular, ComponentInformation.Value);
                TsData sa = initialComponents.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);

                double[][] data = new double[][]{
                    t == null ? null : t.internalStorage(),
                    s == null ? null : sa.internalStorage(),
                    s == null ? null : s.internalStorage(),
                    i == null ? null : i.internalStorage()
                };
                diagnostics = WienerKolmogorovDiagnostics.make(ucm, err, data, cmps);
            } catch (Exception e) {
            }
        }
        return diagnostics;
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
        MAPPING.fillDictionary(null, map, false);
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
            for (Information<T> info : sel) {
                all.put(info.name, info.value);
            }
        }
        return all;
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return log_ == null ? Collections.emptyList() : Collections.unmodifiableList(log_);
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

    public static final String CUTOFF = "parameters_cutoff", CHANGED = "model_changed", SEAS = "seasonality", AR_ROOT = "ar_root", MA_ROOT = "ma_root";
    public static final String 
            TVAR_ESTIMATE="tvar-estimate",
            TVAR_ESTIMATOR="tvar-estimator",
            TVAR_PVALUE="tvar-pvalue",
            SAVAR_ESTIMATE="savar-estimate",
            SAVAR_ESTIMATOR="savar-estimator",
            SAVAR_PVALUE="savar-pvalue",
            SVAR_ESTIMATE="svar-estimate",
            SVAR_ESTIMATOR="svar-estimator",
            SVAR_PVALUE="svar-pvalue",
            IVAR_ESTIMATE="ivar-estimate",
            IVAR_ESTIMATOR="ivar-estimator",
            IVAR_PVALUE="ivar-pvalue",
            SAAC1_ESTIMATE="saac1-estimate",
            SAAC1_ESTIMATOR="saac1-estimator",
            SAAC1_PVALUE="saac1-pvalue",
            IAC1_ESTIMATE="iac1-estimate",
            IAC1_ESTIMATOR="iac1-estimator",
            IAC1_PVALUE="iac1-pvalue",
            TICORR_ESTIMATE="ticorr-estimate",
            TICORR_ESTIMATOR="ticorr-estimator",
            TICORR_PVALUE="ticorr-pvalue",
            SICORR_ESTIMATE="sicorr-estimate",
            SICORR_ESTIMATOR="sicorr-estimator",
            SICORR_PVALUE="sicorr-pvalue",
            TSCORR_ESTIMATE="tscorr-estimate",
            TSCORR_ESTIMATOR="tscorr-estimator",
            TSCORR_PVALUE="tscorr-pvalue"
            ;
    private static final int T_CMP = 0, SA_CMP = 1, I_CMP = 3, S_CMP = 2;

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
        MAPPING.set(SEAS, Boolean.class, source -> !source.decomposition.getComponent(1).isNull());
        MAPPING.set(CUTOFF, Boolean.class, source -> source.model.isCutOff());
        MAPPING.set(CHANGED, Boolean.class, source -> source.model.isChanged());
        MAPPING.setList(AR_ROOT, 1, 3, Complex.class, (source, i) -> {
            Complex[] ar = source.model.getAutoRegressiveRoots();
            if (i > ar.length) {
                return null;
            } else {
                return ar[i - 1].inv();
            }
        });
        MAPPING.setList(MA_ROOT, 1, 3, Complex.class, (source, i) -> {
            Complex[] ma = source.model.getMovingAverageRoots();
            if (i > ma.length) {
                return null;
            } else {
                return ma[i - 1].inv();
            }
        });

        MAPPING.set(TVAR_ESTIMATOR, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimatorVariance(T_CMP);});
        MAPPING.set(TVAR_ESTIMATE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimateVariance(T_CMP);});
        MAPPING.set(TVAR_PVALUE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getPValue(T_CMP);});
        MAPPING.set(TVAR_ESTIMATOR, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimatorVariance(T_CMP);});
        MAPPING.set(TVAR_ESTIMATE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimateVariance(T_CMP);});
        MAPPING.set(TVAR_PVALUE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getPValue(T_CMP);});
        MAPPING.set(TVAR_ESTIMATOR, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimatorVariance(T_CMP);});
        MAPPING.set(TVAR_ESTIMATE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimateVariance(T_CMP);});
        MAPPING.set(TVAR_PVALUE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getPValue(T_CMP);});
        MAPPING.set(SAVAR_ESTIMATOR, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimatorVariance(SA_CMP);});
        MAPPING.set(SAVAR_ESTIMATE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimateVariance(SA_CMP);});
        MAPPING.set(SAVAR_PVALUE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getPValue(SA_CMP);});
        MAPPING.set(SVAR_ESTIMATOR, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimatorVariance(S_CMP);});
        MAPPING.set(SVAR_ESTIMATE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimateVariance(S_CMP);});
        MAPPING.set(SVAR_PVALUE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getPValue(S_CMP);});
        MAPPING.set(IVAR_ESTIMATOR, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimatorVariance(I_CMP);});
        MAPPING.set(IVAR_ESTIMATE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimateVariance(I_CMP);});
        MAPPING.set(IVAR_PVALUE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getPValue(I_CMP);});
//        MAPPING.set(SAAC1_ESTIMATOR, Double.class, source
//                ->{ 
//                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
//                return diag == null ? null : diag.(SA_CMP);});
//        MAPPING.set(SAAC1_ESTIMATE, Double.class, source
//                ->{ 
//                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
//                return diag == null ? null : diag.getEstimateVariance(SA_CMP);});
//        MAPPING.set(SAAC1_PVALUE, Double.class, source
//                ->{ 
//                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
//                return diag == null ? null : diag.getPValue(SA_CMP);});
        MAPPING.set(TSCORR_ESTIMATOR, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimatorCrossCorrelation(T_CMP, S_CMP);});
        MAPPING.set(TSCORR_ESTIMATE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimateCrossCorrelation(T_CMP, S_CMP);});
        MAPPING.set(TSCORR_PVALUE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getPValue(T_CMP, S_CMP);});
        MAPPING.set(TICORR_ESTIMATOR, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimatorCrossCorrelation(T_CMP, I_CMP);});
        MAPPING.set(TICORR_ESTIMATE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimateCrossCorrelation(T_CMP, I_CMP);});
        MAPPING.set(TICORR_PVALUE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getPValue(T_CMP, I_CMP);});
        MAPPING.set(SICORR_ESTIMATOR, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimatorCrossCorrelation(S_CMP, I_CMP);});
        MAPPING.set(SICORR_ESTIMATE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getEstimateCrossCorrelation(S_CMP, I_CMP);});
        MAPPING.set(SICORR_PVALUE, Double.class, source
                ->{ 
                WienerKolmogorovDiagnostics diag=source.diagnostics(); 
                return diag == null ? null : diag.getPValue(S_CMP, I_CMP);});
    }
}
