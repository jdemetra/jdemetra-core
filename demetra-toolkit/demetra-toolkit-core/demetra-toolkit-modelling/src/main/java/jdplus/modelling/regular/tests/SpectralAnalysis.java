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
package jdplus.modelling.regular.tests;

import demetra.timeseries.TsData;
import jdplus.data.DataBlock;
import jdplus.data.analysis.AutoRegressiveSpectrum;
import jdplus.data.analysis.Periodogram;
import jdplus.stats.DescriptiveStatistics;
import nbbrd.design.BuilderPattern;

/**
 *
 * @author Kristof Bayens
 */
public class SpectralAnalysis {

    public static Builder test(TsData s) {
        return new Builder(s);
    }

    @BuilderPattern(SpectralAnalysis.class)
    public static class Builder {

        Builder(TsData s) {
            this.s = s;
        }

        private final TsData s;
        private int nar = 30;
        private int npoints = 60;
        private double sensibility = 6.0 / 52;

        public Builder arLength(int nar) {
            this.nar = nar;
            return this;
        }

        public Builder sensibility(double sensibility) {
            this.sensibility = sensibility;
            return this;
        }

        public Builder resolution(int npoints) {
            this.npoints = npoints;
            return this;
        }

        public SpectralAnalysis build() {
            try {
                AutoRegressiveSpectrum ars = new AutoRegressiveSpectrum(AutoRegressiveSpectrum.Method.Ols);

                int freq = s.getAnnualFrequency();
                double[] dtdfreq = Periodogram.getTradingDaysFrequencies(freq);
                int nsf = freq >= 4 ? (freq - 1) / 2 : freq / 2;
                int[] seasfreq = new int[nsf];
                int iseas = 2 * npoints / freq;
                for (int i = 0; i < seasfreq.length; ++i) {
                    seasfreq[i] = iseas * (i + 1);
                }
                double[] freqs = new double[npoints + 1];
                double fstep = Math.PI / npoints;
                for (int i = 0; i <= npoints; ++i) {
                    freqs[i] = fstep * i;
                }
                // replace td freq, if any
                int[] tdfreq = null;
                if (dtdfreq != null) {
                    tdfreq = new int[dtdfreq.length];
                    // not optimized
                    for (int i = 0; i < tdfreq.length; ++i) {
                        for (int j = 0; j < npoints; ++j) {
                            if (dtdfreq[i] > freqs[j] && dtdfreq[i] <= freqs[j + 1]) {
                                double d0 = dtdfreq[i] - freqs[j], d1 = freqs[j + 1] - dtdfreq[i];
                                if (d0 < d1) {
                                    freqs[j] = dtdfreq[i];
                                    tdfreq[i] = j;
                                } else {
                                    freqs[j + 1] = dtdfreq[i];
                                    tdfreq[i] = j + 1;
                                }
                                break;
                            }
                        }
                    }
                }
                double median = 0, srange = 0;
                double[] svals = null;
                if (ars.process(s.getValues(), nar)) {
                    // build freqs
                    svals = new double[npoints + 1];
                    for (int i = 0; i < freqs.length; ++i) {
                        svals[i] = ars.value(freqs[i]);
                    }
                    DescriptiveStatistics stats = DescriptiveStatistics.of(DataBlock.of(svals).drop(1, 1));
                    median = stats.getMedian();
                    srange = stats.getMax() - stats.getMin();
                }
                SpectralAnalysis rslt = new SpectralAnalysis();
                rslt.svals = svals;
                rslt.median = median;
                rslt.freqs = freqs;
                rslt.seasfreq = seasfreq;
                rslt.tdfreq = tdfreq;
                rslt.threshold = srange*sensibility;
                return rslt;
            } catch (Exception ex) {
                return null;
            }
        }

    }

    private double[] svals, freqs;
    private int[] seasfreq, tdfreq;
    private double threshold, median;
 
    public double getFrequency(int i) {
        return freqs[i];
    }

    public double getSpectrumValue(int i) {
        return svals[i];
    }

    public int[] getSeasonalFrequencies() {
        return seasfreq;
    }

    public int[] getTradingDaysFrequencies() {
        return tdfreq;
    }

    public boolean hasTradingDayPeaks() {
        if (tdfreq == null) {
            return false;
        }
        for (int i = 0; i < tdfreq.length; ++i) {
            if (isSignificant(tdfreq[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSeasonalPeaks() {
        if (seasfreq == null) {
            return false;
        }
        for (int i = 0; i < seasfreq.length; ++i) {
            if (isSignificant(seasfreq[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean isSignificant(int idx) {
        if (svals[idx] < median) {
            return false;
        }
        if (idx == 0) {
            return (svals[idx] - svals[idx + 1]) > threshold;
        } else if (idx == svals.length - 1) {
            return (svals[idx] - svals[idx - 1]) > threshold;
        } else {
            return (svals[idx] - svals[idx - 1]) > threshold
                    && (svals[idx] - svals[idx + 1]) > threshold;
        }
    }
}
