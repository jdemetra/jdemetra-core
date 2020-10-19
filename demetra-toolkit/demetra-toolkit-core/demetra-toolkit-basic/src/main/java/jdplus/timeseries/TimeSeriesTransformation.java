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

package jdplus.timeseries;

import jdplus.data.transformation.LogJacobian;
import demetra.design.Development;
import demetra.timeseries.TimeSeriesData;
import demetra.timeseries.TimeSeriesObs;
import demetra.timeseries.TimeSeriesInterval;

/**
 * Interface for transformation of a time series
 *
 * @author Jean Palate
 * @param <P>
 * @param <O>
 * @param <S>
 */
@Development(status = Development.Status.Release)
public interface TimeSeriesTransformation<P extends TimeSeriesInterval<?>, O extends TimeSeriesObs<P>, S extends TimeSeriesData<P, O>> {


    /**
     * Gives the converse transformation. Applying a transformation and its
     * converse should not change the initial series
     *
     * @return The converse transformation.
     */
    TimeSeriesTransformation<P, O, S> converse();

    /**
     * Transforms a time series.
     *
     * @param data The data being transformed.
     * @param logjacobian I/O parameter. The log of the Jacobian of this transformation
     * @return The transformed data. Null if the transformation was not successful
     */
    S transform(S data, LogJacobian logjacobian);
    
    double transform(P period, double value);
}
