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
package ec.tstoolkit.data;

import ec.tstoolkit.design.NewObject;
import ec.tstoolkit.dstats.Normal;
import ec.tstoolkit.dstats.TestType;
import ec.tstoolkit.stats.StatisticalTest;

/**
 *
 * @author Jean Palate
 */
public class BlackmanTukeySpectrum {

    private static final int MIN_CORR = 1;
    private double[] data_, cov_, spect_;
    private WindowType win_ = WindowType.Tukey;
    private int winLen_ = 44;
    private ITaper taper_;

    public BlackmanTukeySpectrum() {
    }

    private boolean calc() {
        spect_ = null;
        if (data_ == null || winLen_ < 0 || winLen_ >= data_.length) {
            return false;
        }
        if (!meanCorrection()) {
            return false;
        }
        if (!taping()) {
            return false;
        }
        computeCov();
        computeSpectrum();
        return true;
    }

    public void setData(double[] data) {
        data_ = data.clone();
        clear();
    }

    public double[] getData() {
        return data_;
    }

    public int getWindowLength() {
        return winLen_;
    }

    public void setWindowLength(int wlen) {
        winLen_ = wlen;
        spect_ = null;
    }

    public ITaper getTaper() {
        return taper_;
    }

    public void setTaper(ITaper taper) {
        taper_ = taper;
        clear();
    }

    public WindowType getWindowType() {
        return win_;
    }

    public void setWindowType(WindowType wtype) {
        win_ = wtype;
        spect_ = null;
    }

    public double getAutoCovariances(int lag) {
        computeCov();
        return cov_[lag];
    }

    public double[] getSpectrum() {
        if (spect_ == null) {
            calc();
        }
        return spect_;
    }

    public double getSpectrumValue(double freq) {
        if (spect_ == null && !calc()) {
            return Double.NaN;
        }

        int ipos = (int) Math.round(freq * winLen_ / (2 * Math.PI));
        if (ipos == spect_.length) {
            ipos = spect_.length - 1;
        }
        if (ipos < 0 || ipos >= spect_.length) {
            return Double.NaN;
        } else {
            return spect_[ipos];
        }
    }

    public double getAverageSpectrum(double[] freqs) {
        if (spect_ == null && !calc()) {
            return Double.NaN;
        }
        double w2 = winLen_ / 2;
        double s = 0;
        for (int i = 0; i < freqs.length; ++i) {
            int ipos = -1 + (int) Math.round(freqs[i] * w2 / Math.PI);
            if (ipos >= spect_.length) {
                ipos = spect_.length - 1;
            } else if (ipos < 0) {
                ipos = 0;
            }
            s += spect_[ipos];
        }
        return s / freqs.length;
    }

    public StatisticalTest getAverageSpectrumTest(int ifreq) {
        // var corr = 1/n
        // var spec = 4(m / 2 - 1)/n = 2(m - 2) / n
        // var mu = 2(m - 2) / n /f / 2 = 4( m -2) / (f n)
        double e = 4 * (winLen_ - 2.0) / (data_.length * ifreq);
        Normal N = new Normal();
        N.setMean(1);
        N.setStdev(Math.sqrt(e));
        double val = this.getAverageSpectrum(Periodogram.getSeasonalFrequencies(ifreq));
        return new StatisticalTest(N, val, TestType.Upper, true);
    }

    @NewObject
    public double[] window() {
        return win_.window(winLen_);
    }

    public void computeSpectrum() {
        double[] cwnd = window();
        int nspect_ = 1 + winLen_ / 2;
        spect_ = new double[nspect_];
        for (int i = 0; i < winLen_; i++) {
            cwnd[i] *= cov_[i];
        }
        for (int i = 0; i < nspect_; i++) {
            double s = cwnd[0];
            for (int j = 1; j < winLen_; j++) {
                s += 2 * cwnd[j] * Math.cos(Math.PI * 2.0 * i * j / winLen_);
            }
            if (s < 0) {
                s = 0;
            }
            spect_[i] = s / cov_[0];    // to normalize...
        }
    }

    private void clear() {
        cov_ = null;
        spect_ = null;
    }

    private void computeCov() {
        if (data_ == null || winLen_ < 0) {
            return;
        }
        if (cov_ != null && cov_.length > winLen_) {
            return;
        }
        int lstart;
        if (cov_ != null) {
            lstart = cov_.length;
            double[] tmp = new double[winLen_ + 1];
            System.arraycopy(cov_, 0, tmp, 0, lstart);
            cov_ = tmp;
        } else {
            lstart = 0;
            cov_ = new double[winLen_];
        }
        for (int i = lstart; i < winLen_; i++) {
            cov_[i] = DescriptiveStatistics.cov(i, data_);
        }
    }

    private boolean meanCorrection() {
        DescriptiveStatistics ds = new DescriptiveStatistics(data_);
        if (ds.getObservationsCount() < minSize()) {
            return false;
        }
        if (ds.getDataCount() < winLen_ + minCorr()) {
            return false;
        }
        double m = ds.getAverage();
        if (m != 0) {
            for (int i = 0; i < data_.length; ++i) {
                if (Double.isFinite(data_[i])) {
                    data_[i] -= m;
                }
            }
        }
        return true;
    }

    private boolean taping() {
        if (taper_ == null) {
            return true;
        }
        taper_.process(data_);
        return true;
    }

    private int minSize() {
        // TODO
        return 10;
    }

    private int minCorr() {
        // TODO
        return MIN_CORR;
    }

    public boolean isValid() {
        if (spect_ == null) {
            return calc();
        } else {
            return true;
        }
    }
}
