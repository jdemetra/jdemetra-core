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
package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.arima.estimation.Forecasts;

/**
 *
 * @author pcuser
 */
public class LogForecasts {

    public LogForecasts(Forecasts f) {
        fcasts_ = f;
    }

    public boolean isLogCorrectionEnabled() {
        return logc_;
    }

    public void enableLogCorrection(boolean enable) {
        logc_ = enable;
    }

    public double getForecast(int idx) {
        double f = fcasts_.forecast(idx);
        f = Math.exp(f);
        if (logc_) {
            double ser = fcasts_.forecastStdev(idx);
            f *= Math.exp(ser * ser / 2);
        }
        return f;
    }

    public double[] getForecats() {
        double[] f = fcasts_.getForecasts().clone();
        for (int i = 0; i < f.length; ++i) {
            f[i] = Math.exp(f[i]);
        }
        if (logc_) {
            double[] ef = fcasts_.getForecastStdevs();
            for (int i = 0; i < f.length; ++i) {
                f[i] *= Math.exp(ef[i] * ef[i] / 2);
            }
        }
        return f;
    }

    public double getForecastStdev(int idx) {
        double ser = fcasts_.forecastStdev(idx);
        double m = fcasts_.forecast(idx);
        return expStdev(ser, m);
    }

    public double[] getForecatStdevs() {
        double[] ef = fcasts_.getForecastStdevs().clone();
        double[] m = fcasts_.getForecasts();
        for (int i = 0; i < ef.length; ++i) {
            ef[i] = expStdev(ef[i], m[i]);
        }
        return ef;
    }

    public static double expStdev(double ser, double m) {
        if (ser == 0) {
            return 0;
        }
        double lser = m + 0.5 * ser * ser;
        return Math.exp(lser) * Math.sqrt((Math.exp(ser * ser) - 1));
    }

    /**
     * 
     * @param ser standard deviation (on the logs)
     * @param em mean (after exp transformation)
     * @return 
     */
    public static double expStdev2(double ser, double em) {
        if (ser == 0) {
            return 0;
        }
        double lser = 0.5 * ser * ser;
        return em * Math.exp(lser) * Math.sqrt((Math.exp(ser * ser) - 1));
    }

    public static double expMean(double ser, double m, boolean lcorr) {
        m = Math.exp(m);
        if (lcorr) {
            m *= Math.exp(ser * ser / 2);
        }
        return m;
    }
    private boolean logc_ = false;
    private final Forecasts fcasts_;
}
