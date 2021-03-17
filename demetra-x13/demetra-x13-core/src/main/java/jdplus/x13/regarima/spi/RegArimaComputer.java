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
package jdplus.x13.regarima.spi;

import demetra.arima.SarimaModel;
import demetra.processing.DefaultProcessingLog;
import demetra.regarima.RegArima;
import demetra.regarima.RegArimaSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import demetra.timeseries.regression.modelling.LightLinearModel;
import java.util.List;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.x13.regarima.RegArimaKernel;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(RegArima.Processor.class)
public class RegArimaComputer implements RegArima.Processor{

    @Override
    public GeneralLinearModel<SarimaModel>  process(TsData series, RegArimaSpec spec, ModellingContext context, List<String> addtionalItems) {
        RegArimaKernel processor = RegArimaKernel.of(spec, context);
        DefaultProcessingLog log=new DefaultProcessingLog();
        ModelEstimation rslt = processor.process(series, log);
        // TODO: fill details
        return LightLinearModel.<SarimaModel>builder()
//                .regarima(ApiUtility.toApi(rslt))
//                .logs(log.all())
//                .addtionalResults()
                .build();
    }
    
}
