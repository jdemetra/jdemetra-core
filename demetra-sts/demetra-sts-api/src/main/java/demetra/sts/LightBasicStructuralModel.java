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
package demetra.sts;

import demetra.likelihood.DiffuseLikelihoodStatistics;
import demetra.data.DoubleSeq;
import demetra.likelihood.MissingValueEstimation;
import demetra.likelihood.ParametersEstimation;
import demetra.math.matrices.MatrixType;
import demetra.processing.ProcessingLog;
import demetra.sa.SeriesDecomposition;
import demetra.stats.StatisticalTest;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.Variable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PALATEJ
 * @param <M>
 */
@lombok.Value
@lombok.Builder
public class LightBasicStructuralModel<M> implements BasicStructuralModel {

    Description description;
    Estimation estimation;
    BsmDecomposition bsmDecomposition;
    SeriesDecomposition finalDecomposition;

    @lombok.Singular
    private Map<String, StatisticalTest> diagnostics;

    @lombok.Singular
    private Map<String, Object> additionalResults;

    @lombok.Value
    @lombok.Builder
    public static class Description implements BsmDescription {

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
        BsmSpec specification;
        
    }

    @lombok.Value
    @lombok.Builder
    public static class Estimation implements BsmEstimation {

        /**
         * The linear model is composed of the transformed series (corrected for
         * fixed regression variables)
         * and of the free regression variable (including mean correction)
         */
//        @lombok.NonNull
        private DoubleSeq y;
//        @lombok.NonNull
        private MatrixType X;

        /**
         * Regression estimation. The order correspond to the order of the
         * variables
         * Fixed coefficients are not included
         */
//        @lombok.NonNull
        private DoubleSeq coefficients;
//        @lombok.NonNull
        private MatrixType coefficientsCovariance;

//        @lombok.NonNull
        private MissingValueEstimation[] missing;
        /**
         * Parameters of the stochastic component. Fixed parameters are not
         * included
         */
//        @lombok.NonNull
        private ParametersEstimation parameters;

//        @lombok.NonNull
        private DiffuseLikelihoodStatistics statistics;

//        @lombok.NonNull
        private DoubleSeq residuals;

        @lombok.Singular
        private List<ProcessingLog.Information> logs;

    }

}
