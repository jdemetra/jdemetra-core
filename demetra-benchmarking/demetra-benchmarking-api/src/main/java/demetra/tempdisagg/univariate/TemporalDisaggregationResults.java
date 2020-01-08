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

import demetra.benchmarking.descriptors.ResidualsDiagnosticsDescriptor;
import demetra.information.InformationMapping;
import demetra.likelihood.LikelihoodStatistics;
import demetra.likelihood.MaximumLogLikelihood;
import demetra.linearmodel.Coefficient;
import demetra.linearmodel.LinearModelEstimation;
import demetra.processing.ProcResults;
import demetra.timeseries.TsData;
import java.util.LinkedHashMap;
import java.util.Map;
import demetra.math.matrices.MatrixType;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class TemporalDisaggregationResults {

    private @lombok.NonNull
    TsData disaggregatedSeries;
    private @lombok.NonNull
    TsData stdevDisaggregatedSeries;
    private TsData regressionEffects, residuals;
    private MaximumLogLikelihood maximum;
    private LikelihoodStatistics likelihood;
    private LinearModelEstimation estimation;
    private ResidualsDiagnostics residualsDiagnostics;
}
