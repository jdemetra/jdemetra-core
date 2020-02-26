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

import demetra.arima.SarimaModel;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.modelling.LinearModelEstimation;
import demetra.timeseries.regression.modelling.ModellingContext;
import demetra.tramo.TramoProcessor;
import demetra.tramo.TramoSpec;
import java.util.List;
import jdplus.regarima.ApiUtility;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.regsarima.regular.RegSarimaModelling;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(TramoProcessor.Computer.class)
public class TramoComputer implements TramoProcessor.Computer{

    @Override
    public LinearModelEstimation<SarimaModel> compute(TsData series, TramoSpec spec, ModellingContext context, List<String> addtionalItems) {
        jdplus.tramo.TramoProcessor processor = jdplus.tramo.TramoProcessor.of(spec, context);
        ModelEstimation estimation = processor.process(series);
        return ApiUtility.toApi(estimation);
    }
    
}
