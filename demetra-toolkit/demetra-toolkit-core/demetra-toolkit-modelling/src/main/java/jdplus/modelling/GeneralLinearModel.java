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
package jdplus.modelling;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.Parameter;
import demetra.information.Explorable;
import jdplus.stats.likelihood.LikelihoodStatistics;
import demetra.timeseries.regression.MissingValueEstimation;
import demetra.data.ParametersEstimation;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.Variable;
import java.util.List;
import demetra.math.matrices.Matrix;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.regression.ModellingUtility;
import demetra.timeseries.regression.TrendConstant;
import java.util.function.Predicate;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.regression.Regression;
import jdplus.timeseries.simplets.Transformations;

/**
 *
 * @author PALATEJ
 * @param <M>
 */
public interface GeneralLinearModel<M> extends Explorable {

    Description<M> getDescription();

    Estimation getEstimation();

    Residuals getResiduals();

    interface Description<M> {

        /**
         * Original series
         *
         * @return
         */
        TsData getSeries();

        default TsDomain getDomain() {
            return getSeries().getDomain();
        }

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

        TsDomain getDomain();

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
        Matrix getX();

        /**
         * Regression estimation.The order correspond to the order of the
         * variables
         * Fixed coefficients are not included
         *
         * @return
         */
        DoubleSeq getCoefficients();

        /**
         * Covariance of the regression coefficients. The scaling factor
         * (sigma2) is the ML estimate (ssqerr/n)
         *
         * @return
         */
        Matrix getCoefficientsCovariance();

        /**
         * Position corresponding to the estimation domain
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

        List<ProcessingLog.Information> getLogs();

    }

    /**
     * Gets the effect of all the variables (pre-specified or estimated)
     * The result corresponds to the transformed series (log + lp-adjust)
     *
     * @param domain
     * @param test
     * @return
     */
    default TsData deterministicEffect(TsDomain domain, Predicate<Variable> test) {
        Description description = getDescription();
        Estimation estimation = getEstimation();
        if (domain == null) {
            domain = description.getSeries().getDomain();
        }
        DataBlock all = DataBlock.make(domain.getLength());
        Variable[] variables = description.getVariables();
        if (variables.length > 0) {
            DoubleSeqCursor cursor = estimation.getCoefficients().cursor();
            for (int i = 0; i < variables.length; ++i) {
                Variable cur = variables[i];
                if (test.test(cur)) {
                    FastMatrix m = Regression.matrix(domain, cur.getCore());
                    int ic = 0;
                    DataBlockIterator cols = m.columnsIterator();
                    while (cols.hasNext()) {
                        DataBlock col = cols.next();
                        Parameter c = cur.getCoefficient(ic++);
                        if (c.isFree()) {
                            all.addAY(cursor.getAndNext(), col);
                        } else {
                            all.addAY(c.getValue(), col);

                        }
                    }
                } else {
                    cursor.skip(cur.freeCoefficientsCount());
                }
            }
        }
        return TsData.ofInternal(domain.getStartPeriod(), all.getStorage());
    }

    default TsData deterministicEffect(TsDomain domain) {
        return deterministicEffect(domain, v -> true);
    }

    default boolean isMeanCorrection() {
        Description description = getDescription();
        Variable[] variables = description.getVariables();
        for (int i = 0; i < variables.length; ++i) {
            if (variables[i].getCore() instanceof TrendConstant) {
                return true;
            }
        }
        return false;
    }

    default boolean isMeanEstimation() {
        Description description = getDescription();
        Variable[] variables = description.getVariables();
        for (int i = 0; i < variables.length; ++i) {
            if (variables[i].getCore() instanceof TrendConstant) {
                return variables[i].isFree();
            }
        }
        return false;
    }

    default TsData linearizedSeries() {
        Description description = getDescription();
        TsData interp = interpolatedSeries(true);
        Variable[] variables = description.getVariables();
        if (variables.length == 0) {
            return interp;
        }
        TsData det = deterministicEffect(interp.getDomain(), v -> !(v.getCore() instanceof TrendConstant));

        return TsData.subtract(interp, det);
    }

    /**
     * Back-Transforms a series, so that it become comparable to the original
     * one
     *
     * @param s The transformed series (in logs for instance)
     * @param includeLp Specifies if a correction for leap year must be applied
     * @return
     */
    default TsData backTransform(TsData s, boolean includeLp) {
        Description description = getDescription();
        if (description.isLogTransformation()) {
            s = s.exp();
            if (includeLp && description.getLengthOfPeriodTransformation() != LengthOfPeriodType.None) {
                s = Transformations.lengthOfPeriod(description.getLengthOfPeriodTransformation()).converse().transform(s, null);
            }
        }
        return s;
    }

    /**
     * Transforms a series with the same operations as those applied to the
     * original series
     *
     * @param s The series to be transformed
     * @param includeLp Specifies if a correction for leap year must be
     * applied
     * @return
     */
    default TsData transform(TsData s, boolean includeLp) {
        Description description = getDescription();
        if (description.isLogTransformation()) {
            if (includeLp && description.getLengthOfPeriodTransformation() != LengthOfPeriodType.None) {
                s = Transformations.lengthOfPeriod(description.getLengthOfPeriodTransformation()).transform(s, null);
            }
            s = s.log();
        }
        return s;
    }

    default TsData transformedSeries() {
        Description description = getDescription();
        LengthOfPeriodType lp = description.getLengthOfPeriodTransformation();
        TsData tmp = description.getSeries();
        if (description.isLogTransformation()) {
            if (lp != LengthOfPeriodType.None) {
                tmp = Transformations.lengthOfPeriod(lp).transform(tmp, null);
            }
            tmp = Transformations.log().transform(tmp, null);
        }
        return tmp;
    }

    /**
     * Interpolated series (contains all deterministic effects
     *
     * @param bTransformed True if the series has been transformed (log,
     * leap-year)
     * @return The interpolated series
     */
    default TsData interpolatedSeries(boolean bTransformed) {
        Description description = getDescription();
        Estimation estimation = getEstimation();
        // complete for missings
        MissingValueEstimation[] missing = estimation.getMissing();
        int nmissing = missing.length;
        TsData data = bTransformed ? transformedSeries() : description.getSeries();
        if (nmissing > 0) {
            TsDomain domain = description.getDomain();
            int dpos = domain.getStartPeriod().until(estimation.getDomain().getStartPeriod());
            double[] datac = data.getValues().toArray();
            for (int i = 0; i < nmissing; ++i) {
                int pos = dpos + missing[i].getPosition();
                TsDomain mdom = TsDomain.of(domain.get(pos), 1);
                TsData de = deterministicEffect(mdom, v -> true);
                if (bTransformed || !description.isLogTransformation()) {
                    datac[pos] = missing[i].getValue() + de.getValue(0);
                } else {
                    de = backTransform(de, true);
                    datac[pos] = Math.exp(missing[i].getValue()) * de.getValue(0);
                }
            }
            data = TsData.ofInternal(description.getSeries().getStart(), datac);
        }
        return data;
    }

    default double[] missingEstimates() {

        Description description = getDescription();
        Estimation estimation = getEstimation();
        // complete for missings
        MissingValueEstimation[] missing = estimation.getMissing();
        int nmissing = missing.length;
        double[] missingvals = new double[nmissing];
        if (nmissing > 0) {
            TsDomain domain = description.getDomain();
            int dpos = domain.getStartPeriod().until(estimation.getDomain().getStartPeriod());
            for (int i = 0; i < nmissing; ++i) {
                int pos = dpos + missing[i].getPosition();
                TsDomain mdom = TsDomain.of(domain.get(pos), 1);
                TsData det = deterministicEffect(mdom);
                if (!description.isLogTransformation()) {
                    missingvals[i] = missing[i].getValue() + det.getValue(0);
                } else {
                    det = backTransform(det, true);
                    missingvals[i] = Math.exp(missing[i].getValue()) * det.getValue(0);
                }
            }
        }
        return missingvals;
    }

    /**
     * Gets the effect of all the estimated regression variables (= with unknown
     * coefficients)
     * The result corresponds to the transformed series (log + lp-adjust)
     *
     * @param domain
     * @param test
     * @return
     */
    default TsData regressionEffect(TsDomain domain, Predicate<Variable> test) {
        Description description = getDescription();
        Estimation estimation = getEstimation();
        if (domain == null) {
            domain = description.getSeries().getDomain();
        }
        Variable[] variables = description.getVariables();
        DataBlock all = DataBlock.make(domain.getLength());
        if (variables.length > 0) {
            DoubleSeqCursor cursor = estimation.getCoefficients().cursor();
            for (int i = 0; i < variables.length; ++i) {
                Variable cur = variables[i];
                int nfree = cur.freeCoefficientsCount();
                if (nfree > 0) {
                    if (test.test(cur)) {
                        FastMatrix m = Regression.matrix(domain, cur.getCore());
                        int ic = 0;
                        DataBlockIterator cols = m.columnsIterator();
                        while (cols.hasNext()) {
                            DataBlock col = cols.next();
                            Parameter c = cur.getCoefficient(ic++);
                            if (c.isFree()) {
                                all.addAY(cursor.getAndNext(), col);
                            }
                        }
                    } else {
                        cursor.skip(nfree);
                    }
                }
            }
        }
        return TsData.ofInternal(domain.getStartPeriod(), all.getStorage());
    }

    /**
     * Gets the effect of all pre-specified variables (= with fixed
     * coefficients)
     * The result corresponds to the transformed series (log + lp-adjust)
     *
     * @param domain
     * @param test
     * @return
     */
    default TsData preadjustmentEffect(TsDomain domain, Predicate<Variable> test) {
        Description description = getDescription();
        Estimation estimation = getEstimation();
        Variable[] variables = description.getVariables();
        DataBlock all = DataBlock.make(domain.getLength());
        if (variables.length > 0) {
            for (int i = 0; i < variables.length; ++i) {
                Variable cur = variables[i];
                int nfree = cur.freeCoefficientsCount();
                if (cur.dim() > nfree) {
                    if (test.test(cur)) {
                        FastMatrix m = Regression.matrix(domain, cur.getCore());
                        int ic = 0;
                        DataBlockIterator cols = m.columnsIterator();
                        while (cols.hasNext()) {
                            DataBlock col = cols.next();
                            Parameter c = cur.getCoefficient(ic++);
                            if (!c.isFree()) {
                                all.addAY(c.getValue(), col);
                            }
                        }
                    }
                }

            }
        }
        return TsData.ofInternal(domain.getStartPeriod(), all.getStorage());
    }

    /**
     * tde
     *
     * @param domain If the domain is null, the series domain is used
     * @return
     */
    default TsData getTradingDaysEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isDaysRelated(v));
        return backTransform(s, true);
    }

    /**
     * ee
     *
     * @param domain If the domain is null, the series domain is used
     * @return
     */
    default TsData getEasterEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isEaster(v));
        return backTransform(s, false);
    }

    /**
     * mhe
     *
     * @param domain If the domain is null, the series domain is used
     * @return
     */
    default TsData getMovingHolidayEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isMovingHoliday(v));
        return backTransform(s, false);
    }

    /**
     * mhe
     *
     * @param domain If the domain is null, the series domain is used
     * @return
     */
    default TsData getOtherMovingHolidayEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isMovingHoliday(v) && ! ModellingUtility.isEaster(v));
        return backTransform(s, false);
    }
    /**
     * rmde
     *
     * @param domain If the domain is null, the series domain is used
     * @return
     */
    default TsData getRamadanEffect(TsDomain domain) {
        throw new UnsupportedOperationException("Not supported yet.");
//        TsData s = deterministicEffect(domain, v->v instanceof RamadanVariable);
//        return backTransform(s, false);
    }

    /**
     * out
     *
     * @param domain If the domain is null, the series domain is used
     * @return
     */
    default TsData getOutliersEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isOutlier(v));
        return backTransform(s, false);
    }

    /**
     *
     * @param domain If the domain is null, the series domain is used
     * @param ami if true, only the outliers detected by the automatic procedure
     * is used
     * @return
     */
    default TsData getOutliersEffect(TsDomain domain, boolean ami) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isOutlier(v, ami));
        return backTransform(s, false);
    }

    /**
     * cal
     *
     * @param domain If the domain is null, the series domain is used
     * @return
     */
    default TsData getCalendarEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isCalendar(v));
        return backTransform(s, true);
    }

    /**
     * The forecast domain is relative to the series domain, not to the
     * estimation domain
     *
     * @param nfcast
     * @return
     */
    default TsDomain forecastDomain(int nfcast) {
        TsDomain domain = getDescription().getDomain();
        if (nfcast < 0) {
            nfcast = (-nfcast) * domain.getAnnualFrequency();
        }

        return TsDomain.of(domain.getEndPeriod(), nfcast);
    }


    /**
     * The backcast domain is relative to the series domain, not to
     * the
     * estimation domain
     *
     * @param nbcast
     * @return
     */
    default TsDomain backcastDomain(int nbcast) {
        TsDomain domain = getDescription().getDomain();
        if (nbcast < 0) {
            nbcast = (-nbcast) * domain.getAnnualFrequency();
        }
        TsPeriod start = domain.getStartPeriod().plus(-nbcast);
        return TsDomain.of(start, nbcast);
    }

    /**
     * Add/multiply series
     *
     * @param l
     * @param r
     * @return
     */
    default TsData op(TsData l, TsData... r) {
        if (getDescription().isLogTransformation()) {
            return TsData.multiply(l, r);
        } else {
            return TsData.add(l, r);
        }
    }

    /**
     * Subtract/divide two series
     *
     * @param l
     * @param r
     * @return
     */
    default TsData inv_op(TsData l, TsData r) {
        if (getDescription().isLogTransformation()) {
            return TsData.divide(l, r);
        } else {
            return TsData.subtract(l, r);
        }
    }

    
}
