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
public class DifferencingResults{

    private static int searchOrder(double[] data, int ifreq) {
        if (ifreq < 6) {
            for (int i = 0; i < ifreq; ++i) {
                if (DescriptiveStatistics.cov(i + 1, data) < 0) {
                    return 1;
                }
            }
        }
        else {
            for (int i = 0; i < 4; ++i) {
                if (DescriptiveStatistics.cov(i + 1, data) < 0) {
                    return 1;
                }
            }
        }
        if (DescriptiveStatistics.cov(ifreq, data) < 0) {
            return 1;
        }
        else {
            return 2;
        }
    }

    public static DifferencingResults create(TsData input, int delta, boolean mean) {
        TsData diff;
        int del;
        boolean bmean;
        int ifreq = input.getFrequency().intValue();
        if (delta < 0) {
            del = searchOrder(input.internalStorage(), ifreq);
            bmean = del != 2;
        }
        else {
            del = delta;
            bmean = mean;
        }

        diff = input.delta(1, del);
        if (bmean) {
            DescriptiveStatistics stats = new DescriptiveStatistics(diff);
            diff.apply(x->x-stats.getAverage());
        }
        return new DifferencingResults(input, diff, bmean);
    }

    public DifferencingResults(TsData orig, TsData diff, boolean mean) {
        original = orig;
        differenced = diff;
        this.mean = mean;
    }

    public int getDifferencingOrder() {
        return original.getLength() - differenced.getLength();
    }
    
    public TsData getRestrictedOriginal(){
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
