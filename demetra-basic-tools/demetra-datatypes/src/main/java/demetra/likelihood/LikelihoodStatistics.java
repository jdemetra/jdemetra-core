/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package demetra.likelihood;

import demetra.design.BuilderPattern;
import demetra.design.Development;
import java.util.Formatter;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Data
public class LikelihoodStatistics {

    @BuilderPattern(LikelihoodStatistics.class)
    public static class Builder  {

        private final int nobs;
        private final double ll;
        private int neffective, nparams;
        private double adj, ssqerr;

        private Builder(double ll, int nobs) {
            this.ll = ll;
            this.nobs = nobs;
            this.neffective = nobs;
        }

        public Builder differencingOrder(int ndiff) {
            this.neffective -= ndiff;
            return this;
        }

        public Builder llAdjustment(double adj) {
            this.adj = adj;
            return this;
        }

        public Builder ssq(double ssqerr) {
            this.ssqerr = ssqerr;
            return this;
        }

        /**
         * Number of parameters of the likelihood function.
         * <br>
         * The following parameters should be taken into account:
         * <br>
         * - Regression variables, including mean, excluding additive outliers
         * corresponding to missing values
         * <br>
         * - Hyper-parameters (ARMA...), including the scaling factor of the
         * innovations
         *
         * @param nparams
         * @return
         */
        public Builder parametersCount(int nparams) {
            this.nparams = nparams;
            return this;
        }

        public LikelihoodStatistics build() {
            LikelihoodStatistics stats = new LikelihoodStatistics();
            stats.observationsCount = nobs;
            stats.effectiveObservationsCount = neffective;
            stats.logLikelihood = ll;
            stats.estimatedParametersCount = nparams;
            if (Double.isNaN(adj)) {
                adj = 0;
            }
            stats.transformationAdjustment = adj;
            stats.adjustedLogLikelihood = adj == 0 ? stats.logLikelihood : stats.logLikelihood + stats.transformationAdjustment;
            stats.ssqErr = ssqerr;
            stats.calc();
            return stats;
        }

    }

    /**
     *
     * @param ll Log-likelihood
     * @param nobs Number of observations (missing are not taken into account)
     * @return
     */
    public static Builder statistics(double ll, int nobs) {
        return new Builder(ll, nobs);
    }

    private @lombok.Getter int observationsCount, effectiveObservationsCount, estimatedParametersCount;
    private @lombok.Getter double logLikelihood, transformationAdjustment,
            adjustedLogLikelihood, ssqErr,
            AIC, AICC, BIC, BICC, BIC2, HannanQuinn;

    /**
     *
     */
    private void calc() {
        double n = effectiveObservationsCount;
        double np = estimatedParametersCount;
        double ll = adjustedLogLikelihood;
        double nll = logLikelihood;
        AIC = -2 * (ll - np);
        HannanQuinn = -2 * (ll - np * Math.log(Math.log(n)));
        AICC = -2 * (ll - (n * np) / (n - np - 1));
        BIC = -2 * ll + np * Math.log(n);
        BIC2 = (-2 * nll + np * Math.log(n)) / n;
        if (ssqErr != 0) {
            BICC = Math.log(ssqErr / n) + (np - 1) * Math.log(n) / n; // TRAMO-like
        } else {
            ssqErr = Double.NaN;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Number of observations :").append(observationsCount).append(System.lineSeparator());
        if (effectiveObservationsCount != observationsCount) {
            builder.append("Effective number of observations :").append(
                    effectiveObservationsCount).append(System.lineSeparator());
        }
        if (estimatedParametersCount != 0) {
            builder.append("Number of parameters estimated :").append(
                    estimatedParametersCount).append(System.lineSeparator());
        }
        Formatter fmt = new Formatter();
        builder.append("log likelihood :").append(
                fmt.format("%.4f", logLikelihood)).append(System.lineSeparator());
        if (transformationAdjustment != 0) {
            fmt = new Formatter();
            builder.append("Transformation Adjustment :").append(
                    fmt.format("%.4f", transformationAdjustment)).append(System.lineSeparator());
            fmt = new Formatter();
            builder.append("Adjusted log likelihood :").append(
                    fmt.format("%.4f", adjustedLogLikelihood)).append(System.lineSeparator());
        }
        fmt = new Formatter();
        builder.append("AIC :").append(fmt.format("%.4f", AIC)).append(System.lineSeparator());
        fmt = new Formatter();
//        builder.append("AICC (F-corrected-AIC) :").append(
//                fmt.format("%.4f", AICC)).append(System.lineSeparator());
//        fmt = new Formatter();
//        builder.append("Hannan Quinn :").append(fmt.format("%.4f", HannanQuinn)).append(System.lineSeparator());
//        fmt = new Formatter();
        builder.append("BIC :").append(fmt.format("%.4f", BIC)).append(System.lineSeparator());
        fmt = new Formatter();
        if (Double.isFinite(ssqErr)) {
            builder.append("BIC corrected for length :").append(fmt.format("%.4f", BICC));
        }
        return builder.toString();

    }

}
