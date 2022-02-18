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
package demetra.x13;

import demetra.design.Algorithm;
import demetra.processing.GenericResults;
import demetra.processing.ProcResults;
import nbbrd.design.Development;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
public class X13 {

    private final X13Loader.Processor ENGINE = new X13Loader.Processor();
    private final AtomicReference<Processor> LEGACYENGINE=new AtomicReference<Processor>();

    public void setEngine(Processor algorithm) {
        ENGINE.set(algorithm);
    }

    public Processor getEngine() {
        return ENGINE.get();
    }

    public ProcResults process(TsData series, X13Spec spec, ModellingContext context, List<String> items) {
        return ENGINE.get().process(series, spec, context, items);
    }

    public  Map<String, Class> outputDictionary(boolean compact){
        return ENGINE.get().outputDictionary(compact);
    }

    public void setLegacyEngine(Processor algorithm) {
        LEGACYENGINE.set(algorithm);
    }

    public Processor getLegacyEngine() {
        return LEGACYENGINE.get();
    }

    public ProcResults processLegacy(TsData series, X13Spec spec, ModellingContext context, List<String> items) {
        Processor cp = LEGACYENGINE.get();
        if (cp == null)
            throw new X13Exception("No legacy engine");
        return cp.process(series, spec, context, items);
    }
    
    public final static class DefProcessor implements Processor{

        @Override
        public ProcResults process(TsData series, X13Spec spec, ModellingContext context, List<String> items) {
           return GenericResults.notImplemented();
        }

        @Override
        public Map<String, Class> outputDictionary(boolean compact) {
            return Collections.emptyMap();
        }
        
    }
    

    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT, fallback = DefProcessor.class)
    public static interface Processor {

        public ProcResults process(TsData series, X13Spec spec, ModellingContext context, List<String> items);
        public Map<String, Class> outputDictionary(boolean compact);

    }
}
