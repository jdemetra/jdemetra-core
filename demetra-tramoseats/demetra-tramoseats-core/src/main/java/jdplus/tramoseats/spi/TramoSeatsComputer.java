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
package jdplus.tramoseats.spi;

import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.modelling.ModellingContext;
import demetra.tramoseats.TramoSeats;
import demetra.tramoseats.TramoSeatsResults;
import demetra.tramoseats.TramoSeatsSpec;
import java.util.List;
import jdplus.tramoseats.TramoSeatsKernel;
import jdplus.tramoseats.extractors.TramoSeatsExtractor;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(TramoSeats.Processor.class)
public class TramoSeatsComputer implements TramoSeats.Processor {

    @Override
    public TramoSeatsResults process(TsData series, TramoSeatsSpec spec, ModellingContext context, List<String> additionalItems) {
        TramoSeatsKernel tramoseats = TramoSeatsKernel.of(spec, context);
        ProcessingLog log = new ProcessingLog();
        jdplus.tramoseats.TramoSeatsResults rslt = tramoseats.process(series, log);
        // Handling of additional items
        TramoSeatsResults.Builder builder = TramoSeatsResults.builder();
        for (String key : additionalItems) {
            Object data = TramoSeatsExtractor.getMapping().getData(rslt, key, Object.class);
            if (data != null) {
                builder.addtionalResult(key, data);
            }
        }
        return builder
                .preprocessing(jdplus.regarima.ApiUtility.toApi(rslt.getPreprocessing()))
                .decomposition(ApiUtility.toApi(rslt.getDecomposition()))
                .finals(rslt.getFinals())
                .logs(log.all())
                .build();
    }

}
