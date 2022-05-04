/*
 * Copyright 2022 National Bank of Belgium
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
package demetra.highfreq;

import demetra.design.Algorithm;
import demetra.processing.ProcResults;
import nbbrd.design.Development;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 * Low-level algorithms. Should be refined
 * @author palatej
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class ExtendedAirlineAlgorithms {

    private final ExtendedAirlineAlgorithmsLoader.Processor PROCESSOR = new ExtendedAirlineAlgorithmsLoader.Processor();

    public void setProcessor(Processor algorithm) {
        PROCESSOR.set(algorithm);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    /**
     *
     * @param s Time series being decomposed
     * @param airline
     * @param var True if the covariance matrices of the components are computed
     *
     * @return
     */
    public ProcResults process(double[] s, ExtendedAirline airline, boolean var) {
        return PROCESSOR.get().process(s, airline, var);
    }

    public ProcResults process(ExtendedAirlineSpec spec) {
        return PROCESSOR.get().process(spec);
    }

    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    public interface Processor {

        ProcResults process(double[] s, ExtendedAirline airline, boolean var);

        ProcResults process(ExtendedAirlineSpec spec);
    }

}
