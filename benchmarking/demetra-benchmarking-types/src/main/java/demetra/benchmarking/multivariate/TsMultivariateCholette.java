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
import demetra.benchmarking.spi.CholetteAlgorithm;
import demetra.benchmarking.spi.MultivariateCholetteAlgorithm;
import demetra.design.Algorithm;
import demetra.timeseries.simplets.TsData;
import demetra.utilities.ServiceLookup;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TsMultivariateCholette {

    private final AtomicReference<MultivariateCholetteAlgorithm> IMPL = ServiceLookup.firstMutable(MultivariateCholetteAlgorithm.class);

    public void setImplementation(MultivariateCholetteAlgorithm algorithm) {
        IMPL.set(algorithm);
    }

    public MultivariateCholetteAlgorithm getImplementation() {
        return IMPL.get();
    }
    
    public Map<String, TsData> benchmark(Map<String, TsData> input, MultivariateCholetteSpecification spec){
        return IMPL.get().benchmark(input, spec);
    }

}

