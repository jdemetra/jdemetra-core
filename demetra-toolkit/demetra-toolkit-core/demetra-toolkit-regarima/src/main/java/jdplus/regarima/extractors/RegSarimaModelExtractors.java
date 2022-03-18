/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.regarima.extractors;

import demetra.arima.SarimaSpec;
import demetra.data.Parameter;
import demetra.information.InformationDelegate;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.math.matrices.Matrix;
import demetra.modelling.SeriesInfo;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.IEasterVariable;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.timeseries.regression.MissingValueEstimation;
import demetra.timeseries.regression.RegressionItem;
import demetra.timeseries.regression.TrendConstant;
import demetra.timeseries.regression.UserVariable;
import demetra.toolkit.dictionaries.ArimaDictionaries;
import demetra.toolkit.dictionaries.Dictionary;
import demetra.toolkit.dictionaries.RegArimaDictionaries;
import demetra.toolkit.dictionaries.RegressionDictionaries;
import demetra.toolkit.dictionaries.ResidualsDictionaries;
import demetra.toolkit.dictionaries.UtilityDictionaries;
import jdplus.data.DataBlock;
import jdplus.dstats.T;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.modelling.GeneralLinearModel;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.stats.likelihood.LikelihoodStatistics;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

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

    public final int NFCAST = -2, NBCAST = 0;

    @ServiceProvider(InformationExtractor.class)
    public static class Specific extends InformationMapping<RegSarimaModel> {

        private String arimaItem(String key) {
            return Dictionary.concatenate(RegArimaDictionaries.ARIMA, key);
        }

        private String regressionItem(String key) {
            return Dictionary.concatenate(RegArimaDictionaries.REGRESSION, key);
        }

        private String residualsItem(String key) {
            return Dictionary.concatenate(RegArimaDictionaries.RESIDUALS, key);
        }

        private String advancedItem(String key) {
            return Dictionary.concatenate(RegArimaDictionaries.ADVANCED, key);
        }

        private String mlItem(String key) {
            return Dictionary.concatenate(RegArimaDictionaries.MAX, key);
        }

        private RegressionItem phi(RegSarimaModel model, int lag) {
            if (lag <= 0) {
                return null;
            }
            SarimaSpec arima = model.getDescription().getStochasticComponent();
            Parameter[] p = arima.getPhi();
            if (lag > p.length) {
                return null;
            }
            Parameter phi = p[lag - 1];
            if (phi.isFixed()) {
                return new RegressionItem(phi.getValue(), 0, Double.NaN, null);
            }
            int pos = 0;
            for (int i = 0; i < p.length; ++i) {
                if (lag == i + 1) {
                    break;
                }
                if (!p[i].isFixed()) {
                    ++pos;
                }
            }
            return pt(model, phi.getValue(), pos, null);
        }

        private RegressionItem bphi(RegSarimaModel model, int lag) {
            if (lag <= 0) {
                return null;
            }
            SarimaSpec arima = model.getDescription().getStochasticComponent();
            Parameter[] p = arima.getBphi();
            if (lag > p.length) {
                return null;
            }
            Parameter bphi = p[lag - 1];
            if (bphi.isFixed()) {
                return new RegressionItem(bphi.getValue(), 0, Double.NaN, null);
            }
            int pos = Parameter.freeParametersCount(arima.getPhi());
            for (int i = 0; i < p.length; ++i) {
                if (lag == i + 1) {
                    break;
                }
                if (!p[i].isFixed()) {
                    ++pos;
                }
            }
            return pt(model, bphi.getValue(), pos, null);
        }

        private RegressionItem theta(RegSarimaModel model, int lag) {
            if (lag <= 0) {
                return null;
            }
            SarimaSpec arima = model.getDescription().getStochasticComponent();
            Parameter[] p = arima.getTheta();
            if (lag > p.length) {
                return null;
            }
            Parameter theta = p[lag - 1];
            if (theta.isFixed()) {
                return new RegressionItem(theta.getValue(), 0, Double.NaN, null);
            }
            int pos = Parameter.freeParametersCount(arima.getPhi()) + Parameter.freeParametersCount(arima.getBphi());
            for (int i = 0; i < p.length; ++i) {
                if (lag == i + 1) {
                    break;
                }
                if (!p[i].isFixed()) {
                    ++pos;
                }
            }
            return pt(model, theta.getValue(), pos, null);
        }

        private RegressionItem btheta(RegSarimaModel model, int lag) {
            if (lag <= 0) {
                return null;
            }
            SarimaSpec arima = model.getDescription().getStochasticComponent();
            Parameter[] p = arima.getBtheta();
            if (lag > p.length) {
                return null;
            }
            Parameter btheta = p[lag - 1];
            if (btheta.isFixed()) {
                return new RegressionItem(btheta.getValue(), 0, Double.NaN, null);
            }
            int pos = Parameter.freeParametersCount(arima.getPhi())
                    + Parameter.freeParametersCount(arima.getBphi())
                    + Parameter.freeParametersCount(arima.getTheta());
            for (int i = 0; i < p.length; ++i) {
                if (lag == i + 1) {
                    break;
                }
                if (!p[i].isFixed()) {
                    ++pos;
                }
            }
            return pt(model, btheta.getValue(), pos, null);
        }

        RegressionItem pt(RegSarimaModel model, double val, int pos, String name) {
            GeneralLinearModel.Estimation estimation = model.getEstimation();
            LikelihoodStatistics ll = estimation.getStatistics();
            int nobs = ll.getEffectiveObservationsCount(), nparams = ll.getEstimatedParametersCount();
            int nhp = model.freeArimaParametersCount();
            double ndf = nobs - nparams;
            double vcorr = ndf / (ndf + nhp);
            T t = new T(nobs - nparams);
            double stde = Math.sqrt(estimation.getParameters().getCovariance().get(pos, pos) * vcorr);
            double tval = val / stde;
            double prob = 1 - t.getProbabilityForInterval(-tval, tval);
            return new RegressionItem(val, stde, prob, name);
        }

        public Specific() {
            // ARIMA related
            delegate(RegArimaDictionaries.ARIMA, SarimaSpec.class, source
                    -> source.getDescription().getStochasticComponent());

            setArray(arimaItem(ArimaDictionaries.PHI), 1, 12, RegressionItem.class, (source, i) -> {
                return phi(source, i);
            });
            setArray(arimaItem(ArimaDictionaries.BPHI), 1, 12, RegressionItem.class, (source, i) -> {
                return bphi(source, i);
            });
            setArray(arimaItem(ArimaDictionaries.THETA), 1, 12, RegressionItem.class, (source, i) -> {
                return theta(source, i);
            });
            setArray(arimaItem(ArimaDictionaries.BTHETA), 1, 12, RegressionItem.class, (source, i) -> {
                return btheta(source, i);
            });

            //*********************

            setArray(RegressionDictionaries.Y_F, NFCAST, TsData.class, (source, i) -> source.forecasts(i).getForecasts());
            setArray(RegressionDictionaries.Y_B, NBCAST, TsData.class, (source, i) -> source.backcasts(i).getForecasts());
            setArray(RegressionDictionaries.Y_EF, NFCAST, TsData.class, (source,i) -> source.forecasts(i).getForecastsStdev());
            setArray(RegressionDictionaries.Y_EB, NBCAST, TsData.class, (source,i) -> source.backcasts(i).getForecastsStdev());


            set(RegressionDictionaries.YC, TsData.class, source -> source.interpolatedSeries(false));
            
            set(RegressionDictionaries.L, TsData.class, source -> source.linearizedSeries());
//            set(RegressionDictionaries.Y_LIN, TsData.class, source -> {
//                TsData lin = source.linearizedSeries();
//                return source.backTransform(lin, false);
//            });
            
            //********************
            
            
//        MAPPING.set(ModellingDictionary.YC + SeriesInfo.F_SUFFIX, source -> source.forecast(source.getForecastCount(), false));
//        MAPPING.set(ModellingDictionary.YC + SeriesInfo.EF_SUFFIX, source -> source.getForecastError());
//        MAPPING.set(RegressionDictionaries.Y_LIN + SeriesInfo.F_SUFFIX, source -> source.linearizedForecast(source.domain(true).getLength(), true));
//        MAPPING.set(RegressionDictionaries.L + SeriesInfo.F_SUFFIX, source -> source.linearizedForecast(source.getForecastCount()));
//        MAPPING.set(RegressionDictionaries.L + SeriesInfo.B_SUFFIX, source -> source.linearizedBackcast(source.description.getFrequency()));
            set(RegressionDictionaries.YCAL, TsData.class, source -> {
                TsData y = source.getDescription().getSeries();
                TsData cal = source.getCalendarEffect(y.getDomain());
                cal = source.backTransform(cal, true);
                return source.inv_op(y, cal);
            });
//        MAPPING.set(RegressionDictionaries.YCAL + SeriesInfo.F_SUFFIX, source -> source.getYcal(true));

// All deterministic effects
            set(RegressionDictionaries.DET, TsData.class, (RegSarimaModel source) -> {
                TsData det = source.deterministicEffect(null, v->true);
                return source.backTransform(det, true);
            });
            setArray(RegressionDictionaries.DET + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                    (source, i) -> {
                        TsData det = source.deterministicEffect(source.forecastDomain(i), v -> true);
                        return source.backTransform(det, true);
                    });
            setArray(RegressionDictionaries.DET + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                    (source, i) -> {
                        TsData det = source.deterministicEffect(source.backcastDomain(i), v -> true);
                        return source.backTransform(det, true);
                    });

// All calendar effects
            set(RegressionDictionaries.CAL, TsData.class, source -> source.getCalendarEffect(null));
            setArray(RegressionDictionaries.CAL + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                    (source, i) -> source.getCalendarEffect(source.forecastDomain(i)));
            setArray(RegressionDictionaries.CAL + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                    (source, i) -> source.getCalendarEffect(source.backcastDomain(i)));

// Trading days effects
            set(RegressionDictionaries.TDE, TsData.class, source -> source.getTradingDaysEffect(null));
            setArray(RegressionDictionaries.TDE_F, NFCAST, TsData.class,
                    (source, i) -> source.getTradingDaysEffect(source.forecastDomain(i)));
            setArray(RegressionDictionaries.TDE_B, NBCAST, TsData.class,
                    (source, i) -> source.getTradingDaysEffect(source.backcastDomain(i)));

// All moving holidays effects
            set(RegressionDictionaries.MHE, TsData.class, source -> source.getMovingHolidayEffect(null));
            setArray(RegressionDictionaries.MHE_F, NFCAST, TsData.class,
                    (source, i) -> source.getMovingHolidayEffect(source.forecastDomain(i)));
            setArray(RegressionDictionaries.MHE_B, NBCAST, TsData.class,
                    (source, i) -> source.getMovingHolidayEffect(source.backcastDomain(i)));
            
// Easter effect
            set(RegressionDictionaries.EE, TsData.class, source -> source.getEasterEffect(null));
            setArray(RegressionDictionaries.EE_F, NFCAST, TsData.class,
                    (source, i) -> source.getEasterEffect(source.forecastDomain(i)));
            setArray(RegressionDictionaries.EE_B, NBCAST, TsData.class,
                    (source, i) -> source.getEasterEffect(source.backcastDomain(i)));

// Other moving holidays effects
            set(RegressionDictionaries.OMHE, TsData.class, source -> source.getOtherMovingHolidayEffect(null));
            setArray(RegressionDictionaries.OMHE_F, NFCAST, TsData.class,
                    (source, i) -> source.getOtherMovingHolidayEffect(source.forecastDomain(i)));
            setArray(RegressionDictionaries.OMHE_B, NBCAST, TsData.class,
                    (source, i) -> source.getOtherMovingHolidayEffect(source.backcastDomain(i)));

// All Outliers effect
            set(RegressionDictionaries.OUT, TsData.class, source -> source.getOutliersEffect(null));
            setArray(RegressionDictionaries.OUT + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                    (source, i) -> source.getOutliersEffect(source.forecastDomain(i)));
            setArray(RegressionDictionaries.OUT + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                    (source, i) -> source.getOutliersEffect(source.backcastDomain(i)));

            set(regressionItem(RegressionDictionaries.MU), RegressionItem.class,
                    source -> source.regressionItem(v -> v instanceof TrendConstant, 0));
            set(regressionItem(RegressionDictionaries.LP), RegressionItem.class,
                    source -> source.regressionItem(v -> v instanceof ILengthOfPeriodVariable, 0));
            set(regressionItem(RegressionDictionaries.EASTER), RegressionItem.class,
                    source -> source.regressionItem(v -> v instanceof IEasterVariable, 0));
            setArray(regressionItem(RegressionDictionaries.OUTLIERS), 1, 31, RegressionItem.class,
                    (source, i) -> source.regressionItem(v -> v instanceof IOutlier, i - 1));
            setArray(regressionItem(RegressionDictionaries.TD), 1, 7, RegressionItem.class,
                    (source, i) -> source.regressionItem(v -> v instanceof ITradingDaysVariable, i - 1));
            setArray(regressionItem(RegressionDictionaries.USER), 1, 30, RegressionItem.class,
                    (source, i) -> source.regressionItem(v -> v instanceof UserVariable, i - 1));
            setArray(regressionItem(RegressionDictionaries.OUT), 1, 30, RegressionItem.class,
                    (source, i) -> source.regressionItem(v -> v instanceof IOutlier, i - 1));
            setArray(regressionItem(RegressionDictionaries.MISSING), 1, 100, MissingValueEstimation.class,
                    (source, i) -> {
                        MissingValueEstimation[] missing = source.getEstimation().getMissing();
                        return i <= 0 || i > missing.length ? null : missing[i - 1];
                    });

            set(RegressionDictionaries.EE, TsData.class, source -> source.getEasterEffect(null));
            setArray(RegressionDictionaries.EE + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                    (source, i) -> source.getEasterEffect(source.forecastDomain(i)));
            setArray(RegressionDictionaries.EE + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
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
            set(mlItem(UtilityDictionaries.PCORR), Matrix.class, source -> {
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
            
            set(residualsItem(ResidualsDictionaries.SER), Double.class, (RegSarimaModel source)->{
                LikelihoodStatistics stats = source.getEstimation().getStatistics();
                double ssqErr = stats.getSsqErr();
                int ndf=stats.getEffectiveObservationsCount()-stats.getEstimatedParametersCount()-source.freeArimaParametersCount();
                return Math.sqrt(ssqErr/ndf);
            });
            set(residualsItem(ResidualsDictionaries.RES), double[].class, (RegSarimaModel source)->{
                RegSarimaModel.Details details = source.getDetails();
                return details.getIndependentResiduals().toArray();
            });
            set(residualsItem(ResidualsDictionaries.TSRES), TsData.class, (RegSarimaModel source)->source.fullResiduals());
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
