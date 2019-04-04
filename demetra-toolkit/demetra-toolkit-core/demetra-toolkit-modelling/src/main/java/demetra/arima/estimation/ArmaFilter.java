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


package demetra.arima.estimation;

import demetra.arima.IArimaModel;
import demetra.arima.internal.AnsleyFilter;
import demetra.arima.internal.KalmanFilter;
import demetra.arima.internal.LjungBoxFilter;
import demetra.arima.internal.ModifiedLjungBoxFilter;
import demetra.data.DataBlock;
import demetra.design.Algorithm;
import demetra.design.Development;
import demetra.data.DoubleSeq;


/**
 * This interface defines methods used to compute the likelihood of data generated
 * by an arma model.
 * Suppose that y follows an ARMA model with covariance matrix V.
 * For computing the likelihood of y, we need log|V| and the ssq of
 * z=Ay, where A'A=V^-1 
 * 
 * The class defines the transformation z=A'y (filter),
 * the computation of the determinantal term (getLogDeterminant)
 * and returns the size of the (not computed) transformation A' (initialize)
 *  
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@Algorithm
public interface ArmaFilter {

    /**
     * Computes z=Ay where A'A is the inverse of the covariance matrix of the 
     * underlying Arma model. A is m x n
     * @param inrc y (len=n)
     * @param outrc z (len=m)
     */
    void apply(DoubleSeq inrc, DataBlock outrc);

    /**
     * Computes the log of the determinant of the covariance matrix
     * @return
     */
    double getLogDeterminant();

    /**
     * Initializes the filter
     * @param model The Arma model
     * @param length The length of the filter (=n, size of the covariance matrix)
     * @return The length of the filtered series (=m, number of rows of the transformation matrix A).
     * To be noted that the returned value is greater or (usually) equal to "length".
     */
    int prepare(final IArimaModel model, int length);
    
   
    public static ArmaFilter ansley(){
        return new AnsleyFilter();
    }
    public static ArmaFilter ljungBox(){
        return new LjungBoxFilter();
    }
    public static ArmaFilter kalman(){
        return new KalmanFilter();
    }
    public static ArmaFilter modifiedLjungBox(){
        return new ModifiedLjungBoxFilter();
    }
}
