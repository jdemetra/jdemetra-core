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
import demetra.data.ParameterType;
import nbbrd.design.Development;
import demetra.processing.ProcSpecification;
import demetra.tempdisagg.univariate.TemporalDisaggregationSpec.Model;
import demetra.util.Validatable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true,  buildMethodName = "buildWithoutValidation")
public final class TemporalDisaggregationISpec implements ProcSpecification, Validatable<TemporalDisaggregationISpec> {

    public static final AlgorithmDescriptor ALGORITHM = new AlgorithmDescriptor("temporaldisaggregation", "modelI", null);

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return ALGORITHM;
    }

    public static final double DEF_EPS = 1e-12;
    @lombok.NonNull
    private AggregationType aggregationType;
    private int observationPosition;
    @lombok.NonNull
    private Model residualsModel;
    private boolean constant;
    private Parameter parameter;
    private double truncatedRho;
    private double estimationPrecision;

    public boolean isParameterEstimation() {
        return (residualsModel == Model.Ar1 || residualsModel == Model.RwAr1)
                && parameter.getType() != ParameterType.Fixed;
    }

    public static class Builder implements Validatable.Builder<TemporalDisaggregationISpec> {
    }

    public static Builder builder() {
        return new Builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(Model.Ar1)
                .constant(true)
                .parameter(Parameter.initial(.9))
                .truncatedRho(-1)
                .estimationPrecision(DEF_EPS);
    }

    @Override
    public TemporalDisaggregationISpec validate() throws IllegalArgumentException {
        switch (residualsModel) {
            case Rw:
            case Ar1:
                break;
            default:
                throw new IllegalArgumentException("Not implemented yet");

        }
        return this;
    }

}
