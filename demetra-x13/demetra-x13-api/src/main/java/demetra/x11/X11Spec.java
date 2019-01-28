/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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

import demetra.design.Development;
import demetra.sa.DecompositionMode;

/**
 *
 * @author Frank Osaer, Jean Palate, BAYENSK
 */
@Development(status = Development.Status.Beta)
@lombok.Data
public final class X11Spec implements Cloneable {

    public static final double DEF_LSIGMA = 1.5, DEF_USIGMA = 2.5;
    public static final int DEF_FCASTS = -1, DEF_BCASTS = 0;

    private DecompositionMode mode_ = DecompositionMode.Undefined;
    private boolean seasonal_ = true;
    private SeasonalFilterOption[] filters_;
    /**
     * Parameters for extreme values detection [sigmalim option in X12-arima].
     *
     * @param lsigma Lower sigma value for extreme values detection
     * @param usigma Upper sigma value for extreme values detection lsigma
     * should be lower than usigma and higher than .5.
     */
    private double lsigma_ = DEF_LSIGMA, usigma_ = DEF_USIGMA;
    /**
     * Length of the Henderson filter [trendma option in X12-Arima]. When the
     * length is 0, an automatic estimation of the length of the Henderson
     * filter is computed by the algorithm.
     */
    private int henderson_ = 0;
    /**
     * Number of forecasts/backcasts used in X11. By default, 0. When
     * pre-processing is used, the number of forecasts/backcasts corresponds
     * usually to 1 year. Negative values correspond to full years (-3 = 3
     * years)
     */
    private int fcasts_ = DEF_FCASTS, bcasts_ = DEF_BCASTS;
    /**
     * Option of Calendarsigma[X12], specifies the calculation of the standard
     * error calculation used for outlier detection in the X11 part
     */
    private CalendarSigma calendarsigma_ = CalendarSigma.None;
    private SigmavecOption[] sigmavec_;
    private boolean excludefcast_ = false;
    private BiasCorrection bias = BiasCorrection.Legacy;

    private static final X11Spec DEFAULT = new X11Spec();

    public boolean isDefault() {
        return this.equals(DEFAULT);

    }

    public void setCalendarSigma(CalendarSigma calendarsigma) {
        calendarsigma_ = calendarsigma;
    }

    public void setSigmavec(SigmavecOption[] sigmavec) {
        sigmavec_ = sigmavec.clone();
    }

    /**
     * Set the decomposition mode of X11
     *
     * @param mode
     */
    public void setMode(DecompositionMode mode) {
        mode_ = mode;
    }

    /**
     * Parameters for extreme values detection [sigmalim option in X12-arima].
     *
     * @param lsigma Lower sigma value for extreme values detection
     * @param usigma Upper sigma value for extreme values detection lsigma
     * should be lower than usigma and higher than .5. A X11Exception is thrown
     * if lsigma and/or usigma are invalid.
     */
    public void setSigma(double lsigma, double usigma) {
        if (usigma <= lsigma || lsigma <= 0.5) {
            throw new X11Exception("Invalid sigma options");
        }
        lsigma_ = lsigma;
        usigma_ = usigma;
    }

    public void setLowerSigma(double lsigma) {
        if (usigma_ <= lsigma) {
            setSigma(lsigma, lsigma + .5);
        } else {
            setSigma(lsigma, usigma_);
        }
    }

    public void setUpperSigma(double usigma) {
        if (usigma <= lsigma_) {
            setSigma(usigma - .5, usigma);
        } else {
            setSigma(lsigma_, usigma);
        }
    }

    /**
     * Length of the Henderson filter [trendma option in X12-arima]
     *
     * @param len Length of the Henderson filter. When the length is 0, an
     * automatic estimation is made by the program. Otherwise, the length should
     * be an odd number in the range [1, 101].
     */
    public void setHendersonFilterLength(int len) {
        if (len < 0 || len > 101 || (len != 0 && len % 2 == 0)) {
            throw new X11Exception("Invalid henderson length");
        }
        henderson_ = len;
    }

    @Override
    public X11Spec clone() {
        try {
            X11Spec cspec = (X11Spec) super.clone();
            if (filters_ != null) {
                cspec.filters_ = filters_.clone();
            }
            if (sigmavec_ != null) {
                cspec.sigmavec_ = sigmavec_.clone();
            }
            return cspec;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

}
