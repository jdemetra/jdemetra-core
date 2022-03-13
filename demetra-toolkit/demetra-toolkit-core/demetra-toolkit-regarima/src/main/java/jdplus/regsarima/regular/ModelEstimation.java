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
import demetra.arima.SarimaSpec;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.Doubles;
import demetra.data.Parameter;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.ModellingUtility;
import demetra.timeseries.regression.TrendConstant;
import demetra.timeseries.regression.Variable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.regression.Regression;
import jdplus.regarima.IRegArimaComputer;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.RegArimaUtility;
import jdplus.sarima.SarimaModel;
import jdplus.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.stats.likelihood.LikelihoodStatistics;
import jdplus.stats.likelihood.LogLikelihoodFunction;
import jdplus.stats.tests.NiidTests;
import jdplus.timeseries.simplets.Transformations;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.Value
public final class ModelEstimation {

    private static final boolean[] EB = new boolean[0];

    private final TsData originalSeries;
    @lombok.Getter(lombok.AccessLevel.NONE)
    private final TsData interpolated;
    private final TsData transformedSeries;
    private final TsDomain estimationDomain;
    private final boolean logTransformation;
    private final LengthOfPeriodType lpTransformation;

    // Missing values correspond to the positions in the domain of the series !
    private final int[] missing;
    private final @lombok.NonNull
    Variable[] variables;

    private RegArimaModel<SarimaModel> model;
    private ConcentratedLikelihoodWithMissing concentratedLikelihood;

    private int freeArimaParametersCount;
    private boolean[] fixedArimaParameters;
    private double[] arimaParameters, arimaScore;
    private FastMatrix arimaCovariance;

    private LikelihoodStatistics statistics;

    public static ModelEstimation of(ModelDescription builder, IRegArimaComputer<SarimaModel> processor) {
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

        SarimaSpec arima = description.getArimaSpec();
        int free = arima.freeParametersCount(), all = arima.parametersCount();

        List<Variable> vars = description.variables().sequential().collect(Collectors.toList());
        int nvars = (int) vars.size();
        if (description.isMean()) {
            ++nvars;
        }
        this.variables = new Variable[nvars];
        DoubleSeqCursor cursor = estimation.getConcentratedLikelihood().coefficients().cursor();
        int k = 0;
        if (description.isMean()) {
            this.variables[k++] = Variable.variable("const", new TrendConstant(arima.getD(), arima.getBd()));
        }
        // fill the free coefficients
        for (Variable var : vars) {
            int nfree = var.freeCoefficientsCount();
            if (nfree == var.dim()) {
                Parameter[] p = new Parameter[nfree];
                for (int j = 0; j < nfree; ++j) {
                    p[j] = Parameter.estimated(cursor.getAndNext());
                }
                variables[k++] = var.withCoefficients(p);
            } else if (nfree > 0) {
                Parameter[] p = var.getCoefficients();
                for (int j = 0; j < p.length; ++j) {
                    if (p[j].isFree()) {
                        p[j] = Parameter.estimated(cursor.getAndNext());
                    }
                }
                variables[k++] = var.withCoefficients(p);
            } else {
                variables[k++] = var;
            }
        }

        this.model = estimation.getModel();
        this.concentratedLikelihood = estimation.getConcentratedLikelihood();
        this.statistics = estimation.statistics();

        LogLikelihoodFunction.Point<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing> max = estimation.getMax();
        freeArimaParametersCount = arima.freeParametersCount();
        if (max == null) {
            this.arimaParameters = Doubles.EMPTYARRAY;
            this.arimaScore = Doubles.EMPTYARRAY;
            this.arimaCovariance = FastMatrix.EMPTY;
            this.fixedArimaParameters = EB;
        } else {
            this.fixedArimaParameters = null;
//            this.fixedArimaParameters = arima.fixedConstraints();
//            if (arima.fixedParametersCount() == 0) {
            this.arimaParameters = max.getParameters().toArray();
            this.arimaScore = max.getScore().toArray();
            this.arimaCovariance = max.asymptoticCovariance();
//            } else {
//                // expand parameters, score, pcov;
//                this.arimaParameters = arima.parameters();
//                expand(max.getParameters().toArray(), fixedArimaParameters, this.arimaParameters);
//                this.arimaScore = expand(max.getScore().toArray(), fixedArimaParameters, Double.NaN);
//                this.arimaCovariance = expand(max.asymptoticCovariance(), fixedArimaParameters);
//            }
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

    public static FastMatrix expand(FastMatrix cov, boolean[] fixedItems) {
        int dim = fixedItems.length;
        int[] idx = new int[dim];
        for (int i = 0, j = 0; i < fixedItems.length; ++i) {
            if (!fixedItems[i]) {
                idx[j++] = i;
            }
        }
        FastMatrix m = FastMatrix.make(fixedItems.length, fixedItems.length);
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

    public TsData preadjustmentEffect(TsDomain domain, Predicate<Variable> test) {
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

    public TsData deterministicEffect(TsDomain domain, Predicate<Variable> test) {
        DataBlock all = DataBlock.make(domain.getLength());
        if (variables.length > 0) {
            DoubleSeqCursor cursor = concentratedLikelihood.coefficients().cursor();
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

    public TsData linearizedSeries() {
        TsData interp = interpolatedSeries(true);
        if (variables.length == 0) {
            return interp;
        }
        DataBlock rslt = DataBlock.of(interp.getValues());
        DoubleSeqCursor c = concentratedLikelihood.coefficients().cursor();
        int j = 0;
        if (model.isMean()) {
            c.skip(1);
            ++j;
        }
        TsDomain all = interp.getDomain();
        for (int i = j; i < variables.length; ++i) {
            FastMatrix xcur = Regression.matrix(all, variables[i].getCore());
            DataBlockIterator xcols = xcur.columnsIterator();
            while (xcols.hasNext()) {
                rslt.addAY(-c.getAndNext(), xcols.next());
            }
        }
        return TsData.of(interp.getStart(), rslt);
    }

    /**
     * Back-Transforms a series, so that it become comparable to the original
     * one
     *
     * @param s The transformed series (in logs for instance)
     * @param includeLp Specifies if a correction for leap year must be applied
     * @return
     */
    public TsData backTransform(TsData s, boolean includeLp) {
        if (logTransformation) {
            s = s.exp();
            if (includeLp && lpTransformation != LengthOfPeriodType.None) {
                s = Transformations.lengthOfPeriod(lpTransformation).converse().transform(s, null);
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
    public TsData transform(TsData s, boolean includeLp) {
        if (logTransformation) {
            if (includeLp && lpTransformation != LengthOfPeriodType.None) {
                s = Transformations.lengthOfPeriod(lpTransformation).transform(s, null);
            }
            s = s.log();
        }
        return s;
    }

    public TsData fullResiduals() {
        DoubleSeq res = RegArimaUtility.fullResiduals(model, concentratedLikelihood);
        TsPeriod start = transformedSeries.getEnd().plus(-res.length());
        return TsData.of(start, res);
    }

    public NiidTests residualsTests() {
        DoubleSeq res = RegArimaUtility.fullResiduals(model, concentratedLikelihood);
        return NiidTests.builder()
                .data(res)
                .period(originalSeries.getAnnualFrequency())
                .hyperParametersCount(freeArimaParametersCount)
                .build();
    }

    /**
     * tde
     *
     * @param domain
     * @return
     */
    public TsData getTradingDaysEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isDaysRelated(v));
        return backTransform(s, true);
    }

    /**
     * ee
     *
     * @param domain
     * @return
     */
    public TsData getEasterEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isEaster(v));
        return backTransform(s, false);
    }

    /**
     * mhe
     *
     * @param domain
     * @return
     */
    public TsData getMovingHolidayEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isMovingHoliday(v));
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
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isOutlier(v));
        return backTransform(s, false);
    }

    /**
     *
     * @param domain
     * @param ami
     * @return
     */
    public TsData getOutliersEffect(TsDomain domain, boolean ami) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isOutlier(v, ami));
        return backTransform(s, false);
    }

    /**
     * cal
     *
     * @param domain
     * @return
     */
    public TsData getCalendarEffect(TsDomain domain) {
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isCalendar(v));
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
