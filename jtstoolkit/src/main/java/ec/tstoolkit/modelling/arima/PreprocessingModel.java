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
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationMapping;
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
import ec.tstoolkit.timeseries.regression.IEasterVariable;
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
import ec.tstoolkit.timeseries.simplets.TsFrequency;
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
import java.util.function.Function;

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
            if (prespecified == description.isPrespecified(cur.variable)) {
                int rpos = cur.position + istart;
                CoefficientEstimation c = new CoefficientEstimation(b[rpos], se[rpos]);
                o.add(new OutlierEstimation(c, cur.variable, description.getEstimationDomain().getFrequency()));
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
        DataBlock data = new DataBlock(s.internalStorage());
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
        DataBlock data = new DataBlock(s.internalStorage()).reverse();
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

        if (transformed) {
            backTransform(r, true, true);
        }
        return r;
    }

    public TsData backcast(int nb, boolean transformed) {
        TsData b = linearizedBackcast(nb);
        TsData c = regressionEffect(b.getDomain());
        TsData r = TsData.add(b, c);
        if (transformed) {
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
        if (mapping.contains(id)) {
            return mapping.getData(this, id, tclass);
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
    public <T> Map<String, T> searchAll(String wc, Class<T> tclass) {
        Map<String, T> all = mapping.searchAll(this, wc, tclass);
        if (info_ != null) {
            List<Information<T>> sel = info_.select(wc, tclass);
            for (Information<T> info: sel){
                all.put(info.name, info.value);
            }
        } 
        Map<String, T> eall = estimation.searchAll(wc, tclass);
        all.putAll(eall);
        return all;
    }
    
    @Override
    public boolean contains(String id) {
        synchronized (mapping) {
            if (mapping.contains(id)) {
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
        mapping.fillDictionary(prefix, map);
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
        TsData tmp = regressionEffect(domain(fcast), IEasterVariable.class);
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

    public TsFrequency getFrequency() {
        return description.getEstimationDomain().getFrequency();
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
            TsFrequency context = description.getEstimationDomain().getFrequency();
            int pos = description.getRegressionVariablesStartingPosition() + item.position + vpos;
            double c = estimation.getLikelihood().getB()[pos];
            double e = estimation.getLikelihood().getBSer(pos, true, description.getArimaComponent().getFreeParametersCount());
            return new RegressionItem(item.variable.getItemDescription(vpos, context), c, e);
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
            TD="td", TD1 = "td(1)", TD2 = "td(2)", TD3 = "td(3)", TD4 = "td(4)", TD5 = "td(5)", TD6 = "td(6)", TD7 = "td(7)",
            TD8 = "td(8)", TD9 = "td(9)", TD10 = "td(10)", TD11 = "td(11)", TD12 = "td(12)", TD13 = "td(13)", TD14 = "td(14)",
            LP = "lp", OUT="out", OUT1 = "out(1)", OUT2 = "out(2)", OUT3 = "out(3)", OUT4 = "out(4)", OUT5 = "out(5)", OUT6 = "out(6)", OUT7 = "out(7)",
            NOUT = "nout", NOUTAO = "noutao", NOUTLS = "noutls", NOUTTC = "nouttc", NOUTSO = "noutso",
            OUT8 = "out(8)", OUT9 = "out(9)", OUT10 = "out(10)", OUT11 = "out(11)", OUT12 = "out(12)", OUT13 = "out(13)", OUT14 = "out(14)",
            OUT15 = "out(15)", OUT16 = "out(16)", OUT17 = "out(17)", OUT18 = "out(18)", OUT19 = "out(19)", OUT20 = "out(20)",
            OUT21 = "out(21)", OUT22 = "out(22)", OUT23 = "out(23)", OUT24 = "out(24)", OUT25 = "out(25)", OUT26 = "out(26)",
            OUT27 = "out(27)", OUT28 = "out(28)", OUT29 = "out(29)", OUT30 = "out(30)",
            COEFF = "coefficients", COVAR = "covar", COEFFDESC = "description", PCOVAR = "pcovar";

    ;
    // MAPPERS
    public static InformationMapping<PreprocessingModel> getMapping(){
        return mapping;
    }

    public static <T> void setMapping(String name, Class<T> tclass, Function<PreprocessingModel, T> extractor) {
        synchronized (mapping) {
            mapping.set(name, tclass, extractor);
        }
    }

    public static <T> void setTsData(String name, Function<PreprocessingModel, TsData> extractor) {
        synchronized (mapping) {
            mapping.set(name, extractor);
        }
    }

    private static final InformationMapping<PreprocessingModel> mapping = new InformationMapping<>(PreprocessingModel.class);

    static {
        mapping.set(InformationSet.item(SPAN, START), TsPeriod.class, source -> source.description.getSeriesDomain().getStart());
        mapping.set(InformationSet.item(SPAN, END), TsPeriod.class, source -> source.description.getSeriesDomain().getLast());
        mapping.set(InformationSet.item(SPAN, N), Integer.class, source -> source.description.getSeriesDomain().getLength());
        mapping.set(InformationSet.item(ESPAN, START), TsPeriod.class, source -> source.description.getEstimationDomain().getStart());
        mapping.set(InformationSet.item(ESPAN, END), TsPeriod.class, source -> source.description.getEstimationDomain().getLast());
        mapping.set(InformationSet.item(ESPAN, N), Integer.class, source -> source.description.getEstimationDomain().getLength());
        mapping.set(LOG, Boolean.class, source -> source.isMultiplicative());
        mapping.set(ADJUST, Boolean.class, source -> {
            if (source.description.getPreadjustmentType() == PreadjustmentType.None) {
                return null;
            } else {
                return source.description.getLengthOfPeriodType() != LengthOfPeriodType.None;
            }
        });
        mapping.set(ModellingDictionary.Y, source -> source.description.getOriginal());
        mapping.set(ModellingDictionary.Y + SeriesInfo.F_SUFFIX, source -> source.forecast(FCAST_YEAR * source.description.getFrequency(), false));
        mapping.set(ModellingDictionary.Y + SeriesInfo.EF_SUFFIX, source -> source.getForecastError());
        mapping.set(ModellingDictionary.YC, source -> source.interpolatedSeries(false));
        mapping.set(ModellingDictionary.YC + SeriesInfo.F_SUFFIX, source -> source.forecast(FCAST_YEAR * source.description.getFrequency(), false));
        mapping.set(ModellingDictionary.YC + SeriesInfo.EF_SUFFIX, source -> source.getForecastError());
        mapping.set(ModellingDictionary.L, source -> source.linearizedSeries(false));
        mapping.set(ModellingDictionary.Y_LIN, source -> source.linearizedSeries(true));
        mapping.set(ModellingDictionary.Y_LIN + SeriesInfo.F_SUFFIX, source -> source.linearizedForecast(source.domain(true).getLength(), true));
        mapping.set(ModellingDictionary.YCAL, source -> source.getYcal(false));
        mapping.set(ModellingDictionary.YCAL + SeriesInfo.F_SUFFIX, source -> source.getYcal(true));
        mapping.set(ModellingDictionary.DET, source -> source.getDet(false));
        mapping.set(ModellingDictionary.DET + SeriesInfo.F_SUFFIX, source -> source.getDet(true));
        mapping.set(ModellingDictionary.L + SeriesInfo.F_SUFFIX, source -> source.linearizedForecast(FCAST_YEAR * source.description.getFrequency()));
        mapping.set(ModellingDictionary.L + SeriesInfo.B_SUFFIX, source -> source.linearizedBackcast(source.description.getFrequency()));
        mapping.set(ModellingDictionary.CAL, source -> source.getCal(false));
        mapping.set(ModellingDictionary.CAL + SeriesInfo.F_SUFFIX, source -> source.getCal(true));
        mapping.set(ModellingDictionary.TDE, source -> source.getTde(false));
        mapping.set(ModellingDictionary.TDE + SeriesInfo.F_SUFFIX, source -> source.getTde(true));
        mapping.set(ModellingDictionary.MHE, source -> source.getMhe(false));
        mapping.set(ModellingDictionary.MHE + SeriesInfo.F_SUFFIX, source -> source.getMhe(true));
        mapping.set(ModellingDictionary.EE, source -> source.getEe(false));
        mapping.set(ModellingDictionary.EE + SeriesInfo.F_SUFFIX, source -> source.getEe(true));
        mapping.set(ModellingDictionary.OMHE, source -> source.getOmhe(false));
        mapping.set(ModellingDictionary.OMHE + SeriesInfo.F_SUFFIX, source -> source.getOmhe(true));
        mapping.set(ModellingDictionary.OUT, source -> source.getOutlier(ComponentType.Undefined, false));
        mapping.set(ModellingDictionary.OUT + SeriesInfo.F_SUFFIX, source -> source.getOutlier(ComponentType.Undefined, true));
        mapping.set(ModellingDictionary.OUT_I, source -> source.getOutlier(ComponentType.Irregular, false));
        mapping.set(ModellingDictionary.OUT_I + SeriesInfo.F_SUFFIX, source -> source.getOutlier(ComponentType.Irregular, true));
        mapping.set(ModellingDictionary.OUT_T, source -> source.getOutlier(ComponentType.Trend, false));
        mapping.set(ModellingDictionary.OUT_T + SeriesInfo.F_SUFFIX, source -> source.getOutlier(ComponentType.Trend, true));
        mapping.set(ModellingDictionary.OUT_S, source -> source.getOutlier(ComponentType.Seasonal, false));
        mapping.set(ModellingDictionary.OUT_S + SeriesInfo.F_SUFFIX, source -> source.getOutlier(ComponentType.Seasonal, true));
        mapping.set(ModellingDictionary.REG, source -> source.getReg(false));
        mapping.set(ModellingDictionary.REG + SeriesInfo.F_SUFFIX, source -> source.getReg(true));
        mapping.set(ModellingDictionary.REG_T, source -> source.getReg(ComponentType.Trend, false));
        mapping.set(ModellingDictionary.REG_T + SeriesInfo.F_SUFFIX, source -> source.getReg(ComponentType.Trend, true));
        mapping.set(ModellingDictionary.REG_S, source -> source.getReg(ComponentType.Seasonal, false));
        mapping.set(ModellingDictionary.REG_S + SeriesInfo.F_SUFFIX, source -> source.getReg(ComponentType.Seasonal, true));
        mapping.set(ModellingDictionary.REG_I, source -> source.getReg(ComponentType.Irregular, false));
        mapping.set(ModellingDictionary.REG_I + SeriesInfo.F_SUFFIX, source -> source.getReg(ComponentType.Irregular, true));
        mapping.set(ModellingDictionary.REG_SA, source -> source.getReg(ComponentType.SeasonallyAdjusted, false));
        mapping.set(ModellingDictionary.REG_SA + SeriesInfo.F_SUFFIX, source -> source.getReg(ComponentType.SeasonallyAdjusted, true));
        mapping.set(ModellingDictionary.REG_Y, source -> source.getReg(ComponentType.Series, false));
        mapping.set(ModellingDictionary.REG_Y + SeriesInfo.F_SUFFIX, source -> source.getReg(ComponentType.Series, true));
        mapping.set(ModellingDictionary.REG_U, source -> source.getReg(ComponentType.Undefined, false));
        mapping.set(ModellingDictionary.REG_U + SeriesInfo.F_SUFFIX, source -> source.getReg(ComponentType.Undefined, true));
        mapping.set(FULLRES, source -> source.getFullResiduals());
        mapping.set(InformationSet.item(REGRESSION, LP), RegressionItem.class, source -> source.getRegressionItem(ILengthOfPeriodVariable.class, 0));
        mapping.set(InformationSet.item(REGRESSION, NTD), Integer.class, source -> {
            TsVariableList vars = source.vars();
            TsVariableSelection<ITsVariable> sel = vars.select(new TsVariableList.ISelector() {
                @Override
                public boolean accept(ITsVariable var) {
                    return Variable.search(source.description.getCalendars(), var) != null;
                }
            });
            return sel.getVariablesCount();
        });
        mapping.set(InformationSet.item(REGRESSION, NMH), Integer.class, source -> {
            TsVariableList vars = source.vars();
            TsVariableSelection<ITsVariable> sel = vars.select(new TsVariableList.ISelector() {
                @Override
                public boolean accept(ITsVariable var) {
                    return Variable.search(source.description.getMovingHolidays(), var) != null;
                }
            });
            return sel.getVariablesCount();
        });
        mapping.setList(InformationSet.item(REGRESSION, TD), 1, 14, RegressionItem.class, (source, i) -> source.getRegressionItem(ITradingDaysVariable.class, i-1));
        mapping.set(InformationSet.item(REGRESSION, EASTER), RegressionItem.class, source -> source.getRegressionItem(IEasterVariable.class, 0));
        mapping.set(InformationSet.item(REGRESSION, NOUT), Integer.class, source -> source.description.getOutliers().size() + source.description.getPrespecifiedOutliers().size());
        mapping.set(InformationSet.item(REGRESSION, NOUTAO), Integer.class, source -> {
            TsVariableList vars = source.vars();
            return vars.select(OutlierType.AO).getItemsCount();
        });
        mapping.set(InformationSet.item(REGRESSION, NOUTLS), Integer.class, source -> {
            TsVariableList vars = source.vars();
            return vars.select(OutlierType.LS).getItemsCount();
        });
        mapping.set(InformationSet.item(REGRESSION, NOUTTC), Integer.class, source -> {
            TsVariableList vars = source.vars();
            return vars.select(OutlierType.TC).getItemsCount();
        });
        mapping.set(InformationSet.item(REGRESSION, NOUTSO), Integer.class, source -> {
            TsVariableList vars = source.vars();
            return vars.select(OutlierType.SO).getItemsCount();
        });
        mapping.setList(InformationSet.item(REGRESSION, OUT), 1, 50, RegressionItem.class, (source, i) -> source.getRegressionItem(IOutlierVariable.class, i-1));
        mapping.set(InformationSet.item(REGRESSION, COEFF), Parameter[].class, source -> {
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
        mapping.set(InformationSet.item(REGRESSION, COEFFDESC), String[].class, source -> {
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
            TsFrequency context = source.description.getEstimationDomain().getFrequency();
            for (ITsVariable var : items) {
                for (int j = 0; j < var.getDim(); ++j) {
                    str.add(var.getItemDescription(j, context));
                }
            }
            String[] desc = new String[str.size()];
            return str.toArray(desc);
        });
        mapping.set(InformationSet.item(REGRESSION, COVAR), Matrix.class,
                source -> source.estimation.getLikelihood().getBVar(true, source.description.getArimaComponent().getFreeParametersCount()));
        mapping.set(InformationSet.item(REGRESSION, PCOVAR), Matrix.class, source -> source.estimation.getParametersCovariance());
    }
}
