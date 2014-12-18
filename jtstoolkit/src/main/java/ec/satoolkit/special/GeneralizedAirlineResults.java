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
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.special.GeneralizedAirlineModel;
import ec.tstoolkit.arima.special.GeneralizedAirlineMonitor;
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.ssf.DiffuseFilteringResults;
import ec.tstoolkit.ssf.DisturbanceSmoother;
import ec.tstoolkit.ssf.FastFilter;
import ec.tstoolkit.ssf.ucarima.SsfUcarima;
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
public class GeneralizedAirlineResults implements ISaResults {

    public GeneralizedAirlineResults(TsData y, GeneralizedAirlineMonitor monitor, boolean b) {
        y_ = y;
        mul_ = b;
        monitor_ = monitor;
        int best = monitor.searchBestEstimation();
        if (best < 0) {
            best_ = null;
            ucm_ = null;
            t_ = null;
            sa_ = null;
            s_ = null;
            i_ = null;
        } else {
            best_ = monitor.getBestResult();
            UcarimaModel ucm = doCanonicalDecomposition(monitor);
            if (ucm != null) {
                // compute KS
                SsfUcarima ssf = new SsfUcarima(ucm);
                DisturbanceSmoother smoother = new DisturbanceSmoother();
                smoother.setSsf(ssf);
                FastFilter<SsfUcarima> filter = new FastFilter<>();
                filter.setSsf(ssf);
                DiffuseFilteringResults frslts = new DiffuseFilteringResults(true);

                filter.process(new ec.tstoolkit.ssf.SsfData(y_.getValues().internalStorage(), null), frslts);

                smoother.process(new ec.tstoolkit.ssf.SsfData(y_.getValues().internalStorage(), null), frslts);
                ec.tstoolkit.ssf.SmoothingResults srslts = smoother.calcSmoothedStates();

                // ec.tstoolkit.ssf.Smoother smoother = new ec.tstoolkit.ssf.Smoother();
                // smoother.setSsf(ssf);
                //
                // ec.tstoolkit.ssf.SmoothingResults srslts =
                // new ec.tstoolkit.ssf.SmoothingResults();
                //
                // smoother.process(new
                // ec.tstoolkit.ssf.SsfData(s.getValues().internalStorage(), null),
                // srslts);
                TsData[] cmps = new TsData[ucm.getComponentsCount()];
                for (int i = 0; i < ucm.getComponentsCount(); ++i) {
                    double[] tmp = srslts.component(ssf.cmpPos(i));
                    cmps[i] = new TsData(y_.getStart(), tmp, false);
                }

                int cur = 0;
                ucm_ = ucm;
                t_ = cmps[cur++];
                s_ = cmps[cur];
                sa_ = TsData.subtract(y_, cmps[cur++]);
                i_ = cmps[cur];
            } else {
                ucm_ = null;
                t_ = null;
                s_ = null;
                sa_ = null;
                i_ = null;
            }
        }
    }

    private UcarimaModel doCanonicalDecomposition(GeneralizedAirlineMonitor monitor) {
        try {
            UcarimaModel ucm = best_.model.getArima().toUCModel(monitor.getSpecification().getURBound());
            if (ucm == null) {
                return null;
            }
// correction for mean...
            if (monitor.isMeanCorrection()) {
                UcarimaModel tmp = new UcarimaModel();
                ArimaModel tm = ucm.getComponent(0);
                BackFilter ur = BackFilter.D1;
                if (tm.isNull()) {
                    tm = new ArimaModel(null, ur, ur, 0);
                } else {
                    tm = new ArimaModel(tm.getStationaryAR(), tm.getNonStationaryAR().times(ur), tm.getMA().times(ur),
                            tm.getInnovationVariance());
                }
                tmp.addComponent(tm);
                for (int i = 1; i < ucm.getComponentsCount(); ++i) {
                    tmp.addComponent(ucm.getComponent(i));
                }
                return tmp;
            } else {
                return ucm;
            }
        } catch (Exception err) {
            return null;
        }
    }

    public static final String MODEL = "model";
    public static final String SERIES = "series", LEVEL = "level", SLOPE = "slope", NOISE = "noise", SEASONAL = "seasonal";
    private GeneralizedAirlineMonitor monitor_;
    private final InformationSet info_ = new InformationSet();
    private final TsData y_, t_, sa_, s_, i_;
    private final UcarimaModel ucm_;
    private WienerKolmogorovEstimators wk_;
    private boolean mul_;
    private final RegArimaEstimation<GeneralizedAirlineModel> best_;

    public List<String> getTsDataDictionary() {
        return info_.getDictionary(TsData.class);
    }

    public TsData getTsData(String id) {
        return info_.search(id, TsData.class);
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
    public ISeriesDecomposition getSeriesDecomposition() {
        DefaultSeriesDecomposition decomposition
                = new DefaultSeriesDecomposition(mul_ ? DecompositionMode.Multiplicative : DecompositionMode.Additive);
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
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        synchronized (mapper) {
            mapper.fillDictionary(null, map);
            return map;
        }
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        if (mapper.contains(id)) {
            return mapper.getData(this, id, tclass);
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
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    public UcarimaModel getUcarimaModel() {
        return ucm_;
    }

    public WienerKolmogorovEstimators getWienerKolmogorovEstimators() {
        if (ucm_ == null) {
            return null;
        }
        if (wk_ == null) {
            wk_ = new WienerKolmogorovEstimators(ucm_);
        }
        return wk_;

    }

    public int getResultsCount() {
        return monitor_.getResultsCount();
    }

    public RegArimaEstimation<GeneralizedAirlineModel> getResult(int idx) {
        return monitor_.result(idx);
    }

    public RegArimaEstimation<GeneralizedAirlineModel> getBestResult() {
        return monitor_.getBestResult();
    }

    public TsData getResiduals() {
        TsDomain domain = y_.getDomain();
        double[] res = monitor_.getBestResult().likelihood.getResiduals();
        return new TsData(domain.getStart().plus(domain.getLength() - res.length), res, false);
    }

    @Override
    public InformationSet getInformation() {
        return info_;
    }

    // MAPPERS
    public static <T> void addMapping(String name, InformationMapper.Mapper<GeneralizedAirlineResults, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }
    private static final InformationMapper<GeneralizedAirlineResults> mapper = new InformationMapper<>();

    static {
        mapper.add(ModellingDictionary.Y_CMP, new InformationMapper.Mapper<GeneralizedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(GeneralizedAirlineResults source) {
                return source.mul_ ? source.y_.exp() : source.y_;
            }
        });
        mapper.add(ModellingDictionary.T_CMP, new InformationMapper.Mapper<GeneralizedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(GeneralizedAirlineResults source) {
                if (source.t_ == null) {
                    return null;
                }
                return source.mul_ ? source.t_.exp() : source.t_;
            }
        });
        mapper.add(ModellingDictionary.SA_CMP, new InformationMapper.Mapper<GeneralizedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(GeneralizedAirlineResults source) {
                if (source.sa_ == null) {
                    return null;
                }
                return source.mul_ ? source.sa_.exp() : source.sa_;
            }
        });
        mapper.add(ModellingDictionary.S_CMP, new InformationMapper.Mapper<GeneralizedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(GeneralizedAirlineResults source) {
                if (source.s_ == null) {
                    return null;
                }
                return source.mul_ ? source.s_.exp() : source.s_;
            }
        });
        mapper.add(ModellingDictionary.I_CMP, new InformationMapper.Mapper<GeneralizedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(GeneralizedAirlineResults source) {
                if (source.i_ == null) {
                    return null;
                }
                return source.mul_ ? source.i_.exp() : source.i_;
            }
        });
        mapper.add(ModellingDictionary.SI_CMP, new InformationMapper.Mapper<GeneralizedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(GeneralizedAirlineResults source) {
                TsData si = TsData.add(source.s_, source.i_);
                if (si == null) {
                    return null;
                }
                return source.mul_ ? si.exp() : si;
            }
        });
        mapper.add(ModellingDictionary.Y_LIN, new InformationMapper.Mapper<GeneralizedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(GeneralizedAirlineResults source) {
                return source.y_;
            }
        });
        mapper.add(ModellingDictionary.T_LIN, new InformationMapper.Mapper<GeneralizedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(GeneralizedAirlineResults source) {
                return source.t_;
            }
        });
        mapper.add(ModellingDictionary.SA_LIN, new InformationMapper.Mapper<GeneralizedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(GeneralizedAirlineResults source) {
                return source.sa_;
            }
        });
        mapper.add(ModellingDictionary.S_LIN, new InformationMapper.Mapper<GeneralizedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(GeneralizedAirlineResults source) {
                return source.s_;
            }
        });
        mapper.add(ModellingDictionary.I_LIN, new InformationMapper.Mapper<GeneralizedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(GeneralizedAirlineResults source) {
                return source.i_;
            }
        });
        mapper.add("residuals", new InformationMapper.Mapper<GeneralizedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(GeneralizedAirlineResults source) {
                return source.getResiduals();
            }
        });
    }
}
