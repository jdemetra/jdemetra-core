/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.seats;

import demetra.design.Development;
import demetra.sa.SaException;


/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public class SeatsException extends SaException {

    public final static String ERR_PERIOD = "Undefined period";

    public final static String ERR_MODEL = "Invalid model";

    public final static String ERR_DECOMP = "Invalid decomposition";

    public final static String ERR_ESTIMATION = "Model estimation failed";

    public static final String ERR_LENGTH = "Not enough observations", 
            ERR_MISSING = "Missing values are not allowed", ERR_NODATA = "No data";

    /**
     *
     */
    public SeatsException() {
    }

    // / <summary>
    // / Constructor for a time series exception with a specific message
    // / </summary>
    // / <param name="msg">Message of the exception</param>
    /**
     * 
     * @param msg
     */
    public SeatsException(String msg) {
	super(msg);
    }

    /**
     * 
     * @param message
     * @param innerException
     */
    public SeatsException(final String message, final Exception innerException) {
	super(message, innerException);
    }

}
