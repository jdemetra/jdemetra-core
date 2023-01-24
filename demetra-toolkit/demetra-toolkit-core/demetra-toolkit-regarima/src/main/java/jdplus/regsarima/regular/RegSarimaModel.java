/*
 * Copyright 2023 National Bank of Belgium
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
import demetra.information.GenericExplorable;
import jdplus.stats.likelihood.LikelihoodStatistics;
import demetra.timeseries.regression.MissingValueEstimation;
import demetra.data.ParametersEstimation;
import demetra.math.matrices.Matrix;
import demetra.processing.ProcessingLog;
import demetra.stats.ProbabilityType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.TrendConstant;
import demetra.timeseries.regression.Variable;
import demetra.timeseries.regression.RegressionItem;
import demetra.timeseries.regression.ResidualsType;
import demetra.toolkit.dictionaries.ResidualsDictionaries;
import demetra.util.Arrays2;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.data.DataBlockIterator;
import jdplus.dstats.LogNormal;
import jdplus.dstats.T;
import jdplus.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.stats.likelihood.LogLikelihoodFunction;
import jdplus.math.functions.IFunction;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.GeneralLinearModel;
import jdplus.modelling.LightweightLinearModel;
import jdplus.modelling.Residuals;
import jdplus.modelling.regression.RegressionDesc;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaForecasts;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.RegArimaUtility;
import jdplus.regarima.RegArmaModel;
import jdplus.regarima.estimation.RegArmaFunction;
import jdplus.sarima.SarimaModel;
import jdplus.sarima.estimation.SarimaFixedMapping;
import jdplus.sarima.estimation.SarimaMapping;
import jdplus.stats.likelihood.DefaultLikelihoodEvaluation;
import jdplus.stats.tests.NiidTests;

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
            regressionDesc.add(new RegressionDesc("const", cur, 0, pos++, c, e, 2 * tstat.getProbability(Math.abs(c / e), ProbabilityType.Upper)));
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
                    if (e == 0) {
                        p[j] = Parameter.zero();
                        regressionDesc.add(new RegressionDesc(var.getName(), var.getCore(), j, pos++, 0, 0, 0));
                    } else {
                        p[j] = Parameter.estimated(c);
                        regressionDesc.add(new RegressionDesc(var.getName(), var.getCore(), j, pos++, c, e, 2 * tstat.getProbability(Math.abs(c / e), ProbabilityType.Upper)));
                    }
                }
                variables[k++] = var.withCoefficients(p);
            } else if (nfree > 0) {
                Parameter[] p = var.getCoefficients();
                for (int j = 0; j < p.length; ++j) {
                    if (p[j].isFree()) {
                        double c = cursor.getAndNext(), e = Math.sqrt(diag.getAndNext() * vscale);
                        p[j] = Parameter.estimated(c);
                        regressionDesc.add(new RegressionDesc(var.getName(), var.getCore(), j, pos++, c, e, 2 * tstat.getProbability(Math.abs(c / e), ProbabilityType.Upper)));
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
            DoubleSeq y = model.getY();
            missing = new MissingValueEstimation[nmissing];
            DoubleSeqCursor cur = ll.missingCorrections().cursor();
            DoubleSeqCursor vcur = ll.missingUnscaledVariances().cursor();
            int[] pmissing = model.missing();
            for (int i = 0; i < nmissing; ++i) {
                double m = cur.getAndNext();
                double v = vcur.getAndNext();
                missing[i] = new MissingValueEstimation(pmissing[i], y.get(pmissing[i]) - m, Math.sqrt(v * vscale));
            }
        }
        DoubleSeq fullRes = RegArimaUtility.fullResiduals(model, ll);
        LightweightLinearModel.Estimation est = LightweightLinearModel.Estimation.builder()
                .domain(description.getEstimationDomain())
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
                .type(ResidualsType.FullResiduals)
                .res(fullRes)
                .start(description.getEstimationDomain().getEndPeriod().plus(-fullRes.length()))
                .test(ResidualsDictionaries.MEAN, niid.meanTest())
                .test(ResidualsDictionaries.SKEW, niid.skewness())
                .test(ResidualsDictionaries.KURT, niid.kurtosis())
                .test(ResidualsDictionaries.DH, niid.normalityTest())
                .test(ResidualsDictionaries.LB, niid.ljungBox())
                .test(ResidualsDictionaries.BP, niid.boxPierce())
                .test(ResidualsDictionaries.SEASLB, niid.seasonalLjungBox())
                .test(ResidualsDictionaries.SEASBP, niid.seasonalBoxPierce())
                .test(ResidualsDictionaries.LB2, niid.ljungBoxOnSquare())
                .test(ResidualsDictionaries.BP2, niid.boxPierceOnSquare())
                .test(ResidualsDictionaries.NRUNS, niid.runsNumber())
                .test(ResidualsDictionaries.LRUNS, niid.runsLength())
                .test(ResidualsDictionaries.NUDRUNS, niid.upAndDownRunsNumbber())
                .test(ResidualsDictionaries.LUDRUNS, niid.upAndDownRunsLength())
                .build();

        return RegSarimaModel.builder()
                .description(desc)
                .estimation(est)
                .residuals(residuals)
                .details(Details.builder()
                        .independentResiduals(ll.e())
                        .regressionItems(regressionDesc)
                        .build())
                .build();
    }

    private final ConcurrentMap<String, Object> cache = new ConcurrentHashMap<>();

    @lombok.Singular
    private Map<String, Object> additionalResults;

    @lombok.Value
    @lombok.Builder
    public static class Details {

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

    public TsData fullResiduals() {
        DoubleSeq res = residuals.getRes();
        TsPeriod start = residuals.getStart();
        return TsData.of(start, res);
    }

    public int freeArimaParametersCount() {
        return description.getStochasticComponent().freeParametersCount();
    }

    public IFunction likelihoodFunction() {
        RegArmaModel<SarimaModel> regarima = regarima().differencedModel();
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

    public Forecasts backcasts(int nb) {
        if (nb < 0) {
            nb = (-nb) * getAnnualFrequency();
        }
        String key = "backcasts" + nb;
        Forecasts bcasts = (Forecasts) cache.get(key);
        if (bcasts == null) {
            bcasts = internalBackcasts(nb);
            cache.put(key, bcasts);
        }
        return bcasts;
    }
    
    private TsData regY(){
        TsData s=transformedSeries();
        TsData preadjust=this.preadjustmentEffect(s.getDomain(), v->true);
        return TsData.subtract(s, preadjust);
    }

    private Forecasts internalForecasts(int nf) {
        TsDomain dom = this.getDescription().getDomain();
        if (nf == 0) {
            TsData empty = TsData.of(dom.getEndPeriod(), DoubleSeq.empty());
            return new Forecasts(empty, empty, empty, empty);
        }
        
        RegArimaForecasts.Result fcasts;
        DoubleSeq b = getEstimation().getCoefficients();
        LikelihoodStatistics ll = getEstimation().getStatistics();
        double sig2 = ll.getSsqErr() / (ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount());
        TsDomain xdom = dom.extend(0, nf);
        if (b.isEmpty()) {
            fcasts = RegArimaForecasts.calcForecast(arima(), regY().getValues(), nf, sig2);
        } else {
            FastMatrix matrix = regressionMatrix(xdom);
            fcasts = RegArimaForecasts.calcForecast(arima(),
                    regY().getValues(), matrix,
                    b, getEstimation().getCoefficientsCovariance(), sig2);
        }
        TsPeriod fstart = dom.getEndPeriod();
        double[] f = fcasts.getForecasts();
        double[] ef = fcasts.getForecastsStdev();

        TsData tf = TsData.ofInternal(fstart, f);
        tf = TsData.add(tf, preadjustmentEffect(xdom, v -> true));
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

    private Forecasts internalBackcasts(int nb) {
        // we forecast to the past. Reverse everything
        TsDomain dom = getDescription().getDomain();
        if (nb == 0) {
            TsData empty = TsData.of(dom.getStartPeriod(), DoubleSeq.empty());
            return new Forecasts(empty, empty, empty, empty);
        }
        RegArimaForecasts.Result bcasts;
        DoubleSeq b = getEstimation().getCoefficients();
        LikelihoodStatistics ll = getEstimation().getStatistics();
        double sig2 = ll.getSsqErr() / (ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount());
        TsDomain xdom = dom.extend(nb, 0);
        if (b.isEmpty()) {
            bcasts = RegArimaForecasts.calcForecast(arima(), regY().getValues().reverse(), nb, sig2);
        } else {
            FastMatrix matrix = regressionMatrix(xdom);
            // reverse the matrix
            FastMatrix rmatrix = FastMatrix.make(matrix.getRowsCount(), matrix.getColumnsCount());
            DataBlockIterator iter = matrix.columnsIterator(), riter = rmatrix.columnsIterator();
            while (iter.hasNext()) {
                riter.next().copy(iter.next().reverse());
            }
            bcasts = RegArimaForecasts.calcForecast(arima(),
                    regY().getValues().reverse(), matrix,
                    b, getEstimation().getCoefficientsCovariance(), sig2);
        }
        TsPeriod bstart = dom.getStartPeriod().plus(-nb);
        double[] f = bcasts.getForecasts();
        double[] ef = bcasts.getForecastsStdev();
        Arrays2.reverse(f);
        Arrays2.reverse(ef);

        TsData tb = TsData.ofInternal(bstart, f);
        tb = TsData.add(tb, preadjustmentEffect(xdom, v -> true));
        TsData by = backTransform(tb, true);
        TsData eby;
        if (getDescription().isLogTransformation()) {
            double[] e = new double[nb];
            for (int i = 0; i < nb; ++i) {
                e[i] = LogNormal.stdev(f[i], ef[i]);
            }
            eby = TsData.ofInternal(bstart, e);
        } else {
            eby = TsData.ofInternal(bstart, ef);
        }
        return new Forecasts(TsData.ofInternal(bstart, f), TsData.ofInternal(bstart, ef), by, eby);
    }

    @lombok.Value
    public static class Forecasts {

        TsData rawForecasts, rawForecastsStdev;
        TsData forecasts, forecastsStdev;
    }

    public RegressionItem regressionItem(Predicate<ITsVariable> pred, int item) {
        List<RegressionDesc> items = details.getRegressionItems();
        int curitem = 0;
        for (RegressionDesc desc : items) {
            if (pred.test(desc.getCore())) {
                if (item == curitem) {
                    return new RegressionItem(desc.getCoef(), desc.getStderr(), desc.getPvalue(), desc.getCore().description(desc.getItem(), estimation.getDomain()));
                } else {
                    ++curitem;
                }
            }
        }
        return null;
    }
}
