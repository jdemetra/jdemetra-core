/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.regarima.ami;

import demetra.math.Constants;
import jdplus.arima.IArimaModel;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.regarima.RegArimaModel;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
public interface GenericOutliersDetection<T extends IArimaModel> {

    /**
     * Prepare outliers detection. Should be called before any other method
     *
     * @param n Number of observations
     */
    void prepare(int n);

    /**
     * Set the range where the outliers will be searched for.
     * The bounds must be in [0, n]
     *
     * @param start First position (included)
     * @param end Last position (excluded)
     */
    void setBounds(int start, int end);

    /**
     * Exclude the specified outlier (position and type)
     *
     * @param pos Position of the outlier
     * @param type Type of the outlier
     */
    void exclude(int pos, int type);

    /**
     * Search outliers in the given RegArima model
     *
     * @param initialModel
     * @return True if the processing was successful (which doesn't mean that
     * outliers were found),
     * false otherwise.
     */
    boolean process(RegArimaModel<T> initialModel, IArimaMapping<T> mapping);

    /**
     * Returns the detected outliers
     *
     * @return
     */
    int[][] getOutliers();

    /**
     * Outlier critical value using the Ljung algorithm as given in
     * Ljung, G. M. (1993). On outlier detection in time series.
     * Journal of Royal Statistical Society B 55, 559-567.
     * Solution proposed by Brian Monsell (LBS), January 2022
     *
     * @param nobs Number of observations
     * @param alpha alpha for critical value (typically, alpha=0.01)
     * @return The actual critical value
     */
    public static double criticalValue(int nobs, double alpha) {
        double pmod =  2 - Math.sqrt(1 + alpha);
        double acv = Math.sqrt(2 * Math.log(nobs));
        double bcv = acv - (Math.log(Math.log(nobs)) + Math.log(2 * Constants.TWOPI)) / (2 * acv);
        double xcv = -Math.log(-0.5 * Math.log(pmod));
        return (xcv / acv) + bcv;
    }
}
