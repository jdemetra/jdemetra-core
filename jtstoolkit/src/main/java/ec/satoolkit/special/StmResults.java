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

package ec.satoolkit.special;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.ISaResults;
import ec.satoolkit.ISeriesDecomposition;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.eco.DiffuseLikelihood;
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.structural.BasicStructuralModel;
import ec.tstoolkit.structural.BsmMonitor;
import ec.tstoolkit.timeseries.regression.TsVariableList;
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
 * @author pcuser
 */
public class StmResults implements ISaResults {

    public static final String MODEL = "model";
    public static final String SERIES = "series", LEVEL = "level", SLOPE = "slope", NOISE = "noise", SEASONAL = "seasonal";
    private final BsmMonitor monitor_;
    private final SmoothingResults srslts_;
    private final InformationSet info_ = new InformationSet();
    private final TsData y_, t_, sa_, s_, i_;
    private UcarimaModel reduced_;
    private double errFactor_;
    private WienerKolmogorovEstimators wk_;
    private final boolean mul_;
    private final TsVariableList x_;

    public StmResults(TsData y,TsVariableList x, BsmMonitor monitor, boolean mul) {
        monitor_ = monitor;
        y_ = y;
        x_=x;
        mul_ = mul;
        BasicStructuralModel model = monitor.getResult();
        Smoother smoother = new Smoother();
        smoother.setSsf(model);
        SsfData data = new SsfData(y.getValues().internalStorage(), null);
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
        TsData noise = null, level = null, slope = null, seasonal = null;
        if (model.getSpecification().hasNoise()) {
            noise = new TsData(y.getStart(), srslts_.component(cmps[cur++]), false);
            minfo.add(NOISE, noise);
            i_ = noise;
        } else {
            i_ = new TsData(y.getDomain(), 0);

        }
        if (model.getSpecification().hasLevel()) {
            level = new TsData(y.getStart(), srslts_.component(cmps[cur++]), false);
            minfo.add(LEVEL, level);
            t_ = level;
            if (model.getSpecification().hasSlope()) {
                slope = new TsData(y.getStart(), srslts_.component(cmps[cur++]), false);
                minfo.add(SLOPE, slope);
            }
        } else {
            t_ = new TsData(y.getDomain(), 0);
        }
        if (model.getSpecification().hasSeasonal()) {
            seasonal = new TsData(y.getStart(), srslts_.component(cmps[cur++]), false);
            minfo.add(SEASONAL, seasonal);
            s_ = seasonal;
        } else {
            s_ = new TsData(y.getDomain(), 0);
        }
        minfo.add(SERIES, y);
        sa_ = TsData.subtract(y, seasonal);
    }
    
    public TsVariableList getX(){
        return x_;
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
        DefaultSeriesDecomposition decomposition =
                new DefaultSeriesDecomposition(DecompositionMode.Additive);
        decomposition.add(y_, ComponentType.Series);
        decomposition.add(sa_, ComponentType.SeasonallyAdjusted);
        decomposition.add(t_, ComponentType.Trend);
        decomposition.add(s_, ComponentType.Seasonal);
        decomposition.add(i_, ComponentType.Irregular);
        return decomposition;
    }

    @Override
    public ISeriesDecomposition getSeriesDecomposition() {
        DefaultSeriesDecomposition decomposition =
                new DefaultSeriesDecomposition(mul_ ? DecompositionMode.Multiplicative : DecompositionMode.Additive);
        if (mul_) {
            decomposition.add(y_.exp(), ComponentType.Series);
            decomposition.add(sa_.exp(), ComponentType.SeasonallyAdjusted);
            decomposition.add(t_.exp(), ComponentType.Trend);
            decomposition.add(s_.exp(), ComponentType.Seasonal);
            decomposition.add(i_.exp(), ComponentType.Irregular);
        } else {
            decomposition.add(y_, ComponentType.Series);
            decomposition.add(sa_, ComponentType.SeasonallyAdjusted);
            decomposition.add(t_, ComponentType.Trend);
            decomposition.add(s_, ComponentType.Seasonal);
            decomposition.add(i_, ComponentType.Irregular);
        }
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

    public UcarimaModel getUcarimaModel() {
        if (reduced_ == null) {
            reduced_ = monitor_.getResult().computeReducedModel(false);
            errFactor_ = reduced_.normalize();
        }
        return reduced_;
    }

    public double getResidualsScalingFactor() {
        if (reduced_ == null) {
            reduced_ = monitor_.getResult().computeReducedModel(false);
            errFactor_ = reduced_.normalize();
        }
        return Math.sqrt(errFactor_);

    }

    public BasicStructuralModel getModel() {
        return monitor_.getResult();
    }

    public WienerKolmogorovEstimators getWienerKolmogorovEstimators() {
        if (wk_ == null) {
            wk_ = new WienerKolmogorovEstimators(getUcarimaModel());
        }
        return wk_;

    }

    public TsData getResiduals() {
        TsDomain domain = y_.getDomain();
        double[] res = monitor_.getLikelihood().getResiduals();
        return new TsData(domain.getStart().plus(domain.getLength() - res.length), res, false);
    }

    public DiffuseConcentratedLikelihood getLikelihood() {
        return monitor_.getLikelihood();
    }

    public IFunction likelihoodFunction() {
        return monitor_.likelihoodFunction();
    }

    public IFunctionInstance maxLikelihoodFunction() {
        return monitor_.maxLikelihoodFunction();
    }
    
    @Override
    public InformationSet getInformation() {
        return info_;
    }
    // MAPPERS
     public static <T> void addMapping(String name, InformationMapper.Mapper<StmResults, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }

   private static final InformationMapper<StmResults> mapper = new InformationMapper<>();

    static {
        mapper.add(ModellingDictionary.Y_CMP, new InformationMapper.Mapper<StmResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmResults source) {
                return source.mul_ ? source.y_.exp() : source.y_;
            }
        });
        mapper.add(ModellingDictionary.T_CMP, new InformationMapper.Mapper<StmResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmResults source) {
                return source.mul_ ? source.t_.exp() : source.t_;
            }
        });
        mapper.add(ModellingDictionary.SA_CMP, new InformationMapper.Mapper<StmResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmResults source) {
                return source.mul_ ? source.sa_.exp() : source.sa_;
            }
        });
        mapper.add(ModellingDictionary.S_CMP, new InformationMapper.Mapper<StmResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmResults source) {
                return source.mul_ ? source.s_.exp() : source.s_;
            }
        });
        mapper.add(ModellingDictionary.I_CMP, new InformationMapper.Mapper<StmResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmResults source) {
                return source.mul_ ? source.i_.exp() : source.i_;
            }
        });
        mapper.add(ModellingDictionary.SI_CMP, new InformationMapper.Mapper<StmResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmResults source) {
                TsData si=TsData.add(source.s_, source.i_);
                if (si == null)
                    return null;
                return source.mul_ ? si.exp() : si;
            }
        });
        mapper.add(ModellingDictionary.Y_LIN, new InformationMapper.Mapper<StmResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmResults source) {
                return source.y_;
            }
        });
        mapper.add(ModellingDictionary.T_LIN, new InformationMapper.Mapper<StmResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmResults source) {
                return source.t_;
            }
        });
        mapper.add(ModellingDictionary.SA_LIN, new InformationMapper.Mapper<StmResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmResults source) {
                return source.sa_;
            }
        });
        mapper.add(ModellingDictionary.S_LIN, new InformationMapper.Mapper<StmResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmResults source) {
                return source.s_;
            }
        });
        mapper.add(ModellingDictionary.I_LIN, new InformationMapper.Mapper<StmResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmResults source) {
                return source.i_;
            }
        });
        mapper.add("residuals", new InformationMapper.Mapper<StmResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmResults source) {
                return source.getResiduals();
            }
        });
    }
}
