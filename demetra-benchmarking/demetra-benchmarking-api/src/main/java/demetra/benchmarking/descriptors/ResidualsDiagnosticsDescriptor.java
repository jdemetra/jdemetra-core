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

import demetra.design.Development;
import demetra.information.InformationMapping;
import demetra.stats.TestResult;
import demetra.tempdisagg.univariate.ResidualsDiagnostics;
import demetra.timeseries.TsData;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class ResidualsDiagnosticsDescriptor {
    
    public final String FRES="fullresiduals", MEAN="mean", SKEWNESS="skewness", 
            KURTOSIS="kurtosis", DH="doornikhansen", LJUNGBOX="ljungbox",
            DW="durbinwatson",  UDRUNS_NUMBER = "nudruns", UDRUNS_LENGTH = "ludruns",
            RUNS_NUMBER = "nruns", RUNS_LENGTH = "lruns";

    
    private final InformationMapping<ResidualsDiagnostics> MAPPING = new InformationMapping<>(ResidualsDiagnostics.class);

    static {
        MAPPING.set(FRES, TsData.class, source->source.getFullResiduals());
        MAPPING.set(MEAN, TestResult.class, source->source.getMean());
        MAPPING.set(SKEWNESS, TestResult.class, source->source.getSkewness());
        MAPPING.set(KURTOSIS, TestResult.class, source->source.getKurtosis());
        MAPPING.set(DH, TestResult.class, source->source.getDoornikHansen());
        MAPPING.set(LJUNGBOX, TestResult.class, source->source.getLjungBox());
        MAPPING.set(RUNS_NUMBER, TestResult.class, source->source.getRunsNumber());
        MAPPING.set(RUNS_LENGTH, TestResult.class, source->source.getRunsLength());
        MAPPING.set(UDRUNS_NUMBER, TestResult.class, source->source.getUdRunsNumber());
        MAPPING.set(UDRUNS_LENGTH, TestResult.class, source->source.getUdRunsLength());
        MAPPING.set(DW, Double.class, source->source.getDurbinWatson());
    }

    public InformationMapping<ResidualsDiagnostics> getMapping() {
        return MAPPING;
    }
    
}
