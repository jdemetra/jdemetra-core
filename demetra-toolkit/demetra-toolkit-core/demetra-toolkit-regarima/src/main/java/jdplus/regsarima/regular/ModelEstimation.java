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
package jdplus.regsarima.regular;

import demetra.arima.SarimaOrders;
import demetra.data.DoubleSeqCursor;
import demetra.data.Doubles;
import demetra.data.Parameter;
import demetra.timeseries.regression.Variable;
import demetra.design.Development;
import jdplus.regarima.IRegArimaProcessor;
import jdplus.sarima.SarimaModel;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.likelihood.LikelihoodStatistics;
import demetra.timeseries.TsDomain;
import java.util.function.Predicate;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.likelihood.LogLikelihoodFunction;
import jdplus.math.matrices.Matrix;
import jdplus.modelling.regression.Regression;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.timeseries.simplets.Transformations;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.Value
public final class ModelEstimation {

    private final TsData originalSeries;
    @lombok.Getter(lombok.AccessLevel.NONE)
    private final TsData interpolated;
    private final TsData transformedSeries;
    private final TsDomain estimationDomain;
    private final boolean logTransformation;
    private final LengthOfPeriodType lpTransformation;

    // Missing values correspond to the positions in the domain of the series !
    private final int[] missing;
    private final Variable[] variables;

    private RegArimaModel<SarimaModel> model;
    private ConcentratedLikelihoodWithMissing concentratedLikelihood;

    private double[] parameters, score;
    private Matrix parametersCovariance;
    private LikelihoodStatistics statistics;

    private int freeParametersCount;

    public static ModelEstimation of(ModelDescription builder, IRegArimaProcessor<SarimaModel> processor) {
        return new ModelEstimation(builder, builder.estimate(processor));
    }

    public static ModelEstimation of(RegSarimaModelling regarima) {
        return new ModelEstimation(regarima.getDescription(), regarima.getEstimation());
    }

    private ModelEstimation(ModelDescription description, RegArimaEstimation<SarimaModel> estimation) {
        this.originalSeries = description.getSeries();
        this.transformedSeries = description.getTransformedSeries();
        this.interpolated = description.getInterpolatedSeries();
        this.logTransformation = description.isLogTransformation();
        this.lpTransformation = description.getPreadjustment();
        this.missing = description.getMissing();
        this.estimationDomain = description.getEstimationDomain();

        SarimaComponent arima = description.getArimaComponent();
        freeParametersCount = arima.getParametersCount();
        this.variables = description.variables().toArray(q -> new Variable[q]);

        this.model = estimation.getModel();
        this.concentratedLikelihood = estimation.getConcentratedLikelihood();
        this.statistics = estimation.statistics();

        LogLikelihoodFunction.Point<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing> max = estimation.getMax();
        if (max == null) {
            this.parameters = Doubles.EMPTYARRAY;
            this.score = Doubles.EMPTYARRAY;
            this.parametersCovariance = Matrix.EMPTY;
        } else {
            if (arima.getFixedParametersCount() == 0) {
                this.parameters = max.getParameters();
                this.score = max.getScore();
                this.parametersCovariance = max.asymptoticCovariance();
            } else {
                // expand parameters, score, pcov;
                boolean[] fc = arima.fixedConstraints();
                this.parameters = arima.parameters();
                expand(max.getParameters(), fc, this.parameters);
                this.score = expand(max.getScore(), fc, Double.NaN);
                this.parametersCovariance = expand(max.asymptoticCovariance(), fc);
            }
        }
    }

    private static double[] expand(double[] params, boolean[] fixedItems, double value) {
        double[] p = new double[fixedItems.length];
        for (int i = 0, j = 0; i < p.length; ++i) {
            if (fixedItems[i]) {
                p[i] = value;
            } else {
                p[i] = params[j++];
            }
        }
        return p;
    }

    private static void expand(double[] params, boolean[] fixedItems, double[] values) {
        for (int i = 0, j = 0; i < values.length; ++i) {
            if (!fixedItems[i]) {
                values[i] = params[j++];
            }
        }
    }

    public static Matrix expand(Matrix cov, boolean[] fixedItems) {
        int dim = fixedItems.length;
        int[] idx = new int[dim];
        for (int i = 0, j = 0; i < fixedItems.length; ++i) {
            if (!fixedItems[i]) {
                idx[j++] = i;
            }
        }
        Matrix m = Matrix.make(fixedItems.length, fixedItems.length);
        for (int i = 0; i < dim; ++i) {
            for (int j = 0; j <= i; ++j) {
                double s = cov.get(i, j);
                m.set(idx[i], idx[j], s);
                if (i != j) {
                    m.set(idx[j], idx[i], s);
                }
            }
        }
        return m;
    }

    public int getAnnualFrequency() {
        return originalSeries.getAnnualFrequency();
    }

    public SarimaOrders specification() {
        return model.arima().orders();
    }

    public TsData interpolatedSeries(boolean bTransformed) {

        TsData data;
        if (bTransformed) {
            data = transformedSeries;
        } else {
            data = interpolated;
        }

        // complete for missings
        int nmissing = concentratedLikelihood.nmissing();
        if (nmissing > 0) {
            double[] datac = data.getValues().toArray();
            if (bTransformed) {
                DoubleSeqCursor cur = concentratedLikelihood.missingCorrections().cursor();
                for (int i = 0; i < nmissing; ++i) {
                    datac[missing[i]] -= cur.getAndNext();
                }
            } else {
                DoubleSeqCursor cur = concentratedLikelihood.missingCorrections().cursor();
                for (int i = 0; i < nmissing; ++i) {
                    double m = cur.getAndNext();
                    if (logTransformation) {
                        datac[missing[i]] /= Math.exp(m);
                    } else {
                        datac[missing[i]] -= m;
                    }
                }
            }
            data = TsData.ofInternal(originalSeries.getStart(), datac);
        }
        return data;
    }

    public TsData regressionEffect(TsDomain domain, Predicate<Variable> test) {
        DataBlock all = DataBlock.make(domain.getLength());
        if (variables.length > 0) {
            DoubleSeqCursor cursor = concentratedLikelihood.coefficients().cursor();
            if (model.isMean()) {
                cursor.skip(1);
            }
            for (int i = 0; i < variables.length; ++i) {
                Variable cur = variables[i];
                int nfree = cur.freeCoefficientsCount();
                if (nfree > 0) {
                    if (test.test(cur)) {
                        Matrix m = Regression.matrix(domain, cur.getVariable());
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

    public TsData preadjustmentEffect(TsDomain domain, Predicate<Variable> test) {
        DataBlock all = DataBlock.make(domain.getLength());
        if (variables.length > 0) {
            for (int i = 0; i < variables.length; ++i) {
                Variable cur = variables[i];
                int nfree = cur.freeCoefficientsCount();
                if (cur.dim() > nfree) {
                    if (test.test(cur)) {
                        Matrix m = Regression.matrix(domain, cur.getVariable());
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

    public TsData deterministicEffect(TsDomain domain, Predicate<Variable> test) {
        DataBlock all = DataBlock.make(domain.getLength());
        if (variables.length > 0) {
            DoubleSeqCursor cursor = concentratedLikelihood.coefficients().cursor();
            if (model.isMean()) {
                cursor.skip(1);
            }
            for (int i = 0; i < variables.length; ++i) {
                Variable cur = variables[i];
                if (test.test(cur)) {
                    Matrix m = Regression.matrix(domain, cur.getVariable());
                    int ic = 0;
                    DataBlockIterator cols = m.columnsIterator();
                    while (cols.hasNext()) {
                        DataBlock col = cols.next();
                        Parameter c = cur.getCoefficient(ic++);
                        if (c.isFree()) {
                            all.addAY(cursor.getAndNext(), col);
                        }else{
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

    public TsData linearizedSeries() {
        TsData interp = interpolatedSeries(true);
        if (variables.length == 0) {
            return interp;
        }
        DataBlock rslt = DataBlock.of(interp.getValues());
        DoubleSeqCursor c = concentratedLikelihood.coefficients().cursor();
        if (model.isMean()) {
            c.skip(1);
        }
        TsDomain all = interp.getDomain();
        for (int i = 0; i < variables.length; ++i) {
            Matrix xcur = Regression.matrix(all, variables[i].getVariable());
            DataBlockIterator xcols = xcur.columnsIterator();
            while (xcols.hasNext()) {
                rslt.addAY(-c.getAndNext(), xcols.next());
            }
        }
        return TsData.ofInternal(interp.getStart(), rslt);
    }

    public TsData backTransform(TsData s, boolean includeLp) {
        if (logTransformation) {
            s = s.exp();
        }
        if (includeLp && lpTransformation != LengthOfPeriodType.None) {
            s = Transformations.lengthOfPeriod(lpTransformation).converse().transform(s, null);
        }
        return s;
    }

    /**
     * tde
     *
     * @param domain
     * @return
     */
    public TsData getTradingDaysEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> v.isCalendar());
        return backTransform(s, true);
    }

    /**
     * ee
     *
     * @param domain
     * @return
     */
    public TsData getEasterEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> v.isEaster());
        return backTransform(s, false);
    }

    /**
     * mhe
     *
     * @param domain
     * @return
     */
    public TsData getMovingHolidayEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> v.isMovingHolidays());
        return backTransform(s, false);
    }

    /**
     * rmde
     *
     * @param domain
     * @return
     */
    public TsData getRamadanEffect(TsDomain domain) {
        throw new UnsupportedOperationException("Not supported yet.");
//        TsData s = deterministicEffect(domain, v->v instanceof RamadanVariable);
//        return backTransform(s, false);
    }

    /**
     * out
     *
     * @param domain
     * @return
     */
    public TsData getOutliersEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> v .isOutlier());
        return backTransform(s, false);
    }

    /**
     *
     * @param domain
     * @param prespecified
     * @return
     */
    public TsData getOutliersEffect(TsDomain domain, boolean prespecified) {
        TsData s = deterministicEffect(domain, v -> v.isOutlier(prespecified));
        return backTransform(s, false);
    }

    /**
     * cal
     *
     * @param domain
     * @return
     */
    public TsData getCalendarEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> v.isCalendar() || v.isMovingHolidays());
        return backTransform(s, true);
    }

    /**
     * Gets all the deterministic effects
     *
     * @param domain
     * @return
     */
    public TsData getDeterministicEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> true);
        return backTransform(s, true);
    }

}
