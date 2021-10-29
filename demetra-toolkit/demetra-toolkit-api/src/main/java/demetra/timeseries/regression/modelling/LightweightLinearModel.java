/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.timeseries.regression.modelling;

import demetra.data.DoubleSeq;
import demetra.likelihood.LikelihoodStatistics;
import demetra.likelihood.MissingValueEstimation;
import demetra.likelihood.ParametersEstimation;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.Variable;
import java.util.List;
import demetra.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 * @param <M>
 */
@lombok.experimental.UtilityClass
public class LightweightLinearModel{

    @lombok.Value
    @lombok.Builder
    public static class Description<M> implements GeneralLinearModel.Description<M> {

        /**
         * Original series
         */
        TsData series;
        /**
         * Log transformation
         */
        boolean logTransformation;

        /**
         * Transformation for leap year or length of period
         */
        LengthOfPeriodType lengthOfPeriodTransformation;

        /**
         * Regression variables (including mean correction)
         */
        Variable[] variables;
        
         /**
         * For instance SarimaSpec
         */
        M stochasticComponent;
    }

    @lombok.Value
    @lombok.Builder(builderClassName="Builder")
    public static class Estimation implements GeneralLinearModel.Estimation {

        /**
         * The linear model is composed of the transformed series (corrected for
         * fixed regression variables)
         * and of the free regression variable (including mean correction)
         */
        @lombok.NonNull
        private DoubleSeq y;
        @lombok.NonNull
        private Matrix X;

        /**
         * Regression estimation. The order correspond to the order of the
         * variables
         * Fixed coefficients are not included
         */
        @lombok.NonNull
        private DoubleSeq coefficients;
        @lombok.NonNull
        private Matrix coefficientsCovariance;

        @lombok.NonNull
        private MissingValueEstimation[] missing;
        /**
         * Parameters of the stochastic component. Fixed parameters are not
         * included
         */
        @lombok.NonNull
        private ParametersEstimation parameters;

//        @lombok.NonNull
        private LikelihoodStatistics statistics;

        @lombok.Singular
        private List<ProcessingLog.Information> logs;
        
        public static Builder builder(){
            Builder builder=new Builder();
            builder.y=DoubleSeq.empty();
            builder.X=Matrix.empty();
            builder.coefficients=DoubleSeq.empty();
            builder.coefficientsCovariance=Matrix.empty();
            builder.missing=NOMISSING;
            builder.parameters=ParametersEstimation.empty();
            return builder;
        }
        
        private static final MissingValueEstimation[] NOMISSING=new MissingValueEstimation[0];

    }

}
