/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.seats;

import demetra.design.Algorithm;
import demetra.design.Development;
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

    public SeatsResults compute(SeatsSpec spec, List<String> addtionalItems) {
        return ENGINE.get().compute(spec, addtionalItems);
    }

    public void setLegacyEngine(Computer algorithm) {
        LEGACYENGINE.set(algorithm);
    }

    public Computer getLegacyEngine() {
        return LEGACYENGINE.get();
    }

    public SeatsResults computeLegacy(SeatsSpec spec, List<String> addtionalItems) {
        Computer cp = LEGACYENGINE.get();
        if (cp == null)
            throw new SeatsException("No legacy engine");
        return cp.compute(spec, addtionalItems);
    }
 
    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    public static interface Computer {

        SeatsResults compute(SeatsSpec spec, List<String> addtionalItems);

    }
}
