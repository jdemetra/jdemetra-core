/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.benchmarking.univariate;

import demetra.timeseries.TsUnit;
import demetra.timeseries.TsData;
import demetra.util.ServiceLookup;
import java.util.concurrent.atomic.AtomicReference;
import demetra.design.Algorithm;
import demetra.design.Development;
import demetra.design.ServiceDefinition;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class Denton {

    @Algorithm
    @ServiceDefinition
    public static interface Processor {

        TsData benchmark(TsData highFreqSeries, TsData aggregationConstraint, DentonSpec spec);

        TsData benchmark(TsUnit highFreq, TsData aggregationConstraint, DentonSpec spec);
    }

    private final AtomicReference<Processor> PROCESSOR = ServiceLookup.firstMutable(Processor.class);

    public void setProcessor(Processor algorithm) {
        PROCESSOR.set(algorithm);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public TsData benchmark(TsData highFreqSeries, TsData aggregationConstraint, DentonSpec spec) {
        return PROCESSOR.get().benchmark(highFreqSeries, aggregationConstraint, spec);
    }

    public TsData benchmark(TsUnit highFreq, TsData aggregationConstraint, DentonSpec spec) {
        return PROCESSOR.get().benchmark(highFreq, aggregationConstraint, spec);
    }
}
