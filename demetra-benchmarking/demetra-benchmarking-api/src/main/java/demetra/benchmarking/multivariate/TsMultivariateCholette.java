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
package demetra.benchmarking.multivariate;

import demetra.benchmarking.univariate.*;
import demetra.design.Algorithm;
import demetra.timeseries.TsData;
import demetra.util.ServiceLookup;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import demetra.benchmarking.spi.CholetteProcessor;
import demetra.benchmarking.spi.MultivariateCholetteProcessor;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TsMultivariateCholette {

    private final AtomicReference<MultivariateCholetteProcessor> IMPL = ServiceLookup.firstMutable(MultivariateCholetteProcessor.class);

    public void setImplementation(MultivariateCholetteProcessor algorithm) {
        IMPL.set(algorithm);
    }

    public MultivariateCholetteProcessor getImplementation() {
        return IMPL.get();
    }
    
    public Map<String, TsData> benchmark(Map<String, TsData> input, MultivariateCholetteSpecification spec){
        return IMPL.get().benchmark(input, spec);
    }

}

