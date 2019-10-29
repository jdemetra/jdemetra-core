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
package ec.tstoolkit.modelling;

import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
public class DifferencingResults {

    private static boolean checkStationarity(double[] data, int ifreq) {
        if (ifreq <= 4) {
            double var = DescriptiveStatistics.cov(0, data);
            for (int i = 0; i < ifreq; ++i) {
                if (DescriptiveStatistics.cov(i + 1, data) / var <= 0.2) {
                    return true;
                }
            }
        } else {
            if (DescriptiveStatistics.cov(ifreq, data) <= 0) {
                return true;
            }
            for (int i = 0; i < 4; ++i) {
                if (DescriptiveStatistics.cov(i + 1, data) <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static DifferencingResults create(final TsData input, final int delta, final boolean mean) {
        int ifreq = input.getFrequency().intValue();
        if (delta < 0) {
            TsData del = diff(input, 1, true);
            if (! checkStationarity(del.internalStorage(), ifreq))
                del=diff(del, 1, true);
            return new DifferencingResults(input, del, true);
        } else {
            return new DifferencingResults(input, diff(input, delta, mean), mean);
        }
    }

    private static TsData diff(TsData s, int delta, boolean mean) {
        TsData del = s.delta(1, delta);
        if (mean) {
            DescriptiveStatistics stats = new DescriptiveStatistics(del);
            del.apply(x -> x - stats.getAverage());
        }
        return del;
    }

    public DifferencingResults(TsData orig, TsData diff, boolean mean) {
        original = orig;
        differenced = diff;
        this.mean = mean;
    }

    public int getDifferencingOrder() {
        return original.getLength() - differenced.getLength();
    }

    public TsData getRestrictedOriginal() {
        return original.fittoDomain(differenced.getDomain());
    }

    private final TsData original;
    private final TsData differenced;
    private final boolean mean;

    /**
     * @return the original
     */
    public TsData getOriginal() {
        return original;
    }

    /**
     * @return the differenced
     */
    public TsData getDifferenced() {
        return differenced;
    }

    /**
     * @return the mean
     */
    public boolean isMean() {
        return mean;
    }
}
