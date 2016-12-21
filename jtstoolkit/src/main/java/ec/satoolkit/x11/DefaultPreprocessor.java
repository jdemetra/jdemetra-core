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

    PreprocessingModel model_;
    IPreprocessingFilter filter_;

    /**
     * Creates a new default pre-processor
     *
     * @param model The pre-estimated regression model
     * @param filter The filter that will be used to retrieve information from
     * the pre-processing model.
     */
    public DefaultPreprocessor(PreprocessingModel model, IPreprocessingFilter filter) {
        model_ = model;
        filter_ = filter;
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
        TsData sall = s.update(fs);
        InformationSet atables = info.subSet(X11Kernel.A);
        InformationSet btables = info.subSet(X11Kernel.B);
        btables.set(X11Kernel.B1, sall);
        if (fs != null) {
            atables.set(X11Kernel.A1a, model_.forecast(fs.getLength(), false));
        }
        // complete the information sets using the pre-processing model
        TsDomain domain = model_.description.getSeriesDomain();
        // extend the domain for forecasts
        int nf = context.getForecastHorizon(), ny = context.getFrequency();
        domain = domain.extend(0, nf == 0 ? ny : nf);

        TsData mh = model_.movingHolidaysEffect(domain);
        TsData td = model_.tradingDaysEffect(domain);
        model_.backTransform(td, false, true);
        model_.backTransform(mh, false, false);
        atables.add(X11Kernel.A6, td);
        atables.add(X11Kernel.A7, mh);
        //d.add(X11Kernel.D18, cal);
        TsData p = model_.outliersEffect(domain);
        TsData pt = model_.regressionEffect(domain, LevelShift.class);
        TsData ps = model_.regressionEffect(domain, SeasonalOutlier.class);
        TsData pa = model_.regressionEffect(domain, AdditiveOutlier.class);
        TsData pc = model_.regressionEffect(domain, TransitoryChange.class);
        TsData ut = model_.userEffect(domain, ComponentType.Trend);
        TsData ua = model_.userEffect(domain, ComponentType.Irregular);
        TsData us = model_.userEffect(domain, ComponentType.Seasonal);
        TsData usa = model_.userEffect(domain, ComponentType.SeasonallyAdjusted);
        TsData uu = model_.userEffect(domain, ComponentType.Undefined);
        TsData user = model_.userEffect(domain, ComponentType.Series);

        pt = TsData.add(pt, ut);
        ps = TsData.add(ps, us);
        pa = TsData.add(pa, ua);
        model_.backTransform(p, false, false);
        model_.backTransform(pt, false, false);
        model_.backTransform(ps, false, false);
        model_.backTransform(pa, false, false);
        model_.backTransform(pc, false, false);
        model_.backTransform(usa, false, false);
        model_.backTransform(uu, false, false);
        model_.backTransform(user, false, false);

        atables.add(X11Kernel.A8t, pt);
        atables.add(X11Kernel.A8s, ps);
        atables.add(X11Kernel.A8i, invOp(pa, pc));
        atables.add(X11Kernel.A8, invOp(invOp(pt, invOp(pa, pc)),ps));
        atables.add(X11Kernel.A9, invOp(usa, user));
        atables.add(X11Kernel.A9sa, usa);
        atables.add(X11Kernel.A9u, uu);
        atables.add(X11Kernel.A9ser, user);
    }
}
