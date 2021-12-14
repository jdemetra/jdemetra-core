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
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.Doubles;
import demetra.data.Parameter;
import demetra.information.GenericExplorable;
import demetra.likelihood.LikelihoodStatistics;
import demetra.likelihood.MissingValueEstimation;
import demetra.likelihood.ParametersEstimation;
import demetra.math.matrices.Matrix;
import demetra.modelling.implementations.SarimaSpec;
import demetra.processing.ProcessingLog;
import demetra.stats.ProbabilityType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.TrendConstant;
import demetra.timeseries.regression.Variable;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import demetra.timeseries.regression.modelling.LightweightLinearModel;
import demetra.timeseries.regression.modelling.RegressionItem;
import demetra.timeseries.regression.modelling.Residuals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.dstats.LogNormal;
import jdplus.dstats.T;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.likelihood.DefaultLikelihoodEvaluation;
import jdplus.likelihood.LogLikelihoodFunction;
import jdplus.math.functions.IFunction;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.regression.Regression;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaForecasts;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.RegArimaUtility;
import jdplus.regarima.RegArmaModel;
import jdplus.regarima.ami.ModellingUtility;
import jdplus.regarima.estimation.RegArmaFunction;
import jdplus.sarima.SarimaModel;
import jdplus.sarima.estimation.SarimaFixedMapping;
import jdplus.sarima.estimation.SarimaMapping;
import jdplus.stats.tests.NiidTests;
import jdplus.timeseries.simplets.Transformations;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder
public class RegSarimaModel implements GeneralLinearModel<SarimaSpec>, GenericExplorable {

    private static final MissingValueEstimation[] NOMISSING = new MissingValueEstimation[0];

    public static RegSarimaModel of(ModelDescription model, jdplus.regsarima.RegSarimaComputer processor) {
        return RegSarimaModel.of(model, processor.process(model.regarima(), model.mapping()), ProcessingLog.dummy());
    }

    public static RegSarimaModel of(ModelDescription description, RegArimaEstimation<SarimaModel> estimation, ProcessingLog log) {

        SarimaSpec arima = description.getArimaSpec();
        int free = arima.freeParametersCount();
        RegArimaModel<SarimaModel> model = estimation.getModel();
        ConcentratedLikelihoodWithMissing ll = estimation.getConcentratedLikelihood();

        TsData interpolated = description.getInterpolatedSeries();
        TsData transformed = description.getTransformedSeries();

        List<Variable> vars = description.variables().sequential().collect(Collectors.toList());
        int nvars = (int) vars.size();
        if (description.isMean()) {
            ++nvars;
        }
        Variable[] variables = new Variable[nvars];
        DoubleSeqCursor cursor = estimation.getConcentratedLikelihood().coefficients().cursor();
        DoubleSeqCursor.OnMutable diag = estimation.getConcentratedLikelihood().unscaledCovariance().diagonal().cursor();
        int df = ll.degreesOfFreedom() - free;
        double vscale = ll.ssq() / df;
        T tstat = new T(df);

        int k = 0, pos = 0;

        List<RegressionDesc> regressionDesc = new ArrayList<>();
        if (description.isMean()) {
            ITsVariable cur = new TrendConstant(arima.getD(), arima.getBd());
            double c = cursor.getAndNext(), e = Math.sqrt(diag.getAndNext() * vscale);
            regressionDesc.add(new RegressionDesc(cur, 0, pos++, c, e, 2 * tstat.getProbability(Math.abs(c / e), ProbabilityType.Upper)));
            variables[k++] = Variable.variable("const", cur)
                    .withCoefficient(Parameter.estimated(c));
        }
        // fill the free coefficients
        for (Variable var : vars) {
            int nfree = var.freeCoefficientsCount();
            if (nfree == var.dim()) {
                Parameter[] p = new Parameter[nfree];
                for (int j = 0; j < nfree; ++j) {
                    double c = cursor.getAndNext(), e = Math.sqrt(diag.getAndNext() * vscale);
                    p[j] = Parameter.estimated(c);
                    regressionDesc.add(new RegressionDesc(var.getCore(), j, pos++, c, e, 2 * tstat.getProbability(Math.abs(c / e), ProbabilityType.Upper)));
                }
                variables[k++] = var.withCoefficients(p);
            } else if (nfree > 0) {
                Parameter[] p = var.getCoefficients();
                for (int j = 0; j < p.length; ++j) {
                    if (p[j].isFree()) {
                        double c = cursor.getAndNext(), e = Math.sqrt(diag.getAndNext() * vscale);
                        p[j] = Parameter.estimated(c);
                        regressionDesc.add(new RegressionDesc(var.getCore(), j, pos++, c, e, 2 * tstat.getProbability(Math.abs(c / e), ProbabilityType.Upper)));
                    }
                }
                variables[k++] = var.withCoefficients(p);
            } else {
                variables[k++] = var;
            }
        }

        LightweightLinearModel.Description desc = LightweightLinearModel.Description.<SarimaSpec>builder()
                .series(description.getSeries())
                .lengthOfPeriodTransformation(description.getPreadjustment())
                .logTransformation(description.isLogTransformation())
                .variables(variables)
                .stochasticComponent(arima)
                .build();

        LogLikelihoodFunction.Point<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing> max = estimation.getMax();
        ParametersEstimation pestim;
        if (max == null) {
            pestim = new ParametersEstimation(Doubles.EMPTY, FastMatrix.EMPTY, Doubles.EMPTY, null);
        } else {
            pestim = new ParametersEstimation(max.getParameters(), max.asymptoticCovariance(), max.getScore(), "sarima (true signs)");
        }

        // complete for missings
        int nmissing = ll.nmissing();
        MissingValueEstimation[] missing = NOMISSING;
        if (nmissing > 0) {
            double[] datac = interpolated.getValues().toArray();
            int dpos = description.getDomain().getStartPeriod().until(description.getEstimationDomain().getStartPeriod());
            missing = new MissingValueEstimation[nmissing];
            DoubleSeqCursor cur = ll.missingCorrections().cursor();
            DoubleSeqCursor vcur = ll.missingUnscaledVariances().cursor();
            int[] pmissing = model.missing();
            for (int i = 0; i < nmissing; ++i) {
                double m = cur.getAndNext();
                double v = vcur.getAndNext();
                missing[i] = new MissingValueEstimation(pmissing[i] + dpos, model.getY().get(pmissing[i]) - m, Math.sqrt(v * vscale));
                if (description.isLogTransformation()) {
                    datac[pmissing[i] + dpos] /= Math.exp(m);
                } else {
                    datac[pmissing[i] + dpos] -= m;
                }
            }
            transformed = TsData.ofInternal(transformed.getStart(), datac);
        }
        DoubleSeq fullRes = RegArimaUtility.fullResiduals(model, ll);
        LightweightLinearModel.Estimation est = LightweightLinearModel.Estimation.builder()
                .y(model.getY())
                .X(model.allVariables())
                .coefficients(ll.coefficients())
                .coefficientsCovariance(ll.covariance(free, true))
                .parameters(pestim)
                .statistics(estimation.statistics())
                .missing(missing)
                .logs(log.all())
                .build();

        int period = desc.getSeries().getAnnualFrequency();
        NiidTests niid = NiidTests.builder()
                .data(fullRes)
                .period(period)
                .hyperParametersCount(free)
                .build();

        Residuals residuals = Residuals.builder()
                .type(Residuals.Type.FullResiduals)
                .res(fullRes)
                .start(description.getEstimationDomain().getEndPeriod().plus(-fullRes.length()))
                .test(Residuals.MEAN, niid.meanTest())
                .test(Residuals.SKEW, niid.skewness())
                .test(Residuals.KURT, niid.kurtosis())
                .test(Residuals.DH, niid.normalityTest())
                .test(Residuals.LB, niid.ljungBox())
                .test(Residuals.BP, niid.boxPierce())
                .test(Residuals.SEASLB, niid.seasonalLjungBox())
                .test(Residuals.SEASBP, niid.seasonalBoxPierce())
                .test(Residuals.LB2, niid.ljungBoxOnSquare())
                .test(Residuals.BP2, niid.boxPierceOnSquare())
                .test(Residuals.NRUNS, niid.runsNumber())
                .test(Residuals.LRUNS, niid.runsLength())
                .test(Residuals.NUDRUNS, niid.upAndDownRunsNumbber())
                .test(Residuals.LUDRUNS, niid.upAndDownRunsLength())
                .build();

        return RegSarimaModel.builder()
                .description(desc)
                .estimation(est)
                .residuals(residuals)
                .details(Details.builder()
                        .estimationDomain(description.getEstimationDomain())
                        .interpolatedSeries(interpolated)
                        .transformedSeries(transformed)
                        .independentResiduals(ll.e())
                        .regressionItems(regressionDesc)
                        .build())
                .build();
    }

    private final ConcurrentMap<String, Object> cache = new ConcurrentHashMap<String, Object>();

    @lombok.Singular
    private Map<String, Object> additionalResults;

    @lombok.Value
    public static class RegressionDesc {

        ITsVariable core;
        int item;
        int position;

        double coef, stderr, pvalue;

        public double getTStat() {
            return coef / stderr;
        }
    }

    @lombok.Value
    @lombok.Builder
    public static class Details {

        TsDomain estimationDomain;
        TsData interpolatedSeries, transformedSeries;
        DoubleSeq independentResiduals;
        List<RegressionDesc> regressionItems;
    }

    Description<SarimaSpec> description;
    Estimation estimation;
    Residuals residuals;
    Details details;

    public int getAnnualFrequency() {
        return description.getSeries().getAnnualFrequency();
    }

    public SarimaOrders specification() {
        return description.getStochasticComponent().orders();
    }

    public SarimaModel arima() {
        return SarimaModel.builder(description.getStochasticComponent())
                .build();
    }

    public RegArimaModel<SarimaModel> regarima() {

        Matrix X = estimation.getX();
        boolean mean = isMeanEstimation();

        RegArimaModel.Builder builder = RegArimaModel.<SarimaModel>builder()
                .y(estimation.getY())
                .arima(arima())
                .meanCorrection(mean);

        int start = mean ? 1 : 0;
        for (int i = start; i < X.getColumnsCount(); ++i) {
            builder.addX(X.column(i));
        }
        return builder.build();
    }

    public TsData interpolatedSeries(boolean bTransformed) {

        TsData data;
        if (bTransformed) {
            data = details.transformedSeries;
        } else {
            data = details.interpolatedSeries;
        }

        // complete for missings
        MissingValueEstimation[] missing = estimation.getMissing();
        int nmissing = missing.length;
        if (nmissing > 0) {
            double[] datac = data.getValues().toArray();
            if (bTransformed) {
                for (int i = 0; i < nmissing; ++i) {
                    datac[missing[i].getPosition()] = missing[i].getValue();
                }
            } else {
                for (int i = 0; i < nmissing; ++i) {
                    double m = missing[i].getValue();
                    int pos = missing[i].getPosition();
                    if (description.isLogTransformation()) {
                        datac[pos] = Math.exp(m);
                    } else {
                        datac[pos] = m;
                    }
                }
            }
            data = TsData.ofInternal(description.getSeries().getStart(), datac);
        }
        return data;
    }

    /**
     * Gets the effect of all the estimated regression variables
     *
     * @param domain
     * @param test
     * @return
     */
    public TsData regressionEffect(TsDomain domain, Predicate<Variable> test) {
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
     * Gets the effect of all pre-specified variables (including coefficient)
     *
     * @param domain
     * @param test
     * @return
     */
    public TsData preadjustmentEffect(TsDomain domain, Predicate<Variable> test) {
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
     * Gets the effect of all the variables (pre-specified or estimated)
     *
     * @param domain
     * @param test
     * @return
     */
    public TsData deterministicEffect(TsDomain domain, Predicate<Variable> test) {
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

    public boolean isMeanCorrection() {
        Variable[] variables = description.getVariables();
        for (int i = 0; i < variables.length; ++i) {
            if (variables[i].getCore() instanceof TrendConstant) {
                return true;
            }
        }
        return false;
    }

    public boolean isMeanEstimation() {
        Variable[] variables = description.getVariables();
        for (int i = 0; i < variables.length; ++i) {
            if (variables[i].getCore() instanceof TrendConstant) {
                return variables[i].isFree();
            }
        }
        return false;
    }

    public TsData linearizedSeries() {
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
    public TsData backTransform(TsData s, boolean includeLp) {
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
    public TsData transform(TsData s, boolean includeLp) {
        if (description.isLogTransformation()) {
            if (includeLp && description.getLengthOfPeriodTransformation() != LengthOfPeriodType.None) {
                s = Transformations.lengthOfPeriod(description.getLengthOfPeriodTransformation()).transform(s, null);
            }
            s = s.log();
        }
        return s;
    }

    public TsData fullResiduals() {
        DoubleSeq res = residuals.getRes();
        TsPeriod start = residuals.getStart();
        return TsData.of(start, res);
    }

    public int freeArimaParametersCount() {
        return description.getStochasticComponent().freeParametersCount();
    }

    /**
     * tde
     *
     * @param domain If the domain is null, the series domain is used
     * @return
     */
    public TsData getTradingDaysEffect(TsDomain domain) {
        if (domain == null) {
            domain = description.getSeries().getDomain();
        }
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isDaysRelated(v));
        return backTransform(s, true);
    }

    /**
     * ee
     *
     * @param domain If the domain is null, the series domain is used
     * @return
     */
    public TsData getEasterEffect(TsDomain domain) {
        if (domain == null) {
            domain = description.getSeries().getDomain();
        }
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isEaster(v));
        return backTransform(s, false);
    }

    /**
     * mhe
     *
     * @param domain If the domain is null, the series domain is used
     * @return
     */
    public TsData getMovingHolidayEffect(TsDomain domain) {
        if (domain == null) {
            domain = description.getSeries().getDomain();
        }
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isMovingHoliday(v));
        return backTransform(s, false);
    }

    /**
     * rmde
     *
     * @param domain If the domain is null, the series domain is used
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
     * @param domain If the domain is null, the series domain is used
     * @return
     */
    public TsData getOutliersEffect(TsDomain domain) {
        if (domain == null) {
            domain = description.getSeries().getDomain();
        }
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
    public TsData getOutliersEffect(TsDomain domain, boolean ami) {
        if (domain == null) {
            domain = description.getSeries().getDomain();
        }
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isOutlier(v, ami));
        return backTransform(s, false);
    }

    /**
     * cal
     *
     * @param domain If the domain is null, the series domain is used
     * @return
     */
    public TsData getCalendarEffect(TsDomain domain) {
        if (domain == null) {
            domain = description.getSeries().getDomain();
        }
        TsData s = deterministicEffect(domain, v -> ModellingUtility.isCalendar(v));
        return backTransform(s, true);
    }

    /**
     * Gets all the deterministic effects, except mean correction
     *
     * @param domain If the domain is null, the series domain is used
     * @return
     */
    public TsData getDeterministicEffect(TsDomain domain) {
        if (domain == null) {
            domain = description.getSeries().getDomain();
        }
        TsData s = deterministicEffect(domain, v -> !(v.getCore() instanceof TrendConstant));
        return backTransform(s, false);
    }

    public TsData getDeterministicEffect(TsDomain domain, Predicate<Variable> test) {
        if (domain == null) {
            domain = description.getSeries().getDomain();
        }
        TsData s = deterministicEffect(domain, v -> test.test(v));
        return backTransform(s, false);
    }

    public TsData getPreadjustmentEffect(TsDomain domain, Predicate<Variable> test) {
        if (domain == null) {
            domain = description.getSeries().getDomain();
        }
        TsData s = preadjustmentEffect(domain, v -> test.test(v));
        return backTransform(s, true);
    }

    public TsData getRegressionEffect(TsDomain domain, Predicate<Variable> test) {
        if (domain == null) {
            domain = description.getSeries().getDomain();
        }
        TsData s = regressionEffect(domain, v -> test.test(v));
        return backTransform(s, true);
    }

    /**
     * The forecast domain is relative to the series domain, not to the
     * estimation domain
     *
     * @param nfcast
     * @return
     */
    public TsDomain forecastDomain(int nfcast) {
        if (nfcast < 0) {
            nfcast = (-nfcast) * getAnnualFrequency();
        }

        return TsDomain.of(description.getSeries().getDomain().getEndPeriod(), nfcast);
    }

    public Forecasts forecasts(int nf) {
        if (nf < 0) {
            nf = (-nf) * getAnnualFrequency();
        }
        String key = "forecasts" + nf;
        Forecasts fcasts = (Forecasts) cache.get(key);
        if (fcasts == null) {
            fcasts = internalForecasts(nf);
            cache.put(key, fcasts);
        }
        return fcasts;
    }

    private Forecasts internalForecasts(int nf) {
        RegArimaForecasts.Result fcasts;
        DoubleSeq b = getEstimation().getCoefficients();
        LikelihoodStatistics ll = getEstimation().getStatistics();
        double sig2 = ll.getSsqErr() / (ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount() + 1);
        TsDomain edom = getDetails().getEstimationDomain();
        if (b.isEmpty()) {
            fcasts = RegArimaForecasts.calcForecast(arima(),
                    getEstimation().originalY(), nf, sig2);
        } else {
            Variable[] variables = getDescription().getVariables();
            TsDomain xdom = edom.extend(0, nf);
            FastMatrix matrix = Regression.matrix(xdom, Arrays.stream(variables).map(v -> v.getCore()).toArray(n -> new ITsVariable[n]));
            fcasts = RegArimaForecasts.calcForecast(arima(),
                    getEstimation().originalY(), matrix,
                    b, getEstimation().getCoefficientsCovariance(), sig2);
        }
        TsPeriod fstart = edom.getEndPeriod();
        double[] f = fcasts.getForecasts();
        double[] ef = fcasts.getForecastsStdev();

        TsData tf = TsData.ofInternal(fstart, f);
        TsData fy = backTransform(tf, true);
        TsData efy;
        if (getDescription().isLogTransformation()) {
            double[] e = new double[nf];
            for (int i = 0; i < nf; ++i) {
                e[i] = LogNormal.stdev(f[i], ef[i]);
            }
            efy = TsData.ofInternal(fstart, e);
        } else {
            efy = TsData.ofInternal(fstart, ef);
        }
        return new Forecasts(TsData.ofInternal(fstart, f), TsData.ofInternal(fstart, ef), fy, efy);
    }

    /**
     * The backcast domain is relative to the series domain, not to
     * the
     * estimation domain
     *
     * @param nbcast
     * @return
     */
    public TsDomain backcastDomain(int nbcast) {
        if (nbcast < 0) {
            nbcast = (-nbcast) * getAnnualFrequency();
        }
        TsPeriod start = description.getSeries().getDomain().getStartPeriod().plus(-nbcast);
        return TsDomain.of(start, nbcast);
    }

    /**
     * Add/multiply series
     *
     * @param l
     * @param r
     * @return
     */
    public TsData op(TsData l, TsData... r) {
        if (description.isLogTransformation()) {
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
    public TsData inv_op(TsData l, TsData r) {
        if (description.isLogTransformation()) {
            return TsData.divide(l, r);
        } else {
            return TsData.subtract(l, r);
        }
    }

    public RegressionItem regressionItem(Predicate<ITsVariable> pred, int item) {
        List<RegressionDesc> items = details.getRegressionItems();
        int curitem = 0;
        for (RegressionDesc desc : items) {
            if (pred.test(desc.getCore())) {
                if (item == curitem) {
                    return new RegressionItem(desc.core.description(desc.item, details.estimationDomain), desc.coef, desc.stderr, desc.pvalue);
                } else {
                    ++curitem;
                }
            }
        }
        return null;
    }

    public IFunction likelihoodFunction() {
        RegArmaModel<SarimaModel> regarima = regarima().differencedModel();
        IArimaMapping<SarimaModel> mapping = mapping();
        return RegArmaFunction.<SarimaModel>builder(regarima.getY())
                .likelihoodEvaluation(DefaultLikelihoodEvaluation.ml())
                .variables(regarima.getX())
                .mapping(mapping().stationaryMapping())
                .missingCount(regarima.getMissingCount())
                .build();
    }

    public IArimaMapping<SarimaModel> mapping() {
        SarimaSpec arima = description.getStochasticComponent();
        if (arima.hasFixedParameters()) {
            int n = arima.getP() + arima.getBp() + arima.getQ() + arima.getBq();
            double[] p = new double[n];
            boolean[] b = new boolean[n];
            int j = 0;
            Parameter[] P = arima.getPhi();
            for (int i = 0; i < P.length; ++i, ++j) {
                p[j] = P[i].getValue();
                b[j] = P[i].isFixed();
            }
            P = arima.getTheta();
            for (int i = 0; i < P.length; ++i, ++j) {
                p[j] = P[i].getValue();
                b[j] = P[i].isFixed();
            }
            P = arima.getBtheta();
            for (int i = 0; i < P.length; ++i, ++j) {
                p[j] = P[i].getValue();
                b[j] = P[i].isFixed();
            }
            return new SarimaFixedMapping(specification(), DoubleSeq.of(p), b);
        } else {
            return SarimaMapping.of(specification());
        }
    }

    @lombok.Value
    public static class Forecasts {

        TsData rawForecasts, rawForecastsStdev;
        TsData forecasts, forecastsStdev;
    }

}
