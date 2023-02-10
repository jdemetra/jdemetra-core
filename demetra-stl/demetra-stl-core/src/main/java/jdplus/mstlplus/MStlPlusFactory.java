/*
 * Copyright 2023 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.mstlplus;

import jdplus.stlplus.*;
import demetra.arima.SarimaSpec;
import demetra.modelling.regular.ModellingSpec;
import demetra.processing.AlgorithmDescriptor;
import demetra.sa.EstimationPolicyType;
import demetra.sa.SaDiagnosticsFactory;
import demetra.sa.SaManager;
import demetra.sa.SaProcessingFactory;
import demetra.sa.SaProcessor;
import demetra.sa.SaSpecification;
import demetra.stl.MStlPlusSpec;
import demetra.stl.StlDictionaries;
import demetra.stl.StlPlusSpec;
import demetra.stl.StlSpec;
import demetra.timeseries.TsDomain;
import demetra.toolkit.dictionaries.Dictionary;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import jdplus.modelling.GeneralLinearModel;
import jdplus.regarima.diagnostics.OutOfSampleDiagnosticsConfiguration;
import jdplus.regarima.diagnostics.OutliersDiagnosticsConfiguration;
import jdplus.regarima.diagnostics.ResidualsDiagnosticsConfiguration;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnosticsConfiguration;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnosticsFactory;
import jdplus.sa.diagnostics.CoherenceDiagnostics;
import jdplus.sa.diagnostics.CoherenceDiagnosticsConfiguration;
import jdplus.sa.diagnostics.CoherenceDiagnosticsFactory;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnosticsConfiguration;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnosticsFactory;
import jdplus.sa.diagnostics.SaOutOfSampleDiagnosticsFactory;
import jdplus.sa.diagnostics.SaOutliersDiagnosticsFactory;
import jdplus.sa.diagnostics.SaResidualsDiagnosticsFactory;
import jdplus.sa.regarima.FastRegArimaFactory;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(SaProcessingFactory.class)
public class MStlPlusFactory implements SaProcessingFactory<MStlPlusSpec, MStlPlusResults> {

    public static MStlPlusFactory getInstance() {
        return (MStlPlusFactory) SaManager.processors().stream().filter(x -> x instanceof MStlPlusFactory).findAny().orElse(new MStlPlusFactory());
    }

    private final List<SaDiagnosticsFactory<?, MStlPlusResults>> diagnostics = new CopyOnWriteArrayList<>();

    public MStlPlusFactory() {
        diagnostics.addAll(defaultDiagnostics());
    }

    public static List<SaDiagnosticsFactory<?, MStlPlusResults>> defaultDiagnostics() {
//        CoherenceDiagnosticsFactory<StlPlusResults> coherence
//                = new CoherenceDiagnosticsFactory<>(CoherenceDiagnosticsConfiguration.getDefault(),
//                        (StlPlusResults r) -> {
//                            return new CoherenceDiagnostics.Input(r.getFinals().getMode(), r);
//                        }
//                );
//        SaOutOfSampleDiagnosticsFactory<StlPlusResults> outofsample
//                = new SaOutOfSampleDiagnosticsFactory<>(OutOfSampleDiagnosticsConfiguration.getDefault(),
//                        r -> r.getDiagnostics().getGenericDiagnostics().forecastingTest());
//        SaResidualsDiagnosticsFactory<StlPlusResults> residuals
//                = new SaResidualsDiagnosticsFactory<>(ResidualsDiagnosticsConfiguration.getDefault(),
//                        r -> r.getPreprocessing());
//        SaOutliersDiagnosticsFactory<StlPlusResults> outliers
//                = new SaOutliersDiagnosticsFactory<>(OutliersDiagnosticsConfiguration.getDefault(),
//                        r -> r.getPreprocessing());
//
//        AdvancedResidualSeasonalityDiagnosticsFactory<StlPlusResults> advancedResidualSeasonality
//                = new AdvancedResidualSeasonalityDiagnosticsFactory<>(AdvancedResidualSeasonalityDiagnosticsConfiguration.getDefault(),
//                        (StlPlusResults r) -> r.getDiagnostics().getGenericDiagnostics()
//                );
//
//        ResidualTradingDaysDiagnosticsFactory<StlPlusResults> residualTradingDays
//                = new ResidualTradingDaysDiagnosticsFactory<>(ResidualTradingDaysDiagnosticsConfiguration.getDefault(),
//                        (StlPlusResults r) -> r.getDiagnostics().getGenericDiagnostics().residualTradingDaysTests()
//                );

        List<SaDiagnosticsFactory<?, MStlPlusResults>> all = new ArrayList<>();

//        all.add(coherence);
//        all.add(residuals);
//        all.add(outofsample);
//        all.add(outliers);
//        all.add(advancedResidualSeasonality);
//        all.add(residualTradingDays);
        return all;
    }

    @Override
    public AlgorithmDescriptor descriptor() {
        return StlPlusSpec.DESCRIPTOR;
    }

    @Override
    public MStlPlusSpec generateSpec(MStlPlusSpec spec, MStlPlusResults estimation) {
        return null;
//        return generateSpec(spec, estimation.getPreprocessing().getDescription());
    }

//    public MStlPlusSpec generateSpec(MStlPlusSpec spec, GeneralLinearModel.Description<SarimaSpec> desc) {
//
//        ModellingSpec ntspec = FastRegArimaFactory.getInstance().generateSpec(spec.getPreprocessing(), desc);
//        StlSpec nsspec = update(spec.getStl());
//
//        return spec.toBuilder()
//                .preprocessing(ntspec)
//                .stl(nsspec)
//                .build();
//    }

    @Override
    public MStlPlusSpec refreshSpec(MStlPlusSpec currentSpec, MStlPlusSpec domainSpec, EstimationPolicyType policy, TsDomain domain) {
        // NOT COMPLETE
//        if (policy == EstimationPolicyType.None) {
//            return currentSpec;
//        }
//        ModellingSpec ntspec = FastRegArimaFactory.getInstance().refreshSpec(currentSpec.getPreprocessing(), domainSpec.getPreprocessing(), policy, domain);
//        return currentSpec.toBuilder()
//                .preprocessing(ntspec)
//                .build();
        return null;
    }

//    private StlSpec update(StlSpec stl) {
//        // Nothing to do (for the time being)
//        return stl;
//    }

    @Override
    public SaProcessor processor(MStlPlusSpec spec) {
        return (s, cxt, log) -> MStlPlusKernel.of(spec, cxt).process(s, log);
    }

    @Override
    public MStlPlusSpec decode(SaSpecification spec) {
        if (spec instanceof MStlPlusSpec) {
            return (MStlPlusSpec) spec;
        } else {
            return null;
        }
    }

    @Override
    public boolean canHandle(SaSpecification spec) {
        return spec instanceof MStlPlusSpec;
    }

    @Override
    public List<SaDiagnosticsFactory<?, MStlPlusResults>> diagnosticFactories() {
        return Collections.unmodifiableList(diagnostics);
    }

    public void addDiagnostics(SaDiagnosticsFactory<?, MStlPlusResults> diag) {
        diagnostics.add(diag);
    }

    public void replaceDiagnostics(SaDiagnosticsFactory<?, MStlPlusResults> olddiag, SaDiagnosticsFactory<?, MStlPlusResults> newdiag) {
        int idx = diagnostics.indexOf(olddiag);
        if (idx < 0) {
            diagnostics.add(newdiag);
        } else {
            diagnostics.set(idx, newdiag);
        }
    }

    @Override
    public void resetDiagnosticFactories(List<SaDiagnosticsFactory<?, MStlPlusResults>> factories) {
        diagnostics.clear();
        diagnostics.addAll(factories);
    }

    @Override
    public Dictionary outputDictionary() {
        return StlDictionaries.STLPLUSDICTIONARY;
    }

}
