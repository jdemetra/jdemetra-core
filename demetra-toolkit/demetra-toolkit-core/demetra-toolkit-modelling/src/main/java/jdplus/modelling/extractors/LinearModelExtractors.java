/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.extractors;

import demetra.information.BasicInformationExtractor;
import demetra.information.DynamicMapping;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.IEasterVariable;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.IMovingHolidayVariable;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TransitoryChange;
import demetra.timeseries.regression.TrendConstant;
import demetra.timeseries.regression.Variable;
import jdplus.modelling.Residuals;
import java.util.Arrays;
import java.util.function.Predicate;
import nbbrd.service.ServiceProvider;
import demetra.math.matrices.Matrix;
import demetra.toolkit.dictionaries.RegressionDictionaries;
import jdplus.stats.likelihood.LikelihoodStatistics;
import jdplus.modelling.GeneralLinearModel;

/**
 *
 * Contains all the descriptors of a linear model, except additional information
 * and information related to the (generic) stochastic model
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class LinearModelExtractors {

    @ServiceProvider(InformationExtractor.class)
    public static class Specific extends InformationMapping<GeneralLinearModel> {

        public static final int IMEAN = 0, ITD = 10, ILP = 11, IEASTER = 12,
                AO = 20, LS = 21, TC = 22, SO = 23, IOUTLIER = 29,
                IIV = 30, IRAMP = 40, IOTHER = 50;

        public final String LOG = "log",
                ADJUST = "adjust", MEAN = "mean",
                SPAN = "span", START = "start", END = "end", N = "n", NM = "missing", PERIOD = "period",
                REGRESSION = "regression", LIKELIHOOD = "likelihood", MAX = "max", RESIDUALS = "residuals",
                NTD = "ntd", NLP = "nlp", NMH = "nmh", NEASTER = "neaster",
                NOUT = "nout", NOUTAO = "noutao", NOUTLS = "noutls", NOUTTC = "nouttc", NOUTSO = "noutso",
                COEFF = "coefficients", COVAR = "covar", COVAR_ML = "covar-ml", COEFFDESC = "description", REGTYPE = "type",
                P = "parameters", PCOVAR = "pcovar", PCOVAR_ML = "pcovar-ml", PCORR = "pcorr", SCORE = "pscore";

        public Specific() {
            set(RegressionDictionaries.Y, TsData.class, source -> source.getDescription().getSeries());
            set(PERIOD, Integer.class, source -> source.getDescription().getSeries().getAnnualFrequency());
            set(BasicInformationExtractor.concatenate(SPAN, START), TsPeriod.class, source -> source.getDescription().getSeries().getStart());
            set(BasicInformationExtractor.concatenate(SPAN, END), TsPeriod.class, source -> source.getDescription().getSeries().getDomain().getLastPeriod());
            set(BasicInformationExtractor.concatenate(SPAN, N), Integer.class, source -> source.getDescription().getSeries().length());
            set(BasicInformationExtractor.concatenate(SPAN, NM), Integer.class, source -> source.getEstimation().getMissing().length);

            set(LOG, Boolean.class, source -> source.getDescription().isLogTransformation());
            set(ADJUST, String.class, source -> source.getDescription().getLengthOfPeriodTransformation().name());
            set(BasicInformationExtractor.concatenate(REGRESSION, MEAN), Boolean.class,
                    source -> count(source, v -> v instanceof TrendConstant) == 1);
            set(BasicInformationExtractor.concatenate(REGRESSION, NLP), Integer.class,
                    source -> count(source, v -> v instanceof ILengthOfPeriodVariable));
            set(BasicInformationExtractor.concatenate(REGRESSION, NTD), Integer.class,
                    source -> count(source, v -> v instanceof ITradingDaysVariable));
            set(BasicInformationExtractor.concatenate(REGRESSION, NEASTER), Integer.class,
                    source -> count(source, v -> v instanceof IEasterVariable));
            set(BasicInformationExtractor.concatenate(REGRESSION, NMH), Integer.class, source -> count(source, v -> v instanceof IMovingHolidayVariable));
            set(BasicInformationExtractor.concatenate(REGRESSION, NOUT), Integer.class, source -> count(source, v -> v instanceof IOutlier));
            set(BasicInformationExtractor.concatenate(REGRESSION, NOUTAO), Integer.class, source -> count(source, v -> v instanceof AdditiveOutlier));
            set(BasicInformationExtractor.concatenate(REGRESSION, NOUTLS), Integer.class, source -> count(source, v -> v instanceof LevelShift));
            set(BasicInformationExtractor.concatenate(REGRESSION, NOUTTC), Integer.class, source -> count(source, v -> v instanceof TransitoryChange));
            set(BasicInformationExtractor.concatenate(REGRESSION, NOUTSO), Integer.class, source -> count(source, v -> v instanceof PeriodicOutlier));

            set(BasicInformationExtractor.concatenate(REGRESSION, COEFFDESC), String[].class, source -> {
                TsDomain domain = source.getDescription().getSeries().getDomain();
                Variable[] vars = source.getDescription().getVariables();
                if (vars.length == 0) {
                    return null;
                }
                int n = Arrays.stream(vars).mapToInt(var -> var.dim()).sum();
                String[] nvars = new String[n];
                for (int i = 0, j = 0; i < vars.length; ++i) {
                    int m = vars[i].dim();
                    if (m == 1) {
                        nvars[j++] = vars[i].getCore().description(domain);
                    } else {
                        for (int k = 0; k < m; ++k) {
                            nvars[j++] = vars[i].getCore().description(k, domain);
                        }
                    }
                }
                return nvars;
            });
            set(BasicInformationExtractor.concatenate(REGRESSION, REGTYPE), int[].class, (GeneralLinearModel source) -> {
                Variable[] vars = source.getDescription().getVariables();
                if (vars.length == 0) {
                    return null;
                }
                int n = Arrays.stream(vars).mapToInt(var -> var.dim()).sum();
                int[] tvars = new int[n];
                for (int i = 0, j = 0; i < vars.length; ++i) {
                    int m = vars[i].dim();
                    int type = type(vars[i].getCore());
                    for (int k = 0; k < m; ++k) {
                        tvars[j++] = type;
                    }
                }
                return tvars;
            });
 
            set(BasicInformationExtractor.concatenate(REGRESSION, COEFF), double[].class, source -> source.getEstimation().getCoefficients().toArray());
            set(BasicInformationExtractor.concatenate(REGRESSION, COVAR), Matrix.class, source -> source.getEstimation().getCoefficientsCovariance());
            set(BasicInformationExtractor.concatenate(REGRESSION, COVAR_ML), Matrix.class, source
                    -> mul(source.getEstimation().getCoefficientsCovariance(), mlcorrection(source.getEstimation().getStatistics())));
            set(BasicInformationExtractor.concatenate(MAX, P), double[].class, source -> source.getEstimation().getParameters().getValues().toArray());
            set(BasicInformationExtractor.concatenate(MAX, PCOVAR), Matrix.class, source -> source.getEstimation().getParameters().getCovariance());
            set(BasicInformationExtractor.concatenate(MAX, PCOVAR_ML), Matrix.class, source
                    -> mul(source.getEstimation().getParameters().getCovariance(), mlcorrection(source.getEstimation().getStatistics())));
            set(BasicInformationExtractor.concatenate(MAX, SCORE), double[].class, source -> source.getEstimation().getParameters().getScores().toArray());
            delegate(LIKELIHOOD, LikelihoodStatistics.class, source -> source.getEstimation().getStatistics());
            delegate(RESIDUALS, Residuals.class, source -> source.getResiduals());
        }

        private double mlcorrection(LikelihoodStatistics ll) {
            double n = ll.getEffectiveObservationsCount();
            return (n - ll.getEstimatedParametersCount()) / n;
        }

        private Matrix mul(Matrix m, double c) {
            double[] cm = m.toArray();
            for (int i = 0; i < cm.length; ++i) {
                cm[i] *= c;
            }
            return Matrix.of(cm, m.getRowsCount(), m.getColumnsCount());
        }

        private int type(ITsVariable var) {
            if (var instanceof TrendConstant) {
                return IMEAN;
            }
            if (var instanceof ITradingDaysVariable) {
                return ITD;
            }
            if (var instanceof ILengthOfPeriodVariable) {
                return ILP;
            }
            if (var instanceof IEasterVariable) {
                return IEASTER;
            }
            if (var instanceof IOutlier) {
                switch (((IOutlier) var).getCode()) {
                    case AdditiveOutlier.CODE:
                        return AO;
                    case LevelShift.CODE:
                        return LS;
                    case TransitoryChange.CODE:
                        return TC;
                    case PeriodicOutlier.CODE:
                    case PeriodicOutlier.PO:
                        return SO;
                    default:
                        return IOUTLIER;
                }
            }
            if (var instanceof InterventionVariable) {
                return IIV;
            }
            if (var instanceof Ramp) {
                return IRAMP;
            }
            return IOTHER;
        }

        @Override
        public Class getSourceClass() {
            return GeneralLinearModel.class;
        }

        @Override
        public int getPriority() {
            return 1;
        }

        private int countVar(GeneralLinearModel source, Predicate<Variable> pred) {
            Variable[] variables = source.getDescription().getVariables();
            int n = 0;
            for (int i = 0; i < variables.length; ++i) {
                if (pred.test(variables[i])) {
                    n += variables[i].dim();
                }
            }
            return n;

        }

        private int count(GeneralLinearModel source, Predicate<ITsVariable> pred) {
            Variable[] variables = source.getDescription().getVariables();
            int n = 0;
            for (int i = 0; i < variables.length; ++i) {
                ITsVariable core = variables[i].getCore();
                if (pred.test(core)) {
                    n += core.dim();
                }
            }
            return n;
        }

    }

    @ServiceProvider(InformationExtractor.class)
    public static class Dynamic extends DynamicMapping<GeneralLinearModel, Object> {

        public Dynamic() {
            super(null, v -> v.getAdditionalResults());
        }

        @Override
        public Class<GeneralLinearModel> getSourceClass() {
            return GeneralLinearModel.class;
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

}
