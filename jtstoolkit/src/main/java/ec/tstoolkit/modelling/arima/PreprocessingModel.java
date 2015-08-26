/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.arima.estimation.ArmaFunction;
import ec.tstoolkit.arima.estimation.Forecasts;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.CoefficientEstimation;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.RegressionItem;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.DeterministicComponent;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.modelling.UserVariable;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.x13.UscbForecasts;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.regression.EasterVariable;
import ec.tstoolkit.timeseries.regression.ICalendarVariable;
import ec.tstoolkit.timeseries.regression.ILengthOfPeriodVariable;
import ec.tstoolkit.timeseries.regression.IMovingHolidayVariable;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.MissingValueEstimation;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.regression.TsVariableSelection.Item;
import ec.tstoolkit.timeseries.simplets.ITsDataTransformation;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.utilities.Arrays2;
import ec.tstoolkit.utilities.Jdk6;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class PreprocessingModel implements IProcResults {

    public static ComponentType outlierComponent(OutlierType type) {
        switch (type) {
            case AO:
                return ComponentType.Irregular;
            case TC:
                return ComponentType.Irregular;
            case LS:
                return ComponentType.Trend;
            case SO:
                return ComponentType.Seasonal;
            default:
                return ComponentType.Undefined;
        }
    }

    public static OutlierType[] outlierTypes(ComponentType cmp) {
        switch (cmp) {
            case Trend:
                return new OutlierType[]{OutlierType.LS};
            case Seasonal:
                return new OutlierType[]{OutlierType.SO};
            case Irregular:
                return new OutlierType[]{OutlierType.AO, OutlierType.TC};
            default:
                return new OutlierType[]{};
        }
    }

    public final ModelDescription description;
    public final ModelEstimation estimation;
    public InformationSet info_;
    private List<ProcessingInformation> log_ = new ArrayList<>();

    public void backTransform(TsData s, boolean T, boolean S) {
        if (s == null) {
            return;
        }
        List<ITsDataTransformation> back = description.backTransformations(T, S);
        for (ITsDataTransformation t : back) {
            t.transform(s, null);
        }
    }

    public PreprocessingModel(ModelDescription description, ModelEstimation estimation) {
        this.description = description;
        this.estimation = estimation;
    }

    public void updateModel() {
        IReadDataBlock p = estimation.getArima().getParameters();
        DataBlock e = new DataBlock(p.getLength());
        // update the standard deviation
        if (estimation.getParametersCovariance() != null) {
            DataBlock diag = estimation.getParametersCovariance().diagonal();
            for (int i = 0; i < e.getLength(); ++i) {
                double var = diag.get(i);
                e.set(i, var <= 0 ? 0 : Math.sqrt(var));
            }
            description.getArimaComponent().setParameters(p, e, ParameterType.Estimated);
        } else { // update only the parameters
            description.getArimaComponent().setParameters(p, e, ParameterType.Initial);
        }
    }

    public void addProcessingInformation(ProcessingInformation info) {
        log_.add(info);
    }

    public void addProcessingInformation(Collection<ProcessingInformation> info) {
        if (log_ != null && info != null) {
            log_.addAll(info);
        }
    }

    public IFunction likelihoodFunction() {
        RegArimaModel<SarimaModel> regArima = estimation.getRegArima();
        ArmaFunction fn = new ArmaFunction<>(regArima.getDModel(), regArima.getArima().getDifferenceOrder(), regArima.getMissings(), description.defaultMapping());
        fn.llog = true;
        return fn;
    }

    public IFunctionInstance maxLikelihoodFunction() {
        IFunction fn = likelihoodFunction();
        return fn.evaluate(description.defaultMapping().map(estimation.getArima()));
    }

    public MissingValueEstimation[] missings(boolean unbiased) {
        int[] missings = description.getMissingValues();
        if (missings != null) {
            MissingValueEstimation[] m = new MissingValueEstimation[missings.length];
            ConcentratedLikelihood ll = estimation.getLikelihood();
            double[] b = ll.getB();
            int nhp = description.getArimaComponent().getFreeParametersCount();
            double[] se = ll.getBSer(unbiased, nhp);
            int istart = description.isMean() ? 1 : 0;
            for (int i = 0; i < missings.length; ++i) {
                int pos = missings[i];
                TsPeriod period = description.getEstimationDomain().get(pos);
                double val = description.getY()[pos] - b[istart + i];
                MissingValueEstimation cur = new MissingValueEstimation(period, val, se[istart + i]);
                m[i] = cur;
            }
            Arrays.sort(m);
            return m;
        } else {
            return null;
        }
    }

    public TsData interpolatedSeries(boolean bTransformed) {
        TsData data;
        if (!bTransformed) {
            data = description.getOriginal();
        } else {
            data = description.transformedOriginal();
        }

        // complete for missings
        int[] missings = description.getMissingValues();
        if (missings != null) {
            if (estimation == null) {
                return null;
            }
            List<ITsDataTransformation> back;
            if (!bTransformed) {
                back = description.backTransformations(true, true);
            }
            double[] b = estimation.getLikelihood().getB();
            int istart = description.isMean() ? 1 : 0;
            int del = description.getEstimationDomain().getStart().minus(description.getSeriesDomain().getStart());
            for (int i = 0; i < missings.length; ++i) {
                int pos = missings[i];
                double val = description.getY()[pos] - b[istart + i];
                if (!bTransformed) {
                    TsData tmp = new TsData(description.getEstimationDomain().get(pos), 1);
                    tmp.set(0, val);
                    backTransform(tmp, true, true);
                    data.set(del + pos, tmp.get(0));
                } else {
                    data.set(del + pos, val);
                }
            }
        }
        return data;
    }

    public TsData linearizedSeries() {
        if (lin_ != null) {
            return lin_.clone();
        }
        if (estimation == null) {
            return description.transformedOriginal();
        }
        TsData interp = interpolatedSeries(true);
        TsData regs = regressionEffect(description.getSeriesDomain());
        lin_ = TsData.subtract(interp, regs);
        return lin_.clone();
    }

    public TsData linearizedSeries(boolean includeUndefinedReg) {
        TsData s = linearizedSeries();
        if (includeUndefinedReg) {
            TsData reg = userEffect(s.getDomain(), ComponentType.Undefined);
            s = TsData.add(s, reg);
        }
        return s;
    }

    // cmp is used in back transformation
    public TsData regressionEffect(TsDomain domain) {
        if (estimation == null) {
            return null;
        }
        double[] coeffs = estimation.getLikelihood().getB();
        if (coeffs == null) {
            return new TsData(domain, 0);
        } else {
            int istart = description.getRegressionVariablesStartingPosition();

            TsVariableSelection sel = vars().all();
            DataBlock sum = sel.sum(new DataBlock(coeffs, istart, coeffs.length, 1), domain);

            if (sum == null) {
                sum = new DataBlock(domain.getLength());
            }
            TsData rslt = new TsData(domain.getStart(), sum.getData(), false);
            return rslt;
        }
    }

    // cmp is used in back transformation
    public <T extends ITsVariable> TsData regressionEffect(TsDomain domain, Class<T> tclass) {
        if (estimation == null) {
            return null;
        }
        TsVariableSelection sel = vars().selectCompatible(tclass);
        if (sel.isEmpty()) {
            return new TsData(domain, 0);
        }
        double[] coeffs = estimation.getLikelihood().getB();
        int istart = description.getRegressionVariablesStartingPosition();

        DataBlock sum = sel.sum(new DataBlock(coeffs, istart, coeffs.length, 1), domain);

        if (sum == null) {
            sum = new DataBlock(domain.getLength());
        }
        TsData rslt = new TsData(domain.getStart(), sum.getData(), false);
        return rslt;
    }

    private TsData regressionEffect(TsDomain domain, TsVariableList.ISelector selector) {
        TsVariableList list = vars();
        if (list.isEmpty()) {
            return new TsData(domain, 0);
        }
        TsVariableSelection<ITsVariable> sel = list.select(selector);
        if (sel.isEmpty()) {
            return new TsData(domain, 0);
        }
        double[] coeffs = estimation.getLikelihood().getB();
        int istart = description.getRegressionVariablesStartingPosition();

        DataBlock sum = sel.sum(new DataBlock(coeffs, istart, coeffs.length, 1), domain);

        if (sum == null) {
            sum = new DataBlock(domain.getLength());
        }
        TsData rslt = new TsData(domain.getStart(), sum.getData(), false);
        return rslt;
    }

    public TsData tradingDaysEffect(TsDomain domain) {
        return regressionEffect(domain, new TsVariableList.ISelector() {
            @Override
            public boolean accept(ITsVariable var) {
                Variable x = Variable.search(description.getCalendars(),
                        var);
                return x != null;
            }
        });
        // return regressionEffect(domain, ICalendarVariable.class);
    }

    public TsData movingHolidaysEffect(TsDomain domain) {
        return regressionEffect(domain, new TsVariableList.ISelector() {
            @Override
            public boolean accept(ITsVariable var) {
                Variable x = Variable.search(description.getMovingHolidays(),
                        var);
                return x != null;
            }
        });
//        return regressionEffect(domain, IMovingHolidayVariable.class);
    }

    public TsData outliersEffect(TsDomain domain) {
        return regressionEffect(domain, IOutlierVariable.class);
    }

    public TsData outliersEffect(TsDomain domain, final ComponentType type) {
        if (estimation == null) {
            return null;
        }
        if (type == ComponentType.Undefined) {
            return outliersEffect(domain);
        }
        OutlierType[] types = outlierTypes(type);
        TsData rslt = null;
        for (int i = 0; i < types.length; ++i) {
            TsVariableSelection sel = vars().select(types[i]);
            if (!sel.isEmpty()) {
                double[] coeffs = estimation.getLikelihood().getB();
                int istart = description.getRegressionVariablesStartingPosition();

                DataBlock sum = sel.sum(new DataBlock(coeffs, istart, coeffs.length, 1), domain);

                if (sum != null) {
                    rslt = TsData.add(rslt, new TsData(domain.getStart(), sum.getData(), false));
                }
            }
        }
        return rslt;

    }

    private static final OutlierEstimation[] NO_OUTLIER = new OutlierEstimation[0];

    public OutlierEstimation[] outliersEstimation(boolean unbiased, boolean prespecified) {
        ConcentratedLikelihood ll = estimation.getLikelihood();
        if (ll == null) {
            return null; // BUG
        }
        double[] b = ll.getB();
        if (b == null) {
            return NO_OUTLIER;
        }
        int nhp = description.getArimaComponent().getFreeParametersCount();
        double[] se = ll.getBSer(unbiased, nhp);
        int istart = description.getRegressionVariablesStartingPosition();

        TsVariableSelection<IOutlierVariable> sel = vars().select(IOutlierVariable.class);
        ArrayList<OutlierEstimation> o = new ArrayList<>();
        for (TsVariableSelection.Item<IOutlierVariable> cur : sel.elements()) {
            if (cur.variable.isPrespecified() == prespecified) {
                int rpos = cur.position + istart;
                CoefficientEstimation c = new CoefficientEstimation(b[rpos], se[rpos]);
                o.add(new OutlierEstimation(c, cur.variable));
            }
        }
        return o.isEmpty() ? NO_OUTLIER : Jdk6.Collections.toArray(o, OutlierEstimation.class);
    }

    public List<TsData> regressors(TsDomain domain) {
        ArrayList<TsData> regs = new ArrayList<>();
        List<DataBlock> data = vars().all().data(domain);
        for (DataBlock d : data) {
            double[] cur = new double[domain.getLength()];
            d.copyTo(cur, 0);
            regs.add(new TsData(domain.getStart(), cur, false));
        }
        return regs;
    }

    public TsData deterministicEffect(TsDomain domain, final ComponentType type) {
        if (estimation == null || estimation.getLikelihood() == null) {
            return null;
        }
        double[] coeffs = estimation.getLikelihood().getB();
        TsVariableList list = vars();
        if (list.isEmpty()) {
            return new TsData(domain, 0);
        }
        TsVariableSelection<ITsVariable> sel = list.select(
                new TsVariableList.ISelector() {
                    @Override
                    public boolean accept(ITsVariable var) {
                        return description.getType(var) == type;
                    }
                });

        if (sel.isEmpty()) {
            return new TsData(domain, 0);
        }

        int istart = description.getRegressionVariablesStartingPosition();

        DataBlock sum = sel.sum(new DataBlock(coeffs, istart, coeffs.length, 1), domain);

        if (sum == null) {
            sum = new DataBlock(domain.getLength());
        }
        TsData rslt = new TsData(domain.getStart(), sum.getData(), false);
        return rslt;
    }

    public TsData userEffect(TsDomain domain, final ComponentType type) {
        TsVariableList list = vars();
        if (list.isEmpty()) {
            return new TsData(domain, 0);
        }
        TsVariableSelection<ITsVariable> sel = list.select(
                new TsVariableList.ISelector() {
                    @Override
                    public boolean accept(ITsVariable var) {
                        Variable x = Variable.search(description.getUserVariables(),
                                var);
                        return x != null && x.type == type;
                    }
                });

        if (sel.isEmpty()) {
            return new TsData(domain, 0);
        }
        double[] coeffs = estimation.getLikelihood().getB();
        int istart = description.getRegressionVariablesStartingPosition();

        DataBlock sum = sel.sum(new DataBlock(coeffs, istart, coeffs.length, 1), domain);

        if (sum == null) {
            sum = new DataBlock(domain.getLength());
        }
        TsData rslt = new TsData(domain.getStart(), sum.getData(), false);
        return rslt;
    }

    public TsData userEffect(TsDomain domain) {
        TsVariableList list = vars();
        if (list.isEmpty()) {
            return new TsData(domain, 0);
        }
        TsVariableSelection<ITsVariable> sel = list.select(
                new TsVariableList.ISelector() {
                    @Override
                    public boolean accept(ITsVariable var) {
                        Variable x = Variable.search(description.getUserVariables(),
                                var);
                        return x != null;
                    }
                });

        if (sel.isEmpty()) {
            return new TsData(domain, 0);
        }
        double[] coeffs = estimation.getLikelihood().getB();
        int istart = description.getRegressionVariablesStartingPosition();

        DataBlock sum = sel.sum(new DataBlock(coeffs, istart, coeffs.length, 1), domain);

        if (sum == null) {
            sum = new DataBlock(domain.getLength());
        }
        TsData rslt = new TsData(domain.getStart(), sum.getData(), false);
        return rslt;
    }

    public TsData linearizedForecast(int nf) {
        if (fcast_ != null && nf <= fcast_.getLength()) {
            return fcast_.drop(0, fcast_.getLength() - nf);
        }
        TsData s = linearizedSeries(false);
        DataBlock data = new DataBlock(s.getValues().internalStorage());
        // FastArimaForecasts fcast = new FastArimaForecasts(model, false);
        double mean = description.isMean() ? estimation.getLikelihood().getB()[0]
                : 0;
        UscbForecasts fcast = new UscbForecasts(estimation.getArima(), mean);
        double[] forecasts = fcast.forecasts(data, nf);
        TsData fs = new TsData(s.getEnd(), forecasts, false);
        fcast_ = fs.clone();
        return fs;
    }

    public TsData linearizedForecast(int nf, boolean includeUndefinedReg) {
        TsData s = linearizedForecast(nf);
        if (includeUndefinedReg) {
            TsData reg = userEffect(s.getDomain(), ComponentType.Undefined);
            s = TsData.add(s, reg);
        }
        return s;
    }

    public TsData linearizedBackcast(int nf, boolean includeUndefinedReg) {
        TsData s = linearizedBackcast(nf);
        if (includeUndefinedReg) {
            TsData reg = userEffect(s.getDomain(), ComponentType.Undefined);
            s = TsData.add(s, reg);
        }
        return s;
    }

    public Forecasts forecasts(int nf) {
        if (xfcasts_ != null && nf <= xfcasts_.getForecastsCount()) {
            return xfcasts_;
        }
        xfcasts_ = new Forecasts();
        TsDomain fdomain = new TsDomain(description.getEstimationDomain().getEnd(), nf);
        RegArimaEstimation<SarimaModel> est
                = new RegArimaEstimation<>(estimation.getRegArima(),
                        estimation.getLikelihood());
        xfcasts_.calcForecast(est, vars().all().data(fdomain), nf, description.getArimaComponent().getFreeParametersCount());
        return xfcasts_;
    }

    public TsData linearizedBackcast(int nb) {
        if (bcast_ != null && nb <= bcast_.getLength()) {
            return bcast_.drop(bcast_.getLength() - nb, 0);
        }
        TsData s = linearizedSeries(false);
        DataBlock data = new DataBlock(s.getValues().internalStorage()).reverse();
        // FastArimaForecasts fcast = new FastArimaForecasts(model, false);
        double mean = description.isMean() ? estimation.getLikelihood().getB()[0]
                : 0;
        UscbForecasts fcast = new UscbForecasts(estimation.getArima(), mean);
        double[] backcasts = fcast.forecasts(data, nb);
        Arrays2.reverse(backcasts);
        TsData bs = new TsData(s.getStart().minus(nb), backcasts, false);
        bcast_ = bs.clone();
        return bs;
    }

    public TsData forecast(int nf, boolean transformed) {
        TsData f = linearizedForecast(nf);
        TsData c = regressionEffect(f.getDomain());
        TsData r = TsData.add(f, c);

        if (!transformed) {
            backTransform(r, true, true);
        }
        return r;
    }

    public TsData backcast(int nb, boolean transformed) {
        TsData b = linearizedBackcast(nb);
        TsData c = regressionEffect(b.getDomain());
        TsData r = TsData.add(b, c);
        if (!transformed) {
            backTransform(r, true, true);
        }
        return r;
    }

    public DeterministicComponent getDeterministicComponent() {
        DeterministicComponent det = new DeterministicComponent();
        det.setOriginal(description.getOriginal());
        det.setY(this.interpolatedSeries(true));
        det.setLengthOfPeriodAdjustment(description.getLengthOfPeriodType());
        det.setTransformation(description.getTransformation());
        det.setUnits(description.getUnits());
        // add the regression variables
        // users...
        TsDomain estimationDomain = description.getEstimationDomain();
        for (Variable var : description.getUserVariables()) {
            if (var.status.isSelected() && var.getVariable().isSignificant(estimationDomain)) {
                det.add(new UserVariable(var.getVariable(), var.type));
            }
        }
        // calendars...
        for (Variable var : description.getCalendars()) {
            if (var.status.isSelected()) {
                if (var.getVariable() instanceof ILengthOfPeriodVariable) {
                    det.add((ILengthOfPeriodVariable) var.getVariable());
                } else if (var.getVariable() instanceof ICalendarVariable) {
                    det.add((ICalendarVariable) var.getVariable());
                } else {
                    det.add(UserVariable.tradingDays(var.getVariable()));
                }
            }
        }
        // moving holidays...
        for (Variable var : description.getMovingHolidays()) {
            if (var.status.isSelected()) {
                if (var.getVariable() instanceof IMovingHolidayVariable) {
                    det.add((IMovingHolidayVariable) var.getVariable());
                } else {
                    det.add(UserVariable.movingHoliday(var.getVariable()));
                }
            }
        }

        for (IOutlierVariable var : description.getOutliers()) {
            if (var.isSignificant(estimationDomain)) {
                det.add(var);

            }
        }
        for (IOutlierVariable var : description.getPrespecifiedOutliers()) {
            if (var.isSignificant(estimationDomain)) {
                det.add(var);

            }
        }
        double[] b = estimation.getLikelihood().getB();
        if (b != null) {
            int start = description.getRegressionVariablesStartingPosition();
            det.setCoefficients(new DataBlock(b, start, b.length, 1));
        }
        return det;
    }

    @Override
    public Map<String, Class> getDictionary() {
        return dictionary();
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        if (mapper.contains(id)) {
            return mapper.getData(this, id, tclass);
        }
        if (estimation.contains(id)) {
            return estimation.getData(id, tclass);
        }
        if (info_ != null) {
            if (!id.contains(InformationSet.STRSEP)) {
                return info_.deepSearch(id, tclass);
            } else {
                return info_.search(id, tclass);
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean contains(String id) {
        synchronized (mapper) {
            if (mapper.contains(id)) {
                return true;
            }
            if (estimation.contains(id)) {
                return true;
            }
            if (info_ != null) {
                if (!id.contains(InformationSet.STRSEP)) {
                    return info_.deepSearch(id, Object.class) != null;
                } else {
                    return info_.search(id, Object.class) != null;
                }

            } else {
                return false;
            }
        }
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return log_ == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(log_);
    }

    public static void fillDictionary(String prefix, Map<String, Class> map) {
        mapper.fillDictionary(prefix, map);
        ModelEstimation.fillDictionary(prefix, map);
    }

    public static Map<String, Class> dictionary() {
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        fillDictionary(null, map);
        return map;
    }

    private TsData op(TsData l, TsData r) {
        if (description.getTransformation() == DefaultTransformationType.Log) {
            return TsData.multiply(l, r);
        } else {
            return TsData.add(l, r);
        }
    }

    private TsData inv_op(TsData l, TsData r) {
        if (description.getTransformation() == DefaultTransformationType.Log) {
            return TsData.divide(l, r);
        } else {
            return TsData.subtract(l, r);
        }
    }

    private TsData getForecastError() {
        TsDomain fdomain = domain(true);
        Forecasts fcasts = forecasts(fdomain.getLength());
        double[] ef;
        if (isMultiplicative()) {
            LogForecasts lf = new LogForecasts(fcasts);
            ef = lf.getForecatStdevs();
        } else {
            ef = fcasts.getForecastStdevs();
        }
        return new TsData(fdomain.getStart(), ef, true);
    }

//    private TsData getSeries(String id, boolean fcast) {
//        if (id.equals(ModellingDictionary.Y) && fcast) {
//            return forecast(domain(true).getLength(), false);
//        }
//        if (id.equals(ModellingDictionary.YC)) {
//            return interpolatedSeries(false);
//        } else if (id.equals(ModellingDictionary.L)) {
//            return linearizedSeries();
//        } else if (id.equals(ModellingDictionary.Y_LIN)) {
//            return isMultiplicative() ? linearizedSeries().exp() : linearizedSeries();
//        } else if (id.equals(ModellingDictionary.CAL)) {
//            return getCal(fcast);
//        } else if (id.equals(ModellingDictionary.DET)) {
//            return getDet(fcast);
//        } else if (id.equals(ModellingDictionary.EE)) {
//            return getEe(fcast);
//        } else if (id.equals(ModellingDictionary.MHE)) {
//            return getMhe(fcast);
//        } else if (id.equals(ModellingDictionary.RMDE)) {
//            return getRmde(fcast);
//        } else if (id.equals(ModellingDictionary.OMHE)) {
//            return getOmhe(fcast);
//        } else if (id.equals(ModellingDictionary.OUT)) {
//            return getOutlier(ComponentType.Undefined, fcast);
//        } else if (id.equals(ModellingDictionary.OUT_I)) {
//            return getOutlier(ComponentType.Irregular, fcast);
//        } else if (id.equals(ModellingDictionary.OUT_S)) {
//            return getOutlier(ComponentType.Seasonal, fcast);
//        } else if (id.equals(ModellingDictionary.OUT_T)) {
//            return getOutlier(ComponentType.Trend, fcast);
//        } else if (id.equals(ModellingDictionary.REG)) {
//            return getReg(ComponentType.Undefined, fcast);
//        } else if (id.equals(ModellingDictionary.REG_I)) {
//            return getReg(ComponentType.Irregular, fcast);
//        } else if (id.equals(ModellingDictionary.REG_S)) {
//            return getReg(ComponentType.Seasonal, fcast);
//        } else if (id.equals(ModellingDictionary.REG_SA)) {
//            return getReg(ComponentType.Trend, fcast);
//        } else if (id.equals(ModellingDictionary.REG_T)) {
//            return getReg(ComponentType.Trend, fcast);
//        } else if (id.equals(ModellingDictionary.REG_Y)) {
//            return getReg(ComponentType.Series, fcast);
//        } else if (id.equals(ModellingDictionary.YCAL)) {
//            return getYcal(fcast);
//        } else if (id.equals(ModellingDictionary.TDE)) {
//            return getTde(fcast);
//        }
//        return null;
//    }
    private TsData getTde(boolean fcast) {
        TsDomain fdom = domain(fcast);
        TsData tmp = tradingDaysEffect(fdom);
        if (tmp == null) {
            return null;
        }
//        DescriptiveStatistics stats = new DescriptiveStatistics(tmp.getValues());
//        if (stats.isConstant()) {
//            return null;
//        }
        backTransform(tmp, false, true);
        return tmp;
    }

    private TsData getYcal(boolean fcast) {
        return inv_op(fcast ? forecast(getForecastCount(), false) : interpolatedSeries(false), getCal(fcast));
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    private TsData getReg(boolean fcast) {
        TsData tmp = userEffect(domain(fcast));
        if (tmp == null) {
            return null;
        }
        backTransform(tmp, false, false);
        return tmp;
    }

    private TsData getReg(ComponentType componentType, boolean fcast) {
        TsData tmp = userEffect(domain(fcast), componentType);
        if (tmp == null) {
            return null;
        }
        backTransform(tmp, false, false);
        return tmp;
    }

    private TsData getOutlier(ComponentType componentType, boolean fcast) {
        TsData tmp = outliersEffect(domain(fcast), componentType);
        if (tmp == null) {
            return null;
        }
//        DescriptiveStatistics stats = new DescriptiveStatistics(tmp.getValues());
//        if (stats.isConstant()) {
//            return null;
//        }
        backTransform(tmp, false, false);
        return tmp;
    }

    private TsData getOmhe(boolean fcast) {
        TsData tmp = inv_op(getMhe(fcast), op(getEe(fcast), getRmde(fcast)));
        if (tmp == null) {
            return null;
        }
        return tmp;
    }

    private TsData getMhe(boolean fcast) {
        TsData tmp = movingHolidaysEffect(domain(fcast));
        if (tmp == null) {
            return null;
        }
//        DescriptiveStatistics stats = new DescriptiveStatistics(tmp.getValues());
//        if (stats.isConstant()) {
//            return null;
//        }
        backTransform(tmp, false, false);
        return tmp;
    }

    private TsData getEe(boolean fcast) {
        TsData tmp = regressionEffect(domain(fcast), EasterVariable.class);
        if (tmp == null) {
            return null;
        }
//        DescriptiveStatistics stats = new DescriptiveStatistics(tmp.getValues());
//        if (stats.isConstant()) {
//            return null;
//        }
        backTransform(tmp, false, false);
        return tmp;
    }

    private TsData getDet(boolean fcast) {
        TsData tmp = regressionEffect(domain(fcast));
        if (tmp == null) {
            return null;
        }
        backTransform(tmp, false, true);
        return tmp;
    }

    private TsData getCal(boolean fcast) {
        TsData tmp = op(getTde(fcast), getMhe(fcast));
        if (tmp == null) {
            return null;
        }
        return tmp;
    }

    private TsData getRmde(boolean fcast) {
        return null;
    }

    private int getForecastCount() {
        return FCAST_YEAR * description.getFrequency();
    }

    private TsDomain domain(boolean fcast) {
        if (fcast) {
            TsDomain dom = description.getSeriesDomain();
            return new TsDomain(dom.getEnd(), FCAST_YEAR * dom.getFrequency().intValue());
        } else {
            return description.getSeriesDomain();
        }
    }

    public TsData getFullResiduals() {
        if (fullres_ == null) {
            TsDomain domain = domain(false);
            // compute the residuals
            DataBlock res = estimation.getFullResiduals();
            double[] xres = new double[res.getLength()];
            res.copyTo(xres, 0);
            fullres_ = new TsData(domain.getStart().plus(domain.getLength() - xres.length), xres, false);
        }
        return fullres_;
    }

    private TsVariableList vars() {
        if (x_ == null) {
            x_ = description.buildRegressionVariables();
        }
        return x_;
    }

    public boolean isMultiplicative() {
        return description.getTransformation() == DefaultTransformationType.Log;
    }

    public <T extends ITsVariable> RegressionItem getRegressionItem(Class<T> tclass, int vpos) {
        TsVariableSelection<T> sel = vars().select(tclass);
        if (sel.isEmpty()) {
            return null;
        } else {
            int cur = 0;
            while (cur < sel.getItemsCount()) {
                int l = sel.get(cur).variable.getDim();
                if (vpos < l) {
                    break;
                } else {
                    ++cur;
                    vpos -= l;
                }
            }
            if (cur == sel.getItemsCount()) {
                return null;
            }
            Item<T> item = sel.get(cur);
            int pos = description.getRegressionVariablesStartingPosition() + item.position + vpos;
            double c = estimation.getLikelihood().getB()[pos];
            double e = estimation.getLikelihood().getBSer(pos, true, description.getArimaComponent().getFreeParametersCount());
            return new RegressionItem(item.variable.getItemDescription(vpos), false, c, e);
        }
    }
    // some caching...
    private TsVariableList x_;
    private TsData fullres_, lin_, fcast_, bcast_;
    private Forecasts xfcasts_;
    public static final int FCAST_YEAR = 1;
    public static final String LOG = "log",
            ADJUST = "adjust",
            SPAN = "span", ESPAN = "espan", START = "start", END = "end", N = "n",
            REGRESSION = "regression",
            OUTLIERS = "outlier(*)",
            CALENDAR = "calendar(*)",
            EASTER = "easter",
            FULLRES = "fullresiduals",
            FCASTS = "fcasts",
            BCASTS = "bcasts",
            LIN_FCASTS = "lin_fcasts",
            LIN_BCASTS = "lin_bcasts",
            NTD = "ntd", NMH = "nmh",
            TD1 = "td(1)", TD2 = "td(2)", TD3 = "td(3)", TD4 = "td(4)", TD5 = "td(5)", TD6 = "td(6)", TD7 = "td(7)",
            TD8 = "td(8)", TD9 = "td(9)", TD10 = "td(10)", TD11 = "td(11)", TD12 = "td(12)", TD13 = "td(13)", TD14 = "td(14)",
            LP = "lp", OUT1 = "out(1)", OUT2 = "out(2)", OUT3 = "out(3)", OUT4 = "out(4)", OUT5 = "out(5)", OUT6 = "out(6)", OUT7 = "out(7)",
            NOUT = "nout", NOUTAO = "noutao", NOUTLS = "noutls", NOUTTC = "nouttc", NOUTSO = "noutso",
            OUT8 = "out(8)", OUT9 = "out(9)", OUT10 = "out(10)", OUT11 = "out(11)", OUT12 = "out(12)", OUT13 = "out(13)", OUT14 = "out(14)",
            OUT15 = "out(15)", OUT16 = "out(16)", OUT17 = "out(17)", OUT18 = "out(18)", OUT19 = "out(19)", OUT20 = "out(20)",
            OUT21 = "out(21)", OUT22 = "out(22)", OUT23 = "out(23)", OUT24 = "out(24)", OUT25 = "out(25)", OUT26 = "out(26)",
            OUT27 = "out(27)", OUT28 = "out(28)", OUT29 = "out(29)", OUT30 = "out(30)",
            COEFF = "coefficients", COVAR = "covar", COEFFDESC = "description", PCOVAR = "pcovar";

    ;
    // MAPPERS

    public static <T> void addMapping(String name, InformationMapper.Mapper<PreprocessingModel, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }
    private static final InformationMapper<PreprocessingModel> mapper = new InformationMapper<>();

    static {
        mapper.add(InformationSet.item(SPAN, START), new InformationMapper.Mapper<PreprocessingModel, TsPeriod>(TsPeriod.class) {
            @Override
            public TsPeriod retrieve(PreprocessingModel source) {
                return source.description.getSeriesDomain().getStart();
            }
        });
        mapper.add(InformationSet.item(SPAN, END), new InformationMapper.Mapper<PreprocessingModel, TsPeriod>(TsPeriod.class) {
            @Override
            public TsPeriod retrieve(PreprocessingModel source) {
                return source.description.getSeriesDomain().getLast();
            }
        });
        mapper.add(InformationSet.item(SPAN, N), new InformationMapper.Mapper<PreprocessingModel, Integer>(Integer.class) {
            @Override
            public Integer retrieve(PreprocessingModel source) {
                return source.description.getSeriesDomain().getLength();
            }
        });
        mapper.add(InformationSet.item(ESPAN, START), new InformationMapper.Mapper<PreprocessingModel, TsPeriod>(TsPeriod.class) {
            @Override
            public TsPeriod retrieve(PreprocessingModel source) {
                return source.description.getEstimationDomain().getStart();
            }
        });
        mapper.add(InformationSet.item(ESPAN, END), new InformationMapper.Mapper<PreprocessingModel, TsPeriod>(TsPeriod.class) {
            @Override
            public TsPeriod retrieve(PreprocessingModel source) {
                return source.description.getEstimationDomain().getLast();
            }
        });
        mapper.add(InformationSet.item(ESPAN, N), new InformationMapper.Mapper<PreprocessingModel, Integer>(Integer.class) {
            @Override
            public Integer retrieve(PreprocessingModel source) {
                return source.description.getEstimationDomain().getLength();
            }
        });
        mapper.add(LOG, new InformationMapper.Mapper<PreprocessingModel, Boolean>(Boolean.class) {
            @Override
            public Boolean retrieve(PreprocessingModel source) {
                return source.isMultiplicative();
            }
        });
        mapper.add(ADJUST, new InformationMapper.Mapper<PreprocessingModel, Boolean>(Boolean.class) {
            @Override
            public Boolean retrieve(PreprocessingModel source) {
                if (source.description.getPreadjustmentType() == PreadjustmentType.None) {
                    return null;
                }
                return source.description.getLengthOfPeriodType() != LengthOfPeriodType.None;
            }
        });
        mapper.add(ModellingDictionary.Y, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.description.getOriginal();
            }
        });
        mapper.add(ModellingDictionary.Y + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.forecast(FCAST_YEAR * source.description.getFrequency(), false);
            }
        });
        mapper.add(ModellingDictionary.Y + SeriesInfo.EF_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getForecastError();
            }
        });
        mapper.add(ModellingDictionary.YC, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.interpolatedSeries(false);
            }
        });
        mapper.add(ModellingDictionary.YC + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.forecast(FCAST_YEAR * source.description.getFrequency(), false);
            }
        });
        mapper.add(ModellingDictionary.YC + SeriesInfo.EF_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getForecastError();
            }
        });
        mapper.add(ModellingDictionary.L, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.linearizedSeries(false);
            }
        });
        mapper.add(ModellingDictionary.Y_LIN, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.linearizedSeries(true);
            }
        });
        mapper.add(ModellingDictionary.Y_LIN + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.linearizedForecast(source.domain(true).getLength(), true);
            }
        });
        mapper.add(ModellingDictionary.YCAL, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getYcal(false);
            }
        });
        mapper.add(ModellingDictionary.YCAL + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getYcal(true);
            }
        });
        mapper.add(ModellingDictionary.DET, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getDet(false);
            }
        });
        mapper.add(ModellingDictionary.DET + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getDet(true);
            }
        });
        mapper.add(ModellingDictionary.L + SeriesInfo.F_SUFFIX,
                new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
                    @Override
                    public TsData retrieve(PreprocessingModel source) {
                        return source.linearizedForecast(FCAST_YEAR * source.description.getFrequency());
                    }
                });
        mapper.add(ModellingDictionary.L + SeriesInfo.B_SUFFIX,
                new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
                    @Override
                    public TsData retrieve(PreprocessingModel source) {
                        return source.linearizedBackcast(source.description.getFrequency());
                    }
                });
        mapper.add(ModellingDictionary.CAL, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getCal(false);
            }
        });
        mapper.add(ModellingDictionary.CAL + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getCal(true);
            }
        });
        mapper.add(ModellingDictionary.TDE, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getTde(false);
            }
        });
        mapper.add(ModellingDictionary.TDE + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getTde(true);
            }
        });
        mapper.add(ModellingDictionary.MHE, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getMhe(false);
            }
        });
        mapper.add(ModellingDictionary.MHE + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getMhe(true);
            }
        });
        mapper.add(ModellingDictionary.EE, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getEe(false);
            }
        });
        mapper.add(ModellingDictionary.EE + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getEe(true);
            }
        });
        mapper.add(ModellingDictionary.OMHE, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getOmhe(false);
            }
        });
        mapper.add(ModellingDictionary.OMHE + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getOmhe(true);
            }
        });
        mapper.add(ModellingDictionary.OUT, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getOutlier(ComponentType.Undefined, false);
            }
        });
        mapper.add(ModellingDictionary.OUT + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getOutlier(ComponentType.Undefined, true);
            }
        });
        mapper.add(ModellingDictionary.OUT_I, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getOutlier(ComponentType.Irregular, false);
            }
        });
        mapper.add(ModellingDictionary.OUT_I + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getOutlier(ComponentType.Irregular, true);
            }
        });
        mapper.add(ModellingDictionary.OUT_T, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getOutlier(ComponentType.Trend, false);
            }
        });
        mapper.add(ModellingDictionary.OUT_T + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getOutlier(ComponentType.Trend, true);
            }
        });
        mapper.add(ModellingDictionary.OUT_S, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getOutlier(ComponentType.Seasonal, false);
            }
        });
        mapper.add(ModellingDictionary.OUT_S + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getOutlier(ComponentType.Seasonal, true);
            }
        });
        mapper.add(ModellingDictionary.REG, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getReg(false);
            }
        });
        mapper.add(ModellingDictionary.REG + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getReg(true);
            }
        });
        mapper.add(ModellingDictionary.REG_T, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getReg(ComponentType.Trend, false);
            }
        });
        mapper.add(ModellingDictionary.REG_T + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getReg(ComponentType.Trend, true);
            }
        });
        mapper.add(ModellingDictionary.REG_S, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getReg(ComponentType.Seasonal, false);
            }
        });
        mapper.add(ModellingDictionary.REG_S + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getReg(ComponentType.Seasonal, true);
            }
        });
        mapper.add(ModellingDictionary.REG_I, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getReg(ComponentType.Irregular, false);
            }
        });
        mapper.add(ModellingDictionary.REG_I + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getReg(ComponentType.Irregular, true);
            }
        });
        mapper.add(ModellingDictionary.REG_SA, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getReg(ComponentType.SeasonallyAdjusted, false);
            }
        });
        mapper.add(ModellingDictionary.REG_SA + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getReg(ComponentType.SeasonallyAdjusted, true);
            }
        });
        mapper.add(ModellingDictionary.REG_Y, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getReg(ComponentType.Series, false);
            }
        });
        mapper.add(ModellingDictionary.REG_Y + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getReg(ComponentType.Series, true);
            }
        });
        mapper.add(ModellingDictionary.REG_U, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getReg(ComponentType.Undefined, false);
            }
        });
        mapper.add(ModellingDictionary.REG_U + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getReg(ComponentType.Undefined, true);
            }
        });

        mapper.add(FULLRES, new InformationMapper.Mapper<PreprocessingModel, TsData>(TsData.class) {
            @Override
            public TsData retrieve(PreprocessingModel source) {
                return source.getFullResiduals();
            }
        });

        mapper.add(InformationSet.item(REGRESSION, LP), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(ILengthOfPeriodVariable.class, 0);
            }
        });

        mapper.add(InformationSet.item(REGRESSION, NTD), new InformationMapper.Mapper<PreprocessingModel, Integer>(Integer.class) {
            @Override
            public Integer retrieve(final PreprocessingModel source) {
                TsVariableList vars = source.vars();
                TsVariableSelection<ITsVariable> sel = vars.select(new TsVariableList.ISelector() {
                    @Override
                    public boolean accept(ITsVariable var) {
                        return Variable.search(source.description.getCalendars(), var) != null;
                    }
                });
                return sel.getVariablesCount();
            }
        });
        mapper.add(InformationSet.item(REGRESSION, NMH), new InformationMapper.Mapper<PreprocessingModel, Integer>(Integer.class) {
            @Override
            public Integer retrieve(final PreprocessingModel source) {
                TsVariableList vars = source.vars();
                TsVariableSelection<ITsVariable> sel = vars.select(new TsVariableList.ISelector() {
                    @Override
                    public boolean accept(ITsVariable var) {
                        return Variable.search(source.description.getMovingHolidays(), var) != null;
                    }
                });
                return sel.getVariablesCount();
            }
        });
        mapper.add(InformationSet.item(REGRESSION, TD1), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(ITradingDaysVariable.class, 0);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, TD2), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(ITradingDaysVariable.class, 1);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, TD3), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(ITradingDaysVariable.class, 2);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, TD4), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(ITradingDaysVariable.class, 3);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, TD5), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(ITradingDaysVariable.class, 4);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, TD6), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(ITradingDaysVariable.class, 5);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, TD7), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(ITradingDaysVariable.class, 6);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, TD8), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(ITradingDaysVariable.class, 7);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, TD9), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(ITradingDaysVariable.class, 8);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, TD10), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(ITradingDaysVariable.class, 9);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, TD11), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(ITradingDaysVariable.class, 10);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, TD12), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(ITradingDaysVariable.class, 11);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, TD13), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(ITradingDaysVariable.class, 12);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, TD14), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(ITradingDaysVariable.class, 13);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, EASTER), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(EasterVariable.class, 0);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, NOUT), new InformationMapper.Mapper<PreprocessingModel, Integer>(Integer.class) {
            @Override
            public Integer retrieve(PreprocessingModel source) {
                return source.description.getOutliers().size() + source.description.getPrespecifiedOutliers().size();
            }
        });
        mapper.add(InformationSet.item(REGRESSION, NOUTAO), new InformationMapper.Mapper<PreprocessingModel, Integer>(Integer.class) {
            @Override
            public Integer retrieve(PreprocessingModel source) {
                TsVariableList vars = source.vars();
                return vars.select(OutlierType.AO).getItemsCount();
            }
        });
        mapper.add(InformationSet.item(REGRESSION, NOUTLS), new InformationMapper.Mapper<PreprocessingModel, Integer>(Integer.class) {
            @Override
            public Integer retrieve(PreprocessingModel source) {
                TsVariableList vars = source.vars();
                return vars.select(OutlierType.LS).getItemsCount();
            }
        });
        mapper.add(InformationSet.item(REGRESSION, NOUTTC), new InformationMapper.Mapper<PreprocessingModel, Integer>(Integer.class) {
            @Override
            public Integer retrieve(PreprocessingModel source) {
                TsVariableList vars = source.vars();
                return vars.select(OutlierType.TC).getItemsCount();
            }
        });
        mapper.add(InformationSet.item(REGRESSION, NOUTSO), new InformationMapper.Mapper<PreprocessingModel, Integer>(Integer.class) {
            @Override
            public Integer retrieve(PreprocessingModel source) {
                TsVariableList vars = source.vars();
                return vars.select(OutlierType.SO).getItemsCount();
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT1), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 0);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT2), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 1);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT3), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 2);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT4), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 3);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT5), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 4);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT6), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 5);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT7), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 6);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT8), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 7);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT9), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 8);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT10), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 9);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT11), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 10);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT12), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 11);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT13), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 12);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT14), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 13);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT15), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 14);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT16), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 15);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT17), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 16);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT18), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 17);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT19), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 18);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, OUT20), new InformationMapper.Mapper<PreprocessingModel, RegressionItem>(RegressionItem.class) {
            @Override
            public RegressionItem retrieve(PreprocessingModel source) {
                return source.getRegressionItem(IOutlierVariable.class, 19);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, COEFF), new InformationMapper.Mapper<PreprocessingModel, Parameter[]>(Parameter[].class) {
            @Override
            public Parameter[] retrieve(PreprocessingModel source) {
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
            }
        });
        mapper.add(InformationSet.item(REGRESSION, COEFFDESC), new InformationMapper.Mapper<PreprocessingModel, String[]>(String[].class) {
            @Override
            public String[] retrieve(PreprocessingModel source) {
                ArrayList<String> str = new ArrayList<>();
                if (source.description.isMean()) {
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
                ITsVariable[] items = source.vars().items();
                for (ITsVariable var : items) {
                    for (int j = 0; j < var.getDim(); ++j) {
                        str.add(var.getItemDescription(j));
                    }
                }
                String[] desc = new String[str.size()];
                return str.toArray(desc);
            }
        });
        mapper.add(InformationSet.item(REGRESSION, COVAR), new InformationMapper.Mapper<PreprocessingModel, Matrix>(Matrix.class) {
            @Override
            public Matrix retrieve(PreprocessingModel source) {
                return source.estimation.getLikelihood().getBVar(true, source.description.getArimaComponent().getFreeParametersCount());
            }
        });
        mapper.add(InformationSet.item(REGRESSION, PCOVAR), new InformationMapper.Mapper<PreprocessingModel, Matrix>(Matrix.class) {
            @Override
            public Matrix retrieve(PreprocessingModel source) {
                return source.estimation.getParametersCovariance();
            }
        });
    }
}
