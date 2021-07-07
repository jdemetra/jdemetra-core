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
package demetra.sa;

import demetra.processing.DefaultProcessingLog;
import demetra.processing.ProcDiagnostic;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import java.util.ArrayList;
import java.util.List;
import demetra.information.Explorable;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SaManager {
    
    public List<SaProcessingFactory> processors(){
        return SaProcessingFactoryLoader.get();
    }
    
    public List<SaOutputFactory> outputFactories(){
        return SaOutputFactoryLoader.get();
    }

    public Explorable process(TsData series, SaSpecification spec, ModellingContext context, ProcessingLog log) {
        List<SaProcessingFactory> all = SaProcessingFactoryLoader.get();
        for (SaProcessingFactory fac : all) {
            SaSpecification dspec = fac.decode(spec);
            if (dspec != null) {
                return fac.processor(dspec).process(series, context, log);
            }
        }
        return null;
    }

    public SaEstimation process(SaDefinition def, ModellingContext context, boolean verbose) {
        List<SaProcessingFactory> all = SaProcessingFactoryLoader.get();
        SaSpecification spec = def.activeSpecification();
        for (SaProcessingFactory fac : all) {
            SaSpecification dspec = fac.decode(spec);
            if (dspec != null) {
                ProcessingLog log = new DefaultProcessingLog();
                SaProcessor processor = fac.processor(dspec);
                Explorable rslt = processor.process(def.getTs().getData(), context, log);
                List<ProcDiagnostic> tests = new ArrayList<>();
                fac.fillDiagnostics(tests, rslt);
                return SaEstimation.builder()
                        .results(rslt)
                        .log(verbose ? log : ProcessingLog.dummy())
                        .diagnostics(tests)
                        .build();
            }
        }
        return null;
    }

    public <I extends SaSpecification> SaProcessingFactory factoryFor(SaSpecification spec) {
        List<SaProcessingFactory> all = SaProcessingFactoryLoader.get();
        return all.stream().filter(p -> p.canHandle(spec)).findFirst().get();
    }
    
}
