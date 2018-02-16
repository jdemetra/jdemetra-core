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
package ec.satoolkit.seats;

import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.Utilities;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.tstoolkit.ucarima.estimation.BurmanEstimatesC;
import ec.tstoolkit.utilities.Ref;
import java.util.ArrayList;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class WienerKolmogorovEstimator implements IComponentsEstimator {

    private final int npred;
    
    private int nf(TsFrequency freq){
        if (npred>=0)
            return npred;
        else
            return freq.intValue()*(-npred);
    }
    
    public WienerKolmogorovEstimator(int npred){
        this.npred=npred;
    }
    /**
     *
     * @param model
     * @param ucm
     * @param info
     * @param context
     * @return
     */
    @Override
    public DefaultSeriesDecomposition decompose(SeatsModel model, UcarimaModel ucm,
            InformationSet info, SeatsContext context) {
        DefaultSeriesDecomposition decomposition = new DefaultSeriesDecomposition(
                DecompositionMode.Additive);
        BurmanEstimatesC burman = new BurmanEstimatesC();
//	BurmanEstimates burman = new BurmanEstimates();
        UcarimaModel ucmc = ucm.clone();
        ucmc.compact(2, 2);

        TsData s = model.getSeries();
        int nf = nf(s.getFrequency());
        TsDomain fdomain = new TsDomain(s.getEnd(), nf);
        burman.setForecastsCount(nf);

        // check the ucarima model. 
        // ucm=checkModel(ucm);
        if (model.isMeanCorrection()) {
            burman.setUcarimaModelWithMean(ucmc);
        } else {
            burman.setUcarimaModel(ucmc);
        }
        burman.setData(s);
        burman.setSer(model.getSer());
        int ncmps = ucmc.getComponentsCount();

        TsData[] cmps = new TsData[ncmps];
        TsData[] fcmps = new TsData[ncmps];
        TsData[] ecmps = new TsData[ncmps];
        TsData[] efcmps = new TsData[ncmps];

        for (int i = 0; i < ncmps; ++i) {
            if (i == 0 || !ucmc.getComponent(i).isNull()) {
                double[] tmp = burman.estimates(i, true);
                cmps[i] = new TsData(s.getStart(), tmp, false);
                ecmps[i] = new TsData(s.getStart(), burman.stdevEstimates(i), false);
                tmp = burman.forecasts(i, true);
                if (tmp != null) {
                    fcmps[i] = new TsData(s.getEnd(), tmp, false);
                }
                tmp = burman.stdevForecasts(i, true);
                if (tmp != null) {
                    efcmps[i] = new TsData(s.getEnd(), tmp, false);
                }
            }
        }

        TsData fs = new TsData(s.getEnd(), burman.getSeriesForecasts(), false);
        TsData efs = null, efsa = null;
        for (int i = 0; i < efcmps.length; ++i) {
            if (efcmps[i] != null) {
                TsData var = efcmps[i].times(efcmps[i]);
                efs = TsData.add(efs, var);
                if (i != 1) {
                    efsa = TsData.add(efsa, var);
                }

            }
        }

        decomposition.add(s, ComponentType.Series);
        decomposition.add(fs.fittoDomain(fdomain), ComponentType.Series, ComponentInformation.Forecast);
        if (efs != null) {
            decomposition.add(efs.fittoDomain(fdomain).sqrt(), ComponentType.Series, ComponentInformation.StdevForecast);
        }
        if (cmps[0] != null) {
            decomposition.add(cmps[0], ComponentType.Trend);
        }
        if (cmps[1] != null) {
            decomposition.add(cmps[1], ComponentType.Seasonal);
        }

        if (fcmps[0] != null) {
            decomposition.add(fcmps[0].fittoDomain(fdomain), ComponentType.Trend, ComponentInformation.Forecast);
        }
        if (fcmps[1] != null) {
            decomposition.add(fcmps[1].fittoDomain(fdomain), ComponentType.Seasonal, ComponentInformation.Forecast);
        }
        decomposition.add(TsData.subtract(fs, fcmps[1]),
                ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
        if (ecmps[0] != null) {
            decomposition.add(ecmps[0], ComponentType.Trend, ComponentInformation.Stdev);
        }
        if (efcmps[0] != null) {
            decomposition.add(efcmps[0], ComponentType.Trend, ComponentInformation.StdevForecast);
        }
        decomposition.add(TsData.subtract(s, cmps[1]),
                ComponentType.SeasonallyAdjusted);
        if (ecmps[1] != null) {
            decomposition.add(ecmps[1], ComponentType.Seasonal, ComponentInformation.Stdev);
            decomposition.add(ecmps[1], ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev);
            decomposition.add(efsa.sqrt(), ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
        }
        if (efcmps[1] != null) {
            decomposition.add(efcmps[1], ComponentType.Seasonal, ComponentInformation.StdevForecast);
        }
        decomposition.add(cmps[2], ComponentType.Irregular);
        if (fcmps[2] != null) {
            decomposition.add(fcmps[2], ComponentType.Irregular, ComponentInformation.Forecast);
        }
        if (ecmps[2] != null) {
            decomposition.add(ecmps[2], ComponentType.Irregular, ComponentInformation.Stdev);
        }
        if (efcmps[2] != null) {
            decomposition.add(efcmps[2], ComponentType.Irregular, ComponentInformation.StdevForecast);
        }

        return decomposition;
    }

    private UcarimaModel checkModel(UcarimaModel ucm) {
        // if the components have common UR in ar and ma, we must slightly change ma
        ArrayList<ArimaModel> ncmps = new ArrayList<>();
        boolean changed = false;
        for (int i = 0; i < ucm.getComponentsCount(); ++i) {
            ArimaModel cur = ucm.getComponent(i);
            if (!cur.isNull()) {
                BackFilter.SimplifyingTool smp = new BackFilter.SimplifyingTool(true);
                if (smp.simplify(cur.getNonStationaryAR(), cur.getMA())) {
                    Polynomial ma = cur.getMA().getPolynomial();
                    Ref<Polynomial> rslt = new Ref<>(ma);
                    if (Utilities.stabilize(ma, .97, rslt)) {
                        changed = true;
                    }
                    cur = new ArimaModel(cur.getStationaryAR(), cur.getNonStationaryAR(), BackFilter.of(rslt.val.getCoefficients()), cur.getInnovationVariance());

                }
                ncmps.add(cur);
            }
        }
        if (!changed) {
            return ucm;
        } else {
            return new UcarimaModel(ucm.getModel(), ncmps);
        }
    }
}
