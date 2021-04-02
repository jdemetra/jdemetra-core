/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats;

import demetra.modelling.implementations.SarimaSpec;
import demetra.modelling.ComponentInformation;
import demetra.sa.ComponentType;
import demetra.sa.EstimationPolicy;
import demetra.sa.EstimationPolicyType;
import demetra.sa.SaDiagnosticsFactory;
import demetra.sa.SaProcessor;
import demetra.sa.SaSpecification;
import demetra.seats.DecompositionSpec;
import demetra.tramo.TramoSpec;
import demetra.tramoseats.TramoSeatsSpec;
import nbbrd.service.ServiceProvider;
import demetra.sa.SaProcessingFactory;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnostics;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnosticsConfiguration;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnosticsFactory;
import jdplus.sa.diagnostics.CoherenceDiagnostics;
import jdplus.sa.diagnostics.CoherenceDiagnosticsConfiguration;
import jdplus.sa.diagnostics.CoherenceDiagnosticsFactory;
import jdplus.regarima.diagnostics.OutOfSampleDiagnosticsConfiguration;
import jdplus.regarima.diagnostics.OutliersDiagnosticsConfiguration;
import jdplus.regarima.diagnostics.ResidualsDiagnosticsConfiguration;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnostics;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnosticsConfiguration;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnosticsFactory;
import jdplus.sa.diagnostics.SaOutOfSampleDiagnosticsFactory;
import jdplus.sa.diagnostics.SaOutliersDiagnosticsFactory;
import jdplus.sa.diagnostics.SaResidualsDiagnosticsFactory;
import jdplus.seats.diagnostics.SeatsDiagnosticsConfiguration;
import jdplus.seats.diagnostics.SeatsDiagnosticsFactory;
import jdplus.tramo.TramoFactory;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(SaProcessingFactory.class)
public class TramoSeatsFactory implements SaProcessingFactory<TramoSeatsSpec, TramoSeatsResults> {

    public static final TramoSeatsFactory INSTANCE = new TramoSeatsFactory();

    private final List<SaDiagnosticsFactory<TramoSeatsResults>> diagnostics = new CopyOnWriteArrayList<>();

    public TramoSeatsFactory() {
        CoherenceDiagnosticsFactory<TramoSeatsResults> coherence
                = new CoherenceDiagnosticsFactory<>(CoherenceDiagnosticsConfiguration.DEFAULT,
                        (TramoSeatsResults r) -> {
                            return new CoherenceDiagnostics.Input(r.getFinals().getMode(), r);
                        }
                );
        SaOutOfSampleDiagnosticsFactory<TramoSeatsResults> outofsample
                = new SaOutOfSampleDiagnosticsFactory<>(OutOfSampleDiagnosticsConfiguration.DEFAULT,
                        r -> r.getPreprocessing().regarima());
        SaResidualsDiagnosticsFactory<TramoSeatsResults> residuals
                = new SaResidualsDiagnosticsFactory<>(ResidualsDiagnosticsConfiguration.DEFAULT,
                        r -> r.getPreprocessing());
        SaOutliersDiagnosticsFactory<TramoSeatsResults> outliers
                = new SaOutliersDiagnosticsFactory<>(OutliersDiagnosticsConfiguration.DEFAULT,
                        r -> r.getPreprocessing());
        SeatsDiagnosticsFactory<TramoSeatsResults> seats
                = new SeatsDiagnosticsFactory<>(SeatsDiagnosticsConfiguration.DEFAULT,
                        r -> r.getDecomposition());
        AdvancedResidualSeasonalityDiagnosticsFactory<TramoSeatsResults> advancedResidualSeasonality
                = new AdvancedResidualSeasonalityDiagnosticsFactory<>(AdvancedResidualSeasonalityDiagnosticsConfiguration.DEFAULT,
                        (TramoSeatsResults r) -> {
                            boolean mul = r.getPreprocessing().getDescription().isLogTransformation();
                            TsData sa = r.getDecomposition().getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
                            TsData irr = r.getDecomposition().getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value);
                            return new AdvancedResidualSeasonalityDiagnostics.Input(mul, sa, irr);
                        }
                );
        ResidualTradingDaysDiagnosticsFactory<TramoSeatsResults> residualTradingDays
                = new ResidualTradingDaysDiagnosticsFactory<>(ResidualTradingDaysDiagnosticsConfiguration.DEFAULT,
                        (TramoSeatsResults r) -> {
                            boolean mul = r.getPreprocessing().getDescription().isLogTransformation();
                            TsData sa = r.getDecomposition().getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
                            TsData irr = r.getDecomposition().getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value);
                            return new ResidualTradingDaysDiagnostics.Input(mul, sa, irr);
                        }
                );

        diagnostics.add(coherence);
        diagnostics.add(residuals);
        diagnostics.add(outofsample);
        diagnostics.add(outliers);
        diagnostics.add(seats);
        diagnostics.add(advancedResidualSeasonality);
        diagnostics.add(residualTradingDays);

    }

    @Override
    public TramoSeatsSpec generateSpec(TramoSeatsSpec spec, TramoSeatsResults estimation) {
        return generateSpec(spec, estimation.getPreprocessing().getDescription());
    }
        
     public TramoSeatsSpec generateSpec(TramoSeatsSpec spec, GeneralLinearModel.Description<SarimaSpec> desc) {
 
            TramoSpec ntspec = TramoFactory.INSTANCE.generateSpec(spec.getTramo(), desc);
            DecompositionSpec nsspec = update(spec.getSeats());

            return spec.toBuilder()
                    .tramo(ntspec)
                    .seats(nsspec)
                    .build();
    }

    @Override
    public TramoSeatsSpec refreshSpec(TramoSeatsSpec currentSpec, TramoSeatsSpec domainSpec, EstimationPolicyType policy, TsDomain domain) {
        // NOT COMPLETE
        TramoSpec ntspec = TramoFactory.INSTANCE.refreshSpec(currentSpec.getTramo(), domainSpec.getTramo(), policy, domain);
        return currentSpec.toBuilder()
                .tramo(ntspec)
                .build();
   }

    private DecompositionSpec update(DecompositionSpec seats) {
        // Nothing to do (for the time being)
        return seats;
    }

    @Override
    public SaProcessor processor(TramoSeatsSpec spec) {
        return (s, cxt, log) -> TramoSeatsKernel.of(spec, cxt).process(s, log);
    }

    @Override
    public TramoSeatsSpec decode(SaSpecification spec) {
        if (spec instanceof TramoSeatsSpec) {
            return (TramoSeatsSpec) spec;
        } else {
            return null;
        }
    }

    @Override
    public boolean canHandle(SaSpecification spec) {
        return spec instanceof TramoSeatsSpec;
    }

    @Override
    public List<SaDiagnosticsFactory<TramoSeatsResults>> diagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }

    public void addDiagnostics(SaDiagnosticsFactory<TramoSeatsResults> diag) {
        diagnostics.add(diag);
    }

    public void replaceDiagnostics(SaDiagnosticsFactory<TramoSeatsResults> olddiag, SaDiagnosticsFactory<TramoSeatsResults> newdiag) {
        int idx = diagnostics.indexOf(olddiag);
        if (idx < 0) {
            diagnostics.add(newdiag);
        } else {
            diagnostics.set(idx, newdiag);
        }
    }

}
