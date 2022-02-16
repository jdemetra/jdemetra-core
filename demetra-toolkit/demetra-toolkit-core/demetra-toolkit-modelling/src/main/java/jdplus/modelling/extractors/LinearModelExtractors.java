/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.extractors;

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
import demetra.toolkit.dictionaries.Dictionary;
import demetra.toolkit.dictionaries.RegArimaDictionaries;
import demetra.toolkit.dictionaries.RegressionDictionaries;
import demetra.toolkit.dictionaries.UtilityDictionaries;
import jdplus.stats.likelihood.LikelihoodStatistics;
import jdplus.modelling.GeneralLinearModel;

/**
 *
 * Contains all the descriptors of a linear model, except information related to the stochastic model
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class LinearModelExtractors {
    

    @ServiceProvider(InformationExtractor.class)
    public static class Default extends InformationMapping<GeneralLinearModel> {

        public static final int IMEAN = 0, ITD = 10, ILP = 11, IEASTER = 12,
                AO = 20, LS = 21, TC = 22, SO = 23, IOUTLIER = 29,
                IIV = 30, IRAMP = 40, IOTHER = 50;


        private String regressionItem(String key){
            return Dictionary.concatenate(RegArimaDictionaries.REGRESSION, key);
        }

        private String advancedItem(String key){
            return Dictionary.concatenate(RegArimaDictionaries.ADVANCED, key);
        }

        private String mlItem(String key){
            return Dictionary.concatenate(RegArimaDictionaries.MAX, key);
        }

        public Default() {
            set(RegressionDictionaries.Y, TsData.class, source -> source.getDescription().getSeries());
            set(RegressionDictionaries.PERIOD, Integer.class, source -> source.getDescription().getSeries().getAnnualFrequency());
            set(RegressionDictionaries.SPAN_START, TsPeriod.class, source -> source.getDescription().getSeries().getStart());
            set(RegressionDictionaries.SPAN_END, TsPeriod.class, source -> source.getDescription().getSeries().getDomain().getLastPeriod());
            set(RegressionDictionaries.SPAN_N, Integer.class, source -> source.getDescription().getSeries().length());
            set(RegressionDictionaries.SPAN_MISSING, Integer.class, source -> source.getEstimation().getMissing().length);

            set(RegressionDictionaries.LOG, Integer.class, source -> source.getDescription().isLogTransformation()? 1 : 0);
            set(RegressionDictionaries.ADJUST, String.class, source -> source.getDescription().getLengthOfPeriodTransformation().name());

            set(regressionItem(RegressionDictionaries.COEFFDESC), String[].class, source -> {
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
            set(regressionItem(RegressionDictionaries.REGTYPE), int[].class, (GeneralLinearModel source) -> {
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
 
            set(advancedItem(RegressionDictionaries.COEFF), double[].class, source -> source.getEstimation().getCoefficients().toArray());
            set(advancedItem(RegressionDictionaries.COVAR), Matrix.class, source -> source.getEstimation().getCoefficientsCovariance());
            set(advancedItem(RegressionDictionaries.COVAR_ML), Matrix.class, source
                    -> mul(source.getEstimation().getCoefficientsCovariance(), mlcorrection(source.getEstimation().getStatistics())));
            set(mlItem(UtilityDictionaries.P), double[].class, source -> source.getEstimation().getParameters().getValues().toArray());
            set(mlItem(UtilityDictionaries.PCOVAR), Matrix.class, source -> source.getEstimation().getParameters().getCovariance());
            set(mlItem(UtilityDictionaries.PCOVAR_ML), Matrix.class, source
                    -> mul(source.getEstimation().getParameters().getCovariance(), mlcorrection(source.getEstimation().getStatistics())));
            set(mlItem(UtilityDictionaries.SCORE), double[].class, source -> source.getEstimation().getParameters().getScores().toArray());
            delegate(RegArimaDictionaries.LIKELIHOOD, LikelihoodStatistics.class, source -> source.getEstimation().getStatistics());
            delegate(RegArimaDictionaries.RESIDUALS, Residuals.class, source -> source.getResiduals());
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

}
