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

import jdplus.arima.IArimaModel;
import demetra.regarima.RegArimaModel;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
public interface IGenericOutliersDetectionModule<T extends IArimaModel> {


    /**
     * Set the range where the outliers will be searched for
     *
     * @param n Number of observations. Should be called before any other method
     */
    void prepare(int n);
    /**
     * Set the range where the outliers will be searched for
     *
     * @param start First position (included)
     * @param end Last position (excluded)
     */
    void setBounds(int start, int end);
    
    /**
     * Exclude the specified outlier (position and type)
     * @param pos
     * @param type 
     */
    void exclude(int pos, int type);
    
    /**
     * Search outliers in the given RegArima model
     *
     * @param initialModel
     * @return True if the processing was successful (which doesn't mean that outliers were found), 
     * false otherwise.
     */
    boolean process(RegArimaModel<T> initialModel);

    /**
     * Returns the detected outliers
     * @return 
     */
    int[][] getOutliers();

}
