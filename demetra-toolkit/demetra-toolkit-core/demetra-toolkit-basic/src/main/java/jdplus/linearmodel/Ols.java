/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.linearmodel;

import demetra.design.Algorithm;
import nbbrd.design.Development;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class Ols {

    private final OlsLoader.Processor PROCESSOR = new OlsLoader.Processor();

    public void setProcessor(Processor algorithm) {
        PROCESSOR.set(algorithm);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public LeastSquaresResults compute(LinearModel model) {
        return PROCESSOR.get().compute(model);
    }

    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, 
            mutability = Mutability.CONCURRENT,
            fallback = OlsComputer.class)
    @FunctionalInterface
    public static interface Processor {
        LeastSquaresResults compute(LinearModel model);
    }

}
