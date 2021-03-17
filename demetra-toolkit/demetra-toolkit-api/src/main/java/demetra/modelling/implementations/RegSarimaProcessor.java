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
package demetra.modelling.implementations;

import demetra.arima.SarimaModel;
import demetra.design.Algorithm;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import nbbrd.design.Development;
import java.util.List;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 * TODO
 * @author palatej
 */
@Development(status = Development.Status.Temporary)
@lombok.experimental.UtilityClass
public class RegSarimaProcessor {

    private final RegSarimaProcessorLoader.Computer ENGINE = new RegSarimaProcessorLoader.Computer();

    public void setEngine(Computer algorithm) {
        ENGINE.set(algorithm);
    }

    public Computer getEngine() {
        return ENGINE.get();
    }

    /**
     *
     */
    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT, fallback = NoComputer.class)
    public static interface Computer {
        
    }
    
    public static class NoComputer implements Computer{
        
         
    }

}
