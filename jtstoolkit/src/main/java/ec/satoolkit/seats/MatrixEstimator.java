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

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.tstoolkit.ucarima.estimation.McElroyEstimates;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class MatrixEstimator implements IComponentsEstimator {

    private final int npred;
    
    private int nf(TsFrequency freq){
        if (npred>=0)
            return npred;
        else
            return freq.intValue()*(-npred);
    }
    
    public MatrixEstimator(int npred){
        this.npred=npred;
    }
    /**
     *
     * @param model
     * @param info
     * @param context
     * @return
     */
    @Override
    public DefaultSeriesDecomposition decompose(SeatsModel model, UcarimaModel ucm,
            InformationSet info, SeatsContext context) {
        DefaultSeriesDecomposition decomposition = new DefaultSeriesDecomposition(
                DecompositionMode.Additive);
        TsData s = model.getSeries();
        int nf = nf(s.getFrequency());
        TsPeriod s0 = s.getStart(), f0 = s.getEnd();
        boolean hast = !ucm.getComponent(0).isNull()
                || model.isMeanCorrection();
        boolean hass = !ucm.getComponent(1).isNull();
        boolean hastr = !ucm.getComponent(2).isNull();

        // correction for mean...
        if (model.isMeanCorrection()) {
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
            ucm = tmp;
            //IArimaModel sum=ucm.getModel();
        } else {
            ucm = ucm.clone();
            if (hastr) {
                ucm.compact(2, 2);
            }
        }
        ucm.simplify();
        
        McElroyEstimates mc = new McElroyEstimates();
        mc.setForecastsCount(nf);
        mc.setUcarimaModel(ucm);
        mc.setData(s);
        double ser = model.getSer();
        TsData[] cmps = new TsData[ucm.getComponentsCount()];
        TsData[] ecmps = new TsData[ucm.getComponentsCount()];
        TsData[] fcmps = new TsData[ucm.getComponentsCount()];
        TsData[] efcmps = new TsData[ucm.getComponentsCount()];
        for (int i = 0; i < ucm.getComponentsCount(); ++i) {
            if (!ucm.getComponent(i).isNull()) {
                double[] tmp = mc.getComponent(i);
                cmps[i] = new TsData(s0, tmp, false);
                double[] etmp = mc.stdevEstimates(i);
                ecmps[i] = new TsData(s0, etmp, false);
                double[] ftmp = mc.getForecasts(i);
                fcmps[i] = new TsData(f0, ftmp, false);
                double[] eftmp = mc.stdevForecasts(i);
                efcmps[i] = new TsData(f0, eftmp, false);
                ecmps[i].applyOnFinite(x->x*ser);
                efcmps[i].applyOnFinite(x->x*ser);
            }
        }
        
        int cur = 0;
        decomposition.add(s, ComponentType.Series);
        if (hast) {
            decomposition.add(cmps[cur], ComponentType.Trend);
            decomposition.add(fcmps[cur], ComponentType.Trend, ComponentInformation.Forecast);
            decomposition.add(ecmps[cur], ComponentType.Trend, ComponentInformation.Stdev);
            decomposition.add(efcmps[cur], ComponentType.Trend, ComponentInformation.StdevForecast);
            ++cur;
        }
        if (hass) {
            decomposition.add(cmps[cur], ComponentType.Seasonal);
            decomposition.add(fcmps[cur], ComponentType.Seasonal, ComponentInformation.Forecast);
            decomposition.add(ecmps[cur], ComponentType.Seasonal, ComponentInformation.Stdev);
            decomposition.add(efcmps[cur], ComponentType.Seasonal, ComponentInformation.StdevForecast);
            decomposition.add(TsData.subtract(s, cmps[cur]),
                    ComponentType.SeasonallyAdjusted);
            ++cur;
        } else {
            decomposition.add(s, ComponentType.SeasonallyAdjusted);
        }
        
        decomposition.add(cmps[cur], ComponentType.Irregular);
        decomposition.add(fcmps[cur], ComponentType.Irregular, ComponentInformation.Forecast);
        decomposition.add(ecmps[cur], ComponentType.Irregular, ComponentInformation.Stdev);
        decomposition.add(efcmps[cur], ComponentType.Irregular, ComponentInformation.StdevForecast);
        
        TsData fs = new TsData(f0, mc.getForecasts(), false),
                efs = new TsData(f0, mc.stdevForecasts(), false);
        efs.applyOnFinite(x->x*ser);
        decomposition.add(fs, ComponentType.Series, ComponentInformation.Forecast);
        decomposition.add(efs, ComponentType.Series, ComponentInformation.StdevForecast);
        
        return decomposition;
    }
}
