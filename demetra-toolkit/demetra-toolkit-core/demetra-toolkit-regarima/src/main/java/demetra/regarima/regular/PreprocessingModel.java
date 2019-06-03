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
package demetra.regarima.regular;

import jdplus.data.DataBlock;
import demetra.data.DoubleSeqCursor;
import demetra.design.Development;
import demetra.modelling.regression.RegressionUtility;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import jdplus.timeseries.simplets.TsDataToolkit;
import jdplus.timeseries.simplets.TsDataTransformation;
import java.util.List;
import demetra.data.DoubleSeq;

/**
 * The pre-processing model contains all information on the estimated regarima
 * model
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.Value
public class PreprocessingModel {

    // Model description
    private ModelDescription description;
    private ModelEstimation estimation;

//
//    public static ComponentType outlierComponent(OutlierType type) {
//        switch (type) {
//            case AO:
//                return ComponentType.Irregular;
//            case TC:
//                return ComponentType.Irregular;
//            case LS:
//                return ComponentType.Trend;
//            case SO:
//                return ComponentType.Seasonal;
//            default:
//                return ComponentType.Undefined;
//        }
//    }
//
//    public static OutlierType[] outlierTypes(ComponentType cmp) {
//        switch (cmp) {
//            case Trend:
//                return new OutlierType[]{OutlierType.LS};
//            case Seasonal:
//                return new OutlierType[]{OutlierType.SO};
//            case Irregular:
//                return new OutlierType[]{OutlierType.AO, OutlierType.TC};
//            default:
//                return new OutlierType[]{};
//        }
//    }
//
//    private ModelDescription description;
//    private ModelEstimation estimation;
//    private InformationSet information;
//    private List<ProcessingInformation> log;
//
    public TsData backTransform(TsData s) {
        List<TsDataTransformation> back = description.backTransformations();
        for (TsDataTransformation t : back) {
            s = t.transform(s, null);
        }
        return s;
    }

    public double backTransform(TsPeriod p, double s) {
        List<TsDataTransformation> back = description.backTransformations();
        for (TsDataTransformation t : back) {
            s = t.transform(p, s);
        }
        return s;
    }

//
//    public PreprocessingModel(ModelDescription description, ModelEstimation estimation) {
//        this.description = description;
//        this.estimation = estimation;
//    }
//
//    public void updateModel() {
//        IReadDataBlock p = estimation.getArima().getParameters();
//        DataBlock e = new DataBlock(p.getLength());
//        // update the standard deviation
//        if (estimation.getParametersCovariance() != null) {
//            DataBlock diag = estimation.getParametersCovariance().diagonal();
//            for (int i = 0; i < e.getLength(); ++i) {
//                double var = diag.get(i);
//                e.set(i, var <= 0 ? 0 : Math.sqrt(var));
//            }
//            description.getArimaComponent().setParameters(p, e, ParameterType.Estimated);
//        } else { // update only the parameters
//            description.getArimaComponent().setParameters(p, e, ParameterType.Initial);
//        }
//        if (description.getArimaComponent().isEstimatedMean()) {
//            description.getArimaComponent().setMu(new Parameter(estimation.getLikelihood().getB()[0], ParameterType.Estimated));
//        }
//    }
//
//    public void addProcessingInformation(ProcessingInformation info) {
//        log_.add(info);
//    }
//
//    public void addProcessingInformation(Collection<ProcessingInformation> info) {
//        if (log_ != null && info != null) {
//            log_.addAll(info);
//        }
//    }
//
//    public IFunction likelihoodFunction() {
//        RegArimaModel<SarimaModel> regArima = estimation.getRegArima();
//        ArmaFunction fn = new ArmaFunction<>(regArima.getDModel(), regArima.getArima().getDifferenceOrder(), regArima.getMissings(), description.defaultMapping());
//        fn.llog = true;
//        return fn;
//    }
//
//    public IFunctionInstance maxLikelihoodFunction() {
//        IFunction fn = likelihoodFunction();
//        return fn.evaluate(description.defaultMapping().map(estimation.getArima()));
//    }
//
//    public MissingValueEstimation[] missings(boolean unbiased) {
//        int[] missings = description.getMissingValues();
//        if (missings != null) {
//            MissingValueEstimation[] m = new MissingValueEstimation[missings.length];
//            ConcentratedLikelihood ll = estimation.getLikelihood();
//            double[] b = ll.getB();
//            int nhp = description.getArimaComponent().getFreeParametersCount();
//            double[] se = ll.getBSer(unbiased, nhp);
//            int istart = description.isEstimatedMean() ? 1 : 0;
//            for (int i = 0; i < missings.length; ++i) {
//                int pos = missings[i];
//                TsPeriod period = description.getEstimationDomain().get(pos);
//                double val = description.getY()[pos] - b[istart + i];
//                MissingValueEstimation cur = new MissingValueEstimation(period, val, se[istart + i]);
//                m[i] = cur;
//            }
//            Arrays.sort(m);
//            return m;
//        } else {
//            return null;
//        }
//    }
//
    public TsData interpolatedSeries(boolean bTransformed) {
        TsData data;
        if (!bTransformed) {
            data = description.getSeries();
        } else {
            data = description.getTransformedSeries();
        }

        // complete for missings
        int[] missings = description.getMissing();
        if (missings != null) {
            if (estimation == null) {
                return null;
            }
            List<TsDataTransformation> back;
            if (!bTransformed) {
                back = description.backTransformations();
            }
            DoubleSeq m = estimation.getConcentratedLikelihood().missingEstimates();
            DoubleSeqCursor reader = m.cursor();
            double[] tmp = data.getValues().toArray();
            for (int i = 0; i < missings.length; ++i) {
                int pos = missings[i];
                TsPeriod p = description.getDomain().get(pos);
                double val = reader.getAndNext();
                if (!bTransformed) {
                    tmp[pos] = backTransform(p, val);
                } else {
                    tmp[pos] = val;
                }
            }
            data = TsData.ofInternal(data.getStart(), tmp);
        }
        return data;
    }

    public TsData linearizedSeries() {
        if (estimation == null) {
            return description.getTransformedSeries();
        }
        TsData interp = interpolatedSeries(true);
        TsData regs = regressionEffect(description.getDomain());
        TsData lin = TsDataToolkit.subtract(interp, regs);
        return lin;
    }
//
//    public TsData linearizedSeries(boolean includeUndefinedReg) {
//        TsData s = linearizedSeries();
//        if (includeUndefinedReg) {
//            TsData reg = userEffect(s.getDomain(), ComponentType.Undefined);
//            s = TsData.add(s, reg);
//        }
//        return s;
//    }
//

    public TsData preadjustmentEffect(TsDomain domain) {
        if (description.hasFixedEffects()) {
            DataBlock t = DataBlock.make(domain.getLength());
            description.preadjustmentVariables().forEachOrdered(v->RegressionUtility
                    .addAY(domain, t, 1, v.getCoefficients(), v.getVariable()));
            return TsData.ofInternal(domain.getStartPeriod(), t.unmodifiable());
        } else {
            return null;
        }
    }

    public TsData regressionEffect(TsDomain domain) {
        if (estimation == null) {
            return null;
        }
        final int n=domain.getLength();
        DoubleSeq coeffs = estimation.getConcentratedLikelihood().coefficients();
        DoubleSeqCursor reader = coeffs.cursor();
        DataBlock r = DataBlock.make(n);
        if (description.isEstimatedMean()) {
            reader.skip(1);
        }
        
        RegressionUtility.addAY(domain, r, 1, coeffs, description.regressionVariables());

        TsData rslt = TsData.ofInternal(domain.getStartPeriod(), r.unmodifiable());
        return rslt;
    }

//
//    public <T extends ITsVariable> TsData preadjustmentEffect(TsDomain domain, Class<T> tclass) {
//        if (description.hasFixedEffects()) {
//            DataBlock reg = PreadjustmentVariable.regressionEffect(description.preadjustmentVariables(), domain, tclass);
//            return new TsData(domain.getStart(), reg);
//        } else {
//            return null;
//        }
//    }
//
//    // cmp is used in back transformation
//    public <T extends ITsVariable> TsData regressionEffect(TsDomain domain, Class<T> tclass) {
//        if (estimation == null) {
//            return null;
//        }
//        TsVariableSelection sel = vars().selectCompatible(tclass);
//        if (sel.isEmpty()) {
//            return new TsData(domain, 0);
//        }
//        double[] coeffs = estimation.getLikelihood().getB();
//        int istart = description.getRegressionVariablesStartingPosition();
//
//        DataBlock sum = sel.sum(new DataBlock(coeffs, istart, coeffs.length, 1), domain);
//
//        if (sum == null) {
//            sum = new DataBlock(domain.getLength());
//        }
//        TsData rslt = new TsData(domain.getStart(), sum.getData(), false);
//        return rslt;
//    }
//
//    public <T extends ITsVariable> TsData preadjustmentEffect(TsDomain domain, Predicate<PreadjustmentVariable> selector) {
//        if (description.hasFixedEffects()) {
//            DataBlock reg = PreadjustmentVariable.regressionEffect(description.preadjustmentVariables(), domain, selector);
//            return new TsData(domain.getStart(), reg);
//        } else {
//            return null;
//        }
//    }
//
//    private TsData regressionEffect(TsDomain domain, Predicate<Variable> selector) {
//        if (estimation == null) {
//            return null;
//        }
//        double[] coeffs = estimation.getLikelihood().getB();
//        if (coeffs == null) {
//            return new TsData(domain, 0);
//        }
//        TsVariableSelection<ITsVariable> sel = description.buildRegressionVariables(selector);
//        if (sel.isEmpty()) {
//            return new TsData(domain, 0);
//        }
//        DataBlock sum = sel.sum(new DataBlock(coeffs, description.getRegressionVariablesStartingPosition(), coeffs.length, 1), domain);
//        TsData rslt = new TsData(domain.getStart(), sum.getData(), false);
//        return rslt;
//    }
//
//    public TsData deterministicEffect(TsDomain domain) {
//        return TsData.add(regressionEffect(domain), preadjustmentEffect(domain));
//    }
//
//    public TsData deterministicEffect(TsDomain domain, Predicate<ITsVariable> selector) {
//        return TsData.add(regressionEffect(domain, reg -> selector.test(reg.getVariable())),
//                preadjustmentEffect(domain, reg -> selector.test(reg.getVariable())));
//    }
//
//    public <T extends ITsVariable> TsData deterministicEffect(TsDomain domain, Class<T> tclass) {
//        return TsData.add(regressionEffect(domain, tclass), preadjustmentEffect(domain, tclass));
//    }
//
//    public TsData tradingDaysEffect(TsDomain domain) {
//        return deterministicEffect(domain, var -> var instanceof ICalendarVariable);
//        // return regressionEffect(domain, ICalendarVariable.class);
//    }
//
//    public TsData movingHolidaysEffect(TsDomain domain) {
//        return deterministicEffect(domain, var -> var instanceof IMovingHolidayVariable);
////        return regressionEffect(domain, IMovingHolidayVariable.class);
//    }
//
//    public TsData outliersEffect(TsDomain domain) {
//        return deterministicEffect(domain, IOutlierVariable.class);
//    }
//
//    public TsData outliersEffect(TsDomain domain, final ComponentType type) {
//        if (estimation == null) {
//            return null;
//        }
//        if (type == ComponentType.Undefined) {
//            return outliersEffect(domain);
//        } else {
//            return deterministicEffect(domain, var
//                    -> var instanceof IOutlierVariable
//                    && type == Variable.searchType((IOutlierVariable) var));
//        }
//
//    }
//
//    private static final OutlierEstimation[] NO_OUTLIER = new OutlierEstimation[0];
//
//    public OutlierEstimation[] outliersEstimation(boolean unbiased, boolean prespecified) {
//        ConcentratedLikelihood ll = estimation.getLikelihood();
//        if (ll == null) {
//            return null; // BUG
//        }
//        double[] b = ll.getB();
//        if (b == null) {
//            return NO_OUTLIER;
//        }
//        int nhp = description.getArimaComponent().getFreeParametersCount();
//        double[] se = ll.getBSer(unbiased, nhp);
//        int istart = description.getRegressionVariablesStartingPosition();
//
//        TsVariableSelection<IOutlierVariable> sel = vars().select(IOutlierVariable.class);
//        ArrayList<OutlierEstimation> o = new ArrayList<>();
//        for (TsVariableSelection.Item<IOutlierVariable> cur : sel.elements()) {
//            if (prespecified == description.isPrespecified(cur.variable)) {
//                int rpos = cur.position + istart;
//                CoefficientEstimation c = new CoefficientEstimation(b[rpos], se[rpos]);
//                o.add(new OutlierEstimation(c, cur.variable, description.getEstimationDomain().getFrequency()));
//            }
//        }
//        return o.isEmpty() ? NO_OUTLIER : Jdk6.Collections.toArray(o, OutlierEstimation.class);
//    }
//
//    public List<TsData> regressors(TsDomain domain) {
//        ArrayList<TsData> regs = new ArrayList<>();
//        List<DataBlock> data = vars().all().data(domain);
//        for (DataBlock d : data) {
//            double[] cur = new double[domain.getLength()];
//            d.copyTo(cur, 0);
//            regs.add(new TsData(domain.getStart(), cur, false));
//        }
//        return regs;
//    }
//
//    public TsData deterministicEffect(TsDomain domain, final ComponentType type) {
//        return TsData.add(regressionEffect(domain, reg -> reg.type == type),
//                preadjustmentEffect(domain, reg -> reg.getType() == type));
//
//    }
//
//    public TsData userEffect(TsDomain domain, final ComponentType type) {
//        return TsData.add(regressionEffect(domain, reg -> reg.isUser() && reg.type == type),
//                preadjustmentEffect(domain, reg -> reg.isUser() && reg.getType() == type));
//    }
//
//    public TsData userEffect(TsDomain domain) {
//        return TsData.add(regressionEffect(domain, reg -> reg.isUser()),
//                preadjustmentEffect(domain, reg -> reg.isUser()));
//    }
//
//    public TsData linearizedForecast(int nf) {
//        if (fcast_ != null && nf <= fcast_.getLength()) {
//            return fcast_.drop(0, fcast_.getLength() - nf);
//        }
//        TsData s = linearizedSeries(false);
//        DataBlock data = new DataBlock(s.internalStorage());
//        // FastArimaForecasts fcast = new FastArimaForecasts(model, false);
//        double mean = description.isEstimatedMean() ? estimation.getLikelihood().getB()[0]
//                : description.getArimaComponent().getMeanCorrection();
//        UscbForecasts fcast = new UscbForecasts(estimation.getArima(), mean);
//        double[] forecasts = fcast.forecasts(data, nf);
//        TsData fs = new TsData(s.getEnd(), forecasts, false);
//        fcast_ = fs.clone();
//        return fs;
//    }
//
//    public TsData linearizedForecast(int nf, boolean includeUndefinedReg) {
//        TsData s = linearizedForecast(nf);
//        if (includeUndefinedReg) {
//            TsData reg = userEffect(s.getDomain(), ComponentType.Undefined);
//            s = TsData.add(s, reg);
//        }
//        return s;
//    }
//
//    public TsData linearizedBackcast(int nf, boolean includeUndefinedReg) {
//        TsData s = linearizedBackcast(nf);
//        if (includeUndefinedReg) {
//            TsData reg = userEffect(s.getDomain(), ComponentType.Undefined);
//            s = TsData.add(s, reg);
//        }
//        return s;
//    }
//
//    public Forecasts forecasts(int nf) {
//        if (xfcasts_ != null && nf <= xfcasts_.getForecastsCount()) {
//            return xfcasts_;
//        }
//        xfcasts_ = new Forecasts();
//        TsDomain fdomain = new TsDomain(description.getEstimationDomain().getEnd(), nf);
//        RegArimaEstimation<SarimaModel> est
//                = new RegArimaEstimation<>(estimation.getRegArima(),
//                        estimation.getLikelihood());
//        xfcasts_.calcForecast(est, vars().all().data(fdomain), nf, description.getArimaComponent().getFreeParametersCount());
//        return xfcasts_;
//    }
//
//    public TsData linearizedBackcast(int nb) {
//        if (bcast_ != null && nb <= bcast_.getLength()) {
//            return bcast_.drop(bcast_.getLength() - nb, 0);
//        }
//        TsData s = linearizedSeries(false);
//        DataBlock data = new DataBlock(s.internalStorage()).reverse();
//        // FastArimaForecasts fcast = new FastArimaForecasts(model, false);
//        double mean = description.isEstimatedMean() ? estimation.getLikelihood().getB()[0]
//                : description.getArimaComponent().getMeanCorrection();
//        UscbForecasts fcast = new UscbForecasts(estimation.getArima(), mean);
//        double[] backcasts = fcast.forecasts(data, nb);
//        Arrays2.reverse(backcasts);
//        TsData bs = new TsData(s.getStart().minus(nb), backcasts, false);
//        bcast_ = bs.clone();
//        return bs;
//    }
//
//    public TsData forecast(int nf, boolean transformed) {
//        TsData f = linearizedForecast(nf);
//        TsData c = PreprocessingModel.this.deterministicEffect(f.getDomain());
//        TsData r = TsData.add(f, c);
//
//        if (!transformed) {
//            backTransform(r, true, true);
//        }
//        return r;
//    }
//
//    public TsData backcast(int nb, boolean transformed) {
//        TsData b = linearizedBackcast(nb);
//        TsData c = PreprocessingModel.this.deterministicEffect(b.getDomain());
//        TsData r = TsData.add(b, c);
//        if (!transformed) {
//            backTransform(r, true, true);
//        }
//        return r;
//    }
//
//    public DeterministicComponent getDeterministicComponent() {
//        DeterministicComponent det = new DeterministicComponent();
//        det.setOriginal(description.getOriginal());
//        det.setY(this.interpolatedSeries(true));
//        det.setLengthOfPeriodAdjustment(description.getLengthOfPeriodType());
//        det.setTransformation(description.getTransformation());
//        det.setUnits(description.getUnits());
//        // add the pre-adjustments
//        description.preadjustmentVariables().forEach(
//                var -> det.add(var)
//        );
//        // add the estimated regression variables
//        if (estimation == null) {
//            return null;
//        }
//        double[] coeffs = estimation.getLikelihood().getB();
//        if (coeffs != null) {
//            int cur = description.getRegressionVariablesStartingPosition();
//            List<Variable> vars = description.getOrderedVariables();
//            for (Variable var : vars) {
//                int ncur = cur += var.getVariable().getDim();
//                PreadjustmentVariable pvar = PreadjustmentVariable.fix(var, Arrays.copyOfRange(coeffs, cur, ncur));
//                det.add(pvar);
//                cur = ncur;
//            }
//        }
//        return det;
//    }
//
//    @Override
//    public Map<String, Class> getDictionary() {
//        return dictionary(false);
//    }
//
//    @Override
//    public <T> T getData(String id, Class<T> tclass
//    ) {
//        if (MAPPING.contains(id)) {
//            return MAPPING.getData(this, id, tclass);
//        }
//        if (estimation.contains(id)) {
//            return estimation.getData(id, tclass);
//        }
//        if (info_ != null) {
//            if (!id.contains(InformationSet.STRSEP)) {
//                return info_.deepSearch(id, tclass);
//            } else {
//                return info_.search(id, tclass);
//            }
//        } else {
//            return null;
//        }
//    }
//
//    @Override
//    public <T> Map<String, T> searchAll(String wc, Class<T> tclass
//    ) {
//        Map<String, T> all = MAPPING.searchAll(this, wc, tclass);
//        if (info_ != null) {
//            List<Information<T>> sel = info_.select(wc, tclass);
//            for (Information<T> info : sel) {
//                all.put(info.name, info.value);
//            }
//        }
//        Map<String, T> eall = estimation.searchAll(wc, tclass);
//        all.putAll(eall);
//        return all;
//    }
//
//    @Override
//    public boolean contains(String id
//    ) {
//        if (MAPPING.contains(id)) {
//            return true;
//        }
//        if (estimation.contains(id)) {
//            return true;
//        }
//        if (info_ != null) {
//            if (!id.contains(InformationSet.STRSEP)) {
//                return info_.deepSearch(id, Object.class) != null;
//            } else {
//                return info_.search(id, Object.class) != null;
//            }
//
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public List<ProcessingInformation> getProcessingInformation() {
//        return log_ == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(log_);
//    }
//
//    public static void fillDictionary(String prefix, Map<String, Class> map, boolean compact) {
//        MAPPING.fillDictionary(prefix, map, compact);
//        ModelEstimation.fillDictionary(prefix, map, compact);
//    }
//
//    public static Map<String, Class> dictionary(boolean compact) {
//        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
//        fillDictionary(null, map, compact);
//        return map;
//    }
//
//    private TsData op(TsData l, TsData r) {
//        if (description.getTransformation() == DefaultTransformationType.Log) {
//            return TsData.multiply(l, r);
//        } else {
//            return TsData.add(l, r);
//        }
//    }
//
//    private TsData inv_op(TsData l, TsData r) {
//        if (description.getTransformation() == DefaultTransformationType.Log) {
//            return TsData.divide(l, r);
//        } else {
//            return TsData.subtract(l, r);
//        }
//    }
//
//    private TsData getForecastError() {
//        TsDomain fdomain = domain(true);
//        Forecasts fcasts = forecasts(fdomain.getLength());
//        double[] ef;
//        if (isMultiplicative()) {
//            LogForecasts lf = new LogForecasts(fcasts);
//            ef = lf.getForecatStdevs();
//        } else {
//            ef = fcasts.getForecastStdevs();
//        }
//        return new TsData(fdomain.getStart(), ef, true);
//    }
//
////    private TsData getSeries(String id, boolean fcast) {
////        if (id.equals(ModellingDictionary.Y) && fcast) {
////            return forecast(domain(true).getLength(), false);
////        }
////        if (id.equals(ModellingDictionary.YC)) {
////            return interpolatedSeries(false);
////        } else if (id.equals(ModellingDictionary.L)) {
////            return linearizedSeries();
////        } else if (id.equals(ModellingDictionary.Y_LIN)) {
////            return isMultiplicative() ? linearizedSeries().exp() : linearizedSeries();
////        } else if (id.equals(ModellingDictionary.CAL)) {
////            return getCal(fcast);
////        } else if (id.equals(ModellingDictionary.DET)) {
////            return getDet(fcast);
////        } else if (id.equals(ModellingDictionary.EE)) {
////            return getEe(fcast);
////        } else if (id.equals(ModellingDictionary.MHE)) {
////            return getMhe(fcast);
////        } else if (id.equals(ModellingDictionary.RMDE)) {
////            return getRmde(fcast);
////        } else if (id.equals(ModellingDictionary.OMHE)) {
////            return getOmhe(fcast);
////        } else if (id.equals(ModellingDictionary.OUT)) {
////            return getOutlier(ComponentType.Undefined, fcast);
////        } else if (id.equals(ModellingDictionary.OUT_I)) {
////            return getOutlier(ComponentType.Irregular, fcast);
////        } else if (id.equals(ModellingDictionary.OUT_S)) {
////            return getOutlier(ComponentType.Seasonal, fcast);
////        } else if (id.equals(ModellingDictionary.OUT_T)) {
////            return getOutlier(ComponentType.Trend, fcast);
////        } else if (id.equals(ModellingDictionary.REG)) {
////            return getReg(ComponentType.Undefined, fcast);
////        } else if (id.equals(ModellingDictionary.REG_I)) {
////            return getReg(ComponentType.Irregular, fcast);
////        } else if (id.equals(ModellingDictionary.REG_S)) {
////            return getReg(ComponentType.Seasonal, fcast);
////        } else if (id.equals(ModellingDictionary.REG_SA)) {
////            return getReg(ComponentType.Trend, fcast);
////        } else if (id.equals(ModellingDictionary.REG_T)) {
////            return getReg(ComponentType.Trend, fcast);
////        } else if (id.equals(ModellingDictionary.REG_Y)) {
////            return getReg(ComponentType.Series, fcast);
////        } else if (id.equals(ModellingDictionary.YCAL)) {
////            return getYcal(fcast);
////        } else if (id.equals(ModellingDictionary.TDE)) {
////            return getTde(fcast);
////        }
////        return null;
////    }
//    private TsData getTde(boolean fcast) {
//        TsDomain fdom = domain(fcast);
//        TsData tmp = tradingDaysEffect(fdom);
//        if (tmp == null) {
//            return null;
//        }
////        DescriptiveStatistics stats = new DescriptiveStatistics(tmp.getValues());
////        if (stats.isConstant()) {
////            return null;
////        }
//        backTransform(tmp, false, true);
//        return tmp;
//    }
//
//    private TsData getYcal(boolean fcast) {
//        return inv_op(fcast ? forecast(getForecastCount(), false) : interpolatedSeries(false), getCal(fcast));
//        //throw new UnsupportedOperationException("Not yet implemented");
//    }
//
//    private TsData getReg(boolean fcast) {
//        TsData tmp = userEffect(domain(fcast));
//        if (tmp == null) {
//            return null;
//        }
//        backTransform(tmp, false, false);
//        return tmp;
//    }
//
//    private TsData getReg(ComponentType componentType, boolean fcast) {
//        TsData tmp = userEffect(domain(fcast), componentType);
//        if (tmp == null) {
//            return null;
//        }
//        backTransform(tmp, false, false);
//        return tmp;
//    }
//
//    private TsData getOutlier(ComponentType componentType, boolean fcast) {
//        TsData tmp = outliersEffect(domain(fcast), componentType);
//        if (tmp == null) {
//            return null;
//        }
////        DescriptiveStatistics stats = new DescriptiveStatistics(tmp.getValues());
////        if (stats.isConstant()) {
////            return null;
////        }
//        backTransform(tmp, false, false);
//        return tmp;
//    }
//
//    private TsData getOmhe(boolean fcast) {
//        TsData tmp = inv_op(getMhe(fcast), op(getEe(fcast), getRmde(fcast)));
//        if (tmp == null) {
//            return null;
//        }
//        return tmp;
//    }
//
//    private TsData getMhe(boolean fcast) {
//        TsData tmp = movingHolidaysEffect(domain(fcast));
//        if (tmp == null) {
//            return null;
//        }
////        DescriptiveStatistics stats = new DescriptiveStatistics(tmp.getValues());
////        if (stats.isConstant()) {
////            return null;
////        }
//        backTransform(tmp, false, false);
//        return tmp;
//    }
//
//    private TsData getEe(boolean fcast) {
//        TsData tmp = deterministicEffect(domain(fcast), IEasterVariable.class);
//        if (tmp == null) {
//            return null;
//        }
////        DescriptiveStatistics stats = new DescriptiveStatistics(tmp.getValues());
////        if (stats.isConstant()) {
////            return null;
////        }
//        backTransform(tmp, false, false);
//        return tmp;
//    }
//
//    private TsData getDet(boolean fcast) {
//        TsData tmp = deterministicEffect(domain(fcast));
//        if (tmp == null) {
//            return null;
//        }
//        backTransform(tmp, false, true);
//        return tmp;
//    }
//
//    private TsData getCal(boolean fcast) {
//        TsData tmp = op(getTde(fcast), getMhe(fcast));
//        if (tmp == null) {
//            return null;
//        }
//        return tmp;
//    }
//
//    private TsData getRmde(boolean fcast) {
//        return null;
//    }
//
//    private int getForecastCount() {
//        return FCAST_YEAR * description.getFrequency();
//    }
//
//    private TsDomain domain(boolean fcast) {
//        if (fcast) {
//            TsDomain dom = description.getSeriesDomain();
//            return new TsDomain(dom.getEnd(), FCAST_YEAR * dom.getFrequency().intValue());
//        } else {
//            return description.getSeriesDomain();
//        }
//    }
//
//    public TsData getFullResiduals() {
//        if (fullres_ == null) {
//            TsDomain domain = domain(false);
//            // compute the residuals
//            DataBlock res = estimation.getFullResiduals();
//            double[] xres = new double[res.getLength()];
//            res.copyTo(xres, 0);
//            fullres_ = new TsData(domain.getStart().plus(domain.getLength() - xres.length), xres, false);
//        }
//        return fullres_;
//    }
//
//    private TsVariableList vars() {
//        if (x_ == null) {
//            x_ = description.buildRegressionVariables();
//        }
//        return x_;
//    }
//
//    public boolean isMultiplicative() {
//        return description.getTransformation() == DefaultTransformationType.Log;
//    }
//
//    public TsFrequency getFrequency() {
//        return description.getEstimationDomain().getFrequency();
//    }
//
//    public <T extends ITsVariable> RegressionItem getRegressionItem(Class<T> tclass, int vpos) {
//        TsVariableSelection<T> sel = vars().select(tclass);
//        if (sel.isEmpty()) {
//            return null;
//        } else {
//            int cur = 0;
//            while (cur < sel.getItemsCount()) {
//                int l = sel.get(cur).variable.getDim();
//                if (vpos < l) {
//                    break;
//                } else {
//                    ++cur;
//                    vpos -= l;
//                }
//            }
//            if (cur == sel.getItemsCount()) {
//                return null;
//            }
//            Item<T> item = sel.get(cur);
//            TsFrequency context = description.getEstimationDomain().getFrequency();
//            int pos = description.getRegressionVariablesStartingPosition() + item.position + vpos;
//            double c = estimation.getLikelihood().getB()[pos];
//            double e = estimation.getLikelihood().getBSer(pos, true, description.getArimaComponent().getFreeParametersCount());
//            return new RegressionItem(item.variable.getItemDescription(vpos, context), c, e);
//        }
//    }
//    // some caching...
//    private TsVariableList x_;
//    private TsData fullres_, lin_, fcast_, bcast_;
//    private Forecasts xfcasts_;
//    public static final int FCAST_YEAR = 1;
//    public static final String LOG = "log",
//            ADJUST = "adjust",
//            SPAN = "span", ESPAN = "espan", START = "start", END = "end", N = "n",
//            REGRESSION = "regression",
//            OUTLIERS = "outlier(*)",
//            CALENDAR = "calendar(*)",
//            EASTER = "easter",
//            FULLRES = "fullresiduals",
//            FCASTS = "fcasts",
//            BCASTS = "bcasts",
//            LIN_FCASTS = "lin_fcasts",
//            LIN_BCASTS = "lin_bcasts",
//            NTD = "ntd", NMH = "nmh",
//            TD = "td", TD1 = "td(1)", TD2 = "td(2)", TD3 = "td(3)", TD4 = "td(4)", TD5 = "td(5)", TD6 = "td(6)", TD7 = "td(7)",
//            TD8 = "td(8)", TD9 = "td(9)", TD10 = "td(10)", TD11 = "td(11)", TD12 = "td(12)", TD13 = "td(13)", TD14 = "td(14)",
//            LP = "lp", OUT = "out", OUT1 = "out(1)", OUT2 = "out(2)", OUT3 = "out(3)", OUT4 = "out(4)", OUT5 = "out(5)", OUT6 = "out(6)", OUT7 = "out(7)",
//            NOUT = "nout", NOUTAO = "noutao", NOUTLS = "noutls", NOUTTC = "nouttc", NOUTSO = "noutso",
//            OUT8 = "out(8)", OUT9 = "out(9)", OUT10 = "out(10)", OUT11 = "out(11)", OUT12 = "out(12)", OUT13 = "out(13)", OUT14 = "out(14)",
//            OUT15 = "out(15)", OUT16 = "out(16)", OUT17 = "out(17)", OUT18 = "out(18)", OUT19 = "out(19)", OUT20 = "out(20)",
//            OUT21 = "out(21)", OUT22 = "out(22)", OUT23 = "out(23)", OUT24 = "out(24)", OUT25 = "out(25)", OUT26 = "out(26)",
//            OUT27 = "out(27)", OUT28 = "out(28)", OUT29 = "out(29)", OUT30 = "out(30)",
//            COEFF = "coefficients", COVAR = "covar", COEFFDESC = "description", PCOVAR = "pcovar";
//
//    ;
//    // MAPPING
//    public static InformationMapping<PreprocessingModel> getMapping() {
//        return MAPPING;
//    }
//
//    public static <T> void setMapping(String name, Class<T> tclass, Function<PreprocessingModel, T> extractor) {
//        MAPPING.set(name, tclass, extractor);
//    }
//
//    public static <T> void setTsData(String name, Function<PreprocessingModel, TsData> extractor) {
//        MAPPING.set(name, extractor);
//    }
//
//    private static final InformationMapping<PreprocessingModel> MAPPING = new InformationMapping<>(PreprocessingModel.class);
//
//    static {
//        MAPPING.set(InformationSet.item(SPAN, START), TsPeriod.class, source -> source.description.getSeriesDomain().getStart());
//        MAPPING.set(InformationSet.item(SPAN, END), TsPeriod.class, source -> source.description.getSeriesDomain().getLast());
//        MAPPING.set(InformationSet.item(SPAN, N), Integer.class, source -> source.description.getSeriesDomain().getLength());
//        MAPPING.set(InformationSet.item(ESPAN, START), TsPeriod.class, source -> source.description.getEstimationDomain().getStart());
//        MAPPING.set(InformationSet.item(ESPAN, END), TsPeriod.class, source -> source.description.getEstimationDomain().getLast());
//        MAPPING.set(InformationSet.item(ESPAN, N), Integer.class, source -> source.description.getEstimationDomain().getLength());
//        MAPPING.set(LOG, Boolean.class, source -> source.isMultiplicative());
//        MAPPING.set(ADJUST, Boolean.class, source -> {
//            if (source.description.getPreadjustmentType() == PreadjustmentType.None) {
//                return null;
//            } else {
//                return source.description.getLengthOfPeriodType() != LengthOfPeriodType.None;
//            }
//        });
//        MAPPING.set(ModellingDictionary.Y, source -> source.description.getOriginal());
//        MAPPING.set(ModellingDictionary.Y + SeriesInfo.F_SUFFIX, source -> source.forecast(FCAST_YEAR * source.description.getFrequency(), false));
//        MAPPING.set(ModellingDictionary.Y + SeriesInfo.EF_SUFFIX, source -> source.getForecastError());
//        MAPPING.set(ModellingDictionary.YC, source -> source.interpolatedSeries(false));
//        MAPPING.set(ModellingDictionary.YC + SeriesInfo.F_SUFFIX, source -> source.forecast(FCAST_YEAR * source.description.getFrequency(), false));
//        MAPPING.set(ModellingDictionary.YC + SeriesInfo.EF_SUFFIX, source -> source.getForecastError());
//        MAPPING.set(ModellingDictionary.L, source -> source.linearizedSeries(false));
//        MAPPING.set(ModellingDictionary.Y_LIN, source -> source.linearizedSeries(true));
//        MAPPING.set(ModellingDictionary.Y_LIN + SeriesInfo.F_SUFFIX, source -> source.linearizedForecast(source.domain(true).getLength(), true));
//        MAPPING.set(ModellingDictionary.YCAL, source -> source.getYcal(false));
//        MAPPING.set(ModellingDictionary.YCAL + SeriesInfo.F_SUFFIX, source -> source.getYcal(true));
//        MAPPING.set(ModellingDictionary.DET, source -> source.getDet(false));
//        MAPPING.set(ModellingDictionary.DET + SeriesInfo.F_SUFFIX, source -> source.getDet(true));
//        MAPPING.set(ModellingDictionary.L + SeriesInfo.F_SUFFIX, source -> source.linearizedForecast(FCAST_YEAR * source.description.getFrequency()));
//        MAPPING.set(ModellingDictionary.L + SeriesInfo.B_SUFFIX, source -> source.linearizedBackcast(source.description.getFrequency()));
//        MAPPING.set(ModellingDictionary.CAL, source -> source.getCal(false));
//        MAPPING.set(ModellingDictionary.CAL + SeriesInfo.F_SUFFIX, source -> source.getCal(true));
//        MAPPING.set(ModellingDictionary.TDE, source -> source.getTde(false));
//        MAPPING.set(ModellingDictionary.TDE + SeriesInfo.F_SUFFIX, source -> source.getTde(true));
//        MAPPING.set(ModellingDictionary.MHE, source -> source.getMhe(false));
//        MAPPING.set(ModellingDictionary.MHE + SeriesInfo.F_SUFFIX, source -> source.getMhe(true));
//        MAPPING.set(ModellingDictionary.EE, source -> source.getEe(false));
//        MAPPING.set(ModellingDictionary.EE + SeriesInfo.F_SUFFIX, source -> source.getEe(true));
//        MAPPING.set(ModellingDictionary.OMHE, source -> source.getOmhe(false));
//        MAPPING.set(ModellingDictionary.OMHE + SeriesInfo.F_SUFFIX, source -> source.getOmhe(true));
//        MAPPING.set(ModellingDictionary.OUT, source -> source.getOutlier(ComponentType.Undefined, false));
//        MAPPING.set(ModellingDictionary.OUT + SeriesInfo.F_SUFFIX, source -> source.getOutlier(ComponentType.Undefined, true));
//        MAPPING.set(ModellingDictionary.OUT_I, source -> source.getOutlier(ComponentType.Irregular, false));
//        MAPPING.set(ModellingDictionary.OUT_I + SeriesInfo.F_SUFFIX, source -> source.getOutlier(ComponentType.Irregular, true));
//        MAPPING.set(ModellingDictionary.OUT_T, source -> source.getOutlier(ComponentType.Trend, false));
//        MAPPING.set(ModellingDictionary.OUT_T + SeriesInfo.F_SUFFIX, source -> source.getOutlier(ComponentType.Trend, true));
//        MAPPING.set(ModellingDictionary.OUT_S, source -> source.getOutlier(ComponentType.Seasonal, false));
//        MAPPING.set(ModellingDictionary.OUT_S + SeriesInfo.F_SUFFIX, source -> source.getOutlier(ComponentType.Seasonal, true));
//        MAPPING.set(ModellingDictionary.REG, source -> source.getReg(false));
//        MAPPING.set(ModellingDictionary.REG + SeriesInfo.F_SUFFIX, source -> source.getReg(true));
//        MAPPING.set(ModellingDictionary.REG_T, source -> source.getReg(ComponentType.Trend, false));
//        MAPPING.set(ModellingDictionary.REG_T + SeriesInfo.F_SUFFIX, source -> source.getReg(ComponentType.Trend, true));
//        MAPPING.set(ModellingDictionary.REG_S, source -> source.getReg(ComponentType.Seasonal, false));
//        MAPPING.set(ModellingDictionary.REG_S + SeriesInfo.F_SUFFIX, source -> source.getReg(ComponentType.Seasonal, true));
//        MAPPING.set(ModellingDictionary.REG_I, source -> source.getReg(ComponentType.Irregular, false));
//        MAPPING.set(ModellingDictionary.REG_I + SeriesInfo.F_SUFFIX, source -> source.getReg(ComponentType.Irregular, true));
//        MAPPING.set(ModellingDictionary.REG_SA, source -> source.getReg(ComponentType.SeasonallyAdjusted, false));
//        MAPPING.set(ModellingDictionary.REG_SA + SeriesInfo.F_SUFFIX, source -> source.getReg(ComponentType.SeasonallyAdjusted, true));
//        MAPPING.set(ModellingDictionary.REG_Y, source -> source.getReg(ComponentType.Series, false));
//        MAPPING.set(ModellingDictionary.REG_Y + SeriesInfo.F_SUFFIX, source -> source.getReg(ComponentType.Series, true));
//        MAPPING.set(ModellingDictionary.REG_U, source -> source.getReg(ComponentType.Undefined, false));
//        MAPPING.set(ModellingDictionary.REG_U + SeriesInfo.F_SUFFIX, source -> source.getReg(ComponentType.Undefined, true));
//        MAPPING.set(FULLRES, source -> source.getFullResiduals());
//        MAPPING.set(InformationSet.item(REGRESSION, LP), RegressionItem.class, source -> source.getRegressionItem(ILengthOfPeriodVariable.class, 0));
//        MAPPING.set(InformationSet.item(REGRESSION, NTD), Integer.class, source -> {
//            return source.description.countRegressors(var -> var.status.isSelected() && var.getVariable() instanceof ICalendarVariable);
//        });
//        MAPPING.set(InformationSet.item(REGRESSION, NMH), Integer.class, source -> {
//            return source.description.countRegressors(var -> var.status.isSelected() && var.getVariable() instanceof IMovingHolidayVariable);
//        });
//        MAPPING.setList(InformationSet.item(REGRESSION, TD), 1, 15, RegressionItem.class, (source, i) -> source.getRegressionItem(ITradingDaysVariable.class, i - 1));
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
//
//    }
//
//    private static int nperiods(PreprocessingModel m, int n) {
//        if (n >= 0) {
//            return n;
//        } else {
//            return -n * m.getFrequency().intValue();
//        }
//    }
}
