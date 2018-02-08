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
package demetra.regarima.ami;

import demetra.arima.IArimaModel;
import demetra.regarima.RegArimaModel;
import demetra.timeseries.regression.IOutlier;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
public interface IOutliersDetectionModule<T extends IArimaModel> {

    /**
     * Search outliers in the given RegArima model
     *
     * @param initialModel
     * @return
     */
    boolean process(RegArimaModel<T> initialModel);

    /**
     * Set the range where the outliers will be searched for
     *
     * @param start First position (included)
     * @param end Last position (excluded)
     */
    void setBounds(int start, int end);
    
    /**
     * Set the selectivity level of the outliers detection module.
     * The initial ("normal") level is 0.
     *
     * @param level Selectivity level. 0 by default. Should be negative
     */
    void setSelectivity(int level);

    /**
     * Reduce the selectivity of the outliers detection module
     *
     * @return True if the selectivity has been successfully reduced
     */
    boolean reduceSelectivity();

    int getSelectivity();

    /**
     * Exclude the specified outlier (position and type)
     * @param pos
     * @param type 
     */
    void exclude(int pos, int type);
    
    /**
     * Returns the detected outliers
     * @return 
     */
    int[][] getOutliers();

}
