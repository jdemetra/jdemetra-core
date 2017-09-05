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
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TsException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -1747718409573441163L;

    /**
     *
     */
    public static final String INVALID_YEAR = "Invalid year",
            INVALID_MONTH = "Invalid month", INVALID_DAY = "Invalid day",
            INVALID_FREQ = "Invalid frequnecy",
            INCOMPATIBLE_FREQ = "Incompatible frequencies",
            INCOMPATIBLE_DOMAIN = "Incompatible domains",
            INVALID_PERIOD = "Invalid period",
            INVALID_OPERATION ="Invalid operation",
            INVALID_AGGREGATIONMODE = "Invalid aggregation mode",
            DOMAIN_EMPTY = "Empty domain",
            TS_EMPTY = "Empty series";

    // / <summary>Default constructor</summary>
    /**
     *
     */
    public TsException() {
    }

    // / <summary>
    // / Constructor for a time series exception with a specific message
    // / </summary>
    // / <param name="msg">Message of the exception</param>
    /**
     *
     * @param msg
     */
    public TsException(String msg) {
        super(msg);
    }

    /**
     *
     * @param message
     * @param innerException
     */
    public TsException(final String message, final Exception innerException) {
        super(message, innerException);
    }
}
