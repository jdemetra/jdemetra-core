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


package ec.tstoolkit.timeseries.analysis;

import ec.tstoolkit.arima.Spectrum;
import ec.tstoolkit.data.AutoRegressiveSpectrum;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.data.Periodogram;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Kristof Bayens
 */
public class SpectralDiagnostic {

    private double[] svals_, freqs_;
    private int[] seasfreq_, tdfreq_;
    private int nar_ = 30;
    private int npoints_ = 60;
    private double sensibility_ = 6.0 / 52;
    private double srange_, median_;

    public boolean test(TsData ts)
    {
        try {
            AutoRegressiveSpectrum ars = new AutoRegressiveSpectrum(AutoRegressiveSpectrum.Method.Ols);

            int freq = ts.getFrequency().intValue();
            double[] tdfreq = Periodogram.getTradingDaysFrequencies(freq);
            int nsf = freq >= 4 ? (freq - 1) / 2 : freq / 2;
            seasfreq_ = new int[nsf];
            int iseas = 2 * npoints_ / freq;
            for (int i = 0; i < seasfreq_.length; ++i)
                seasfreq_[i] = iseas * (i + 1);
            freqs_ = new double[npoints_ + 1];
            svals_ = new double[npoints_ + 1];
            double fstep = Math.PI / npoints_;
            for (int i = 0; i <= npoints_; ++i)
                freqs_[i] = fstep * i;
            // replace td freq, if any
            if (tdfreq != null)
            {
                tdfreq_ = new int[tdfreq.length];
                // not optimized
                for (int i = 0; i < tdfreq.length; ++i)
                {
                    for (int j = 0; j < npoints_; ++j)
                        if (tdfreq[i] > freqs_[j] && tdfreq[i] <= freqs_[j + 1])
                        {
                            double d0 = tdfreq[i] - freqs_[j], d1 = freqs_[j + 1] - tdfreq[i];
                            if (d0 < d1) {
                                freqs_[j] = tdfreq[i];
                                tdfreq_[i] = j;
                            }
                            else {
                                freqs_[j + 1] = tdfreq[i];
                                tdfreq_[i] = j + 1;
                            }
                            break;
                        }
                }
            }
             if ( ars.process(ts, nar_)) {
                // build freqs
                for (int i = 0; i < freqs_.length; ++i)
                    svals_[i] = ars.value(freqs_[i]);

                DescriptiveStatistics stats = new DescriptiveStatistics(new DataBlock(svals_).drop(1, 1));
                median_ = stats.getMedian();
                srange_ = stats.getMax() - stats.getMin();
                return true;
            }
            else
                return false;
        }
        catch(Exception ex) {
            return false;
        }
    }

    public int getARLength() {
        return nar_;
    }

    public void setARLength(int value) {
        nar_ = value;
    }

    public double getSensitivity() {
        return sensibility_;
    }

    public void setSensitivity(double value) {
        sensibility_ = value;
    }

    public int getNPoints() {
        return npoints_;
    }

    public void setNPoints(int value) {
        npoints_ = value;
    }

    public double getFrequency(int i) {
        return freqs_[i];
    }

    public double getSpectrumValue(int i) {
        return svals_[i];
    }

    public int[] getSeasonalFrequencies() {
        return seasfreq_;
    }

    public int[] getTradingDaysFrequencies() {
        return tdfreq_;
    }

    public boolean hasTradingDayPeaks() {
        if (tdfreq_ == null)
            return false;
        for (int i = 0; i < tdfreq_.length; ++i) {
            if (isSignificant(tdfreq_[i]))
                return true;
        }
        return false;
    }

    public boolean hasSeasonalPeaks() {
        if (seasfreq_ == null)
            return false;
        for (int i = 0; i < seasfreq_.length; ++i) {
            if (isSignificant(seasfreq_[i]))
                return true;
        }
        return false;
    }

    public boolean isSignificant(int idx) {
        if (svals_[idx] < median_)
            return false;
        if (idx == 0)
            return (svals_[idx] - svals_[idx + 1]) > sensibility_ * srange_;
        else if (idx == svals_.length - 1)
            return (svals_[idx] - svals_[idx - 1]) > sensibility_ * srange_;
        else
            return (svals_[idx] - svals_[idx - 1]) > sensibility_ * srange_
                    && (svals_[idx] - svals_[idx + 1]) > sensibility_ * srange_;
    }
}
