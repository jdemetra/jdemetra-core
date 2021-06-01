/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats;

import demetra.modelling.implementations.SarimaSpec;
import demetra.data.Parameter;
import demetra.data.ParameterType;
import demetra.likelihood.LikelihoodStatistics;
import demetra.processing.ProcessingLog;
import demetra.sa.ComponentType;
import demetra.sa.SeriesDecomposition;
import demetra.seats.SeatsModelSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import demetra.tramo.TransformSpec;
import demetra.tramoseats.TramoSeatsSpec;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.sa.modelling.RegArimaDecomposer;
import jdplus.sa.modelling.TwoStepsDecomposition;
import jdplus.sarima.SarimaModel;
import jdplus.seats.SeatsKernel;
import jdplus.seats.SeatsResults;
import jdplus.seats.SeatsToolkit;
import jdplus.tramo.TramoKernel;

/**
 *
 * @author palatej
 */
@lombok.Value
public class TramoSeatsKernel {

    private static PreliminaryChecks of(TramoSeatsSpec spec) {

        TransformSpec transform = spec.getTramo().getTransform();
        return (s, logs) -> {
            TsData sc = s.select(transform.getSpan());
            if (transform.isPreliminaryCheck()) {
                jdplus.sa.PreliminaryChecks.testSeries(sc);
            }
            return sc;
        };
    }

    private PreliminaryChecks preliminary;
    private TramoKernel tramo;
    private SeatsKernel seats;

    public static TramoSeatsKernel of(TramoSeatsSpec spec, ModellingContext context) {
        PreliminaryChecks check = of(spec);
        TramoKernel tramo = TramoKernel.of(spec.getTramo(), context);
        SeatsKernel seats = new SeatsKernel(SeatsToolkit.of(spec.getSeats()));
        return new TramoSeatsKernel(check, tramo, seats);
    }

    public TramoSeatsResults process(TsData s, ProcessingLog log) {
        if (log == null) {
            log = ProcessingLog.dummy();
        }
        // Step 0. Preliminary checks
        TsData sc = preliminary.check(s, log);
        // Step 1. Tramo
        RegSarimaModel preprocessing = tramo.process(sc, log);
        // Step 2. Link between tramo and seats
        SeatsModelSpec smodel = of(preprocessing);
        // Step 3. Seats
        SeatsResults srslts = seats.process(smodel, log);
        // Step 4. Final decomposition
        SeriesDecomposition finals = TwoStepsDecomposition.merge(preprocessing, srslts.getFinalComponents());

        // Step 5. Diagnostics
        return TramoSeatsResults.builder()
                .preprocessing(preprocessing)
                .decomposition(srslts)
                .finals(finals)
                .build();
    }

    private static SeatsModelSpec of(RegSarimaModel model) {
        TsData series = model.interpolatedSeries(false);
        TsData det = model.getDeterministicEffect(series.getDomain());
        TsData yreg = RegArimaDecomposer.deterministicEffect(model, series.getDomain(), ComponentType.Series, false);
        if (model.getDescription().isLogTransformation()) {
            series = TsData.divide(series, TsData.divide(det, yreg));
        } else {
            series = TsData.subtract(series, TsData.subtract(det, yreg));
        }

        SarimaModel arima = model.arima();
        SarimaSpec sarima = SarimaSpec.builder()
                .d(arima.getD())
                .bd(arima.getBd())
                .phi(Parameter.of(arima.getPhi(), ParameterType.Fixed))
                .theta(Parameter.of(arima.getTheta(), ParameterType.Fixed))
                .bphi(Parameter.of(arima.getBphi(), ParameterType.Fixed))
                .btheta(Parameter.of(arima.getBtheta(), ParameterType.Fixed))
                .build();
        LikelihoodStatistics ll = model.getEstimation().getStatistics();
        return SeatsModelSpec.builder()
                .series(series)
                .log(model.getDescription().isLogTransformation())
                .meanCorrection(model.isMeanCorrection())
                .sarimaSpec(sarima)
                .innovationVariance(ll.getSsqErr() / (ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount()))
                .build();
    }

}
