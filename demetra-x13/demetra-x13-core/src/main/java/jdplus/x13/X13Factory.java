/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13;

import demetra.modelling.TransformationType;
import demetra.regarima.RegArimaSpec;
import demetra.sa.DecompositionMode;
import demetra.sa.EstimationPolicy;
import demetra.sa.EstimationPolicyType;
import demetra.sa.SaDiagnosticsFactory;
import demetra.sa.SaProcessor;
import demetra.sa.SaSpecification;
import demetra.x11.X11Results;
import demetra.x11.X11Spec;
import demetra.x13.X13Spec;
import nbbrd.service.ServiceProvider;
import demetra.sa.SaProcessingFactory;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import jdplus.regarima.diagnostics.OutOfSampleDiagnosticsConfiguration;
import jdplus.regarima.diagnostics.OutliersDiagnosticsConfiguration;
import jdplus.regarima.diagnostics.ResidualsDiagnosticsConfiguration;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnostics;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnosticsConfiguration;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnosticsFactory;
import jdplus.sa.diagnostics.CoherenceDiagnostics;
import jdplus.sa.diagnostics.CoherenceDiagnosticsConfiguration;
import jdplus.sa.diagnostics.CoherenceDiagnosticsFactory;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnostics;
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

    public static final X13Factory INSTANCE = new X13Factory();

    private final List<SaDiagnosticsFactory<X13Results>> diagnostics = new CopyOnWriteArrayList<>();

    public X13Factory() {
        CoherenceDiagnosticsFactory<X13Results> coherence
                = new CoherenceDiagnosticsFactory<>(CoherenceDiagnosticsConfiguration.DEFAULT,
                        (X13Results r) -> {
                            return new CoherenceDiagnostics.Input(r.getDecomposition().getMode(), r);
                        }
                );
        SaOutOfSampleDiagnosticsFactory<X13Results> outofsample
                = new SaOutOfSampleDiagnosticsFactory<>(OutOfSampleDiagnosticsConfiguration.DEFAULT,
                        r -> r.getPreprocessing().regarima());
        SaResidualsDiagnosticsFactory<X13Results> residuals
                = new SaResidualsDiagnosticsFactory<>(ResidualsDiagnosticsConfiguration.DEFAULT,
                        r -> r.getPreprocessing());
        SaOutliersDiagnosticsFactory<X13Results> outliers
                = new SaOutliersDiagnosticsFactory<>(OutliersDiagnosticsConfiguration.DEFAULT,
                        r -> r.getPreprocessing());
        MDiagnosticsFactory mstats = new MDiagnosticsFactory(MDiagnosticsConfiguration.DEFAULT);
        AdvancedResidualSeasonalityDiagnosticsFactory<X13Results> advancedResidualSeasonality
                = new AdvancedResidualSeasonalityDiagnosticsFactory<>(AdvancedResidualSeasonalityDiagnosticsConfiguration.DEFAULT,
                        (X13Results r) -> {
                            boolean mul = r.getPreprocessing().getDescription().isLogTransformation();
                            TsData sa = r.getDecomposition().getD11();
                            TsData irr = r.getDecomposition().getD13();
                            return new AdvancedResidualSeasonalityDiagnostics.Input(mul, sa, irr);
                        }
                );
        ResidualTradingDaysDiagnosticsFactory<X13Results> residualTradingDays
                = new ResidualTradingDaysDiagnosticsFactory<>(ResidualTradingDaysDiagnosticsConfiguration.DEFAULT,
                        (X13Results r) -> {
                            boolean mul = r.getPreprocessing().getDescription().isLogTransformation();
                            TsData sa = r.getDecomposition().getD11();
                            TsData irr = r.getDecomposition().getD13();
                            return new ResidualTradingDaysDiagnostics.Input(mul, sa, irr);
                        }
                );

        diagnostics.add(coherence);
        diagnostics.add(residuals);
        diagnostics.add(outofsample);
        diagnostics.add(outliers);
        diagnostics.add(mstats);
        diagnostics.add(advancedResidualSeasonality);
        diagnostics.add(residualTradingDays);

    }

    @Override
    public X13Spec generateSpec(X13Spec spec, X13Results estimation) {
 
            RegArimaSpec nrspec = RegArimaFactory.INSTANCE.generateSpec(spec.getRegArima(), estimation.getPreprocessing().getDescription());
            X11Spec nxspec = update(spec.getX11(), estimation.getDecomposition());

            return spec.toBuilder()
                    .regArima(nrspec)
                    .x11(nxspec)
                    .build();
    }

    @Override
    public X13Spec refreshSpec(X13Spec currentSpec, X13Spec domainSpec, EstimationPolicyType policy, TsDomain frozen) {
        RegArimaSpec nrspec = RegArimaFactory.INSTANCE.refreshSpec(currentSpec.getRegArima(), domainSpec.getRegArima(), policy, frozen);
        X11Spec x11 = currentSpec.getX11();
        if (nrspec.getTransform().getFunction() == TransformationType.Auto){
            x11=x11.toBuilder()
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
    public List<SaDiagnosticsFactory<X13Results>> diagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }

    public void addDiagnostics(SaDiagnosticsFactory<X13Results> diag) {
        diagnostics.add(diag);
    }

    public void replaceDiagnostics(SaDiagnosticsFactory<X13Results> olddiag, SaDiagnosticsFactory<X13Results> newdiag) {
        int idx = diagnostics.indexOf(olddiag);
        if (idx < 0) {
            diagnostics.add(newdiag);
        } else {
            diagnostics.set(idx, newdiag);
        }
    }

}
