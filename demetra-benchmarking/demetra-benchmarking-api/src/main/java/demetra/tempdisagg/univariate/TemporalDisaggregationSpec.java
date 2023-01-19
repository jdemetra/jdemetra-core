/*
 * Copyright 2022 National Bank of Belgium.
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
import demetra.timeseries.TimeSelector;
import demetra.processing.ProcSpecification;
import demetra.ssf.SsfInitialization;
import demetra.util.Validatable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Getter
@lombok.Builder(toBuilder = true, buildMethodName = "buildWithoutValidation")
public final class TemporalDisaggregationSpec implements ProcSpecification, Validatable<TemporalDisaggregationSpec> {

    public static final String VERSION = "3.0.0";

    public static final String FAMILY = "temporaldisaggregation";
    public static final String METHOD = "generic";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);
    
    public static final SsfInitialization DEF_ALGORITHM=SsfInitialization.SqrtDiffuse;
    public static final boolean DEF_FAST=true, DEF_RESCALE=true, DEF_LOG=false, DEF_DIFFUSE=false;
    
   public static final double DEF_EPS = 1e-5;
    
    public static final AggregationType DEF_AGGREGATION=AggregationType.Sum;
    
    public static final TemporalDisaggregationSpec CHOWLIN = builder()
            .estimationSpan(TimeSelector.all())
            .aggregationType(AggregationType.Sum)
            .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
            .constant(true)
            .truncatedParameter(0.0)
            .fast(DEF_FAST)
            .estimationPrecision(DEF_EPS)
            .rescale(DEF_RESCALE)
            .algorithm(DEF_ALGORITHM)
            .defaultPeriod(4)
            .build();

    public static final TemporalDisaggregationSpec FERNANDEZ = builder()
            .estimationSpan(TimeSelector.all())
            .aggregationType(AggregationType.Sum)
            .residualsModel(TemporalDisaggregationSpec.Model.Rw)
            .constant(false)
            .fast(DEF_FAST)
            .estimationPrecision(DEF_EPS)
            .rescale(DEF_RESCALE)
            .algorithm(DEF_ALGORITHM)
            .defaultPeriod(4)
            .build();


    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
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
            return switch (this) {
                case Rw, RwAr1 -> 1;
                case I2 -> 2;
                case I3 -> 3;
                default -> 0;
            };
        }
    }

     @lombok.NonNull
    private AggregationType aggregationType;
    private int observationPosition;
    private int defaultPeriod;

    @lombok.NonNull
    private Model residualsModel;
    private boolean constant, trend;
    private Parameter parameter;
    @lombok.NonNull
    private TimeSelector estimationSpan;
    private boolean log, diffuseRegressors;
    private Double truncatedParameter;
    private boolean zeroInitialization, fast;

    private double estimationPrecision;
    private SsfInitialization algorithm;
    private boolean rescale;

    public boolean isParameterEstimation() {
        return (residualsModel == Model.Ar1 || residualsModel == Model.RwAr1)
                && parameter.getType() != ParameterType.Fixed;
    }

    public static class Builder implements Validatable.Builder<TemporalDisaggregationSpec> {
    }

    public static Builder builder() {
        return new Builder()
                .aggregationType(DEF_AGGREGATION)
                .residualsModel(Model.Ar1)
                .constant(true)
                .estimationSpan(TimeSelector.all())
                .fast(DEF_FAST)
                .algorithm(DEF_ALGORITHM)
                .rescale(DEF_RESCALE)
                .parameter(Parameter.undefined())
                .estimationPrecision(DEF_EPS)
                .defaultPeriod(4);
    }

    @Override
    public TemporalDisaggregationSpec validate() throws IllegalArgumentException {
        switch (residualsModel) {
            case Rw, RwAr1 -> {
                if (constant && !zeroInitialization) {
                    throw new IllegalArgumentException("constant not allowed");
                }
            }
            case I2, I3 -> {
                if (constant && !zeroInitialization) {
                    throw new IllegalArgumentException("constant not allowed");
                }
                if (trend && !zeroInitialization) {
                    throw new IllegalArgumentException("trend not allowed");
                }
            }
        }
        return this;
    }
    
    @Override
    public String toString(){
        return switch (residualsModel) {
            case Ar1 -> "Chow-Lin";
            case Rw -> "Fernandez";
            case RwAr1 -> "Litterman";
            case Wn -> "Ols";
            default -> "regression";
        };
    }

}
