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
import ec.tstoolkit.information.InformationMapping;
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
import java.util.function.Function;

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
    public static final String NOISE = "noisecomponent", NOISE_DATA = "noise", IRREGULAR = "irregular", NOISE_LBOUND = "lbound", NOISE_UBOUND = "ubound", RESIDUALS = "residuals";
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
        SsfData data = new SsfData(y_.internalStorage(), null);
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
        MAPPING.fillDictionary(null, map, false);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        if (MAPPING.contains(id)) {
            return MAPPING.getData(this, id, tclass);
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
        if (MAPPING.contains(id)) {
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

    // MAPPING
    public static InformationMapping<MixedAirlineResults> getMapping() {
        return MAPPING;
    }

    public static <T> void setMapping(String name, Class<T> tclass, Function<MixedAirlineResults, T> extractor) {
        MAPPING.set(name, tclass, extractor);
    }

    public static <T> void setTsData(String name, Function<MixedAirlineResults, TsData> extractor) {
        MAPPING.set(name, extractor);
    }

    private static final InformationMapping<MixedAirlineResults> MAPPING = new InformationMapping<>(MixedAirlineResults.class);

    static {
        MAPPING.set(ModellingDictionary.Y_CMP, source -> source.mul_ ? source.y_.exp() : source.y_);
        MAPPING.set(ModellingDictionary.T_CMP, source -> source.mul_ ? source.t_.exp() : source.t_);
        MAPPING.set(ModellingDictionary.SA_CMP, source -> source.mul_ ? source.sa_.exp() : source.sa_);
        MAPPING.set(ModellingDictionary.S_CMP, source -> source.mul_ ? source.s_.exp() : source.s_);
        MAPPING.set(ModellingDictionary.I_CMP, source -> source.mul_ ? source.i_.exp() : source.i_);
        MAPPING.set(ModellingDictionary.Y_LIN, source -> source.y_);
        MAPPING.set(ModellingDictionary.T_LIN, source -> source.t_);
        MAPPING.set(ModellingDictionary.SA_LIN, source -> source.sa_);
        MAPPING.set(ModellingDictionary.S_LIN, source -> source.s_);
        MAPPING.set(ModellingDictionary.I_LIN, source -> source.i_);
        MAPPING.set(ModellingDictionary.SI_CMP, source -> {
            TsData si = TsData.add(source.s_, source.i_);
            if (si == null) {
                return null;
            }
            return source.mul_ ? si.exp() : si;
        });
        MAPPING.set(RESIDUALS, source -> source.getResiduals());
        MAPPING.set(IRREGULAR, source -> source.im_);
        MAPPING.set(NOISE_DATA, source -> source.noise_);
    }
}
