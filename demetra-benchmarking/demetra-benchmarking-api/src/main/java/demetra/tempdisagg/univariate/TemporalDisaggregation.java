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

import demetra.design.Algorithm;
import demetra.processing.ProcResults;
import nbbrd.design.Development;
import nbbrd.service.ServiceDefinition;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import java.util.List;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class TemporalDisaggregation {

    private final TemporalDisaggregationLoader.Processor PROCESSOR = new TemporalDisaggregationLoader.Processor();

    public void setProcessor(Processor algorithm) {
        PROCESSOR.set(algorithm);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public ProcResults process(TsData aggregatedSeries, TsData[] indicators, TemporalDisaggregationSpec spec, List<String> items) {
        return PROCESSOR.get().process(aggregatedSeries, indicators, spec, items);
    }

    public ProcResults process(TsData aggregatedSeries, TsDomain domain, TemporalDisaggregationSpec spec, List<String> items) {
        return PROCESSOR.get().process(aggregatedSeries, domain, spec, items);
    }

    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    public interface Processor {

        ProcResults process(TsData aggregatedSeries, TsData[] indicators, TemporalDisaggregationSpec spec, List<String> items);

        ProcResults process(TsData aggregatedSeries, TsDomain domain, TemporalDisaggregationSpec spec, List<String> items);
    }
}
