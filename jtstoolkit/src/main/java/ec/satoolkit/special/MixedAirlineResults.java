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

import ec.tstoolkit.modelling.ComponentInformation;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.ISaResults;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.seats.SeatsResults;
import ec.satoolkit.seats.SeatsToolkit;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.arima.special.MixedAirlineModel;
import ec.tstoolkit.arima.special.MixedAirlineMonitor;
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfData;
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
public class MixedAirlineResults implements ISaResults {

    MixedAirlineResults(TsData y, MixedAirlineMonitor monitor, boolean b) {
        y_ = y;
        mul_ = b;
        this.monitor_ = monitor;
        MixedAirlineMonitor.MixedEstimation rslt = monitor.getBestModel();
        if (rslt != null) {
            computeDecomposition(rslt.model);
        }
    }
    public static final String NOISE = "noisecomponent", NOISE_DATA = "noise", IRREGULAR = "irregular", NOISE_LBOUND = "lbound", NOISE_UBOUND = "ubound";
    private MixedAirlineMonitor monitor_;
    private SmoothingResults srslts_;
    private final InformationSet info_ = new InformationSet();
    private TsData y_, yc_, im_, t_, sa_, s_, noise_, i_, enoise_;
    private UcarimaModel ucm_;
    private WienerKolmogorovEstimators wk_;
    private boolean mul_;

    private void computeDecomposition(MixedAirlineModel model) {
        Smoother smoother = new Smoother();
        ISsf ssf = model.makeSsf();
        smoother.setSsf(ssf);
        smoother.setCalcVar(true);
        SsfData data = new SsfData(y_.getValues().internalStorage(), null);
        srslts_ = new SmoothingResults(true, true);
        smoother.process(data, srslts_);
        noise_ = new TsData(y_.getStart(), srslts_.component(ssf.getStateDim() - 1), false);
        enoise_ = new TsData(y_.getStart(), srslts_.componentStdev(ssf.getStateDim() - 1), false);
        for (int i = 0; i < noise_.getLength(); ++i) {
            if (noise_.get(i) == 0) {
                enoise_.set(i, 0);
            }
        }

        yc_ = TsData.subtract(y_, noise_);
        // use seats
        ec.satoolkit.seats.SeatsKernel kernel = new ec.satoolkit.seats.SeatsKernel();
        ec.satoolkit.seats.SeatsSpecification spec = new ec.satoolkit.seats.SeatsSpecification();
        kernel.setToolkit(SeatsToolkit.create(spec));
        SeatsResults rslts = kernel.process(yc_);
        ucm_ = rslts.getUcarimaModel();

        t_ = rslts.getSeriesDecomposition().getSeries(ComponentType.Trend, ComponentInformation.Value);
        sa_ = rslts.getSeriesDecomposition().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
        s_ = rslts.getSeriesDecomposition().getSeries(ComponentType.Seasonal, ComponentInformation.Value);
        im_ = rslts.getSeriesDecomposition().getSeries(ComponentType.Irregular, ComponentInformation.Value);
        i_ = TsData.add(noise_, im_);
        sa_ = TsData.add(sa_, noise_);
        InformationSet ninfo = info_.subSet(NOISE);
        ninfo.add(NOISE_DATA, noise_);
        ninfo.add(IRREGULAR, im_);
        ninfo.add(NOISE_LBOUND, TsData.subtract(noise_, enoise_));
        ninfo.add(NOISE_UBOUND, TsData.add(noise_, enoise_));
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

    public MixedAirlineMonitor.MixedEstimation getBestModel() {
        return monitor_.getBestModel();
    }

    public int getBestModelPosition() {
        return monitor_.getBestModelPosition();
    }

    public List<MixedAirlineMonitor.MixedEstimation> getAllModels() {
        return monitor_.getAllResults();
    }

    public TsData getResiduals() {
        TsDomain domain = y_.getDomain();
        double[] res = monitor_.getBestModel().ll.getResiduals();
        return new TsData(domain.getStart().plus(domain.getLength() - res.length), res, false);

    }

    public TsData getNoise() {
        return noise_;
    }

    public TsData getNoiseStdev() {
        return enoise_;
    }

    @Override
    public InformationSet getInformation() {
        return info_;
    }

    // MAPPERS
    public static <T> void addMapping(String name, InformationMapper.Mapper<MixedAirlineResults, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }
    private static final InformationMapper<MixedAirlineResults> mapper = new InformationMapper<>();

    static {
        mapper.add(ModellingDictionary.Y_CMP, new InformationMapper.Mapper<MixedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedAirlineResults source) {
                return source.mul_ ? source.y_.exp() : source.y_;
            }
        });
        mapper.add(ModellingDictionary.T_CMP, new InformationMapper.Mapper<MixedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedAirlineResults source) {
                return source.mul_ ? source.t_.exp() : source.t_;
            }
        });
        mapper.add(ModellingDictionary.SA_CMP, new InformationMapper.Mapper<MixedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedAirlineResults source) {
                return source.mul_ ? source.sa_.exp() : source.sa_;
            }
        });
        mapper.add(ModellingDictionary.S_CMP, new InformationMapper.Mapper<MixedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedAirlineResults source) {
                return source.mul_ ? source.s_.exp() : source.s_;
            }
        });
        mapper.add(ModellingDictionary.I_CMP, new InformationMapper.Mapper<MixedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedAirlineResults source) {
                return source.mul_ ? source.i_.exp() : source.i_;
            }
        });
        mapper.add(ModellingDictionary.Y_LIN, new InformationMapper.Mapper<MixedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedAirlineResults source) {
                return source.y_;
            }
        });
        mapper.add(ModellingDictionary.T_LIN, new InformationMapper.Mapper<MixedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedAirlineResults source) {
                return source.t_;
            }
        });
        mapper.add(ModellingDictionary.SA_LIN, new InformationMapper.Mapper<MixedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedAirlineResults source) {
                return source.sa_;
            }
        });
        mapper.add(ModellingDictionary.S_LIN, new InformationMapper.Mapper<MixedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedAirlineResults source) {
                return source.s_;
            }
        });
        mapper.add(ModellingDictionary.I_LIN, new InformationMapper.Mapper<MixedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedAirlineResults source) {
                return source.i_;
            }
        });
        mapper.add(ModellingDictionary.SI_CMP, new InformationMapper.Mapper<MixedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedAirlineResults source) {
                TsData si=TsData.add(source.s_, source.i_);
                if (si == null)
                    return null;
                return source.mul_ ? si.exp() : si;
            }
        });
        mapper.add("residuals", new InformationMapper.Mapper<MixedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedAirlineResults source) {
                return source.getResiduals();
            }
        });
        mapper.add(IRREGULAR, new InformationMapper.Mapper<MixedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedAirlineResults source) {
                return source.im_;
            }
        });
        mapper.add(NOISE_DATA, new InformationMapper.Mapper<MixedAirlineResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedAirlineResults source) {
                return source.noise_;
            }
        });
    }
}
