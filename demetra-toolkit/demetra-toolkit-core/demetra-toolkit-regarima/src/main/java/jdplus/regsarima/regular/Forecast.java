/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.regsarima.regular;

import demetra.data.DoubleSeq;
import demetra.likelihood.LikelihoodStatistics;
import demetra.modelling.implementations.RegSarimaProcessor;
import demetra.modelling.implementations.SarimaSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.Variable;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import java.util.Arrays;
import jdplus.dstats.LogNormal;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.regression.Regression;
import jdplus.regarima.RegArimaForecasts;

/**
 *
 * @author PALATEJ
 */
public class Forecast {

    private final RegSarimaProcessor kernel;
    private final int nf;
    private TsData fy, efy;
    private double[] f, ef;

    /**
     * Creates a new module for detecting outliers in the last observations
     *
     * @param kernel
     * @param nf
     */
    public Forecast(RegSarimaProcessor kernel, int nf) {
        this.kernel = kernel;
        this.nf = nf;
    }

    /**
     * Check outliers at the end of a given series
     *
     * @param data The checked series
     * @return true if the series has been successfully processed, false
     * otherwise. The returned value doesn't indicate the presence or not of
     * outliers.
     */
    public boolean process(TsData data) {
        try {
            clear();
            if (!testSeries(data)) {
                return false;
            }
            GeneralLinearModel<SarimaSpec> gmodel = kernel.process(data, null);
            if (gmodel == null || !(gmodel instanceof RegSarimaModel)) {
                return false;
            }
            RegSarimaModel model = (RegSarimaModel) gmodel;
            RegArimaForecasts.Result fcasts;
            DoubleSeq b = model.getEstimation().getCoefficients();
            LikelihoodStatistics ll = model.getEstimation().getStatistics();
            double sig2 = ll.getSsqErr() / (ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount() + 1);
            TsDomain edom = model.getDetails().getEstimationDomain();
            if (b.isEmpty()) {
                fcasts = RegArimaForecasts.calcForecast(model.arima(),
                        model.getEstimation().originalY(), nf, sig2);
            } else {
                Variable[] variables = model.getDescription().getVariables();
                TsDomain xdom = edom.extend(0, nf);
                FastMatrix matrix = Regression.matrix(xdom, Arrays.stream(variables).map(v -> v.getCore()).toArray(n -> new ITsVariable[n]));
                fcasts = RegArimaForecasts.calcForecast(model.arima(),
                        model.getEstimation().originalY(), matrix,
                        b, model.getEstimation().getCoefficientsCovariance(), sig2);
            }
            TsPeriod fstart = edom.getEndPeriod();
            f = fcasts.getForecasts();
            ef = fcasts.getForecastsStdev();

            TsData tf = TsData.ofInternal(fstart, f);
            fy = model.backTransform(tf, true);

            if (model.getDescription().isLogTransformation()) {
                double[] e = new double[nf];
                for (int i = 0; i < nf; ++i) {
                    e[i] = LogNormal.stdev(f[i], ef[i]);
                }
                efy = TsData.ofInternal(fstart, e);
            } else {
                efy = TsData.ofInternal(fstart, ef);
            }

            return true;
        } catch (Exception err) {
            return false;
        }
    }

    public DoubleSeq getRawForecasts() {
        return DoubleSeq.of(f);
    }

    public DoubleSeq getRawForecastsStdev() {
        return DoubleSeq.of(ef);
    }

    public DoubleSeq getForecasts() {
        return fy.getValues();
    }

    public DoubleSeq getForecastsStdev() {
        return efy.getValues();
    }

    public boolean testSeries(final TsData y) {
        if (y == null) {
            return false;
        }
        int nz = y.length();
        int ifreq = y.getAnnualFrequency();
        if (nz < Math.max(8, 3 * ifreq)) {
            return false;
        }
        int nrepeat = y.getValues().getRepeatCount();
        if (nrepeat > MAX_REPEAT_COUNT * nz / 100) {
            return false;
        }
        int nm = y.getValues().count(z -> !Double.isFinite(z));
        if (nm > MAX_MISSING_COUNT * nz / 100) {
            return false;
        }
        return true;
    }

    private void clear() {
        f = null;
        ef = null;
        fy = null;
    }

    public final static int MAX_REPEAT_COUNT = 80, MAX_MISSING_COUNT = 33;
}
