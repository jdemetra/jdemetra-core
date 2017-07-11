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
package ec.satoolkit.x11;

import ec.satoolkit.DecompositionMode;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 * A X11Context contains general information about a given X11Processing.
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class X11Context {

    private DecompositionMode mode;
    private TsDomain edomain;
    private final int nfcasts, nbcasts;

    /**
     * Creates a new context
     *
     * @param mode    The decomposition mode used in the processing
     * @param nfcasts The number of forecasts that will be used during the
     *                processing. 
     */
    @Deprecated
    public X11Context(final DecompositionMode mode, final int nfcasts) {
        this.mode = mode;
        this.nfcasts = nfcasts;
        this.nbcasts = 0;
    }

    /**
     * Creates a new context
     *
     * @param mode    The decomposition mode used in the processing
     * @param nbcasts The number of backcasts that will be used during the
     *                processing. 
     * @param nfcasts The number of forecasts that will be used during the
     *                processing. 
     */
    public X11Context(final DecompositionMode mode, final int nbcasts, final int nfcasts) {
        this.mode = mode;
        this.nfcasts = nfcasts;
        this.nbcasts = nbcasts;
    }
    /**
     *
     * @return
     */
    public TsDomain getEstimationDomain() {
        return edomain;
    }

    /**
     *
     * @return
     */
    public int getForecastHorizon() {
        if (nfcasts >= 0) {
            return nfcasts;
        } else {
            int freq = edomain.getFrequency().intValue();
            return -nfcasts * freq;
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
            int freq = edomain.getFrequency().intValue();
            return -nbcasts * freq;
        }
    }
    /**
     * Gets the current annual frequency, as an integer
     *
     * @return 4 or 12
     */
    public int getFrequency() {
        return edomain.getFrequency().intValue();
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
     * Gets the current decomposition mode
     *
     * @return The decomposition mode.
     */
    public DecompositionMode getMode() {
        return mode;
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
            return TsData.subtract(l, r);
        } else {
            return TsData.divide(l, r);
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
            return TsData.add(l, r);
        } else {
            return TsData.multiply(l, r);
        }
    }

    /**
     * Controls that the given series can be processed by X11
     *
     * @param s The considered time series
     * @throws X11Exception is thrown if the series is invalid. Invalid series
     *                      are: - Series with an annual frequency other than 4 or 12 - Series with
     *                      less than 3 years of observations - Series with negative values (in the
     *                      case of non-additive decomposition) - Series with missing values
     */
    public void check(final TsData s) {
        edomain = s.getDomain();

        int freq = s.getFrequency().intValue();
        if (freq != 4 && freq != 12) {
            throw new X11Exception(X11Exception.ERR_FREQ);
        }
        if (s.getLength() < 3 * freq) {
            throw new X11Exception(X11Exception.ERR_LENGTH);
        }
        if (s.hasMissingValues()) {
            throw new X11Exception(X11Exception.ERR_MISSING);
        }

        if (mode != DecompositionMode.Additive) {
            double[] vals = s.internalStorage();
            for (int i = 0; i < vals.length; ++i) {
                if (vals[i] <= 0) {
                    throw new X11Exception(X11Exception.ERR_NEG);
                }
            }
        }
    }

    /**
     * Set the decomposition mode of the context
     *
     * @param mode
     */
    public void setMode(DecompositionMode mode) {
        this.mode = mode;
    }
}
