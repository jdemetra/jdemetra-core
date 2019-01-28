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

import demetra.design.Algorithm;
import demetra.timeseries.TsData;
import demetra.util.ServiceLookup;
import java.util.concurrent.atomic.AtomicReference;
import demetra.benchmarking.spi.CholetteProcessor;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TsCholette {

    private final AtomicReference<CholetteProcessor> IMPL = ServiceLookup.firstMutable(CholetteProcessor.class);

    public void setImplementation(CholetteProcessor algorithm) {
        IMPL.set(algorithm);
    }

    public CholetteProcessor getImplementation() {
        return IMPL.get();
    }
    
    public TsData benchmark(TsData source, TsData target, CholetteSpecification spec){
        return IMPL.get().benchmark(source, target, spec);
    }

}

