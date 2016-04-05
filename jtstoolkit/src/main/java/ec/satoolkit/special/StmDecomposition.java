/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.special;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.ISaResults;
import ec.satoolkit.ISeriesDecomposition;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.ssf.ExtendedSsfData;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.structural.BasicStructuralModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.tstoolkit.ucarima.WienerKolmogorovEstimators;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class StmDecomposition implements ISaResults{

    public static final String MODEL = "model";
    public static final String SERIES = "series", LEVEL = "level", CYCLE = "cycle", SLOPE = "slope", NOISE = "noise", SEASONAL = "seasonal";
    private final SmoothingResults srslts_;
    private final TsData y_, yf_, t_, sa_, s_, i_, c_;
    private final boolean mul_;
    private final InformationSet info_ = new InformationSet();
    private UcarimaModel reduced_;
    private double errFactor_;
    private WienerKolmogorovEstimators wk_;
    private final BasicStructuralModel model_;

    public StmDecomposition(TsData ylin, BasicStructuralModel model, boolean mul) {
        y_ = ylin;
        mul_ = mul;
        model_=model;
        Smoother smoother = new Smoother();
        smoother.setSsf(model);
        ExtendedSsfData data = new ExtendedSsfData(new SsfData(ylin.internalStorage(), null));
        data.setForecastsCount(ylin.getFrequency().intValue());
        TsDomain full=ylin.getDomain().extend(0, data.getForecastsCount());
        srslts_ = new SmoothingResults();
        smoother.process(data, srslts_);
//        DisturbanceSmoother smoother = new DisturbanceSmoother();
//        smoother.setSsf(model);
//        SsfData data = new SsfData(y.getValues().internalStorage(), null);
//        smoother.process(data);
//        srslts_ = smoother.calcSmoothedStates();
        InformationSet minfo = info_.subSet(MODEL);
        int[] cmps = model.getCmpPositions();
        int cur = 0;
        TsData noise, level, slope, seasonal = null, cycle = null;
        if (model.getSpecification().hasNoise()) {
            noise = new TsData(ylin.getStart(), srslts_.component(cmps[cur++]), false);
            minfo.add(NOISE, noise);
            i_ = noise;
        } else {
            i_ = new TsData(full, 0);
        }
        if (model.getSpecification().hasCycle()) {
            cycle = new TsData(ylin.getStart(), srslts_.component(cmps[cur++]), false);
            minfo.add(CYCLE, cycle);
            c_ = cycle;
        } else {
            c_ = new TsData(full, 0);
        }
        if (model.getSpecification().hasLevel()) {
            level = new TsData(ylin.getStart(), srslts_.component(cmps[cur++]), false);
            minfo.add(LEVEL, level);
            t_ = TsData.add(level, cycle);
            if (model.getSpecification().hasSlope()) {
                slope = new TsData(ylin.getStart(), srslts_.component(cmps[cur++]), false);
                minfo.add(SLOPE, slope);
            }
        } else {
            t_ = c_; //new TsData(y.getDomain(), 0);
        }
        if (model.getSpecification().hasSeasonal()) {
            seasonal = new TsData(ylin.getStart(), srslts_.component(cmps[cur++]), false);
            minfo.add(SEASONAL, seasonal);
            s_ = seasonal;
        } else {
            s_ = new TsData(full, 0);
        }
        minfo.add(SERIES, ylin);
        TsData all = TsData.add(t_, s_);
        all = TsData.add(all, i_);
        all = all.update(y_);
        sa_ = TsData.subtract(all, seasonal);
        yf_ = all.drop(y_.getLength(), 0);
    }

    public UcarimaModel getUcarimaModel() {
        if (reduced_ == null) {
            reduced_ = model_.computeReducedModel(false);
            errFactor_ = reduced_.normalize();
        }
        return reduced_;
    }

    public double getResidualsScalingFactor() {
        if (reduced_ == null) {
            reduced_ = model_.computeReducedModel(false);
            errFactor_ = reduced_.normalize();
        }
        return Math.sqrt(errFactor_);

    }
    public WienerKolmogorovEstimators getWienerKolmogorovEstimators() {
        if (wk_ == null) {
            wk_ = new WienerKolmogorovEstimators(getUcarimaModel());
        }
        return wk_;

    }

    public List<String> getTsDataDictionary() {
        return info_.getDictionary(TsData.class);
    }

    @Override
    public boolean contains(String id) {
        synchronized (mapper) {
            if (mapper.contains(id)) {
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
        return Collections.EMPTY_LIST;
    }

    public ISeriesDecomposition getComponents() {
        DefaultSeriesDecomposition decomposition
                = new DefaultSeriesDecomposition(DecompositionMode.Additive);
        TsDomain dom = y_.getDomain(), fdom = yf_.getDomain();
        decomposition.add(y_, ComponentType.Series);
        decomposition.add(sa_.fittoDomain(dom), ComponentType.SeasonallyAdjusted);
        decomposition.add(t_.fittoDomain(dom), ComponentType.Trend);
        decomposition.add(s_.fittoDomain(dom), ComponentType.Seasonal);
        decomposition.add(i_.fittoDomain(dom), ComponentType.Irregular);
        decomposition.add(yf_, ComponentType.Series, ComponentInformation.Forecast);
        decomposition.add(sa_.fittoDomain(fdom), ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
        decomposition.add(t_.fittoDomain(fdom), ComponentType.Trend, ComponentInformation.Forecast);
        decomposition.add(s_.fittoDomain(fdom), ComponentType.Seasonal, ComponentInformation.Forecast);
        decomposition.add(i_.fittoDomain(fdom), ComponentType.Irregular, ComponentInformation.Forecast);
        return decomposition;
    }

    @Override
    public ISeriesDecomposition getSeriesDecomposition() {
        if (!mul_) {
            return getComponents();
        }
        DefaultSeriesDecomposition decomposition
                = new DefaultSeriesDecomposition(DecompositionMode.Multiplicative);
        TsDomain dom = y_.getDomain(), fdom = yf_.getDomain();
        decomposition.add(y_.exp(), ComponentType.Series);
        decomposition.add(sa_.fittoDomain(dom).exp(), ComponentType.SeasonallyAdjusted);
        decomposition.add(t_.fittoDomain(dom).exp(), ComponentType.Trend);
        decomposition.add(s_.fittoDomain(dom).exp(), ComponentType.Seasonal);
        decomposition.add(i_.fittoDomain(dom).exp(), ComponentType.Irregular);
        decomposition.add(yf_.exp(), ComponentType.Series, ComponentInformation.Forecast);
        decomposition.add(sa_.fittoDomain(fdom).exp(), ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
        decomposition.add(t_.fittoDomain(fdom).exp(), ComponentType.Trend, ComponentInformation.Forecast);
        decomposition.add(s_.fittoDomain(fdom).exp(), ComponentType.Seasonal, ComponentInformation.Forecast);
        decomposition.add(i_.fittoDomain(fdom).exp(), ComponentType.Irregular, ComponentInformation.Forecast);
        return decomposition;
    }

    @Override
    public Map<String, Class> getDictionary() {
        // TODO
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        mapper.fillDictionary(null, map);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        synchronized (mapper) {
            if (mapper.contains(id)) {
                return mapper.getData(this, id, tclass);
            }
            if (!id.contains(InformationSet.STRSEP)) {
                return info_.deepSearch(id, tclass);
            } else {
                return info_.search(id, tclass);
            }
        }
    }

    @Override
    public InformationSet getInformation() {
        return info_;
    }

    // MAPPERS

    public static <T> void addMapping(String name, InformationMapper.Mapper<StmDecomposition, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }

    private static final InformationMapper<StmDecomposition> mapper = new InformationMapper<>();

    static {
        mapper.add(ModellingDictionary.Y_CMP, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                return source.mul_ ? source.y_.exp() : source.y_;
            }
        });
        mapper.add(ModellingDictionary.Y_CMP + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                return source.mul_ ? source.yf_.exp() : source.yf_;
            }
        });
        mapper.add(ModellingDictionary.T_CMP, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                TsData x = source.t_.fittoDomain(source.y_.getDomain());
                return source.mul_ ? x.exp() : x;
            }
        });
        mapper.add(ModellingDictionary.T_CMP + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                TsData x = source.t_.fittoDomain(source.yf_.getDomain());
                return source.mul_ ? x.exp() : x;
            }
        });
        mapper.add(ModellingDictionary.T_CMP + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                TsData x = source.t_.fittoDomain(source.yf_.getDomain());
                return source.mul_ ? x.exp() : x;
            }
        });
        mapper.add(ModellingDictionary.SA_CMP, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                TsData x = source.sa_.fittoDomain(source.y_.getDomain());
                return source.mul_ ? x.exp() : x;
            }
        });
        mapper.add(ModellingDictionary.SA_CMP + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                TsData x = source.sa_.fittoDomain(source.yf_.getDomain());
                return source.mul_ ? x.exp() : x;
            }
        });
        mapper.add(ModellingDictionary.S_CMP, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                TsData x = source.s_.fittoDomain(source.y_.getDomain());
                return source.mul_ ? x.exp() : x;
            }
        });
        mapper.add(ModellingDictionary.S_CMP + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                TsData x = source.s_.fittoDomain(source.yf_.getDomain());
                return source.mul_ ? x.exp() : x;
            }
        });
        mapper.add(ModellingDictionary.I_CMP, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                TsData x = source.i_.fittoDomain(source.y_.getDomain());
                return source.mul_ ? x.exp() : x;
            }
        });
        mapper.add(ModellingDictionary.I_CMP + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                TsData x = source.i_.fittoDomain(source.yf_.getDomain());
                return source.mul_ ? x.exp() : x;
            }
        });
        mapper.add(ModellingDictionary.SI_CMP, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                TsData si = TsData.add(source.s_, source.i_);
                return source.mul_ ? si.exp() : si;
            }
        });
        mapper.add(ModellingDictionary.Y_LIN, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                return source.y_;
            }
        });
        mapper.add(ModellingDictionary.Y_LIN + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                return source.yf_;
            }
        });
        mapper.add(ModellingDictionary.T_LIN, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                return source.t_.fittoDomain(source.y_.getDomain());
            }
        });
        mapper.add(ModellingDictionary.T_LIN + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                return source.t_.fittoDomain(source.yf_.getDomain());
            }
        });
        mapper.add(ModellingDictionary.SA_LIN, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                return source.sa_.fittoDomain(source.y_.getDomain());
            }
        });
        mapper.add(ModellingDictionary.SA_LIN + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                return source.sa_.fittoDomain(source.yf_.getDomain());
            }
        });
        mapper.add(ModellingDictionary.S_LIN, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                return source.s_.fittoDomain(source.y_.getDomain());
            }
        });
        mapper.add(ModellingDictionary.S_LIN + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                return source.s_.fittoDomain(source.yf_.getDomain());
            }
        });
        mapper.add(ModellingDictionary.I_LIN, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                return source.i_.fittoDomain(source.y_.getDomain());
            }
        });
        mapper.add(ModellingDictionary.I_LIN + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<StmDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmDecomposition source) {
                return source.i_.fittoDomain(source.yf_.getDomain());
            }
        });
    }
}
