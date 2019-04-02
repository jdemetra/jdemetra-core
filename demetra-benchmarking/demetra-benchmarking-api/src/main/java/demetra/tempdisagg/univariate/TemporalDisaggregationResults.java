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
import demetra.descriptors.stats.LikelihoodStatisticsDescriptor;
import demetra.descriptors.stats.MaximumLikelihoodDescriptor;
import demetra.information.InformationMapping;
import demetra.likelihood.LikelihoodStatistics;
import demetra.likelihood.MaximumLogLikelihood;
import demetra.linearmodel.Coefficient;
import demetra.linearmodel.LinearModelEstimation;
import demetra.maths.MatrixType;
import demetra.processing.ProcResults;
import demetra.timeseries.TsData;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class TemporalDisaggregationResults implements ProcResults {

    private @lombok.NonNull
    TsData disaggregatedSeries;
    private @lombok.NonNull
    TsData stdevDisaggregatedSeries;
    private TsData regressionEffects, residuals;
    private MaximumLogLikelihood maximum;
    private LikelihoodStatistics likelihood;
    private LinearModelEstimation estimation;
    private ResidualsDiagnostics residualsDiagnostics;

    @Override
    public boolean contains(String id) {
        return MAPPING.contains(id);
    }

    @Override
    public Map<String, Class> getDictionary() {
        Map<String, Class> dic = new LinkedHashMap<>();
        MAPPING.fillDictionary(null, dic, true);
        return dic;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return MAPPING.getData(this, id, tclass);
    }

    public static final String LIKELIHOOD = "likelihood", DISAGG = "disagg", EDISAGG = "edisagg",
            RES = "residuals", ML = "ml", COEFF="coeff", NX="nx", COVAR="covar", C="c", REGEFFECT = "regeffect";

    private static final InformationMapping<TemporalDisaggregationResults> MAPPING = new InformationMapping<>(TemporalDisaggregationResults.class);

    static {
        MAPPING.set(DISAGG, TsData.class, source -> source.getDisaggregatedSeries());
        MAPPING.set(EDISAGG, TsData.class, source -> source.getStdevDisaggregatedSeries());
        MAPPING.set(REGEFFECT, TsData.class, source -> source.getRegressionEffects());
        MAPPING.delegate(ML, MaximumLikelihoodDescriptor.getMapping(), source -> source.getMaximum());
        MAPPING.setArray(COEFF, 1, 9, Coefficient.class, (TemporalDisaggregationResults source, Integer i)->
        {
            LinearModelEstimation estimation = source.getEstimation();
            Coefficient[] coefficients = estimation.getCoefficients();
            if (i>=1 && i<=coefficients.length){
                return coefficients[i-1];
            }
            return null;
        });
        MAPPING.set(NX, Integer.class, source->source.estimation.nx());
        MAPPING.set(C, double[].class, source->source.getEstimation().values().toArray());
        MAPPING.set(COVAR, MatrixType.class, source->source.getEstimation().getCovariance());
        MAPPING.delegate(LIKELIHOOD, LikelihoodStatisticsDescriptor.getMapping(), source -> source.getLikelihood());
        MAPPING.delegate(RES, ResidualsDiagnosticsDescriptor.getMapping(), source -> source.getResidualsDiagnostics());
    }

    public static InformationMapping<TemporalDisaggregationResults> getMapping() {
        return MAPPING;
    }

}
