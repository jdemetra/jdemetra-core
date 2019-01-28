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
import demetra.benchmarking.spi.DentonProcessor;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TsDenton {

    private final AtomicReference<DentonProcessor> IMPL = ServiceLookup.firstMutable(DentonProcessor.class);

    public void setImplementation(DentonProcessor algorithm) {
        IMPL.set(algorithm);
    }

    public DentonProcessor getImplementation() {
        return IMPL.get();
    }
    
    public TsData benchmark(TsData highFreqSeries, TsData aggregationConstraint, DentonSpecification spec){
        return IMPL.get().benchmark(highFreqSeries, aggregationConstraint, spec);
    }

    public TsData benchmark(TsUnit highFreq, TsData aggregationConstraint, DentonSpecification spec){
        return IMPL.get().benchmark(highFreq, aggregationConstraint, spec);
    }
}

