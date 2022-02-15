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

import demetra.information.InformationExtractors;
import demetra.processing.DefaultProcessingLog;
import demetra.processing.GenericResults;
import demetra.processing.ProcResults;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import demetra.tramoseats.TramoSeats;
import demetra.tramoseats.TramoSeatsSpec;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jdplus.tramoseats.TramoSeatsKernel;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(TramoSeats.Processor.class)
public class TramoSeatsProcessor implements TramoSeats.Processor {

    @Override
    public ProcResults process(TsData series, TramoSeatsSpec spec, ModellingContext context, List<String> items) {
        TramoSeatsKernel tramoseats = TramoSeatsKernel.of(spec, context);
        DefaultProcessingLog log = new DefaultProcessingLog();
        jdplus.tramoseats.TramoSeatsResults rslt = tramoseats.process(series, log);
        return GenericResults.of(rslt, items, log);
    }

    @Override
    public Map<String, Class> outputDictionary(boolean compact) {
        Map<String, Class> dic = new LinkedHashMap<>();
        InformationExtractors.fillDictionary(jdplus.tramoseats.TramoSeatsResults.class, null, dic, compact);
        return dic;
    }

}
