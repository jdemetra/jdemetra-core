/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.modelling;

import demetra.data.DoubleSeq;
import demetra.stats.AutoCovariances;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author palatej
 */
@lombok.Getter
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DifferencingResult {

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
    public static DifferencingResult of(DoubleSeq input, int period, int delta, boolean mean) {
        if (delta < 0) {
            DoubleSeq del = input.delta(1, 1);
            del = del.removeMean();
            if (!checkStationarity(del, period)) {
                del = del.delta(1, 1);
                return new DifferencingResult(input, del, false);
            } else {
                return new DifferencingResult(input, del, true);
            }
        } else {
            DoubleSeq del = input.delta(1, delta);
            if (mean) {
                del = del.removeMean();
            }
            return new DifferencingResult(input, del, mean);
        }
    }
    private final DoubleSeq original;
    private final DoubleSeq differenced;
    private final boolean meanCorrection;

    public int getDifferencingOrder() {
        return original.length() - differenced.length();
    }

    public DoubleSeq getRestrictedOriginal() {
        return original.drop(getDifferencingOrder(), 0);
    }

}
