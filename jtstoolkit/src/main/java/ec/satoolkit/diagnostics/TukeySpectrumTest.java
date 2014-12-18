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

package ec.satoolkit.diagnostics;

import ec.tstoolkit.data.BlackmanTukeySpectrum;
import ec.tstoolkit.data.ITaper;
import ec.tstoolkit.data.Periodogram;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author pcuser
 */
public class TukeySpectrumTest {

    private final BlackmanTukeySpectrum tukey = new BlackmanTukeySpectrum();
    private int winlen_;
    private double[] speaks;

    public TukeySpectrumTest() {
    }

    public void setTaper(ITaper taper) {
        tukey.setTaper(taper);
    }

    /**
     * @return the winlen_
     */
    public int getWindowLength() {
        return winlen_;
    }

    /**
     * @param winlen_ the winlen_ to set
     */
    public void setWindowLength(int winlen_) {
        this.winlen_ = winlen_;
    }

    public boolean test(TsData s) {
        clear();
        tukey.setData(s.getValues().internalStorage());
        int l;
        int freq = s.getFrequency().intValue();
        if (winlen_ == 0) {
            int ndata = s.getLength();
            l = Math.min((ndata * 3 / 4 / freq) * freq, freq * 10);
        }
        else {
            l = winlen_;
        }
        tukey.setWindowLength(l);
        if (!tukey.isValid()) {
            return false;
        }
        // compute statistics;
        computeStatistics(freq);
        return true;
    }

    private void computeStatistics(int freq) {
        double[] sfreqs = Periodogram.getSeasonalFrequencies(freq);
        speaks = new double[sfreqs.length];
        double w2 = tukey.getWindowLength() / 2;
        double[] sp = tukey.getSpectrum();
        for (int i = 0; i < sfreqs.length; ++i) {
            int ipos = -1 + (int) Math.round(sfreqs[i] * w2 / Math.PI);
            if (ipos >= sp.length) {
                ipos = sp.length - 1;
            }
            else if (ipos < 0) {
                ipos = 0;
            }
            speaks[i] = sp[ipos];
        }
    }

    public double averagePeak() {
        if (speaks == null) {
            return Double.NaN;
        }
        double s = 0;
        for (int i = 0; i < speaks.length; ++i) {
            s += speaks[i];
        }
        return s / speaks.length;
    }

    public boolean hasSignificantSeasonalPeaks() {
        if (speaks == null) {
            return false;
        }
        return averagePeak() > threshold();
    }

    private double threshold() {
        if (speaks.length <= 2) {
            return 2.8; // 0.001
        }
        else {
            return 2.1; // 0.001
        }
    }

    private void clear() {
        speaks = null;
    }
}
