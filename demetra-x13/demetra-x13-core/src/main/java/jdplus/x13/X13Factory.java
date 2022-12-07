/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13;

import demetra.modelling.TransformationType;
import demetra.processing.AlgorithmDescriptor;
import demetra.regarima.RegArimaSpec;
import demetra.sa.DecompositionMode;
import demetra.sa.EstimationPolicyType;
import demetra.sa.SaDiagnosticsFactory;
import demetra.sa.SaManager;
import demetra.sa.SaProcessor;
import demetra.sa.SaSpecification;
import jdplus.x11.X11Results;
import demetra.x11.X11Spec;
import demetra.x13.X13Spec;
import nbbrd.service.ServiceProvider;
import demetra.sa.SaProcessingFactory;
import demetra.timeseries.TsDomain;
import demetra.toolkit.dictionaries.Dictionary;
import demetra.x13.X13Dictionaries;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
import jdplus.x13.diagnostics.MDiagnosticsConfiguration;
import jdplus.x13.diagnostics.MDiagnosticsFactory;
import jdplus.x13.regarima.RegArimaFactory;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(SaProcessingFactory.class)
public class X13Factory implements SaProcessingFactory<X13Spec, X13Results> {

    public static X13Factory getInstance() {
        return (X13Factory) SaManager.processors().stream().filter(x -> x instanceof X13Factory).findAny().orElse(new X13Factory());
    }

    private final List<SaDiagnosticsFactory<?, X13Results>> diagnostics = new CopyOnWriteArrayList<>();

    public static List<SaDiagnosticsFactory<?, X13Results>> defaultDiagnostics() {
        CoherenceDiagnosticsFactory<X13Results> coherence
                = new CoherenceDiagnosticsFactory<>(CoherenceDiagnosticsConfiguration.getDefault(),
                        (X13Results r) -> {
                            return new CoherenceDiagnostics.Input(r.getDecomposition().getMode(), r);
                        }
                );

        SaOutOfSampleDiagnosticsFactory<X13Results> outofsample
                = new SaOutOfSampleDiagnosticsFactory<>(OutOfSampleDiagnosticsConfiguration.getDefault(),
                        r -> r.getDiagnostics().getGenericDiagnostics().forecastingTest());
        SaResidualsDiagnosticsFactory<X13Results> residuals
                = new SaResidualsDiagnosticsFactory<>(ResidualsDiagnosticsConfiguration.getDefault(),
                        r -> r.getPreprocessing());
        SaOutliersDiagnosticsFactory<X13Results> outliers
                = new SaOutliersDiagnosticsFactory<>(OutliersDiagnosticsConfiguration.getDefault(),
                        r -> r.getPreprocessing());
        MDiagnosticsFactory mstats = new MDiagnosticsFactory(MDiagnosticsConfiguration.getDefault());
        AdvancedResidualSeasonalityDiagnosticsFactory<X13Results> advancedResidualSeasonality
                = new AdvancedResidualSeasonalityDiagnosticsFactory<>(AdvancedResidualSeasonalityDiagnosticsConfiguration.getDefault(),
                        (X13Results r) -> r.getDiagnostics().getGenericDiagnostics()
                );
        ResidualTradingDaysDiagnosticsFactory<X13Results> residualTradingDays
                = new ResidualTradingDaysDiagnosticsFactory<>(ResidualTradingDaysDiagnosticsConfiguration.getDefault(),
                        (X13Results r) -> r.getDiagnostics().getGenericDiagnostics().residualTradingDaysTests()
                );

        List<SaDiagnosticsFactory<?, X13Results>> all = new ArrayList<>();
        all.add(coherence);
        all.add(residuals);
        all.add(outofsample);
        all.add(outliers);
        all.add(mstats);
        all.add(advancedResidualSeasonality);
        all.add(residualTradingDays);
        return all;
    }

    public X13Factory() {
        diagnostics.addAll(defaultDiagnostics());
    }

    @Override
    public AlgorithmDescriptor descriptor() {
        return X13Spec.DESCRIPTOR_V3;
    }

    @Override
    public X13Spec generateSpec(X13Spec spec, X13Results estimation) {

        RegArimaSpec nrspec = RegArimaFactory.getInstance().generateSpec(spec.getRegArima(), estimation.getPreprocessing().getDescription());
        X11Spec nxspec = update(spec.getX11(), estimation.getDecomposition());

        return spec.toBuilder()
                .regArima(nrspec)
                .x11(nxspec)
                .build();
    }

    @Override
    public X13Spec refreshSpec(X13Spec currentSpec, X13Spec domainSpec, EstimationPolicyType policy, TsDomain frozen) {
        if (policy == policy.None) {
            return currentSpec;
        }
        RegArimaSpec nrspec = RegArimaFactory.getInstance().refreshSpec(currentSpec.getRegArima(), domainSpec.getRegArima(), policy, frozen);
        X11Spec x11 = currentSpec.getX11();
        if (nrspec.getTransform().getFunction() == TransformationType.Auto) {
            x11 = x11.toBuilder()
                    .mode(DecompositionMode.Undefined)
                    .build();
        }
        return currentSpec.toBuilder()
                .regArima(nrspec)
                .x11(x11)
                .build();
    }

    private X11Spec update(X11Spec x11, X11Results rslts) {
        // Nothing to do (for the time being)
        return x11;
    }

    @Override
    public boolean canHandle(SaSpecification spec) {
        return spec instanceof X13Spec;
    }

    @Override
    public SaProcessor processor(X13Spec spec) {
        return (s, cxt, log) -> X13Kernel.of(spec, cxt).process(s, log);
    }

    @Override
    public X13Spec decode(SaSpecification spec) {
        if (spec instanceof X13Spec) {
            return (X13Spec) spec;
        } else {
            return null;
        }
    }

    @Override
    public List<SaDiagnosticsFactory<?, X13Results>> diagnosticFactories() {
        return Collections.unmodifiableList(diagnostics);
    }

    public void addDiagnostics(SaDiagnosticsFactory<?, X13Results> diag) {
        diagnostics.add(diag);
    }

    public void replaceDiagnostics(SaDiagnosticsFactory<?, X13Results> olddiag, SaDiagnosticsFactory<?, X13Results> newdiag) {
        int idx = diagnostics.indexOf(olddiag);
        if (idx < 0) {
            diagnostics.add(newdiag);
        } else {
            diagnostics.set(idx, newdiag);
        }
    }

    @Override
    public void resetDiagnosticFactories(List<SaDiagnosticsFactory<?, X13Results>> factories) {
        diagnostics.clear();
        diagnostics.addAll(factories);
    }

    @Override
    public Dictionary outputDictionary() {
        return X13Dictionaries.X13DICTIONARY;
    }

}
