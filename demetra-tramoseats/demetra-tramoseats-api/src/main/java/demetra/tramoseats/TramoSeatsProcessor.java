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
package demetra.tramoseats;

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
public class TramoSeatsProcessor {

    private final TramoSeatsProcessorLoader.Computer ENGINE = new TramoSeatsProcessorLoader.Computer();
    private final AtomicReference<Computer> LEGACYENGINE=new AtomicReference<Computer>();

    public void setEngine(Computer algorithm) {
        ENGINE.set(algorithm);
    }

    public Computer getEngine() {
        return ENGINE.get();
    }

    public TramoSeatsResults compute(TsData series, TramoSeatsSpec spec, ModellingContext context, List<String> addtionalItems) {
        return ENGINE.get().compute(series, spec, context, addtionalItems);
    }

    public void setLegacyEngine(Computer algorithm) {
        LEGACYENGINE.set(algorithm);
    }

    public Computer getLegacyEngine() {
        return LEGACYENGINE.get();
    }

    public TramoSeatsResults computeLegacy(TsData series, TramoSeatsSpec spec, ModellingContext context, List<String> addtionalItems) {
        Computer cp = LEGACYENGINE.get();
        if (cp == null)
            throw new TramoSeatsException("No legacy engine");
        return cp.compute(series, spec, context, addtionalItems);
    }

    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    @FunctionalInterface
    public static interface Computer {

        public TramoSeatsResults compute(TsData series, TramoSeatsSpec spec, ModellingContext context, List<String> addtionalItems);

    }
}
