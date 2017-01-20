/*
 * Copyright 2013 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package ec.tstoolkit.modelling;

import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.information.InformationMapping;
import ec.tstoolkit.modelling.arima.ModelEstimation;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.regression.Constant;
import ec.tstoolkit.timeseries.regression.DiffConstant;
import ec.tstoolkit.timeseries.regression.ICalendarVariable;
import ec.tstoolkit.timeseries.regression.IEasterVariable;
import ec.tstoolkit.timeseries.regression.IMovingHolidayVariable;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.ITsModifier;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.Ramp;
import ec.tstoolkit.timeseries.regression.Sequence;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.ConstTransformation;
import ec.tstoolkit.timeseries.simplets.ExpTransformation;
import ec.tstoolkit.timeseries.simplets.ITsDataTransformation;
import ec.tstoolkit.timeseries.simplets.LengthOfPeriodTransformation;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @author Jean Palate
 */
public class DeterministicComponent implements IProcResults {

    public static final int FCAST_YEAR = 1;

    private TsData original_, y_;
    private LengthOfPeriodType lp_ = LengthOfPeriodType.None;
    private DefaultTransformationType function_ = DefaultTransformationType.None;
    private final List<PreadjustmentVariable> x_ = new ArrayList<>();
    private DataBlock b_;
    private double units_ = 1;

    public DeterministicComponent() {
    }

    /**
     * @return the original_
     */
    public TsData getOriginal() {
        return original_.clone();
    }

    public TsData getY() {
        return y_;
    }

    public TsVariableList getX() {
        TsVariableList x = new TsVariableList();
        x_.stream().forEach(var -> x.add(var.getVariable()));
        return x;
    }

    public void setOriginal(TsData s) {
        original_ = s;
    }

    public void setY(TsData s) {
        y_ = s;
    }

    public void add(PreadjustmentVariable var) {
        x_.add(var);
    }

    public void clearX() {
        x_.clear();
    }

    public static ComponentType getType(IOutlierVariable var) {
        OutlierType ot = var.getOutlierType();
        switch (ot) {
            case AO:
            case TC:
            case WO:
                return ComponentType.Irregular;
            case LS:
                return ComponentType.Trend;
            case SO:
                return ComponentType.Seasonal;
            default:
                return ComponentType.Undefined;
        }
    }

    public static ComponentType getType(InterventionVariable var) {
        if (var.getDeltaS() > 0 && var.getDelta() > 0) {
            return ComponentType.Undefined;
        }
        Sequence[] sequences = var.getSequences();
        int maxseq = 0;
        for (int i = 0; i < sequences.length; ++i) {
            int len = sequences[i].end.difference(sequences[i].start) / 365;
            if (len > maxseq) {
                maxseq = len;
            }
        }
        if (maxseq > 0) {
            return var.getDeltaS() == 0 ? ComponentType.Trend : ComponentType.Undefined;
        }
        if (var.getDeltaS() > 0) {
            return ComponentType.Seasonal;
        }
        if (var.getDelta() > .8) {
            return ComponentType.Trend;
        }
        return ComponentType.Irregular;
    }

    public static ComponentType getType(final ITsVariable var) {

        // outliers
        if (var instanceof IOutlierVariable) {
            return getType((IOutlierVariable) var);
        }
        if (var instanceof ICalendarVariable) {
            return ComponentType.CalendarEffect;
        }
        if (var instanceof IMovingHolidayVariable) {
            return ComponentType.CalendarEffect;
        }
        if (var instanceof InterventionVariable) {
            return getType((InterventionVariable) var);
        }
        if (var instanceof Ramp) {
            return ComponentType.Trend;
        }
        if (var instanceof UserVariable) {
            return ((UserVariable) var).getType();
        }
        if (var instanceof Constant || var instanceof DiffConstant) {
            return ComponentType.Trend;
        }
        if (var instanceof ITsModifier) {
            ITsModifier m = (ITsModifier) var;
            return getType(m.getVariable());
        }

        return ComponentType.Undefined;

    }

    /**
     * @return the estimationDomain_
     */
    public TsDomain getSeriesDomain() {
        return original_.getDomain();
    }

    /**
     * @return the estimationDomain_
     */
    public TsDomain getEstimationDomain() {
        return y_.getDomain();
    }

    public int getFrequency() {
        return y_.getFrequency().intValue();
    }

    public DefaultTransformationType getTransformation() {
        return function_;
    }

    public LengthOfPeriodType getLengthOfPeriodAdjustment() {
        return lp_;
    }

    public double getUnits() {
        return units_;
    }

    public void setTransformation(DefaultTransformationType fn) {
        function_ = fn;
    }

    public void setLengthOfPeriodAdjustment(LengthOfPeriodType lp) {
        lp_ = lp;
    }

    public void setUnits(double u) {
        units_ = u;
    }

    /**
     * Back transformation
     *
     * @param T The series that will be back transformed contains the trend
     * component
     * @param S The series that will be back transformed contains the seasonal
     * component
     * @return The list of the transformation
     */
    public List<ITsDataTransformation> backTransformations(boolean T, boolean S) {
        ArrayList<ITsDataTransformation> tr = new ArrayList<>();

        if (function_ == DefaultTransformationType.Log) {
            tr.add(new ExpTransformation());
        }
        if (S && lp_ != LengthOfPeriodType.None) {
            tr.add(new LengthOfPeriodTransformation(lp_).converse());
        }
        if (units_ != 1 && (function_ == DefaultTransformationType.Log || T)) {
            tr.add(ConstTransformation.unit(1 / units_));
        }
        return tr;
    }

    public void backTransform(TsData s, boolean T, boolean S) {
        if (s == null) {
            return;
        }
        List<ITsDataTransformation> back = backTransformations(T, S);
        for (ITsDataTransformation t : back) {
            t.transform(s, null);
        }
    }

    public TsData interpolatedSeries(boolean bTransformed) {
        TsData y = y_.clone();
        if (!bTransformed) {
            backTransform(y, true, true);
        }
        return y;
    }

    public TsData linearizedSeries() {
        TsData interp = interpolatedSeries(true);
        TsData regs = regressionEffect(getSeriesDomain());
        return TsData.subtract(interp, regs);
    }

    // cmp is used in back transformation
    public <T extends ITsVariable> TsData regressionEffect(TsDomain domain) {
        if (b_ == null) {
            return new TsData(domain, 0);
        } else {
            DataBlock sum = PreadjustmentVariable.regressionEffect(x_.stream(), domain);

            if (sum == null) {
                sum = new DataBlock(domain.getLength());
            }
            TsData rslt = new TsData(domain.getStart(), sum.getData(), false);
            return rslt;
        }
    }

    // cmp is used in back transformation
    public <T extends ITsVariable> TsData regressionEffect(TsDomain domain, Class<T> tclass) {
        DataBlock sum = PreadjustmentVariable.regressionEffect(x_.stream(), domain, tclass);

        if (sum == null) {
            sum = new DataBlock(domain.getLength());
        }
        TsData rslt = new TsData(domain.getStart(), sum.getData(), false);
        return rslt;
    }

    private TsData regressionEffect(TsDomain domain, Predicate<PreadjustmentVariable> selector) {
        DataBlock sum = PreadjustmentVariable.regressionEffect(x_.stream(), domain, selector);
        return new TsData(domain.getStart(), sum.getData(), false);
    }

    public TsData outliersEffect(TsDomain domain) {
        return regressionEffect(domain, reg -> reg.isOutlier());
    }

    public TsData outliersEffect(TsDomain domain, final ComponentType type) {
        if (type == ComponentType.Undefined) {
            return regressionEffect(domain, reg -> reg.isOutlier());
        } else {
            return regressionEffect(domain, reg -> reg.getType() == type && reg.isOutlier());
        }
    }

    public List<TsData> regressors(TsDomain domain) {
        ArrayList<TsData> regs = new ArrayList<>();
        x_.stream().forEach(
                var -> {
                    DataBlock z = new DataBlock(domain.getLength());
                    var.addEffect(z, domain);
                    regs.add(new TsData(domain.getStart(), z.getData(), false));
                }
        );
        return regs;
    }

    public TsData deterministicEffect(TsDomain domain, final ComponentType type) {
        return regressionEffect(domain, reg -> reg.getType() == type);
    }

    public TsData userEffect(TsDomain domain, final ComponentType type) {
        return regressionEffect(domain, reg -> reg.getType() == type && reg.isUser());
    }

    public int countVariables(final Predicate<PreadjustmentVariable> pred) {
        int n = 0;
        for (PreadjustmentVariable var : x_) {
            if (pred.test(var)) {
                ++n;
            }
        }
        return n;
    }

    public int countRegressors(final Predicate<PreadjustmentVariable> pred) {
        int n = 0;
        for (PreadjustmentVariable var : x_) {
            if (pred.test(var)) {
                n += var.getVariable().getDim();
            }
        }
        return n;
    }

    @Override
    public Map<String, Class> getDictionary(boolean compact) {
        return dictionary(compact);
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        if (MAPPING.contains(id)) {
            return MAPPING.getData(this, id, tclass);
        } else {
            return null;
        }
    }

    @Override
    public boolean contains(String id) {
        synchronized (MAPPING) {
            return MAPPING.contains(id);
        }
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    public static void fillDictionary(String prefix, Map<String, Class> map, boolean compact) {
        MAPPING.fillDictionary(prefix, map, compact);
        ModelEstimation.fillDictionary(prefix, map, compact);
    }

    public static Map<String, Class> dictionary(boolean compact) {
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        fillDictionary(null, map, compact);
        return map;
    }

    private TsData op(TsData l, TsData r) {
        if (function_ == DefaultTransformationType.Log) {
            return TsData.multiply(l, r);
        } else {
            return TsData.add(l, r);
        }
    }

    private TsData inv_op(TsData l, TsData r) {
        if (function_ == DefaultTransformationType.Log) {
            return TsData.divide(l, r);
        } else {
            return TsData.subtract(l, r);
        }
    }

    private int getForecastCount() {
        return FCAST_YEAR * getFrequency();
    }

    private TsDomain domain(boolean fcast) {
        if (fcast) {
            TsDomain dom = getSeriesDomain();
            return new TsDomain(dom.getEnd(), FCAST_YEAR * dom.getFrequency().intValue());
        } else {
            return getSeriesDomain();
        }
    }

    public boolean isMultiplicative() {
        return function_ == DefaultTransformationType.Log;
    }

    public boolean setCoefficients(IReadDataBlock c) {
        if (c.getLength() != countRegressors(reg -> true)) {
            return false;
        }
        int cur = 0;
        for (PreadjustmentVariable var : x_) {
            int n = var.getCoefficients().length;
            c.rextract(cur, n).copyTo(var.getCoefficients(), 0);
            cur += n;
        }
        return true;
    }

    public static final String LOG = "log",
            ADJUST = "adjust",
            EASTER = "easter",
            NTD = "ntd", NMH = "nmh";
    // MAPPERS

    // MAPPING
    public static InformationMapping<DeterministicComponent> getMapping() {
        return MAPPING;
    }

    public static <T> void setMapping(String name, Class<T> tclass, Function<DeterministicComponent, T> extractor) {
        MAPPING.set(name, tclass, extractor);
    }

    public static <T> void setTsData(String name, Function<DeterministicComponent, TsData> extractor) {
        MAPPING.set(name, extractor);
    }

    private static final InformationMapping<DeterministicComponent> MAPPING = new InformationMapping<>(DeterministicComponent.class);

    static {
        MAPPING.set(LOG, Boolean.class, source -> source.isMultiplicative());
        MAPPING.set(ADJUST, Boolean.class, source -> source.getLengthOfPeriodAdjustment() != LengthOfPeriodType.None);
        MAPPING.set(ModellingDictionary.Y, source -> source.getOriginal());
        MAPPING.set(ModellingDictionary.YC, source -> source.interpolatedSeries(false));
        MAPPING.set(ModellingDictionary.Y_LIN, source -> source.linearizedSeries());
        MAPPING.set(ModellingDictionary.L, source -> source.linearizedSeries());
        MAPPING.set(ModellingDictionary.YCAL, source -> {
            TsData td = source.regressionEffect(source.domain(true), ICalendarVariable.class);
            TsData mh = source.regressionEffect(source.domain(true), IMovingHolidayVariable.class);
            TsData cal = TsData.add(td, mh);
            source.backTransform(cal, false, true);
            return source.inv_op(source.interpolatedSeries(false), cal);
        });
        MAPPING.set(ModellingDictionary.DET, source -> {
            TsData reg = source.regressionEffect(source.domain(false));
            source.backTransform(reg, false, true);
            return reg;
        });
        MAPPING.set(ModellingDictionary.DET + SeriesInfo.F_SUFFIX, source -> {
            TsData reg = source.regressionEffect(source.domain(true));
            source.backTransform(reg, false, true);
            return reg;
        });
        MAPPING.set(ModellingDictionary.CAL, source -> {
            TsData td = source.regressionEffect(source.domain(false), ICalendarVariable.class);
            TsData mh = source.regressionEffect(source.domain(false), IMovingHolidayVariable.class);
            TsData cal = TsData.add(td, mh);
            source.backTransform(cal, false, true);
            return cal;
        });
        MAPPING.set(ModellingDictionary.CAL + SeriesInfo.F_SUFFIX, source -> {
            TsData td = source.regressionEffect(source.domain(true), ICalendarVariable.class);
            TsData mh = source.regressionEffect(source.domain(true), IMovingHolidayVariable.class);
            TsData cal = TsData.add(td, mh);
            source.backTransform(cal, false, true);
            return cal;
        });
        MAPPING.set(ModellingDictionary.TDE, source -> {
            TsData cal = source.regressionEffect(source.domain(false), ICalendarVariable.class);
            source.backTransform(cal, false, true);
            return cal;
        });
        MAPPING.set(ModellingDictionary.TDE + SeriesInfo.F_SUFFIX, source -> {
            TsData cal = source.regressionEffect(source.domain(true), ICalendarVariable.class);
            source.backTransform(cal, false, true);
            return cal;
        });
        MAPPING.set(ModellingDictionary.EE, source -> {
            TsData cal = source.regressionEffect(source.domain(false), IEasterVariable.class);
            source.backTransform(cal, false, false);
            return cal;
        });
        MAPPING.set(ModellingDictionary.EE + SeriesInfo.F_SUFFIX, source -> {
            TsData cal = source.regressionEffect(source.domain(true), IEasterVariable.class);
            source.backTransform(cal, false, false);
            return cal;
        });
        MAPPING.set(ModellingDictionary.OMHE, source -> {
            TsData cal = source.regressionEffect(source.domain(false),
                    var -> (var instanceof IMovingHolidayVariable) && !(var instanceof IEasterVariable));
            source.backTransform(cal, false, false);
            return cal;
        });
        MAPPING.set(ModellingDictionary.OMHE + SeriesInfo.F_SUFFIX, source -> {
            TsData cal = source.regressionEffect(source.domain(true),
                    var -> (var instanceof IMovingHolidayVariable) && !(var instanceof IEasterVariable));
            source.backTransform(cal, false, false);
            return cal;
        });
        MAPPING.set(ModellingDictionary.MHE, source -> {
            TsData cal = source.regressionEffect(source.domain(false), IMovingHolidayVariable.class);
            source.backTransform(cal, false, false);
            return cal;
        });
        MAPPING.set(ModellingDictionary.MHE + SeriesInfo.F_SUFFIX, source -> {
            TsData cal = source.regressionEffect(source.domain(true), IMovingHolidayVariable.class);
            source.backTransform(cal, false, false);
            return cal;
        });
        MAPPING.set(ModellingDictionary.OUT, source -> {
            TsData o = source.outliersEffect(source.domain(false));
            source.backTransform(o, false, false);
            return o;
        });
        MAPPING.set(ModellingDictionary.OUT + SeriesInfo.F_SUFFIX, source -> {
            TsData o = source.outliersEffect(source.domain(true));
            source.backTransform(o, false, false);
            return o;
        });
        MAPPING.set(ModellingDictionary.OUT_I, source -> {
            TsData o = source.outliersEffect(source.domain(false), ComponentType.Irregular);
            source.backTransform(o, false, false);
            return o;
        });
        MAPPING.set(ModellingDictionary.OUT_I + SeriesInfo.F_SUFFIX, source -> {
            TsData o = source.outliersEffect(source.domain(true), ComponentType.Irregular);
            source.backTransform(o, false, false);
            return o;
        });
        MAPPING.set(ModellingDictionary.OUT_T, source -> {
            TsData o = source.outliersEffect(source.domain(false), ComponentType.Trend);
            source.backTransform(o, false, false);
            return o;
        });
        MAPPING.set(ModellingDictionary.OUT_T + SeriesInfo.F_SUFFIX, source -> {
            TsData o = source.outliersEffect(source.domain(true), ComponentType.Trend);
            source.backTransform(o, false, false);
            return o;
        });
        MAPPING.set(ModellingDictionary.OUT_S, source -> {
            TsData o = source.outliersEffect(source.domain(false), ComponentType.Seasonal);
            source.backTransform(o, false, false);
            return o;
        });
        MAPPING.set(ModellingDictionary.OUT_S + SeriesInfo.F_SUFFIX, source -> {
            TsData o = source.outliersEffect(source.domain(true), ComponentType.Seasonal);
            source.backTransform(o, false, false);
            return o;
        });
        MAPPING.set(ModellingDictionary.REG, source -> {
            TsData r = source.regressionEffect(source.domain(false), UserVariable.class);
            source.backTransform(r, false, false);
            return r;
        });
        MAPPING.set(ModellingDictionary.REG + SeriesInfo.F_SUFFIX, source -> {
            TsData r = source.regressionEffect(source.domain(true), UserVariable.class);
            source.backTransform(r, false, false);
            return r;
        });
        MAPPING.set(ModellingDictionary.REG_T, source -> {
            TsData r = source.userEffect(source.domain(false), ComponentType.Trend);
            source.backTransform(r, false, false);
            return r;
        });
        MAPPING.set(ModellingDictionary.REG_T + SeriesInfo.F_SUFFIX, source -> {
            TsData r = source.userEffect(source.domain(true), ComponentType.Trend);
            source.backTransform(r, false, false);
            return r;
        });
        MAPPING.set(ModellingDictionary.REG_S, source -> {
            TsData r = source.userEffect(source.domain(false), ComponentType.Seasonal);
            source.backTransform(r, false, false);
            return r;
        });
        MAPPING.set(ModellingDictionary.REG_S + SeriesInfo.F_SUFFIX, source -> {
            TsData r = source.userEffect(source.domain(true), ComponentType.Seasonal);
            source.backTransform(r, false, false);
            return r;
        });
        MAPPING.set(ModellingDictionary.REG_I, source -> {
            TsData r = source.userEffect(source.domain(false), ComponentType.Irregular);
            source.backTransform(r, false, false);
            return r;
        });
        MAPPING.set(ModellingDictionary.REG_I + SeriesInfo.F_SUFFIX, source -> {
            TsData r = source.userEffect(source.domain(true), ComponentType.Irregular);
            source.backTransform(r, false, false);
            return r;
        });
        MAPPING.set(ModellingDictionary.REG_SA, source -> {
            TsData r = source.userEffect(source.domain(false), ComponentType.SeasonallyAdjusted);
            source.backTransform(r, false, false);
            return r;
        });
        MAPPING.set(ModellingDictionary.REG_SA + SeriesInfo.F_SUFFIX, source -> {
            TsData r = source.userEffect(source.domain(true), ComponentType.SeasonallyAdjusted);
            source.backTransform(r, false, false);
            return r;
        });
        MAPPING.set(ModellingDictionary.REG_Y, source -> {
            TsData r = source.userEffect(source.domain(false), ComponentType.Series);
            source.backTransform(r, false, false);
            return r;
        });
        MAPPING.set(ModellingDictionary.REG_Y + SeriesInfo.F_SUFFIX, source -> {
            TsData r = source.userEffect(source.domain(true), ComponentType.Series);
            source.backTransform(r, false, false);
            return r;
        });

        MAPPING.set(NTD, Integer.class, source -> source.countRegressors(reg -> reg.isCalendar()));
        MAPPING.set(NMH, Integer.class, source -> source.countRegressors(reg -> reg.isMovingHoliday()));
    }
}
