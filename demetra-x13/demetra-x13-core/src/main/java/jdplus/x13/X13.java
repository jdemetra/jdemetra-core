/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.x13;

import demetra.regarima.BasicSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.modelling.ModellingContext;
import demetra.x11.X11Kernel;
import demetra.x13.X13Spec;
import demetra.x13.regarima.RegArimaKernel;
import jdplus.sa.SaVariablesMapping;

/**
 *
 * @author palatej
 */
@lombok.Value
public class X13 {

    private static PreliminaryChecks of(X13Spec spec) {
        BasicSpec basic = spec.getRegArima().getBasic();
        return (s, logs) -> {
            TsData sc = s.select(basic.getSpan());
            if (basic.isPreliminaryCheck()) {
                jdplus.sa.diagnostics.PreliminaryChecks.testSeries(sc);
            }
            return sc;
        };
    }

    private PreliminaryChecks preliminary;
    private RegArimaKernel preprocessing;
    private SaVariablesMapping samapping;
    private X11Kernel x11;

    public static X13 of(X13Spec spec, ModellingContext context) {
        PreliminaryChecks check = of(spec);
        RegArimaKernel regarima = RegArimaKernel.of(spec.getRegArima(), context);
        SaVariablesMapping mapping = new SaVariablesMapping();
        X11Kernel x11=new X11Kernel();
        
        // TO DO: fill maping with existing information in TramoSpec (section Regression)
        return new X13(check, regarima, mapping, x11);
    }

//    public X13Results compute(TsData s, ProcessingLog log) {
//        // Step 0. Preliminary checks
//        TsData sc = preliminary.check(s, log);
//        // Step 1. Tramo
//        ModelEstimation preprocessing = tramo.process(sc, log);
//        // Step 2. Link between tramo and seats
//        SaVariablesMapping nmapping = new SaVariablesMapping();
//        nmapping.addDefault(Arrays
//                .stream(preprocessing.getVariables())
//                .map(var -> var.getVariable())
//                .toArray(q -> new ITsVariable[q]));
//        nmapping.put(samapping);
//        TsData yc = preprocessing.interpolatedSeries(false);
//
//        RegArimaDecomposer decomposer = RegArimaDecomposer.of(preprocessing, nmapping);
//        SeatsModelSpec smodel = of(decomposer);
//        // Step 3. Seats
//        SeatsResults srslts = seats.process(smodel, log);
//        // Step 4. Final decomposition
//        SeriesDecomposition finals = TwoStepsDecomposition.merge(decomposer, srslts.getFinalComponents());
//        // Step 5. Diagnostics
//        return new X13Results(preprocessing, srslts, finals);
//    }
//
//    private static SeatsModelSpec of(RegArimaDecomposer decomposer) {
//        ModelEstimation model = decomposer.getModel();
//        TsData series = model.interpolatedSeries(false);
//        TsData det = model.getDeterministicEffect(series.getDomain());
//        TsData yreg = decomposer.deterministicEffect(series.getDomain(), ComponentType.Series, false);
//        if (model.isLogTransformation()) {
//            series = TsData.divide(series, TsData.divide(det, yreg));
//        } else {
//            series = TsData.subtract(series, TsData.subtract(det, yreg));
//        }
//
//        SarimaModel arima = model.getModel().arima();
//        SarimaSpec sarima = SarimaSpec.builder()
//                .d(arima.getRegularDifferenceOrder())
//                .bd(arima.getSeasonalDifferenceOrder())
//                .phi(ParameterSpec.of(arima.phi(), ParameterType.Estimated))
//                .theta(ParameterSpec.of(arima.theta(), ParameterType.Estimated))
//                .bphi(ParameterSpec.of(arima.bphi(), ParameterType.Estimated))
//                .btheta(ParameterSpec.of(arima.btheta(), ParameterType.Estimated))
//                .build();
//        
//        return SeatsModelSpec.builder()
//                .series(series.getValues())
//                .log(model.isLogTransformation())
//                .meanCorrection(model.getModel().isMean())
//                .period(series.getAnnualFrequency())
//                .sarimaSpec(sarima)
//                .build();
//    }
//
}
