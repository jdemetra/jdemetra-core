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


package demetra.maths.functions.minpack;

import demetra.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public interface IEstimationProblem {
    /**
     * 
     * @param idx
     * @param bound
     */
    void bound(int idx, boolean bound);

    /**
     * 
     * @param midx
     * @param pidx
     * @return
     */
    double getMeasurementParialDerivative(int midx, int pidx);

    // measurements...
    /**
     * 
     * @return
     */
    int getMeasurementsCount();

    /**
     *
     * @param idx
     * @return
     */
    double getMeasurementValue(int idx);

    /**
     * 
     * @param idx
     * @return
     */
    double getMeasurementWheight(int idx);

    /**
     * 
     * @param idx
     * @return
     */
    double getParameterEstimate(int idx);

    // parameters
    /**
     * 
     * @return
     */
    int getParametersCount();

    /**
     * 
     * @param midx
     * @return
     */
    double getResidual(int midx);

    /**
     * 
     * @param midx
     * @return
     */
    double getTheoreticalValue(int midx);

    /**
     *
     * @param idx
     * @return
     */
    double getUnboundParameterEstimate(int idx);

    // unbound parameters
    /**
     * 
     * @return
     */
    int getUnboundParametersCount();

    /**
     * 
     * @param idx
     * @param ignore
     */
    void ignoreMeasurement(int idx, boolean ignore);

    /**
     * 
     * @param idx
     * @return
     */
    boolean isBound(int idx);

    /**
     * 
     * @param idx
     * @return
     */
    boolean isMeasurementIgnore(int idx);

    /**
     * 
     * @param idx
     * @param val
     */
    void setParameterEstimate(int idx, double val);

    /**
     * 
     * @param idx
     * @param val
     */
    void setUnboundParameterEstimate(int idx, double val);

    boolean compute();
    
    IEstimationProblem save();
}
