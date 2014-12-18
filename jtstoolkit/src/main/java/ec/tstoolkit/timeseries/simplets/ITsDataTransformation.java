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

package ec.tstoolkit.timeseries.simplets;

import ec.tstoolkit.design.Development;

/**
 * Interface for transformation of a time series
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ITsDataTransformation {

    /**
     * Contains the log of the Jacobian of a transformation of a time series.
     * @author Jean Palate
     */
    @Development(status = Development.Status.Alpha)
    public class LogJacobian {

        /**
         * The value of the Jacobian
         */
        public double value;
        /**
         * The starting position (included) of the transformation
         */
        /**
         * The ending position (excluded) of the transformation
         */
        public final int start, end;

        /**
         * Creates a log Jacobian with the limits of the transformation
         *
         * @param start Starting position (included)
         * @param end Ending position (excluded)
         */
        public LogJacobian(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    /**
     * Gives the converse transformation. Applying a transformation and its
     * converse should not change the initial series
     *
     * @return The converse transformation.
     */
    ITsDataTransformation converse();

    /**
     * Transforms a time series.
     *
     * @param data I/O parameter. The time series being transformed.
     * @param log I/O parameter. The log of the Jacobian of this transformation
     * @return True if the transformation was successful, false otherwise.
     */
    boolean transform(TsData data, LogJacobian logjacobian);
}
