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
package jdplus.tramo;

import demetra.stats.AutoCovariances;
import java.util.function.IntToDoubleFunction;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class DifferencingResults {

    private static boolean checkStationarity(DoubleSeq data, int period) {
        IntToDoubleFunction cov = AutoCovariances.autoCovarianceFunction(data, 0);
        if (period <= 4) {
            double var = cov.applyAsDouble(0);
            for (int i = 1; i <= period; ++i) {
                if (cov.applyAsDouble(i) / var <= 0.2) {
                    return true;
                }
            }
        } else {
            if (cov.applyAsDouble(period) <= 0) {
                return true;
            }
            for (int i = 1; i <= 4; ++i) {
                if (cov.applyAsDouble(i) <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Compute differences
     *
     * @param input The series being differenced
     * @param period Periodicity of the data
     * @param delta The differencing order. If delta is negative, the routine
     * searches the differencing order, based on the covariances at several
     * lags.
     * @param mean The series must be corrected for mean (not used if the
     * differencing order is automatically identified)
     * @return
     */
    public static DifferencingResults of(DoubleSeq input, int period, int delta, boolean mean) {
        if (delta < 0) {
            DoubleSeq del = input.delta(1, 1);
            del = del.removeMean();
            if (!checkStationarity(del, period)) {
                del = del.delta(1, 1);
                return new DifferencingResults(input, del, false);
            } else {
                return new DifferencingResults(input, del, true);
            }
        } else {
            DoubleSeq del = input.delta(1, delta);
            if (mean) {
                del = del.removeMean();
            }
            return new DifferencingResults(input, del, mean);
        }
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

    public DoubleSeq getRestrictedOriginal() {
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
