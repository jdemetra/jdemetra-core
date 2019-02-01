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
package demetra.benchmarking.univariate;

import demetra.design.Algorithm;
import demetra.timeseries.TsData;
import demetra.util.ServiceLookup;
import java.util.concurrent.atomic.AtomicReference;
import demetra.design.Development;
import demetra.design.ServiceDefinition;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class Cholette {

    @Algorithm
    @ServiceDefinition
    @FunctionalInterface
    public static interface Processor {

        TsData benchmark(TsData source, TsData target, CholetteSpec spec);
    }

    private final AtomicReference<Processor> PROCESSOR = ServiceLookup.firstMutable(Processor.class);

    public void setProcessor(Processor algorithm) {
        PROCESSOR.set(algorithm);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public TsData benchmark(TsData source, TsData target, CholetteSpec spec) {
        return PROCESSOR.get().benchmark(source, target, spec);
    }

}
