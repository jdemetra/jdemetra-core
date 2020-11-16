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

import demetra.data.DoubleSeq;
import nbbrd.design.Development;
import demetra.likelihood.LikelihoodStatistics;
import demetra.math.functions.ObjectiveFunctionPoint;
import demetra.math.matrices.MatrixType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.Variable;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
@Development(status = Development.Status.Alpha)
public class TemporalDisaggregationResults {

    @lombok.NonNull
    TsData originalSeries;
    
    @lombok.NonNull
    TsDomain disaggregationDomain;
    
    /**
     * Regression variables
     */
    private Variable[] indicators;
    
    
    /**
     * Regression estimation. The order correspond to the order of the variables
     * 
     */
    private DoubleSeq coefficients;
    private MatrixType coefficientsCovariance;
    
    ObjectiveFunctionPoint maximum;
    
    LikelihoodStatistics likelihood;
    ResidualsDiagnostics residualsDiagnostics;

    @lombok.NonNull
    TsData disaggregatedSeries;
    
    @lombok.NonNull
    TsData stdevDisaggregatedSeries;
    
    TsData regressionEffects, residuals;
    
    @lombok.Singular
    private Map<String, Object> addtionalResults;
}
