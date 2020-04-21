/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats;

import demetra.arima.SarimaSpec;
import demetra.data.Parameter;
import demetra.data.ParameterType;
import demetra.processing.ProcessingLog;
import demetra.sa.ComponentType;
import demetra.sa.SeriesDecomposition;
import demetra.seats.SeatsModelSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.modelling.ModellingContext;
import demetra.tramo.TransformSpec;
import demetra.tramoseats.TramoSeatsSpec;
import java.util.Arrays;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.sa.RegArimaDecomposer;
import jdplus.sa.SaVariablesMapping;
import jdplus.sa.TwoStepsDecomposition;
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
    private SaVariablesMapping samapping;
    private SeatsKernel seats;

    public static TramoSeatsKernel of(TramoSeatsSpec spec, ModellingContext context) {
        PreliminaryChecks check = of(spec);
        TramoKernel tramo = TramoKernel.of(spec.getTramo(), context);
        SaVariablesMapping mapping = new SaVariablesMapping();
        // TO DO: fill maping with existing information in TramoSpec (section Regression)
        SeatsKernel seats = new SeatsKernel(SeatsToolkit.of(spec.getSeats()));
        return new TramoSeatsKernel(check, tramo, mapping, seats);
    }

    public TramoSeatsResults process(TsData s, ProcessingLog log) {
        // Step 0. Preliminary checks
        TsData sc = preliminary.check(s, log);
        // Step 1. Tramo
        ModelEstimation preprocessing = tramo.process(sc, log);
        // Step 2. Link between tramo and seats
        SaVariablesMapping nmapping = new SaVariablesMapping();
        nmapping.addDefault(Arrays
                .stream(preprocessing.getVariables())
                .map(var -> var.getVariable())
                .toArray(q -> new ITsVariable[q]));
        nmapping.put(samapping);
        RegArimaDecomposer decomposer = RegArimaDecomposer.of(preprocessing, nmapping);
        SeatsModelSpec smodel = of(decomposer);
        // Step 3. Seats
        SeatsResults srslts = seats.process(smodel, log);
        // Step 4. Final decomposition
        SeriesDecomposition finals = TwoStepsDecomposition.merge(decomposer, srslts.getFinalComponents());
        // Step 5. Diagnostics
        return new TramoSeatsResults(preprocessing, srslts, finals);
    }

    private static SeatsModelSpec of(RegArimaDecomposer decomposer) {
        ModelEstimation model = decomposer.getModel();
        TsData series = model.interpolatedSeries(false);
        TsData det = model.getDeterministicEffect(series.getDomain());
        TsData yreg = decomposer.deterministicEffect(series.getDomain(), ComponentType.Series, false);
        if (model.isLogTransformation()) {
            series = TsData.divide(series, TsData.divide(det, yreg));
        } else {
            series = TsData.subtract(series, TsData.subtract(det, yreg));
        }

        SarimaModel arima = model.getModel().arima();
        SarimaSpec sarima = SarimaSpec.builder()
                .d(arima.getRegularDifferenceOrder())
                .bd(arima.getSeasonalDifferenceOrder())
                .phi(Parameter.of(arima.phi(), ParameterType.Estimated))
                .theta(Parameter.of(arima.theta(), ParameterType.Estimated))
                .bphi(Parameter.of(arima.bphi(), ParameterType.Estimated))
                .btheta(Parameter.of(arima.btheta(), ParameterType.Estimated))
                .build();
        
        return SeatsModelSpec.builder()
                .series(series)
                .log(model.isLogTransformation())
                .meanCorrection(model.getModel().isMean())
                .sarimaSpec(sarima)
                .build();
    }

}
