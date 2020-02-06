/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramo;

import demetra.arima.SarimaModel;
import demetra.design.Algorithm;
import demetra.design.Development;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.modelling.LinearModelEstimation;
import demetra.timeseries.regression.modelling.ModellingContext;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class TramoProcessor {

    private final TramoProcessorLoader.Computer ENGINE = new TramoProcessorLoader.Computer();
    private final AtomicReference<Computer> LEGACYENGINE=new AtomicReference<Computer>();

    public void setEngine(Computer algorithm) {
        ENGINE.set(algorithm);
    }

    public Computer getEngine() {
        return ENGINE.get();
    }

    public LinearModelEstimation<SarimaModel> compute(TsData series, TramoSpec spec, ModellingContext context, List<String> addtionalItems) {
        return ENGINE.get().compute(series, spec, context, addtionalItems);
    }

    public void setLegacyEngine(Computer algorithm) {
        LEGACYENGINE.set(algorithm);
    }

    public Computer getLegacyEngine() {
        return LEGACYENGINE.get();
    }

    public LinearModelEstimation<SarimaModel> computeLegacy(TsData series, TramoSpec spec, ModellingContext context, List<String> addtionalItems) {
        Computer cp = LEGACYENGINE.get();
        if (cp == null)
            throw new TramoException("No legacy engine");
        return cp.compute(series, spec, context, addtionalItems);
    }

    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    @FunctionalInterface
    public static interface Computer {

        public LinearModelEstimation<SarimaModel> compute(TsData series, TramoSpec spec, ModellingContext context, List<String> addtionalItems);

    }
}
