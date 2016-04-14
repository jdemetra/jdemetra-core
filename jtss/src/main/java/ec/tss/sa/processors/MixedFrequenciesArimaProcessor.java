/*
 * Copyright 2013-2014 National Bank of Belgium
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
package ec.tss.sa.processors;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.GenericSaProcessingFactory;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.IProcessingFactory;
import ec.tstoolkit.algorithm.IProcessingNode;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.algorithm.SequentialProcessing;
import ec.tstoolkit.arima.special.mixedfrequencies.EstimateSpec;
import ec.tstoolkit.arima.special.mixedfrequencies.MixedFrequenciesModelDecomposition;
import ec.tstoolkit.arima.special.mixedfrequencies.MixedFrequenciesModelEstimation;
import ec.tstoolkit.arima.special.mixedfrequencies.MixedFrequenciesMonitor;
import ec.tstoolkit.arima.special.mixedfrequencies.MixedFrequenciesSpecification;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Deprecated
public class MixedFrequenciesArimaProcessor implements IProcessingFactory<MixedFrequenciesSpecification, TsData[], CompositeResults> {

    public static final String FAMILY = GenericSaProcessingFactory.FAMILY;
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, "Mixed frequencies Arima model", null);

    public static final String PROCESSING = "processing", DECOMPOSITION = "decomposition", FINAL = "final", BENCHMARKING = "benchmarking";

    public static final MixedFrequenciesArimaProcessor instance = new MixedFrequenciesArimaProcessor();

    @Override
    public void dispose() {
    }

    @Override
    public AlgorithmDescriptor getInformation() {
        return DESCRIPTOR;
    }

    @Override
    public boolean canHandle(IProcSpecification spec) {
        return spec instanceof MixedFrequenciesSpecification;
    }

    @Override
    public IProcessing<TsData[], CompositeResults> generateProcessing(MixedFrequenciesSpecification specification, ProcessingContext context) {
        return new DefaultProcessing(specification);
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<MixedFrequenciesSpecification> specClass) {
        Map<String, Class> dic = new HashMap<>();
        MixedFrequenciesSpecification.fillDictionary(null, dic);
        return dic;
    }

    @Override
    public Map<String, Class> getOutputDictionary() {
        Map<String, Class> dic = new HashMap<>();
        return dic;
    }

    public static class DefaultProcessing implements IProcessing<TsData[], CompositeResults> {

        private final MixedFrequenciesSpecification spec;

        public DefaultProcessing(MixedFrequenciesSpecification spec) {
            this.spec = spec.clone();
        }

        @Override
        public CompositeResults process(TsData[] input) {
            SequentialProcessing processing = new SequentialProcessing();
            processing.add(createModellingStep(spec));
            processing.add(createDecompositionStep(spec));
            processing.add(createFinalStep());
            return processing.process(input);
        }

        protected static <S extends IProcSpecification> IProcessingNode<TsData[]> createModellingStep(final MixedFrequenciesSpecification spec) {
            return new IProcessingNode<TsData[]>() {

                @Override
                public String getName() {
                    return PROCESSING;
                }

                @Override
                public String getPrefix() {
                    return null;
                }

                @Override
                public Status process(TsData[] ts, Map<String, IProcResults> results) {
                    if (ts.length != 2) {
                        return Status.Invalid;
                    }
                    MixedFrequenciesMonitor monitor = new MixedFrequenciesMonitor();
                    if (!monitor.process(ts[0], ts[1], spec)) {
                        return Status.Invalid;
                    } else {
                        MixedFrequenciesModelEstimation rslt
                                = new MixedFrequenciesModelEstimation(monitor, spec.getBasic().getDataType(), spec.getBasic().isLog());
                        if (spec.getBasic().isLog() && spec.getEstimate().getMethod() == EstimateSpec.Method.KalmanFilter) {
                            int l = monitor.getLowFreqData().getLength();
                            rslt.setLogTransformation(l * Math.log(monitor.getFrequenciesRatio()));
                        }
                        results.put(PROCESSING, rslt);
                        return Status.Valid;
                    }
                }
            };
        }

        protected static <S extends IProcSpecification> IProcessingNode<TsData[]> createDecompositionStep(final MixedFrequenciesSpecification spec) {
            return new IProcessingNode<TsData[]>() {

                @Override
                public String getName() {
                    return DECOMPOSITION;
                }

                @Override
                public String getPrefix() {
                    return null;
                }

                @Override
                public Status process(TsData[] ts, Map<String, IProcResults> results) {
                    if (ts.length != 2) {
                        return Status.Invalid;
                    }
                    IProcResults proc = results.get(PROCESSING);
                    if (proc == null || !(proc instanceof MixedFrequenciesModelEstimation)) {
                        return Status.Unprocessed;
                    }
                    MixedFrequenciesModelEstimation model = (MixedFrequenciesModelEstimation) proc;
                    boolean log = spec.getBasic().isLog();
                    MixedFrequenciesModelDecomposition decomposition = new MixedFrequenciesModelDecomposition();
                    // linearized series
                    TsData h = model.getHighFreqLinearizedSeries(), l = model.getLowFreqLinearizedSeries();

                    if (decomposition.decompose(h, l, model.getArima(), spec.getArima().isMean(), spec.getBasic().getDataType(), log)) {
                        results.put(DECOMPOSITION, decomposition);
                        return Status.Valid;
                    } else {
                        return Status.Invalid;
                    }
                }
            };
        }

        protected static IProcessingNode<TsData[]> createFinalStep() {
            return new IProcessingNode<TsData[]>() {
                @Override
                public String getName() {
                    return FINAL;
                }

                @Override
                public String getPrefix() {
                    return null;
                }

                @Override
                public Status process(final TsData[] ts, Map<String, IProcResults> results) {

                    IProcResults proc0 = results.get(PROCESSING);
                    if (proc0 == null || !(proc0 instanceof MixedFrequenciesModelEstimation)) {
                        return Status.Unprocessed;
                    }
                    MixedFrequenciesModelEstimation model = (MixedFrequenciesModelEstimation) proc0;
                    IProcResults proc1 = results.get(DECOMPOSITION);
                    if (proc1 == null || !(proc1 instanceof MixedFrequenciesModelDecomposition)) {
                        return Status.Unprocessed;
                    }
                    MixedFrequenciesModelDecomposition decomp = (MixedFrequenciesModelDecomposition) proc1;

                    DefaultSeriesDecomposition finals = new DefaultSeriesDecomposition(decomp.isMultiplicative() ? DecompositionMode.Multiplicative
                            : DecompositionMode.Additive);
                    // complete the decomposition

                    finals.add(model.getInterpolatedSeries(false), ComponentType.Series);
                    finals.add(model.getInterpolationErrors(false), ComponentType.Series, ComponentInformation.Stdev);

                    boolean mul = decomp.isMultiplicative();
                    TsData y = decomp.getSeries(ComponentType.Series, ComponentInformation.Value);
                    TsDomain cdomain = y.getDomain();
                    TsData fy = decomp.getSeries(ComponentType.Series, ComponentInformation.Forecast);
                    TsDomain fdomain = fy.getDomain();
                    TsData detT = model.regressionEffect(cdomain, ComponentType.Series);
                    TsData detS = model.regressionEffect(cdomain, ComponentType.Seasonal);
                    TsData detC = model.regressionEffect(cdomain, ComponentType.CalendarEffect);
                    detS = TsData.add(detS, detC);
                    TsData detI = model.regressionEffect(cdomain, ComponentType.Irregular);
                    TsData detY = model.regressionEffect(cdomain);
                    TsData fdetT = model.regressionEffect(fdomain, ComponentType.Series);
                    TsData fdetS = model.regressionEffect(fdomain, ComponentType.Seasonal);
                    TsData fdetC = model.regressionEffect(fdomain, ComponentType.CalendarEffect);
                    fdetS = TsData.add(fdetS, fdetC);
                    TsData fdetI = model.regressionEffect(fdomain, ComponentType.Irregular);
                    TsData fdetY = model.regressionEffect(fdomain);
                    if (mul) {
                        if (detT != null) {
                            detT = detT.exp();
                            fdetT = fdetT.exp();
                        }
                        if (detS != null) {
                            detS = detS.exp();
                            fdetS = fdetS.exp();
                        }
                        if (detI != null) {
                            detI = detI.exp();
                            fdetI = fdetI.exp();
                        }
                        if (detY != null) {
                            detY = detY.exp();
                            fdetY = fdetY.exp();
                        }
                    }
                    finals.add(op(mul, y, detY), ComponentType.Series);
                    TsData t = op(mul, detT, decomp.getSeries(ComponentType.Trend,
                            ComponentInformation.Value));
                    finals.add(t, ComponentType.Trend);
                    TsData s = op(mul, detS, decomp.getSeries(ComponentType.Seasonal,
                            ComponentInformation.Value));
                    finals.add(s, ComponentType.Seasonal);
                    TsData i = op(mul, detI, decomp.getSeries(ComponentType.Irregular,
                            ComponentInformation.Value));
                    finals.add(i, ComponentType.Irregular);
                    finals.add(op(mul, t, i), ComponentType.SeasonallyAdjusted);
                    finals.add(op(mul, fy, fdetY), ComponentType.Series, ComponentInformation.Forecast);
                    TsData ft = op(mul, fdetT, decomp.getSeries(ComponentType.Trend,
                            ComponentInformation.Forecast));
                    finals.add(ft, ComponentType.Trend, ComponentInformation.Forecast);
                    TsData fs = op(mul, fdetS, decomp.getSeries(ComponentType.Seasonal,
                            ComponentInformation.Forecast));
                    finals.add(fs, ComponentType.Seasonal, ComponentInformation.Forecast);
                    TsData fi = op(mul, fdetI, decomp.getSeries(ComponentType.Irregular,
                            ComponentInformation.Forecast));
                    finals.add(fi, ComponentType.Irregular, ComponentInformation.Forecast);
                    finals.add(op(mul, ft, fi), ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);

                    results.put(FINAL, finals);
                    return Status.Valid;
                }

            };
        }

        private static TsData op(boolean mul, TsData l, TsData r) {
            if (mul) {
                return TsData.multiply(l, r);
            } else {
                return TsData.add(l, r);
            }
        }

        private static TsData inv_op(boolean mul, TsData l, TsData r) {
            if (mul) {
                return TsData.divide(l, r);
            } else {
                return TsData.subtract(l, r);
            }
        }
    }
}
