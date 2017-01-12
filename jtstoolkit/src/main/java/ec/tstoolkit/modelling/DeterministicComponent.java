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
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.information.InformationMapper;
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
        return regressionEffect(domain, reg->reg.isOutlier());
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
    
    public int countVariables(final Predicate<PreadjustmentVariable> pred){
        int n=0;
        for (PreadjustmentVariable var : x_){
            if (pred.test(var))
                ++n;
        }
        return n;
    }

    public int countRegressors(final Predicate<PreadjustmentVariable> pred){
        int n=0;
        for (PreadjustmentVariable var : x_){
            if (pred.test(var))
                n+=var.getVariable().getDim();
        }
        return n;
    }

    @Override
    public Map<String, Class> getDictionary() {
        return dictionary();
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        if (mapper.contains(id)) {
            return mapper.getData(this, id, tclass);
        } else {
            return null;
        }
    }

    @Override
    public boolean contains(String id) {
        synchronized (mapper) {
            return mapper.contains(id);
        }
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
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
        if (c.getLength() != countRegressors(reg->true))
            return false;
        int cur=0;
        for (PreadjustmentVariable var : x_){
            int n=var.getCoefficients().length;
            c.rextract(cur, n).copyTo(var.getCoefficients(), 0);
            cur+=n;
        }
        return true;
    }


    public static final String LOG = "log",
            ADJUST = "adjust",
            EASTER = "easter",
            NTD = "ntd", NMH = "nmh";
    // MAPPERS

    public static <T> void addMapping(String name, InformationMapper.Mapper<DeterministicComponent, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }
    private static final InformationMapper<DeterministicComponent> mapper = new InformationMapper<>();

    static {
        mapper.add(LOG, new InformationMapper.Mapper<DeterministicComponent, Boolean>(Boolean.class) {
            @Override
            public Boolean retrieve(DeterministicComponent source) {
                return source.isMultiplicative();
            }
        });
        mapper.add(ADJUST, new InformationMapper.Mapper<DeterministicComponent, Boolean>(Boolean.class) {
            @Override
            public Boolean retrieve(DeterministicComponent source) {
                return source.getLengthOfPeriodAdjustment() != LengthOfPeriodType.None;
            }
        });
        mapper.add(ModellingDictionary.Y, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                return source.getOriginal();
            }
        });
        mapper.add(ModellingDictionary.YC, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                return source.interpolatedSeries(false);
            }
        });
        mapper.add(ModellingDictionary.Y_LIN, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                return source.linearizedSeries();
            }
        });
        mapper.add(ModellingDictionary.L, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                return source.linearizedSeries();
            }
        });
        mapper.add(ModellingDictionary.YCAL, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData td = source.regressionEffect(source.domain(true), ICalendarVariable.class);
                TsData mh = source.regressionEffect(source.domain(true), IMovingHolidayVariable.class);
                TsData cal = TsData.add(td, mh);
                source.backTransform(cal, false, true);
                return source.inv_op(source.interpolatedSeries(false), cal);
            }
        });
        mapper.add(ModellingDictionary.DET, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData reg = source.regressionEffect(source.domain(false));
                source.backTransform(reg, false, true);
                return reg;
            }
        });
        mapper.add(ModellingDictionary.DET + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData reg = source.regressionEffect(source.domain(true));
                source.backTransform(reg, false, true);
                return reg;
            }
        });
        mapper.add(ModellingDictionary.CAL, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData td = source.regressionEffect(source.domain(false), ICalendarVariable.class);
                TsData mh = source.regressionEffect(source.domain(false), IMovingHolidayVariable.class);
                TsData cal = TsData.add(td, mh);
                source.backTransform(cal, false, true);
                return cal;
            }
        });
        mapper.add(ModellingDictionary.CAL + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData td = source.regressionEffect(source.domain(true), ICalendarVariable.class);
                TsData mh = source.regressionEffect(source.domain(true), IMovingHolidayVariable.class);
                TsData cal = TsData.add(td, mh);
                source.backTransform(cal, false, true);
                return cal;
            }
        });
        mapper.add(ModellingDictionary.TDE, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData cal = source.regressionEffect(source.domain(false), ICalendarVariable.class);
                source.backTransform(cal, false, true);
                return cal;
            }
        });
        mapper.add(ModellingDictionary.TDE + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData cal = source.regressionEffect(source.domain(true), ICalendarVariable.class);
                source.backTransform(cal, false, true);
                return cal;
            }
        });
        mapper.add(ModellingDictionary.EE, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData cal = source.regressionEffect(source.domain(false), IEasterVariable.class);
                source.backTransform(cal, false, false);
                return cal;
            }
        });
        mapper.add(ModellingDictionary.EE + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData cal = source.regressionEffect(source.domain(true), IEasterVariable.class);
                source.backTransform(cal, false, false);
                return cal;
            }
        });
        mapper.add(ModellingDictionary.OMHE, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData cal = source.regressionEffect(source.domain(false),
                        var -> (var instanceof IMovingHolidayVariable) && !(var instanceof IEasterVariable));
                source.backTransform(cal, false, false);
                return cal;
            }
        });
        mapper.add(ModellingDictionary.OMHE + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData cal = source.regressionEffect(source.domain(true),
                        var -> (var instanceof IMovingHolidayVariable) && !(var instanceof IEasterVariable));
                source.backTransform(cal, false, false);
                return cal;
            }
        });
        mapper.add(ModellingDictionary.MHE, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData cal = source.regressionEffect(source.domain(false), IMovingHolidayVariable.class);
                source.backTransform(cal, false, false);
                return cal;
            }
        });
        mapper.add(ModellingDictionary.MHE + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData cal = source.regressionEffect(source.domain(true), IMovingHolidayVariable.class);
                source.backTransform(cal, false, false);
                return cal;
            }
        });
        mapper.add(ModellingDictionary.OUT, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData o = source.outliersEffect(source.domain(false));
                source.backTransform(o, false, false);
                return o;
            }
        });
        mapper.add(ModellingDictionary.OUT + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData o = source.outliersEffect(source.domain(true));
                source.backTransform(o, false, false);
                return o;
            }
        });
        mapper.add(ModellingDictionary.OUT_I, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData o = source.outliersEffect(source.domain(false), ComponentType.Irregular);
                source.backTransform(o, false, false);
                return o;
            }
        });
        mapper.add(ModellingDictionary.OUT_I + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData o = source.outliersEffect(source.domain(true), ComponentType.Irregular);
                source.backTransform(o, false, false);
                return o;
            }
        });
        mapper.add(ModellingDictionary.OUT_T, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData o = source.outliersEffect(source.domain(false), ComponentType.Trend);
                source.backTransform(o, false, false);
                return o;
            }
        });
        mapper.add(ModellingDictionary.OUT_T + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData o = source.outliersEffect(source.domain(true), ComponentType.Trend);
                source.backTransform(o, false, false);
                return o;
            }
        });
        mapper.add(ModellingDictionary.OUT_S, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData o = source.outliersEffect(source.domain(false), ComponentType.Seasonal);
                source.backTransform(o, false, false);
                return o;
            }
        });
        mapper.add(ModellingDictionary.OUT_S + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData o = source.outliersEffect(source.domain(true), ComponentType.Seasonal);
                source.backTransform(o, false, false);
                return o;
            }
        });
        mapper.add(ModellingDictionary.REG, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData r = source.regressionEffect(source.domain(false), UserVariable.class);
                source.backTransform(r, false, false);
                return r;
            }
        });
        mapper.add(ModellingDictionary.REG + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData r = source.regressionEffect(source.domain(true), UserVariable.class);
                source.backTransform(r, false, false);
                return r;
            }
        });
        mapper.add(ModellingDictionary.REG_T, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData r = source.userEffect(source.domain(false), ComponentType.Trend);
                source.backTransform(r, false, false);
                return r;
            }
        });
        mapper.add(ModellingDictionary.REG_T + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData r = source.userEffect(source.domain(true), ComponentType.Trend);
                source.backTransform(r, false, false);
                return r;
            }
        });
        mapper.add(ModellingDictionary.REG_S, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData r = source.userEffect(source.domain(false), ComponentType.Seasonal);
                source.backTransform(r, false, false);
                return r;
            }
        });
        mapper.add(ModellingDictionary.REG_S + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData r = source.userEffect(source.domain(true), ComponentType.Seasonal);
                source.backTransform(r, false, false);
                return r;
            }
        });
        mapper.add(ModellingDictionary.REG_I, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData r = source.userEffect(source.domain(false), ComponentType.Irregular);
                source.backTransform(r, false, false);
                return r;
            }
        });
        mapper.add(ModellingDictionary.REG_I + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData r = source.userEffect(source.domain(true), ComponentType.Irregular);
                source.backTransform(r, false, false);
                return r;
            }
        });
        mapper.add(ModellingDictionary.REG_SA, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData r = source.userEffect(source.domain(false), ComponentType.SeasonallyAdjusted);
                source.backTransform(r, false, false);
                return r;
            }
        });
        mapper.add(ModellingDictionary.REG_SA + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData r = source.userEffect(source.domain(true), ComponentType.SeasonallyAdjusted);
                source.backTransform(r, false, false);
                return r;
            }
        });
        mapper.add(ModellingDictionary.REG_Y, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData r = source.userEffect(source.domain(false), ComponentType.Series);
                source.backTransform(r, false, false);
                return r;
            }
        });
        mapper.add(ModellingDictionary.REG_Y + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<DeterministicComponent, TsData>(TsData.class) {
            @Override
            public TsData retrieve(DeterministicComponent source) {
                TsData r = source.userEffect(source.domain(true), ComponentType.Series);
                source.backTransform(r, false, false);
                return r;
            }
        });

        mapper.add(NTD, new InformationMapper.Mapper<DeterministicComponent, Integer>(Integer.class) {
            @Override
            public Integer retrieve(final DeterministicComponent source) {
                return source.countRegressors(reg->reg.isCalendar());
            }
        });
        mapper.add(NMH, new InformationMapper.Mapper<DeterministicComponent, Integer>(Integer.class) {
            @Override
            public Integer retrieve(final DeterministicComponent source) {
                return source.countRegressors(reg->reg.isMovingHoliday());
            }
        });
    }

}
