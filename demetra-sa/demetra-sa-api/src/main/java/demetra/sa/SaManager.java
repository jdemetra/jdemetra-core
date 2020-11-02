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

import demetra.information.InformationSet;
import demetra.processing.ProcDiagnostic;
import demetra.processing.ProcResults;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SaManager {
  
    public ProcResults process(TsData series, SaSpecification spec, ModellingContext context, ProcessingLog log){
        List<SaProcessingFactory> all = SaProcessingFactoryLoader.get();
        for (SaProcessingFactory fac : all){
            SaSpecification dspec=fac.decode(spec);
            if (dspec != null){
                return fac.processor(dspec).process(series, context, log);
            }
        }
        return null;
    }
    
    public SaEstimation process(SaDefinition def, ModellingContext context, boolean verbose){
        List<SaProcessingFactory> all = SaProcessingFactoryLoader.get();
        SaSpecification spec=def.activeSpecification();
        for (SaProcessingFactory fac : all){
            SaSpecification dspec=fac.decode(spec);
            if (dspec != null){
                ProcessingLog log= new ProcessingLog();
                SaProcessor processor = fac.processor(dspec);
                ProcResults rslt = processor.process(def.getTs().getData(), context, log);
                InformationSet diagnostics = fac.diagnosticsOf(rslt);
                return SaEstimation.builder()
                        .results(rslt)
                        .log(verbose ? log : null)
                        .diagnostics(diagnostics)
                        .quality(ProcDiagnostic.summary(diagnostics))
                        .build();
            }
        }
        return null;
    }
    
    public <I extends SaSpecification> SaProcessingFactory factoryFor(SaSpecification spec){
        List<SaProcessingFactory> all = SaProcessingFactoryLoader.get();
        return all.stream().filter(p->p.canHandle(spec)).findFirst().get();
     }
}
