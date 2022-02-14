/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.regsarima.regular;

import demetra.data.DoubleSeq;
import demetra.modelling.implementations.RegSarimaProcessor;
import demetra.arima.SarimaSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.modelling.GeneralLinearModel;

/**
 *
 * @author PALATEJ
 */
public class Forecast {

    private final RegSarimaProcessor kernel;
    private final int nf;
    private RegSarimaModel.Forecasts fcasts;

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
            fcasts = model.forecasts(nf);
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    public DoubleSeq getRawForecasts() {
        return fcasts.getRawForecasts().getValues();
    }

    public DoubleSeq getRawForecastsStdev() {
        return fcasts.getRawForecastsStdev().getValues();
    }

    public DoubleSeq getForecasts() {
        return fcasts.getForecasts().getValues();
    }

    public DoubleSeq getForecastsStdev() {
        return fcasts.getForecastsStdev().getValues();
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
        fcasts=null;
     }

    public final static int MAX_REPEAT_COUNT = 80, MAX_MISSING_COUNT = 33;
}
