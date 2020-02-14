/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.seats;

import demetra.arima.SarimaModel;
import demetra.data.DoubleSeq;
import demetra.design.Algorithm;
import demetra.design.Development;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.modelling.LinearModelEstimation;
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
public class SeatsProcessor {

    private final SeatsProcessorLoader.Computer ENGINE = new SeatsProcessorLoader.Computer();
    private final AtomicReference<Computer> LEGACYENGINE=new AtomicReference<Computer>();

    public void setEngine(Computer algorithm) {
        ENGINE.set(algorithm);
    }

    public Computer getEngine() {
        return ENGINE.get();
    }

    public SeatsResults compute(DoubleSeq series, int period, SeatsSpec spec, List<String> addtionalItems) {
        return ENGINE.get().compute(series, period, spec, addtionalItems);
    }

    public void setLegacyEngine(Computer algorithm) {
        LEGACYENGINE.set(algorithm);
    }

    public Computer getLegacyEngine() {
        return LEGACYENGINE.get();
    }

    public SeatsResults computeLegacy(DoubleSeq series, int period, SeatsSpec spec, List<String> addtionalItems) {
        Computer cp = LEGACYENGINE.get();
        if (cp == null)
            throw new SeatsException("No legacy engine");
        return cp.compute(series, period, spec, addtionalItems);
    }
 
    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    public static interface Computer {

        SeatsResults compute(DoubleSeq series, int period, SeatsSpec spec, List<String> addtionalItems);

    }
}
