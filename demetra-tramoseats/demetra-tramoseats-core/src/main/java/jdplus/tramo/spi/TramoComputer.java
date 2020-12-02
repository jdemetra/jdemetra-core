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
package jdplus.tramo.spi;

import demetra.processing.DefaultProcessingLog;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.modelling.RegSarimaResults;
import demetra.tramo.Tramo;
import demetra.tramo.TramoSpec;
import java.util.List;
import jdplus.regarima.ApiUtility;
import jdplus.regsarima.regular.ModelEstimation;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(Tramo.Processor.class)
public class TramoComputer implements Tramo.Processor{

    @Override
    public RegSarimaResults process(TsData series, TramoSpec spec, ModellingContext context, List<String> addtionalItems) {
        jdplus.tramo.TramoKernel processor = jdplus.tramo.TramoKernel.of(spec, context);
        DefaultProcessingLog log=new DefaultProcessingLog();
        ModelEstimation rslt = processor.process(series, log);
        // TODO: fill details
        return RegSarimaResults.builder()
                .regarima(ApiUtility.toApi(rslt))
                .logs(log.all())
//                .addtionalResults()
                .build();
    }
    
}
