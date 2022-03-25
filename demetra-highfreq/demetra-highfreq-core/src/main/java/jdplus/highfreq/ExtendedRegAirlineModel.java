/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.highfreq;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.Doubles;
import demetra.data.Parameter;
import demetra.data.ParametersEstimation;
import demetra.highfreq.ExtendedAirlineSpec;
import demetra.information.GenericExplorable;
import demetra.processing.ProcessingLog;
import demetra.stats.ProbabilityType;
import demetra.timeseries.regression.Constant;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.MissingValueEstimation;
import demetra.timeseries.regression.ResidualsType;
import demetra.timeseries.regression.Variable;
import demetra.toolkit.dictionaries.ResidualsDictionaries;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jdplus.arima.ArimaModel;
import jdplus.dstats.T;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.GeneralLinearModel;
import jdplus.modelling.LightweightLinearModel;
import jdplus.modelling.Residuals;
import jdplus.modelling.regression.RegressionDesc;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.RegArimaUtility;
import jdplus.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.stats.likelihood.LogLikelihoodFunction;
import jdplus.stats.tests.NiidTests;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder
public class ExtendedRegAirlineModel implements GeneralLinearModel<ExtendedAirlineSpec>, GenericExplorable {

    private static final MissingValueEstimation[] NOMISSING = new MissingValueEstimation[0];

    public static ExtendedRegAirlineModel of(ModelDescription description, RegArimaEstimation<ArimaModel> estimation, ProcessingLog log) {

        ExtendedAirlineSpec stochasticSpec = description.getStochasticSpec();
        int free = stochasticSpec.freeParametersCount();
        RegArimaModel<ArimaModel> model = estimation.getModel();
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
            ITsVariable cur = Constant.C;
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
        

        LightweightLinearModel.Description desc = LightweightLinearModel.Description.<ExtendedAirlineSpec>builder()
                .series(description.getSeries())
                .logTransformation(description.isLogTransformation())
                .variables(variables)
                .stochasticComponent(stochasticSpec)
                .build();

        LogLikelihoodFunction.Point<RegArimaModel<ArimaModel>, ConcentratedLikelihoodWithMissing> max = estimation.getMax();
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
                .test(ResidualsDictionaries.NRUNS, niid.runsNumber())
                .test(ResidualsDictionaries.LRUNS, niid.runsLength())
                .test(ResidualsDictionaries.NUDRUNS, niid.upAndDownRunsNumbber())
                .test(ResidualsDictionaries.LUDRUNS, niid.upAndDownRunsLength())
                .build();

        return ExtendedRegAirlineModel.builder()
                .description(desc)
                .estimation(est)
                .residuals(residuals)
                .regressionItems(regressionDesc)
                .independentResiduals(ll.e())
                .build();
    }
    Description<ExtendedAirlineSpec> description;

    Estimation estimation;

    Residuals residuals;

    DoubleSeq independentResiduals;
    List<RegressionDesc> regressionItems;

}
