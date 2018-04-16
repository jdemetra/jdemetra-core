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

import ec.satoolkit.DecompositionMode;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.modelling.arima.x13.UscbForecasts;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.sarima.estimation.SarimaMapping;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 * Extension of the series by means of an airline model (without mean). The
 * actual computation is provided by the following modules: - estimation of the
 * model: ec.tstoolkit.sarima.estimation.GlsSarimaMonitor - computation of the
 * forecasts: ec.tstoolkit.modelling.arima.x13.UscbForecasts. See those classes
 * for further information
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
class AirlinePreprocessor extends DefaultX11Algorithm implements
        IX11Preprocessor {

    /**
     * Creates a new module of pre-processing
     *
     * @param context The current context
     */
    public AirlinePreprocessor() {
    }

    private void addfcasts(TsData s, InformationSet info, int nb, int nf) {
        DataBlock data = new DataBlock(s.internalStorage());
        SarimaSpecification spec = new SarimaSpecification(context.getFrequency());
        spec.airline();
        RegArimaModel<SarimaModel> regarima = new RegArimaModel<>(
                new SarimaModel(spec), data);
        GlsSarimaMonitor monitor = new GlsSarimaMonitor();
        monitor.setMinimizer(new ProxyMinimizer(new LevenbergMarquardtMethod()));
        monitor.setPrecision(1e-7);
        SarimaModel model = monitor.process(regarima).model.getArima();
        SarimaMapping.stabilize(model);

        // FastArimaForecasts fcast = new FastArimaForecasts(model, false);
        UscbForecasts fcast = new UscbForecasts(model);
        TsData xs = s;
        if (nf > 0) {
            double[] forecasts = fcast.forecasts(data, nf);
            TsData fs = new TsData(s.getEnd(), forecasts, false);
            xs = s.update(fs);
            info.subSet(X11Kernel.A).set(X11Kernel.A1a, fs);
        }
        if (nb > 0) {
            double[] backcasts = fcast.forecasts(data.reverse(), nb);
            ec.tstoolkit.utilities.Arrays2.reverse(backcasts);
            TsData bs = new TsData(s.getStart().minus(backcasts.length), backcasts, false);
            xs = bs.update(xs);
        }
        info.subSet(X11Kernel.B).set(X11Kernel.B1, xs);
    }

    private void mulfcasts(TsData s, InformationSet info, int nb, int nf) {
        TsData ls = s.log();
        TsData xs = s;
        DataBlock data = new DataBlock(ls.internalStorage());
        SarimaSpecification spec = new SarimaSpecification(context.getFrequency());
        spec.airline();
        RegArimaModel<SarimaModel> regarima = new RegArimaModel<>(
                new SarimaModel(spec), data);
        GlsSarimaMonitor monitor = new GlsSarimaMonitor();
        monitor.setMinimizer(new ProxyMinimizer(new LevenbergMarquardtMethod()));
        monitor.setPrecision(1e-7);
        SarimaModel model = monitor.process(regarima).model.getArima();
        SarimaMapping.stabilize(model);
        // FastArimaForecasts fcast = new FastArimaForecasts(model, false);
        UscbForecasts fcast = new UscbForecasts(model);
        TsData fs = null;
        if (nf > 0) {
            double[] forecasts = fcast.forecasts(data, nf);
            fs = new TsData(s.getEnd(), forecasts, false);
            fs = fs.exp();
            xs = s.update(fs);
            info.subSet(X11Kernel.A).set(X11Kernel.A1a, fs);
        }
        if (nb > 0) {
            double[] backcasts = fcast.forecasts(data.reverse(), nb);
            ec.tstoolkit.utilities.Arrays2.reverse(backcasts);
            TsData bs = new TsData(s.getStart().minus(backcasts.length), backcasts, false);
            xs = bs.exp().update(xs);
        }
        info.subSet(X11Kernel.B).set(X11Kernel.B1, xs);
    }

    /**
     * Starting from the series a1 (in a-tables), the preprocessing will
     * complete the series a1a (in a-tables)(if forecasts) and b1 (in b-tables).
     *
     * @param info The information set that contains the input (a1) as well as
     * the output ([a1a], b1)
     */
    @Override
    public void preprocess(InformationSet info) {
        TsData a1 = info.subSet(X11Kernel.A).get(X11Kernel.A1, TsData.class);
        if (context.isPseudoAdditive()) {
            // Can't use preprocessing
            info.subSet(X11Kernel.B).set(X11Kernel.B1, a1);
        } else {
            int nf = context.getForecastHorizon();
            int nb = context.getBackcastHorizon();
            if (nf == 0 && nb == 0) {
                info.subSet(X11Kernel.B).set(X11Kernel.B1, a1);
            } else if (context.isMultiplicative()){
                mulfcasts(a1, info, nb, nf);
            } else {
                addfcasts(a1, info, nb, nf);
            }
        }
    }
}
