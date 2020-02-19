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
package jdplus.regarima;

import demetra.information.InformationMapping;
import demetra.information.InformationSet;
import demetra.modelling.ModellingDictionary;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.LengthOfPeriodType;
import jdplus.regsarima.regular.ModelEstimation;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class ModelEstimationDescriptor {

    private static final InformationMapping<ModelEstimation> MAPPING = new InformationMapping<>(ModelEstimation.class);

    public static InformationMapping<ModelEstimation> getMapping() {
        return MAPPING;
    }

    private int ncasts = -2;
    public static final String LOG = "log",
            ADJUST = "adjust",
            SPAN = "span", ESPAN = "espan", START = "start", END = "end", N = "n", NM = "missing", PERIOD = "period",
            REGRESSION = "regression",
            OUTLIERS = "outlier(*)",
            CALENDAR = "calendar(*)",
            EASTER = "easter",
            FULLRES = "fullresiduals",
            FCASTS = "fcasts",
            EFCASTS = "efcasts",
            BCASTS = "bcasts",
            LIN_FCASTS = "lin_fcasts",
            LIN_BCASTS = "lin_bcasts",
            NTD = "ntd", NMH = "nmh",
            TD = "td", TD1 = "td(1)", TD2 = "td(2)", TD3 = "td(3)", TD4 = "td(4)", TD5 = "td(5)", TD6 = "td(6)", TD7 = "td(7)",
            TD8 = "td(8)", TD9 = "td(9)", TD10 = "td(10)", TD11 = "td(11)", TD12 = "td(12)", TD13 = "td(13)", TD14 = "td(14)",
            LP = "lp", OUT = "out", OUT1 = "out(1)", OUT2 = "out(2)", OUT3 = "out(3)", OUT4 = "out(4)", OUT5 = "out(5)", OUT6 = "out(6)", OUT7 = "out(7)",
            NOUT = "nout", NOUTAO = "noutao", NOUTLS = "noutls", NOUTTC = "nouttc", NOUTSO = "noutso",
            OUT8 = "out(8)", OUT9 = "out(9)", OUT10 = "out(10)", OUT11 = "out(11)", OUT12 = "out(12)", OUT13 = "out(13)", OUT14 = "out(14)",
            OUT15 = "out(15)", OUT16 = "out(16)", OUT17 = "out(17)", OUT18 = "out(18)", OUT19 = "out(19)", OUT20 = "out(20)",
            OUT21 = "out(21)", OUT22 = "out(22)", OUT23 = "out(23)", OUT24 = "out(24)", OUT25 = "out(25)", OUT26 = "out(26)",
            OUT27 = "out(27)", OUT28 = "out(28)", OUT29 = "out(29)", OUT30 = "out(30)",
            USER = "user", COEFF = "coefficients", COVAR = "covar", COEFFDESC = "description", PCOVAR = "pcovar",
            TD_DERIVED = "td-derived", TD_FTEST = "td-ftest";

    static {
        MAPPING.set(PERIOD, Integer.class, source -> source.getEstimationDomain().getAnnualFrequency());
        MAPPING.set(InformationSet.item(SPAN, START), TsPeriod.class, source -> source.getOriginalSeries().getStart());
        MAPPING.set(InformationSet.item(SPAN, END), TsPeriod.class, source -> source.getOriginalSeries().getDomain().getLastPeriod());
        MAPPING.set(InformationSet.item(SPAN, N), Integer.class, source -> source.getOriginalSeries().length());
        MAPPING.set(InformationSet.item(SPAN, NM), Integer.class, source -> source.getMissing() == null ? 0 : source.getMissing().length);
        MAPPING.set(InformationSet.item(ESPAN, START), TsPeriod.class, source -> source.getEstimationDomain().getStartPeriod());
        MAPPING.set(InformationSet.item(ESPAN, END), TsPeriod.class, source -> source.getEstimationDomain().getLastPeriod());
        MAPPING.set(InformationSet.item(ESPAN, N), Integer.class, source -> source.getEstimationDomain().getLength());
        MAPPING.set(LOG, Boolean.class, source -> source.isLogTransformation());
        MAPPING.set(ADJUST, Boolean.class, source -> source.getLpTransformation() != LengthOfPeriodType.None);
        MAPPING.set(ModellingDictionary.Y, TsData.class, source -> source.getOriginalSeries());
//        MAPPING.set(ModellingDictionary.Y + SeriesInfo.F_SUFFIX, source -> source.forecast(source.getForecastCount(), false));
//        MAPPING.set(ModellingDictionary.Y + SeriesInfo.EF_SUFFIX, source -> source.getForecastError());
        MAPPING.set(ModellingDictionary.YC, TsData.class, source -> source.interpolatedSeries(false));
//        MAPPING.set(ModellingDictionary.YC + SeriesInfo.F_SUFFIX, source -> source.forecast(source.getForecastCount(), false));
//        MAPPING.set(ModellingDictionary.YC + SeriesInfo.EF_SUFFIX, source -> source.getForecastError());
        MAPPING.set(ModellingDictionary.L, TsData.class, source -> source.backTransform(source.linearizedSeries(), true));
        MAPPING.set(ModellingDictionary.Y_LIN, TsData.class, source -> source.linearizedSeries());
//        MAPPING.set(ModellingDictionary.Y_LIN + SeriesInfo.F_SUFFIX, source -> source.linearizedForecast(source.domain(true).getLength(), true));
//        MAPPING.set(ModellingDictionary.YCAL, TsData.class, source -> source.getYcal(false));
//        MAPPING.set(ModellingDictionary.YCAL + SeriesInfo.F_SUFFIX, source -> source.getYcal(true));
        MAPPING.set(ModellingDictionary.DET, TsData.class, source -> source.getDeterministicEffect(source.getEstimationDomain()));
//        MAPPING.set(ModellingDictionary.DET + SeriesInfo.F_SUFFIX, source -> source.getDet(true));
//        MAPPING.set(ModellingDictionary.L + SeriesInfo.F_SUFFIX, source -> source.linearizedForecast(source.getForecastCount()));
//        MAPPING.set(ModellingDictionary.L + SeriesInfo.B_SUFFIX, source -> source.linearizedBackcast(source.description.getFrequency()));
        MAPPING.set(ModellingDictionary.CAL, TsData.class, source -> source.getCalendareEffect(source.getEstimationDomain()));
//        MAPPING.set(ModellingDictionary.CAL + SeriesInfo.F_SUFFIX, source -> source.getCal(true));
        MAPPING.set(ModellingDictionary.TDE, TsData.class, source -> source.getTradingDaysEffect(source.getEstimationDomain()));
//        MAPPING.set(ModellingDictionary.TDE + SeriesInfo.F_SUFFIX, source -> source.getTde(true));
        MAPPING.set(ModellingDictionary.MHE, TsData.class, source -> source.getMovingHolidayEffect(source.getEstimationDomain()));
//        MAPPING.set(ModellingDictionary.MHE + SeriesInfo.F_SUFFIX, source -> source.getMhe(true));
        MAPPING.set(ModellingDictionary.EE, TsData.class, source -> source.getEasterEffect(source.getEstimationDomain()));
//        MAPPING.set(ModellingDictionary.EE + SeriesInfo.F_SUFFIX, source -> source.getEe(true));
//        MAPPING.set(ModellingDictionary.OMHE, source -> source.getOmhe(false));
//        MAPPING.set(ModellingDictionary.OMHE + SeriesInfo.F_SUFFIX, source -> source.getOmhe(true));
        MAPPING.set(ModellingDictionary.OUT, TsData.class, source -> source.getOutliersEffect(source.getEstimationDomain()));
//        MAPPING.set(ModellingDictionary.REG, TsData.class, source -> source.getReg(false));
//        MAPPING.set(ModellingDictionary.REG + SeriesInfo.F_SUFFIX, source -> source.getReg(true));
//        MAPPING.set(FULLRES, source -> source.getFullResiduals());
//        MAPPING.set(InformationSet.item(REGRESSION, LP), RegressionItem.class, source -> source.getRegressionItem(ILengthOfPeriodVariable.class, 0));
//        MAPPING.set(InformationSet.item(REGRESSION, NTD), Integer.class, source -> {
//            return source.description.countRegressors(var -> var.status.isSelected() && var.getVariable() instanceof ICalendarVariable);
//        });
//        MAPPING.set(InformationSet.item(REGRESSION, NMH), Integer.class, source -> {
//            return source.description.countRegressors(var -> var.status.isSelected() && var.getVariable() instanceof IMovingHolidayVariable);
//        });
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
//        MAPPING.set(InformationSet.item(REGRESSION, EASTER), RegressionItem.class, source -> source.getRegressionItem(IEasterVariable.class, 0));
//        MAPPING.set(InformationSet.item(REGRESSION, NOUT), Integer.class, source -> source.description.getOutliers().size() + source.description.getPrespecifiedOutliers().size());
//        MAPPING.set(InformationSet.item(REGRESSION, NOUTAO), Integer.class, source -> {
//            TsVariableList vars = source.vars();
//            return vars.select(OutlierType.AO).getItemsCount();
//        });
//        MAPPING.set(InformationSet.item(REGRESSION, NOUTLS), Integer.class, source -> {
//            TsVariableList vars = source.vars();
//            return vars.select(OutlierType.LS).getItemsCount();
//        });
//        MAPPING.set(InformationSet.item(REGRESSION, NOUTTC), Integer.class, source -> {
//            TsVariableList vars = source.vars();
//            return vars.select(OutlierType.TC).getItemsCount();
//        });
//        MAPPING.set(InformationSet.item(REGRESSION, NOUTSO), Integer.class, source -> {
//            TsVariableList vars = source.vars();
//            return vars.select(OutlierType.SO).getItemsCount();
//        });
//        MAPPING.setList(InformationSet.item(REGRESSION, OUT), 1, 31, RegressionItem.class, (source, i) -> source.getRegressionItem(IOutlierVariable.class, i - 1));
//        MAPPING.setList(InformationSet.item(REGRESSION, USER), 1, 31, RegressionItem.class, (source, i) -> source.getRegressionItem(IUserTsVariable.class, i - 1));
//        MAPPING.set(InformationSet.item(REGRESSION, COEFF), Parameter[].class, source -> {
//            double[] c = source.estimation.getLikelihood().getB();
//            if (c == null) {
//                return new Parameter[0];
//            }
//            Parameter[] C = new Parameter[c.length];
//            double[] e = source.estimation.getLikelihood().getBSer(true, source.description.getArimaComponent().getFreeParametersCount());
//            for (int i = 0; i < C.length; ++i) {
//                Parameter p = new Parameter(c[i], ParameterType.Estimated);
//                p.setStde(e[i]);
//                C[i] = p;
//            }
//            return C;
//        });
//        MAPPING.set(InformationSet.item(REGRESSION, COEFFDESC), String[].class, source -> {
//            ArrayList<String> str = new ArrayList<>();
//            if (source.description.isEstimatedMean()) {
//                str.add("Mean");
//            }
//            int[] missings = source.description.getMissingValues();
//            if (missings != null) {
//                for (int i = 0; i < missings.length; ++i) {
//                    int pos = missings[i];
//                    TsPeriod period = source.description.getEstimationDomain().get(pos);
//                    str.add("Missing: " + period.toString());
//                }
//            }
//            ITsVariable[] items = source.vars().items();
//            TsFrequency context = source.description.getEstimationDomain().getFrequency();
//            for (ITsVariable var : items) {
//                for (int j = 0; j < var.getDim(); ++j) {
//                    str.add(var.getItemDescription(j, context));
//                }
//            }
//            String[] desc = new String[str.size()];
//            return str.toArray(desc);
//        });
//        MAPPING.set(InformationSet.item(REGRESSION, COVAR), Matrix.class,
//                source -> source.estimation.getLikelihood().getBVar(true, source.description.getArimaComponent().getFreeParametersCount()));
//        MAPPING.set(InformationSet.item(REGRESSION, PCOVAR), Matrix.class, source -> source.estimation.getParametersCovariance());
//        MAPPING.set(FCASTS, -2, TsData.class, (source, i) -> source.forecast(nperiods(source, i), false));
//        MAPPING.set(BCASTS, -2, TsData.class, (source, i) -> source.backcast(nperiods(source, i), false));
//        MAPPING.set(LIN_FCASTS, -2, TsData.class, (source, i) -> source.linearizedForecast(nperiods(source, i)));
//        MAPPING.set(LIN_BCASTS, -2, TsData.class, (source, i) -> source.linearizedBackcast(nperiods(source, i)));
//        MAPPING.set(EFCASTS, -2, TsData.class, (source, i) ->
//        {
//            int np = nperiods(source, i);
//            TsDomain fdomain = new TsDomain(source.description.getSeriesDomain().getEnd(), np);
//            Forecasts fcasts = source.forecasts(np);
//            double[] ef;
//            if (source.isMultiplicative()) {
//                LogForecasts lf = new LogForecasts(fcasts);
//                ef = lf.getForecastStdevs();
//            } else {
//                ef = fcasts.getForecastStdevs();
//            }
//            return new TsData(fdomain.getStart(), ef, true);
//        });

    }
}
