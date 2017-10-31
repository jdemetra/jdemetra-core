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

import demetra.design.Development;
import demetra.timeseries.simplets.TsData;


/**
 * Corrects a time series for its extreme values.
 *
 * The extreme values corrector can be used in two different ways:
 *
 * In a first way (tables B4, B4g, B9, B9g) 1. xtr.analyse(b4d); 2.
 * b4=xtr.computeCorecctions(b3); 3. b4g=xtr.applyCorrections(b3, b4);
 *
 * idem with b8, b9, b9g
 *
 * In a second way (tables X17, X20), use: 1. xtr.analyse(s) 2.
 * x17=xtr.getObservationWeights(); 3. x20=xtr.getCorrectionsFactors();
 *
 * @author Frank Osaer, Jean Palate, Christiane Hofer
 */
@Development(status = Development.Status.Release)
public interface IExtremeValuesCorrector extends IX11Algorithm {

    /**
     * Detects the extremes values of a given series
     *
     * @param s The considered series
     * @return The number of abnormal values (= weight different from 1).
     */
    int analyse(TsData s);

    /**
     * Computes the corrections for a given series (tables B4, B9)
     *
     * @param s The series being corrected
     * @return A new time series is always returned. It will contain missing
     * values for the periods that should not be corrected and the actual
     * corrections for the other periods
     */
    TsData computeCorrections(TsData s);

    /**
     * Apply the corrections computed with the computeCorrections method (tables
     * B4g, B9g)
     *
     * @param s The series that must be corrected
     * @param corrections
     * @return The corrected series
     */
    TsData applyCorrections(TsData s, TsData corrections);

    /**
     * Gets the weights of the observations, which are used in the tables B17,
     * C17 The weights of the observations should be in the range [0, 1], O
     * corresponding to an highly extreme value and 1 to a normal observation.
     *
     * @return The weights of the observations
     */
    TsData getObservationWeights();

    /**
     * Gets the correction factors, which are used in the tables B20, C20
     *
     * @return
     */
    TsData getCorrectionFactors();

    /**
     * Sets the limits for the detection of extreme values.
     *
     * @param lsig The low sigma value
     * @param usig The high sigma value
     */
    public void setSigma(double lsig, double usig);

    /**
     *
     * @param isExcludefcast true if the forcast should be excluded for the
     * calculation of the Standarddeviation of the outliers
     */
    void setExcludefcast(boolean isExcludefcast);

    /**
     *
     * @return true, if the forcast is excluded for the calculation of the
     * Standarddeviation of the outliers
     */
    boolean getExcludefcast();

    /**
     * Should not be used. Information in context
     *
     * @param forcasthorizont in numnber of periods
     */
    @Deprecated
    default void setForecasthorizont(int forcasthorizont) {
        throw new java.lang.UnsupportedOperationException();
    }

    /**
     *
     * @return number of periods forecasted
     */
    @Deprecated
    default int getForecasthorizont() {
        throw new java.lang.UnsupportedOperationException();
    }

}
