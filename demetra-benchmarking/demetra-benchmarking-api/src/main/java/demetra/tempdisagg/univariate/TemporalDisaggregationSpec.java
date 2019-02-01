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

import demetra.processing.AlgorithmDescriptor;
import demetra.data.AggregationType;
import demetra.data.Parameter;
import demetra.design.Development;
import demetra.timeseries.TimeSelector;
import demetra.processing.ProcSpecification;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true)
public final class TemporalDisaggregationSpec implements ProcSpecification {

    public static final AlgorithmDescriptor ALGORITHM = new AlgorithmDescriptor("temporaldisaggregation", "generic", null);

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return ALGORITHM;
    }

    public static enum Model {

        Wn,
        Ar1,
        Rw,
        RwAr1,
        I2, I3;

        public boolean hasParameter() {
            return this == Ar1 || this == RwAr1;
        }

        public boolean isStationary() {
            return this == Ar1 || this == Wn;
        }

        public int getParametersCount() {
            return (this == Ar1 || this == RwAr1) ? 1 : 0;
        }

        public int getDifferencingOrder() {
            switch (this) {
                case Rw:
                case RwAr1:
                    return 1;
                case I2:
                    return 2;
                case I3:
                    return 3;
                default:
                    return 0;
            }
        }
    }

    public static final double DEF_EPS = 1e-5;
    @lombok.NonNull
    private AggregationType aggregationType;
    @lombok.NonNull
    private Model residualsModel;
    private boolean constant, trend;
    @lombok.NonNull
    private TimeSelector estimationSpan;
    private boolean log, diffuseRegressors;
    private Double parameter, truncatedParameter;
    private boolean zeroInitialization, maximumLikelihood;
    private double estimationPrecision;

    public static TemporalDisaggregationSpecBuilder builder() {
        return new TemporalDisaggregationSpecBuilder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(Model.Ar1)
                .constant(true)
                .estimationSpan(TimeSelector.all())
                .maximumLikelihood(true)
                .truncatedParameter(0.0)
                .estimationPrecision(DEF_EPS);
    }

}
