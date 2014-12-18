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
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.ssf.*;
import ec.tstoolkit.ssf.ucarima.SsfUcarima;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.ucarima.UcarimaModel;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class KalmanEstimator implements IComponentsEstimator {

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
        TsData s = model.getSeries();
        int n = s.getLength(), nf = s.getFrequency().intValue();
        TsDomain sdomain = s.getDomain(), fdomain = new TsDomain(s.getEnd(), nf);
        boolean hast = !ucm.getComponent(0).isNull()
                || model.isMeanCorrection();
        boolean hass = !ucm.getComponent(1).isNull();
        boolean hastr = !ucm.getComponent(2).isNull();
        boolean hasirr = hastr || !ucm.getComponent(3).isNull();
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

        // add forecasts
        ec.tstoolkit.ssf.SsfData sdata = new ec.tstoolkit.ssf.SsfData(s.getValues().internalStorage(), null);
        ec.tstoolkit.ssf.ExtendedSsfData xsdata = new ec.tstoolkit.ssf.ExtendedSsfData(sdata);
        xsdata.setForecastsCount(nf);

        SsfUcarima ssf = new SsfUcarima(ucm);
        // compute KS
//	DisturbanceSmoother smoother = new DisturbanceSmoother();
//	Smoother smoother = new Smoother();
//	smoother.setSsf(ssf);
//	Filter<SsfUcarima> filter = new Filter<SsfUcarima>();
//	filter.setSsf(ssf);
//	DiffuseFilteringResults frslts = new DiffuseFilteringResults(true);
//	filter.process(xsdata , frslts);

//	smoother.process(xsdata, frslts);
//	ec.tstoolkit.ssf.SmoothingResults srslts = smoother
//		.calcSmoothedStates();
        ec.tstoolkit.ssf.Smoother smoother = new ec.tstoolkit.ssf.Smoother();
        smoother.setSsf(ssf);
        smoother.setCalcVar(true);

        ec.tstoolkit.ssf.SmoothingResults srslts
                = new ec.tstoolkit.ssf.SmoothingResults();

        smoother.process(xsdata, srslts);
        // for using the same standard error (unbiased stdandard error, not ml)
        srslts.setStandardError(model.getSer());

        TsData[] cmps = new TsData[ucm.getComponentsCount()];
        TsData[] fcmps = new TsData[ucm.getComponentsCount()];
        TsData[] ecmps = new TsData[ucm.getComponentsCount()];
        TsData[] efcmps = new TsData[ucm.getComponentsCount()];
        for (int i = 0; i < ucm.getComponentsCount(); ++i) {
            double[] tmp = srslts.component(ssf.cmpPos(i));
            TsData cur = new TsData(s.getStart(), tmp, false);
            cmps[i] = cur.fittoDomain(sdomain);
            fcmps[i] = cur.fittoDomain(fdomain);
            double[] etmp = srslts.componentStdev(ssf.cmpPos(i));
            TsData ecur = new TsData(s.getStart(), etmp, false);
            ecmps[i] = ecur.fittoDomain(sdomain);
            efcmps[i] = ecur.fittoDomain(fdomain);
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
        if (hasirr) {
            decomposition.add(cmps[cur], ComponentType.Irregular);
            decomposition.add(fcmps[cur], ComponentType.Irregular, ComponentInformation.Forecast);
            decomposition.add(ecmps[cur], ComponentType.Irregular, ComponentInformation.Stdev);
            decomposition.add(efcmps[cur], ComponentType.Irregular, ComponentInformation.StdevForecast);
        }
        // computes the forecasts of the series
        DataBlock z = new DataBlock(ssf.getStateDim());
        ssf.Z(0, z);
        double[] f = srslts.zcomponent(z);
        double[] ef = srslts.zcomponent(z);
        TsData sf = new TsData(fdomain), sef = new TsData(fdomain);
        for (int i = 0; i < fdomain.getLength(); ++i) {
            sf.set(i, f[n + i]);
            sef.set(i, ef[n + i]);
        }
        decomposition.add(sf, ComponentType.Series, ComponentInformation.Forecast);
        decomposition.add(sef, ComponentType.Series, ComponentInformation.StdevForecast);
        return decomposition;
    }
}
