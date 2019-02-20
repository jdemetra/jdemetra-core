/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.tempdisagg.univariate;

import demetra.benchmarking.spi.ITemporalDisaggregation;
import demetra.design.Development;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.util.ServiceLookup;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class TemporalDisaggregation {


    private final AtomicReference<ITemporalDisaggregation> PROCESSOR = ServiceLookup.firstMutable(ITemporalDisaggregation.class);

    public void setProcessor(ITemporalDisaggregation algorithm) {
        PROCESSOR.set(algorithm);
    }

    public ITemporalDisaggregation getProcessor() {
        return PROCESSOR.get();
    }

    public TemporalDisaggregationResults process(TsData aggregatedSeries, TsData[] indicators, TemporalDisaggregationSpec spec) {
        return PROCESSOR.get().process(aggregatedSeries, indicators, spec);
    }

    public TemporalDisaggregationResults process(TsData aggregatedSeries, TsDomain domain, TemporalDisaggregationSpec spec) {
        return PROCESSOR.get().process(aggregatedSeries, domain, spec);
    }

}
