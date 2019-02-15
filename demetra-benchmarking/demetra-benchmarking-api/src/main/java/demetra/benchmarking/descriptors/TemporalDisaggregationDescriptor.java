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
package demetra.benchmarking.descriptors;

import demetra.descriptors.stats.DiffuseConcentratedLikelihoodDescriptor;
import demetra.design.Development;
import demetra.information.InformationMapping;
import demetra.tempdisagg.univariate.TemporalDisaggregationResults;
import demetra.timeseries.TsData;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class TemporalDisaggregationDescriptor {
    
    public final String LIKELIHOOD="likelihood", DISAGG="disagg", EDISAGG="edisagg",
            RES="residuals", FRES="fullresiduals", WNRES="wnresiduals";
    
    final InformationMapping<TemporalDisaggregationResults> MAPPING = new InformationMapping<>(TemporalDisaggregationResults.class);

    static {
        MAPPING.delegate(LIKELIHOOD, DiffuseConcentratedLikelihoodDescriptor.getMapping(), source->source.getConcentratedLikelihood());
        MAPPING.set(DISAGG, TsData.class, source->source.getDisaggregatedSeries());
        MAPPING.set(EDISAGG, TsData.class, source->source.getStdevDisaggregatedSeries());
    }

    public InformationMapping<TemporalDisaggregationResults> getMapping() {
        return MAPPING;
    }
    
}
