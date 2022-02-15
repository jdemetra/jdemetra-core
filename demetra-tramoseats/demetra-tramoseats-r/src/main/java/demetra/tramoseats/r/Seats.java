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
package demetra.tramoseats.r;

import demetra.arima.SarimaSpec;
import demetra.processing.DefaultProcessingLog;
import demetra.processing.ProcessingLog;
import demetra.seats.DecompositionSpec;
import demetra.seats.SeatsModelSpec;
import demetra.timeseries.TsData;
import jdplus.sarima.SarimaModel;
import jdplus.seats.DefaultModelDecomposer;
import jdplus.seats.SeatsKernel;
import jdplus.seats.SeatsResults;
import jdplus.seats.SeatsToolkit;
import jdplus.ucarima.UcarimaModel;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Seats {
    
    private final String[] CMPS={"trend", "seasonal", "transitory", "irregular"};
    
    public UcarimaModel decompose(SarimaModel arima, double eps, double rmod, double smod, double smodpi, String approximation){
        DecompositionSpec spec=DecompositionSpec.builder()
                .approximationMode(DecompositionSpec.ModelApproximationMode.valueOf(approximation))
                .seasTolerance(eps)
                .trendBoundary(rmod)
                .seasBoundary(smod)
                .seasBoundaryAtPi(smodpi)
                .build();
        return new DefaultModelDecomposer(spec).decompose(arima);
    }

    public SeatsResults process(TsData data, boolean log, int[] order, int[] seasonal, boolean mean, int nb, int nf) {

        SarimaSpec arima = SarimaSpec.builder()
                .p(order[0])
                .d(order[1])
                .q(order[2])
                .bp(seasonal == null ? 0 : seasonal[0])
                .bd(seasonal == null ? 0 : seasonal[1])
                .bq(seasonal == null ? 0 : seasonal[2])
                .build();

        SeatsModelSpec model = SeatsModelSpec.builder()
                .series(data)
                .sarimaSpec(arima)
                .log(log)
                .meanCorrection(mean)
                .build();

        DecompositionSpec dspec = DecompositionSpec.builder()
                .forecastCount(nf)
                .backcastCount(nb)
                .build();

        SeatsToolkit toolkit = SeatsToolkit.of(dspec);
        SeatsKernel kernel = new SeatsKernel(toolkit);
        ProcessingLog plog = new DefaultProcessingLog();
        return kernel.process(model, plog);
    }

}
