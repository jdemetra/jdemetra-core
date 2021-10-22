/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.regarima.extractors;

import demetra.information.InformationMapping;
import demetra.timeseries.TsPeriod;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.regsarima.regular.RegSarimaModel;
import demetra.information.BasicInformationExtractor;
import demetra.information.InformationDelegate;
import demetra.information.InformationExtractor;
import demetra.modelling.ModellingDictionary;
import demetra.modelling.SeriesInfo;
import demetra.modelling.implementations.SarimaSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.IEasterVariable;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TrendConstant;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import demetra.timeseries.regression.modelling.RegressionItem;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;
import demetra.math.matrices.Matrix;

/**
 *
 * Contains all the descriptors of a linear model, except additional information
 * and information related to the (generic) stochastic model
 *
 * @author palatej
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class RegSarimaModelExtractors {

    public final int NFCAST = -2, NBCAST = -2;

    @ServiceProvider(InformationExtractor.class)
    public static class Specific extends InformationMapping<RegSarimaModel> {

        public final String SARIMA = "arima",
                ESPAN = "espan", START = "start", END = "end", N = "n", NM = "missing", PERIOD = "period",
                REGRESSION = "regression", LIKELIHOOD = "likelihood", MAX = "max",
                OUTLIERS = "outlier(*)",
                CALENDAR = "calendar(*)",
                EASTER = "easter",
                TD = "td", TD1 = "td(1)", TD2 = "td(2)", TD3 = "td(3)", TD4 = "td(4)", TD5 = "td(5)", TD6 = "td(6)", TD7 = "td(7)",
                TD8 = "td(8)", TD9 = "td(9)", TD10 = "td(10)", TD11 = "td(11)", TD12 = "td(12)", TD13 = "td(13)", TD14 = "td(14)",
                MU = "mu", LP = "lp", OUT = "out", OUT1 = "out(1)", OUT2 = "out(2)", OUT3 = "out(3)", OUT4 = "out(4)", OUT5 = "out(5)", OUT6 = "out(6)", OUT7 = "out(7)",
                OUT8 = "out(8)", OUT9 = "out(9)", OUT10 = "out(10)", OUT11 = "out(11)", OUT12 = "out(12)", OUT13 = "out(13)", OUT14 = "out(14)",
                OUT15 = "out(15)", OUT16 = "out(16)", OUT17 = "out(17)", OUT18 = "out(18)", OUT19 = "out(19)", OUT20 = "out(20)",
                OUT21 = "out(21)", OUT22 = "out(22)", OUT23 = "out(23)", OUT24 = "out(24)", OUT25 = "out(25)", OUT26 = "out(26)",
                OUT27 = "out(27)", OUT28 = "out(28)", OUT29 = "out(29)", OUT30 = "out(30)", RAMP = "ramp",
                PCORR = "pcorr";

        public Specific() {
            delegate(SARIMA, SarimaSpec.class, source
                    -> source.getDescription().getStochasticComponent());

            set(BasicInformationExtractor.concatenate(ESPAN, START), TsPeriod.class, source -> source.getDetails().getEstimationDomain().getStartPeriod());
            set(BasicInformationExtractor.concatenate(ESPAN, END), TsPeriod.class, source -> source.getDetails().getEstimationDomain().getLastPeriod());
            set(BasicInformationExtractor.concatenate(ESPAN, N), Integer.class, source -> source.getDetails().getEstimationDomain().getLength());

//        set(ModellingDictionary.Y + SeriesInfo.F_SUFFIX, TsData.class, source -> source.forecast(source.getForecastCount(), false));
//        MAPPING.set(ModellingDictionary.Y + SeriesInfo.EF_SUFFIX, source -> source.getForecastError());
            set(ModellingDictionary.YC, TsData.class, source -> source.interpolatedSeries(false));
//        MAPPING.set(ModellingDictionary.YC + SeriesInfo.F_SUFFIX, source -> source.forecast(source.getForecastCount(), false));
//        MAPPING.set(ModellingDictionary.YC + SeriesInfo.EF_SUFFIX, source -> source.getForecastError());
            set(ModellingDictionary.L, TsData.class, source -> source.linearizedSeries());
            set(ModellingDictionary.Y_LIN, TsData.class, source -> {
                TsData lin = source.linearizedSeries();
                return source.backTransform(lin, false);
            });
//        MAPPING.set(ModellingDictionary.Y_LIN + SeriesInfo.F_SUFFIX, source -> source.linearizedForecast(source.domain(true).getLength(), true));
//        MAPPING.set(ModellingDictionary.L + SeriesInfo.F_SUFFIX, source -> source.linearizedForecast(source.getForecastCount()));
//        MAPPING.set(ModellingDictionary.L + SeriesInfo.B_SUFFIX, source -> source.linearizedBackcast(source.description.getFrequency()));
            set(ModellingDictionary.YCAL, TsData.class, source -> {
                TsData y = source.getDescription().getSeries();
                TsData cal = source.getCalendarEffect(y.getDomain());
                cal = source.backTransform(cal, true);
                return source.inv_op(y, cal);
            });
//        MAPPING.set(ModellingDictionary.YCAL + SeriesInfo.F_SUFFIX, source -> source.getYcal(true));

// All determinsitic effects
            set(ModellingDictionary.DET, TsData.class, source -> source.getDeterministicEffect(null));
            setArray(ModellingDictionary.DET + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                    (source, i) -> source.getDeterministicEffect(source.forecastDomain(i)));
            setArray(ModellingDictionary.DET + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                    (source, i) -> source.getDeterministicEffect(source.backcastDomain(i)));

// All calendar effects
            set(ModellingDictionary.CAL, TsData.class, source -> source.getCalendarEffect(null));
            setArray(ModellingDictionary.CAL + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                    (source, i) -> source.getCalendarEffect(source.forecastDomain(i)));
            setArray(ModellingDictionary.CAL + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                    (source, i) -> source.getCalendarEffect(source.backcastDomain(i)));

// Trading days effects
            set(ModellingDictionary.TDE, TsData.class, source -> source.getTradingDaysEffect(null));
            setArray(ModellingDictionary.TDE + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                    (source, i) -> source.getTradingDaysEffect(source.forecastDomain(i)));
            setArray(ModellingDictionary.TDE + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                    (source, i) -> source.getTradingDaysEffect(source.backcastDomain(i)));

// All moving holidays effects
            set(ModellingDictionary.MHE, TsData.class, source -> source.getMovingHolidayEffect(null));
            setArray(ModellingDictionary.MHE + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                    (source, i) -> source.getMovingHolidayEffect(source.forecastDomain(i)));
            setArray(ModellingDictionary.MHE + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                    (source, i) -> source.getMovingHolidayEffect(source.backcastDomain(i)));

// Easter effect
            set(ModellingDictionary.EE, TsData.class, source -> source.getEasterEffect(null));
            setArray(ModellingDictionary.EE + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                    (source, i) -> source.getEasterEffect(source.forecastDomain(i)));
            setArray(ModellingDictionary.EE + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                    (source, i) -> source.getEasterEffect(source.backcastDomain(i)));

// All Outliers effect
            set(ModellingDictionary.OUT, TsData.class, source -> source.getOutliersEffect(null));
            setArray(ModellingDictionary.OUT + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                    (source, i) -> source.getOutliersEffect(source.forecastDomain(i)));
            setArray(ModellingDictionary.OUT + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                    (source, i) -> source.getOutliersEffect(source.backcastDomain(i)));

            set(BasicInformationExtractor.concatenate(REGRESSION, MU), RegressionItem.class,
                    source -> source.regressionItem(v -> v instanceof TrendConstant, 0));
            set(BasicInformationExtractor.concatenate(REGRESSION, LP), RegressionItem.class,
                    source -> source.regressionItem(v -> v instanceof ILengthOfPeriodVariable, 0));
            set(BasicInformationExtractor.concatenate(REGRESSION, EASTER), RegressionItem.class,
                    source -> source.regressionItem(v -> v instanceof IEasterVariable, 0));
            setArray(BasicInformationExtractor.concatenate(REGRESSION, OUT), 1, 31, RegressionItem.class,
                    (source, i) -> source.regressionItem(v -> v instanceof IOutlier, i - 1));
            setArray(BasicInformationExtractor.concatenate(REGRESSION, TD), 1, 7, RegressionItem.class,
                    (source, i) -> source.regressionItem(v -> v instanceof ITradingDaysVariable, i - 1));
            setArray(BasicInformationExtractor.concatenate(REGRESSION, RAMP), 1, 31, RegressionItem.class,
                    (source, i) -> source.regressionItem(v -> v instanceof Ramp, i - 1));

            set(ModellingDictionary.EE, TsData.class, source -> source.getEasterEffect(null));
            setArray(ModellingDictionary.EE + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                    (source, i) -> source.getEasterEffect(source.forecastDomain(i)));
            setArray(ModellingDictionary.EE + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                    (source, i) -> source.getEasterEffect(source.backcastDomain(i)));
            
//        MAPPING.set(FULLRES, source -> source.getFullResiduals());
//        MAPPING.setList(InformationSet.item(REGRESSION, TD), 1, 15, RegressionItem.class, (source, i) -> source.getRegressionItem(ITradingDaysVariable.class, i - 1));
//        MAPPING.set(InformationSet.item(REGRESSION, TD_DERIVED), RegressionItem.class, source -> {
//            TsVariableSelection<ITsVariable> regs = source.x_.select(var -> var instanceof ITradingDaysVariable);
//            if (regs.isEmpty() || regs.getItemsCount() > 1) {
//                return null;
//            }
//            Item<ITsVariable> reg = regs.elements()[0];
//            int ndim = reg.variable.getDim();
//            if (ndim <= 1) {
//                return null;
//            } else {
//                ConcentratedLikelihood ll = source.estimation.getLikelihood();
//                int nhp = source.description.getArimaComponent().getFreeParametersCount();
//                int start = source.description.getRegressionVariablesStartingPosition();
//                double[] b = ll.getB();
//                int k0 = start + reg.position, k1 = k0 + ndim;
//                double bd = 0;
//                for (int k = k0; k < k1; ++k) {
//                    bd -= b[k];
//                }
//                double var = ll.getBVar(true, nhp).subMatrix(k0, k1, k0, k1).sum();
//                double tval = bd / Math.sqrt(var);
//                T t = new T();
//                t.setDegreesofFreedom(ll.getDegreesOfFreedom(true, nhp));
//                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
//                return new RegressionItem("td-derived", bd, Math.sqrt(var), prob);
//            }
//        });
//        MAPPING.set(InformationSet.item(REGRESSION, TD_FTEST), Double.class, source -> {
//            TsVariableSelection<ITsVariable> regs = source.x_.select(var -> var instanceof ITradingDaysVariable);
//            if (regs.isEmpty()) {
//                return null;
//            }
//            int nvars = regs.getVariablesCount();
//            if (regs.getItemsCount() == 1 && nvars > 1) {
//                try {
//                    JointRegressionTest jtest = new JointRegressionTest(.05);
//                    jtest.accept(source.estimation.getLikelihood(),
//                            source.description.getArimaComponent().getFreeParametersCount(),
//                            source.description.getRegressionVariablesStartingPosition() + regs.get(0).position,
//                            nvars, null);
//                    return jtest.getTest().getPValue();
//                } catch (Exception ex) {
//                }
//            }
//            return null;
//        });
// 
//        MAPPING.setList(InformationSet.item(REGRESSION, USER), 1, 31, RegressionItem.class, (source, i) -> source.getRegressionItem(IUserTsVariable.class, i - 1));
            set(BasicInformationExtractor.concatenate(MAX, PCORR), Matrix.class, source -> {
                FastMatrix cov = FastMatrix.of(source.getEstimation().getParameters().getCovariance());
                DataBlock diag = cov.diagonal();
                for (int i = 0; i < cov.getRowsCount(); ++i) {
                    double vi = diag.get(i);
                    for (int j = 0; j < i; ++j) {
                        double vj = diag.get(j);
                        if (vi != 0 && vj != 0) {
                            cov.mul(i, j, 1 / Math.sqrt(vi * vj));
                        }
                    }
                }
                SymmetricMatrix.fromLower(cov);
                diag.set(1);
                return cov;
            });
        }

        @Override
        public Class<RegSarimaModel> getSourceClass() {
            return RegSarimaModel.class;
        }
    }

    @ServiceProvider(InformationExtractor.class)
    public static class GenericExtractor extends InformationDelegate<RegSarimaModel, GeneralLinearModel> {

        public GenericExtractor() {
            super(v -> v);
        }

        @Override
        public Class<GeneralLinearModel> getDelegateClass() {
            return GeneralLinearModel.class;
        }

        @Override
        public Class<RegSarimaModel> getSourceClass() {
            return RegSarimaModel.class;
        }

    }

}
