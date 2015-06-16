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
package ec.satoolkit.algorithm.implementation;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.GenericSaProcessingFactory;
import static ec.satoolkit.GenericSaProcessingFactory.PREPROCESSING;
import ec.satoolkit.ISaResults;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.benchmarking.SaBenchmarkingResults;
import ec.satoolkit.benchmarking.SaBenchmarkingSpec;
import ec.satoolkit.special.StmDecomposition;
import ec.satoolkit.special.StmEstimation;
import ec.satoolkit.special.StmSpecification;
import ec.tstoolkit.algorithm.*;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.DeterministicComponent;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.Method;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.structural.BsmMonitor;
import ec.tstoolkit.structural.BsmSpecification;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class StmProcessingFactory extends GenericSaProcessingFactory implements IProcessingFactory<StmSpecification, TsData, CompositeResults> {

    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, "Structural model", null);

    public static final String ESTIMATION = "estimation", DETERMINISTIC = "deterministic";

    private static SequentialProcessing<TsData> create(StmSpecification xspec, ProcessingContext context) {
        SequentialProcessing processing = new SequentialProcessing();
        if (xspec.getPreprocessingSpec().method != Method.None) {
            addPreprocessingStep(xspec.buildPreprocessor(context), processing);
        }
        addEstimationStep(xspec.getDecompositionSpec(), processing);
        addDeterministicStep(processing);
        addDecompositionStep(processing);
        addFinalStep(processing);
        SaBenchmarkingSpec bspec = xspec.getBenchmarkingSpec();
        if (bspec != null && bspec.isEnabled()) {
            addBenchmarkingStep(bspec, processing);
        }
        return processing;
    }
    
    protected static void addPreprocessingStep(IPreprocessor preprocessor, SequentialProcessing sproc) {
        sproc.add(createPreprocessingStep(preprocessor, PREPROCESSING, PREPROCESSING));
    }

    public static final StmProcessingFactory instance = new StmProcessingFactory();

    protected StmProcessingFactory() {
    }

    public static CompositeResults process(TsData s, StmSpecification xspec) {
        SequentialProcessing<TsData> processing = create(xspec, null);
        return processing.process(s);
    }

    @Override
    public SequentialProcessing<TsData> generateProcessing(StmSpecification xspec, ProcessingContext context) {
        return create(xspec, context);
    }

    public SequentialProcessing<TsData> generateProcessing(StmSpecification xspec) {
        return create(xspec, null);
    }

    @Override
    public AlgorithmDescriptor getInformation() {
        return DESCRIPTOR;
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean canHandle(IProcSpecification spec) {
        return spec instanceof StmSpecification;
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<StmSpecification> specClass) {
        HashMap<String, Class> dic = new HashMap<>();
        StmSpecification.fillDictionary(null, dic);
        return dic;
    }

    @Override
    public Map<String, Class> getOutputDictionary() {
        HashMap<String, Class> dic = new HashMap<>();
        PreprocessingModel.fillDictionary(null, dic);
        DefaultSeriesDecomposition.fillDictionary(null, dic);
        SaBenchmarkingResults.fillDictionary(BENCHMARKING, dic);
        return dic;
    }

    private static void addEstimationStep(BsmSpecification decompositionSpec, SequentialProcessing processing) {
        processing.add(createEstimationStep(decompositionSpec));
    }

    protected static <S extends IProcSpecification> IProcessingNode<TsData> createEstimationStep(final BsmSpecification decompositionSpec) {
        return new IProcessingNode<TsData>() {

            @Override
            public String getName() {
                return ESTIMATION;
            }

            @Override
            public String getPrefix() {
                return ESTIMATION;
            }

            @Override
            public IProcessing.Status process(TsData input, Map<String, IProcResults> results) {
                PreprocessingModel model = model(results);
                StmEstimation estimation = null;
                BsmMonitor monitor = new BsmMonitor();
                monitor.setSpecification(decompositionSpec);
                if (model == null) {
                    if (monitor.process(input.getValues().internalStorage(), input.getFrequency().intValue())) {
                        estimation = new StmEstimation(input, new TsVariableList(), monitor);
                    }
                } else {
                    TsData y = model.description.transformedOriginal();
                    TsVariableList x = model.description.buildRegressionVariables();
                    if (x.isEmpty()) {
                        if (monitor.process(input.getValues().internalStorage(), input.getFrequency().intValue())) {
                            estimation = new StmEstimation(input, x, monitor);
                        }
                    } else {
                        Matrix mx = x.all().matrix(y.getDomain());
                        if (monitor.process(y.getValues().internalStorage(), mx.subMatrix(), y.getFrequency().intValue())) {
                            estimation = new StmEstimation(y, x, monitor);
                        }
                    }
                }
                if (estimation == null) {
                    return IProcessing.Status.Invalid;
                } else {
                    results.put(ESTIMATION, estimation);
                    return IProcessing.Status.Valid;
                }
            }
        };
    }

    private static void addDeterministicStep(SequentialProcessing processing) {
        processing.add(createDeterministicStep());
    }

    protected static <S extends IProcSpecification> IProcessingNode<TsData> createDeterministicStep() {
        return new IProcessingNode<TsData>() {

            @Override
            public String getName() {
                return DETERMINISTIC;
            }

            @Override
            public String getPrefix() {
                return null;
            }

            @Override
            public IProcessing.Status process(TsData input, Map<String, IProcResults> results) {
                PreprocessingModel model = model(results);
                if (model == null) {
                    return IProcessing.Status.Unprocessed;
                }
                StmEstimation estimation = estimation(results);
                if (estimation == null) {
                    return IProcessing.Status.Invalid;
                }
                DeterministicComponent det = model.getDeterministicComponent();
                det.setCoefficients(new ReadDataBlock(estimation.getLikelihood().getB()));
                results.put(DETERMINISTIC, det);
                return IProcessing.Status.Valid;
            }
        };
    }

    private static void addDecompositionStep(SequentialProcessing processing) {
        processing.add(createDecompositionStep());
    }

    protected static <S extends IProcSpecification> IProcessingNode<TsData> createDecompositionStep() {
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
            public IProcessing.Status process(TsData input, Map<String, IProcResults> results) {
                StmEstimation e = estimation(results);
                if (e == null) {
                    return IProcessing.Status.Unprocessed;
                }
                // Gets the linearized series.
                DeterministicComponent det = deterministic(results);
                TsData y;
                boolean mul;
                if (det == null) {
                    y = input;
                    mul = false;
                } else {
                    y = det.linearizedSeries();
                    mul = det.isMultiplicative();
                }
                StmDecomposition decomposition = new StmDecomposition(y, e.getModel(), mul);
                results.put(DECOMPOSITION, decomposition);
                return IProcessing.Status.Valid;
            }
        };
    }

    private static void addFinalStep(SequentialProcessing processing) {
        processing.add(createFinalStep());
    }

    protected static <S extends IProcSpecification> IProcessingNode<TsData> createFinalStep() {
        return new IProcessingNode<TsData>() {

            @Override
            public String getName() {
                return FINAL;
            }

            @Override
            public String getPrefix() {
                return null;
            }

            @Override
            public IProcessing.Status process(TsData input, Map<String, IProcResults> results) {
                ISaResults decomp = decomposition(results);
                ISeriesDecomposition ldecomp = decomp.getSeriesDecomposition();
                DeterministicComponent det = deterministic(results);
                if (det == null) {
                    DefaultSeriesDecomposition finals = new DefaultSeriesDecomposition(decomp.getSeriesDecomposition().getMode());
                    // complete the decomposition
                    TsDomain domain = input.getDomain();
                    TsData fy = ldecomp.getSeries(ComponentType.Series, ComponentInformation.Forecast);
                    TsDomain fdomain = fy == null ? null : fy.getDomain();

                    finals.add(input, ComponentType.Series);
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
                    return IProcessing.Status.Valid;
                } else {
                    boolean mul = ldecomp.getMode() != DecompositionMode.Additive;
                    TsDomain domain = input.getDomain();
                    TsData fdata = ldecomp.getSeries(ComponentType.Series, ComponentInformation.Forecast);
                    TsDomain fdomain = fdata == null ? null : fdata.getDomain();
                    TsDomain cdomain = fdomain == null ? domain : domain.union(fdomain);
                    TsData detT = det.deterministicEffect(cdomain, ComponentType.Trend);
                    if (detT != null) {
                        det.backTransform(detT, true, false);
                    }
                    TsData detS = det.deterministicEffect(cdomain, ComponentType.Seasonal);
                    TsData detC = det.deterministicEffect(cdomain, ComponentType.CalendarEffect);
                    if (detS != null || detC != null) {
                        detS = op(false, detS, detC);
                        det.backTransform(detS, false, true);
                    }
                    TsData detI = det.deterministicEffect(cdomain, ComponentType.Irregular);
                    if (detI != null) {
                        det.backTransform(detI, false, false);
                    }
                    TsData detY = det.deterministicEffect(cdomain, ComponentType.Series);
                    if (detY != null) {
                        det.backTransform(detY, false, false);
                    }

                    DefaultSeriesDecomposition finals = new DefaultSeriesDecomposition(ldecomp.getMode());
                    // ???
                    TsData y = inv_op(mul, input, detY);
                    finals.add(y, ComponentType.Series);
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
                    finals.add(inv_op(mul, y, s), ComponentType.SeasonallyAdjusted);

                    // forecasts...
                    TsData ndet = op(mul, detT, detS);
                    ndet = op(mul, ndet, detI);

                    if (fdomain != null) {
                        TsData fy = op(mul, fdata, ndet);
                        //               finals.add(fy, ComponentType.Series, ComponentInformation.Forecast);
                        TsData ftl = ldecomp.getSeries(ComponentType.Trend,
                                ComponentInformation.Forecast);
                        if (ftl != null) {
                            TsData ft = op(mul, detT, ftl);
                            if (!fdomain.equals(ft.getDomain())) {
                                ft = ft.fittoDomain(fdomain);
                            }
                            finals.add(ft, ComponentType.Trend, ComponentInformation.Forecast);
                        }
                        TsData fsl = ldecomp.getSeries(ComponentType.Seasonal,
                                ComponentInformation.Forecast);
                        TsData fs = null;
                        if (fsl != null) {
                            fs = op(mul, detS, fsl);
                            if (!fdomain.equals(fs.getDomain())) {
                                fs = fs.fittoDomain(fdomain);
                            }
                            finals.add(fs, ComponentType.Seasonal, ComponentInformation.Forecast);
                        }

                        TsData fil = ldecomp.getSeries(ComponentType.Irregular,
                                ComponentInformation.Forecast);
                        if (fil != null) {
                            TsData fi = op(mul, detI, fil);
                            if (!fdomain.equals(fi.getDomain())) {
                                fi = fi.fittoDomain(fdomain);
                            }
                            finals.add(fi, ComponentType.Irregular, ComponentInformation.Forecast);
                        }
                        TsData fsa = inv_op(mul, fy, fs);
                        if (fsa != null) {
                            if (!fdomain.equals(fsa.getDomain())) {
                                fsa = fsa.fittoDomain(fdomain);
                            }
                            finals.add(fsa, ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
                        }
                    }
                    results.put(FINAL, finals);
                    return IProcessing.Status.Valid;
                }
            }
        };
    }

    protected static StmEstimation estimation(Map<String, IProcResults> results) {
        IProcResults imodel = results.get(ESTIMATION);
        if (imodel == null || !(imodel instanceof StmEstimation)) {
            return null;
        }
        return (StmEstimation) imodel;
    }

    protected static DeterministicComponent deterministic(Map<String, IProcResults> results) {
        IProcResults imodel = results.get(DETERMINISTIC);
        if (imodel == null || !(imodel instanceof DeterministicComponent)) {
            return null;
        }
        return (DeterministicComponent) imodel;
    }

}
