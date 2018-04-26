/*
 * Copyright 2017 National Bank of Belgium
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
package ec.tstoolkit.jdr.regarima;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.arima.estimation.Forecasts;
import ec.tstoolkit.information.RegressionItem;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.modelling.arima.LogForecasts;
import ec.tstoolkit.modelling.arima.PreadjustmentType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.regression.ICalendarVariable;
import ec.tstoolkit.timeseries.regression.IEasterVariable;
import ec.tstoolkit.timeseries.regression.ILengthOfPeriodVariable;
import ec.tstoolkit.timeseries.regression.IMovingHolidayVariable;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.ArrayList;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class RegArimaInfo {

    public final int FCAST_YEAR = 1;
    public final String LOG = "log",
            ADJUST = "adjust",
            SPAN = "span", ESPAN = "espan", START = "start", END = "end", N = "n",
            REGRESSION = "regression",
            EASTER = "easter",
            FULLRES = "fullresiduals",
            FCASTS = "fcasts",
            EFCASTS = "efcasts",
            BCASTS = "bcasts",
            LIN_FCASTS = "lin_fcasts",
            LIN_BCASTS = "lin_bcasts",
            NTD = "ntd", NMH = "nmh",
            TD = "td",
            LP = "lp", OUT = "out",
            NOUT = "nout", NOUTAO = "noutao", NOUTLS = "noutls", NOUTTC = "nouttc", NOUTSO = "noutso",
            COEFF = "coefficients", COVAR = "covar", COEFFDESC = "description", PCOVAR = "pcovar";
    ;
    
    final InformationMapping<PreprocessingModel> MAPPING = new InformationMapping<>(PreprocessingModel.class);

    public InformationMapping<PreprocessingModel> getMapping() {
        return MAPPING;
    }

    static {
        MAPPING.set(InformationExtractor.concatenate(SPAN, START), TsPeriod.class, source -> source.description.getSeriesDomain().getStart());
        MAPPING.set(InformationExtractor.concatenate(SPAN, END), TsPeriod.class, source -> source.description.getSeriesDomain().getLast());
        MAPPING.set(InformationExtractor.concatenate(SPAN, N), Integer.class, source -> source.description.getSeriesDomain().getLength());
        MAPPING.set(InformationExtractor.concatenate(ESPAN, START), TsPeriod.class, source -> source.description.getEstimationDomain().getStart());
        MAPPING.set(InformationExtractor.concatenate(ESPAN, END), TsPeriod.class, source -> source.description.getEstimationDomain().getLast());
        MAPPING.set(InformationExtractor.concatenate(ESPAN, N), Integer.class, source -> source.description.getEstimationDomain().getLength());
        MAPPING.set(LOG, Boolean.class, source -> source.isMultiplicative());
        MAPPING.set(ADJUST, Boolean.class, source -> {
            if (source.description.getPreadjustmentType() == PreadjustmentType.None) {
                return null;
            } else {
                return source.description.getLengthOfPeriodType() != LengthOfPeriodType.None;
            }
        });
        MAPPING.set(ModellingDictionary.Y, TsData.class, source -> source.description.getOriginal());
        MAPPING.set(ModellingDictionary.Y + SeriesInfo.F_SUFFIX, TsData.class, source -> source.forecast(FCAST_YEAR * source.description.getFrequency(), false));
        MAPPING.set(ModellingDictionary.Y + SeriesInfo.EF_SUFFIX, TsData.class, source -> forecastError(source));
        MAPPING.set(ModellingDictionary.YC, TsData.class, source -> source.interpolatedSeries(false));
        MAPPING.set(ModellingDictionary.YC + SeriesInfo.F_SUFFIX, TsData.class, source -> source.forecast(FCAST_YEAR * source.description.getFrequency(), false));
        MAPPING.set(ModellingDictionary.YC + SeriesInfo.EF_SUFFIX, TsData.class, source -> forecastError(source));
        MAPPING.set(ModellingDictionary.L, TsData.class, source -> source.linearizedSeries(false));
        MAPPING.set(ModellingDictionary.Y_LIN, TsData.class, source -> source.linearizedSeries(true));
        MAPPING.set(ModellingDictionary.Y_LIN + SeriesInfo.F_SUFFIX, TsData.class, source -> source.linearizedForecast(domain(source, true).getLength(), true));
        MAPPING.set(ModellingDictionary.YCAL, TsData.class, source -> ycal(source, false));
        MAPPING.set(ModellingDictionary.YCAL + SeriesInfo.F_SUFFIX, TsData.class, source -> ycal(source, true));
        MAPPING.set(ModellingDictionary.DET, TsData.class, source -> det(source, false));
        MAPPING.set(ModellingDictionary.DET + SeriesInfo.F_SUFFIX, TsData.class, source -> det(source, true));
        MAPPING.set(ModellingDictionary.L + SeriesInfo.F_SUFFIX, TsData.class, source -> source.linearizedForecast(FCAST_YEAR * source.description.getFrequency()));
        MAPPING.set(ModellingDictionary.L + SeriesInfo.B_SUFFIX, TsData.class, source -> source.linearizedBackcast(source.description.getFrequency()));
        MAPPING.set(ModellingDictionary.CAL, TsData.class, source -> cal(source, false));
        MAPPING.set(ModellingDictionary.CAL + SeriesInfo.F_SUFFIX, TsData.class, source -> cal(source, true));
        MAPPING.set(ModellingDictionary.TDE, TsData.class, source -> tde(source, false));
        MAPPING.set(ModellingDictionary.TDE + SeriesInfo.F_SUFFIX, TsData.class, source -> tde(source, true));
        MAPPING.set(ModellingDictionary.MHE, TsData.class, source -> mhe(source, false));
        MAPPING.set(ModellingDictionary.MHE + SeriesInfo.F_SUFFIX, TsData.class, source -> mhe(source, true));
        MAPPING.set(ModellingDictionary.EE, TsData.class, source -> ee(source, false));
        MAPPING.set(ModellingDictionary.EE + SeriesInfo.F_SUFFIX, TsData.class, source -> ee(source, true));
        MAPPING.set(ModellingDictionary.OMHE, TsData.class, source -> omhe(source, false));
        MAPPING.set(ModellingDictionary.OMHE + SeriesInfo.F_SUFFIX, TsData.class, source -> omhe(source, true));
        MAPPING.set(ModellingDictionary.OUT, TsData.class, source -> outlier(source, ComponentType.Undefined, false));
        MAPPING.set(ModellingDictionary.OUT + SeriesInfo.F_SUFFIX, TsData.class, source -> outlier(source, ComponentType.Undefined, true));
        MAPPING.set(ModellingDictionary.OUT_I, TsData.class, source -> outlier(source, ComponentType.Irregular, false));
        MAPPING.set(ModellingDictionary.OUT_I + SeriesInfo.F_SUFFIX, TsData.class, source -> outlier(source, ComponentType.Irregular, true));
        MAPPING.set(ModellingDictionary.OUT_T, TsData.class, source -> outlier(source, ComponentType.Trend, false));
        MAPPING.set(ModellingDictionary.OUT_T + SeriesInfo.F_SUFFIX, TsData.class, source -> outlier(source, ComponentType.Trend, true));
        MAPPING.set(ModellingDictionary.OUT_S, TsData.class, source -> outlier(source, ComponentType.Seasonal, false));
        MAPPING.set(ModellingDictionary.OUT_S + SeriesInfo.F_SUFFIX, TsData.class, source -> outlier(source, ComponentType.Seasonal, true));
        MAPPING.set(ModellingDictionary.REG, TsData.class, source -> reg(source, false));
        MAPPING.set(ModellingDictionary.REG + SeriesInfo.F_SUFFIX, TsData.class, source -> reg(source, true));
        MAPPING.set(ModellingDictionary.REG_T, TsData.class, source -> reg(source, ComponentType.Trend, false));
        MAPPING.set(ModellingDictionary.REG_T + SeriesInfo.F_SUFFIX, TsData.class, source -> reg(source, ComponentType.Trend, true));
        MAPPING.set(ModellingDictionary.REG_S, TsData.class, source -> reg(source, ComponentType.Seasonal, false));
        MAPPING.set(ModellingDictionary.REG_S + SeriesInfo.F_SUFFIX, TsData.class, source -> reg(source, ComponentType.Seasonal, true));
        MAPPING.set(ModellingDictionary.REG_I, TsData.class, source -> reg(source, ComponentType.Irregular, false));
        MAPPING.set(ModellingDictionary.REG_I + SeriesInfo.F_SUFFIX, TsData.class, source -> reg(source, ComponentType.Irregular, true));
        MAPPING.set(ModellingDictionary.REG_SA, TsData.class, source -> reg(source, ComponentType.SeasonallyAdjusted, false));
        MAPPING.set(ModellingDictionary.REG_SA + SeriesInfo.F_SUFFIX, TsData.class, source -> reg(source, ComponentType.SeasonallyAdjusted, true));
        MAPPING.set(ModellingDictionary.REG_Y, TsData.class, source -> reg(source, ComponentType.Series, false));
        MAPPING.set(ModellingDictionary.REG_Y + SeriesInfo.F_SUFFIX, TsData.class, source -> reg(source, ComponentType.Series, true));
        MAPPING.set(ModellingDictionary.REG_U, TsData.class, source -> reg(source, ComponentType.Undefined, false));
        MAPPING.set(ModellingDictionary.REG_U + SeriesInfo.F_SUFFIX, TsData.class, source -> reg(source, ComponentType.Undefined, true));
        MAPPING.set(FULLRES, TsData.class, source -> source.getFullResiduals());
        MAPPING.set(LP, RegressionItem.class, source -> source.getRegressionItem(ILengthOfPeriodVariable.class, 0));
        MAPPING.set(NTD, Integer.class, source -> {
            return source.description.countRegressors(var -> var.status.isSelected() && var.getVariable() instanceof ICalendarVariable);
        });
        MAPPING.set(NMH, Integer.class, source -> {
            return source.description.countRegressors(var -> var.status.isSelected() && var.getVariable() instanceof IMovingHolidayVariable);
        });
        MAPPING.setArray(TD, 1, 15, RegressionItem.class, (source, i) -> source.getRegressionItem(ITradingDaysVariable.class, i - 1));
        MAPPING.set(EASTER, RegressionItem.class, source -> source.getRegressionItem(IEasterVariable.class, 0));
        MAPPING.set(NOUT, Integer.class, source -> source.description.getOutliers().size() + source.description.getPrespecifiedOutliers().size());
        MAPPING.set(NOUTAO, Integer.class, source -> {
            TsVariableList vars = source.description.buildRegressionVariables();
            return vars.select(OutlierType.AO).getItemsCount();
        });
        MAPPING.set(NOUTLS, Integer.class, source -> {
            TsVariableList vars = source.description.buildRegressionVariables();
            return vars.select(OutlierType.LS).getItemsCount();
        });
        MAPPING.set(NOUTTC, Integer.class, source -> {
            TsVariableList vars = source.description.buildRegressionVariables();
            return vars.select(OutlierType.TC).getItemsCount();
        });
        MAPPING.set(NOUTSO, Integer.class, source -> {
            TsVariableList vars = source.description.buildRegressionVariables();
            return vars.select(OutlierType.SO).getItemsCount();
        });
        MAPPING.setArray(OUT, 1, 31, RegressionItem.class, (source, i) -> source.getRegressionItem(IOutlierVariable.class, i - 1));
        MAPPING.set(COEFF, Parameter[].class, source -> {
            double[] c = source.estimation.getLikelihood().getB();
            if (c == null) {
                return new Parameter[0];
            }
            Parameter[] C = new Parameter[c.length];
            double[] e = source.estimation.getLikelihood().getBSer(true, source.description.getArimaComponent().getFreeParametersCount());
            for (int i = 0; i < C.length; ++i) {
                Parameter p = new Parameter(c[i], ParameterType.Estimated);
                p.setStde(e[i]);
                C[i] = p;
            }
            return C;
        });
        MAPPING.set(COEFFDESC, String[].class, source -> {
            ArrayList<String> str = new ArrayList<>();
            if (source.description.isEstimatedMean()) {
                str.add("Mean");
            }
            int[] missings = source.description.getMissingValues();
            if (missings != null) {
                for (int i = 0; i < missings.length; ++i) {
                    int pos = missings[i];
                    TsPeriod period = source.description.getEstimationDomain().get(pos);
                    str.add("Missing: " + period.toString());
                }
            }
            ITsVariable[] items = source.description.buildRegressionVariables().items();
            TsFrequency context = source.description.getEstimationDomain().getFrequency();
            for (ITsVariable var : items) {
                for (int j = 0; j < var.getDim(); ++j) {
                    str.add(var.getItemDescription(j, context));
                }
            }
            String[] desc = new String[str.size()];
            return str.toArray(desc);
        });
        MAPPING.set(COVAR, Matrix.class,
                source -> source.estimation.getLikelihood().getBVar(true, source.description.getArimaComponent().getFreeParametersCount()));
        MAPPING.set(PCOVAR, Matrix.class, source -> source.estimation.getParametersCovariance());
        MAPPING.setArray(FCASTS, -2, TsData.class, (source, i) -> source.forecast(nperiods(source, i), false));
        MAPPING.setArray(BCASTS, -2, TsData.class, (source, i) -> source.backcast(nperiods(source, i), false));
        MAPPING.setArray(LIN_FCASTS, -2, TsData.class, (source, i) -> source.linearizedForecast(nperiods(source, i)));
        MAPPING.setArray(LIN_BCASTS, -2, TsData.class, (source, i) -> source.linearizedBackcast(nperiods(source, i)));
        MAPPING.setArray(EFCASTS, -2, TsData.class, (source, i)
                -> {
            int np = nperiods(source, i);
            TsDomain fdomain = new TsDomain(source.description.getSeriesDomain().getEnd(), np);
            Forecasts fcasts = source.forecasts(np);
            double[] ef;
            if (source.isMultiplicative()) {
                LogForecasts lf = new LogForecasts(fcasts);
                ef = lf.getForecatStdevs();
            } else {
                ef = fcasts.getForecastStdevs();
            }
            return new TsData(fdomain.getStart(), ef, true);
        });

    }

    private int nperiods(PreprocessingModel m, int n) {
        if (n >= 0) {
            return n;
        } else {
            return -n * m.getFrequency().intValue();
        }
    }

    private TsData reg(PreprocessingModel model, ComponentType componentType, boolean fcast) {
        TsData tmp = model.userEffect(domain(model, fcast), componentType);
        if (tmp == null) {
            return null;
        }
        model.backTransform(tmp, false, false);
        return tmp;
    }

    private TsData reg(PreprocessingModel model, boolean fcast) {
        TsData tmp = model.userEffect(domain(model, fcast));
        if (tmp == null) {
            return null;
        }
        model.backTransform(tmp, false, false);
        return tmp;
    }

    private TsDomain domain(PreprocessingModel model, boolean fcast) {
        if (fcast) {
            TsDomain dom = model.description.getSeriesDomain();
            return new TsDomain(dom.getEnd(), FCAST_YEAR * dom.getFrequency().intValue());
        } else {
            return model.description.getSeriesDomain();
        }
    }

    private TsData outlier(PreprocessingModel model, ComponentType componentType, boolean fcast) {
        TsData tmp = model.outliersEffect(domain(model, fcast), componentType);
        if (tmp == null) {
            return null;
        }
        model.backTransform(tmp, false, false);
        return tmp;
    }

    private TsData tde(PreprocessingModel model, boolean fcast) {
        TsDomain fdom = domain(model, fcast);
        TsData tmp = model.tradingDaysEffect(fdom);
        if (tmp == null) {
            return null;
        }
        model.backTransform(tmp, false, true);
        return tmp;
    }

    private TsData ycal(PreprocessingModel model, boolean fcast) {
        return inv_op(model, fcast ? model.forecast(FCAST_YEAR * model.description.getFrequency(), false) : model.interpolatedSeries(false), cal(model, fcast));
    }

    private TsData omhe(PreprocessingModel model, boolean fcast) {
        TsData tmp = inv_op(model, mhe(model, fcast), ee(model, fcast));
        if (tmp == null) {
            return null;
        }
        return tmp;
    }

    private TsData mhe(PreprocessingModel model, boolean fcast) {
        TsData tmp = model.movingHolidaysEffect(domain(model, fcast));
        if (tmp == null) {
            return null;
        }
        model.backTransform(tmp, false, false);
        return tmp;
    }

    private TsData ee(PreprocessingModel model, boolean fcast) {
        TsData tmp = model.deterministicEffect(domain(model, fcast), IEasterVariable.class);
        if (tmp == null) {
            return null;
        }
        model.backTransform(tmp, false, false);
        return tmp;
    }

    private TsData det(PreprocessingModel model, boolean fcast) {
        TsData tmp = model.deterministicEffect(domain(model, fcast));
        if (tmp == null) {
            return null;
        }
        model.backTransform(tmp, false, true);
        return tmp;
    }

    private TsData cal(PreprocessingModel model, boolean fcast) {
        TsData tmp = op(model, tde(model, fcast), mhe(model, fcast));
        if (tmp == null) {
            return null;
        }
        return tmp;
    }

    private TsData op(PreprocessingModel model, TsData l, TsData r) {
        if (model.description.getTransformation() == DefaultTransformationType.Log) {
            return TsData.multiply(l, r);
        } else {
            return TsData.add(l, r);
        }
    }

    private TsData inv_op(PreprocessingModel model, TsData l, TsData r) {
        if (model.description.getTransformation() == DefaultTransformationType.Log) {
            return TsData.divide(l, r);
        } else {
            return TsData.subtract(l, r);
        }
    }

    private TsData forecastError(PreprocessingModel model) {
        TsDomain fdomain = domain(model, true);
        Forecasts fcasts = model.forecasts(fdomain.getLength());
        double[] ef;
        if (model.isMultiplicative()) {
            LogForecasts lf = new LogForecasts(fcasts);
            ef = lf.getForecatStdevs();
        } else {
            ef = fcasts.getForecastStdevs();
        }
        return new TsData(fdomain.getStart(), ef, true);
    }
}
