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

package demetra.modelling;

import demetra.data.Doubles;
import demetra.stats.AutoCovariances;
import demetra.timeseries.TsData;
import demetra.timeseries.simplets.TsDataToolkit;
import java.util.function.IntToDoubleFunction;
import demetra.data.DoubleSeq;


/**
 *
 * @author Jean Palate
 */
public class DifferencingResults{

    private static int searchOrder(DoubleSeq data, int period) {
        IntToDoubleFunction cov=AutoCovariances.autoCovarianceFunction(data, 0);
        if (period < 6) {
            for (int i = 1; i < period; ++i) {
                if (cov.applyAsDouble(i) < 0) {
                    return 1;
                }
            }
        }
        else {
            for (int i = 1; i <= 4; ++i) {
                if (cov.applyAsDouble(i) < 0) {
                    return 1;
                }
            }
        }
        if (cov.applyAsDouble(period) < 0) {
            return 1;
        }
        else {
            return 2;
        }
    }

    /**
     * Compute differences
     * @param input The series being differenced
     * @param period Periodicity of the data
     * @param delta The differencing order. If delta is negative, the routine 
     * searches the differencing order, based on the covariances at several lags.
     * @param mean The series must be corrected for mean (not used if the differencing
     * order is automatically identified)
     * @return 
     */
    public static DifferencingResults of(DoubleSeq input, int period, int delta, boolean mean) {
        DoubleSeq diff;
        int del;
        boolean bmean;
        if (delta < 0) {
            del = searchOrder(input, period);
            bmean = del != 2;
        }
        else {
            del = delta;
            bmean = mean;
        }

        diff = Doubles.delta(input, 1, del);
        if (bmean) {
            diff=Doubles.removeMean(diff);
        }
        return new DifferencingResults(input, diff, bmean);
    }

    private final DoubleSeq original;
    private final DoubleSeq differenced;
    private final boolean mean;

    private DifferencingResults(DoubleSeq orig, DoubleSeq diff, boolean mean) {
        original = orig;
        differenced = diff;
        this.mean = mean;
    }

    public int getDifferencingOrder() {
        return original.length() - differenced.length();
    }
    
    public DoubleSeq getRestrictedOriginal(){
        return original.drop(getDifferencingOrder(), 0);
    }
    
    /**
     * @return the original
     */
    public DoubleSeq getOriginal() {
        return original;
    }

    /**
     * @return the differenced
     */
    public DoubleSeq getDifferenced() {
        return differenced;
    }

    /**
     * @return the mean
     */
    public boolean isMean() {
        return mean;
    }
}
