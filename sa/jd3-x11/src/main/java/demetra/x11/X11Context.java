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
package demetra.x11;

import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.sa.DecompositionMode;
import demetra.timeseries.TsDomain;
import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsDataToolkit;
import static demetra.timeseries.simplets.TsDataToolkit.add;
import static demetra.timeseries.simplets.TsDataToolkit.divide;
import static demetra.timeseries.simplets.TsDataToolkit.multiply;
import static demetra.timeseries.simplets.TsDataToolkit.subtract;

/**
 * A X11Context contains general information about a given X11Processing.
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
@lombok.Builder
public final class X11Context {

    DecompositionMode mode;
    int period;
    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    int nfcasts, nbcasts;

    /**
     *
     * @return
     */
    public int getForecastHorizon() {
        if (nfcasts >= 0) {
            return nfcasts;
        } else {
            return -nfcasts * period;
        }
    }

    /**
     *
     * @return
     */
    public int getBackcastHorizon() {
        if (nbcasts >= 0) {
            return nbcasts;
        } else {
            return -nbcasts * period;
        }
    }

    /**
     * Gets the default mean values, which is 1 or 0, following the
     * decomposition mode (1 in the case of multiplicative decomposition).
     *
     * @return 1 or 0.
     */
    public double getMean() {
        return mode == DecompositionMode.Multiplicative ? 1 : 0;
    }

    /**
     *
     * @return
     */
    public final boolean isMultiplicative() {
        return mode == DecompositionMode.Multiplicative;
    }

    /**
     * Subtracts/divides two time series, following the decomposition mode.
     * (divides in the case of multiplicative decomposition)
     *
     * @param l The left operand
     * @param r The right operand
     * @return A new time series is returned
     */
    public final TsData op(TsData l, TsData r) {
        if (mode != DecompositionMode.Multiplicative) {
            return subtract(l, r);
        } else {
            return divide(l, r);
        }
    }

    /**
     * Adds/multiplies two time series, following the decomposition mode.
     * (multiplies in the case of multiplicative decomposition)
     *
     * @param l The left operand
     * @param r The right operand
     * @return A new time series is returned
     */
    public final TsData invOp(TsData l, TsData r) {
        if (mode != DecompositionMode.Multiplicative) {
            return add(l, r);
        } else {
            return multiply(l, r);
        }
    }

    /**
     * Controls that the given series can be processed by X11
     *
     * @param s The considered time series
     * @throws X11Exception is thrown if the series is invalid. Invalid series
     * are: - Series with an annual frequency other than 4 or 12 - Series with
     * less than 3 years of observations - Series with negative values (in the
     * case of non-additive decomposition) - Series with missing values
     */
    public void check(final DoubleSequence s) {
        if (s.length() < 3 * period) {
            throw new X11Exception(X11Exception.ERR_LENGTH);
        }
        if (s.anyMatch(x -> Double.isInfinite(x))) {
            throw new X11Exception(X11Exception.ERR_MISSING);
        }

        if (mode != DecompositionMode.Additive) {
            if (s.anyMatch(x -> x <= 0)) {
                throw new X11Exception(X11Exception.ERR_NEG);
            }
        }
    }
}
