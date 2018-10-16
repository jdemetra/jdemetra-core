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

import ec.benchmarking.simplets.TsCholette;
import ec.satoolkit.benchmarking.SaBenchmarkingResults;
import ec.satoolkit.benchmarking.SaBenchmarkingSpec;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.DefaultProcessingFactory;
import ec.tstoolkit.algorithm.IProcDocument;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing.Status;
import ec.tstoolkit.algorithm.IProcessingNode;
import ec.tstoolkit.algorithm.SequentialProcessing;
import ec.tstoolkit.algorithm.SingleTsData;
import ec.tstoolkit.algorithm.SingleTsDataProcessing.Validation;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class GenericSaProcessingFactory {
    // create series node

    public static final String FAMILY = "Seasonal adjustment";
    public static final String PREPROCESSING = "preprocessing",
            DECOMPOSITION = "decomposition", FINAL = "final", BENCHMARKING = "benchmarking", DIAGNOSTICS = "diagnostics", GENERAL = "general";
    public final static int MAX_REPEAT_COUNT = 80, MAX_MISSING_COUNT = 33;

    public static void testSeries(final TsData y) {
        if (y == null) {
            throw new SaException("Missing series");
        }
        int nz = y.getObsCount();
        int ifreq = y.getFrequency().intValue();
        if (nz < Math.max(8, 3 * ifreq)) {
            throw new SaException("Not enough data");
        }
        int nrepeat = y.getRepeatCount();
        if (nrepeat > MAX_REPEAT_COUNT * nz / 100) {
            throw new SaException("Too many identical values");
        }
        int nm = y.getMissingValuesCount();
        if (nm > MAX_MISSING_COUNT * nz / 100) {
            throw new SaException("Too many missing values");
        }
    }

    protected static <S extends IProcSpecification> IProcessingNode<TsData> createPreprocessingStep(final IPreprocessor preprocessor, final int ncasts, final String name, final String prefix) {
        return new IProcessingNode<TsData>() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getPrefix() {
                return prefix;
            }

            @Override
            public Status process(TsData ts, Map<String, IProcResults> results) {
                ModellingContext context = new ModellingContext();
                TsData s = series(ts, results);
                if (s == null) {
                    s = ts;
                }
                if (s == null) {
                    return Status.Invalid;
                }
                PreprocessingModel model = preprocessor.process(s, context);
                if (model == null) {
                    return Status.Invalid;
                } else {
                    model.setNcasts(ncasts);
                    results.put(name, model);
                    return Status.Valid;
                }
            }
        };
    }

    protected static IProcessingNode<TsData> createInitialStep(final TsPeriodSelector selector, boolean validate) {
        return (!validate) ? DefaultProcessingFactory.createInitialStep(selector) : DefaultProcessingFactory.createInitialStep(selector, new Validation() {
            @Override
            public boolean validate(TsData s) {
                testSeries(s);
                return true;//To change body of generated methods, choose Tools | Templates.
            }
        });
    }

    protected static TsData series(TsData ts, Map<String, IProcResults> results) {
        IProcResults preprocessing = results.get(PREPROCESSING);
        if (preprocessing != null && preprocessing instanceof PreprocessingModel) {
            TsData yc = preprocessing.getData(ModellingDictionary.YC, TsData.class);
            if (yc != null) {
                return yc;
            }
        }

        IProcResults input = results.get(IProcDocument.INPUT);
        if (input == null || !(input instanceof SingleTsData)) {
            return ts;
        }
        SingleTsData sdata = (SingleTsData) input;
        return sdata.getSeries();
    }

    protected static PreprocessingModel model(Map<String, IProcResults> results) {
        IProcResults imodel = results.get(PREPROCESSING);
        if (imodel == null || !(imodel instanceof PreprocessingModel)) {
            return null;
        }
        return (PreprocessingModel) imodel;
    }

    protected static ISaResults decomposition(Map<String, IProcResults> results) {
        IProcResults decomp = results.get(DECOMPOSITION);
        if (decomp == null || !(decomp instanceof ISaResults)) {
            return null;
        }
        return (ISaResults) decomp;
    }

    protected static void addInitialStep(TsPeriodSelector sel, boolean validation, SequentialProcessing sproc) {
        sproc.add(createInitialStep(sel, validation));
    }

    protected static void addInitialStep(TsPeriodSelector sel, SequentialProcessing sproc) {
        sproc.add(createInitialStep(sel, true));
    }

    protected static void addPreprocessingStep(IPreprocessor preprocessor, final int ncasts, SequentialProcessing sproc) {
        sproc.add(createPreprocessingStep(preprocessor, ncasts, PREPROCESSING, PREPROCESSING));
    }

    protected static <R extends ISaResults> IProcessingNode<TsData> createDecompositionStep(final IDefaultSeriesDecomposer<R> decomposer, final IPreprocessingFilter filter) {
        return new IProcessingNode<TsData>() {
            @Override
            public String getName() {
                return DECOMPOSITION;
            }

            @Override
            public String getPrefix() {
                return DECOMPOSITION;
            }

            @Override
            public Status process(final TsData ts, Map<String, IProcResults> results) {
                PreprocessingModel m = model(results);
                TsData s = series(ts, results);
                if (s == null) {
                    s = ts;
                }
                if (m == null) {
                    if (!decomposer.decompose(s)) {
                        return Status.Invalid;
                    }
                    R decomp = decomposer.getDecomposition();
                    results.put(DECOMPOSITION, decomp);
                    return Status.Valid;
                } else {
                    if (!filter.process(m)) {
                        return Status.Invalid;
                    }
                    if (!decomposer.decompose(m, filter)) {
                        return Status.Invalid;
                    }
                    R decomp = decomposer.getDecomposition();
                    results.put(DECOMPOSITION, decomp);
                    return Status.Valid;
                }
            }
        };
    }

    protected static <R extends ISaResults> void addDecompositionStep(final IDefaultSeriesDecomposer decomposer, IPreprocessingFilter filter, SequentialProcessing sproc) {
        sproc.add(createDecompositionStep(decomposer, filter));
    }

    protected static IProcessingNode<TsData> createFinalStep(final IPreprocessingFilter filter) {
        return new IProcessingNode<TsData>() {
            @Override
            public String getName() {
                return FINAL;
            }

            @Override
            public String getPrefix() {
                return FINAL;
            }

            @Override
            public Status process(final TsData ts, Map<String, IProcResults> results) {

                TsData orig = series(ts, results);
                if (orig == null) {
                    orig = ts;
                }
                ISaResults decomp = decomposition(results);
                ISeriesDecomposition ldecomp = decomp.getSeriesDecomposition();
                PreprocessingModel pm = model(results);
                if (pm == null) {
                    DefaultSeriesDecomposition finals = new DefaultSeriesDecomposition(decomp.getSeriesDecomposition().getMode());
                    // complete the decomposition
                    TsDomain domain = orig.getDomain();
                    TsData fy = ldecomp.getSeries(ComponentType.Series, ComponentInformation.Forecast);
                    TsDomain fdomain = fy == null ? null : fy.getDomain();

                    finals.add(orig, ComponentType.Series);
                    TsData t = ldecomp.getSeries(ComponentType.Trend,
                            ComponentInformation.Value);
                    if (t != null && !domain.equals(t.getDomain())) {
                        t = t.fittoDomain(domain);
                    }
                    finals.add(t, ComponentType.Trend);
                    TsData s = ldecomp.getSeries(ComponentType.Seasonal,
                            ComponentInformation.Value);
                    if (s != null && !domain.equals(s.getDomain())) {
                        s = s.fittoDomain(domain);
                    }
                    finals.add(s, ComponentType.Seasonal);
                    TsData i = ldecomp.getSeries(ComponentType.Irregular,
                            ComponentInformation.Value);
                    if (i != null && !domain.equals(i.getDomain())) {
                        i = i.fittoDomain(domain);
                    }
                    finals.add(i, ComponentType.Irregular);
                    TsData sa = ldecomp.getSeries(ComponentType.SeasonallyAdjusted,
                            ComponentInformation.Value);
                    if (sa != null && !domain.equals(sa.getDomain())) {
                        sa = sa.fittoDomain(domain);
                    }

                    finals.add(sa, ComponentType.SeasonallyAdjusted);

                    // forecasts...
                    if (fdomain != null) {
                        finals.add(fy, ComponentType.Series, ComponentInformation.Forecast);
                        TsData ft = ldecomp.getSeries(ComponentType.Trend,
                                ComponentInformation.Forecast);
                        if (ft != null) {
                            if (!fdomain.equals(ft.getDomain())) {
                                ft = ft.fittoDomain(fdomain);
                            }
                            finals.add(ft, ComponentType.Trend, ComponentInformation.Forecast);
                        }
                        TsData fs = ldecomp.getSeries(ComponentType.Seasonal,
                                ComponentInformation.Forecast);
                        if (fs != null) {
                            if (!fdomain.equals(fs.getDomain())) {
                                fs = fs.fittoDomain(fdomain);
                            }
                            finals.add(fs, ComponentType.Seasonal, ComponentInformation.Forecast);
                        }
                        TsData fsa = ldecomp.getSeries(ComponentType.SeasonallyAdjusted,
                                ComponentInformation.Forecast);
                        if (fsa != null) {
                            if (!fdomain.equals(fsa.getDomain())) {
                                fsa = fsa.fittoDomain(fdomain);
                            }
                            finals.add(fsa, ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
                        }
                    }
                    results.put(FINAL, finals);
                    return Status.Valid;
                } else if (!filter.isInitialized()) {
                    return Status.Invalid;
                } else {
                    boolean mul = ldecomp.getMode() != DecompositionMode.Additive;
                    TsDomain domain = orig.getDomain();
                    TsData fdata = ldecomp.getSeries(ComponentType.Series, ComponentInformation.Forecast);
                    TsDomain fdomain = fdata == null ? null : fdata.getDomain();
                    TsDomain cdomain = fdomain == null ? domain : domain.union(fdomain);
                    TsData detT = filter.getCorrection(cdomain, ComponentType.Trend, false);
                    TsData detS = filter.getCorrection(cdomain, ComponentType.Seasonal, false);
                    TsData detI = filter.getCorrection(cdomain, ComponentType.Irregular, false);
                    TsData detY = filter.getCorrection(cdomain, ComponentType.Series, false);
                    TsData detSA = filter.getCorrection(cdomain, ComponentType.SeasonallyAdjusted, false);
//                    TsData detU = filter.getCorrection(cdomain, ComponentType.Undefined, false);

                    DefaultSeriesDecomposition finals = new DefaultSeriesDecomposition(ldecomp.getMode());
                    TsData y = inv_op(mul, orig, detY);
                    finals.add(orig, ComponentType.Series);
                    TsData t = op(mul, detT, ldecomp.getSeries(ComponentType.Trend,
                            ComponentInformation.Value));
                    if (t != null && !domain.equals(t.getDomain())) {
                        t = t.fittoDomain(domain);
                    }
                    finals.add(t, ComponentType.Trend);
                    TsData s = op(mul, detS, ldecomp.getSeries(ComponentType.Seasonal,
                            ComponentInformation.Value));
                    if (s != null && !domain.equals(s.getDomain())) {
                        s = s.fittoDomain(domain);
                    }
                    finals.add(s, ComponentType.Seasonal);
                    TsData i = op(mul, detI, ldecomp.getSeries(ComponentType.Irregular,
                            ComponentInformation.Value));
                    if (i != null && !domain.equals(i.getDomain())) {
                        i = i.fittoDomain(domain);
                    }
                    finals.add(i, ComponentType.Irregular);
                    if (ldecomp.getMode() == DecompositionMode.PseudoAdditive) {
                        finals.add(op(mul, t, i), ComponentType.SeasonallyAdjusted);
                    } else {
                        finals.add(inv_op(mul, y, s), ComponentType.SeasonallyAdjusted);
                    }

                    // forecasts...
                    if (fdomain != null) {
                        TsData ftl = ldecomp.getSeries(ComponentType.Trend,
                                ComponentInformation.Forecast);
                        TsData ft = op(mul, detT, ftl);
                        if (ft != null) {
                            if (!fdomain.equals(ft.getDomain())) {
                                ft = ft.fittoDomain(fdomain);
                            }
                            finals.add(ft, ComponentType.Trend, ComponentInformation.Forecast);
                        }
                        TsData fsl = ldecomp.getSeries(ComponentType.Seasonal,
                                ComponentInformation.Forecast);
                        TsData fs = op(mul, detS, fsl);
                        if (fs != null) {
                            if (!fdomain.equals(fs.getDomain())) {
                                fs = fs.fittoDomain(fdomain);
                            }
                            finals.add(fs, ComponentType.Seasonal, ComponentInformation.Forecast);
                        }

                        TsData fil = ldecomp.getSeries(ComponentType.Irregular,
                                ComponentInformation.Forecast);
                        TsData fi = op(mul, detI, fil);
                        if (fi != null) {
                            if (!fdomain.equals(fi.getDomain())) {
                                fi = fi.fittoDomain(fdomain);
                            }
                            finals.add(fi, ComponentType.Irregular, ComponentInformation.Forecast);
                        }
                        TsData fy = pm.forecast(fdomain.getLength(), false);
                        finals.add(fy, ComponentType.Series, ComponentInformation.Forecast);
                        TsData fsa = op(mul, ft, fi);
                        fsa = op(mul, fsa, detSA);
                        if (fsa != null) {
                            if (!fdomain.equals(fsa.getDomain())) {
                                fsa = fsa.fittoDomain(fdomain);
                            }
                            finals.add(fsa, ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
                        }
                    }
                    results.put(FINAL, finals);
                    return Status.Valid;
                }
            }
        };
    }

    // computes the final decomposition
    protected static void addFinalStep(IPreprocessingFilter filter, SequentialProcessing sproc) {
        sproc.add(createFinalStep(filter));
    }

    protected static void addGeneralStep(SequentialProcessing sproc) {
        sproc.add(createGeneralStep());
    }

    protected static IProcessingNode<TsData> createGeneralStep() {

        return new IProcessingNode<TsData>() {
            @Override
            public String getName() {
                return GENERAL;
            }

            @Override
            public String getPrefix() {
                return null;
            }

            @Override
            public Status process(TsData s, Map<String, IProcResults> results) {
                PreprocessingModel pm = (PreprocessingModel) results.get(PREPROCESSING);
                ISaResults decomp = (ISaResults) results.get(DECOMPOSITION);
                ISeriesDecomposition finals = (ISeriesDecomposition) results.get(FINAL);
                GenericSaResults sa = GenericSaResults.of(pm, decomp, finals);
                if (sa == null) {
                    return Status.Unprocessed;
                }
                results.put(GENERAL, sa);
                return Status.Valid;
            }
        };
    }

    protected static void addDiagnosticsStep(SequentialProcessing sproc) {
        sproc.add(createDiagnosticsStep());
    }

    protected static IProcessingNode<TsData> createDiagnosticsStep() {

        return new IProcessingNode<TsData>() {
            @Override
            public String getName() {
                return DIAGNOSTICS;
            }

            @Override
            public String getPrefix() {
                return DIAGNOSTICS;
            }

            @Override
            public Status process(TsData s, Map<String, IProcResults> results) {
                PreprocessingModel pm = (PreprocessingModel) results.get(PREPROCESSING);
                ISaResults decomp = (ISaResults) results.get(DECOMPOSITION);
                ISeriesDecomposition finals = (ISeriesDecomposition) results.get(FINAL);
                GenericSaDiagnostics sa = GenericSaDiagnostics.of(pm, decomp, finals);
                if (sa == null) {
                    return Status.Unprocessed;
                }
                results.put(DIAGNOSTICS, sa);
                return Status.Valid;
            }
        };
    }

    protected static IProcessingNode<TsData> createBenchmarkingStep(final SaBenchmarkingSpec spec) {

        return new IProcessingNode<TsData>() {
            @Override
            public String getName() {
                return BENCHMARKING;
            }

            @Override
            public String getPrefix() {
                return BENCHMARKING;
            }

            @Override
            public Status process(TsData s, Map<String, IProcResults> results) {

                if (!spec.isEnabled()) {
                    return Status.Unprocessed;
                }
                TsData orig = CompositeResults.searchData(results, ModellingDictionary.YC, TsData.class);
                TsData cal = CompositeResults.searchData(results, ModellingDictionary.YCAL, TsData.class);
                TsData sa = CompositeResults.searchData(results, ModellingDictionary.SA, TsData.class);
                if (spec.isUsingForecast()) {
                    TsData origf = CompositeResults.searchData(results, ModellingDictionary.Y + SeriesInfo.F_SUFFIX, TsData.class);
                    TsData calf = CompositeResults.searchData(results, ModellingDictionary.YCAL + SeriesInfo.F_SUFFIX, TsData.class);
                    TsData saf = CompositeResults.searchData(results, ModellingDictionary.SA + SeriesInfo.F_SUFFIX, TsData.class);
                    orig = orig.update(origf);
                    cal = cal.update(calf);
                    sa = sa.update(saf);
                }
                TsData target = spec.getTarget() == SaBenchmarkingSpec.Target.Original ? orig : cal;

                if (target == null) {
                    target = series(s, results);
                }
                // computes the benchmarking...
                TsCholette cholette = new TsCholette();
                cholette.setAggregationType(TsAggregationType.Sum);
                cholette.setLambda(spec.getLambda());
                cholette.setRho(spec.getRho());
                cholette.setBiasCorrection(spec.getBias());
                target = target.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);

                TsData benchSa = cholette.process(sa, target);
                if (benchSa == null) {
                    return Status.Invalid;
                }
                results.put(BENCHMARKING, new SaBenchmarkingResults(sa, target, benchSa));
                return Status.Valid;
            }
        };
    }

    protected static void addBenchmarkingStep(SaBenchmarkingSpec spec, SequentialProcessing sproc) {
        sproc.add(createBenchmarkingStep(spec));
    }

    protected static TsData op(boolean mul, TsData l, TsData r) {
        if (mul) {
            return TsData.multiply(l, r);
        } else {
            return TsData.add(l, r);
        }
    }

    protected static TsData inv_op(boolean mul, TsData l, TsData r) {
        if (mul) {
            return TsData.divide(l, r);
        } else {
            return TsData.subtract(l, r);
        }
    }
}
