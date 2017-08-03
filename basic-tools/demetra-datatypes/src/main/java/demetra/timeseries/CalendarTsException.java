/*
* Copyright 2017 National Bank of Belgium
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
package demetra.timeseries;

import demetra.design.Development;

/**
 *
 * @author Mats Maggi
 */
@Development(status = Development.Status.Alpha)
public class CalendarTsException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -1747718409573441163L;

    /**
     *
     */
    public static final String INCOMPATIBLE_DOMAIN = "Incompatible domain", INCOMPATIBLE_FREQ = "Incompatible frequencies";

    /**
     *
     */
    public CalendarTsException() {
    }

    /**
     * Constructor for a calendar time series exception with a specific message
     *
     * @param msg Message of the exception
     */
    public CalendarTsException(String msg) {
        super(msg);
    }

    /**
     * Constructor for a calendar time series exception with a specific message
     * and inner exception
     *
     * @param message Message of the exception
     * @param innerException Inner exception
     */
    public CalendarTsException(final String message, final Exception innerException) {
        super(message, innerException);
    }
}
