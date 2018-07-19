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
package ec.satoolkit.x11;

import ec.satoolkit.IPreprocessingFilter;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.regression.AdditiveOutlier;
import ec.tstoolkit.timeseries.regression.LevelShift;
import ec.tstoolkit.timeseries.regression.SeasonalOutlier;
import ec.tstoolkit.timeseries.regression.TransitoryChange;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 * Extension of the series using information provided by a PreprocessingModel.
 * The actual computation is provided by the PreprocessingModel itself
 *
 * @author Jean Palate
 */
public class DefaultPreprocessor extends DefaultX11Algorithm implements
        IX11Preprocessor {

    private final PreprocessingModel model_;
    private final IPreprocessingFilter filter_;
    private final boolean noapply;

    /**
     * Creates a new default pre-processor
     *
     * @param model The pre-estimated regression model
     * @param filter The filter that will be used to retrieve information from
     * the pre-processing model.
     */
    public DefaultPreprocessor(PreprocessingModel model, IPreprocessingFilter filter, boolean noapply) {
        model_ = model;
        filter_ = filter;
        this.noapply = noapply;
    }

    /**
     *
     * @param s
     * @param info
     */
    @Override
    public void preprocess(InformationSet info) {
        TsData s = filter_.getCorrectedSeries(false);
        TsData fs = filter_.getCorrectedForecasts(false);
        TsData bs = filter_.getCorrectedBackcasts(false);
        TsData sall = s.update(fs);
        if (bs != null) {
            sall = bs.update(sall);
        }
        InformationSet atables = info.subSet(X11Kernel.A);
        InformationSet btables = info.subSet(X11Kernel.B);
        btables.set(X11Kernel.B1, sall);
        if (fs != null) {
            atables.set(X11Kernel.A1a, model_.forecast(fs.getLength(), false));
        }
        if (bs != null) {
            atables.set(X11Kernel.A1b, model_.backcast(bs.getLength(), false));
        }
        if (noapply)
            return;
        // complete the information sets using the pre-processing model
        TsDomain domain = model_.description.getSeriesDomain();
        // extend the domain for forecasts
        int nf = context.getForecastHorizon(), nb = context.getBackcastHorizon(), ny = context.getFrequency();
        domain = domain.extend(nb, nf == 0 ? ny : nf);

        TsData mh = model_.movingHolidaysEffect(domain);
        TsData td = model_.tradingDaysEffect(domain);
        model_.backTransform(td, false, true);
        model_.backTransform(mh, false, false);
        atables.add(X11Kernel.A6, td);
        atables.add(X11Kernel.A7, mh);
        //d.add(X11Kernel.D18, cal);
        TsData p = model_.outliersEffect(domain);
        TsData pt = model_.deterministicEffect(domain, LevelShift.class);
        TsData ps = model_.deterministicEffect(domain, SeasonalOutlier.class);
        TsData pa = model_.deterministicEffect(domain, AdditiveOutlier.class);
        TsData pc = model_.deterministicEffect(domain, TransitoryChange.class);
        TsData ut = model_.userEffect(domain, ComponentType.Trend);
        TsData ua = model_.userEffect(domain, ComponentType.Irregular);
        TsData us = model_.userEffect(domain, ComponentType.Seasonal);
        TsData usa = model_.userEffect(domain, ComponentType.SeasonallyAdjusted);
        TsData uu = model_.userEffect(domain, ComponentType.Undefined);
        TsData user = model_.userEffect(domain, ComponentType.Series);

        pt = TsData.add(pt, ut);
        ps = TsData.add(ps, us);
        pa = TsData.add(pa, ua);
        TsData pi = TsData.add(pa, pc);
        TsData pall = TsData.add(pt, TsData.add(ps, pi));
        TsData u = TsData.add(usa, user);
        model_.backTransform(p, false, false);
        model_.backTransform(pt, false, false);
        model_.backTransform(ps, false, false);
        model_.backTransform(pa, false, false);
        model_.backTransform(pc, false, false);
        model_.backTransform(pi, false, false);
        model_.backTransform(pall, false, false);
        model_.backTransform(usa, false, false);
        model_.backTransform(uu, false, false);
        model_.backTransform(user, false, false);
        model_.backTransform(u, false, false);

        atables.add(X11Kernel.A8t, pt);
        atables.add(X11Kernel.A8s, ps);
        atables.add(X11Kernel.A8i, pi);
        atables.add(X11Kernel.A8, pall);
        atables.add(X11Kernel.A9, u);
        atables.add(X11Kernel.A9sa, usa);
        atables.add(X11Kernel.A9u, uu);
        atables.add(X11Kernel.A9ser, user);
    }
}
