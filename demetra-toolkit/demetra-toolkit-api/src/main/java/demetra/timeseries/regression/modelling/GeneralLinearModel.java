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
import demetra.math.matrices.MatrixType;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.Variable;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author PALATEJ
 * @param <M>
 */
public interface GeneralLinearModel<M> {

    Description<M> getDescription();

    Estimation getEstimation();

    Map<String, Object> getAdditionalResults();

    interface Description<M> {

        /**
         * Original series
         *
         * @return
         */
        TsData getSeries();

        /**
         * Log transformation
         *
         * @return
         */
        boolean isLogTransformation();

        /**
         * Transformation for leap year or length of period
         *
         * @return
         */
        LengthOfPeriodType getLengthOfPeriodTransformation();

        boolean isMean();

        /**
         * Regression variables (including mean correction)
         *
         * @return
         */
        Variable[] getVariables();

        /**
         * For instance SarimaSpec
         *
         * @return
         */
        M getStochasticComponent();

    }

    interface Estimation {

        /**
         * The linear model is composed of the transformed series (corrected for
         * fixed regression variables)
         * and of the free regression variable (including mean correction)
         *
         * @return
         */
        DoubleSeq getY();

        /**
         * @return y not corrected for missing
         */
        default DoubleSeq originalY() {
            DoubleSeq y = getY();
            if (y.anyMatch(z -> Double.isNaN(z))) {
                // already contains the missing values
                return y;
            }
            MissingValueEstimation[] missing = getMissing();
            if (missing.length == 0) {
                return y;
            }
            double[] z = y.toArray();
            for (int i = 0; i < missing.length; ++i) {
                z[missing[i].getPosition()] = Double.NaN;
            }
            return DoubleSeq.of(z);
        }

        /**
         * Regression variables (including meanCorrection)
         *
         * @return
         */
        MatrixType getX();

        /**
         * Regression estimation.The order correspond to the order of the
         * variables
         * Fixed coefficients are not included
         *
         * @return
         */
        DoubleSeq getCoefficients();

        /**
         *
         * @return
         */
        MatrixType getCoefficientsCovariance();

        /**
         *
         * @return
         */
        MissingValueEstimation[] getMissing();

        /**
         * Parameters of the stochastic component.Fixed parameters are not
         * included
         *
         * @return
         */
        ParametersEstimation getParameters();

        /**
         *
         * @return
         */
        LikelihoodStatistics getStatistics();

        DoubleSeq getResiduals();

        List<ProcessingLog.Information> getLogs();

    }

}
